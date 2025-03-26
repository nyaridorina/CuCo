package com.example.cuco

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import com.example.cuco.ui.CameraPreview

@Composable
fun CameraOcrScreen() {
    // This holds the recognized & converted results
    var detectedPrices by remember { mutableStateOf<List<String>>(emptyList()) }

    Column {
        // 1) Camera preview which calls back with recognized prices
        CameraPreview(
            onPricesDetected = { newPrices ->
                // Each frame may bring a new list of recognized items
                detectedPrices = newPrices
            }
        )

        // 2) List them
        LazyColumn {
            items(detectedPrices) { priceString ->
                Text(text = priceString)
            }
        }
    }
}
