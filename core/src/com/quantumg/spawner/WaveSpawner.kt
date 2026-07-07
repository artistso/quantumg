// QuantumG/spawner/WaveSpawner.kt
package com.quantumg.spawner

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.quantumg.core.GameState
import com.quantumg.data.QuantumState
import com.quantumg.entities.QuantumEnemy
import com.quantumg.talent.TalentManager

class WaveSpawner(
    private val arenaWidth: Float,
    private val arenaHeight: Float,
    private val onEnemySpawned: (QuantumEnemy) -> Unit,
    private val onWaveComplete: () -> Unit
) {
    private var enemiesPerWave = 5
    private var enemiesSpawned = 0
    private var enemiesAlive = 0
    private var spawnTimer = 0f
    private val spawnInterval = 1.5f
    private var isSpawning = false
    private var bossSpawned = false

    fun markBossSpawned() { bossSpawned = true }
    fun hasBossSpawned(): Boolean = bossSpawned

    private fun getScaledEnemyCount(): Int = 5 + (GameState.player.level - 1) * 2
    private fun getScaledEnemyHp(): Float = 20f + (GameState.player.level - 1) * 8f
    private fun getScaledEnemySpeed(): Float = 60f + (GameState.player.level - 1) * 5f
    private fun getScaledXpReward(): Int = 10 + (GameState.player.level - 1) * 5

    fun startWave() {
        enemiesPerWave = getScaledEnemyCount()
        enemiesSpawned = 0
        enemiesAlive = 0
        spawnTimer = 0f
        isSpawning = true
        bossSpawned = false
        Gdx.app.log("QuantumG", "Wave ${GameState.currentWave} starting! $enemiesPerWave enemies.")
    }

    fun update(delta: Float, playerPos: Vector2) {
        if (!isSpawning) return
        spawnTimer += delta
        if (spawnTimer >= spawnInterval && enemiesSpawned < enemiesPerWave) {
            spawnTimer = 0f
            spawnEnemy(playerPos)
        }
        if (enemiesSpawned >= enemiesPerWave && enemiesAlive == 0) {
            isSpawning = false
            GameState.currentWave += 1
            Gdx.app.log("QuantumG", "Wave ${GameState.currentWave - 1} complete!")
            onWaveComplete()
            GameState.saveGame()
        }
    }

    private fun spawnEnemy(playerPos: Vector2) {
        val edge = MathUtils.random(0, 3)
        val x = when (edge) {
            0 -> MathUtils.random(0f, arenaWidth)
            1 -> MathUtils.random(0f, arenaWidth)
            2 -> 0f
            else -> arenaWidth
        }
        val y = when (edge) {
            0 -> arenaHeight
            1 -> 0f
            2 -> MathUtils.random(0f, arenaHeight)
            else -> MathUtils.random(0f, arenaHeight)
        }
        val state = QuantumState.values().random()
        val enemy = QuantumEnemy(
            id = "enemy_${System.currentTimeMillis()}_${MathUtils.random(1000)}",
            position = Vector2(x, y),
            state = state,
            hp = getScaledEnemyHp(),
            maxHp = getScaledEnemyHp(),
            speed = getScaledEnemySpeed(),
            rewardXp = getScaledXpReward()
        )
        enemiesAlive++
        enemiesSpawned++
        onEnemySpawned(enemy)
    }

    fun onEnemyKilled() { enemiesAlive-- }
    fun isWaveActive(): Boolean = isSpawning || enemiesAlive > 0
}