// QuantumG/nerd/EasterEggs.kt (or append to QuantumNerdSystem)
package com.quantumg.nerd

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.MathUtils
import com.quantumg.core.GameState
import com.quantumg.data.QuantumState

object EasterEggs {
    // ============================================================
    // 1. SECRET DIALOGUE LINES (Pop up on screen)
    // ============================================================
    private val dialoguePool = mapOf(
        "zelda" to listOf(
            "It's dangerous to go alone! Take this.",
            "Listen!",
            "The flow of time is always cruel...",
            "You've met with a terrible fate, haven't you?"
        ),
        "megaman" to listOf(
            "Mega Buster!",
            "Jump and shoot!",
            "I am the super fighting robot!"
        ),
        "wow" to listOf(
            "For the Horde!",
            "For the Alliance!",
            "You are not prepared!",
            "Thrall's balls!"
        ),
        "starwars" to listOf(
            "I am your father.",
            "Use the force, Luke.",
            "These are not the droids you're looking for.",
            "May the force be with you."
        ),
        "matrix" to listOf(
            "There is no spoon.",
            "Welcome to the desert of the real.",
            "Whoa.",
            "I know kung fu."
        ),
        "rickandmorty" to listOf(
            "Wubba lubba dub dub!",
            "I'm pickle Rick!",
            "Show me what you got!",
            "Get schwifty!"
        ),
        "futurama" to listOf(
            "Good news, everyone!",
            "Bite my shiny metal ass!",
            "I'm going to build my own theme park!",
            "Shut up and take my money!"
        ),
        "quantum" to listOf(
            "Schrödinger's cat is both alive and dead.",
            "The observer changes the observed.",
            "Entanglement is weird.",
            "Quantum tunneling: the art of being somewhere else."
        ),
        "math" to listOf(
            "Riemann hypothesis: still unsolved.",
            "Fermat's last theorem: solved.",
            "The answer is 42.",
            "Pi is exactly 3.14... wait, no it isn't."
        )
    )

    private var lastDialogueTime = 0f
    private var dialogueCooldown = 5f // seconds

    fun triggerRandomDialogue(triggerCategory: String? = null) {
        val now = System.currentTimeMillis() / 1000f
        if (now - lastDialogueTime < dialogueCooldown) return

        val pool = if (triggerCategory != null && dialoguePool.containsKey(triggerCategory)) {
            dialoguePool[triggerCategory]!!
        } else {
            dialoguePool.values.flatten()
        }
        val line = pool.random()
        // Display on screen via a callback (you'll need to set this)
        onDialogueTrigger?.invoke(line)
        lastDialogueTime = now
    }

    var onDialogueTrigger: ((String) -> Unit)? = null

    // ============================================================
    // 2. HIDDEN INTERACTIONS (UI click easter eggs)
    // ============================================================
    fun handleUIInteraction(elementId: String) {
        when (elementId) {
            "talent_tree_button" -> {
                if (GameState.player.talentPoints == 0) {
                    triggerRandomDialogue("zelda")
                }
            }
            "alchemy_crucible_button" -> {
                if (GameState.player.unlockedSpells.size < 3) {
                    triggerRandomDialogue("wow") // For the Horde! (new player)
                }
            }
            "achievement_button" -> {
                if (GameState.enemiesKilled > 100) {
                    triggerRandomDialogue("matrix")
                }
            }
            "settings_button" -> {
                triggerRandomDialogue("futurama")
            }
            "wave_counter" -> {
                if (GameState.currentWave % 10 == 0) {
                    triggerRandomDialogue("starwars")
                }
            }
        }
    }

    // ============================================================
    // 3. BOSS INTRO QUOTES (Nerdy one-liners)
    // ============================================================
    val bossIntroQuotes = listOf(
        "I am the one who knocks!" to "Breaking Bad",
        "You shall not pass!" to "Lord of the Rings",
        "I'll be back." to "Terminator",
        "I see dead people." to "The Sixth Sense",
        "Say hello to my little friend!" to "Scarface",
        "It's over 9000!" to "Dragon Ball Z",
        "I am your father." to "Star Wars",
        "The cake is a lie." to "Portal",
        "I'm the king of the world!" to "Titanic",
        "Winter is coming." to "Game of Thrones",
        "To infinity and beyond!" to "Toy Story",
        "I'm the Juggernaut, bitch!" to "X-Men",
        "Hasta la vista, baby." to "Terminator 2"
    )

