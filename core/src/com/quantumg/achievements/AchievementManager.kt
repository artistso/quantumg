// QuantumG/achievements/AchievementManager.kt
package com.quantumg.achievements

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Color
import com.quantumg.core.GameState
import com.quantumg.nerd.EasterEggs
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

@Serializable
data class AchievementData(
    val id: String,
    val unlocked: Boolean = false,
    val progress: Int = 0,
    val maxProgress: Int = 0
)

@Serializable
data class AchievementSave(
    val achievements: MutableList<AchievementData> = mutableListOf()
)

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val color: Color,
    val maxProgress: Int,
    val rewardDescription: String
)

object AchievementManager {
    private const val SAVE_FILE = "achievements.json"
    private val json = Json { prettyPrint = true }
    private var saveData = AchievementSave()
    private var notificationCallback: ((String, String, Color) -> Unit)? = null

    val allAchievements: List<Achievement> by lazy {
        val base = listOf(
            Achievement("leeroy_jenkins", "LEEROY JENKINS!", "Kill 50 enemies in a single wave.", "⚔️", Color.RED, 50,
                "Your spells gain +10% damage for the next wave."),
            Achievement("plate_of_chicken", "Plate of Chicken", "Craft 10 spells in the Alchemy Crucible.", "🍗", Color.ORANGE, 10,
                "Unlocks the 'Chicken Feather' spell."),
            Achievement("dangerous_to_go_alone", "It's Dangerous to Go Alone!", "Craft your very first spell.", "🗡️", Color.GREEN, 1,
                "Grants a permanent +5 max HP."),
            Achievement("mega_buster", "Mega Buster!", "Kill 10 enemies with a single Superposition split.", "🔫", Color.CYAN, 10,
                "Superposition spells fire 1 additional clone."),
            Achievement("banjo_kazooie", "Banjo-Kazooie!", "Collect 50 total ingredients.", "🎵", Color.GOLD, 50,
                "Doubles the ingredient drop rate for 1 minute."),
            Achievement("wow_level_10", "For the Horde! (or Alliance)", "Reach player level 10.", "🏆", Color.PURPLE, 1,
                "Unlocks the 'War Stomp' talent node for free."),
            Achievement("quantum_master", "Quantum Master", "Unlock every spell in the game.", "⚛️", Color(0.2f, 0.8f, 1f, 1f), 1,
                "All spells cost 50% less mana permanently."),
            Achievement("boss_slayer", "Boss Slayer", "Defeat the Quantum Aberration boss.", "💀", Color(1f, 0.2f, 0.8f, 1f), 1,
                "Your max HP increases by 50 permanently."),
        )
        // Append hidden Easter egg achievements
        base + EasterEggs.hiddenAchievements
    }

    fun loadAchievements() {
        val file: FileHandle = Gdx.files.local(SAVE_FILE)
        if (!file.exists()) {
            saveData.achievements = allAchievements.map { AchievementData(it.id, false, 0, it.maxProgress) }.toMutableList()
            save()
            return
        }
        try {
            val jsonString = file.readString()
            saveData = json.decodeFromString<AchievementSave>(jsonString)
            for (ach in allAchievements) {
                if (saveData.achievements.none { it.id == ach.id }) {
                    saveData.achievements.add(AchievementData(ach.id, false, 0, ach.maxProgress))
                }
            }
            save()
        } catch (e: Exception) {
            Gdx.app.log("QuantumG", "Corrupt achievements! Resetting.")
            resetAll()
        }
    }

    fun save() {
        val jsonString = json.encodeToString(saveData)
        Gdx.files.local(SAVE_FILE).writeString(jsonString, false)
    }

    private fun resetAll() {
        saveData.achievements = allAchievements.map { AchievementData(it.id, false, 0, it.maxProgress) }.toMutableList()
        save()
    }

    fun progressAchievement(id: String, amount: Int = 1) {
        val entry = saveData.achievements.find { it.id == id } ?: return
        if (entry.unlocked) return
        entry.progress = minOf(entry.progress + amount, entry.maxProgress)
        if (entry.progress >= entry.maxProgress) {
            unlockAchievement(id)
        }
        save()
    }

    private fun unlockAchievement(id: String) {
        val entry = saveData.achievements.find { it.id == id } ?: return
        if (entry.unlocked) return
        entry.unlocked = true
        val ach = allAchievements.find { it.id == id } ?: return
        applyReward(ach)
        notificationCallback?.invoke(ach.title, ach.description, ach.color)
        Gdx.app.log("QuantumG", "ACHIEVEMENT UNLOCKED: ${ach.title}")
        save()
    }

    private fun applyReward(ach: Achievement) {
        when (ach.id) {
            "dangerous_to_go_alone" -> {
                GameState.player.maxHp += 5
                GameState.player.currentHp = GameState.player.maxHp
            }
            "wow_level_10" -> {
                GameState.player.talentPoints += 1
            }
            "boss_slayer" -> {
                GameState.player.maxHp += 50
                GameState.player.currentHp = GameState.player.maxHp
            }
        }
        GameState.saveGame()
    }

    fun isUnlocked(id: String): Boolean = saveData.achievements.find { it.id == id }?.unlocked ?: false
    fun getProgress(id: String): Int = saveData.achievements.find { it.id == id }?.progress ?: 0

    fun getAllWithStatus(): List<Pair<Achievement, AchievementData>> {
        return allAchievements.map { ach ->
            ach to (saveData.achievements.find { it.id == ach.id } ?: AchievementData(ach.id))
        }
    }

    fun setNotificationCallback(callback: (String, String, Color) -> Unit) {
        notificationCallback = callback
    }
}