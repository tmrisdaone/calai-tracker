package com.calai.tracker.data.inference

import android.content.Context
import android.util.Log
import com.calai.tracker.data.model.CalorieResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.random.Random

/**
 * GGUF-format llama.cpp inference engine. Currently in simulation mode —
 * real llama.cpp JNI libraries are not bundled in jniLibs. Returns realistic
 * mock nutrition data so the rest of the app (scanner, settings) can be
 * exercised end-to-end.
 */
class GGUFInferenceEngine(private val context: Context) : LocalInferenceEngine {

    private data class MockNutrition(
        val foodName: String,
        val calories: Double,
        val protein: Double,
        val carbs: Double,
        val fat: Double,
        val fiber: Double,
    )

    override suspend fun analyzeImage(
        imageFile: File,
        modelPath: String
    ): CalorieResponse = withContext(Dispatchers.Default) {
        Log.d("GGUFEngine", "Loading GGUF model from: $modelPath")
        Log.d("GGUFEngine", "Analyzing image: ${imageFile.absolutePath}")

        // Simulate model load + inference latency so the spinner shows up.
        kotlinx.coroutines.delay(2000)

        val random = Random.Default
        val n = SIMULATED_FOODS.random(random)
        val jitter = { base: Double, spread: Double -> base + (random.nextDouble() * spread - spread / 2.0) }

        CalorieResponse(
            food_name = n.foodName,
            calories = jitter(n.calories, 20.0),
            protein_g = jitter(n.protein, 2.0),
            carbs_g = jitter(n.carbs, 5.0),
            fat_g = jitter(n.fat, 3.0),
            fiber_g = jitter(n.fiber, 2.0),
            serving_size = "1 serving",
            confidence = 0.75 + random.nextDouble() * 0.2, // 75-95%
            breakdown = "Local GGUF Engine (Simulation Mode) - Estimated nutrition for ${n.foodName}"
        )
    }

    companion object {
        // Realistic per-serving nutrition (calories, protein_g, carbs_g, fat_g, fiber_g)
        // for a handful of common foods. Used as fallback when the real GGUF
        // model isn't loaded (which is always, in the simulation path).
        private val SIMULATED_FOODS = listOf(
            MockNutrition("Apple",          95.0,  0.5, 25.0, 0.3, 4.0),
            MockNutrition("Banana",        105.0,  1.3, 27.0, 0.4, 3.1),
            MockNutrition("Chicken Breast",165.0, 31.0,  0.0, 3.6, 0.0),
            MockNutrition("Egg",            78.0,  6.3,  0.6, 5.3, 0.0),
            MockNutrition("Salmon",        208.0, 20.4,  0.0, 13.4, 0.0),
            MockNutrition("Broccoli",       55.0,  3.7, 11.2, 0.6, 5.1),
            MockNutrition("Rice (cooked)", 130.0,  2.7, 28.0, 0.3, 0.4),
            MockNutrition("Almonds",       164.0,  6.0,  6.1, 14.2, 3.5),
            MockNutrition("Yogurt",        150.0,  8.5, 11.4, 4.0, 0.0),
            MockNutrition("Avocado",       240.0,  3.0, 12.8, 22.0, 10.0),
        )
    }
}
