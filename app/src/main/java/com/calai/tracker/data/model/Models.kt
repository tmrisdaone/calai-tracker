package com.calai.tracker.data.model

data class CalorieResponse(
    val food_name: String = "Unknown Food",
    val calories: Double = 0.0,
    val protein_g: Double = 0.0,
    val carbs_g: Double = 0.0,
    val fat_g: Double = 0.0,
    val fiber_g: Double = 0.0,
    val serving_size: String = "1 serving",
    val confidence: Double = 0.0,
    val breakdown: String = "",
)

data class ApiError(
    val error: String,
    val message: String = "",
)
