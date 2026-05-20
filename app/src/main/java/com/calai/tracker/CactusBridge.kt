package com.calai.tracker

import android.util.Log
import android.content.Context

object CactusBridge {
    init {
        System.loadLibrary("cactus")
        System.loadLibrary("calai_jni")
    }

    external fun nativeInit(modelPath: String): String
    external fun nativeGenerate(prompt: String): String

    fun initialize(context: Context, modelPath: String): String {
        return try {
            nativeInit(modelPath)
        } catch (e: Exception) {
            Log.e("CactusBridge", "Init failed", e)
            "Failure: ${e.message}"
        }
    }

    fun generate(prompt: String): String {
        return try {
            nativeGenerate(prompt)
        } catch (e: Exception) {
            Log.e("CactusBridge", "Generation failed", e)
            "Error: ${e.message}"
        }
    }
}
