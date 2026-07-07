// QuantumG/data/PlayerCharacter.kt
package com.quantumg.data

import kotlinx.serialization.Serializable

@Serializable
data class PlayerCharacter(
    var level: Int = 1,
    var xp: Int = 0,
    var currentHp: Int = 100,
    var maxHp: Int = 100,
    var currentMana: Int = 50,
    var maxMana: Int = 50,
    var movementSpeed: Float = 4f,
    var talentPoints: Int = 0,
    var unlockedSpells: MutableList<Spell> = mutableListOf(),
    var inventoryIngredients: MutableMap<Ingredient, Int> = mutableMapOf(),
)

@Serializable
data class QuantumAnchor(
    val id: String,
    val positionX: Float,
    val positionY: Float,
    var activated: Boolean = false,
    val linkedAnchors: List<String> = emptyList(),
)