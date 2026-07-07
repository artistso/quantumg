// QuantumG/ui/TalentTreeUI.kt
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
import com.quantumg.nerd.EasterEggs
import com.quantumg.talent.TalentManager
import com.quantumg.talent.TalentNode

class TalentTreeUI(
    private val onClose: () -> Unit
) {
    val stage = Stage(ScreenViewport())
    private val skin = createTalentSkin()
    private var closeRequested = false

    init {
        buildTree()
    }

    private fun buildTree() {
        val overlay = Table().apply {
            setFillParent(true)
            background = createColoredDrawable(Color.BLACK, 0.75f)
        }

        val window = Window("Talent Tree", skin).apply {
            setModal(true)
            setMovable(false)
            setSize(800f, 700f)
            center()
        }

        val content = Table()
        content.pad(20f)

        val pointsLabel = Label("Available Points: ${GameState.player.talentPoints}", skin)
        content.add(pointsLabel).colspan(3).center().padBottom(20f).row()

        val tree = TalentManager.talentTree
        for (node in tree) {
            val currentLevel = TalentManager.getInvestedLevel(node.id)
            val isMaxed = currentLevel >= node.maxLevel
            val hasPrereqs = TalentManager.checkPrerequisites(node.id)
            val canAfford = GameState.player.talentPoints > 0 && !isMaxed && hasPrereqs

            val card = Table(skin).apply {
                background = createColoredDrawable(
                    if (canAfford) Color.GREEN else if (isMaxed) Color.GRAY else Color.DARK_GRAY,
                    0.8f
                )
                pad(15f)
            }

            card.add(Label("${node.iconEmoji} ${node.name}", skin)).left().row()
            card.add(Label("Level: $currentLevel/${node.maxLevel}", skin)).left().row()
            card.add(Label(node.description, skin)).left().wrap().row()

            if (node.prerequisites.isNotEmpty()) {
                val prereqText = "Requires: ${node.prerequisites.joinToString()}"
                card.add(Label(prereqText, skin)).left().padTop(5f).row()
            }

            val actionBtn = TextButton(
                if (isMaxed) "MAXED" else if (canAfford) "SPEND POINT" else "LOCKED",
                skin
            )
            actionBtn.isDisabled = !canAfford
            actionBtn.addListener {
                if (canAfford) {
                    val success = TalentManager.spendPoint(node.id)
                    if (success) {
                        EasterEggs.handleUIInteraction("talent_tree_button")
                        rebuildTree()
                    }
                }
                true
            }
            card.add(actionBtn).padTop(10f).center()
            content.add(card).pad(10f).width(220f).height(180f)
        }

        val closeBtn = TextButton("Close", skin)
        closeBtn.addListener {
            closeRequested = true
            true
        }
        content.row()
        content.add(closeBtn).colspan(3).padTop(30f).center()

        window.add(content).fill()
        overlay.add(window).center()
        stage.addActor(overlay)
    }

    fun rebuildTree() {
        stage.clear()
        buildTree()
    }

    fun shouldClose(): Boolean = closeRequested

    fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    fun dispose() {
        stage.dispose()
    }

    private fun createTalentSkin(): Skin {
        val skin = Skin()
        val font = BitmapFont()
        skin.add("default-font", font)

        val labelStyle = Label.LabelStyle()
        labelStyle.font = font
        labelStyle.fontColor = Color.WHITE
        skin.add("default", labelStyle)

        val buttonStyle = TextButton.TextButtonStyle()
        buttonStyle.font = font
        buttonStyle.up = createColoredDrawable(Color.LIGHT_GRAY)
        buttonStyle.down = createColoredDrawable(Color.GRAY)
        buttonStyle.over = createColoredDrawable(Color.WHITE)
        skin.add("default", buttonStyle)

        val windowStyle = Window.WindowStyle()
        windowStyle.titleFont = font
        windowStyle.titleFontColor = Color.GOLD
        windowStyle.background = createColoredDrawable(Color(0.1f, 0.1f, 0.2f, 0.95f))
        skin.add("default", windowStyle)

        return skin
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