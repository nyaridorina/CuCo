package com.example.cuco.ui

import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.cuco.util.CurrencyParser
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

@Composable
fun CameraPreview(
    onPricesDetected: (List<String>) -> Unit
) {
    val context = LocalContext.current

    AndroidView(factory = { ctx ->
        val previewView = PreviewView(ctx)

        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

            imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(ctx)) { imageProxy ->
                val mediaImage = imageProxy.image
                if (mediaImage != null) {
                    val rotation = imageProxy.imageInfo.rotationDegrees
                    val inputImage = InputImage.fromMediaImage(mediaImage, rotation)
                    
                    textRecognizer.process(inputImage)
                        .addOnSuccessListener { visionText ->
                            val recognizedText = visionText.text
                            // Convert recognized text with the parser
                            // (which internally calls the exchange rate if needed)
                            CurrencyParser.parsePrices(recognizedText) { prices ->
                                // This is an async callback from parsePrices
                                onPricesDetected(prices)
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e("CameraPreview", "OCR error", e)
                        }
                        .addOnCompleteListener {
                            imageProxy.close()
                        }
                } else {
                    imageProxy.close()
                }
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    (ctx as androidx.activity.ComponentActivity),
                    cameraSelector,
                    preview,
                    imageAnalysis
                )
            } catch (exc: Exception) {
                Log.e("CameraPreview", "Camera bind failed", exc)
            }

        }, ContextCompat.getMainExecutor(ctx))

        previewView
    })
}
