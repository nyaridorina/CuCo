package com.example.cuco

import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.common.InputImage

object OCRProcessor {
    fun processImage(image: InputImage, onResult: (String) -> Unit) {
        val recognizer = TextRecognition.getClient()
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val text = visionText.text
                if (text.contains(Regex("\\d+[.,]?\\d*"))) {
                    onResult(text)
                }
            }
            .addOnFailureListener {
                onResult("Error processing image")
            }
    }
}
