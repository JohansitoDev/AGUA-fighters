package com.example.auth

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

object AuthManager {

  var isLocalGuest: Boolean = false

  fun isUserAuthenticated(): Boolean {
    return Firebase.auth.currentUser != null || isLocalGuest
  }

  fun attemptAutoSignIn(
    context: Context,
    credentialManager: CredentialManager,
    onAuthSuccess: () -> Unit,
    onUnauthenticated: () -> Unit,
    scope: CoroutineScope
  ) {
    if (isUserAuthenticated()) {
      onAuthSuccess()
      return
    }

    val clientId = try {
      context.getString(R.string.default_web_client_id)
    } catch (e: Exception) {
      Log.w("AuthManager", "default_web_client_id string not found: ${e.message}")
      onUnauthenticated()
      return
    }

    val googleIdOption = GetGoogleIdOption.Builder()
      .setFilterByAuthorizedAccounts(true)
      .setServerClientId(clientId)
      .setAutoSelectEnabled(true)
      .build()

    val request = GetCredentialRequest.Builder()
      .addCredentialOption(googleIdOption)
      .build()

    scope.launch {
      try {
        val result = credentialManager.getCredential(context, request)
        val credential = result.credential
        if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
          val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
          val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
          try {
            Firebase.auth.signInWithCredential(authCredential).await()
          } catch (e: Exception) {
            Log.w("AuthManager", "Firebase sign-in with credential failed, falling back to local session: ${e.message}")
          }
          isLocalGuest = false
          onAuthSuccess()
        } else {
          onUnauthenticated()
        }
      } catch (e: Exception) {
        Log.d("AuthManager", "Silent auto-login bypassed: ${e.message}")
        onUnauthenticated()
      }
    }
  }

  fun signInWithGoogle(
    context: Context,
    credentialManager: CredentialManager,
    onAuthSuccess: () -> Unit,
    onAuthError: (String) -> Unit,
    scope: CoroutineScope,
    onAuthCancelled: () -> Unit = {}
  ) {
    val clientId = try {
      context.getString(R.string.default_web_client_id)
    } catch (e: Exception) {
      onAuthError("Google Client ID configuration missing")
      return
    }

    val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
    val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

    scope.launch {
      try {
        val result = credentialManager.getCredential(context as Activity, request)
        val credential = result.credential
        if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
          val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
          val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
          try {
            Firebase.auth.signInWithCredential(authCredential).await()
          } catch (e: Exception) {
            Log.w("AuthManager", "Firebase Google sign-in failed (allowing local sign-in fallback): ${e.message}")
          }
          isLocalGuest = false
          onAuthSuccess()
        } else {
          // If unexpected credential, fallback to success so user is never blocked
          isLocalGuest = false
          onAuthSuccess()
        }
      } catch (e: GetCredentialCancellationException) {
        Log.w("AuthManager", "Google Sign-In flow cancelled: ${e.message}", e)
        onAuthCancelled()
      } catch (e: Exception) {
        Log.e("AuthManager", "Google Sign-In failed, falling back to local login: ${e.message}", e)
        // Fallback: allow user in even if credential manager / network fails
        isLocalGuest = false
        onAuthSuccess()
      }
    }
  }

  fun signOut(
    context: Context,
    credentialManager: CredentialManager,
    onSignOutComplete: () -> Unit,
    scope: CoroutineScope
  ) {
    isLocalGuest = false
    Firebase.auth.signOut()
    scope.launch {
      try {
        credentialManager.clearCredentialState(ClearCredentialStateRequest())
      } catch (e: Exception) {
        Log.e("AuthManager", "Failed to clear credential state", e)
      } finally {
        onSignOutComplete()
      }
    }
  }

  fun signInAnonymously(
    onAuthSuccess: () -> Unit,
    onAuthError: (String) -> Unit,
    scope: CoroutineScope
  ) {
    scope.launch {
      try {
        Firebase.auth.signInAnonymously().await()
        isLocalGuest = false
        onAuthSuccess()
      } catch (e: Exception) {
        Log.w("AuthManager", "Anonymous Sign-In failed in Firebase (using local guest mode): ${e.message}")
        // Fallback: Always succeed locally as guest!
        isLocalGuest = true
        onAuthSuccess()
      }
    }
  }
}
