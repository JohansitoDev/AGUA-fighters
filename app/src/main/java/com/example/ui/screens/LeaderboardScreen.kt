package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FishRoster
import com.example.data.model.LeaderboardEntry
import com.example.data.model.UserProfile
import com.example.ui.components.AquaBottomNavBar
import com.example.ui.theme.AquaBlack
import com.example.ui.theme.AquaCyan
import com.example.ui.theme.AquaGold
import com.example.ui.theme.AquaTextMuted
import com.example.ui.theme.AquaTextSecondary
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.AquaViewModel

@Composable
fun LeaderboardScreen(
  viewModel: AquaViewModel,
  leaderboard: List<LeaderboardEntry>,
  profile: UserProfile?,
  modifier: Modifier = Modifier
) {
  // Pre-populate with high scores if list is currently sparse
  val displayList = rememberDisplayLeaderboard(leaderboard, profile)

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(AquaBlack)
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
    ) {
      // 1. Top Bar
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .testTag("leaderboard_back_btn")
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

        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.EmojiEvents,
            contentDescription = null,
            tint = AquaGold,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "GLOBAL RANKINGS",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
          )
        }

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.size(42.dp))
      }

      // 2. Podium (Top 3)
      if (displayList.size >= 3) {
        PodiumView(top3 = displayList.take(3))
      }

      Spacer(modifier = Modifier.height(14.dp))

      // 3. Leaderboard Rows
      LazyColumn(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        itemsIndexed(displayList) { index, entry ->
          val isCurrentPlayer = entry.userId == profile?.userId
          LeaderboardRow(
            rank = index + 1,
            entry = entry,
            isCurrentPlayer = isCurrentPlayer
          )
        }

        item {
          Spacer(modifier = Modifier.height(100.dp))
        }
      }
    }

    // Floating Frosted Glass Bottom Navigation Bar
    AquaBottomNavBar(
      currentScreen = AppScreen.LEADERBOARD,
      onNavigate = { viewModel.navigateTo(it) },
      modifier = Modifier.align(Alignment.BottomCenter)
    )
  }
}

@Composable
private fun PodiumView(top3: List<LeaderboardEntry>) {
  val first = top3.getOrNull(0) ?: return
  val second = top3.getOrNull(1) ?: return
  val third = top3.getOrNull(2) ?: return

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 24.dp, vertical = 8.dp),
    horizontalArrangement = Arrangement.SpaceEvenly,
    verticalAlignment = Alignment.Bottom
  ) {
    // 2nd Place
    PodiumColumn(entry = second, rank = 2, height = 95.dp, badgeColor = Color(0xFFC0C0C0))
    // 1st Place
    PodiumColumn(entry = first, rank = 1, height = 120.dp, badgeColor = AquaGold)
    // 3rd Place
    PodiumColumn(entry = third, rank = 3, height = 80.dp, badgeColor = Color(0xFFCD7F32))
  }
}

@Composable
private fun PodiumColumn(
  entry: LeaderboardEntry,
  rank: Int,
  height: androidx.compose.ui.unit.Dp,
  badgeColor: Color
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(
      text = when (rank) {
        1 -> "👑"
        2 -> "🥈"
        else -> "🥉"
      },
      fontSize = 20.sp
    )

    Text(
      text = entry.username.take(10),
      color = Color.White,
      fontSize = 12.sp,
      fontWeight = FontWeight.Bold,
      maxLines = 1
    )

    Text(
      text = "${entry.trophies} 🏆",
      color = AquaGold,
      fontSize = 11.sp,
      fontWeight = FontWeight.SemiBold
    )

    Spacer(modifier = Modifier.height(6.dp))

    Box(
      modifier = Modifier
        .width(80.dp)
        .height(height)
        .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
        .background(
          Brush.verticalGradient(
            listOf(badgeColor.copy(alpha = 0.5f), Color(0xFF161922))
          )
        )
        .border(1.dp, badgeColor.copy(alpha = 0.7f), RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = "#$rank",
        color = Color.White,
        fontSize = 18.sp,
        fontWeight = FontWeight.ExtraBold
      )
    }
  }
}

@Composable
private fun LeaderboardRow(
  rank: Int,
  entry: LeaderboardEntry,
  isCurrentPlayer: Boolean
) {
  Row(
    modifier = Modifier
      .testTag("leaderboard_row_$rank")
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .background(if (isCurrentPlayer) Color(0x3300B4FF) else Color(0xFF141720))
      .border(
        1.dp,
        if (isCurrentPlayer) AquaCyan else Color(0x18FFFFFF),
        RoundedCornerShape(16.dp)
      )
      .padding(horizontal = 16.dp, vertical = 12.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    // Rank number
    Text(
      text = "$rank",
      color = if (rank <= 3) AquaGold else AquaTextSecondary,
      fontSize = 15.sp,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.width(32.dp)
    )

    // Avatar
    Box(
      modifier = Modifier
        .size(36.dp)
        .clip(CircleShape)
        .background(Color(0xFF222838)),
      contentAlignment = Alignment.Center
    ) {
      Text("🐠", fontSize = 18.sp)
    }

    Spacer(modifier = Modifier.width(12.dp))

    // Player Details
    Column(modifier = Modifier.weight(1f)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = entry.username,
          color = Color.White,
          fontSize = 14.sp,
          fontWeight = FontWeight.SemiBold
        )
        if (isCurrentPlayer) {
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "(YOU)",
            color = AquaCyan,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }
      Text(
        text = "Level ${entry.level}  •  ${entry.wins} Wins",
        color = AquaTextMuted,
        fontSize = 11.sp
      )
    }

    // Trophies count
    Text(
      text = "${entry.trophies} 🏆",
      color = AquaGold,
      fontSize = 14.sp,
      fontWeight = FontWeight.Bold
    )
  }
}

private fun rememberDisplayLeaderboard(
  firestoreList: List<LeaderboardEntry>,
  profile: UserProfile?
): List<LeaderboardEntry> {
  val defaultPlayers = listOf(
    LeaderboardEntry(userId = "bot_1", username = "AquaticKing", trophies = 2850, wins = 64, level = 18, favoriteFish = "betta_fighter"),
    LeaderboardEntry(userId = "bot_2", username = "TidalMaster", trophies = 2420, wins = 52, level = 15, favoriteFish = "warsee_fish"),
    LeaderboardEntry(userId = "bot_3", username = "ReefViper", trophies = 2100, wins = 45, level = 13, favoriteFish = "puffer_spike"),
    LeaderboardEntry(userId = "bot_4", username = "CoralStriker", trophies = 1850, wins = 38, level = 11, favoriteFish = "clownfish"),
    LeaderboardEntry(userId = "bot_5", username = "DeepBlue99", trophies = 1500, wins = 29, level = 9, favoriteFish = "oranda_goldfish"),
    LeaderboardEntry(userId = "bot_6", username = "AbyssHunter", trophies = 1200, wins = 21, level = 7, favoriteFish = "betta_fighter")
  )

  val combined = (firestoreList + defaultPlayers).distinctBy { it.userId }.toMutableList()

  if (profile != null) {
    val existingIndex = combined.indexOfFirst { it.userId == profile.userId }
    val userEntry = LeaderboardEntry(
      userId = profile.userId,
      username = profile.username,
      trophies = profile.trophies,
      wins = profile.wins,
      level = profile.level,
      favoriteFish = profile.selectedFishId
    )
    if (existingIndex >= 0) {
      combined[existingIndex] = userEntry
    } else {
      combined.add(userEntry)
    }
  }

  return combined.sortedByDescending { it.trophies }
}
