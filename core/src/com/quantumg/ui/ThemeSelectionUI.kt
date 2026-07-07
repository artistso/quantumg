// QuantumG/ui/ThemeSelectionUI.kt
package com.quantumg.ui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.quantumg.themes.ThemeManager
import com.quantumg.themes.ThemePalette

class ThemeSelectionUI(
    private val onClose: () -> Unit,
    private val onThemeApplied: () -> Unit // Callback to refresh HUD
) {
    val stage = Stage(ScreenViewport())
    private val skin = ThemeManager.getSkin()
    private var closeRequested = false

    init {
        buildSelector()
    }

    private fun buildSelector() {
        val overlay = Table().apply {
            setFillParent(true)
            background = ThemeManager.createDrawable(
                Color(0f, 0f, 0f, 0.7f)
            )
        }

        val window = Window("🎨 Choose Your Theme", skin).apply {
            setModal(true)
            setMovable(false)
            setSize(600f, 350f)
            center()
        }

        val content = Table()
        content.pad(20f)

        // Description
        content.add(Label("Pick a color palette for your quantum journey:", skin))
            .colspan(2)
            .center()
            .padBottom(20f)
            .row()

        // Show each theme as a clickable card
        val themes = ThemeManager.getAvailableThemes()
        for (theme in themes) {
            val isActive = theme.id == ThemeManager.getCurrentTheme().id
            
            // Card background
            val card = Table(skin).apply {
                background = if (isActive) {
                    ThemeManager.createDrawable(theme.secondary, 0.3f) // Highlight active
                } else {
                    ThemeManager.createDrawable(theme.background, 0.5f)
                }
                pad(15f)
            }

            // Color swatch preview (a small row of circles)
            val swatchTable = Table()
            swatchTable.add(createSwatch(theme.primary)).padRight(5f)
            swatchTable.add(createSwatch(theme.secondary)).padRight(5f)
            swatchTable.add(createSwatch(theme.accent)).padRight(5f)
            swatchTable.add(createSwatch(theme.background))

            card.add(Label(theme.name, skin)).left().row()
            card.add(swatchTable).left().row()

            // Apply button (or "Active" badge)
            val actionBtn = TextButton(
                if (isActive) "✅ Active" else "Apply",
                skin
            )
            if (isActive) {
                actionBtn.isDisabled = true
            } else {
                actionBtn.addListener {
                    ThemeManager.setTheme(theme.id)
                    onThemeApplied() // Refresh the HUD
                    closeRequested = true // Close the picker
                    true
                }
            }
            card.add(actionBtn).padTop(10f).center()

            content.add(card).pad(8f).width(250f).height(130f)
        }

        // Close button
        content.row()
        val closeBtn = TextButton("Close", skin)
        closeBtn.addListener {
            closeRequested = true
            true
        }
        content.add(closeBtn).colspan(2).padTop(20f).center()

        window.add(content)
        overlay.add(window).center()
        stage.addActor(overlay)
    }

    private fun createSwatch(color: Color): Table {
        val table = Table()
        table.background = ThemeManager.createDrawable(color, 1f)
        table.setSize(20f, 20f)
        // Add a tiny white border around the swatch
        return table
    }

    fun shouldClose(): Boolean = closeRequested

    fun resize(w: Int, h: Int) {
        stage.viewport.update(w, h, true)
    }

    fun dispose() {
        stage.dispose()
    }
}