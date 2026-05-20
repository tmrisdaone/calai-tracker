package com.calai.tracker.cactus

import android.content.Context
import android.os.Environment
import java.io.File
import java.net.URL
import java.io.FileOutputStream

object CactusModelManager {
    private const val MODEL_URL = "https://huggingface.co/cactus-compute/gemma4-2b-int4/resolve/main/model.bin"
    private const val MODEL_FILENAME = "cactus_gemma4.bin"

    fun ensureModelDownloaded(context: Context): String {
        val modelFile = File(context.filesDir, MODEL_FILENAME)
        if (!modelFile.exists()) {
            println("Downloading Cactus local model... this may take a while.")
            URL(MODEL_URL).openStream().use { input ->
                FileOutputStream(modelFile).use { output ->
                    input.copyTo(output)
                }
            }
        }
        return modelFile.absolutePath
    }
}
