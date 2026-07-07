// QuantumG/core/GameState.kt
package com.quantumg.core

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.quantumg.data.PlayerCharacter
import com.quantumg.data.QuantumAnchor
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

@Serializable
data class SaveData(
    val player: PlayerCharacter,
    val unlockedAchievements: List<String>,
    val currentWave: Int,
    val totalPlayTimeSeconds: Long
)

object GameState {
    private const val SAVE_FILE = "quantumg_save.json"
    private val json = Json { prettyPrint = true }

    var player = PlayerCharacter()
    var currentWave = 1
    var enemiesKilled = 0
    var anchors: MutableList<QuantumAnchor> = mutableListOf()

    fun saveGame() {
        val data = SaveData(
            player = player,
            unlockedAchievements = listOf(),
            currentWave = currentWave,
            totalPlayTimeSeconds = (System.currentTimeMillis() / 1000)
        )
        val jsonString = json.encodeToString(data)
        val file: FileHandle = Gdx.files.local(SAVE_FILE)
        file.writeString(jsonString, false)
    }

    fun loadGame(): Boolean {
        val file: FileHandle = Gdx.files.local(SAVE_FILE)
        if (!file.exists()) {
            Gdx.app.log("QuantumG", "No save found. Starting fresh.")
            return false
        }
        try {
            val jsonString = file.readString()
            val data = json.decodeFromString<SaveData>(jsonString)
            this.player = data.player
            this.currentWave = data.currentWave
            Gdx.app.log("QuantumG", "Game Loaded Successfully!")
            return true
        } catch (e: Exception) {
            Gdx.app.log("QuantumG", "Corrupt save file! Starting fresh.")
            return false
        }
    }

    fun addXp(amount: Int) {
        player.xp += amount
        val xpNeeded = player.level * 100
        if (player.xp >= xpNeeded) {
            player.level += 1
            player.xp -= xpNeeded
            player.talentPoints += 1
            player.maxHp += 20
            player.currentHp = player.maxHp
            // Achievement check for level 10
            if (player.level == 10) {
                com.quantumg.achievements.AchievementManager.progressAchievement("wow_level_10", 1)
            }
            Gdx.app.log("QuantumG", "Level Up! Now Level ${player.level}")
            saveGame()
        }
    }
}