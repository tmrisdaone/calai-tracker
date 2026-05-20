package com.calai.tracker.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.calai.tracker.settings.AppSettings
import com.calai.tracker.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: AppSettings,
    onBack: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val baseUrl by settings.apiBaseUrl.collectAsState(initial = AppSettings.DEFAULT_API_BASE)
    val apiKey by settings.apiKey.collectAsState(initial = "")
    val modelName by settings.modelName.collectAsState(initial = "gpt-4o-mini")
    val localModelPath by settings.localModelPath.collectAsState(initial = null)

    var editBaseUrl by remember(baseUrl) { mutableStateOf(baseUrl) }
    var editApiKey by remember(apiKey) { mutableStateOf(apiKey) }
    var editModel by remember(modelName) { mutableStateOf(modelName) }
    var showKey by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf(false) }

    val modelPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                // In a real app, we'd resolve the actual file path from the URI
                // For now, we save the URI string as the path
                settings.saveLocalModelPath(it.toString())
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold, color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = TextSecondary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GlassBg),
            )
        },
        containerColor = GlassBg,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                "API Configuration",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
            )

            Text(
                "Configure the AI endpoint that analyzes food images. " +
                        "Uses any OpenAI-compatible chat completion API.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )

            Spacer(Modifier.height(8.dp))

            // API Base URL
            OutlinedTextField(
                value = editBaseUrl,
                onValueChange = { editBaseUrl = it },
                label = { Text("API Base URL") },
                placeholder = { Text("https://api.openai.com/v1") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Emerald,
                    unfocusedBorderColor = GlassStroke,
                    focusedLabelColor = Emerald,
                    cursorColor = Emerald,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                ),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            )

            // API Key
            OutlinedTextField(
                value = editApiKey,
                onValueChange = { editApiKey = it },
                label = { Text("API Key") },
                placeholder = { Text("sk-...") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showKey = !showKey }) {
                        Icon(
                            if (showKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            "Toggle visibility",
                            tint = TextMuted,
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Emerald,
                    unfocusedBorderColor = GlassStroke,
                    focusedLabelColor = Emerald,
                    cursorColor = Emerald,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                ),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            )

            // Model Name
            OutlinedTextField(
                value = editModel,
                onValueChange = { editModel = it },
                label = { Text("Model Name") },
                placeholder = { Text("gpt-4o-mini") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Emerald,
                    unfocusedBorderColor = GlassStroke,
                    focusedLabelColor = Emerald,
                    cursorColor = Emerald,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                ),
                shape = RoundedCornerShape(12.dp),
            )

            // Local GGUF Model
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Local GGUF Model",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary
                    )
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { 
                                modelPickerLauncher.launch("*/*") 
                            },
                        color = SurfaceElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, GlassStroke)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = localModelPath?.substringAfterLast("/") ?: "No local model selected",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (localModelPath == null) TextMuted else TextSecondary,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                Icons.Default.Folder,
                                contentDescription = "Pick Model",
                                tint = Emerald,
                                modifier = Modifier.size(20.dp)
                            )
                            if (localModelPath != null) {
                                IconButton(
                                    onClick = {
                                        scope.launch {
                                            settings.saveLocalModelPath(null)
                                        }
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Cancel,
                                        contentDescription = "Clear Model",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Save button
            Button(
                onClick = {
                    scope.launch {
                        settings.saveApiBaseUrl(editBaseUrl.trim())
                        settings.saveApiKey(editApiKey.trim())
                        settings.saveModelName(editModel.trim())
                        saved = true
                        kotlinx.coroutines.delay(1500)
                        saved = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Emerald),
            ) {
                Text(
                    if (saved) "✓ Saved!" else "Save",
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 4.dp),
                )
            }

            Spacer(Modifier.height(16.dp))

            // Quick presets
            Text(
                "Quick Presets",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
            )

            PresetChip("OpenAI", "https://api.openai.com/v1") {
                editBaseUrl = it
            }
            PresetChip("OpenRouter", "https://openrouter.ai/api/v1") {
                editBaseUrl = it
            }
            PresetChip("Groq", "https://api.groq.com/openai/v1") {
                editBaseUrl = it
            }
            PresetChip("Together AI", "https://api.together.xyz/v1") {
                editBaseUrl = it
            }
        }
    }
}

@Composable
private fun PresetChip(name: String, url: String, onClick: (String) -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        shape = RoundedCornerShape(12.dp),
        color = SurfaceElevated,
        onClick = { onClick(url) },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(name, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                Text(url, style = MaterialTheme.typography.bodySmall, color = TextMuted)
            }
            Text("Tap", style = MaterialTheme.typography.labelSmall, color = Emerald)
        }
    }
}
