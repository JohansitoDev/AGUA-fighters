package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import com.example.R
import com.example.auth.AuthManager
import com.example.ui.theme.AquaBlack
import com.example.ui.theme.AquaCyan
import com.example.ui.theme.AquaTextMuted
import com.example.ui.theme.AquaTextSecondary

@Composable
fun AuthScreen(
  onAuthSuccess: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  val credentialManager = remember { CredentialManager.create(context) }

  var isLoading by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          colors = listOf(
            AquaBlack,
            Color(0xFF041224),
            Color(0xFF010813)
          )
        )
      ),
    contentAlignment = Alignment.Center
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 32.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      // Fighting Fish Logo Emblem
      Box(
        modifier = Modifier
          .size(140.dp)
          .clip(CircleShape)
          .background(Color(0x3300B4FF))
          .border(2.dp, AquaCyan, CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Image(
          painter = painterResource(id = R.drawable.fish_betta),
          contentDescription = "Aqua Fighters Logo",
          modifier = Modifier.size(110.dp),
          contentScale = ContentScale.Fit
        )
      }

      Spacer(modifier = Modifier.height(24.dp))

      Text(
        text = "AQUA FIGHTERS",
        color = Color.White,
        fontSize = 28.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 1.sp
      )

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = "Deep-sea 2D fish battles with dynamic aquatic combat, online real-time multiplayer, and global rankings.",
        color = AquaTextSecondary,
        fontSize = 14.sp,
        textAlign = TextAlign.Center,
        lineHeight = 20.sp
      )

      Spacer(modifier = Modifier.height(36.dp))

      // Google Sign-In Button
      Button(
        onClick = {
          isLoading = true
          errorMessage = null
          AuthManager.signInWithGoogle(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = {
              isLoading = false
              onAuthSuccess()
            },
            onAuthError = { err ->
              isLoading = false
              errorMessage = err
            },
            scope = coroutineScope,
            onAuthCancelled = {
              isLoading = false
            }
          )
        },
        enabled = !isLoading,
        modifier = Modifier
          .testTag("google_sign_in_btn")
          .fillMaxWidth()
          .height(54.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
        shape = RoundedCornerShape(27.dp)
      ) {
        if (isLoading) {
          CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            color = Color.Black,
            strokeWidth = 2.dp
          )
        } else {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
          ) {
            Text(
              text = "G",
              color = Color(0xFF4285F4),
              fontSize = 20.sp,
              fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
              text = "Sign in with Google",
              color = Color.Black,
              fontSize = 16.sp,
              fontWeight = FontWeight.SemiBold
            )
          }
        }
      }

      if (errorMessage != null) {
        Spacer(modifier = Modifier.height(14.dp))
        Text(
          text = errorMessage ?: "",
          color = Color(0xFFFF5252),
          fontSize = 13.sp,
          textAlign = TextAlign.Center
        )
      }

      Spacer(modifier = Modifier.height(24.dp))

      Text(
        text = "Secure Google Authentication powered by Credential Manager",
        color = AquaTextMuted,
        fontSize = 11.sp,
        textAlign = TextAlign.Center
      )
    }
  }
}
