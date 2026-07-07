package com.quantumg.desktop

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import com.quantumg.QuantumGGame

object DesktopLauncher {
    @JvmStatic
    fun main(args: Array<String>) {
        val config = Lwjgl3ApplicationConfiguration().apply {
            setTitle("QuantumG")
            setWindowedMode(1280, 720)
            useVsync(true)
            setForegroundFPS(60)
            setBackGroundFPS(30)
        }
        Lwjgl3Application(QuantumGGame(), config)
    }
}
