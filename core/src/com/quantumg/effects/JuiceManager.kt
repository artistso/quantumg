// QuantumG/effects/JuiceManager.kt
package com.quantumg.effects

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2

object JuiceManager {
    // --- Camera Shake ---
    private var shakeIntensity = 0f
    private var shakeDuration = 0f
    private var shakeX = 0f
    private var shakeY = 0f

    // --- Hit-Stop (Freeze Frames) ---
    private var hitStopDuration = 0f
    private var hitStopTimer = 0f
    private var isFrozen = false
    private var originalDelta = 0f

    // --- Trigger a screen shake ---
    fun shake(intensity: Float = 10f, duration: Float = 0.3f) {
        shakeIntensity = intensity
        shakeDuration = duration
    }

    // --- Trigger Hit-Stop (freeze frame) ---
    fun hitStop(duration: Float = 0.05f) {
        hitStopDuration = duration
        hitStopTimer = 0f
        isFrozen = true
    }

    // --- Call this at the start of your render loop ---
    fun applyDelta(delta: Float): Float {
        // 1. Handle Hit-Stop
        if (isFrozen) {
            hitStopTimer += delta
            if (hitStopTimer >= hitStopDuration) {
                isFrozen = false
                hitStopTimer = 0f
            }
            return 0f // Freeze time!
        }
        return delta
    }

    // --- Call this every frame to update the shake offset ---
    fun updateShake(delta: Float): Vector2 {
        if (shakeDuration > 0) {
            shakeDuration -= delta
            val intensity = shakeIntensity * (shakeDuration / (shakeDuration + delta))
            shakeX = MathUtils.random(-intensity, intensity)
            shakeY = MathUtils.random(-intensity, intensity)
            return Vector2(shakeX, shakeY)
        }
        shakeX = 0f
        shakeY = 0f
        return Vector2.Zero
    }

    // --- Check if we're currently shaking ---
    fun isShaking(): Boolean = shakeDuration > 0

    // --- Reset ---
    fun reset() {
        shakeIntensity = 0f
        shakeDuration = 0f
        shakeX = 0f
        shakeY = 0f
        isFrozen = false
        hitStopDuration = 0f
        hitStopTimer = 0f
    }
}