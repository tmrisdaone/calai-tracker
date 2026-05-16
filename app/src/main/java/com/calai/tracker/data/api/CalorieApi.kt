package com.calai.tracker.data.api

import com.calai.tracker.data.model.CalorieResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import java.util.concurrent.TimeUnit

class CalorieApi(
    private var baseUrl: String = "https://api.openai.com/v1",
    private var apiKey: String = "",
) {
    private val client: OkHttpClient
    private val gson = Gson()
    private val jsonMedia = "application/json; charset=utf-8".toMediaType()
    private val imageMedia = "image/jpeg".toMediaType()

    // System prompt telling the AI how to respond
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
            level = HttpLoggingInterceptor.Level.BODY
        }
        client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    fun updateConfig(baseUrl: String, apiKey: String) {
        this.baseUrl = baseUrl.trimEnd('/')
        this.apiKey = apiKey
    }

    suspend fun analyzeFood(imageFile: File): CalorieResponse = withContext(Dispatchers.IO) {
        val imageBytes = imageFile.readBytes()
        val base64Image = android.util.Base64.encodeToString(imageBytes, android.util.Base64.NO_WRAP)

        val requestBody = buildJsonBody(base64Image)
        val request = Request.Builder()
            .url("$baseUrl/chat/completions")
            .header("Authorization", "Bearer $apiKey")
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

    private fun buildJsonBody(base64Image: String): String {
        return gson.toJson(mapOf(
            "model" to "gpt-4o-mini",
            "messages" to listOf(
                mapOf("role" to "system", "content" to systemPrompt),
                mapOf("role" to "user", "content" to listOf(
                    mapOf("type" to "text", "text" to "Analyze this food image and estimate its nutritional content."),
                    mapOf("type" to "image_url", "image_url" to mapOf(
                        "url" to "data:image/jpeg;base64,$base64Image"
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

            // Parse the nested JSON from the AI response
            val cleaned = content.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
            return gson.fromJson(cleaned, CalorieResponse::class.java)
        } catch (e: Exception) {
            // Return raw text in a response so user can see what the AI said
            return CalorieResponse(
                food_name = "Parse Error",
                calories = 0.0,
                breakdown = "Could not parse response: ${json.take(300)}"
            )
        }
    }
}
