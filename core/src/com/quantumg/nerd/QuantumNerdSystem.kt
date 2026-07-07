// QuantumG/nerd/QuantumNerdSystem.kt
package com.quantumg.nerd

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.JsonReader
import com.badlogic.gdx.utils.JsonValue
import com.badlogic.gdx.utils.JsonWriter
import com.quantumg.core.GameState
import com.quantumg.data.Ingredient
import com.quantumg.data.Spell
import com.quantumg.data.QuantumState
import org.ejml.simple.SimpleMatrix
import kotlin.math.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

// ============================================================
// 1. LORE & CODEX (Dark Souls style)
// ============================================================
@Serializable
data class CodexEntry(
    val id: String,
    val title: String,
    val text: String,
    var unlocked: Boolean = false
)

@Serializable
data class Glyph(
    val positionX: Float,
    val positionY: Float,
    val symbol: String, // e.g., "Δ", "Ψ", "Ω"
    var collected: Boolean = false
)

// ============================================================
// 2. STATS TRACKER (Data Junkie)
// ============================================================
@Serializable
data class PlayerStats(
    var totalDamageDealt: Double = 0.0,
    var totalSpellsCast: Int = 0,
    var mostUsedSpell: String = "None",
    var favoriteIngredient: String = "None",
    var averageWaveTime: Float = 0f,
    var totalParticlesSpawned: Int = 0,
    var bossKills: Int = 0,
    var timesDied: Int = 0,
    var distanceTraveled: Float = 0f,
    var longestWave: Int = 0,
    var totalIngredientCount: MutableMap<String, Int> = mutableMapOf()
)

// ============================================================
// 3. DIFFICULTY & CHALLENGES
// ============================================================
enum class GameMode {
    NORMAL,
    HARDCORE,      // Permadeath
    SPEEDRUN,      // Timer active
    IRON_MAN,      // No healing
    NO_UPGRADES    // No talents/alchemy
}

// ============================================================
// 4. COSMETICS & SKINS (Fashion Nerd)
// ============================================================
enum class PlayerSkin(val displayName: String, val color: Color) {
    DEFAULT("Quantum Mage", Color(0.2f, 0.8f, 1f, 1f)),
    ARCHIVIST("The Archivist", Color(0.9f, 0.9f, 0.5f, 1f)),
    ALCHEMIST("The Alchemist", Color(0.2f, 1f, 0.2f, 1f)),
    MAD_SCIENTIST("Mad Scientist", Color(1f, 0.5f, 0.0f, 1f)),
    LEEROY("Leeroy Jenkins", Color(1f, 0.2f, 0.2f, 1f)),
    ZELDA("Hero of Time", Color(0.0f, 0.8f, 0.2f, 1f)),
    MEGA_MAN("Mega Buster", Color(0.0f, 0.6f, 1f, 1f)),
    BANJO("Banjo & Kazooie", Color(1f, 0.6f, 0.0f, 1f))
}

// ============================================================
// 5. GENETIC ALGORITHM / EVOLUTION (Enemy adaptation)
// ============================================================
@Serializable
data class EnemySpecies(
    val id: String,
    var resistanceToStates: MutableMap<String, Float> = mutableMapOf(),
    var survivalCount: Int = 0,
    var generation: Int = 0
)

// ============================================================
// 6. PUZZLE SYSTEM (Brain Teaser)
// ============================================================
@Serializable
data class QuantumPuzzle(
    val id: String,
    val description: String,
    val targetMatrix: List<List<Int>>, // 2x2 or 3x3 matrix
    val initialState: List<List<Int>>,
    val reward: String // e.g., "+5 Max Mana"
)

// ============================================================
// 7. MOD SUPPORT (JSON Loader)
// ============================================================
data class ModSpell(
    val id: String,
    val name: String,
    val damage: Float,
    val state: String
)

// ============================================================
// 8. COSMIC EVENT CALENDAR
// ============================================================
data class CosmicEvent(
    val id: String,
    val name: String,
    val description: String,
    val month: Int,
    val day: Int,
    val modifier: (Float) -> Float // modifies damage, speed, etc.
)

