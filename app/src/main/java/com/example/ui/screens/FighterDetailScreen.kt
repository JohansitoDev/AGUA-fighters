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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FishRoster
import com.example.data.model.FishSpecies
import com.example.data.model.UserProfile
import com.example.ui.theme.AquaBlack
import com.example.ui.theme.AquaCyan
import com.example.ui.theme.AquaGold
import com.example.ui.theme.AquaTextMuted
import com.example.ui.theme.AquaTextSecondary
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.AquaViewModel

@Composable
fun FighterDetailScreen(
  viewModel: AquaViewModel,
  fish: FishSpecies,
  profile: UserProfile?,
  modifier: Modifier = Modifier
) {
  val isUnlocked = profile?.unlockedFishes?.contains(fish.id) ?: (fish.costCoins == 0)
  val isEquipped = profile?.selectedFishId == fish.id

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(AquaBlack)
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .verticalScroll(rememberScrollState())
        .padding(bottom = 110.dp)
    ) {
      // 1. Top Bar (< Back Button and Center Title "BASKET" / "FIGHTER SPECS")
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .testTag("detail_back_btn")
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
            modifier = Modifier
              .size(16.dp)
              .padding(start = 4.dp)
          )
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
          text = "BASKET",
          color = Color.White,
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.5.sp
        )

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.size(42.dp))
      }

      // 2. Large Hero Fish Display
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(260.dp),
        contentAlignment = Alignment.Center
      ) {
        Image(
          painter = painterResource(id = fish.drawableRes),
          contentDescription = fish.name,
          modifier = Modifier
            .size(240.dp)
            .padding(10.dp),
          contentScale = ContentScale.Fit
        )
      }

      // 3. Title & Subtitle Section
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 24.dp)
      ) {
        Text(
          text = fish.subtitle,
          color = AquaTextSecondary,
          fontSize = 15.sp,
          fontWeight = FontWeight.Normal
        )
        Text(
          text = fish.name,
          color = Color.White,
          fontSize = 32.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = String.format("%.2f$", fish.priceUsd),
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "per unit  •  ⚡ ATK ${fish.baseAtk}  HP ${fish.baseHp}",
            color = AquaTextSecondary,
            fontSize = 13.sp
          )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 4. Specifications Header & Rating
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Specifications",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
          )

          // 4.5 ★ Rating Badge
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .background(Color(0xFF1E212B))
              .padding(horizontal = 10.dp, vertical = 5.dp)
          ) {
            Text(
              text = String.format("%.1f", fish.rating),
              color = Color.White,
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
              imageVector = Icons.Default.Star,
              contentDescription = "Rating",
              tint = AquaGold,
              modifier = Modifier.size(16.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 5. 2x3 Grid of Dark Rounded Spec Cards (Weight, Size, Temperature, Tank size, Age, Special Diet)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          SpecCard(title = "Weight", value = fish.weightStr, modifier = Modifier.weight(1f))
          SpecCard(title = "Size", value = fish.sizeStr, modifier = Modifier.weight(1f))
          SpecCard(title = "Temperature", value = fish.tempStr, modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          SpecCard(title = "Tank size", value = fish.tankSizeStr, modifier = Modifier.weight(1f))
          SpecCard(title = "Age", value = fish.ageStr, modifier = Modifier.weight(1f))
          SpecCard(title = "Special Diet", value = fish.specialDiet, modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 6. Similar Species Horizontal Carousel
        Text(
          text = "Similar Species",
          color = Color.White,
          fontSize = 18.sp,
          fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          FishRoster.ALL_FISHES.filter { it.id != fish.id }.forEach { similarFish ->
            Box(
              modifier = Modifier
                .testTag("similar_fish_${similarFish.id}")
                .size(92.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFF161922))
                .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(18.dp))
                .clickable { viewModel.selectFishForInspection(similarFish) },
              contentAlignment = Alignment.Center
            ) {
              Image(
                painter = painterResource(id = similarFish.drawableRes),
                contentDescription = similarFish.name,
                modifier = Modifier
                  .size(80.dp)
                  .padding(6.dp),
                contentScale = ContentScale.Fit
              )
            }
          }
        }
      }
    }

    // 7. Bottom Floating Action Row (Equip icon on left, Wide "Add to cart / ENTER BATTLE >>>" on right)
    Row(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .fillMaxWidth()
        .navigationBarsPadding()
        .padding(horizontal = 24.dp, vertical = 14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // Round Icon Button (Equip or Basket)
      Box(
        modifier = Modifier
          .testTag("action_equip_btn")
          .size(54.dp)
          .clip(CircleShape)
          .background(Color(0xFF1E212B))
          .border(1.dp, Color(0x22FFFFFF), CircleShape)
          .clickable {
            if (isUnlocked) {
              viewModel.equipSelectedFish()
            } else {
              viewModel.unlockSelectedFish()
            }
          },
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.ShoppingBag,
          contentDescription = "Equip / Cart",
          tint = if (isEquipped) AquaCyan else Color.White,
          modifier = Modifier.size(24.dp)
        )
      }

      // Wide Main Action Button with >>> arrows
      Row(
        modifier = Modifier
          .testTag("action_fight_btn")
          .weight(1f)
          .height(54.dp)
          .clip(RoundedCornerShape(28.dp))
          .background(Color(0xFF1B1E26))
          .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(28.dp))
          .clickable {
            viewModel.startSoloBattle()
          }
          .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = if (isUnlocked) "Enter Battle" else "Unlock for ${fish.costCoins} 🪙",
          color = Color.White,
          fontSize = 16.sp,
          fontWeight = FontWeight.SemiBold
        )

        Text(
          text = ">>>",
          color = Color(0xFF6E7487),
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 2.sp
        )
      }
    }
  }
}

@Composable
private fun SpecCard(
  title: String,
  value: String,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .height(68.dp)
      .clip(RoundedCornerShape(16.dp))
      .background(Color(0xFF181B22))
      .border(1.dp, Color(0x18FFFFFF), RoundedCornerShape(16.dp))
      .padding(horizontal = 12.dp, vertical = 10.dp)
  ) {
    Column(
      verticalArrangement = Arrangement.SpaceBetween,
      modifier = Modifier.fillMaxSize()
    ) {
      Text(
        text = title,
        color = AquaTextMuted,
        fontSize = 11.sp,
        fontWeight = FontWeight.Normal
      )
      Text(
        text = value,
        color = Color.White,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold
      )
    }
  }
}
