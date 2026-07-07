// QuantumG/entities/BossEnemy.kt
package com.quantumg.entities

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.graphics.Color
import com.quantumg.data.QuantumState
import com.quantumg.core.GameState

class BossEnemy(
    id: String,
    position: Vector2,
    hp: Float,
    speed: Float,
    rewardXp: Int
) : QuantumEnemy(
    id = id,
    position = position,
    state = QuantumState.SUPERPOSITION, // Boss uses Superposition as default
    hp = hp,
    maxHp = hp,
    speed = speed,
    rewardXp = rewardXp,
    attackDamage = 20f
) {
    // Boss-specific properties
    var reflectionCooldown = 0f
    var isReflecting = false
    val reflectDuration = 1.5f
    val reflectInterval = 4f // Reflects every 4 seconds

    // Boss glow is bigger and has a pulsing effect
    override var glowColor = Color(1f, 0.2f, 0.8f, 1f) // Hot pink

    // Split mechanic
    private var hasSplit = false

    override fun update(delta: Float, playerPos: Vector2) {
        if (!isActive) return

        // 1. Handle reflection cooldown
        reflectionCooldown -= delta
        if (reflectionCooldown <= 0) {
            isReflecting = true
            reflectionCooldown = reflectInterval
            // Visual cue: flash white
            glowColor = Color.WHITE
        }
        if (isReflecting && reflectionCooldown > reflectInterval - reflectDuration) {
            // Keep reflecting
        } else {
            isReflecting = false
            glowColor = Color(1f, 0.2f, 0.8f, 1f) // Reset to pink
        }

        // 2. Boss moves TOWARD the player (aggressive)
        val dir = Vector2(playerPos).sub(position).nor()
        position.add(dir.scl(speed * delta * 1.2f)) // Boss is faster

        // 3. Split mechanic (at 50% HP)
        if (!hasSplit && hp < maxHp * 0.5f) {
            hasSplit = true
            // Spawn 3 mini-enemies in a circle around the boss
            // We'll handle this in the CombatManager callback
        }
    }

    // Override takeDamage to add reflection logic
    override fun takeDamage(damage: Float): Boolean {
        if (isReflecting) {
            // Reflect 100% of the damage back to the player! (We'll handle this in the CombatManager)
            // For now, we just return false (no damage taken) and log it.
            println("💥 BOSS REFLECTED DAMAGE!")
            return false
        }
        return super.takeDamage(damage)
    }

    // When boss dies, it drops high-tier ingredients
    override fun generateLoot(): List<Ingredient> {
        // Always drops a Boson + 3 random others
        return listOf(Ingredient.BOSON) + List(3) { Ingredient.values().random() }
    }
}