    fun getBossIntroQuote(): String {
        val (quote, source) = bossIntroQuotes.random()
        return "$quote  (― $source)"
    }

    // ============================================================
    // 4. SECRET SPELL SEQUENCES (Cast spells in order to trigger)
    // ============================================================
    private val secretSequences = mapOf(
        listOf("LINE", "CIRCLE", "LINE") to {
            triggerRandomDialogue("megaman")
            // Spawn a Mega Man-style projectile
            Gdx.app.log("EasterEgg", "Mega Buster sequence triggered!")
        },
        listOf("CIRCLE", "CIRCLE", "LINE", "LINE") to {
            triggerRandomDialogue("zelda")
            // Unlock a hidden "Triforce" buff
            Gdx.app.log("EasterEgg", "Triforce unlocked!")
        },
        listOf("LINE_VERTICAL", "LINE_HORIZONTAL", "CIRCLE") to {
            triggerRandomDialogue("matrix")
            // Slomo mode for 5 seconds
            Gdx.app.log("EasterEgg", "Bullet time!")
        }
    )

    private val gestureHistory = mutableListOf<String>()

    fun trackGesture(gesture: String) {
        gestureHistory.add(gesture)
        if (gestureHistory.size > 10) gestureHistory.removeAt(0)

        // Check if any sequence matches the tail of history
        for ((sequence, action) in secretSequences) {
            if (gestureHistory.takeLast(sequence.size) == sequence) {
                action()
                gestureHistory.clear()
                break
            }
        }
    }

    // ============================================================
    // 5. HIDDEN ACHIEVEMENTS (More nerdy references)
    // ============================================================
    // These are added to the AchievementManager's allAchievements list dynamically
    val hiddenAchievements = listOf(
        Achievement(
            id = "nerd_pi_day",
            title = "π Day",
            description = "Play the game on March 14th.",
            icon = "🥧",
            color = Color(1f, 0.8f, 0.2f, 1f),
            maxProgress = 1,
            rewardDescription = "All damage multiplied by π forever!"
        ),
        Achievement(
            id = "nerd_cake_lie",
            title = "The Cake is a Lie",
            description = "Try to collect an ingredient that doesn't exist.",
            icon = "🎂",
            color = Color(1f, 0.5f, 0.5f, 1f),
            maxProgress = 1,
            rewardDescription = "Unlocks a portal gun cosmetic."
        ),
        Achievement(
            id = "nerd_9000",
            title = "Over 9000!",
            description = "Deal 9000 total damage.",
            icon = "💥",
            color = Color(1f, 1f, 0.2f, 1f),
            maxProgress = 9000,
            rewardDescription = "All spells get a 10% critical hit chance."
        ),
        Achievement(
            id = "nerd_bullet_time",
            title = "Bullet Time",
            description = "Trigger the Matrix secret sequence.",
            icon = "🕶️",
            color = Color(0.2f, 1f, 0.2f, 1f),
            maxProgress = 1,
            rewardDescription = "Unlock slow-motion toggle."
        ),
        Achievement(
            id = "nerd_kung_fu",
            title = "I Know Kung Fu",
            description = "Kill an enemy with a Tunneling projectile while moving.",
            icon = "🥋",
            color = Color(1f, 0.5f, 0f, 1f),
            maxProgress = 1,
            rewardDescription = "Movement speed permanently +10%."
        ),
        Achievement(
            id = "nerd_winter",
            title = "Winter is Coming",
            description = "Play during winter (December-February).",
            icon = "❄️",
            color = Color(0.5f, 0.8f, 1f, 1f),
            maxProgress = 1,
            rewardDescription = "Enemies slow down by 20%."
        ),
        Achievement(
            id = "nerd_fourtytwo",
            title = "The Answer",
            description = "Reach level 42.",
            icon = "🔢",
            color = Color(0.5f, 0.2f, 0.8f, 1f),
            maxProgress = 1,
            rewardDescription = "42% damage bonus on all spells."
        ),
        Achievement(
            id = "nerd_banjo",
            title = "Banjo-Kazooie",
            description = "Collect 100 ingredients.",
            icon = "🎸",
            color = Color(1f, 0.6f, 0f, 1f),
            maxProgress = 100,
            rewardDescription = "Unlock the 'Jiggy' skin."
        )
    )

