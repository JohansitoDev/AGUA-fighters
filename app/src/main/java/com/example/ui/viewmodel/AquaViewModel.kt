package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.FishRoster
import com.example.data.model.FishSpecies
import com.example.data.model.LeaderboardEntry
import com.example.data.model.MatchRoom
import com.example.data.model.UserProfile
import com.example.data.repository.AquaRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppScreen {
  ROSTER,
  DETAIL,
  ARENA,
  LOBBY,
  LEADERBOARD
}

class AquaViewModel(application: Application) : AndroidViewModel(application) {

  private val repository = AquaRepository(application)

  private val _currentScreen = MutableStateFlow(AppScreen.ROSTER)
  val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

  private val _selectedFish = MutableStateFlow(FishRoster.ALL_FISHES.first())
  val selectedFish: StateFlow<FishSpecies> = _selectedFish.asStateFlow()

  private val _opponentFish = MutableStateFlow(FishRoster.ALL_FISHES[1])
  val opponentFish: StateFlow<FishSpecies> = _opponentFish.asStateFlow()

  private val _isMultiplayerMatch = MutableStateFlow(false)
  val isMultiplayerMatch: StateFlow<Boolean> = _isMultiplayerMatch.asStateFlow()

  private val _currentMatchId = MutableStateFlow<String?>(null)
  val currentMatchId: StateFlow<String?> = _currentMatchId.asStateFlow()

  private val _matchmakingStatus = MutableStateFlow("Searching for opponent...")
  val matchmakingStatus: StateFlow<String> = _matchmakingStatus.asStateFlow()

  private val _toastMessage = MutableStateFlow<String?>(null)
  val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

  val userProfile: StateFlow<UserProfile?> = repository.observeUserProfile(
    repository.currentUserId() ?: "offline"
  ).catch { emit(null) }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = null
    )

  val leaderboard: StateFlow<List<LeaderboardEntry>> = repository.observeLeaderboard()
    .catch { emit(emptyList()) }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList()
    )

  init {
    initializeProfile()
  }

  fun initializeProfile() {
    viewModelScope.launch {
      if (Firebase.auth.currentUser != null) {
        val result = repository.ensureProfileExists()
        result.onSuccess { profile ->
          val fish = FishRoster.getById(profile.selectedFishId)
          _selectedFish.value = fish
        }
      }
    }
  }

  fun navigateTo(screen: AppScreen) {
    _currentScreen.value = screen
  }

  fun selectFishForInspection(fish: FishSpecies) {
    _selectedFish.value = fish
    _currentScreen.value = AppScreen.DETAIL
  }

  fun equipSelectedFish() {
    val fish = _selectedFish.value
    viewModelScope.launch {
      repository.selectFish(fish.id)
      _toastMessage.value = "${fish.name} equipped as primary fighter!"
    }
  }

  fun unlockSelectedFish() {
    val fish = _selectedFish.value
    viewModelScope.launch {
      val res = repository.unlockFish(fish.id, fish.costCoins)
      if (res.isSuccess) {
        _toastMessage.value = "Unlocked ${fish.name}!"
      } else {
        _toastMessage.value = res.exceptionOrNull()?.message ?: "Failed to unlock fish"
      }
    }
  }

  fun startSoloBattle(opponent: FishSpecies = FishRoster.ALL_FISHES.random()) {
    _opponentFish.value = if (opponent.id == _selectedFish.value.id) {
      FishRoster.ALL_FISHES.firstOrNull { it.id != opponent.id } ?: opponent
    } else {
      opponent
    }
    _isMultiplayerMatch.value = false
    _currentMatchId.value = null
    _currentScreen.value = AppScreen.ARENA
  }

  fun startQuickMatch() {
    _isMultiplayerMatch.value = true
    _currentScreen.value = AppScreen.LOBBY
    _matchmakingStatus.value = "Searching for active battle rooms..."

    viewModelScope.launch {
      val searchResult = repository.findWaitingRoom()
      val waitingRoom = searchResult.getOrNull()

      if (waitingRoom != null) {
        _matchmakingStatus.value = "Opponent found: ${waitingRoom.hostName}! Joining room..."
        val joinRes = repository.joinMatchRoom(waitingRoom.id, _selectedFish.value.id)
        if (joinRes.isSuccess) {
          _currentMatchId.value = waitingRoom.id
          _opponentFish.value = FishRoster.getById(waitingRoom.hostFish)
          _currentScreen.value = AppScreen.ARENA
        } else {
          fallbackToAI()
        }
      } else {
        _matchmakingStatus.value = "Creating online battle arena..."
        val createRes = repository.createMatchRoom(_selectedFish.value.id)
        if (createRes.isSuccess) {
          val matchId = createRes.getOrThrow()
          _currentMatchId.value = matchId
          _matchmakingStatus.value = "Waiting for challenger to join (or Bot fallback in 4s)..."

          // Observe for guest
          viewModelScope.launch {
            repository.observeMatchRoom(matchId).collect { room ->
              if (room != null && room.guestId != null && room.status == "active") {
                _opponentFish.value = FishRoster.getById(room.guestFish ?: "warsee_fish")
                _currentScreen.value = AppScreen.ARENA
              }
            }
          }

          // If no player joins within 4 seconds, deploy online Bot opponent to match
          kotlinx.coroutines.delay(4000)
          if (_currentScreen.value == AppScreen.LOBBY) {
            val botOpponent = FishRoster.ALL_FISHES.filter { it.id != _selectedFish.value.id }.random()
            _opponentFish.value = botOpponent
            _currentScreen.value = AppScreen.ARENA
          }
        } else {
          fallbackToAI()
        }
      }
    }
  }

  private fun fallbackToAI() {
    _matchmakingStatus.value = "Challenger matched! Entering Arena..."
    val botOpponent = FishRoster.ALL_FISHES.filter { it.id != _selectedFish.value.id }.random()
    _opponentFish.value = botOpponent
    _currentScreen.value = AppScreen.ARENA
  }

  fun onBattleFinished(won: Boolean) {
    viewModelScope.launch {
      val trophiesDelta = if (won) 35 else -12
      val coinsDelta = if (won) 60 else 15
      val xpDelta = if (won) 50 else 20

      repository.recordMatchResult(
        won = won,
        trophiesDelta = trophiesDelta,
        coinsDelta = coinsDelta,
        xpDelta = xpDelta
      )

      val matchId = _currentMatchId.value
      if (matchId != null) {
        val winnerId = if (won) (repository.currentUserId() ?: "") else "opponent"
        repository.finishMatch(matchId, winnerId)
      }
    }
  }

  fun clearToast() {
    _toastMessage.value = null
  }
}
