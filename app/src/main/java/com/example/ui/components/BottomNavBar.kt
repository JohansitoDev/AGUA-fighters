package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SportsKabaddi
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AquaCardBorder
import com.example.ui.theme.AquaCyan
import com.example.ui.theme.AquaTextMuted
import com.example.ui.viewmodel.AppScreen

@Composable
fun AquaBottomNavBar(
  currentScreen: AppScreen,
  onNavigate: (AppScreen) -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .navigationBarsPadding()
      .padding(horizontal = 24.dp, vertical = 12.dp),
    contentAlignment = Alignment.Center
  ) {
    // Frosted glass capsule floating container
    Row(
      modifier = Modifier
        .clip(RoundedCornerShape(32.dp))
        .background(
          Brush.verticalGradient(
            colors = listOf(
              Color(0xCC1A1E29),
              Color(0xEE12141C)
            )
          )
        )
        .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(32.dp))
        .padding(horizontal = 16.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.spacedBy(20.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // 1. Home / Roster
      NavItem(
        icon = Icons.Default.Home,
        contentDescription = "Home Roster",
        isSelected = currentScreen == AppScreen.ROSTER,
        tag = "nav_home",
        onClick = { onNavigate(AppScreen.ROSTER) }
      )

      // 2. Search / Detail
      NavItem(
        icon = Icons.Default.Search,
        contentDescription = "Fighter Specs",
        isSelected = currentScreen == AppScreen.DETAIL,
        tag = "nav_search",
        onClick = { onNavigate(AppScreen.DETAIL) }
      )

      // 3. Battle Arena
      NavItem(
        icon = Icons.Default.SportsKabaddi,
        contentDescription = "Battle Arena",
        isSelected = currentScreen == AppScreen.ARENA || currentScreen == AppScreen.LOBBY,
        tag = "nav_arena",
        onClick = { onNavigate(AppScreen.LOBBY) }
      )

      // 4. Global Leaderboard / Basket
      NavItem(
        icon = Icons.Default.EmojiEvents,
        contentDescription = "Leaderboard",
        isSelected = currentScreen == AppScreen.LEADERBOARD,
        tag = "nav_leaderboard",
        onClick = { onNavigate(AppScreen.LEADERBOARD) }
      )
    }
  }
}

@Composable
private fun NavItem(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  contentDescription: String,
  isSelected: Boolean,
  tag: String,
  onClick: () -> Unit
) {
  Box(
    modifier = Modifier
      .testTag(tag)
      .size(48.dp)
      .clip(CircleShape)
      .background(if (isSelected) Color(0x3300B4FF) else Color.Transparent)
      .clickable(onClick = onClick),
    contentAlignment = Alignment.Center
  ) {
    Icon(
      imageVector = icon,
      contentDescription = contentDescription,
      tint = if (isSelected) AquaCyan else Color(0xFF8E95A5),
      modifier = Modifier.size(24.dp)
    )
  }
}
