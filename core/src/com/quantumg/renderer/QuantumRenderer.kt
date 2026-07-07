// QuantumG/renderer/QuantumRenderer.kt
package com.quantumg.renderer

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Pool
import com.quantumg.themes.ThemeManager

// --- 1. A Renderable Entity (Poolable) ---
class RenderableEntity : Pool.Poolable {
    var x = 0f
    var y = 0f
    var width = 20f
    var height = 20f
    var region: TextureRegion? = null
    var color = Color.WHITE
    var alpha = 1f
    var rotation = 0f
    var isActive = false

    override fun reset() {
        x = 0f; y = 0f; width = 20f; height = 20f
        region = null
        color = Color.WHITE
        alpha = 1f
        rotation = 0f
        isActive = false
    }

    fun set(position: Vector2, region: TextureRegion, color: Color, size: Float) {
        this.x = position.x
        this.y = position.y
        this.width = size
        this.height = size
        this.region = region
        this.color = color
        this.alpha = 1f
        this.isActive = true
    }
}

// --- 2. The Main Renderer (Singleton/Context) ---
class QuantumRenderer {
    private val batch = SpriteBatch()
    private val pool = Pool<RenderableEntity> { RenderableEntity() }
    private val activeEntities = Array<RenderableEntity>()
    
    // Pre-generated texture atlas (we create colored circles procedurally)
    private val textureCache = mutableMapOf<String, TextureRegion>()

    init {
        // Pre-generate common shapes: a circle, a glow, a star (for bosses)
        getCircleRegion(32, Color.WHITE) // Default white circle
        getCircleRegion(32, Color.RED)   // Red circle
        getCircleRegion(32, Color.CYAN)  // Cyan circle
        getCircleRegion(32, Color.PURPLE)
        getCircleRegion(32, Color.ORANGE)
        // Glow region (larger, blurred circle - we fake it with a slightly transparent bigger circle)
        getGlowRegion(64)
    }

    // --- Generate a colored circle texture region (cached) ---
    private fun getCircleRegion(size: Int, color: Color): TextureRegion {
        val key = "${size}_${color.toString()}"
        return textureCache.getOrPut(key) {
            val pixmap = Pixmap(size, size, Pixmap.Format.RGBA8888)
            pixmap.setColor(color)
            // Draw a filled circle
            pixmap.fillCircle(size / 2, size / 2, size / 2 - 1)
            // Anti-aliased edge (add a soft border)
            pixmap.setColor(Color(1f, 1f, 1f, 0f))
            pixmap.drawCircle(size / 2, size / 2, size / 2 - 2)
            val texture = Texture(pixmap)
            pixmap.dispose()
            TextureRegion(texture)
        }
    }

    private fun getGlowRegion(size: Int): TextureRegion {
        val key = "glow_$size"
        return textureCache.getOrPut(key) {
            val pixmap = Pixmap(size, size, Pixmap.Format.RGBA8888)
            // Radial gradient effect: center opaque, edges transparent
            for (x in 0 until size) {
                for (y in 0 until size) {
                    val dx = x - size / 2f
                    val dy = y - size / 2f
                    val dist = Math.sqrt((dx * dx + dy * dy).toDouble())
                    val maxDist = size / 2f
                    val alpha = (1f - (dist / maxDist).toFloat()).coerceIn(0f, 1f)
                    pixmap.setColor(Color(1f, 1f, 1f, alpha * 0.5f))
                    pixmap.drawPixel(x, y)
                }
            }
            val texture = Texture(pixmap)
            pixmap.dispose()
            TextureRegion(texture)
        }
    }

    // --- API: Spawn a renderable entity (returns a reference for updates) ---
    fun spawnEntity(position: Vector2, color: Color, size: Float = 20f, isGlow: Boolean = false): RenderableEntity {
        val entity = pool.obtain()
        val region = if (isGlow) getGlowRegion(64) else getCircleRegion(size.toInt(), color)
        entity.set(position, region, color, size)
        activeEntities.add(entity)
        return entity
    }

    // --- Remove an entity from the active list (recycle it) ---
    fun recycleEntity(entity: RenderableEntity) {
        if (entity.isActive) {
            entity.isActive = false
            activeEntities.removeValue(entity, true)
            pool.free(entity)
        }
    }

    // --- Update all entities (e.g., fade out, scale up) ---
    fun update(delta: Float) {
        // We can add behaviors here (e.g., fade out alpha over time)
        // For now, we just let the game logic handle it.
    }

    // --- RENDER: Call this once per frame ---
    fun render(cameraOffsetX: Float = 0f, cameraOffsetY: Float = 0f) {
        val palette = ThemeManager.getCurrentTheme()
        batch.projectionMatrix = batch.projectionMatrix.translate(cameraOffsetX, cameraOffsetY, 0f)
        batch.begin()
        
        for (entity in activeEntities) {
            if (!entity.isActive) continue
            val region = entity.region ?: continue
            val color = entity.color
            batch.setColor(color.r, color.g, color.b, entity.alpha)
            batch.draw(
                region,
                entity.x - entity.width / 2,
                entity.y - entity.height / 2,
                entity.width,
                entity.height
            )
        }
        
        batch.end()
        // Reset projection matrix for next frame (or we handle it outside)
    }

    // --- Clear all entities (for scene transitions) ---
    fun clear() {
        for (entity in activeEntities) {
            entity.isActive = false
            pool.free(entity)
        }
        activeEntities.clear()
    }

    fun dispose() {
        batch.dispose()
        textureCache.values.forEach { it.texture.dispose() }
    }
}