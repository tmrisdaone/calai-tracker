package com.calai.tracker.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.calai.tracker.data.CameraHelper
import com.calai.tracker.data.api.CalorieApi
import com.calai.tracker.data.model.CalorieResponse
import com.calai.tracker.ui.components.CalorieResultCard
import com.calai.tracker.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    api: CalorieApi,
    onOpenSettings: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }

    var isAnalyzing by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<CalorieResponse?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var previewReady by remember { mutableStateOf(false) }

    val cameraHelper = remember { CameraHelper(context) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                analyzeUri(api, it, context, onStart = { isAnalyzing = true },
                    onResult = { res -> result = res; isAnalyzing = false },
                    onError = { e -> errorMessage = e; isAnalyzing = false })
            }
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Lifecycle-aware camera start
    DisposableEffect(Unit) {
        onDispose { cameraHelper.release() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CalAI", fontWeight = FontWeight.Bold, color = TextPrimary) },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, "Settings", tint = TextSecondary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GlassBg),
            )
        },
        containerColor = GlassBg,
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Camera preview area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp)
                        .clip(RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    if (hasCameraPermission) {
                        AndroidView(
                            factory = { ctx ->
                                PreviewView(ctx).also { view ->
                                    cameraHelper.startCamera(
                                        lifecycleOwner = ctx as androidx.lifecycle.LifecycleOwner,
                                        previewView = view,
                                        onReady = { previewReady = true },
                                        onError = { errorMessage = it },
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp),
                        ) {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = TextMuted,
                            )
                            Spacer(Modifier.height(12.dp))
                            Text("Camera permission needed", color = TextSecondary, textAlign = TextAlign.Center)
                            Spacer(Modifier.height(8.dp))
                            Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                                Text("Grant Permission")
                            }
                        }
                    }
                }

                // Bottom controls
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    if (isAnalyzing) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Emerald, modifier = Modifier.size(40.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("Analyzing food...", color = TextSecondary)
                        }
                    } else if (result == null) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(32.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            // Gallery
                            IconButton(
                                onClick = { galleryLauncher.launch("image/*") },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(SurfaceElevated),
                            ) {
                                Icon(
                                    Icons.Default.PhotoLibrary, "Pick from gallery",
                                    tint = TextMuted, modifier = Modifier.size(24.dp)
                                )
                            }

                            // Shutter
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(listOf(Emerald, EmeraldDark))
                                    )
                                    .clickable {
                                        cameraHelper.takePhoto(
                                            onSuccess = { file ->
                                                scope.launch {
                                                    analyzeFile(api, file, context,
                                                        onStart = { isAnalyzing = true },
                                                        onResult = { res -> result = res; isAnalyzing = false },
                                                        onError = { e -> errorMessage = e; isAnalyzing = false }
                                                    )
                                                }
                                            },
                                            onError = { errorMessage = it }
                                        )
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                )
                            }

                            // Settings shortcut
                            IconButton(
                                onClick = onOpenSettings,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(SurfaceElevated),
                            ) {
                                Icon(
                                    Icons.Default.Tune, "Settings",
                                    tint = TextMuted, modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Result overlay
            if (result != null && !isAnalyzing) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f))
                        .clickable { result = null },
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    Box(
                        modifier = Modifier
                            .padding(16.dp)
                            .clickable(enabled = false) {}
                    ) {
                        CalorieResultCard(
                            response = result!!,
                            onDismiss = { result = null },
                        )
                    }
                }
            }

            // Error snackbar
            if (errorMessage != null) {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    containerColor = CalRed.copy(alpha = 0.9f),
                ) {
                    Text(errorMessage ?: "", color = Color.White)
                }
                LaunchedEffect(errorMessage) {
                    kotlinx.coroutines.delay(3000)
                    errorMessage = null
                }
            }
        }
    }
}

private suspend fun analyzeFile(
    api: CalorieApi,
    file: File,
    context: Context,
    onStart: () -> Unit,
    onResult: (CalorieResponse) -> Unit,
    onError: (String) -> Unit,
) {
    onStart()
    try {
        val result = withContext(Dispatchers.IO) { api.analyzeFood(file) }
        onResult(result)
    } catch (e: Exception) {
        onError(e.message ?: "Unknown error")
    }
}

private suspend fun analyzeUri(
    api: CalorieApi,
    uri: Uri,
    context: Context,
    onStart: () -> Unit,
    onResult: (CalorieResponse) -> Unit,
    onError: (String) -> Unit,
) {
    onStart()
    try {
        val tempFile = File(context.cacheDir, "images/gallery_${System.currentTimeMillis()}.jpg")
        tempFile.parentFile?.mkdirs()
        context.contentResolver.openInputStream(uri)?.use { input ->
            tempFile.outputStream().use { output -> input.copyTo(output) }
        } ?: throw Exception("Cannot read image")
        val result = withContext(Dispatchers.IO) { api.analyzeFood(tempFile) }
        tempFile.delete()
        onResult(result)
    } catch (e: Exception) {
        onError(e.message ?: "Unknown error")
    }
}
