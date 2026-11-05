package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.auth.AuthManager
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.BattleArenaScreen
import com.example.ui.screens.FighterDetailScreen
import com.example.ui.screens.LeaderboardScreen
import com.example.ui.screens.OnlineLobbyScreen
import com.example.ui.screens.RosterHomeScreen
import com.example.ui.theme.AquaBlack
import com.example.ui.theme.AquaFightersTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.AquaViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      AquaFightersTheme {
        AquaAppRoot()
      }
    }
  }
}

@Composable
fun AquaAppRoot() {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  val credentialManager = remember { CredentialManager.create(context) }

  var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }

  // Listen to Auth State
  DisposableEffect(Unit) {
    val listener = FirebaseAuth.AuthStateListener { auth ->
      currentUser = auth.currentUser
    }
    Firebase.auth.addAuthStateListener(listener)
    onDispose {
      Firebase.auth.removeAuthStateListener(listener)
    }
  }

  // Silent Auto Sign-In on Launch
  LaunchedEffect(Unit) {
    if (currentUser == null) {
      AuthManager.attemptAutoSignIn(
        context = context,
        credentialManager = credentialManager,
        onAuthSuccess = { currentUser = Firebase.auth.currentUser },
        onUnauthenticated = { /* stay on AuthScreen */ },
        scope = coroutineScope
      )
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(AquaBlack)
  ) {
    if (currentUser == null) {
      AuthScreen(
        onAuthSuccess = {
          currentUser = Firebase.auth.currentUser
        }
      )
    } else {
      AquaMainContent(
        onSignOut = {
          AuthManager.signOut(
            context = context,
            credentialManager = credentialManager,
            onSignOutComplete = {
              currentUser = null
            },
            scope = coroutineScope
          )
        }
      )
    }
  }
}

@Composable
fun AquaMainContent(
  onSignOut: () -> Unit,
  viewModel: AquaViewModel = viewModel()
) {
  val context = LocalContext.current
  val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
  val selectedFish by viewModel.selectedFish.collectAsStateWithLifecycle()
  val opponentFish by viewModel.opponentFish.collectAsStateWithLifecycle()
  val profile by viewModel.userProfile.collectAsStateWithLifecycle()
  val leaderboard by viewModel.leaderboard.collectAsStateWithLifecycle()
  val matchmakingStatus by viewModel.matchmakingStatus.collectAsStateWithLifecycle()
  val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()

  LaunchedEffect(toastMessage) {
    toastMessage?.let {
      Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
      viewModel.clearToast()
    }
  }

  when (currentScreen) {
    AppScreen.ROSTER -> {
      RosterHomeScreen(
        viewModel = viewModel,
        profile = profile,
        onSignOut = onSignOut
      )
    }

    AppScreen.DETAIL -> {
      BackHandler {
        viewModel.navigateTo(AppScreen.ROSTER)
      }
      FighterDetailScreen(
        viewModel = viewModel,
        fish = selectedFish,
        profile = profile
      )
    }

    AppScreen.ARENA -> {
      BackHandler {
        viewModel.navigateTo(AppScreen.ROSTER)
      }
      BattleArenaScreen(
        viewModel = viewModel,
        playerFish = selectedFish,
        opponentFish = opponentFish
      )
    }

    AppScreen.LOBBY -> {
      BackHandler {
        viewModel.navigateTo(AppScreen.ROSTER)
      }
      OnlineLobbyScreen(
        viewModel = viewModel,
        selectedFish = selectedFish,
        matchmakingStatus = matchmakingStatus
      )
    }

    AppScreen.LEADERBOARD -> {
      BackHandler {
        viewModel.navigateTo(AppScreen.ROSTER)
      }
      LeaderboardScreen(
        viewModel = viewModel,
        leaderboard = leaderboard,
        profile = profile
      )
    }
  }
}
