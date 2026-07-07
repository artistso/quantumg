import space.kscience.kmath.linear.*
import space.kscience.kmath.operations.*

class QuantumStateEngine {
    fun applyQuantumGate(state: DoubleArray, gate: Array<DoubleArray>): DoubleArray {
        // Matrix-vector multiplication for quantum state evolution
        val result = DoubleArray(state.size)
        for (i in gate.indices) {
            var sum = 0.0
            for (j in state.indices) {
                sum += gate[i][j] * state[j]
            }
            result[i] = sum
        }
        return result
    }
}