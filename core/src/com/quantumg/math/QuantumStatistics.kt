import com.jksalcedo.kotlinmathlib.KMath

class QuantumStatistics {
    fun calculateQuantumEntropy(probabilities: List<Double>): Double {
        // Shannon entropy for quantum measurement outcomes
        return -probabilities.sumOf { p -> 
            if (p > 0) p * KMath.log2(p) else 0.0 
        }
    }
}