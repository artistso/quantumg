// QuantumG/puzzles/QuantumLockPuzzle.kt
package com.quantumg.puzzles

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import qkd.*

class QuantumLockPuzzle(
    private val shapeRenderer: ShapeRenderer,
    private val onSolved: (String) -> Unit // Callback with the unlocked key
) {
    private val qkd = QuantumKeyExchange()
    private var playerBasis: List<Boolean>? = null
    private var aliceBasis: List<Boolean>? = null
    private var aliceBits: List<Boolean>? = null
    private var bobResults: List<Boolean>? = null
    private var siftedKey: String? = null
    private var isCompleted = false
    
    fun startPuzzle() {
        // Alice prepares quantum states
        aliceBasis = qkd.generateRandomBasis()
        aliceBits = qkd.generateRandomBits()
        // Bob measures (simulated)
        bobResults = qkd.measure(aliceBits!!, aliceBasis!!)
        // Player must guess the correct basis (for simplicity, they just click a button)
        isCompleted = false
    }
    
    fun submitPlayerBasis(basis: List<Boolean>) {
        if (isCompleted) return
        playerBasis = basis
        // Compare player's basis with Alice's basis to derive the key
        val key = qkd.siftKey(aliceBasis!!, basis, aliceBits!!, bobResults!!)
        if (key.isNotEmpty()) {
            siftedKey = key
            isCompleted = true
            onSolved(key)
        }
    }
    
    fun render() {
        // Visualize the quantum states as colored circles on screen
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        val alicePos = Vector2(200f, 300f)
        val bobPos = Vector2(600f, 300f)
        for (i in 0 until (aliceBits?.size ?: 0).coerceAtMost(20)) {
            val bit = aliceBits!![i]
            val color = if (bit) Color.GREEN else Color.RED
            val t = i.toFloat() / 20
            val pos = Vector2(
                alicePos.x + (bobPos.x - alicePos.x) * t,
                alicePos.y + (bobPos.y - alicePos.y) * t
            )
            shapeRenderer.color = color
            shapeRenderer.circle(pos.x, pos.y, 5f)
        }
        shapeRenderer.end()
    }
}