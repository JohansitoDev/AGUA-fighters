package com.example.ui.screens

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FishSpecies
import com.example.game.ActionType
import com.example.game.Fighter
import com.example.game.FishCombatEngine
import com.example.ui.theme.AquaCyan
import com.example.ui.theme.AquaGold
import com.example.ui.theme.AquaHealthGreen
import com.example.ui.theme.AquaHealthRed
import com.example.ui.theme.AquaOrange
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.AquaViewModel
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun BattleArenaScreen(
  viewModel: AquaViewModel,
  playerFish: FishSpecies,
  opponentFish: FishSpecies,
  modifier: Modifier = Modifier
) {
  val engine = remember(playerFish.id, opponentFish.id) {
    FishCombatEngine(
      playerSpecies = playerFish,
      opponentSpecies = opponentFish,
      playerName = "You",
      opponentName = "Challenger"
    )
  }

  var joystickX by remember { mutableFloatStateOf(0f) }
  var joystickY by remember { mutableFloatStateOf(0f) }
  var timeSec by remember { mutableFloatStateOf(0f) }
  var hasReportedOutcome by remember { mutableStateOf(false) }

  // Game Loop driven by frame ticks
  LaunchedEffect(Unit) {
    var lastNanos = System.nanoTime()
    while (true) {
      withFrameNanos { now ->
        val dt = ((now - lastNanos) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
        lastNanos = now
        timeSec += dt
        try {
          engine.update(dt, joystickX, joystickY)

          if (engine.isGameOver && !hasReportedOutcome) {
            hasReportedOutcome = true
            viewModel.onBattleFinished(engine.isPlayerWinner)
          }
        } catch (e: Exception) {
          Log.e("BattleArena", "Error updating combat engine", e)
        }
      }
    }
  }

  BoxWithConstraints(
    modifier = modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          colors = listOf(
            Color(0xFF030D1B),
            Color(0xFF021327),
            Color(0xFF010B17)
          )
        )
      )
  ) {
    val screenWidth = constraints.maxWidth.toFloat()
    val screenHeight = constraints.maxHeight.toFloat()
    engine.setArenaDimensions(screenWidth, screenHeight)

    // 1. Dynamic Underwater Canvas (Fish, Bubbles, Caustics, Projectiles)
    Canvas(
      modifier = Modifier.fillMaxSize()
    ) {
      // Draw underwater caustic rays
      val localTime = timeSec
      for (i in 0..4) {
        val startX = (i * 220f + sin(localTime + i) * 60f)
        drawLine(
          color = Color(0x0C00B4FF),
          start = Offset(startX, 0f),
          end = Offset(startX + 180f, size.height),
          strokeWidth = 90f
        )
      }

      // Draw ambient floating air bubbles
      for (p in engine.particles) {
        drawCircle(
          color = Color(0xFF5ED3FF).copy(alpha = p.alpha),
          radius = p.radius,
          center = Offset(p.x, p.y)
        )
      }

      // Draw Bubbles Projectiles
      for (b in engine.bubbles) {
        val bubbleColor = if (b.isFromPlayer) Color(0xFF00E5FF) else Color(0xFFFF5252)
        // Outer glow
        drawCircle(
          color = bubbleColor.copy(alpha = 0.4f),
          radius = b.radius + 6f,
          center = Offset(b.x, b.y)
        )
        // Main bubble
        drawCircle(
          color = bubbleColor,
          radius = b.radius,
          center = Offset(b.x, b.y)
        )
        // Highlight shine
        drawCircle(
          color = Color.White.copy(alpha = 0.8f),
          radius = b.radius * 0.35f,
          center = Offset(b.x - b.radius * 0.3f, b.y - b.radius * 0.3f)
        )
      }

      // Draw Player Fish
      drawFighter(engine.player, isPlayer = true, timeSec)

      // Draw Opponent Fish
      drawFighter(engine.opponent, isPlayer = false, timeSec)
    }

    // 2. Top Arena HUD (Health bars, stamina, combat timer)
    TopCombatHud(
      engine = engine,
      modifier = Modifier
        .align(Alignment.TopCenter)
        .statusBarsPadding()
        .padding(horizontal = 16.dp, vertical = 8.dp)
    )

    // 3. Floating Damage Numbers
    for (text in engine.combatTexts) {
      Text(
        text = text.text,
        color = if (text.isCritical) Color(0xFFFFD700) else Color.White.copy(alpha = text.alpha),
        fontSize = if (text.isCritical) 20.sp else 16.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = (text.x / 3).dp, top = (text.y / 3).dp)
      )
    }

    // 4. Bottom Controls: Virtual Analog Joystick (Left) + Combat Action Buttons (Right)
    Row(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .fillMaxWidth()
        .navigationBarsPadding()
        .padding(horizontal = 20.dp, vertical = 20.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.Bottom
    ) {
      // Left: Virtual Analog Joystick
      VirtualAnalogJoystick(
        modifier = Modifier.testTag("virtual_joystick"),
        onMove = { dx, dy ->
          joystickX = dx
          joystickY = dy
        }
      )

      // Right: 4 Ergonomic Action Buttons
      CombatActionButtons(
        engine = engine,
        modifier = Modifier.testTag("combat_actions_panel")
      )
    }

    // 5. Game Over / Victory Modal Overlay
    if (engine.isGameOver) {
      GameOverModal(
        isWinner = engine.isPlayerWinner,
        playerFish = playerFish,
        onPlayAgain = {
          viewModel.startSoloBattle()
        },
        onExit = {
          viewModel.navigateTo(AppScreen.ROSTER)
        },
        modifier = Modifier.align(Alignment.Center)
      )
    }
  }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFighter(
  fighter: Fighter,
  isPlayer: Boolean,
  timeSec: Float
) {
  val angleDegrees = Math.toDegrees(fighter.facingAngle.toDouble()).toFloat()

  rotate(degrees = angleDegrees, pivot = Offset(fighter.x, fighter.y)) {
    val bodyColor = if (isPlayer) Color(0xFFFF7A00) else Color(0xFF00B4FF)
    val finColor = if (isPlayer) Color(0xFFFFB800) else Color(0xFF00E5FF)

    // Draw Shield if active
    if (fighter.shieldActive) {
      drawCircle(
        color = Color(0x6600E5FF),
        radius = 70f,
        center = Offset(fighter.x, fighter.y)
      )
      drawCircle(
        color = Color.White,
        radius = 70f,
        center = Offset(fighter.x, fighter.y),
        style = Stroke(width = 4f)
      )
    }

    // Tail Fin flutter with sine-wave
    val tailWag = sin(fighter.tailPhase) * 16f
    val tailPath = Path().apply {
      moveTo(fighter.x - 30f, fighter.y)
      quadraticTo(
        fighter.x - 65f, fighter.y + tailWag,
        fighter.x - 85f, fighter.y + tailWag * 1.5f
      )
      quadraticTo(
        fighter.x - 65f, fighter.y - tailWag,
        fighter.x - 30f, fighter.y
      )
    }
    drawPath(tailPath, color = finColor.copy(alpha = 0.85f))

    // Dorsal Fin
    val dorsalPath = Path().apply {
      moveTo(fighter.x - 10f, fighter.y - 25f)
      quadraticTo(fighter.x - 20f, fighter.y - 45f, fighter.x + 10f, fighter.y - 20f)
      close()
    }
    drawPath(dorsalPath, color = finColor)

    // Main Oval Fish Body
    drawOval(
      color = bodyColor,
      topLeft = Offset(fighter.x - 40f, fighter.y - 24f),
      size = androidx.compose.ui.geometry.Size(80f, 48f)
    )

    // Pectoral Fin
    drawOval(
      color = finColor.copy(alpha = 0.9f),
      topLeft = Offset(fighter.x - 5f, fighter.y + 6f),
      size = androidx.compose.ui.geometry.Size(26f, 18f)
    )

    // Fish Eye
    drawCircle(
      color = Color.White,
      radius = 7f,
      center = Offset(fighter.x + 22f, fighter.y - 8f)
    )
    drawCircle(
      color = Color.Black,
      radius = 3.5f,
      center = Offset(fighter.x + 24f, fighter.y - 8f)
    )

    // Mouth
    drawArc(
      color = Color(0xFF4A1000),
      startAngle = -20f,
      sweepAngle = 40f,
      useCenter = false,
      topLeft = Offset(fighter.x + 32f, fighter.y - 4f),
      size = androidx.compose.ui.geometry.Size(12f, 8f),
      style = Stroke(width = 2.5f)
    )
  }
}

