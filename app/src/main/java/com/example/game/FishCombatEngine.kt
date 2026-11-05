package com.example.game

import androidx.compose.ui.geometry.Offset
import com.example.data.model.FishSpecies
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

enum class ActionType {
  IDLE,
  SWIM,
  BITE_DASH,
  BUBBLE_CAST,
  VORTEX_SHIELD,
  TSUNAMI_BURST,
  HIT_STUN
}

data class BubbleProjectile(
  var x: Float,
  var y: Float,
  var vx: Float,
  var vy: Float,
  val radius: Float = 14f,
  val isFromPlayer: Boolean,
  val damage: Int,
  var lifetime: Float = 2.5f,
  var wobblePhase: Float = Random.nextFloat() * 6.28f
)

data class CombatText(
  val text: String,
  var x: Float,
  var y: Float,
  var alpha: Float = 1.0f,
  val isCritical: Boolean = false
)

data class WaterParticle(
  var x: Float,
  var y: Float,
  var vx: Float,
  var vy: Float,
  val radius: Float,
  var alpha: Float,
  val isAirBubble: Boolean = true
)

class Fighter(
  val species: FishSpecies,
  var x: Float,
  var y: Float,
  val isPlayer: Boolean,
  val displayName: String
) {
  var vx: Float = 0f
  var vy: Float = 0f
  var facingAngle: Float = if (isPlayer) 0f else PI.toFloat()
  var hp: Int = species.baseHp
  val maxHp: Int = species.baseHp
  var stamina: Float = 100f
  val maxStamina: Float = 100f

  var currentAction: ActionType = ActionType.IDLE
  var actionTimer: Float = 0f
  var shieldActive: Boolean = false
  var tailPhase: Float = 0f

  // Cooldowns
  var biteCooldown: Float = 0f
  var bubbleCooldown: Float = 0f
  var vortexCooldown: Float = 0f
  var specialCooldown: Float = 0f

  fun update(dt: Float, arenaWidth: Float, arenaHeight: Float) {
    // Regenerate stamina
    stamina = (stamina + 15f * dt).coerceAtMost(maxStamina)

    // Cooldown decrements
    biteCooldown = (biteCooldown - dt).coerceAtLeast(0f)
    bubbleCooldown = (bubbleCooldown - dt).coerceAtLeast(0f)
    vortexCooldown = (vortexCooldown - dt).coerceAtLeast(0f)
    specialCooldown = (specialCooldown - dt).coerceAtLeast(0f)

    // Action timer
    if (actionTimer > 0f) {
      actionTimer -= dt
      if (actionTimer <= 0f) {
        if (currentAction == ActionType.VORTEX_SHIELD) {
          shieldActive = false
        }
        currentAction = ActionType.IDLE
      }
    }

    // Water fluid physics
    x += vx * dt
    y += vy * dt

    // Fluid drag damping
    vx *= 0.92f
    vy *= 0.92f

    // Tail swimming animation
    val speed = sqrt(vx * vx + vy * vy)
    tailPhase += (speed * 0.05f + 4f) * dt

    // Arena boundary constraints
    val padding = 60f
    if (x < padding) { x = padding; vx = 0f }
    if (x > arenaWidth - padding) { x = arenaWidth - padding; vx = 0f }
    if (y < padding) { y = padding; vy = 0f }
    if (y > arenaHeight - padding) { y = arenaHeight - padding; vy = 0f }
  }

  fun move(inputX: Float, inputY: Float, dt: Float) {
    if (currentAction == ActionType.HIT_STUN) return

    val mag = sqrt(inputX * inputX + inputY * inputY)
    if (mag > 0.05f) {
      val targetAngle = atan2(inputY, inputX)
      // Smooth angle rotation
      facingAngle = targetAngle

      val accel = 850f * species.baseSpeed
      vx += (inputX / mag) * accel * dt
      vy += (inputY / mag) * accel * dt

      if (currentAction == ActionType.IDLE) {
        currentAction = ActionType.SWIM
      }
    } else {
      if (currentAction == ActionType.SWIM) {
        currentAction = ActionType.IDLE
      }
    }
  }

  fun canBite(): Boolean = biteCooldown <= 0f && stamina >= 20f && currentAction != ActionType.HIT_STUN
  fun canBubble(): Boolean = bubbleCooldown <= 0f && stamina >= 25f && currentAction != ActionType.HIT_STUN
  fun canVortex(): Boolean = vortexCooldown <= 0f && stamina >= 35f && currentAction != ActionType.HIT_STUN
  fun canSpecial(): Boolean = specialCooldown <= 0f && stamina >= 60f && currentAction != ActionType.HIT_STUN

  fun triggerBiteDash() {
    stamina -= 20f
    biteCooldown = 1.0f
    currentAction = ActionType.BITE_DASH
    actionTimer = 0.45f
    // Dash forward in facing direction
    val dashSpeed = 950f * species.baseSpeed
    vx = cos(facingAngle) * dashSpeed
    vy = sin(facingAngle) * dashSpeed
  }

  fun triggerBubbleCast() {
    stamina -= 25f
    bubbleCooldown = 1.2f
    currentAction = ActionType.BUBBLE_CAST
    actionTimer = 0.35f
  }

  fun triggerVortexShield() {
    stamina -= 35f
    vortexCooldown = 4.0f
    currentAction = ActionType.VORTEX_SHIELD
    actionTimer = 2.0f
    shieldActive = true
  }

  fun triggerSpecialTsunami() {
    stamina -= 60f
    specialCooldown = 7.0f
    currentAction = ActionType.TSUNAMI_BURST
    actionTimer = 0.8f
    val tsunamiBurst = 800f
    vx = cos(facingAngle) * tsunamiBurst
    vy = sin(facingAngle) * tsunamiBurst
  }

  fun takeDamage(amount: Int) {
    if (shieldActive) {
      // Shield absorbs 75% damage
      val reduced = (amount * 0.25f).toInt().coerceAtLeast(1)
      hp = (hp - reduced).coerceAtLeast(0)
    } else {
      hp = (hp - amount).coerceAtLeast(0)
      currentAction = ActionType.HIT_STUN
      actionTimer = 0.25f
    }
  }
}

