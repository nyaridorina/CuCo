package com.example.currencylensapp.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import com.example.currencylensapp.ui.CameraPreview

@Composable
fun CameraOcrScreen() {
    // Ebbe tároljuk a legutóbb felismert árakat
    var detectedPrices by remember { mutableStateOf<List<String>>(emptyList()) }

    Column {
        // 1) Kamera preview (ami a valós idejű OCR eredményeket callbackben adja vissza)
        CameraPreview(
            onPricesDetected = { newPrices ->
                detectedPrices = newPrices
            }
        )

        // 2) Listázzuk a talált árakat
        LazyColumn {
            items(detectedPrices) { price ->
                Text(text = price)
            }
        }
    }
}