@Composable
private fun TopCombatHud(
  engine: FishCombatEngine,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    // Player 1 Card (You)
    FighterHudCard(
      name = engine.player.displayName,
      speciesName = engine.player.species.name,
      hp = engine.player.hp,
      maxHp = engine.player.maxHp,
      stamina = engine.player.stamina,
      maxStamina = engine.player.maxStamina,
      isPlayer = true
    )

    // Center VS / Combo Badge
    Column(
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Box(
        modifier = Modifier
          .clip(CircleShape)
          .background(Color(0x991E2433))
          .border(1.dp, Color(0x33FFFFFF), CircleShape)
          .padding(horizontal = 10.dp, vertical = 6.dp)
      ) {
        Text(
          text = "VS",
          color = Color.White,
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold
        )
      }

      if (engine.comboCount > 1) {
        Text(
          text = "${engine.comboCount}x COMBO!",
          color = AquaGold,
          fontSize = 12.sp,
          fontWeight = FontWeight.ExtraBold
        )
      }
    }

    // Player 2 Card (Opponent)
    FighterHudCard(
      name = engine.opponent.displayName,
      speciesName = engine.opponent.species.name,
      hp = engine.opponent.hp,
      maxHp = engine.opponent.maxHp,
      stamina = engine.opponent.stamina,
      maxStamina = engine.opponent.maxStamina,
      isPlayer = false
    )
  }
}

