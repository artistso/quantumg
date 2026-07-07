// QuantumG/visualizer/QuantumFieldVisualizer.kt
package com.quantumg.visualizer

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import org.curvesapi.NurbsCurve
import org.curvesapi.Point
import graphs.*
import com.quantumg.geometry.QuantumFieldGeometry
import com.quantumg.network.QuantumNetwork

class QuantumFieldVisualizer(private val shapeRenderer: ShapeRenderer) {
    private val geometryEngine = QuantumFieldGeometry()
    private val networkEngine = QuantumNetwork()
    
    // Cached NURBS curves for performance
    private val fieldCurves = mutableMapOf<String, List<Vector2>>()
    
    // Render a NURBS curve with a given color and thickness
    fun renderCurve(controlPoints: List<Pair<Double, Double>>, color: Color, thickness: Float = 2f) {
        val samples = geometryEngine.createQuantumOrbit(controlPoints)
        val points = samples.map { Vector2(it.first.toFloat(), it.second.toFloat()) }
        
        shapeRenderer.color = color
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        for (i in 0 until points.size - 1) {
            shapeRenderer.line(points[i], points[i + 1])
        }
        shapeRenderer.end()
        
        // Draw control points as small circles
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = Color(1f, 1f, 1f, 0.5f)
        for (ctrl in controlPoints) {
            shapeRenderer.circle(ctrl.first.toFloat(), ctrl.second.toFloat(), 4f)
        }
        shapeRenderer.end()
    }
    
    // Render a graph network (nodes + edges)
    fun renderGraph(network: QuantumNetwork, nodeColor: Color, edgeColor: Color) {
        val graph = network.getGraph() // Returns the underlying graph structure
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        shapeRenderer.color = edgeColor
        for (edge in graph.edges) {
            val from = edge.first
            val to = edge.second
            shapeRenderer.line(from.x, from.y, to.x, to.y)
        }
        shapeRenderer.end()
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = nodeColor
        for (vertex in graph.vertices) {
            shapeRenderer.circle(vertex.x, vertex.y, 6f)
        }
        shapeRenderer.end()
    }
    
    // Visualize a quantum key exchange (flashing particles)
    fun renderQKDExchange(alicePos: Vector2, bobPos: Vector2, bits: List<Boolean>) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        for ((index, bit) in bits.withIndex()) {
            val t = index.toFloat() / bits.size
            val pos = Vector2(
                alicePos.x + (bobPos.x - alicePos.x) * t,
                alicePos.y + (bobPos.y - alicePos.y) * t
            )
            shapeRenderer.color = if (bit) Color.GREEN else Color.RED
            shapeRenderer.circle(pos.x, pos.y, 3f)
        }
        shapeRenderer.end()
    }
}