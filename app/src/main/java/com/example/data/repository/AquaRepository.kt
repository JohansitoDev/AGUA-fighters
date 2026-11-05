package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.auth.AuthManager
import com.example.data.model.FishRoster
import com.example.data.model.LeaderboardEntry
import com.example.data.model.MatchRoom
import com.example.data.model.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class AquaRepository(private val db: FirebaseFirestore) {

  constructor(context: Context) : this(
    FirebaseFirestore.getInstance(
      context.applicationContext.getString(R.string.firestore_database_id)
    )
  )

  private val auth = Firebase.auth

  fun requireUserId(): String {
    return auth.currentUser?.uid
      ?: if (AuthManager.isLocalGuest) "guest_user"
      else throw IllegalStateException("User must be signed in before accessing Firestore.")
  }

  fun currentUserId(): String? = auth.currentUser?.uid ?: if (AuthManager.isLocalGuest) "guest_user" else null
  fun currentDisplayName(): String = auth.currentUser?.displayName?.ifBlank { null } ?: (if (AuthManager.isLocalGuest) "Guest Player" else "Jon Karter")

  // 1. User Profile Observation
  fun observeUserProfile(userId: String): Flow<UserProfile?> = flow {
    val path = "users/$userId"
    emitAll(
      db.collection("users").document(userId)
        .snapshots()
        .map { snapshot ->
          snapshot.toObject(UserProfile::class.java)
        }
        .catch { error ->
          val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
          if (firestoreError == null && error is CancellationException) throw error
          val exceptionToLog = firestoreError ?: (error as? Exception)
          if (exceptionToLog != null) {
            handleFirestoreError(exceptionToLog, OperationType.GET, path)
          }
          throw error
        }
    )
  }

  suspend fun ensureProfileExists(): Result<UserProfile> {
    val uid = requireUserId()
    val name = currentDisplayName()
    val docRef = db.collection("users").document(uid)
    return try {
      val snapshot = docRef.get().await()
      if (snapshot.exists()) {
        val profile = snapshot.toObject(UserProfile::class.java) ?: UserProfile(userId = uid, username = name)
        Result.success(profile)
      } else {
        val newProfile = UserProfile(
          userId = uid,
          username = name,
          avatarFish = "oranda_goldfish",
          level = 1,
          xp = 0,
          coins = 200,
          trophies = 100,
          wins = 0,
          losses = 0,
          unlockedFishes = listOf("oranda_goldfish"),
          selectedFishId = "oranda_goldfish"
        )
        val payload = mapOf(
          "userId" to uid,
          "username" to name,
          "avatarFish" to "oranda_goldfish",
          "level" to 1,
          "xp" to 0,
          "coins" to 200,
          "trophies" to 100,
          "wins" to 0,
          "losses" to 0,
          "unlockedFishes" to listOf("oranda_goldfish"),
          "selectedFishId" to "oranda_goldfish",
          "createdAt" to FieldValue.serverTimestamp()
        )
        docRef.set(payload).await()
        updateLeaderboardEntry(name, 100, 0, 1, "oranda_goldfish")
        Result.success(newProfile)
      }
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, docRef.path)
      Result.failure(e)
    }
  }

  suspend fun selectFish(fishId: String): Result<Unit> {
    val uid = requireUserId()
    val docRef = db.collection("users").document(uid)
    return try {
      docRef.update(
        mapOf(
          "selectedFishId" to fishId,
          "avatarFish" to fishId,
          "updatedAt" to FieldValue.serverTimestamp()
        )
      ).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docRef.path)
      Result.failure(e)
    }
  }

  suspend fun unlockFish(fishId: String, costCoins: Int): Result<Unit> {
    val uid = requireUserId()
    val docRef = db.collection("users").document(uid)
    return try {
      val snap = docRef.get().await()
      val currentCoins = snap.getLong("coins")?.toInt() ?: 0
      if (currentCoins < costCoins) {
        return Result.failure(IllegalStateException("Not enough battle coins ($currentCoins / $costCoins)"))
      }
      docRef.update(
        mapOf(
          "coins" to (currentCoins - costCoins),
          "unlockedFishes" to FieldValue.arrayUnion(fishId),
          "selectedFishId" to fishId,
          "updatedAt" to FieldValue.serverTimestamp()
        )
      ).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docRef.path)
      Result.failure(e)
    }
  }

  suspend fun recordMatchResult(won: Boolean, trophiesDelta: Int, coinsDelta: Int, xpDelta: Int): Result<Unit> {
    val uid = requireUserId()
    val docRef = db.collection("users").document(uid)
    return try {
      val snap = docRef.get().await()
      val currentCoins = snap.getLong("coins")?.toInt() ?: 200
      val currentTrophies = snap.getLong("trophies")?.toInt() ?: 100
      val currentXp = snap.getLong("xp")?.toInt() ?: 0
      val currentLevel = snap.getLong("level")?.toInt() ?: 1
      val currentWins = snap.getLong("wins")?.toInt() ?: 0
      val currentLosses = snap.getLong("losses")?.toInt() ?: 0
      val username = snap.getString("username") ?: currentDisplayName()
      val selectedFishId = snap.getString("selectedFishId") ?: "oranda_goldfish"

      val newCoins = (currentCoins + coinsDelta).coerceAtLeast(0)
      val newTrophies = (currentTrophies + trophiesDelta).coerceAtLeast(0)
      val newXp = currentXp + xpDelta
      val newLevel = 1 + (newXp / 200)
      val newWins = if (won) currentWins + 1 else currentWins
      val newLosses = if (!won) currentLosses + 1 else currentLosses

      docRef.update(
        mapOf(
          "coins" to newCoins,
          "trophies" to newTrophies,
          "xp" to newXp,
          "level" to newLevel,
          "wins" to newWins,
          "losses" to newLosses,
          "updatedAt" to FieldValue.serverTimestamp()
        )
      ).await()

      updateLeaderboardEntry(username, newTrophies, newWins, newLevel, selectedFishId)
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docRef.path)
      Result.failure(e)
    }
  }

  suspend fun updateLeaderboardEntry(username: String, trophies: Int, wins: Int, level: Int, favoriteFish: String): Result<Unit> {
    val uid = requireUserId()
    val docRef = db.collection("leaderboard").document(uid)
    return try {
      val payload = mapOf(
        "userId" to uid,
        "username" to username,
        "trophies" to trophies,
        "wins" to wins,
        "level" to level,
        "favoriteFish" to favoriteFish,
        "updatedAt" to FieldValue.serverTimestamp()
      )
      docRef.set(payload).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, docRef.path)
      Result.failure(e)
    }
  }

  // 2. Global Leaderboard Observation
  fun observeLeaderboard(): Flow<List<LeaderboardEntry>> = flow {
    val path = "leaderboard"
    emitAll(
      db.collection(path)
        .orderBy("trophies", Query.Direction.DESCENDING)
        .limit(50)
        .snapshots()
        .map { snapshot ->
          snapshot.toObjects(LeaderboardEntry::class.java)
        }
        .catch { error ->
          val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
          if (firestoreError == null && error is CancellationException) throw error
          val exceptionToLog = firestoreError ?: (error as? Exception)
          if (exceptionToLog != null) {
            handleFirestoreError(exceptionToLog, OperationType.LIST, path)
          }
          throw error
        }
    )
  }

  // 3. Multiplayer Battle Room
  suspend fun createMatchRoom(hostFishId: String): Result<String> {
    val uid = requireUserId()
    val hostName = currentDisplayName()
    val matchId = "battle_" + UUID.randomUUID().toString().take(8)
    val docRef = db.collection("matches").document(matchId)
    val fish = FishRoster.getById(hostFishId)

    return try {
      val payload = mapOf(
        "id" to matchId,
        "hostId" to uid,
        "hostName" to hostName,
        "hostFish" to hostFishId,
        "hostHp" to fish.baseHp,
        "hostMaxHp" to fish.baseHp,
        "hostX" to 0.25,
        "hostY" to 0.5,
        "hostAction" to "idle",
        "status" to "waiting",
        "createdAt" to FieldValue.serverTimestamp()
      )
      docRef.set(payload).await()
      Result.success(matchId)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, docRef.path)
      Result.failure(e)
    }
  }

  suspend fun findWaitingRoom(): Result<MatchRoom?> {
    requireUserId()
    val path = "matches"
    return try {
      val snapshot = db.collection(path)
        .whereEqualTo("status", "waiting")
        .limit(5)
        .get()
        .await()
      val waitingRooms = snapshot.toObjects(MatchRoom::class.java)
        .filter { it.hostId != currentUserId() }
      Result.success(waitingRooms.firstOrNull())
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.LIST, path)
      Result.failure(e)
    }
  }

  suspend fun joinMatchRoom(matchId: String, guestFishId: String): Result<Unit> {
    val uid = requireUserId()
    val guestName = currentDisplayName()
    val docRef = db.collection("matches").document(matchId)
    val fish = FishRoster.getById(guestFishId)

    return try {
      docRef.update(
        mapOf(
          "guestId" to uid,
          "guestName" to guestName,
          "guestFish" to guestFishId,
          "guestHp" to fish.baseHp,
          "guestMaxHp" to fish.baseHp,
          "guestX" to 0.75,
          "guestY" to 0.5,
          "guestAction" to "idle",
          "status" to "active",
          "updatedAt" to FieldValue.serverTimestamp()
        )
      ).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docRef.path)
      Result.failure(e)
    }
  }

  fun observeMatchRoom(matchId: String): Flow<MatchRoom?> = flow {
    val path = "matches/$matchId"
    emitAll(
      db.collection("matches").document(matchId)
        .snapshots()
        .map { snapshot ->
          snapshot.toObject(MatchRoom::class.java)
        }
        .catch { error ->
          val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
          if (firestoreError == null && error is CancellationException) throw error
          val exceptionToLog = firestoreError ?: (error as? Exception)
          if (exceptionToLog != null) {
            handleFirestoreError(exceptionToLog, OperationType.GET, path)
          }
          throw error
        }
    )
  }

  suspend fun updateMatchCombat(
    matchId: String,
    isHost: Boolean,
    x: Double,
    y: Double,
    hp: Int,
    action: String
  ): Result<Unit> {
    requireUserId()
    val docRef = db.collection("matches").document(matchId)
    val updates = if (isHost) {
      mapOf(
        "hostX" to x,
        "hostY" to y,
        "hostHp" to hp,
        "hostAction" to action,
        "updatedAt" to FieldValue.serverTimestamp()
      )
    } else {
      mapOf(
        "guestX" to x,
        "guestY" to y,
        "guestHp" to hp,
        "guestAction" to action,
        "updatedAt" to FieldValue.serverTimestamp()
      )
    }
    return try {
      docRef.update(updates).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docRef.path)
      Result.failure(e)
    }
  }

  suspend fun finishMatch(matchId: String, winnerId: String): Result<Unit> {
    requireUserId()
    val docRef = db.collection("matches").document(matchId)
    return try {
      docRef.update(
        mapOf(
          "status" to "finished",
          "winnerId" to winnerId,
          "updatedAt" to FieldValue.serverTimestamp()
        )
      ).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docRef.path)
      Result.failure(e)
    }
  }
}
