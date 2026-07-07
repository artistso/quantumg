// QuantumG/entities/TopologicalBoss.kt
package com.quantumg.entities

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.graphics.Color
import com.quantumg.topology.TopologicalQuantumField
import com.quantumg.core.GameState
import com.quantumg.data.QuantumState

class TopologicalBoss(
    id: String,
    position: Vector2,
    hp: Float,
    speed: Float,
    rewardXp: Int
) : QuantumEnemy(
    id = id,
    position = position,
    state = QuantumState.SUPERPOSITION,
    hp = hp,
    maxHp = hp,
    speed = speed,
    rewardXp = rewardXp,
    attackDamage = 25f
) {
    private val topologyEngine = TopologicalQuantumField()
    
    // Current morph state: "twist", "stretch", "fold", "normal"
    private var morphState = "normal"
    private var morphTimer = 0f
    private val morphInterval = 3f // Change every 3 seconds
    
    // The boss's shape is represented as a list of vertices (for rendering)
    var shapeVertices: List<Vector2> = generateDefaultShape()
    
    // Override glow color to reflect current morph
    override var glowColor = Color(1f, 0.4f, 0.8f, 1f) // Magenta base
    
    private fun generateDefaultShape(): List<Vector2> {
        // Start as a pentagon
        return List(5) { i ->
            val angle = i * MathUtils.PI2 / 5
            Vector2(
                position.x + MathUtils.cos(angle) * 40f,
                position.y + MathUtils.sin(angle) * 40f
            )
        }
    }
    
    override fun update(delta: Float, playerPos: Vector2) {
        if (!isActive) return
        
        // 1. Update morph state
        morphTimer += delta
        if (morphTimer >= morphInterval) {
            morphTimer = 0f
            morphState = listOf("twist", "stretch", "fold", "normal").random()
            // Apply homeomorphism to shape
            applyMorph()
            // Update visual properties
            glowColor = when (morphState) {
                "twist" -> Color(0.2f, 1f, 0.5f, 1f) // Cyan-green
                "stretch" -> Color(1f, 0.8f, 0.0f, 1f) // Gold
                "fold" -> Color(1f, 0.0f, 0.0f, 1f) // Red
                else -> Color(1f, 0.4f, 0.8f, 1f) // Magenta
            }
        }
        
        // 2. Move toward player (but slightly slower when morphing)
        val dir = Vector2(playerPos).sub(position).nor()
        val morphSpeedFactor = when (morphState) {
            "twist" -> 0.7f
            "stretch" -> 1.0f
            "fold" -> 0.5f
            else -> 0.8f
        }
        position.add(dir.scl(speed * delta * morphSpeedFactor))
        
        // 3. Update shape vertices to follow position
        shapeVertices = shapeVertices.map { 
            val offset = it.cpy().sub(position) // Relative to boss center
            Vector2(position.x + offset.x, position.y + offset.y)
        }
    }
    
    private fun applyMorph() {
        // Convert shape vertices to list of Pairs for topology engine
        val pairs = shapeVertices.map { it.x.toDouble() to it.y.toDouble() }
        val transformed = topologyEngine.applyHomeomorphism(pairs, morphState)
        shapeVertices = transformed.map { Vector2(it.first.toFloat(), it.second.toFloat()) }
    }
    
    // Override takeDamage to add a morph-based damage reduction
    override fun takeDamage(damage: Float): Boolean {
        // When in "fold" state, boss takes 50% less damage
        val effectiveDamage = if (morphState == "fold") damage * 0.5f else damage
        return super.takeDamage(effectiveDamage)
    }
    
    // Render the boss as a polygon (called from CombatManager)
    fun render(shapeRenderer: ShapeRenderer) {
        if (!isActive) return
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        shapeRenderer.color = glowColor
        for (i in 0 until shapeVertices.size) {
            val j = (i + 1) % shapeVertices.size
            shapeRenderer.line(shapeVertices[i], shapeVertices[j])
        }
        shapeRenderer.end()
        
        // Draw health bar
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        val hpPercent = hp / maxHp
        shapeRenderer.color = Color.GREEN
        shapeRenderer.rect(position.x - 30f, position.y + 50f, 60f * hpPercent, 5f)
        shapeRenderer.color = Color.RED
        shapeRenderer.rect(position.x - 30f + 60f * hpPercent, position.y + 50f, 60f * (1 - hpPercent), 5f)
        shapeRenderer.end()
    }
}