// QuantumG/audio/QuantumSoundManager.kt
package com.quantumg.audio

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.math.MathUtils

class QuantumSoundManager {
    companion object {
        private const val SAMPLE_RATE = 44100f
        private val generatedSounds = mutableMapOf<String, Sound>()
        private var bgMusic: Music? = null
        
        // Volume control
        var masterVolume = 1.0f
        var sfxVolume = 1.0f
        var musicVolume = 0.5f
        
        // --- GENERATE PROCEDURAL SOUNDS ---
        
        // 1. Quantum Bolt SFX (ZAP!)
        fun generateBoltSound(): Sound {
            val key = "quantum_bolt"
            return generatedSounds.getOrPut(key) {
                // Generate a quick descending sine sweep with a pop
                val duration = 0.15f
                val samples = (duration * SAMPLE_RATE).toInt()
                val data = ShortArray(samples)
                
                for (i in 0 until samples) {
                    val t = i / SAMPLE_RATE
                    val freq = 800f - t * 2000f // Descending pitch
                    val amplitude = 0.5f * Math.exp(-t * 10.0).toFloat() // Exponential decay
                    val sample = amplitude * Math.sin(2.0 * Math.PI * freq * t).toFloat()
                    // Add a tiny bit of white noise for "quantum fuzz"
                    val noise = (Math.random().toFloat() - 0.5f) * 0.1f
                    data[i] = ((sample + noise) * 32767f).toInt().coerceIn(-32768, 32767).toShort()
                }
                
                createSoundFromPCM(data, SAMPLE_RATE.toInt())
            }
        }
        
        // 2. Entanglement Link SFX (Wobble)
        fun generateEntangleSound(): Sound {
            val key = "entanglement"
            return generatedSounds.getOrPut(key) {
                val duration = 0.3f
                val samples = (duration * SAMPLE_RATE).toInt()
                val data = ShortArray(samples)
                
                for (i in 0 until samples) {
                    val t = i / SAMPLE_RATE
                    val freq1 = 440f + t * 200f
                    val freq2 = 880f - t * 300f
                    val amplitude = 0.4f * Math.sin(t * 5.0).toFloat().coerceAtLeast(0f)
                    val sample = amplitude * ( 
                        Math.sin(2.0 * Math.PI * freq1 * t).toFloat() * 0.6f +
                        Math.sin(2.0 * Math.PI * freq2 * t).toFloat() * 0.4f
                    )
                    data[i] = (sample * 32767f).toInt().coerceIn(-32768, 32767).toShort()
                }
                
                createSoundFromPCM(data, SAMPLE_RATE.toInt())
            }
        }
        
        // 3. Achievement Unlock! (Triumphant chord)
        fun generateAchievementSound(): Sound {
            val key = "achievement"
            return generatedSounds.getOrPut(key) {
                val duration = 0.5f
                val samples = (duration * SAMPLE_RATE).toInt()
                val data = ShortArray(samples)
                
                // C Major arpeggio: C-E-G-C with a bright attack
                val frequencies = listOf(261.63f, 329.63f, 392.00f, 523.25f)
                for (i in 0 until samples) {
                    val t = i / SAMPLE_RATE
                    val amp = 0.8f * Math.exp(-t * 0.5).toFloat() // Decay
                    var sample = 0f
                    for (j in frequencies.indices) {
                        val delay = j * 0.025f // Stagger the notes
                        if (t > delay) {
                            val freq = frequencies[j]
                            val noteAmp = amp * (1f - j * 0.1f)
                            sample += noteAmp * Math.sin(2.0 * Math.PI * freq * (t - delay)).toFloat()
                        }
                    }
                    data[i] = (sample * 0.5f * 32767f).toInt().coerceIn(-32768, 32767).toShort()
                }
                
                createSoundFromPCM(data, SAMPLE_RATE.toInt())
            }
        }
        
        // 4. Level Up! (Ascending tone)
        fun generateLevelUpSound(): Sound {
            val key = "level_up"
            return generatedSounds.getOrPut(key) {
                val duration = 0.25f
                val samples = (duration * SAMPLE_RATE).toInt()
                val data = ShortArray(samples)
                
                for (i in 0 until samples) {
                    val t = i / SAMPLE_RATE
                    val freq = 440f + t * 660f // Ascending
                    val amplitude = 0.6f * (1f - t / duration)
                    val vibrato = 0.05f * Math.sin(2.0 * Math.PI * 20.0 * t).toFloat()
                    val sample = amplitude * Math.sin(2.0 * Math.PI * (freq + vibrato) * t).toFloat()
                    data[i] = (sample * 32767f).toInt().coerceIn(-32768, 32767).toShort()
                }
                
                createSoundFromPCM(data, SAMPLE_RATE.toInt())
            }
        }
        
        // 5. Boss Spawn! (Dramatic low rumble)
        fun generateBossSound(): Sound {
            val key = "boss_spawn"
            return generatedSounds.getOrPut(key) {
                val duration = 0.8f
                val samples = (duration * SAMPLE_RATE).toInt()
                val data = ShortArray(samples)
                
                for (i in 0 until samples) {
                    val t = i / SAMPLE_RATE
                    val freq = 80f + t * 120f // Ascending rumble
                    val amplitude = 0.8f * (1f - Math.exp(-t * 20.0)).toFloat() // Slow attack
                    val sample = amplitude * (
                        Math.sin(2.0 * Math.PI * freq * t).toFloat() * 0.5f +
                        Math.sin(2.0 * Math.PI * (freq * 0.5f) * t).toFloat() * 0.3f +
                        Math.sin(2.0 * Math.PI * (freq * 1.5f) * t).toFloat() * 0.2f
                    )
                    data[i] = (sample * 32767f).toInt().coerceIn(-32768, 32767).toShort()
                }
                
                createSoundFromPCM(data, SAMPLE_RATE.toInt())
            }
        }
        
        // 6. Quantum Bubble (Ambient, soothing)
        fun generateBubbleSound(): Sound {
            val key = "quantum_bubble"
            return generatedSounds.getOrPut(key) {
                val duration = 0.4f
                val samples = (duration * SAMPLE_RATE).toInt()
                val data = ShortArray(samples)
                
                for (i in 0 until samples) {
                    val t = i / SAMPLE_RATE
                    val freq = 400f + 200f * Math.sin(t * 10.0).toFloat()
                    val amplitude = 0.3f * (1f - t / duration)
                    val sample = amplitude * Math.sin(2.0 * Math.PI * freq * t).toFloat()
                    // Add gentle harmonics
                    val harmonic = 0.2f * amplitude * Math.sin(4.0 * Math.PI * freq * t).toFloat()
                    data[i] = ((sample + harmonic) * 32767f).toInt().coerceIn(-32768, 32767).toShort()
                }
                
                createSoundFromPCM(data, SAMPLE_RATE.toInt())
            }
        }
        
        // --- HELPER: Create LibGDX Sound from PCM data ---
        private fun createSoundFromPCM(data: ShortArray, sampleRate: Int): Sound {
            // Convert ShortArray to byte array (PCM 16-bit, little endian)
            val byteArray = ByteArray(data.size * 2)
            for (i in data.indices) {
                val value = data[i].toInt()
                byteArray[i * 2] = (value and 0xFF).toByte()
                byteArray[i * 2 + 1] = (value shr 8 and 0xFF).toByte()
            }
            
            // Create a Sound from the PCM data
            // Note: This is a simplified approach; in production you'd use a custom Sound implementation
            // For now, we'll use a workaround: write to a temporary file and load it
            val tempFile = Gdx.files.local("temp_sound_${System.currentTimeMillis()}.wav")
            // Write WAV header (simplified)
            val wavData = createWavData(byteArray, sampleRate, data.size)
            tempFile.writeBytes(wavData, false)
            
            val sound = Gdx.audio.newSound(tempFile)
            // Delete temp file after loading (or keep it for caching)
            // tempFile.delete()
            return sound
        }
        
        private fun createWavData(pcmData: ByteArray, sampleRate: Int, sampleCount: Int): ByteArray {
            // WAV header construction (44 bytes for PCM)
            val header = ByteArray(44)
            // RIFF header
            header[0] = 'R'.code.toByte()
            header[1] = 'I'.code.toByte()
            header[2] = 'F'.code.toByte()
            header[3] = 'F'.code.toByte()
            // Chunk size (36 + data size)
            val dataSize = pcmData.size
            val chunkSize = 36 + dataSize
            header[4] = (chunkSize and 0xFF).toByte()
            header[5] = (chunkSize shr 8 and 0xFF).toByte()
            header[6] = (chunkSize shr 16 and 0xFF).toByte()
            header[7] = (chunkSize shr 24 and 0xFF).toByte()
            // WAVE
            header[8] = 'W'.code.toByte()
            header[9] = 'A'.code.toByte()
            header[10] = 'V'.code.toByte()
            header[11] = 'E'.code.toByte()
            // fmt chunk
            header[12] = 'f'.code.toByte()
            header[13] = 'm'.code.toByte()
            header[14] = 't'.code.toByte()
            header[15] = ' '.code.toByte()
            // fmt size (16)
            header[16] = 16
            header[17] = 0
            header[18] = 0
            header[19] = 0
            // Audio format (1 = PCM)
            header[20] = 1
            header[21] = 0
            // Channels (1 = mono)
            header[22] = 1
            header[23] = 0
            // Sample rate
            header[24] = (sampleRate and 0xFF).toByte()
            header[25] = (sampleRate shr 8 and 0xFF).toByte()
            header[26] = (sampleRate shr 16 and 0xFF).toByte()
            header[27] = (sampleRate shr 24 and 0xFF).toByte()
            // Byte rate (sampleRate * channels * bitsPerSample/8)
            val byteRate = sampleRate * 1 * 2 // 16-bit = 2 bytes
            header[28] = (byteRate and 0xFF).toByte()
            header[29] = (byteRate shr 8 and 0xFF).toByte()
            header[30] = (byteRate shr 16 and 0xFF).toByte()
            header[31] = (byteRate shr 24 and 0xFF).toByte()
            // Block align (channels * bitsPerSample/8)
            header[32] = 2
            header[33] = 0
            // Bits per sample (16)
            header[34] = 16
            header[35] = 0
            // data chunk
            header[36] = 'd'.code.toByte()
            header[37] = 'a'.code.toByte()
            header[38] = 't'.code.toByte()
            header[39] = 'a'.code.toByte()
            header[40] = (dataSize and 0xFF).toByte()
            header[41] = (dataSize shr 8 and 0xFF).toByte()
            header[42] = (dataSize shr 16 and 0xFF).toByte()
            header[43] = (dataSize shr 24 and 0xFF).toByte()
            
            // Combine header + PCM data
            return header + pcmData
        }
        
        // --- PLAYBACK FUNCTIONS ---
        
        fun playQuantumBolt() {
            val sound = generateBoltSound()
            sound.play(sfxVolume * masterVolume)
        }
        
        fun playEntanglement() {
            val sound = generateEntangleSound()
            sound.play(sfxVolume * masterVolume * 0.7f)
        }
        
        fun playAchievement() {
            val sound = generateAchievementSound()
            sound.play(sfxVolume * masterVolume * 0.8f)
        }
        
        fun playLevelUp() {
            val sound = generateLevelUpSound()
            sound.play(sfxVolume * masterVolume * 0.9f)
        }
        
        fun playBossSpawn() {
            val sound = generateBossSound()
            sound.play(sfxVolume * masterVolume * 0.6f)
        }
        
        fun playQuantumBubble(positionX: Float = 0f, positionY: Float = 0f) {
            // For spatial audio, we could pan left/right based on position
            val sound = generateBubbleSound()
            // Placeholder for panning (LibGDX Sound doesn't support 2D panning directly)
            sound.play(sfxVolume * masterVolume * 0.3f)
        }
        
        // --- MUSIC (Looping ambient track) ---
        
        fun startAmbientMusic() {
            // Generate a simple ambient drone (infinite loop)
            // For now, we'll use a placeholder - in production, you'd load a real music file
            // We'll generate a simple sine wave loop
            bgMusic?.stop()
            bgMusic = generateAmbientDrone()
            bgMusic?.isLooping = true
            bgMusic?.volume = musicVolume * masterVolume
            bgMusic?.play()
        }
        
        private fun generateAmbientDrone(): Music {
            // Simplified: use a very long sample or a generated wave
            // For now, we'll just play a sine wave as placeholder
            // In production: use a real .wav or .mp3 file
            val tempFile = Gdx.files.local("ambient_drone.wav")
            // Generate 10 seconds of ambient drone
            val duration = 10f
            val samples = (duration * SAMPLE_RATE).toInt()
            val data = ShortArray(samples)
            
            for (i in 0 until samples) {
                val t = i / SAMPLE_RATE
                val freq1 = 110f + 20f * Math.sin(t * 0.1).toFloat()
                val freq2 = 220f + 15f * Math.sin(t * 0.07).toFloat()
                val amp = 0.2f
                val sample = amp * (
                    Math.sin(2.0 * Math.PI * freq1 * t).toFloat() * 0.6f +
                    Math.sin(2.0 * Math.PI * freq2 * t).toFloat() * 0.4f
                )
                data[i] = (sample * 32767f).toInt().coerceIn(-32768, 32767).toShort()
            }
            
            val pcmData = data.toList().fold(ByteArray(0)) { acc, value ->
                val bytes = byteArrayOf(
                    (value.toInt() and 0xFF).toByte(),
                    (value.toInt() shr 8 and 0xFF).toByte()
                )
                acc + bytes
            }
            
            val wavData = createWavData(pcmData, SAMPLE_RATE.toInt(), data.size)
            tempFile.writeBytes(wavData, false)
            
            return Gdx.audio.newMusic(tempFile)
        }
        
        fun stopMusic() {
            bgMusic?.stop()
            bgMusic?.dispose()
        }
        
        fun setMusicVolume(volume: Float) {
            musicVolume = volume.coerceIn(0f, 1f)
            bgMusic?.volume = musicVolume * masterVolume
        }
        
        // --- CLEANUP ---
        
        fun dispose() {
            bgMusic?.dispose()
            for (sound in generatedSounds.values) {
                sound.dispose()
            }
            generatedSounds.clear()
        }
    }
}