@Composable
private fun FighterHudCard(
  name: String,
  speciesName: String,
  hp: Int,
  maxHp: Int,
  stamina: Float,
  maxStamina: Float,
  isPlayer: Boolean
) {
  val hpFraction = (hp.toFloat() / maxHp).coerceIn(0f, 1f)
  val staminaFraction = (stamina / maxStamina).coerceIn(0f, 1f)

  Column(
    modifier = Modifier
      .width(140.dp)
      .clip(RoundedCornerShape(14.dp))
      .background(Color(0xCC131722))
      .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(14.dp))
      .padding(horizontal = 10.dp, vertical = 8.dp)
  ) {
    Text(
      text = if (isPlayer) "YOU" else name,
      color = if (isPlayer) AquaCyan else Color(0xFFFF5252),
      fontSize = 12.sp,
      fontWeight = FontWeight.Bold
    )
    Text(
      text = speciesName,
      color = Color.White,
      fontSize = 11.sp,
      maxLines = 1
    )

    Spacer(modifier = Modifier.height(6.dp))

    // Health Bar
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(8.dp)
        .clip(RoundedCornerShape(4.dp))
        .background(Color(0xFF262C3D))
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth(hpFraction)
          .height(8.dp)
          .clip(RoundedCornerShape(4.dp))
          .background(if (hpFraction > 0.35f) AquaHealthGreen else AquaHealthRed)
      )
    }

    Spacer(modifier = Modifier.height(4.dp))

    // Stamina Bar
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(4.dp)
        .clip(RoundedCornerShape(2.dp))
        .background(Color(0xFF1E2330))
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth(staminaFraction)
          .height(4.dp)
          .clip(RoundedCornerShape(2.dp))
          .background(AquaCyan)
      )
    }
  }
}

@Composable
private fun VirtualAnalogJoystick(
  onMove: (Float, Float) -> Unit,
  modifier: Modifier = Modifier
) {
  var knobOffset by remember { mutableStateOf(Offset.Zero) }
  val maxRadius = 60f

  Box(
    modifier = modifier
      .size(130.dp)
      .clip(CircleShape)
      .background(Color(0x441E2433))
      .border(2.dp, Color(0x3300B4FF), CircleShape)
      .pointerInput(Unit) {
        detectDragGestures(
          onDragStart = { },
          onDragEnd = {
            knobOffset = Offset.Zero
            onMove(0f, 0f)
          },
          onDragCancel = {
            knobOffset = Offset.Zero
            onMove(0f, 0f)
          },
          onDrag = { change, dragAmount ->
            change.consume()
            val newOffset = knobOffset + dragAmount
            val dist = sqrt(newOffset.x * newOffset.x + newOffset.y * newOffset.y)
            knobOffset = if (dist > maxRadius) {
              Offset((newOffset.x / dist) * maxRadius, (newOffset.y / dist) * maxRadius)
            } else {
              newOffset
            }
            onMove(knobOffset.x / maxRadius, knobOffset.y / maxRadius)
          }
        )
      },
    contentAlignment = Alignment.Center
  ) {
    // Inner Knob using offset for high-performance fluid drag
    Box(
      modifier = Modifier
        .size(52.dp)
        .offset(
          x = (knobOffset.x / 2).dp,
          y = (knobOffset.y / 2).dp
        )
        .clip(CircleShape)
        .background(
          Brush.radialGradient(
            colors = listOf(Color(0xFF00E5FF), Color(0xFF0072FE))
          )
        )
        .border(2.dp, Color.White, CircleShape)
    )
  }
}