// ============================================================
// 9. THE MASTER NERD STATE (Saves everything)
// ============================================================
@Serializable
data class NerdSaveData(
    var codex: MutableList<CodexEntry> = mutableListOf(),
    var glyphs: MutableList<Glyph> = mutableListOf(),
    var stats: PlayerStats = PlayerStats(),
    var gameMode: GameMode = GameMode.NORMAL,
    var currentSkin: PlayerSkin = PlayerSkin.DEFAULT,
    var unlockedSkins: MutableList<String> = mutableListOf(PlayerSkin.DEFAULT.name),
    var enemySpecies: MutableList<EnemySpecies> = mutableListOf(),
    var completedPuzzles: MutableList<String> = mutableListOf(),
    var currentSeed: String = "",
    var hardcoreRunActive: Boolean = false,
    var isGodMode: Boolean = false,
    var isCatMode: Boolean = false,
    var waveBossRushUnlocked: Boolean = false,
    var totalPlayTime: Float = 0f,
    var lastSaveTimestamp: Long = 0L
)

// ============================================================
// 10. THE MAIN NERD ENGINE
// ============================================================
object QuantumNerdSystem {
    private const val SAVE_FILE = "quantum_nerd_data.json"
    private val json = Json { prettyPrint = true }
    
    // --- State ---
    var nerdData = NerdSaveData()
    var cheatBuffer = "" // For Konami code style
    var speedrunTimer = 0f
    var isSpeedrunActive = false
    
    // --- Callbacks (set these in your main game) ---
    var onCodexUnlock: ((String) -> Unit)? = null
    var onPuzzleComplete: ((String) -> Unit)? = null
    var onCosmicEventTrigger: ((String) -> Unit)? = null
    var onBossRushStart: (() -> Unit)? = null
    
    // ============================================================
    // 1. CODEX & LORE
    // ============================================================
    private val allCodexEntries = listOf(
        CodexEntry("collapse_war", "The Collapse War", "In the age of deterministic observation, the Entangled Empire fought the Superposition Federation..."),
        CodexEntry("schrodinger_schism", "The Schrödinger Schism", "When the cat was both alive and dead, the universe split into countless branches..."),
        CodexEntry("entangled_empire", "The Entangled Empire", "They believed that connection was the ultimate truth. Every particle was linked..."),
        CodexEntry("planck_constant", "Planck's Secret", "ℏ = 6.62607015 × 10⁻³⁴ J·s. The quantum of action. The smallest unit of change."),
        CodexEntry("tunneling_travelers", "Tunneling Travelers", "Those who pass through barriers are not lost. They are merely in a different potential well."),
    )
    
    fun unlockCodexEntry(id: String) {
        val entry = allCodexEntries.find { it.id == id } ?: return
        val existing = nerdData.codex.find { it.id == id }
        if (existing != null) {
            if (!existing.unlocked) {
                existing.unlocked = true
                onCodexUnlock?.invoke(entry.title)
                save()
            }
        } else {
            val newEntry = entry.copy(unlocked = true)
            nerdData.codex.add(newEntry)
            onCodexUnlock?.invoke(entry.title)
            save()
        }
    }
    
    fun getCodexProgress(): Int {
        return nerdData.codex.count { it.unlocked }
    }
    
    // ============================================================
    // 2. HIDDEN GLYPHS (Metroidvania)
    // ============================================================
    fun checkGlyphCollection(playerX: Float, playerY: Float): Boolean {
        for (glyph in nerdData.glyphs) {
            if (!glyph.collected) {
                val dist = sqrt((glyph.positionX - playerX).pow(2) + (glyph.positionY - playerY).pow(2))
                if (dist < 50f) {
                    glyph.collected = true
                    // Triggers special event
                    if (nerdData.glyphs.all { it.collected }) {
                        unlockCodexEntry("entangled_empire")
                        // Spawn a secret boss!
                    }
                    save()
                    return true
                }
            }
        }
        return false
    }
    
    fun generateGlyphs(arenaWidth: Float, arenaHeight: Float) {
        if (nerdData.glyphs.isEmpty()) {
            nerdData.glyphs = listOf(
                Glyph(arenaWidth * 0.2f, arenaHeight * 0.2f, "Δ"),
                Glyph(arenaWidth * 0.8f, arenaHeight * 0.15f, "Ψ"),
                Glyph(arenaWidth * 0.1f, arenaHeight * 0.8f, "Ω"),
                Glyph(arenaWidth * 0.9f, arenaHeight * 0.7f, "∇"),
                Glyph(arenaWidth * 0.5f, arenaHeight * 0.5f, "∞")
            ).toMutableList()
            save()
        }
    }
    
