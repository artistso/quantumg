// QuantumG/effects/ParticleBurst.kt
package com.quantumg.effects

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Array

data class Particle(
    var position: Vector2,
    var velocity: Vector2,
    var life: Float,
    val maxLife: Float,
    val size: Float,
    val color: Color,
    var isActive: Boolean = true
)

class ParticleBurst {
    private val particles = Array<Particle>()
    private val maxParticles = 300

    // Spawn an explosion at a given position with a specific color theme
    fun spawnExplosion(position: Vector2, color: Color, count: Int = 50, spread: Float = 300f) {
        for (i in 0 until count) {
            val angle = MathUtils.random(0f, MathUtils.PI2)
            val speed = MathUtils.random(50f, spread)
            val life = MathUtils.random(0.5f, 1.5f)
            val size = MathUtils.random(4f, 12f)
            val p = Particle(
                position = Vector2(position),
                velocity = Vector2(MathUtils.cos(angle) * speed, MathUtils.sin(angle) * speed),
                life = life,
                maxLife = life,
                size = size,
                color = Color(color).mul(MathUtils.random(0.7f, 1.0f))
            )
            particles.add(p)
        }
        trimExcess()
    }

    // Spawn a trail of particles behind a moving projectile
    fun spawnTrail(position: Vector2, color: Color, count: Int = 3) {
        for (i in 0 until count) {
            val p = Particle(
                position = Vector2(position).add(
                    MathUtils.random(-5f, 5f),
                    MathUtils.random(-5f, 5f)
                ),
                velocity = Vector2(MathUtils.random(-10f, 10f), MathUtils.random(-10f, 10f)),
                life = 0.3f,
                maxLife = 0.3f,
                size = MathUtils.random(2f, 5f),
                color = Color(color).mul(0.5f)
            )
            particles.add(p)
        }
        trimExcess()
    }

    private fun trimExcess() {
        while (particles.size > maxParticles) {
            particles.removeIndex(0)
        }
    }

    // Update all particles (called every frame)
    fun update(delta: Float) {
        val iter = particles.iterator()
        while (iter.hasNext()) {
            val p = iter.next()
            p.life -= delta
            if (p.life <= 0) {
                p.isActive = false
                iter.remove()
                continue
            }
            // Apply velocity (with some drag)
            p.position.add(p.velocity.x * delta, p.velocity.y * delta)
            p.velocity.scl(0.98f) // Slow down over time
        }
    }

    // Render all particles
    fun render(shapeRenderer: ShapeRenderer) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        for (p in particles) {
            if (!p.isActive) continue
            val alpha = p.life / p.maxLife
            shapeRenderer.color = Color(p.color).mul(alpha)
            shapeRenderer.circle(p.position.x, p.position.y, p.size * alpha, 10)
        }
        shapeRenderer.end()
    }

    fun isEmpty(): Boolean = particles.isEmpty()
    fun clear() { particles.clear() }
}