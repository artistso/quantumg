// QuantumG/ui/AchievementBanner.kt
package com.quantumg.ui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.viewport.ScreenViewport

class AchievementBanner {
    private val stage = Stage(ScreenViewport())
    private val skin = createSkin()
    private var currentBanner: Table? = null

    fun showAchievement(title: String, description: String, color: Color) {
        // Dismiss old banner if present
        currentBanner?.remove()

        val banner = Table(skin).apply {
            background = createColoredDrawable(color, 0.9f)
            pad(20f)
            // Position at the top, centered
            setPosition(
                (Gdx.graphics.width - 600f) / 2,
                Gdx.graphics.height.toFloat() - 120f
            )
            setSize(600f, 80f)
        }

        // Title
        val titleLabel = Label("🏆 $title", skin).apply {
            setColor(Color.WHITE)
            setFontScale(1.5f)
        }
        // Description
        val descLabel = Label(description, skin).apply {
            setColor(Color.LIGHT_GRAY)
            setFontScale(1.0f)
        }

        banner.add(titleLabel).center().row()
        banner.add(descLabel).center()

        // Slide-in animation
        banner.setPosition(banner.x, Gdx.graphics.height.toFloat() + 100f)
        banner.addAction(
            Actions.sequence(
                Actions.moveTo(banner.x, Gdx.graphics.height.toFloat() - 120f, 0.5f),
                Actions.delay(3.0f),
                Actions.moveTo(banner.x, Gdx.graphics.height.toFloat() + 100f, 0.5f),
                Actions.removeActor()
            )
        )

        stage.addActor(banner)
        currentBanner = banner
    }

    fun act(delta: Float) {
        stage.act(delta)
    }

    fun draw() {
        stage.draw()
    }

    fun dispose() {
        stage.dispose()
    }

    private fun createSkin(): Skin {
        val skin = Skin()
        val font = BitmapFont()
        skin.add("default-font", font)
        val labelStyle = Label.LabelStyle()
        labelStyle.font = font
        labelStyle.fontColor = Color.WHITE
        skin.add("default", labelStyle)
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