    // ============================================================
    // 3. MATH CONSTANTS (Secret Numbers - Pi, e, Phi, Hbar)
    // ============================================================
    object QuantumConstants {
        const val PI = Math.PI
        const val E = Math.E
        const val PHI = 1.618033988749895 // Golden Ratio
        const val HBAR = 1.054571817e-34 // Planck's constant over 2π
        const val FIBONACCI = 1.618033988749895 // Close enough!
        
        fun applyPiBoost(baseDamage: Double): Double {
            return baseDamage * (1 + PI / 10)
        }
        
        fun applyPhiScaling(level: Int): Double {
            return level.toDouble() * PHI / 2
        }
        
        fun entanglementProbability(): Double {
            // Use hbar to make it incredibly small, but we scale it to game values
            return (HBAR * 1e34).toDouble().coerceIn(0.0, 1.0)
        }
    }
    
    // ============================================================
    // 4. STATS TRACKING (Data Junkie)
    // ============================================================
    fun trackDamage(damage: Float) {
        nerdData.stats.totalDamageDealt += damage
        save()
    }
    
    fun trackSpellCast(spellId: String) {
        nerdData.stats.totalSpellsCast++
        val current = nerdData.stats.mostUsedSpell
        // Simple majority: just track counts in a separate map
        if (nerdData.stats.mostUsedSpell == "None") {
            nerdData.stats.mostUsedSpell = spellId
        }
        save()
    }
    
    fun trackIngredient(ing: Ingredient) {
        val name = ing.name
        nerdData.stats.totalIngredientCount[name] = (nerdData.stats.totalIngredientCount[name] ?: 0) + 1
        // Update favorite
        val favorite = nerdData.stats.totalIngredientCount.maxByOrNull { it.value }
        if (favorite != null) {
            nerdData.stats.favoriteIngredient = favorite.key
        }
        save()
    }
    
    fun trackDistanceMoved(dx: Float, dy: Float) {
        nerdData.stats.distanceTraveled += sqrt(dx*dx + dy*dy)
        save()
    }
    
    fun trackDeath() {
        nerdData.stats.timesDied++
        if (nerdData.gameMode == GameMode.HARDCORE && nerdData.hardcoreRunActive) {
            // Wipe save! (Permadeath)
            Gdx.app.log("QuantumG", "💀 HARDCORE: Game Over. Save wiped.")
            nerdData.hardcoreRunActive = false
            // We handle the wipe logic outside
        }
        save()
    }
    
    // ============================================================
    // 5. DIFFICULTY MODES
    // ============================================================
    fun setMode(mode: GameMode) {
        nerdData.gameMode = mode
        when (mode) {
            GameMode.HARDCORE -> nerdData.hardcoreRunActive = true
            GameMode.SPEEDRUN -> {
                isSpeedrunActive = true
                speedrunTimer = 0f
            }
            GameMode.IRON_MAN -> {
                // Disable health regen
                // We'll handle this in the game loop
            }
            GameMode.NO_UPGRADES -> {
                // Disable talents and alchemy
            }
            else -> {}
        }
        save()
    }
    
    // ============================================================
    // 6. CHEAT CODES (Konami Style)
    // ============================================================
    private val cheatMap = mapOf(
        "KITTY" to { toggleCatMode() },
        "GOD" to { toggleGodMode() },
        "SILVER" to { setManaCheat() },
        "PLATE" to { spawnChicken() },
        "LEEROY" to { triggerLeeroy() },
        "LEVELUP" to { instantLevelUp() },
        "UNLOCKALL" to { unlockAll() },
        "BOSS" to { spawnBossNow() },
        "HEAL" to { healPlayer() },
        "SUPERPOSITION" to { unlockSuperSpell() }
    )
    
    fun processCheat(input: String) {
        cheatBuffer += input.uppercase()
        if (cheatBuffer.length > 20) cheatBuffer = cheatBuffer.takeLast(20)
        
        for ((code, action) in cheatMap) {
            if (cheatBuffer.contains(code)) {
                action()
                cheatBuffer = "" // Reset after triggering
                Gdx.app.log("QuantumG", "🔓 Cheat Activated: $code")
                break
            }
        }
    }
    
    private fun toggleGodMode() {
        nerdData.isGodMode = !nerdData.isGodMode
        Gdx.app.log("QuantumG", if (nerdData.isGodMode) "🛡️ God Mode ON" else "🛡️ God Mode OFF")
    }
    
    private fun toggleCatMode() {
        nerdData.isCatMode = !nerdData.isCatMode
        Gdx.app.log("QuantumG", if (nerdData.isCatMode) "🐱 Cat Mode ON" else "🐱 Cat Mode OFF")
    }
    
