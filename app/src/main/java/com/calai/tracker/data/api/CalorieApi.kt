package com.calai.tracker.data.api

import android.content.Context
import android.util.Base64
import com.calai.tracker.data.model.CalorieResponse
import com.calai.tracker.data.inference.LocalInferenceEngine
import com.calai.tracker.data.inference.CactusInferenceEngine
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import java.util.concurrent.TimeUnit

class CalorieApi(
    private var baseUrl: String = "http://localhost:11434/api",
    private var apiKey: String = "",
    private var currentModel: String = "llama2",
) {
    private val client: OkHttpClient
    private val gson = Gson()
    private val jsonMedia = "application/json; charset=utf-8".toMediaType()
    private var localEngine: LocalInferenceEngine? = null


    private val systemPrompt = """
        You are a precise food calorie analyzer. Given an image of food, identify what it is and estimate its nutritional content.
        Respond ONLY with valid JSON (no markdown, no code fences) in this exact format:
        {
          "food_name": "name of the food",
          "calories": total_calories,
          "protein_g": protein_in_grams,
          "carbs_g": carbs_in_grams,
          "fat_g": fat_in_grams,
          "fiber_g": fiber_in_grams,
          "serving_size": "estimated serving size description",
          "confidence": confidence_score_0_to_1,
          "breakdown": "brief reasoning"
        }
        Use realistic estimates based on typical serving sizes. If unsure, provide your best guess with lower confidence.
    """.trimIndent()

    init {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    suspend fun updateConfig(baseUrl: String, apiKey: String, model: String = "llama2", context: Context? = null) {
        this.baseUrl = baseUrl.trimEnd('/')
        this.apiKey = apiKey
        this.currentModel = model
        if (context != null) {
            this.localEngine = CactusInferenceEngine(context)
        }
    }

    suspend fun analyzeFood(imageFile: File, localModelPath: String? = null): CalorieResponse = withContext(Dispatchers.IO) {
        if (!localModelPath.isNullOrEmpty()) {
            localEngine?.analyzeImage(imageFile, localModelPath) 
                ?: throw Exception("Local engine not initialized")
        } else {
            val imageBytes = imageFile.readBytes()
            val base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP)

            val mimeType = detectMimeType(imageFile.name)
            val requestBody = buildJsonBody(base64Image, mimeType)
            val request = Request.Builder()
                .url("$baseUrl/chat/completions")
                .header("Content-Type", "application/json")
                .post(requestBody.toRequestBody(jsonMedia))
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: throw Exception("Empty response from API")

            if (!response.isSuccessful) {
                throw Exception("API error ${response.code}: ${body.take(200)}")
            }

            parseResponse(body)
        }
    }

    private fun detectMimeType(fileName: String): String {
        val name = fileName.lowercase()
        return when {
            name.endsWith(".png") -> "image/png"
            name.endsWith(".webp") -> "image/webp"
            name.endsWith(".gif") -> "image/gif"
            name.endsWith(".heic") || name.endsWith(".heif") -> "image/heic"
            else -> "image/jpeg"
        }
    }

    private fun buildJsonBody(base64Image: String, mimeType: String): String {
        return gson.toJson(mapOf(
            "model" to currentModel,
            "messages" to listOf(
                mapOf("role" to "system", "content" to systemPrompt),
                mapOf("role" to "user", "content" to listOf(
                    mapOf("type" to "text", "text" to "Analyze this food image and estimate its nutritional content."),
                    mapOf("type" to "image_url", "image_url" to mapOf(
                        "url" to "data:$mimeType;base64,$base64Image"
                    ))
                ))
            ),
            "max_tokens" to 500,
            "temperature" to 0.3,
        ))
    }

    private fun parseResponse(json: String): CalorieResponse {
        try {
            val root = gson.fromJson(json, Map::class.java)
            val choices = root["choices"] as? List<*>
            val message = choices?.firstOrNull()
                ?.let { it as? Map<*, *> }
                ?.let { it["message"] as? Map<*, *> }
            val content = message?.let { message["content"] as? String } ?: throw Exception("No content in response")

            val cleaned = content.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
            return gson.fromJson(cleaned, CalorieResponse::class.java)
        } catch (e: Exception) {
            return CalorieResponse(
                food_name = "Parse Error",
                calories = 0.0,
                breakdown = "Could not parse response: ${json.take(300)}"
            )
        }
    }
}
