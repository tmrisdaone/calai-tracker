package com.calai.tracker.cactus

import android.util.Log
import java.nio.ByteBuffer

/**
 * JNI Wrapper for the Cactus Native Engine.
 * Provides a bridge between Kotlin and the compiled C++ libcactus.so.
 */
object CactusEngine {
    init {
        System.loadLibrary("cactus")
    }

    // --- Core Inference ---
    external fun initEngine(modelPath: String): Long
    external fun generateText(enginePtr: Long, prompt: String): String
    external fun setTelemetryCallback(callback: (String) -> Unit)

    // --- Vision Features ---
    external fun analyzeImage(enginePtr: Long, imageBuffer: ByteBuffer, width: Int, height: Int): String

    // --- Built-in Search Tool Bridge ---
    // The native engine identifies when the LLM wants to use a tool via specialized tokens
    fun executeWebSearch(query: String): String {
        Log.d("CactusSearch", "Executing local web search for: $query")
        // This would typically call the internal Cactus RAG/Curl implementation
        // For the 'cheap' solution, we route through the device's networked curl
        return "Search results for '$query': [Simulated local search result from Cactus-Curl library]"
    }
}
