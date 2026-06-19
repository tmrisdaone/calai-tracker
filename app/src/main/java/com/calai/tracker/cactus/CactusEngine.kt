package com.calai.tracker.cactus

import android.util.Log

/**
 * JNI Wrapper for the Cactus Native Engine.
 *
 * NOTE: The Cactus FFI surface in `cactus.h` is richer than what the
 * Kotlin side used to assume, and a real integration would need to
 * wire `cactus_init(model_path, corpus_dir, cache_index)` and
 * `cactus_complete(model, messages_json, response_buffer, ...)`
 * carefully (model lifetime, JSON message construction, output buffer
 * management, token streaming callback). For now the Kotlin layer
 * stands in for the native side entirely — the `external` declarations
 * were removed because the JNI shim (native-lib.cpp) only stubs the
 * CactusBridge entry points and there is no CactusEngine JNI.
 *
 * The Cactus integration is wired up at the higher level:
 *   - telemetry: emits a fake status string on a coroutine timer
 *   - search: pure-Kotlin stub returning simulated results
 *
 * When a real libcactus.so integration lands, restore the `external`
 * keywords and add the matching JNI symbols to native-lib.cpp.
 */
object CactusEngine {
    private const val TAG = "CactusEngine"

    @Volatile
    private var telemetryCallback: ((String) -> Unit)? = null

    @Volatile
    private var telemetryJob: kotlinx.coroutines.Job? = null

    /**
     * Register a callback to receive telemetry updates. Starts a
     * coroutine that emits a fake "ready" status once and then idles.
     * Real implementation would push tokens/usage from the native engine.
     */
    fun setTelemetryCallback(callback: (String) -> Unit) {
        telemetryCallback = callback
        Log.d(TAG, "Telemetry callback registered")
        // Fire once with a ready status so the UI can settle, then stop.
        // A real impl would keep streaming until setTelemetryCallback(null).
        try {
            callback("Ready (Cactus integration pending)")
        } catch (t: Throwable) {
            Log.w(TAG, "Telemetry callback threw", t)
        }
    }

    /**
     * Clear any active telemetry callback.
     */
    fun clearTelemetryCallback() {
        telemetryCallback = null
        telemetryJob?.cancel()
        telemetryJob = null
    }

    // --- Core Inference (stubbed until JNI is wired up) ---
    fun initEngine(modelPath: String): Long {
        Log.w(TAG, "initEngine called with $modelPath — Cactus integration pending")
        // Return a non-null placeholder so callers that only check for
        // null don't NPE. Real impl would call cactus_init() and return
        // the model handle.
        return 0L
    }

    fun generateText(enginePtr: Long, prompt: String): String {
        Log.w(TAG, "generateText called — Cactus integration pending")
        return "Cactus integration pending. Prompt was: ${prompt.take(80)}"
    }

    // --- Vision Features (stubbed) ---
    fun analyzeImage(
        enginePtr: Long,
        imageBuffer: java.nio.ByteBuffer,
        width: Int,
        height: Int
    ): String {
        Log.w(TAG, "analyzeImage called — Cactus integration pending")
        return "Cactus integration pending. Image: ${width}x${height}"
    }

    // --- Built-in Search Tool Bridge ---
    // The native engine identifies when the LLM wants to use a tool via specialized tokens
    fun executeWebSearch(query: String): String {
        Log.d(TAG, "Executing local web search for: $query")
        return "Search results for '$query': [Simulated local search result]"
    }
}
