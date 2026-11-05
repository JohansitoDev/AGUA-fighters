package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class LeaderboardEntry(
  @DocumentId val id: String = "",
  val userId: String = "",
  val username: String = "",
  val trophies: Int = 0,
  val wins: Int = 0,
  val level: Int = 1,
  val favoriteFish: String = "oranda_goldfish",
  @ServerTimestamp val updatedAt: Timestamp? = null
)