class FishCombatEngine(
  val playerSpecies: FishSpecies,
  val opponentSpecies: FishSpecies,
  val playerName: String,
  val opponentName: String,
  val isMultiplayer: Boolean = false
) {
  var arenaWidth: Float = 1000f
  var arenaHeight: Float = 1600f

  val player = Fighter(playerSpecies, 300f, 900f, isPlayer = true, displayName = playerName)
  val opponent = Fighter(opponentSpecies, 700f, 400f, isPlayer = false, displayName = opponentName)

  val bubbles = mutableListOf<BubbleProjectile>()
  val particles = mutableListOf<WaterParticle>()
  val combatTexts = mutableListOf<CombatText>()

  var isGameOver: Boolean = false
  var isPlayerWinner: Boolean = false
  var comboCount: Int = 0
  var comboTimer: Float = 0f

  // Bot AI timers
  private var aiDecisionTimer: Float = 0f
  private var aiTargetX: Float = 500f
  private var aiTargetY: Float = 500f

  init {
    // Seed ambient floating bubbles
    for (i in 0..30) {
      particles.add(
        WaterParticle(
          x = Random.nextFloat() * 1000f,
          y = Random.nextFloat() * 1600f,
          vx = (Random.nextFloat() - 0.5f) * 20f,
          vy = -Random.nextFloat() * 50f - 15f,
          radius = Random.nextFloat() * 5f + 3f,
          alpha = Random.nextFloat() * 0.5f + 0.2f
        )
      )
    }
  }

  fun setArenaDimensions(w: Float, h: Float) {
    arenaWidth = w
    arenaHeight = h
    if (player.x > w) player.x = w * 0.3f
    if (opponent.x > w) opponent.x = w * 0.7f
  }

  fun update(dt: Float, joystickX: Float, joystickY: Float) {
    if (isGameOver) return

    // Update Combo Timer
    if (comboCount > 0) {
      comboTimer -= dt
      if (comboTimer <= 0f) {
        comboCount = 0
      }
    }

    // Player Movement
    player.move(joystickX, joystickY, dt)
    player.update(dt, arenaWidth, arenaHeight)

    // Opponent Movement (Local AI if not multiplayer)
    if (!isMultiplayer) {
      updateOpponentAI(dt)
    }
    opponent.update(dt, arenaWidth, arenaHeight)

    // Update Bubbles
    val bubbleIter = bubbles.iterator()
    while (bubbleIter.hasNext()) {
      val b = bubbleIter.next()
      b.x += b.vx * dt
      b.y += b.vy * dt
      b.wobblePhase += 8f * dt
      b.lifetime -= dt

      // Collision with targets
      val target = if (b.isFromPlayer) opponent else player
      val dx = b.x - target.x
      val dy = b.y - target.y
      val dist = sqrt(dx * dx + dy * dy)

      if (dist < target.species.baseAtk + 35f) {
        // Hit!
        target.takeDamage(b.damage)
        spawnSplashParticles(b.x, b.y, 8)
        spawnCombatText("-${b.damage}", b.x, b.y - 20f, false)

        if (b.isFromPlayer) {
          comboCount++
          comboTimer = 2.5f
        }
        bubbleIter.remove()
        continue
      }

      if (b.lifetime <= 0f || b.x < 0 || b.x > arenaWidth || b.y < 0 || b.y > arenaHeight) {
        spawnSplashParticles(b.x, b.y, 4)
        bubbleIter.remove()
      }
    }

    // Check Melee Collisions (Bite / Dash / Tsunami)
    checkMeleeCollision(player, opponent)
    checkMeleeCollision(opponent, player)

    // Update ambient particles
    for (p in particles) {
      p.x += p.vx * dt
      p.y += p.vy * dt
      if (p.y < -20f) {
        p.y = arenaHeight + 20f
        p.x = Random.nextFloat() * arenaWidth
      }
    }

    // Update combat text
    val textIter = combatTexts.iterator()
    while (textIter.hasNext()) {
      val ct = textIter.next()
      ct.y -= 40f * dt
      ct.alpha -= 0.8f * dt
      if (ct.alpha <= 0f) {
        textIter.remove()
      }
    }

    // Check Win/Loss conditions
    if (opponent.hp <= 0 && !isGameOver) {
      isGameOver = true
      isPlayerWinner = true
    } else if (player.hp <= 0 && !isGameOver) {
      isGameOver = true
      isPlayerWinner = false
    }
  }

  private fun checkMeleeCollision(attacker: Fighter, defender: Fighter) {
    if (attacker.currentAction == ActionType.BITE_DASH || attacker.currentAction == ActionType.TSUNAMI_BURST) {
      val dx = defender.x - attacker.x
      val dy = defender.y - attacker.y
      val dist = sqrt(dx * dx + dy * dy)
      val hitRadius = 75f

      if (dist < hitRadius) {
        val isTsunami = attacker.currentAction == ActionType.TSUNAMI_BURST
        val baseDamage = if (isTsunami) attacker.species.baseAtk * 2 else attacker.species.baseAtk
        val isCrit = Random.nextFloat() < 0.25f
        val finalDamage = if (isCrit) (baseDamage * 1.5f).toInt() else baseDamage

        defender.takeDamage(finalDamage)
        // Knockback defender
        val knockbackForce = if (isTsunami) 700f else 450f
        val kx = (dx / (dist + 0.1f)) * knockbackForce
        val ky = (dy / (dist + 0.1f)) * knockbackForce
        defender.vx = kx
        defender.vy = ky

        spawnSplashParticles(defender.x, defender.y, 16)
        spawnCombatText(if (isCrit) "CRIT -$finalDamage!" else "-$finalDamage", defender.x, defender.y - 30f, isCrit)

        if (attacker.isPlayer) {
          comboCount++
          comboTimer = 2.5f
        }

        // End attack dash
        attacker.actionTimer = 0.05f
      }
    }
  }

  private fun updateOpponentAI(dt: Float) {
    aiDecisionTimer -= dt
    if (aiDecisionTimer <= 0f) {
      aiDecisionTimer = Random.nextFloat() * 0.8f + 0.4f

      // Decide target movement
      val dx = player.x - opponent.x
      val dy = player.y - opponent.y
      val dist = sqrt(dx * dx + dy * dy)

      if (dist > 350f) {
        // Close in
        aiTargetX = player.x + (Random.nextFloat() - 0.5f) * 100f
        aiTargetY = player.y + (Random.nextFloat() - 0.5f) * 100f
      } else if (dist < 150f && opponent.canVortex()) {
        opponent.triggerVortexShield()
        spawnSplashParticles(opponent.x, opponent.y, 12)
      } else if (dist < 200f && opponent.canBite()) {
        // Face player and bite!
        opponent.facingAngle = atan2(dy, dx)
        opponent.triggerBiteDash()
        spawnSplashParticles(opponent.x, opponent.y, 10)
      } else if (opponent.canBubble()) {
        opponent.facingAngle = atan2(dy, dx)
        opponent.triggerBubbleCast()
        spawnBubbleAttack(opponent)
      } else {
        // Flank
        aiTargetX = player.x + (if (Random.nextBoolean()) 200f else -200f)
        aiTargetY = player.y + (Random.nextFloat() - 0.5f) * 120f
      }
    }

    val dirX = aiTargetX - opponent.x
    val dirY = aiTargetY - opponent.y
    val mag = sqrt(dirX * dirX + dirY * dirY)
    if (mag > 20f) {
      opponent.move(dirX / mag, dirY / mag, dt)
    }
  }

  fun playerBite() {
    if (player.canBite()) {
      player.triggerBiteDash()
      spawnSplashParticles(player.x, player.y, 12)
    }
  }

  fun playerBubble() {
    if (player.canBubble()) {
      player.triggerBubbleCast()
      spawnBubbleAttack(player)
    }
  }

  fun playerVortex() {
    if (player.canVortex()) {
      player.triggerVortexShield()
      spawnSplashParticles(player.x, player.y, 20)
    }
  }

  fun playerSpecial() {
    if (player.canSpecial()) {
      player.triggerSpecialTsunami()
      spawnSplashParticles(player.x, player.y, 35)
      // Fire 3 piercing tsunami waves
      val spread = 0.35f
      for (angleOffset in listOf(-spread, 0f, spread)) {
        val speed = 550f
        val a = player.facingAngle + angleOffset
        bubbles.add(
          BubbleProjectile(
            x = player.x + cos(a) * 45f,
            y = player.y + sin(a) * 45f,
            vx = cos(a) * speed,
            vy = sin(a) * speed,
            radius = 24f,
            isFromPlayer = true,
            damage = (player.species.baseAtk * 1.6f).toInt(),
            lifetime = 3.0f
          )
        )
      }
    }
  }

  private fun spawnBubbleAttack(fighter: Fighter) {
    val speed = 480f
    val spread = 0.15f
    for (offsetAngle in listOf(-spread, spread)) {
      val a = fighter.facingAngle + offsetAngle
      bubbles.add(
        BubbleProjectile(
          x = fighter.x + cos(a) * 35f,
          y = fighter.y + sin(a) * 35f,
          vx = cos(a) * speed,
          vy = sin(a) * speed,
          radius = 15f,
          isFromPlayer = fighter.isPlayer,
          damage = (fighter.species.baseAtk * 0.85f).toInt(),
          lifetime = 2.2f
        )
      )
    }
    spawnSplashParticles(fighter.x, fighter.y, 6)
  }

  private fun spawnSplashParticles(x: Float, y: Float, count: Int) {
    for (i in 0 until count) {
      val angle = Random.nextFloat() * 6.28f
      val speed = Random.nextFloat() * 120f + 40f
      particles.add(
        WaterParticle(
          x = x,
          y = y,
          vx = cos(angle) * speed,
          vy = sin(angle) * speed,
          radius = Random.nextFloat() * 6f + 2f,
          alpha = 0.8f,
          isAirBubble = Random.nextBoolean()
        )
      )
    }
  }

  private fun spawnCombatText(text: String, x: Float, y: Float, isCrit: Boolean) {
    combatTexts.add(CombatText(text = text, x = x, y = y, isCritical = isCrit))
  }
}