    private fun setManaCheat() {
        GameState.player.currentMana = 9999
        Gdx.app.log("QuantumG", "💎 Unlimited Mana!")
    }
    
    private fun spawnChicken() {
        Gdx.app.log("QuantumG", "🍗 Plate of Chicken spawned!")
        // In the main game, we'd create a chicken entity.
    }
    
    private fun triggerLeeroy() {
        Gdx.app.log("QuantumG", "⚔️ LEEROY JENKINS!!! Enemies are charging!")
        // In the main game, we'd modify enemy AI to rush.
    }
    
    private fun instantLevelUp() {
        GameState.addXp(10000)
        Gdx.app.log("QuantumG", "⬆️ LEVEL UP!")
    }
    
    private fun unlockAll() {
        // Unlock all talents, spells, and achievements
        // (We'll hook this up later)
        Gdx.app.log("QuantumG", "🏆 ALL UNLOCKED!")
    }
    
    private fun spawnBossNow() {
        Gdx.app.log("QuantumG", "👾 Boss spawned!")
        // Hook up to WaveSpawner
    }
    
    private fun healPlayer() {
        GameState.player.currentHp = GameState.player.maxHp
        Gdx.app.log("QuantumG", "❤️ Fully healed!")
    }
    
    private fun unlockSuperSpell() {
        Gdx.app.log("QuantumG", "⚡ Superposition Master Spell Unlocked!")
        // Add a spell to the player's inventory
    }
    
    // ============================================================
    // 7. GENETIC ALGORITHM EVOLUTION (Enemy Adaptation)
    // ============================================================
    fun trackEnemySurvival(enemyId: String, state: String) {
        var species = nerdData.enemySpecies.find { it.id == enemyId }
        if (species == null) {
            species = EnemySpecies(id = enemyId)
            nerdData.enemySpecies.add(species)
        }
        species.survivalCount++
        species.generation++
        // Increase resistance to the state that killed it (if any)
        if (state.isNotEmpty()) {
            species.resistanceToStates[state] = (species.resistanceToStates[state] ?: 0f) + 0.05f
        }
        save()
    }
    
    fun getResistanceAgainst(enemyId: String, state: String): Float {
        val species = nerdData.enemySpecies.find { it.id == enemyId }
        return species?.resistanceToStates?.get(state) ?: 0f
    }
    
    // ============================================================
    // 8. QUANTUM PUZZLES (Brain Teasers)
    // ============================================================
    private val puzzleLibrary = listOf(
        QuantumPuzzle(
            id = "puzzle_1",
            description = "Arrange the qubits to create a Bell state:\n|Φ+⟩ = (|00⟩ + |11⟩)/√2",
            targetMatrix = listOf(listOf(1, 0), listOf(0, 1)),
            initialState = listOf(listOf(0, 1), listOf(1, 0)),
            reward = "+5 Max Mana"
        ),
        QuantumPuzzle(
            id = "puzzle_2",
            description = "Apply the Hadamard gate to create superposition:\nH|0⟩ = (|0⟩ + |1⟩)/√2",
            targetMatrix = listOf(listOf(1, 1), listOf(1, -1)),
            initialState = listOf(listOf(1, 0), listOf(0, 1)),
            reward = "+10 Max HP"
        )
    )
    
    fun generatePuzzleForWave(wave: Int): QuantumPuzzle? {
        if (wave % 5 == 0) {
            val idx = ((wave / 5) - 1) % puzzleLibrary.size
            return puzzleLibrary[idx]
        }
        return null
    }
    
    fun solvePuzzle(puzzleId: String, matrix: List<List<Int>>): Boolean {
        val puzzle = puzzleLibrary.find { it.id == puzzleId } ?: return false
        if (nerdData.completedPuzzles.contains(puzzleId)) return false
        
        // Check if matrix matches target
        var matches = true
        for (i in puzzle.targetMatrix.indices) {
            for (j in puzzle.targetMatrix[i].indices) {
                if (puzzle.targetMatrix[i][j] != matrix[i][j]) {
                    matches = false
                    break
                }
            }
        }
        
        if (matches) {
            nerdData.completedPuzzles.add(puzzleId)
            onPuzzleComplete?.invoke(puzzle.reward)
            save()
            return true
        }
        return false
    }
    
    // ============================================================
    // 9. BOSS RUSH MODE
    // ============================================================
    fun unlockBossRush() {
        nerdData.waveBossRushUnlocked = true
        onBossRushStart?.invoke()
        save()
    }
    
