// QuantumG/input/QuantumTouchController.kt
package com.quantumg.input

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.Color

class QuantumTouchController(
    private val playerPos: Vector2,
    private val moveSpeed: Float = 250f,
    private val onMove: (Vector2) -> Unit
) : InputAdapter() {

    private var joystickActive = false
    private var joystickBase = Vector2()
    private var joystickKnob = Vector2()
    private val joystickRadius = 80f
    private var targetPosition: Vector2? = null
    private var isMovingToTarget = false
    private val screenWidth = Gdx.graphics.width.toFloat()
    private val screenHeight = Gdx.graphics.height.toFloat()

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        val x = screenX.toFloat()
        val worldY = screenHeight - screenY.toFloat()

        if (x < screenWidth / 2) {
            joystickActive = true
            joystickBase.set(x, worldY)
            joystickKnob.set(x, worldY)
            isMovingToTarget = false
        } else {
            targetPosition = Vector2(x, worldY)
            isMovingToTarget = true
            joystickActive = false
        }
        return true
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        if (!joystickActive) return false
        val x = screenX.toFloat()
        val y = screenHeight - screenY.toFloat()
        val dx = x - joystickBase.x
        val dy = y - joystickBase.y
        val dist = MathUtils.sqrt(dx * dx + dy * dy)
        if (dist > joystickRadius) {
            joystickKnob.set(
                joystickBase.x + (dx / dist) * joystickRadius,
                joystickBase.y + (dy / dist) * joystickRadius
            )
        } else {
            joystickKnob.set(x, y)
        }
        if (dist > 20f) {
            onMove(Vector2(dx, dy).nor())
        } else {
            onMove(Vector2.Zero)
        }
        return true
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        if (joystickActive) {
            joystickActive = false
            joystickKnob.set(joystickBase)
            onMove(Vector2.Zero)
        }
        return true
    }

    fun update(delta: Float) {
        if (isMovingToTarget && targetPosition != null) {
            val target = targetPosition!!
            val dist = playerPos.dst(target)
            if (dist < 5f) {
                isMovingToTarget = false
                targetPosition = null
                onMove(Vector2.Zero)
                return
            }
            val direction = Vector2(target).sub(playerPos).nor()
            playerPos.add(direction.scl(moveSpeed * delta))
            onMove(direction)
        }
    }

    fun render(shapeRenderer: ShapeRenderer) {
        if (!joystickActive) return
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = Color(1f, 1f, 1f, 0.3f)
        shapeRenderer.circle(joystickBase.x, joystickBase.y, joystickRadius, 30)
        shapeRenderer.color = Color(1f, 1f, 1f, 0.7f)
        shapeRenderer.circle(joystickKnob.x, joystickKnob.y, 25f, 20)
        shapeRenderer.color = Color.CYAN
        shapeRenderer.circle(joystickKnob.x, joystickKnob.y, 10f, 15)
        shapeRenderer.end()
    }

    fun reset() {
        isMovingToTarget = false
        targetPosition = null
        joystickActive = false
        onMove(Vector2.Zero)
    }
}