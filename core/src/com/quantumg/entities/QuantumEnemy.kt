// QuantumG/entities/QuantumEnemy.kt
package com.quantumg.entities

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.MathUtils
import com.quantumg.core.GameState
import com.quantumg.data.Ingredient
import com.quantumg.data.QuantumState
import com.quantumg.themes.ThemeManager
import com.badlogic.gdx.graphics.Color

open class QuantumEnemy(
    val id: String,
    var position: Vector2,
    var state: QuantumState,
    var hp: Float,
    val maxHp: Float,
    val speed: Float,
    val rewardXp: Int,
    val attackDamage: Float = 5f,
    var isActive: Boolean = true
) {
    private var targetPosition: Vector2? = null
    private var wanderTimer = 0f
    private val detectionRange = 400f

    // State-specific glow — respects current theme
    var glowColor: Color = ThemeManager.getCurrentTheme().enemyGlow

    open fun update(delta: Float, playerPos: Vector2) {
        if (!isActive) return

        val distToPlayer = position.dst(playerPos)

        if (distToPlayer < detectionRange) {
            val direction = Vector2(playerPos).sub(position).nor()
            position.add(direction.scl(speed * delta))
        } else {
            wanderTimer += delta
            if (wanderTimer > 2f || targetPosition == null) {
                val angle = MathUtils.random(0f, MathUtils.PI2)
                val radius = MathUtils.random(100f, 300f)
                targetPosition = Vector2(
                    position.x + MathUtils.cos(angle) * radius,
                    position.y + MathUtils.sin(angle) * radius
                )
                wanderTimer = 0f
            }
            targetPosition?.let { target ->
                val dir = Vector2(target).sub(position).nor()
                position.add(dir.scl(speed * delta * 0.5f))
            }
        }
    }

    open fun takeDamage(damage: Float): Boolean {
        hp -= damage
        if (hp <= 0) {
            hp = 0f
            isActive = false
            return true
        }
        return false
    }

    fun generateLoot(): List<Ingredient> {
        val count = MathUtils.random(1, 3)
        return List(count) { Ingredient.values().random() }
    }
}