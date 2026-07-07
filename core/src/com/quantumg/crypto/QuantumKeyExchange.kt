import qkd.*

class QuantumKeyExchange {
    private val alice = QKDParticipant("Alice")
    private val bob = QKDParticipant("Bob")
    
    fun exchangeQuantumKey(): String {
        // Simulate BB84 protocol
        val aliceBasis = generateRandomBasis()
        val aliceBits = generateRandomBits()
        
        // Alice sends quantum states (simulated as classical data)
        val quantumStates = alice.encode(aliceBits, aliceBasis)
        
        // Bob measures with random basis
        val bobBasis = generateRandomBasis()
        val bobResults = bob.measure(quantumStates, bobBasis)
        
        // Compare basis (sifted key)
        val siftedKey = alice.siftKey(aliceBasis, bobBasis, aliceBits, bobResults)
        return siftedKey
    }
    
    private fun generateRandomBasis(): List<Boolean> {
        return List(128) { Math.random() > 0.5 }
    }
    
    private fun generateRandomBits(): List<Boolean> {
        return List(128) { Math.random() > 0.5 }
    }
}