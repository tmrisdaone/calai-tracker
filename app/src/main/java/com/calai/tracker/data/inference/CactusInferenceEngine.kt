package com.calai.tracker.data.inference

import android.content.Context
import android.util.Log
import com.calai.tracker.CactusBridge
import com.calai.tracker.data.model.CalorieResponse
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class CactusInferenceEngine(private val context: Context) : LocalInferenceEngine {
    private val gson = Gson()

    override suspend fun analyzeImage(imageFile: File, modelPath: String): CalorieResponse = withContext(Dispatchers.Default) {
        Log.d("CactusEngine", "Loading Cactus model from: $modelPath")
        
        // Initialize the native engine
        val initResult = CactusBridge.initialize(context, modelPath)
        if (initResult != "Success") {
            throw Exception("Cactus Native Init Failed: $initResult")
        }

        // Construct the prompt for the local vision model
        // Note: libcactus handles the image loading internally or via cactus_complete
        // For now, we pass the path in the prompt as a simplified interface
        val prompt = "Analyze this food image at path: ${imageFile.absolutePath}. " +
                    "Respond ONLY with valid JSON in this format: " +
                    "{\"food_name\": \"...\", \"calories\": 0.0, \"protein_g\": 0.0, \"carbs_g\": 0.0, \"fat_g\": 0.0, \"fiber_g\": 0.0, \"serving_size\": \"...\", \"confidence\": 0.0, \"breakdown\": \"...\"}"

        Log.d("CactusEngine", "Generating local analysis...")
        val responseText = CactusBridge.generate(prompt)
        
        try {
            val cleaned = responseText.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
            gson.fromJson(cleaned, CalorieResponse::class.java)
        } catch (e: Exception) {
            Log.e("CactusEngine", "Parse error", e)
            CalorieResponse(
                food_name = "Cactus Parse Error",
                calories = 0.0,
                breakdown = "Local model output: $responseText"
            )
        }
    }
}