    fun isBossRushUnlocked(): Boolean = nerdData.waveBossRushUnlocked
    
    // ============================================================
    // 10. COSMIC EVENTS (Calendar-based)
    // ============================================================
    private val cosmicEvents = listOf(
        CosmicEvent("new_year", "Quantum Fireworks", "All particles glow and split!", 1, 1) { it * 1.5f },
        CosmicEvent("halloween", "Ghostly Tunneling", "Enemies become ethereal!", 10, 31) { it * 0.8f },
        CosmicEvent("pi_day", "Pi Day", "Damage multiplied by π!", 3, 14) { (it * Math.PI).toFloat() },
        CosmicEvent("christmas", "Quantum Snow", "Slow motion effect!", 12, 25) { it * 0.5f },
        CosmicEvent("earth_day", "Entangled Earth", "Healing is doubled!", 4, 22) { it * 2.0f },
        CosmicEvent("nerd_day", "May the 4th", "Star Wars references everywhere!", 5, 4) { it * 1.1f }
    )
    
    private var activeEvent: CosmicEvent? = null
    
    fun checkCosmicEvents(): CosmicEvent? {
        val today = Gdx.app.preferences.getInteger("current_day", -1)
        val currentMonth = if (today == -1) 1 else today // Simplified: we'll trust the system time via Gdx.app?
        // Actually, we can just use the device's date via Java Calendar if available.
        // For LibGDX, we'll use a simple check.
        // Let's just check all events and see if today matches.
        // Since we can't easily get the date in GWT, we'll use a placeholder:
        // In production, use: Calendar.getInstance().get(Calendar.MONTH) + 1, Calendar.DAY_OF_MONTH
        // For now, we'll just return the first event if none active.
        if (activeEvent == null) {
            // Placeholder: just activate Pi Day for demo
            activeEvent = cosmicEvents.find { it.month == 3 && it.day == 14 } ?: cosmicEvents.first()
            onCosmicEventTrigger?.invoke(activeEvent!!.name)
        }
        return activeEvent
    }
    
    fun applyCosmicModifier(baseValue: Float): Float {
        return activeEvent?.modifier?.invoke(baseValue) ?: baseValue
    }
    
    // ============================================================
    // 11. MOD SUPPORT (JSON Loader)
    // ============================================================
    fun loadMods(): List<ModSpell> {
        val modsDir = Gdx.files.local("mods/")
        if (!modsDir.exists()) {
            modsDir.mkdirs()
            // Create a sample mod
            val sampleMod = """
                {
                    "id": "sample_mod",
                    "name": "Chicken Launcher",
                    "damage": 999.0,
                    "state": "SUPERPOSITION"
                }
            """.trimIndent()
            Gdx.files.local("mods/sample_mod.json").writeString(sampleMod, false)
        }
        
        val loadedSpells = mutableListOf<ModSpell>()
        for (file in modsDir.list()) {
            if (file.extension() == "json") {
                try {
                    val jsonContent = file.readString()
                    val reader = JsonReader()
                    val root = reader.parse(jsonContent)
                    val id = root.getString("id")
                    val name = root.getString("name")
                    val damage = root.getFloat("damage")
                    val state = root.getString("state")
                    loadedSpells.add(ModSpell(id, name, damage, state))
                    Gdx.app.log("QuantumG", "✅ Mod loaded: $name")
                } catch (e: Exception) {
                    Gdx.app.log("QuantumG", "❌ Failed to load mod: ${file.name()}")
                }
            }
        }
        return loadedSpells
    }
    
    // ============================================================
    // 12. EXPORT STATS TO CSV (Spreadsheet Warrior)
    // ============================================================
    fun exportStatsToCSV() {
        val stats = nerdData.stats
        val csv = buildString {
            appendLine("Stat,Value")
            appendLine("Total Damage Dealt,${stats.totalDamageDealt}")
            appendLine("Total Spells Cast,${stats.totalSpellsCast}")
            appendLine("Most Used Spell,${stats.mostUsedSpell}")
            appendLine("Favorite Ingredient,${stats.favoriteIngredient}")
            appendLine("Average Wave Time,${stats.averageWaveTime}")
            appendLine("Total Particles Spawned,${stats.totalParticlesSpawned}")
            appendLine("Boss Kills,${stats.bossKills}")
            appendLine("Times Died,${stats.timesDied}")
            appendLine("Distance Traveled,${stats.distanceTraveled}")
            appendLine("Longest Wave,${stats.longestWave}")
            appendLine("Total Play Time (s),${nerdData.totalPlayTime}")
            appendLine("Wave Boss Rush Unlocked,${nerdData.waveBossRushUnlocked}")
            for ((ing, count) in stats.totalIngredientCount) {
                appendLine("Ingredient: $ing,$count")
            }
        }
        val file = Gdx.files.local("quantum_stats_${System.currentTimeMillis()}.csv")
        file.writeString(csv, false)
        Gdx.app.log("QuantumG", "📊 Stats exported to: ${file.path()}")
    }
    
