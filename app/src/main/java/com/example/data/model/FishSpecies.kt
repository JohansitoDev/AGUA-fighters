package com.example.data.model

import androidx.annotation.DrawableRes
import com.example.R

data class FishSpecies(
  val id: String,
  val name: String,
  val subtitle: String,
  val priceUsd: Double,
  val costCoins: Int,
  val rating: Float,
  val weightStr: String,
  val sizeStr: String,
  val tempStr: String,
  val tankSizeStr: String,
  val ageStr: String,
  val specialDiet: String,
  val specialAbilityName: String,
  val specialAbilityDesc: String,
  val baseHp: Int,
  val baseAtk: Int,
  val baseDef: Int,
  val baseSpeed: Float,
  @DrawableRes val drawableRes: Int
)

object FishRoster {
  val ALL_FISHES = listOf(
    FishSpecies(
      id = "oranda_goldfish",
      name = "GOLDFISH",
      subtitle = "Oranda",
      priceUsd = 12.00,
      costCoins = 0, // Default starter
      rating = 4.5f,
      weightStr = "4.8 QZ",
      sizeStr = "4.9 inch",
      tempStr = "18-20°C",
      tankSizeStr = "18 gal min",
      ageStr = "2 Years",
      specialDiet = "Omnivore",
      specialAbilityName = "Bubble Vortex",
      specialAbilityDesc = "Charges an aquatic swirl that traps enemies and deals continuous damage.",
      baseHp = 100,
      baseAtk = 22,
      baseDef = 14,
      baseSpeed = 1.0f,
      drawableRes = R.drawable.fish_oranda
    ),
    FishSpecies(
      id = "warsee_fish",
      name = "WARSEE FISH",
      subtitle = "Cichlid",
      priceUsd = 16.00,
      costCoins = 300,
      rating = 4.8f,
      weightStr = "6.2 QZ",
      sizeStr = "5.5 inch",
      tempStr = "22-26°C",
      tankSizeStr = "30 gal min",
      ageStr = "3 Years",
      specialDiet = "Carnivore",
      specialAbilityName = "Reef Fin Slash",
      specialAbilityDesc = "High-speed tactical dash delivering razor sharp fin lacerations.",
      baseHp = 120,
      baseAtk = 28,
      baseDef = 18,
      baseSpeed = 1.25f,
      drawableRes = R.drawable.fish_warsee
    ),
    FishSpecies(
      id = "clownfish",
      name = "CLOWNFISH",
      subtitle = "Amphiprion",
      priceUsd = 20.00,
      costCoins = 600,
      rating = 4.6f,
      weightStr = "3.5 QZ",
      sizeStr = "3.8 inch",
      tempStr = "24-27°C",
      tankSizeStr = "20 gal min",
      ageStr = "1.5 Years",
      specialDiet = "Anemone Pellets",
      specialAbilityName = "Bio-Electric Dart",
      specialAbilityDesc = "Zips unpredictably around the enemy, releasing disorienting sparks.",
      baseHp = 90,
      baseAtk = 32,
      baseDef = 12,
      baseSpeed = 1.4f,
      drawableRes = R.drawable.fish_clownfish
    ),
    FishSpecies(
      id = "betta_fighter",
      name = "BETTA DRAGON",
      subtitle = "Splendens",
      priceUsd = 25.00,
      costCoins = 1000,
      rating = 4.9f,
      weightStr = "5.0 QZ",
      sizeStr = "4.2 inch",
      tempStr = "25-28°C",
      tankSizeStr = "25 gal min",
      ageStr = "2 Years",
      specialDiet = "Bloodworms",
      specialAbilityName = "Crimson Tsunami",
      specialAbilityDesc = "Unleashes fierce multi-strike combo with flared predatory fins.",
      baseHp = 110,
      baseAtk = 38,
      baseDef = 15,
      baseSpeed = 1.35f,
      drawableRes = R.drawable.fish_betta
    ),
    FishSpecies(
      id = "puffer_spike",
      name = "GOLDEN PUFFER",
      subtitle = "Tetraodontidae",
      priceUsd = 30.00,
      costCoins = 1500,
      rating = 4.7f,
      weightStr = "7.8 QZ",
      sizeStr = "4.0 inch",
      tempStr = "23-26°C",
      tankSizeStr = "35 gal min",
      ageStr = "2.5 Years",
      specialDiet = "Crustaceans",
      specialAbilityName = "Spike Fortress",
      specialAbilityDesc = "Inflates to double size with toxic thorns, reflecting incoming attacks.",
      baseHp = 150,
      baseAtk = 20,
      baseDef = 35,
      baseSpeed = 0.9f,
      drawableRes = R.drawable.fish_puffer
    )
  )

  fun getById(id: String): FishSpecies {
    return ALL_FISHES.find { it.id == id } ?: ALL_FISHES.first()
  }
}
