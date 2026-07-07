// QuantumG/combat/QuantumProjectileSystem.kt
package com.quantumg.combat

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Array
import com.quantumg.data.QuantumState
import com.quantumg.data.Spell
import com.quantumg.core.GameState
import com.quantumg.themes.ThemeManager
import com.quantumg.talent.TalentManager
import kotlinx.coroutines.*

class QuantumProjectileSystem(private val shapeRenderer: ShapeRenderer) {
    private val projectiles = Array<Projectile>()
    private val entanglementLines = mutableListOf<Pair<Vector2, Vector2>>()
    private val maxProjectiles = 100
    private val scope = CoroutineScope(Dispatchers.Default)

    inner class Projectile(
        val id: String,
        val position: Vector2,
        val velocity: Vector2,
        val state: QuantumState,
        val damage: Float,
        val ownerId: String = "player",
        var isActive: Boolean = true,
        val isClone: Boolean = false,
    ) {
        val trail = mutableListOf<Vector2>()
        var lifeTime = 0f
        val maxLife = 5f
        val clones = mutableListOf<Projectile>()

        fun update(delta: Float) {
            lifeTime += delta
            if (lifeTime > maxLife) { isActive = false; return }
            position.add(velocity.x * delta, velocity.y * delta)
            trail.add(Vector2(position))
            if (trail.size > 20) trail.removeAt(0)

            when (state) {
                QuantumState.SUPERPOSITION -> {
                    if (clones.size < 3 && lifeTime % 0.5f < delta) {
                        val angle = MathUtils.random(0f, MathUtils.PI2)
                        val cloneVel = Vector2(velocity).rotateRad(angle).scl(1.2f)
                        val clone = Projectile(
                            id = "${id}_clone_${clones.size}",
                            position = Vector2(position),
                            velocity = cloneVel,
                            state = QuantumState.COLLAPSED,
                            damage = damage * 0.6f,
                            isClone = true,
                        )
                        clones.add(clone)
                    }
                }
                else -> { /* ENTANGLED, TUNNELING, COLLAPSED handled in collision */ }
            }
        }

        fun isOffScreen(): Boolean {
            return position.x < -50 || position.x > Gdx.graphics.width + 50 ||
                    position.y < -50 || position.y > Gdx.graphics.height + 50
        }
    }

    fun fireSpell(spell: Spell, source: Vector2, target: Vector2) {
        if (GameState.player.currentMana < spell.manaCost) {
            Gdx.app.log("QuantumG", "Not enough mana!")
            return
        }
        val direction = Vector2(target).sub(source).nor()
        val speed = 500f
        val projectile = Projectile(
            id = "proj_${System.currentTimeMillis()}_${MathUtils.random(1000)}",
            position = Vector2(source),
            velocity = direction.scl(speed),
            state = spell.effectType,
            damage = spell.baseDamage * (1 + (TalentManager.getTotalBonus("spell_damage") / 100))
        )
        projectiles.add(projectile)
        GameState.player.currentMana -= spell.manaCost
    }

    fun update(delta: Float) {
        val iterator = projectiles.iterator()
        while (iterator.hasNext()) {
            val proj = iterator.next()
            proj.update(delta)
            if (!proj.isActive || proj.isOffScreen()) {
                iterator.remove()
            }
            val cloneIter = proj.clones.iterator()
            while (cloneIter.hasNext()) {
                val clone = cloneIter.next()
                clone.update(delta)
                if (!clone.isActive || clone.isOffScreen()) {
                    cloneIter.remove()
                }
            }
        }
    }

    fun render() {
        val palette = ThemeManager.getCurrentTheme()
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        for (proj in projectiles) {
            val color = when (proj.state) {
                QuantumState.SUPERPOSITION -> palette.projectileColor
                QuantumState.ENTANGLED -> palette.secondary
                QuantumState.TUNNELING -> Color.ORANGE
                QuantumState.COLLAPSED -> Color.RED
            }
            shapeRenderer.color = color
            shapeRenderer.circle(proj.position.x, proj.position.y, 12f, 20)
            shapeRenderer.color = Color(color).mul(0.3f)
            for (point in proj.trail) {
                shapeRenderer.circle(point.x, point.y, 5f, 10)
            }
            for (clone in proj.clones) {
                shapeRenderer.color = Color(color).mul(0.5f)
                shapeRenderer.circle(clone.position.x, clone.position.y, 8f, 15)
                if (clone.trail.isNotEmpty()) {
                    shapeRenderer.color = Color(color).mul(0.2f)
                    val last = clone.trail.last()
                    shapeRenderer.circle(last.x, last.y, 4f, 8)
                }
            }
        }
        shapeRenderer.end()

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        for (link in entanglementLines) {
            shapeRenderer.color = Color.PURPLE
            shapeRenderer.line(link.first, link.second)
            shapeRenderer.color = Color.WHITE
            shapeRenderer.line(
                link.first.x + 2f, link.first.y + 2f,
                link.second.x + 2f, link.second.y + 2f
            )
        }
        shapeRenderer.end()
        entanglementLines.clear()
    }

    fun getAllProjectiles(): Array<Projectile> {
        val all = Array<Projectile>()
        for (proj in projectiles) {
            all.add(proj)
            for (clone in proj.clones) {
                all.add(clone)
            }
        }
        return all
    }

    fun createReflectedProjectile(source: Projectile, position: Vector2): Projectile {
        val reflected = Projectile(
            id = "reflected_${source.id}",
            position = Vector2(position),
            velocity = Vector2(source.velocity).scl(-1f),
            state = QuantumState.COLLAPSED,
            damage = source.damage * 0.5f,
            isClone = true,
        )
        projectiles.add(reflected)
        return reflected
    }

    fun linkEnemies(pos1: Vector2, pos2: Vector2) {
        entanglementLines.add(pos1 to pos2)
    }

    fun dispose() {
        scope.cancel()
    }
}