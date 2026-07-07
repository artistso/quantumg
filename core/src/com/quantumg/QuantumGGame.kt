package com.quantumg

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import com.quantumg.QuantumGGame
import com.quantumg.combat.CombatManager
import com.quantumg.combat.QuantumProjectileSystem
import com.quantumg.effects.JuiceManager
import com.quantumg.effects.ParticleBurst
import com.quantumg.entities.BossEnemy
import com.quantumg.entities.QuantumEnemy
import com.quantumg.entities.TopologicalBoss
import com.quantumg.input.GestureSpellCaster
import com.quantumg.input.QuantumTouchController
import com.quantumg.renderer.QuantumRenderer
import com.quantumg.spawner.WaveSpawner
import com.quantumg.talent.TalentManager
import com.quantumg.ui.AlchemyCrucibleUI
import com.quantumg.ui.HudManager
import com.quantumg.ui.TalentTreeUI
import com.quantumg.ui.ThemeSelectionUI
import com.quantumg.visualizer.QuantumFieldVisualizer

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2

/**
 * QuantumG — A quantum-themed LibGDX game.
 *
 * This is the main game class that ties together all systems:
 * combat, spawning, alchemy, talents, achievements, themes,
 * audio, nerd/easter-eggs, and visual effects.
 *
 * Assembled from DeepSeek-generated code snippets.
 * Some integration points may need manual review.
 */
class QuantumGGame : ApplicationAdapter() {

    // === Core Systems ===
    private lateinit var shapeRenderer: ShapeRenderer
    private lateinit var quantumRenderer: QuantumRenderer

    // === Player ===
    private val playerPos = Vector2()
    private var cameraOffset = Vector2()

    // === Combat & Spawning ===
    private lateinit var projectileSystem: QuantumProjectileSystem
    private lateinit var combatManager: CombatManager
    private lateinit var waveSpawner: WaveSpawner
    private var bossSpawnedThisWave = false

    // === Input ===
    private lateinit var touchController: QuantumTouchController
    private lateinit var gestureCaster: GestureSpellCaster

    // === UI Overlays ===
    private lateinit var hud: HudManager
    private var talentTreeUI: TalentTreeUI? = null
    private var alchemyUI: AlchemyCrucibleUI? = null
    private var themeUI: ThemeSelectionUI? = null
    private var achievementBanner: AchievementBanner? = null

    // === Effects ===
    private lateinit var particleEffects: ParticleBurst

    // === Visualizer ===
    private lateinit var fieldVisualizer: QuantumFieldVisualizer
    private var morphingBoss: TopologicalBoss? = null

    // === Arena ===
    private var arenaWidth = 0f
    private var arenaHeight = 0f

    // === Delta Time ===
    private val targetDelta = 1f / 60f

