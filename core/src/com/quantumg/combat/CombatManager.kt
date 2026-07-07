// QuantumG/combat/CombatManager.kt
package com.quantumg.combat

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.MathUtils
import com.quantumg.core.GameState
import com.quantumg.data.QuantumState
import com.quantumg.data.Ingredient
import com.quantumg.entities.QuantumEnemy
import com.quantumg.entities.BossEnemy
import com.quantumg.spawner.WaveSpawner

class CombatManager(
    private val projectileSystem: QuantumProjectileSystem,
    private val waveSpawner: WaveSpawner,
    private val onLootDrop: (Vector2, List<Ingredient>) -> Unit,
    private val onBossSpawn: ((Vector2) -> Unit)? = null,
) {
    private val enemies = mutableListOf<QuantumEnemy>()

    fun addEnemy(enemy: QuantumEnemy) {
        enemies.add(enemy)
        if (enemy is BossEnemy) {
            onBossSpawn?.invoke(enemy.position)
        }
    }

    fun update(delta: Float, playerPos: Vector2) {
        for (enemy in enemies) {
            if (enemy.isActive) {
                enemy.update(delta, playerPos)
            }
        }
        enemies.removeAll { !it.isActive }
    }

    fun checkCollisions() {
        val projectiles = projectileSystem.getAllProjectiles()
        for (proj in projectiles) {
            if (!proj.isActive) continue
            for (enemy in enemies) {
                if (!enemy.isActive) continue
                val dist = proj.position.dst(enemy.position)
                if (dist < 30f) {
                    if (enemy is BossEnemy && enemy.isReflecting) {
                        val reflectedProj = projectileSystem.createReflectedProjectile(
                            proj, enemy.position
                        )
                    }
                    val killed = enemy.takeDamage(proj.damage)
                    when (proj.state) {
                        QuantumState.SUPERPOSITION -> {
                            for (nearby in enemies) {
                                if (nearby.id != enemy.id && nearby.isActive) {
                                    val d = nearby.position.dst(enemy.position)
                                    if (d < 100f) {
                                        nearby.takeDamage(proj.damage * 0.3f)
                                    }
                                }
                            }
                        }
                        QuantumState.ENTANGLED -> {
                            val nearest = enemies
                                .filter { it.id != enemy.id && it.isActive }
                                .minByOrNull { it.position.dst(enemy.position) }
                            if (nearest != null && nearest.position.dst(enemy.position) < 200f) {
                                projectileSystem.linkEnemies(enemy.position, nearest.position)
                                enemy.state = QuantumState.ENTANGLED
                                nearest.state = QuantumState.ENTANGLED
                            }
                        }
                        else -> { }
                    }
                    proj.isActive = false
                    if (killed) {
                        onEnemyDeath(enemy)
                    }
                    break
                }
            }
        }
    }

    private fun onEnemyDeath(enemy: QuantumEnemy) {
        GameState.addXp(enemy.rewardXp)
        GameState.enemiesKilled++
        val loot = enemy.generateLoot()
        for (ing in loot) {
            GameState.player.inventoryIngredients[ing] =
                (GameState.player.inventoryIngredients[ing] ?: 0) + 1
        }
        onLootDrop(enemy.position, loot)
        waveSpawner.onEnemyKilled()
        if (GameState.enemiesKilled % 5 == 0) {
            GameState.saveGame()
        }
    }

    fun getEnemyCount(): Int = enemies.count { it.isActive }
    fun getEnemyList(): List<QuantumEnemy> = enemies
}
