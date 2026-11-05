package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SportsKabaddi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.FishRoster
import com.example.data.model.FishSpecies
import com.example.data.model.UserProfile
import com.example.ui.components.AquaBottomNavBar
import com.example.ui.theme.AquaBlack
import com.example.ui.theme.AquaCardBorder
import com.example.ui.theme.AquaCyan
import com.example.ui.theme.AquaDarkCard
import com.example.ui.theme.AquaGold
import com.example.ui.theme.AquaTextMuted
import com.example.ui.theme.AquaTextPrimary
import com.example.ui.theme.AquaTextSecondary
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.AquaViewModel

@Composable
fun RosterHomeScreen(
  viewModel: AquaViewModel,
  profile: UserProfile?,
  onSignOut: () -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedCategory by remember { mutableStateOf("Fish") }
  val categories = listOf("Fish", "Arena", "Tank", "Substrates", "Cleaning Kit")

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
      // 1. Top Header (Grid Menu on left, Profile Pill on right)
      TopHeaderRow(
        profile = profile,
        onSignOut = onSignOut
      )

      Spacer(modifier = Modifier.height(16.dp))

      // 2. Category Filter Pills Bar
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState())
          .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        categories.forEach { category ->
          val isSelected = selectedCategory == category
          Box(
            modifier = Modifier
              .testTag("category_pill_$category")
              .clip(RoundedCornerShape(20.dp))
              .background(if (isSelected) AquaCyan else Color(0x33252A36))
              .clickable {
                selectedCategory = category
                if (category == "Arena") {
                  viewModel.navigateTo(AppScreen.LOBBY)
                }
              }
              .padding(horizontal = 20.dp, vertical = 9.dp)
          ) {
            Text(
              text = category,
              color = if (isSelected) Color.White else AquaTextSecondary,
              fontSize = 14.sp,
              fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // 3. Fish Fighter Cards (Vertical Scroll)
      LazyColumn(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
      ) {
        items(FishRoster.ALL_FISHES) { fish ->
          FishFighterCard(
            fish = fish,
            isUnlocked = profile?.unlockedFishes?.contains(fish.id) ?: (fish.costCoins == 0),
            isSelected = profile?.selectedFishId == fish.id,
            onInspect = {
              viewModel.selectFishForInspection(fish)
            },
            onQuickFight = {
              viewModel.selectFishForInspection(fish)
              viewModel.startSoloBattle()
            }
          )
        }

        item {
          Spacer(modifier = Modifier.height(100.dp))
        }
      }
    }

    // Floating Frosted Glass Bottom Navigation Bar
    AquaBottomNavBar(
      currentScreen = AppScreen.ROSTER,
      onNavigate = { viewModel.navigateTo(it) },
      modifier = Modifier.align(Alignment.BottomCenter)
    )
  }
}

@Composable
private fun TopHeaderRow(
  profile: UserProfile?,
  onSignOut: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 20.dp, vertical = 10.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    // Left Grid Button
    Box(
      modifier = Modifier
        .testTag("menu_grid_btn")
        .size(46.dp)
        .clip(RoundedCornerShape(14.dp))
        .background(Color(0xFF1E212B))
        .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(14.dp)),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = Icons.Default.GridView,
        contentDescription = "Menu Grid",
        tint = Color.White,
        modifier = Modifier.size(22.dp)
      )
    }

    // Right User Profile Pill (Jon Karter with Avatar)
    Row(
      modifier = Modifier
        .testTag("user_profile_pill")
        .clip(RoundedCornerShape(26.dp))
        .background(Color(0xFF1E212B))
        .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(26.dp))
        .clickable(onClick = onSignOut)
        .padding(horizontal = 12.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Circular avatar
      Box(
        modifier = Modifier
          .size(32.dp)
          .clip(CircleShape)
          .background(Color(0xFF8CE071)),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = "👨🏻‍💻",
          fontSize = 18.sp
        )
      }

      Spacer(modifier = Modifier.width(10.dp))

      Column {
        Text(
          text = profile?.username?.ifBlank { "Jon Karter" } ?: "Jon Karter",
          color = Color.White,
          fontSize = 14.sp,
          fontWeight = FontWeight.Medium
        )
        Text(
          text = "🏆 ${profile?.trophies ?: 100} • 🪙 ${profile?.coins ?: 200}",
          color = AquaGold,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold
        )
      }

      Spacer(modifier = Modifier.width(4.dp))
    }
  }
}

@Composable
fun FishFighterCard(
  fish: FishSpecies,
  isUnlocked: Boolean,
  isSelected: Boolean,
  onInspect: () -> Unit,
  onQuickFight: () -> Unit
) {
  Box(
    modifier = Modifier
      .testTag("card_fish_${fish.id}")
      .fillMaxWidth()
      .height(230.dp)
      .clip(RoundedCornerShape(26.dp))
      .background(
        Brush.verticalGradient(
          colors = listOf(
            Color(0xFF141720),
            Color(0xFF0F1117)
          )
        )
      )
      .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(26.dp))
      .clickable(onClick = onInspect)
  ) {
    // Top Left: Price ($12.00) or Rank
    Row(
      modifier = Modifier
        .align(Alignment.TopStart)
        .padding(start = 20.dp, top = 18.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = String.format("%.2f$", fish.priceUsd),
        color = Color.White,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold
      )
      if (isSelected) {
        Spacer(modifier = Modifier.width(8.dp))
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(AquaCyan)
            .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
          Text("EQUIPPED", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }
      }
    }

    // Top Right: Shopping bag / Equip action icon
    Box(
      modifier = Modifier
        .align(Alignment.TopEnd)
        .padding(end = 18.dp, top = 16.dp)
        .size(36.dp)
        .clip(CircleShape)
        .background(Color(0x33FFFFFF))
        .clickable(onClick = onQuickFight),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = if (!isUnlocked) Icons.Default.Lock else Icons.Default.ShoppingBag,
        contentDescription = "Equip or Fight",
        tint = Color.White,
        modifier = Modifier.size(18.dp)
      )
    }

    // Center Image: Large photorealistic fish swimming!
    Image(
      painter = painterResource(id = fish.drawableRes),
      contentDescription = "${fish.subtitle} ${fish.name}",
      modifier = Modifier
        .align(Alignment.Center)
        .size(190.dp)
        .padding(bottom = 20.dp),
      contentScale = ContentScale.Fit
    )

    // Bottom Floating Frosted Glass Label Pill: [ Oranda Goldfish  (>) ]
    Row(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(bottom = 16.dp, start = 18.dp, end = 18.dp)
        .fillMaxWidth()
        .clip(RoundedCornerShape(30.dp))
        .background(Color(0x881E2330))
        .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(30.dp))
        .clickable(onClick = onInspect)
        .padding(horizontal = 20.dp, vertical = 10.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "${fish.subtitle} ${fish.name.lowercase().replaceFirstChar { it.uppercase() }}",
        color = Color.White,
        fontSize = 15.sp,
        fontWeight = FontWeight.Medium
      )

      // Circular White Action Button with right chevron arrow
      Box(
        modifier = Modifier
          .size(34.dp)
          .clip(CircleShape)
          .background(Color.White),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
          contentDescription = "Inspect Details",
          tint = Color.Black,
          modifier = Modifier.size(14.dp)
        )
      }
    }
  }
}
