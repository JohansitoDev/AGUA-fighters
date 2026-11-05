package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FishSpecies
import com.example.ui.components.AquaBottomNavBar
import com.example.ui.theme.AquaBlack
import com.example.ui.theme.AquaCyan
import com.example.ui.theme.AquaGold
import com.example.ui.theme.AquaTextSecondary
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.AquaViewModel

@Composable
fun OnlineLobbyScreen(
  viewModel: AquaViewModel,
  selectedFish: FishSpecies,
  matchmakingStatus: String,
  modifier: Modifier = Modifier
) {
  val infiniteTransition = rememberInfiniteTransition(label = "sonar")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 1.0f,
    targetValue = 1.6f,
    animationSpec = infiniteRepeatable(
      animation = tween(1400, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "pulseScale"
  )
  val pulseAlpha by infiniteTransition.animateFloat(
    initialValue = 0.6f,
    targetValue = 0.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(1400, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "pulseAlpha"
  )

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(AquaBlack)
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .padding(horizontal = 24.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Top Bar
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .testTag("lobby_back_btn")
            .size(42.dp)
            .clip(CircleShape)
            .background(Color(0xFF1E212B))
            .clickable { viewModel.navigateTo(AppScreen.ROSTER) },
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
            contentDescription = "Back",
            tint = Color.White,
            modifier = Modifier.size(16.dp).padding(start = 4.dp)
          )
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
          text = "ONLINE ARENA",
          color = Color.White,
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.5.sp
        )

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.size(42.dp))
      }

      Spacer(modifier = Modifier.height(30.dp))

      // Sonar Pulse Ring animation
      Box(
        modifier = Modifier.size(240.dp),
        contentAlignment = Alignment.Center
      ) {
        // Outer pulsing ring
        Box(
          modifier = Modifier
            .size(200.dp)
            .scale(pulseScale)
            .clip(CircleShape)
            .border(2.dp, AquaCyan.copy(alpha = pulseAlpha), CircleShape)
        )

        // Middle ring
        Box(
          modifier = Modifier
            .size(190.dp)
            .clip(CircleShape)
            .background(Color(0x33141B26))
            .border(1.5.dp, Color(0x3300B4FF), CircleShape)
        )

        // Center Fish Asset
        Image(
          painter = painterResource(id = selectedFish.drawableRes),
          contentDescription = selectedFish.name,
          modifier = Modifier.size(140.dp),
          contentScale = ContentScale.Fit
        )
      }

      Spacer(modifier = Modifier.height(24.dp))

      Text(
        text = selectedFish.name,
        color = Color.White,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = "Fighter Ready  •  Power ${selectedFish.baseAtk * 10}",
        color = AquaCyan,
        fontSize = 14.sp
      )

      Spacer(modifier = Modifier.height(30.dp))

      // Matchmaking Status Card
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(18.dp))
          .background(Color(0xFF141722))
          .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(18.dp))
          .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        CircularProgressIndicator(
          modifier = Modifier.size(24.dp),
          color = AquaCyan,
          strokeWidth = 2.5.dp
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column {
          Text(
            text = "Matchmaking",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
          )
          Text(
            text = matchmakingStatus,
            color = AquaTextSecondary,
            fontSize = 12.sp
          )
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Instant Match vs Bot Button
      Button(
        onClick = { viewModel.startSoloBattle() },
        modifier = Modifier
          .testTag("btn_instant_battle")
          .fillMaxWidth()
          .height(52.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F2432)),
        shape = RoundedCornerShape(26.dp)
      ) {
        Icon(
          imageVector = Icons.Default.PlayArrow,
          contentDescription = null,
          tint = AquaGold
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Instant Practice Match (AI)",
          color = Color.White,
          fontSize = 15.sp,
          fontWeight = FontWeight.SemiBold
        )
      }
    }

    // Bottom Navigation Bar
    AquaBottomNavBar(
      currentScreen = AppScreen.LOBBY,
      onNavigate = { viewModel.navigateTo(it) },
      modifier = Modifier.align(Alignment.BottomCenter)
    )
  }
}
