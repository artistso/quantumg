// QuantumG/data/QuantumTypes.kt
package com.quantumg.data

import com.badlogic.gdx.math.Vector2

// The 4 fundamental quantum states for our alchemy
enum class QuantumState {
    SUPERPOSITION,  // Does 2 things at once (splits damage)
    ENTANGLED,      // Links two enemies (share health/damage)
    COLLAPSED,      // High single-target damage, deterministic
    TUNNELING       // Passes through obstacles (ignore walls)
}

// Base ingredient dropped by enemies
enum class Ingredient {
    QUARK_UP, QUARK_DOWN, GLUON, PHOTON, LEPTON, BOSON
}

// A spell/ability the player casts via gesture
data class Spell(
    val id: String,
    val name: String,           // e.g., "Schrodinger's Strike"
    val gesturePattern: String, // e.g., "ZIGZAG" or "CIRCLE"
    val effectType: QuantumState,
    val baseDamage: Float,
    val cooldownSeconds: Float,
    val manaCost: Int,
    val alchemyRecipe: List<Ingredient> // How to craft/upgrade it
)

// A Rune Tower (turret) - but we'll call them "Quantum Anchors"
data class QuantumAnchor(
    val id: String,
    val position: Vector2,
    var state: QuantumState = QuantumState.SUPERPOSITION,
    val attackRange: Float,
    val damagePerHit: Float,
    val fireRate: Float,
    // Anchors can be upgraded using alchemy
    var level: Int = 1
)

// The player's RPG stats
data class PlayerCharacter(
    val name: String = "Quantum Archivist",
    var level: Int = 1,
    var xp: Int = 0,
    var maxHp: Int = 100,
    var currentHp: Int = 100,
    var maxMana: Int = 50,
    var currentMana: Int = 50,
    var movementSpeed: Float = 4.0f,
    // Permanent alchemy inventory (crafted spells and upgrades)
    val unlockedSpells: MutableList<Spell> = mutableListOf(),
    val inventoryIngredients: MutableMap<Ingredient, Int> = mutableMapOf(),
    // Talent tree points (we tie this to level-ups)
    var talentPoints: Int = 0
)

// Enemy data with quantum flavor
data class QuantumEnemy(
    val id: String,
    val position: Vector2,
    val state: QuantumState,
    var hp: Float,
    val maxHp: Float,
    val speed: Float,
    val rewardXp: Int,
    // When killed, drops random ingredients
    fun generateLoot(): List<Ingredient> {
        return listOf(Ingredient.values().random())
    }
)