    // ============================================================
    // 6. GESTURE-BASED SECRETS (Draw specific shapes)
    // ============================================================
    private val secretGestures = mapOf(
        "STAR" to { triggerRandomDialogue("starwars") },
        "HEART" to { triggerRandomDialogue("zelda") },
        "TRIANGLE" to { triggerRandomDialogue("wow") },
        "INFINITY" to { triggerRandomDialogue("quantum") }
    )

    fun checkSecretGesture(gestureName: String) {
        if (secretGestures.containsKey(gestureName)) {
            secretGestures[gestureName]?.invoke()
            // Also unlock a hidden achievement
            AchievementManager.progressAchievement("nerd_kung_fu") // example
        }
    }

    // ============================================================
    // 7. RANDOM HIDDEN EVENTS (After certain number of kills)
    // ============================================================
    fun checkHiddenEvents(killCount: Int) {
        when (killCount) {
            7 -> triggerRandomDialogue("matrix") // "The number 7 is lucky"
            42 -> triggerRandomDialogue("math") // "The answer"
            69 -> triggerRandomDialogue("rickandmorty") // "Nice"
            100 -> triggerRandomDialogue("futurama") // "Good news!"
            666 -> triggerRandomDialogue("starwars") // "The dark side"
            1337 -> triggerRandomDialogue("megaman") // "LEET!"
        }
    }

    // ============================================================
    // 8. HIDDEN COLOR PALETTE (Copic inspired secret)
    // ============================================================
    val secretPalettes = mapOf(
        "zelda_gold" to listOf(Color(0.8f, 0.6f, 0.0f, 1f), Color(0.0f, 0.2f, 0.4f, 1f)),
        "megaman_blue" to listOf(Color(0.0f, 0.4f, 0.8f, 1f), Color(0.8f, 0.2f, 0.0f, 1f)),
        "banjo_orange" to listOf(Color(1f, 0.5f, 0.0f, 1f), Color(0.0f, 0.5f, 0.0f, 1f)),
        "portal_orange" to listOf(Color(1f, 0.5f, 0.0f, 1f), Color(0.2f, 0.6f, 1f, 1f))
    )

    fun unlockSecretPalette(name: String) {
        // Hook into ThemeManager to add new palette
        Gdx.app.log("EasterEgg", "🎨 Secret palette unlocked: $name")
        // You would add logic to add this palette to ThemeManager's list
    }

    // ============================================================
    // 9. SOUND EASTER EGGS (Play special audio on hidden events)
    // ============================================================
    fun playSecretSound(id: String) {
        when (id) {
            "wilhelm" -> QuantumSoundManager.generateBoltSound().play(0.5f) // Placeholder
            "mario" -> QuantumSoundManager.generateLevelUpSound().play(0.8f) // Reuse
            "zelda" -> QuantumSoundManager.playQuantumBubble() // Reuse
        }
    }

    // ============================================================
    // 10. HIDDEN MODE: "QUANTUM FLUX" (Toggle via cheat code)
    // ============================================================
    var isFluxCapacitorActive = false

    fun toggleFluxCapacitor() {
        isFluxCapacitorActive = !isFluxCapacitorActive
        if (isFluxCapacitorActive) {
            Gdx.app.log("EasterEgg", "⚡ Flux Capacitor active! 1.21 Gigawatts!")
            // Reverse wave order or randomize enemy speeds
        } else {
            Gdx.app.log("EasterEgg", "Flux Capacitor deactivated.")
        }
    }

    // ============================================================
    // 11. BOSS DIALOGUE (Based on player's progress)
    // ============================================================
    fun getBossIntroMessage(bossLevel: Int): String {
        return when (bossLevel) {
            1 -> "You are not prepared!"
            2 -> "I've been expecting you."
            3 -> "I am the alpha and the omega."
            4 -> "Your journey ends here."
            5 -> "The universe is a simulation. I am the admin."
            else -> "I am the Quantum Aberration. Submit."
        }
    }

    // ============================================================
    // 12. HIDDEN TALENT (Only appears after secret achievement)
    // ============================================================
    fun unlockSecretTalent() {
        if (AchievementManager.isUnlocked("nerd_fourtytwo")) {
            // Add a special talent node that multiplies damage by 42%
            Gdx.app.log("EasterEgg", "🔓 Secret talent: 'Answer to Everything' unlocked!")
        }
    }
}