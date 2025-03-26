package com.example.currencylensapp.ui

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
import com.example.currencylensapp.util.CurrencyParser // ha a parser is ott van
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

            // 1) Preview (látható kamerakép)
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            // 2) ImageAnalysis (frame-ek elemzése)
            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            // ML Kit text recognizer
            val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

            // Analyzer callback
            imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(ctx)) { imageProxy ->
                val mediaImage = imageProxy.image
                if (mediaImage != null) {
                    val rotation = imageProxy.imageInfo.rotationDegrees
                    val inputImage = InputImage.fromMediaImage(mediaImage, rotation)

                    textRecognizer.process(inputImage)
                        .addOnSuccessListener { visionText ->
                            val fullText = visionText.text
                            // Kinyerjük a talált árakat
                            val prices = CurrencyParser.findPrices(fullText)
                            // Visszaadjuk a felső rétegnek
                            onPricesDetected(prices)
                        }
                        .addOnFailureListener { e ->
                            Log.e("CameraPreview", "OCR error", e)
                        }
                        .addOnCompleteListener {
                            // Fontos: zárjuk le az ImageProxy-t
                            imageProxy.close()
                        }
                } else {
                    imageProxy.close()
                }
            }

            // 3) Kiválasztjuk a hátsó kamerát
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Bind
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
