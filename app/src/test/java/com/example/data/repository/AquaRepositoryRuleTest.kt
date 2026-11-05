package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.UserProfile
import com.example.data.model.MatchRoom
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class AquaRepositoryRuleTest : FirestoreEmulatorTestBase() {

  @Test
  fun ensureProfileExists_createsUserProfileAndLeaderboard(): Unit = runBlocking {
    signInTestUser(ALICE_EMAIL)
    val repo = AquaRepository(firestore)

    val profileResult = withTimeout(DEFAULT_TIMEOUT_MS) { repo.ensureProfileExists() }
    assertTrue(profileResult.isSuccess)
    val profile = profileResult.getOrThrow()
    assertTrue(profile.userId.isNotEmpty())
    assertEquals(100, profile.trophies)
    assertEquals(200, profile.coins)
  }

  @Test
  fun observeUserProfile_authenticatedOwner_returnsProfile(): Unit = runBlocking {
    val uid = signInTestUser(ALICE_EMAIL)
    val repo = AquaRepository(firestore)
    repo.ensureProfileExists().getOrThrow()

    val profile: UserProfile? = withTimeout(FLOW_TIMEOUT_MS) {
      repo.observeUserProfile(uid).first { it != null }
    }
    assertNotNull(profile)
    assertEquals(uid, profile?.userId)
  }

  @Test
  fun createMatchRoom_validUser_createsMatch(): Unit = runBlocking {
    signInTestUser(ALICE_EMAIL)
    val repo = AquaRepository(firestore)

    val matchResult = withTimeout(DEFAULT_TIMEOUT_MS) { repo.createMatchRoom("oranda_goldfish") }
    assertTrue(matchResult.isSuccess)
    val matchId = matchResult.getOrThrow()
    assertTrue(matchId.startsWith("battle_"))

    val matchRoom: MatchRoom? = withTimeout(FLOW_TIMEOUT_MS) {
      repo.observeMatchRoom(matchId).first { it != null }
    }
    assertEquals("oranda_goldfish", matchRoom?.hostFish)
    assertEquals("waiting", matchRoom?.status)
  }

  @Test
  fun observeUserProfile_unauthenticatedUser_failsWithPermissionDenied(): Unit = runBlocking {
    val uid = signInTestUser(ALICE_EMAIL)
    val repo = AquaRepository(firestore)
    repo.ensureProfileExists().getOrThrow()

    // Sign out to simulate unauthenticated access
    auth.signOut()

    try {
      withTimeout(FLOW_TIMEOUT_MS) {
        repo.observeUserProfile(uid).first()
      }
      fail("Expected exception for unauthenticated observer")
    } catch (e: Exception) {
      val firestoreException = e.unwrapFirestoreException()
      assertNotNull("Expected nested FirebaseFirestoreException, got $e", firestoreException)
      assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreException?.code)
    }
  }

  private companion object {
    const val ALICE_EMAIL = "alice_aqua@test.com"
    const val DEFAULT_TIMEOUT_MS = 5000L
    const val FLOW_TIMEOUT_MS = 4000L
  }
}
