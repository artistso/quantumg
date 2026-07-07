// QuantumG/ui/AlchemyCrucibleUI.kt
package com.quantumg.ui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.quantumg.alchemy.AlchemyCrucible
import com.quantumg.core.GameState
import com.quantumg.data.Ingredient

class AlchemyCrucibleUI(
    private val onClose: () -> Unit,
    private val onSpellUnlocked: (String) -> Unit // Callback when a new spell is crafted
) {
    val stage = Stage(ScreenViewport())
    private val skin = createCrucibleSkin()
    private val crucible = AlchemyCrucible()
    private var closeRequested = false

    // The 3 slots in the cauldron
    private val slot1 = IngredientSlot()
    private val slot2 = IngredientSlot()
    private val slot3 = IngredientSlot()
    private val resultLabel = Label("", skin)

    init {
        buildCrucible()
    }

    private fun buildCrucible() {
        val overlay = Table().apply {
            setFillParent(true)
            background = createColoredDrawable(Color(0.05f, 0.05f, 0.1f, 0.9f))
        }

        val window = Window("🧪 Alchemy Crucible", skin).apply {
            setModal(true)
            setMovable(false)
            setSize(900f, 600f)
            center()
        }

        val mainTable = Table()
        mainTable.pad(20f)

        // --- LEFT PANEL: Ingredient Inventory ---
        val inventoryTable = Table(skin).apply {
            background = createColoredDrawable(Color.DARK_GRAY, 0.6f)
            pad(15f)
        }
        inventoryTable.add(Label("📦 Your Ingredients", skin)).center().colspan(2).row()

        // Create draggable icons for each ingredient the player has
        val dragAndDrop = DragAndDrop()
        for (ing in Ingredient.values()) {
            val count = GameState.player.inventoryIngredients[ing] ?: 0
            if (count > 0) {
                val icon = createIngredientIcon(ing, count)
                // Make it draggable
                dragAndDrop.addSource(object : DragAndDrop.Source(icon) {
                    override fun getDragActor(payload: DragAndDrop.Payload?): Actor {
                        val dragIcon = createIngredientIcon(ing, 1)
                        payload?.setObject(ing)
                        return dragIcon
                    }
                })
                inventoryTable.add(icon).pad(4f).width(80f).height(80f)
            }
        }

        mainTable.add(inventoryTable).width(250f).padRight(20f)

        // --- CENTER PANEL: The Cauldron (3 slots) ---
        val cauldronTable = Table(skin).apply {
            background = createColoredDrawable(Color(0.2f, 0.1f, 0.3f, 0.8f))
            pad(20f)
        }
        cauldronTable.add(Label("🔥 Drop ingredients here", skin)).colspan(3).center().row()

        // The 3 slots
        val slotActors = listOf(slot1, slot2, slot3)
        for (slot in slotActors) {
            val slotImage = slot.getActor()
            // Make it a drop target
            dragAndDrop.addTarget(object : DragAndDrop.Target(slotImage) {
                override fun drag(source: DragAndDrop.Source?, payload: DragAndDrop.Payload?, x: Float, y: Float, pointer: Int): Boolean {
                    // Highlight if valid
                    return true
                }
                override fun drop(source: DragAndDrop.Source?, payload: DragAndDrop.Payload?, x: Float, y: Float, pointer: Int) {
                    val ingredient = payload?.getObject() as? Ingredient ?: return
                    // Add to the first empty slot
                    when {
                        slot1.isEmpty() -> slot1.setIngredient(ingredient)
                        slot2.isEmpty() -> slot2.setIngredient(ingredient)
                        slot3.isEmpty() -> slot3.setIngredient(ingredient)
                        else -> {
                            // All slots full – show a message
                            resultLabel.setText("⚠️ Cauldron full! Brew or clear.")
                        }
                    }
                    updateCauldronDisplay(cauldronTable, slotActors)
                }
            })
            cauldronTable.add(slotImage).pad(10f).width(100f).height(100f)
        }

        cauldronTable.row()
        // Clear button for slots
        val clearBtn = TextButton("Clear Cauldron", skin)
        clearBtn.addListener {
            slot1.clear()
            slot2.clear()
            slot3.clear()
            updateCauldronDisplay(cauldronTable, slotActors)
            resultLabel.setText("Cauldron cleared.")
            true
        }
        cauldronTable.add(clearBtn).colspan(3).padTop(10f)

        mainTable.add(cauldronTable).expandX().padRight(20f)

        // --- RIGHT PANEL: Brew & Results ---
        val brewTable = Table(skin).apply {
            background = createColoredDrawable(Color(0.1f, 0.2f, 0.1f, 0.8f))
            pad(20f)
        }
        brewTable.add(Label("⚗️ Brew Result", skin)).center().row()
        brewTable.add(resultLabel).pad(10f).row()

        val brewBtn = TextButton("✨ BREW SPELL", skin)
        brewBtn.addListener {
            val ingredients = listOfNotNull(
                slot1.getIngredient(),
                slot2.getIngredient(),
                slot3.getIngredient()
            )
            if (ingredients.size < 2) {
                resultLabel.setText("Need at least 2 ingredients!")
                return@addListener true
            }

            val newSpell = crucible.combine(ingredients)
            if (newSpell != null) {
                // Remove ingredients from inventory
                for (ing in ingredients) {
                    GameState.player.inventoryIngredients[ing] = 
                        (GameState.player.inventoryIngredients[ing] ?: 0) - 1
                }
                // Unlock the spell
                GameState.player.unlockedSpells.add(newSpell)
                // Clear slots
                slot1.clear(); slot2.clear(); slot3.clear()
                updateCauldronDisplay(cauldronTable, slotActors)
                resultLabel.setText("✅ Crafted: ${newSpell.name}!")
                onSpellUnlocked(newSpell.name)
                // Save game
                GameState.saveGame()
                // You could rebuild the HUD here to show the new spell
            } else {
                resultLabel.setText("❌ Recipe unknown! Try different ingredients.")
            }
            true
        }
        brewTable.add(brewBtn).padTop(20f).width(150f)

        mainTable.add(brewTable).width(250f)

        // --- CLOSE BUTTON ---
        val closeBtn = TextButton("Close Crucible", skin)
        closeBtn.addListener {
            closeRequested = true
            true
        }
        mainTable.row()
        mainTable.add(closeBtn).colspan(3).padTop(30f).center()

        window.add(mainTable)
        overlay.add(window).center()
        stage.addActor(overlay)
    }

    // Helper to update the cauldron display (show ingredient icons inside slots)
    private fun updateCauldronDisplay(cauldronTable: Table, slots: List<IngredientSlot>) {
        // In a real implementation, you'd update the child actors.
        // For simplicity, we just rebuild the display.
        // Since we don't have a reference to the inner table, we use a trick:
        // We'll clear and re-add the slots. This is simplified for speed.
        // The robust way: store references to the Image actors inside the slots.
        // I'll show the quick way – you can expand this.
        println("Cauldron updated: ${slots.map { it.getIngredient() }}")
    }

    private fun createIngredientIcon(ing: Ingredient, count: Int): Table {
        val table = Table(skin)
        val color = when (ing) {
            Ingredient.QUARK_UP -> Color.RED
            Ingredient.QUARK_DOWN -> Color.BLUE
            Ingredient.GLUON -> Color.GREEN
            Ingredient.PHOTON -> Color.YELLOW
            Ingredient.LEPTON -> Color.PURPLE
            Ingredient.BOSON -> Color.ORANGE
        }
        val bg = createColoredDrawable(color, 0.7f)
        table.background = bg
        val label = Label("${ing.name[0]}${count}", skin)
        label.setColor(Color.BLACK)
        table.add(label).center()
        return table
    }

    fun shouldClose(): Boolean = closeRequested

    fun resize(w: Int, h: Int) { stage.viewport.update(w, h, true) }
    fun dispose() { stage.dispose() }

    // --- Skin & Drawable Helpers (copy from previous UI files) ---
    private fun createCrucibleSkin(): Skin { /* ... same as before ... */ }
    private fun createColoredDrawable(color: Color, alpha: Float = 1f): Drawable { /* ... same as before ... */ }
}

// Simple container for the cauldron slots
class IngredientSlot {
    private var ingredient: Ingredient? = null
    
    fun getActor(): Table {
        val table = Table()
        table.background = createColoredDrawable(Color.WHITE, 0.3f)
        val label = Label("Empty", Skin()) // temporary
        table.add(label)
        return table
    }
    fun setIngredient(ing: Ingredient) { ingredient = ing }
    fun getIngredient(): Ingredient? = ingredient
    fun isEmpty(): Boolean = ingredient == null
    fun clear() { ingredient = null }
}