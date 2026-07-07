// QuantumG/talent/TalentTree.kt
package com.quantumg.talent

import com.quantumg.core.GameState
import com.quantumg.data.QuantumState

// Each node in the tree
data class TalentNode(
    val id: String,
    val name: String,
    val description: String,
    val iconEmoji: String, // e.g., "⚛️", "🌀", "🔥" for quick visual
    val maxLevel: Int = 3,
    val prerequisites: List<String> = emptyList(), // List of parent Talent IDs
    val effects: List<TalentEffect>
)

// What actually happens when you spend a point
sealed class TalentEffect {
    // Increases base damage of ALL spells by X%
    data class SpellDamageBoost(val percentIncrease: Float) : TalentEffect()
    
    // Adds a passive quantum aura to the player
    data class UnlockPassiveAura(val state: QuantumState) : TalentEffect()
    
    // Reduces ingredient cost for specific recipes
    data class AlchemyDiscount(val ingredientType: String, val discountAmount: Int) : TalentEffect()
    
    // Gives the player a new starting spell (no crafting required)
    data class UnlockStarterSpell(val spellId: String) : TalentEffect()
    
    // Makes the player move faster / dodge
    data class MovementSpeedBoost(val flatIncrease: Float) : TalentEffect()
}

// The manager that tracks spent points and applies bonuses
object TalentManager {
    
    // The full tree definition
    val talentTree = listOf(
        TalentNode(
            id = "quantum_fury",
            name = "Quantum Fury",
            description = "Your beams split into 2 additional targets.",
            iconEmoji = "⚡",
            maxLevel = 3,
            effects = listOf(TalentEffect.SpellDamageBoost(15f)) // 15% per level
        ),
        TalentNode(
            id = "alchemy_mastery",
            name = "Alchemy Mastery",
            description = "Crafting spells costs 1 less ingredient.",
            iconEmoji = "🧪",
            maxLevel = 2,
            prerequisites = listOf("quantum_fury"), // Must have at least 1 point in Fury first
            effects = listOf(TalentEffect.AlchemyDiscount("ALL", 1))
        ),
        TalentNode(
            id = "tunneling_ghost",
            name = "Tunneling Ghost",
            description = "You phase through enemies when dodging.",
            iconEmoji = "👻",
            maxLevel = 1,
            prerequisites = listOf("alchemy_mastery"),
            effects = listOf(TalentEffect.MovementSpeedBoost(1.5f))
        ),
        TalentNode(
            id = "entanglement_chain",
            name = "Entanglement Chain",
            description = "When you cast Entangle, it spreads to 2 nearby enemies.",
            iconEmoji = "🔗",
            maxLevel = 3,
            effects = listOf(TalentEffect.SpellDamageBoost(10f))
        )
    )

    // The player's current investment (mutable map stored in GameState later)
    private val investedPoints: MutableMap<String, Int> = mutableMapOf()

    // Spend a talent point (called from UI/level-up)
    fun spendPoint(talentId: String): Boolean {
        val node = talentTree.find { it.id == talentId } ?: return false
        val currentLevel = investedPoints.getOrDefault(talentId, 0)
        
        // Check max level
        if (currentLevel >= node.maxLevel) return false
        
        // Check prerequisites: all prerequisite talents must have at least 1 point
        for (preReqId in node.prerequisites) {
            if (investedPoints.getOrDefault(preReqId, 0) < 1) {
                return false // Locked!
            }
        }
        
        // Check if player has unspent points
        if (GameState.player.talentPoints <= 0) return false
        
        // Spend it!
        investedPoints[talentId] = currentLevel + 1
        GameState.player.talentPoints -= 1
        
        // Apply the effects immediately to the player
        applyEffects(node)
        
        // Save the game
        GameState.saveGame()
        return true
    }

    // Apply the actual in-game bonuses
    private fun applyEffects(node: TalentNode) {
        val level = investedPoints.getOrDefault(node.id, 0)
        for (effect in node.effects) {
            when (effect) {
                is TalentEffect.SpellDamageBoost -> {
                    // In your actual combat system, multiply spell damage by (1 + (level * effect.percentIncrease / 100))
                    println("Spell Damage increased by ${level * effect.percentIncrease}%")
                }
                is TalentEffect.MovementSpeedBoost -> {
                    GameState.player.movementSpeed += effect.flatIncrease
                }
                is TalentEffect.AlchemyDiscount -> {
                    // When crafting, subtract this discount from ingredient requirements
                    println("Alchemy discount active for ${effect.ingredientType}")
                }
                is TalentEffect.UnlockStarterSpell -> {
                    // Give the player a free spell if they don't have it
                    // We'll implement this in the SpellManager
                }
                is TalentEffect.UnlockPassiveAura -> {
                    // Enable the passive rendering in your render loop
                }
            }
        }
    }

    // Get the total bonus for a specific effect type (used in combat calculations)
    fun getTotalBonus(effectType: String): Float {
        // Iterate through invested talents and sum up the relevant bonuses
        // Simplified for now: just return the raw boost from Fury
        val furyLevel = investedPoints.getOrDefault("quantum_fury", 0)
        return furyLevel * 15f // 15% per level
    }

    // Reset all talents (useful for testing)
    fun resetAll() {
        investedPoints.clear()
        GameState.player.talentPoints += 10 // Refund roughly
    }

    // Helper: get invested level for a talent node
    fun getInvestedLevel(talentId: String): Int {
        return investedPoints.getOrDefault(talentId, 0)
    }

    // Helper: check if all prerequisites are met
    fun checkPrerequisites(talentId: String): Boolean {
        val node = talentTree.find { it.id == talentId } ?: return false
        for (preReq in node.prerequisites) {
            if (investedPoints.getOrDefault(preReq, 0) < 1) {
                return false
            }
        }
        return true
    }
}