    override fun create() {
        shapeRenderer = ShapeRenderer()
        quantumRenderer = QuantumRenderer()

        arenaWidth = Gdx.graphics.width.toFloat()
        arenaHeight = Gdx.graphics.height.toFloat()

        playerPos.set(arenaWidth / 2f, arenaHeight / 3f)

        // --- Particle Effects ---
        particleEffects = ParticleBurst()

        // --- Projectile System ---
        projectileSystem = QuantumProjectileSystem()

        // --- Wave Spawner ---
        waveSpawner = WaveSpawner(
            onWaveComplete = {
                if (GameState.currentWave % 5 == 0) {
                    bossSpawnedThisWave = true
                }
            }
        )

        // --- Combat Manager ---
        combatManager = CombatManager(
            projectileSystem = projectileSystem,
            waveSpawner = waveSpawner,
            onLootDrop = { position, ingredients ->
                particleEffects.spawnExplosion(position, Color.GOLD, 30, 200f)
                quantumRenderer.spawnEntity(position, ThemeManager.getCurrentTheme().particleColor, MathUtils.random(5f, 15f))
            },
            onBossSpawn = { position ->
                particleEffects.spawnExplosion(position, Color.PURPLE, 100, 500f)
                JuiceManager.shake(25f, 0.6f)
            }
        )

        // --- Touch Controller ---
        touchController = QuantumTouchController(
            playerPos = playerPos,
            moveSpeed = GameState.player.movementSpeed * 60f,
            onMove = { direction -> /* playerPos updated in loop */ }
        )

        // --- Gesture Spell Caster ---
        gestureCaster = GestureSpellCaster { spell, targetPos ->
            projectileSystem.fireSpell(spell, playerPos, targetPos)
        }

        // --- Field Visualizer ---
        fieldVisualizer = QuantumFieldVisualizer(shapeRenderer)

        // --- HUD ---
        hud = HudManager(
            onTalentTreeOpen = {
                if (talentTreeUI == null) {
                    talentTreeUI = TalentTreeUI(
                        onClose = {
                            talentTreeUI?.dispose()
                            talentTreeUI = null
                            Gdx.input.inputProcessor = hud.stage
                        }
                    )
                    Gdx.input.inputProcessor = talentTreeUI!!.stage
                }
            },
            onAlchemyOpen = {
                if (alchemyUI == null) {
                    alchemyUI = AlchemyCrucibleUI(
                        onClose = {
                            alchemyUI?.dispose()
                            alchemyUI = null
                            Gdx.input.inputProcessor = hud.stage
                        },
                        onSpellUnlocked = { spellId ->
                            AchievementManager.progressAchievement("dangerous_to_go_alone", 1)
                            QuantumSoundManager.playAchievement()
                        }
                    )
                    Gdx.input.inputProcessor = alchemyUI!!.stage
                }
            },
            onThemeOpen = {
                if (themeUI == null) {
                    themeUI = ThemeSelectionUI(
                        onClose = {
                            themeUI?.dispose()
                            themeUI = null
                            Gdx.input.inputProcessor = hud.stage
                        },
                        onThemeApplied = {
                            hud.rebuildUI()
                            talentTreeUI?.rebuildTree()
                        }
                    )
                    Gdx.input.inputProcessor = themeUI!!.stage
                }
            },
            onAchievementsOpen = {
                // Gallery shown via AchievementManager
            }
        )

        // --- Achievement Banner ---
        achievementBanner = AchievementBanner()

        // --- Nerd System ---
        QuantumNerdSystem.load()
        QuantumNerdSystem.generateGlyphs(arenaWidth, arenaHeight)
        val cosmicEvent = QuantumNerdSystem.checkCosmicEvents()
        if (cosmicEvent != null) {
            Gdx.app.log("QuantumG", "Cosmic Event: ${cosmicEvent.name}")
        }

        // --- Load Mods ---
        val modSpells = QuantumNerdSystem.loadMods()
        for (mod in modSpells) {
            val newSpell = Spell(
                id = mod.id,
                name = mod.name,
                gesturePattern = "LINE",
                effectType = QuantumState.valueOf(mod.state),
                baseDamage = mod.damage,
                cooldownSeconds = 1.0f,
                manaCost = 10,
                alchemyRecipe = emptyList()
            )
            GameState.player.unlockedSpells.add(newSpell)
        }

        // --- Easter Egg Callbacks ---
        EasterEggs.onDialogueTrigger = { line ->
            Gdx.app.log("EasterEgg", line)
        }
        QuantumNerdSystem.onCodexUnlock = { title ->
            Gdx.app.log("QuantumG", "Codex Unlocked: $title")
        }
        QuantumNerdSystem.onPuzzleComplete = { reward ->
            when {
                reward.contains("Mana") -> GameState.player.maxMana += 5
                reward.contains("HP") -> GameState.player.maxHp += 10
            }
            GameState.saveGame()
        }

        // --- Audio ---
        QuantumSoundManager.startAmbientMusic()

        // --- Input Multiplexer ---
        Gdx.input.inputProcessor = hud.stage

        // --- Start Game ---
        waveSpawner.startWave()
    }