@Composable
private fun CombatActionButtons(
  engine: FishCombatEngine,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(10.dp),
    horizontalAlignment = Alignment.End
  ) {
    Row(
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      // 1. Vortex Shield
      CombatButton(
        tag = "btn_vortex",
        label = "Vortex",
        icon = Icons.Default.Shield,
        color = Color(0xFF00B4FF),
        enabled = engine.player.canVortex(),
        onClick = { engine.playerVortex() }
      )

      // 2. Bubble Blast (Ranged)
      CombatButton(
        tag = "btn_bubble",
        label = "Bubble",
        icon = Icons.Default.WaterDrop,
        color = Color(0xFF00E676),
        enabled = engine.player.canBubble(),
        onClick = { engine.playerBubble() }
      )
    }

    Row(
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      // 3. Ultimate Tsunami Strike
      CombatButton(
        tag = "btn_special",
        label = "Tsunami",
        icon = Icons.Default.Waves,
        color = Color(0xFFFFB800),
        enabled = engine.player.canSpecial(),
        size = 62.dp,
        onClick = { engine.playerSpecial() }
      )

      // 4. Primary Bite / Melee Dash
      CombatButton(
        tag = "btn_bite",
        label = "Bite",
        icon = null,
        color = Color(0xFFFF5252),
        enabled = engine.player.canBite(),
        size = 62.dp,
        onClick = { engine.playerBite() }
      )
    }
  }
}

@Composable
private fun CombatButton(
  tag: String,
  label: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector?,
  color: Color,
  enabled: Boolean,
  size: androidx.compose.ui.unit.Dp = 52.dp,
  onClick: () -> Unit
) {
  Box(
    modifier = Modifier
      .testTag(tag)
      .size(size)
      .clip(CircleShape)
      .background(if (enabled) color.copy(alpha = 0.85f) else Color(0x332B3140))
      .border(1.5.dp, if (enabled) Color.White.copy(alpha = 0.6f) else Color(0x22FFFFFF), CircleShape)
      .clickable(enabled = enabled, onClick = onClick),
    contentAlignment = Alignment.Center
  ) {
    if (icon != null) {
      Icon(
        imageVector = icon,
        contentDescription = label,
        tint = if (enabled) Color.White else Color(0x66FFFFFF),
        modifier = Modifier.size(24.dp)
      )
    } else {
      Text(
        text = "🦈",
        fontSize = 24.sp
      )
    }
  }
}

@Composable
private fun GameOverModal(
  isWinner: Boolean,
  playerFish: FishSpecies,
  onPlayAgain: () -> Unit,
  onExit: () -> Unit,
  modifier: Modifier = Modifier
) {
  AnimatedVisibility(
    visible = true,
    enter = fadeIn() + scaleIn(),
    modifier = modifier.padding(24.dp)
  ) {
    Box(
      modifier = Modifier
        .testTag("game_over_dialog")
        .fillMaxWidth()
        .clip(RoundedCornerShape(28.dp))
        .background(Color(0xF010131B))
        .border(2.dp, if (isWinner) AquaGold else Color(0xFFFF5252), RoundedCornerShape(28.dp))
        .padding(24.dp),
      contentAlignment = Alignment.Center
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        Text(
          text = if (isWinner) "🏆 VICTORY!" else "💔 DEFEAT",
          color = if (isWinner) AquaGold else Color(0xFFFF5252),
          fontSize = 28.sp,
          fontWeight = FontWeight.ExtraBold,
          letterSpacing = 1.sp
        )

        Text(
          text = if (isWinner) "Your ${playerFish.name} triumphed in the reef!" else "Better luck next dive, warrior!",
          color = Color.White,
          fontSize = 14.sp
        )

        // Rewards Section
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF191D28))
            .padding(14.dp),
          horizontalArrangement = Arrangement.SpaceAround
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Trophies", color = Color(0xFF8E95A5), fontSize = 11.sp)
            Text(if (isWinner) "+35 🏆" else "-12 🏆", color = if (isWinner) AquaGold else Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
          }
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Pearls", color = Color(0xFF8E95A5), fontSize = 11.sp)
            Text(if (isWinner) "+60 🪙" else "+15 🪙", color = AquaCyan, fontWeight = FontWeight.Bold, fontSize = 16.sp)
          }
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("XP", color = Color(0xFF8E95A5), fontSize = 11.sp)
            Text("+50 XP", color = Color(0xFF00E676), fontWeight = FontWeight.Bold, fontSize = 16.sp)
          }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Button(
          onClick = onPlayAgain,
          modifier = Modifier
            .testTag("btn_play_again")
            .fillMaxWidth()
            .height(48.dp),
          colors = ButtonDefaults.buttonColors(containerColor = AquaCyan),
          shape = RoundedCornerShape(24.dp)
        ) {
          Text("Play Again", color = Color.White, fontWeight = FontWeight.Bold)
        }

        Button(
          onClick = onExit,
          modifier = Modifier
            .testTag("btn_exit_arena")
            .fillMaxWidth()
            .height(48.dp),
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF222634)),
          shape = RoundedCornerShape(24.dp)
        ) {
          Text("Return to Roster", color = Color.White)
        }
      }
    }
  }
}
