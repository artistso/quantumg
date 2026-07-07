// QuantumG/input/GestureSpellCaster.kt
package com.quantumg.input

import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.MathUtils
import com.quantumg.core.GameState
import com.quantumg.data.Spell
import com.quantumg.nerd.EasterEggs

class GestureSpellCaster(
    private val onSpellCast: (Spell, Vector2) -> Unit
) : InputAdapter() {

    private val touchPoints = mutableListOf<Vector2>()
    private var isDragging = false
    private val minPointsForGesture = 10
    private val circleThreshold = 0.3f

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        touchPoints.clear()
        isDragging = true
        touchPoints.add(Vector2(screenX.toFloat(), screenY.toFloat()))
        return true
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        if (isDragging) {
            touchPoints.add(Vector2(screenX.toFloat(), screenY.toFloat()))
            if (touchPoints.size > 50) touchPoints.removeAt(0)
        }
        return true
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        if (!isDragging || touchPoints.size < minPointsForGesture) {
            isDragging = false
            return false
        }
        touchPoints.add(Vector2(screenX.toFloat(), screenY.toFloat()))
        val gestureType = recognizeGesture(touchPoints)

        // Check secret gestures for Easter eggs
        EasterEggs.checkSecretGesture(gestureType)

        val matchingSpell = GameState.player.unlockedSpells.find {
            it.gesturePattern.equals(gestureType, ignoreCase = true)
        }
        matchingSpell?.let { spell ->
            val targetPos = Vector2(screenX.toFloat(), screenY.toFloat())
            onSpellCast(spell, targetPos)
            GameState.player.currentMana -= spell.manaCost
            EasterEggs.trackGesture(spell.gesturePattern)
        }
        isDragging = false
        touchPoints.clear()
        return true
    }

    private fun recognizeGesture(points: List<Vector2>): String {
        if (points.size < 10) return "UNKNOWN"
        var minX = Float.MAX_VALUE; var maxX = Float.MIN_VALUE
        var minY = Float.MAX_VALUE; var maxY = Float.MIN_VALUE
        for (p in points) {
            if (p.x < minX) minX = p.x; if (p.x > maxX) maxX = p.x
            if (p.y < minY) minY = p.y; if (p.y > maxY) maxY = p.y
        }
        val width = maxX - minX; val height = maxY - minY
        val centerX = (minX + maxX) / 2f; val centerY = (minY + maxY) / 2f
        val avgRadius = (width + height) / 4f
        var totalDeviation = 0f
        for (p in points) {
            val dist = Vector2(p.x - centerX, p.y - centerY).len()
            totalDeviation += Math.abs(dist - avgRadius) / avgRadius
        }
        val avgDeviation = totalDeviation / points.size
        if (avgDeviation < circleThreshold && width > 50 && height > 50) return "CIRCLE"

        val startPoint = points.first(); val endPoint = points.last()
        val dx = Math.abs(endPoint.x - startPoint.x)
        val dy = Math.abs(endPoint.y - startPoint.y)
        var pathLength = 0f
        for (i in 1 until points.size) pathLength += points[i].dst(points[i - 1])
        val straightDist = startPoint.dst(endPoint)
        val straightness = pathLength / straightDist
        if (straightness < 1.4f && straightDist > 100f) {
            return if (dx > dy) "LINE_HORIZONTAL" else "LINE_VERTICAL"
        }
        return "UNKNOWN"
    }
}