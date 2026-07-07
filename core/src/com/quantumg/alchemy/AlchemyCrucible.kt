// QuantumG/alchemy/AlchemyCrucible.kt
package com.quantumg.alchemy

import com.quantumg.data.Ingredient
import com.quantumg.data.Spell

class AlchemyCrucible {
    
    // Recipe book: Combine ingredients to get a new Spell or passive buff
    private val recipes = mapOf(
        // Quantum Beam: 2 Up Quarks + 1 Gluon
        listOf(Ingredient.QUARK_UP, Ingredient.QUARK_UP, Ingredient.GLUON) to 
            Spell(
                id = "spell_beam",
                name = "Gluon Annihilator",
                gesturePattern = "LINE",
                effectType = QuantumState.COLLAPSED,
                baseDamage = 35f,
                cooldownSeconds = 1.5f,
                manaCost = 15,
                alchemyRecipe = listOf(Ingredient.QUARK_UP, Ingredient.QUARK_UP, Ingredient.GLUON)
            ),
        
        // Entanglement Trap: 2 Leptons + 1 Boson
        listOf(Ingredient.LEPTON, Ingredient.LEPTON, Ingredient.BOSON) to 
            Spell(
                id = "spell_entangle",
                name = "Quantum Entanglement Web",
                gesturePattern = "CIRCLE",
                effectType = QuantumState.ENTANGLED,
                baseDamage = 0f, // Does no damage, but links enemies!
                cooldownSeconds = 8f,
                manaCost = 25,
                alchemyRecipe = listOf(Ingredient.LEPTON, Ingredient.LEPTON, Ingredient.BOSON)
            )
    )

    fun combine(ingredients: List<Ingredient>): Spell? {
        // Sort ingredients so order doesn't matter for the recipe match
        val sorted = ingredients.sorted()
        return recipes[sorted]
    }

    // Upgrade an existing Anchor using pure Photons (light/matter alchemy)
    fun upgradeAnchor(anchor: QuantumAnchor, photons: Int): Boolean {
        if (photons < 3) return false
        anchor.level += 1
        anchor.damagePerHit *= 1.15f // 15% scaling
        anchor.attackRange += 5f
        return true
    }
}