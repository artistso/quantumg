// QuantumG/themes/ThemeManager.kt
package com.quantumg.themes

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable

data class ThemePalette(
    val id: String,
    val name: String,
    val primary: Color,
    val secondary: Color,
    val accent: Color,
    val background: Color,
    val backgroundDark: Color,
    val text: Color,
    val textLight: Color,
    val enemyGlow: Color,
    val projectileColor: Color,
    val particleColor: Color,
    val gridColor: Color
)

object ThemeManager {

    private val themes = mapOf(
        "copic_pink" to ThemePalette(
            id = "copic_pink",
            name = "Copic Pink & White",
            primary = Color(1f, 0.42f, 0.7f, 1f),
            secondary = Color(1f, 0.08f, 0.58f, 1f),
            accent = Color(1f, 0.71f, 0.76f, 1f),
            background = Color(1f, 0.96f, 0.96f, 0.95f),
            backgroundDark = Color(0.2f, 0.05f, 0.1f, 0.85f),
            text = Color(0.29f, 0.0f, 0.06f, 1f),
            textLight = Color(0.5f, 0.2f, 0.3f, 1f),
            enemyGlow = Color(1f, 0.2f, 0.6f, 1f),
            projectileColor = Color(1f, 0.08f, 0.58f, 1f),
            particleColor = Color(1f, 0.42f, 0.7f, 1f),
            gridColor = Color(1f, 0.71f, 0.76f, 0.15f)
        ),
        "quantum_cyan" to ThemePalette(
            id = "quantum_cyan",
            name = "Quantum Cyan",
            primary = Color(0f, 0.8f, 1f, 1f),
            secondary = Color(0f, 0.5f, 0.8f, 1f),
            accent = Color(0.5f, 1f, 0.8f, 1f),
            background = Color(0.05f, 0.05f, 0.15f, 0.95f),
            backgroundDark = Color(0f, 0f, 0f, 0.85f),
            text = Color(1f, 1f, 1f, 1f),
            textLight = Color(0.7f, 0.9f, 1f, 1f),
            enemyGlow = Color(0f, 0.8f, 1f, 1f),
            projectileColor = Color(0f, 0.8f, 1f, 1f),
            particleColor = Color(0.5f, 1f, 0.8f, 1f),
            gridColor = Color(0f, 0.5f, 0.8f, 0.1f)
        )
    )

    private var currentThemeId = "copic_pink"
    private var currentPalette: ThemePalette = themes[currentThemeId]!!
    private var uiSkin: Skin? = null

    fun getCurrentTheme(): ThemePalette = currentPalette
    fun getAvailableThemes(): List<ThemePalette> = themes.values.toList()

    fun getSkin(): Skin {
        if (uiSkin == null) {
            uiSkin = buildSkin(currentPalette)
        }
        return uiSkin!!
    }

    fun setTheme(themeId: String): Boolean {
        val newPalette = themes[themeId] ?: return false
        currentPalette = newPalette
        currentThemeId = themeId
        uiSkin?.dispose()
        uiSkin = buildSkin(currentPalette)
        Gdx.app.log("QuantumG", "Theme switched to: ${newPalette.name}")
        return true
    }

    private fun buildSkin(palette: ThemePalette): Skin {
        val skin = Skin()
        val font = BitmapFont()
        skin.add("default-font", font)

        val labelStyle = Label.LabelStyle()
        labelStyle.font = font
        labelStyle.fontColor = palette.text
        skin.add("default", labelStyle)

        val btnStyle = TextButton.TextButtonStyle()
        btnStyle.font = font
        btnStyle.up = createDrawable(palette.primary, 0.9f)
        btnStyle.down = createDrawable(palette.secondary, 1f)
        btnStyle.over = createDrawable(palette.accent, 0.8f)
        btnStyle.fontColor = palette.text
        btnStyle.downFontColor = Color.WHITE
        skin.add("default", btnStyle)

        val windowStyle = Window.WindowStyle()
        windowStyle.titleFont = font
        windowStyle.titleFontColor = palette.secondary
        windowStyle.background = createDrawable(palette.background, 0.95f)
        skin.add("default", windowStyle)

        val scrollStyle = ScrollPane.ScrollPaneStyle()
        scrollStyle.vScrollKnob = createDrawable(palette.primary, 0.6f)
        scrollStyle.vScroll = createDrawable(palette.textLight, 0.3f)
        skin.add("default", scrollStyle)

        return skin
    }

    private fun createDrawable(color: Color, alpha: Float = 1f): Drawable {
        val pixmap = Pixmap(1, 1, Pixmap.Format.RGBA8888)
        pixmap.setColor(color.r, color.g, color.b, alpha)
        pixmap.fill()
        val texture = Texture(pixmap)
        pixmap.dispose()
        return TextureRegionDrawable(texture)
    }

    fun createBorderedDrawable(color: Color, borderSize: Int = 2): Drawable {
        val pixmap = Pixmap(1, 1, Pixmap.Format.RGBA8888)
        pixmap.setColor(color.r, color.g, color.b, 1f)
        pixmap.fill()
        val texture = Texture(pixmap)
        pixmap.dispose()
        return TextureRegionDrawable(texture)
    }

    fun notifyThemeChange() {
        // Hook for main game to refresh UI
    }
}