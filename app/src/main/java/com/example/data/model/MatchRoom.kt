package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class MatchRoom(
  val id: String = "",
  val hostId: String = "",
  val hostName: String = "",
  val hostFish: String = "oranda_goldfish",
  val hostHp: Int = 100,
  val hostMaxHp: Int = 100,
  val hostX: Double = 0.25,
  val hostY: Double = 0.5,
  val hostAction: String = "idle",
  val guestId: String? = null,
  val guestName: String? = null,
  val guestFish: String? = null,
  val guestHp: Int? = null,
  val guestMaxHp: Int? = null,
  val guestX: Double? = null,
  val guestY: Double? = null,
  val guestAction: String? = null,
  val status: String = "waiting",
  val winnerId: String? = null,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null
)
