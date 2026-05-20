package com.calai.tracker.data.inference

import android.content.Context
import android.util.Log
import com.calai.tracker.data.model.CalorieResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*

class GGUFInferenceEngine(private val context: Context) : LocalInferenceEngine {
    override suspend fun analyzeImage(imageFile: File, modelPath: String): CalorieResponse = withContext(Dispatchers.Default) {
        Log.d("GGUFEngine", "Loading GGUF model from: $modelPath")
        Log.d("GGUFEngine", "Analyzing image: ${imageFile.absolutePath}")

        // SIMULATION MODE: 
        // Until the native llama.cpp .so libraries are bundled in jniLibs,
        // we simulate the local inference process with realistic mock data.

        kotlinx.coroutines.delay(2000) // Simulate model loading/processing

        // Generate realistic mock nutrition data based on common foods
        val random = Random()
        val foodTypes = listOf(
            "Apple" to Pair(95.0, Pair(0.5, Pair(25.0, Pair(0.3, Pair(4.0, 0.9))))),
            "Banana" to Pair(105.0, Pair(1.3, Pair(27.0, Pair(0.4, Pair(3.1, 0.85))))),
            "Chicken Breast" to Pair(165.0, Pair(31.0, Pair(0.0, Pair(3.6, Pair(0.0, 0.9))))),
            "Egg" to Pair(78.0, Pair(6.3, Pair(0.6, Pair(5.3, Pair(0.0, 0.8))))),
            "Salmon" to Pair(208.0, Pair(20.4, Pair(0.0, Pair(13.4, Pair(0.0, 0.85))))),
            "Broccoli" to Pair(55.0, Pair(3.7, Pair(11.2, Pair(0.6, Pair(5.1, 0.9))))),
            "Rice (cooked)" to Pair(130.0, Pair(2.7, Pair(28.0, Pair(0.3, Pair(0.4, 0.75))))),
            "Almonds" to Pair(164.0, Pair(6.0, Pair(6.1, Pair(14.2, Pair(3.5, 0.7))))),
            "Yogurt" to Pair(150.0, Pair(8.5, Pair(11.4, Pair(4.0, Pair(0.0, 0.8))))),
            "Avocado" to Pair(240.0, Pair(3.0, Pair(12.8, Pair(22.0, Pair(10.0, 0.85)))))
        )

        val (foodName, nutrition) = foodTypes.random()
        val (calories, macros) = nutrition
        val (protein, carbsFatFiber) = macros
        val (carbs, fatFiber) = carbsFatFiber
        val (fat, fiber) = fatFiber

        return@withContext CalorieResponse(
            food_name = foodName,
            calories = calories + random.nextDouble() * 20 - 10, // ±10 cal variation
            protein_g = protein + random.nextDouble() * 2 - 1, // ±2g protein variation
            carbs_g = carbs + random.nextDouble() * 5 - 2.5, // ±2.5g carbs variation
            fat_g = fat + random.nextDouble() * 3 - 1.5, // ±1.5g fat variation
            fiber_g = fiber + random.nextDouble() * 2 - 1, // ±1g fiber variation
            serving_size = "1 serving",
            confidence = 0.75 + random.nextDouble() * 0.2, // 75-95% confidence
            breakdown = "Local GGUF Engine (Simulation Mode) - Estimated nutrition for $foodName"
        )
    }
}
