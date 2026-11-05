package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserProfile(
  @DocumentId val id: String = "",
  val userId: String = "",
  val username: String = "AquaWarrior",
  val avatarFish: String = "oranda_goldfish",
  val level: Int = 1,
  val xp: Int = 0,
  val coins: Int = 200,
  val trophies: Int = 100,
  val wins: Int = 0,
  val losses: Int = 0,
  val unlockedFishes: List<String> = listOf("oranda_goldfish"),
  val selectedFishId: String = "oranda_goldfish",
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null
)
