package com.calai.tracker.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors

class CameraHelper(private val context: Context) {

    private val executor = Executors.newSingleThreadExecutor()
    private var imageCapture: ImageCapture? = null

    fun startCamera(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        onReady: () -> Unit,
        onError: (String) -> Unit,
    ) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build()
                preview.setSurfaceProvider(previewView.surfaceProvider)

                imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                    .build()

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
                onReady()
            } catch (e: Exception) {
                onError("Camera error: ${e.message}")
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun takePhoto(
        onSuccess: (File) -> Unit,
        onError: (String) -> Unit,
    ) {
        val capture = imageCapture ?: run {
            onError("Camera not initialized")
            return
        }

        val photoFile = File(
            context.cacheDir,
            "images/${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.jpg"
        )
        photoFile.parentFile?.mkdirs()

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        capture.takePicture(
            outputOptions,
            executor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    try {
                        val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
                        val compressed = File(photoFile.parent, "compressed_${photoFile.name}")
                        FileOutputStream(compressed).use { out ->
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
                        }
                        photoFile.delete()
                        onSuccess(compressed)
                    } catch (e: Exception) {
                        onSuccess(photoFile)
                    }
                }
                override fun onError(exception: ImageCaptureException) {
                    onError("Capture failed: ${exception.message}")
                }
            }
        )
    }

    fun release() {
        executor.shutdown()
    }
}