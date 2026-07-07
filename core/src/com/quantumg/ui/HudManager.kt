// QuantumG/ui/HudManager.kt
package com.quantumg.ui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.quantumg.core.GameState
import com.quantumg.data.Ingredient
import com.quantumg.themes.ThemeManager

class HudManager(
    private val onTalentTreeOpen: () -> Unit,
    private val onAlchemyOpen: () -> Unit,
    private val onThemeOpen: (() -> Unit)? = null,
    private val onAchievementsOpen: (() -> Unit)? = null,
) {
    val stage = Stage(ScreenViewport())
    private val skin = ThemeManager.getSkin()

    private lateinit var hpLabel: Label
    private lateinit var manaLabel: Label
    private lateinit var levelLabel: Label
    private lateinit var waveLabel: Label
    private lateinit var ingredientLabels: MutableMap<Ingredient, Label>
    private lateinit var spellButtons: MutableList<TextButton>

    init {
        buildHud()
    }

    private fun buildHud() {
        val root = Table().apply {
            setFillParent(true)
            pad(20f)
        }

        // --- TOP ROW ---
        val topRow = Table()
        topRow.left().top()

        val playerPanel = Table(skin).apply {
            background = createColoredDrawable(Color.DARK_GRAY, 0.8f)
            pad(15f)
        }
        hpLabel = Label("HP: ${GameState.player.currentHp}/${GameState.player.maxHp}", skin)
        manaLabel = Label("Mana: ${GameState.player.currentMana}/${GameState.player.maxMana}", skin)
        levelLabel = Label("Lv. ${GameState.player.level}", skin)

        playerPanel.add(levelLabel).padRight(20f)
        playerPanel.add(hpLabel).padRight(20f)
        playerPanel.add(manaLabel)
        topRow.add(playerPanel).expandX().left()

        val wavePanel = Table(skin).apply {
            background = createColoredDrawable(Color.DARK_GRAY, 0.8f)
            pad(15f)
        }
        waveLabel = Label("Wave: ${GameState.currentWave}  |  Kills: ${GameState.enemiesKilled}", skin)
        wavePanel.add(waveLabel)
        topRow.add(wavePanel).expandX().right()

        root.add(topRow).fillX().row()
        root.add().expandY().fill().row()

        // --- BOTTOM ROW ---
        val bottomRow = Table()
        bottomRow.bottom()

        val ingredientPanel = Table(skin).apply {
            background = createColoredDrawable(Color.BROWN, 0.7f)
            pad(10f)
        }
        ingredientPanel.add(Label("Ingredients", skin)).colspan(2).center().row()

        ingredientLabels = mutableMapOf()
        for (ing in Ingredient.values()) {
            val count = GameState.player.inventoryIngredients[ing] ?: 0
            val label = Label("${ing.name}: $count", skin)
            ingredientLabels[ing] = label
            ingredientPanel.add(label).left().pad(2f).row()
        }
        bottomRow.add(ingredientPanel).width(200f).padBottom(20f)

        val spellPanel = Table(skin).apply {
            background = createColoredDrawable(Color.BLUE, 0.5f)
            pad(10f)
        }
        spellPanel.add(Label("Spells (draw gesture)", skin)).colspan(4).center().row()

        spellButtons = mutableListOf()
        val spells = GameState.player.unlockedSpells
        for (spell in spells) {
            val btn = TextButton("${spell.name}\n${spell.gesturePattern}", skin)
            btn.isDisabled = true
            spellButtons.add(btn)
            spellPanel.add(btn).pad(4f).width(100f).height(60f)
        }
        for (i in spells.size until 4) {
            val dummy = TextButton("Empty Slot", skin)
            dummy.isDisabled = true
            spellPanel.add(dummy).pad(4f).width(100f).height(60f)
        }
        bottomRow.add(spellPanel).expandX().center().padBottom(20f)

        // Action Buttons
        val actionPanel = Table(skin).apply {
            background = createColoredDrawable(Color.GOLD, 0.7f)
            pad(15f)
        }

        val talentBtn = TextButton("Talent Tree (${GameState.player.talentPoints} pts)", skin)
        talentBtn.addListener { onTalentTreeOpen(); true }

        val alchemyBtn = TextButton("Alchemy Crucible", skin)
        alchemyBtn.addListener { onAlchemyOpen(); true }

        val achievementsBtn = TextButton("Achievements", skin)
        achievementsBtn.addListener { onAchievementsOpen?.invoke(); true }

        val themeBtn = TextButton("Themes", skin)
        themeBtn.addListener { onThemeOpen?.invoke(); true }

        actionPanel.add(talentBtn).pad(5f).width(180f).row()
        actionPanel.add(alchemyBtn).pad(5f).width(180f).row()
        actionPanel.add(achievementsBtn).pad(5f).width(180f).row()
        actionPanel.add(themeBtn).pad(5f).width(180f)

        bottomRow.add(actionPanel).width(200f).padBottom(20f)
        root.add(bottomRow).fillX().height(280f).row()

        stage.addActor(root)
    }

    fun update() {
        hpLabel.setText("HP: ${GameState.player.currentHp}/${GameState.player.maxHp}")
        manaLabel.setText("Mana: ${GameState.player.currentMana}/${GameState.player.maxMana}")
        levelLabel.setText("Lv. ${GameState.player.level}")
        waveLabel.setText("Wave: ${GameState.currentWave}  |  Kills: ${GameState.enemiesKilled}")
        for ((ing, label) in ingredientLabels) {
            val count = GameState.player.inventoryIngredients[ing] ?: 0
            label.setText("${ing.name}: $count")
        }
    }

    fun rebuildUI() {
        stage.root.clear()
        // Re-skin from ThemeManager
        val newSkin = ThemeManager.getSkin()
        stage.root.clearChildren()
        buildHud()
    }

    fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    fun dispose() {
        stage.dispose()
    }

    private fun createColoredDrawable(color: Color, alpha: Float = 1f): Drawable {
        val pixmap = Pixmap(1, 1, Pixmap.Format.RGBA8888)
        pixmap.setColor(color.r, color.g, color.b, alpha)
        pixmap.fill()
        val texture = Texture(pixmap)
        pixmap.dispose()
        return TextureRegionDrawable(texture)
    }
}