    override fun render() {
        var delta = Math.min(Gdx.graphics.deltaTime, targetDelta * 2)

        // Hit-Stop
        delta = JuiceManager.applyDelta(delta)

        if (delta > 0) {
            // --- Camera shake ---
            cameraOffset = JuiceManager.updateShake(delta)

            // --- Touch input ---
            touchController.update(delta)

            // --- Particles ---
            particleEffects.update(delta)

            // --- Boss spawn check ---
            if (bossSpawnedThisWave && waveSpawner.isWaveActive() && !waveSpawner.hasBossSpawned()) {
                val boss = BossEnemy(
                    id = "boss_${System.currentTimeMillis()}",
                    position = Vector2(
                        MathUtils.random(100f, arenaWidth - 100f),
                        arenaHeight + 50f
                    ),
                    hp = 200f + (GameState.player.level * 20f),
                    speed = 40f + (GameState.player.level * 3f),
                    rewardXp = 100 + (GameState.player.level * 20)
                )
                combatManager.addEnemy(boss)
                bossSpawnedThisWave = false
                waveSpawner.markBossSpawned()
                particleEffects.spawnExplosion(boss.position, Color.PURPLE, 80, 400f)
            }

            // --- Combat ---
            combatManager.update(delta, playerPos)
            projectileSystem.update(delta)
            combatManager.checkCollisions()

            // --- Nerd System ---
            QuantumNerdSystem.update(delta, playerPos.x, playerPos.y, arenaWidth, arenaHeight)
            EasterEggs.checkHiddenEvents(GameState.enemiesKilled)
        }

        // === RENDER ===
        renderArena()

        // Enemies
        renderEnemies()

        // Projectiles
        projectileSystem.render()

        // Particles
        particleEffects.render(shapeRenderer)

        // Touch visualizer
        touchController.render(shapeRenderer)

        // Field Visualizer (NURBS, graphs, QKD)
        morphingBoss?.let { boss ->
            boss.render(shapeRenderer)
            val controlPoints = boss.shapeVertices.map { Pair(it.x.toDouble(), it.y.toDouble()) }
            fieldVisualizer.renderCurve(controlPoints, boss.glowColor, 3f)
        }

        // === UI ===
        hud.update()
        hud.stage.act(Gdx.graphics.deltaTime)
        hud.stage.draw()

        talentTreeUI?.let {
            it.stage.act(Gdx.graphics.deltaTime)
            it.stage.draw()
            if (it.shouldClose()) {
                it.dispose()
                talentTreeUI = null
                Gdx.input.inputProcessor = hud.stage
            }
        }

        alchemyUI?.let {
            it.stage.act(Gdx.graphics.deltaTime)
            it.stage.draw()
            if (it.shouldClose()) {
                it.dispose()
                alchemyUI = null
                Gdx.input.inputProcessor = hud.stage
            }
        }

        themeUI?.let {
            it.stage.act(Gdx.graphics.deltaTime)
            it.stage.draw()
            if (it.shouldClose()) {
                it.dispose()
                themeUI = null
                Gdx.input.inputProcessor = hud.stage
            }
        }

        // Achievement banner
        achievementBanner?.render(Gdx.graphics.deltaTime)
    }

    private fun renderArena() {
        shapeRenderer.projectionMatrix = /* camera matrix -- set by your camera system */
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        shapeRenderer.color = Color(0.2f, 0.5f, 0.8f, 0.1f)
        for (i in 0 until 20) {
            val x = i * 100f
            shapeRenderer.line(x, 0f, x, arenaHeight)
        }
        for (i in 0 until 15) {
            val y = i * 100f
            shapeRenderer.line(0f, y, arenaWidth, y)
        }
        shapeRenderer.end()
    }

    private fun renderEnemies() {
        // Enemies are rendered by CombatManager or individually
        // This is a placeholder for custom enemy rendering
    }

    override fun resize(width: Int, height: Int) {
        arenaWidth = width.toFloat()
        arenaHeight = height.toFloat()
        hud.resize(width, height)
        talentTreeUI?.resize(width, height)
    }

    override fun dispose() {
        shapeRenderer.dispose()
        quantumRenderer.dispose()
        projectileSystem.dispose()
        hud.dispose()
        talentTreeUI?.dispose()
        alchemyUI?.dispose()
        themeUI?.dispose()
        particleEffects.clear()
        touchController.reset()
        QuantumNerdSystem.save()
    }
}
