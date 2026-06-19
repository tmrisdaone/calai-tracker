package com.calai.tracker.data.inference

import com.calai.tracker.data.model.CalorieResponse
import java.io.File

interface LocalInferenceEngine {
    suspend fun analyzeImage(imageFile: File, modelPath: String): CalorieResponse
}