    // ============================================================
    // 13. SECRET "OMNISCIENCE" REWARD (Ultimate Nerd)
    // ============================================================
    fun checkOmniscience(): Boolean {
        val conditions = listOf(
            getCodexProgress() == allCodexEntries.size,
            nerdData.completedPuzzles.size == puzzleLibrary.size,
            GameState.player.level >= 50,
            GameState.enemiesKilled >= 1000,
            nerdData.stats.bossKills >= 10,
            nerdData.stats.totalSpellsCast >= 100,
            nerdData.glyphs.all { it.collected }
        )
        val unlocked = conditions.all { it }
        if (unlocked) {
            Gdx.app.log("QuantumG", "🧠 THE QUANTUM OMNISCIENCE ACHIEVED!")
            Gdx.app.log("QuantumG", "Thank you for playing. You have seen everything.")
            Gdx.app.log("QuantumG", "Lore: The universe is a simulation. You are the observer.")
        }
        return unlocked
    }
    
    // ============================================================
    // 14. SAVE / LOAD (Persistence)
    // ============================================================
    fun save() {
        try {
            // Add initial codex entries if empty
            if (nerdData.codex.isEmpty()) {
                nerdData.codex = allCodexEntries.map { it.copy() }.toMutableList()
            }
            nerdData.lastSaveTimestamp = System.currentTimeMillis()
            val jsonString = json.encodeToString(nerdData)
            val file: FileHandle = Gdx.files.local(SAVE_FILE)
            file.writeString(jsonString, false)
        } catch (e: Exception) {
            Gdx.app.log("QuantumG", "❌ Failed to save NerdData: ${e.message}")
        }
    }
    
    fun load() {
        val file: FileHandle = Gdx.files.local(SAVE_FILE)
        if (!file.exists()) {
            // Initialize default
            nerdData = NerdSaveData()
            nerdData.codex = allCodexEntries.map { it.copy() }.toMutableList()
            save()
            return
        }
        try {
            val jsonString = file.readString()
            val loaded = json.decodeFromString<NerdSaveData>(jsonString)
            // Migrate missing fields if any
            if (loaded.codex.isEmpty()) {
                loaded.codex = allCodexEntries.map { it.copy() }.toMutableList()
            }
            nerdData = loaded
            Gdx.app.log("QuantumG", "📂 Nerd Data Loaded!")
        } catch (e: Exception) {
            Gdx.app.log("QuantumG", "❌ Corrupt Nerd Data! Resetting. Error: ${e.message}")
            nerdData = NerdSaveData()
            nerdData.codex = allCodexEntries.map { it.copy() }.toMutableList()
            save()
        }
    }
    
    // ============================================================
    // 15. UPDATE LOOP (Call this every frame)
    // ============================================================
    fun update(delta: Float, playerX: Float, playerY: Float, arenaW: Float, arenaH: Float) {
        nerdData.totalPlayTime += delta
        
        // Speedrun timer
        if (nerdData.gameMode == GameMode.SPEEDRUN && isSpeedrunActive) {
            speedrunTimer += delta
            if (nerdData.stats.longestWave < GameState.currentWave) {
                nerdData.stats.longestWave = GameState.currentWave
            }
        }
        
        // Glyph collection (passive check)
        checkGlyphCollection(playerX, playerY)
        
        // Cosmic events (check once per day, but we'll check it lazily)
        if (activeEvent == null) {
            checkCosmicEvents()
        }
    }
    
    // ============================================================
    // 16. RESET (For testing)
    // ============================================================
    fun reset() {
        val file = Gdx.files.local(SAVE_FILE)
        file.delete()
        nerdData = NerdSaveData()
        nerdData.codex = allCodexEntries.map { it.copy() }.toMutableList()
        cheatBuffer = ""
        activeEvent = null
        save()
        Gdx.app.log("QuantumG", "🗑️ Nerd Data Reset.")
    }
}