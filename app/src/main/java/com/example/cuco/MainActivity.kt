package com.example.cuco  // Ellenőrizd, hogy ez megegyezik az AndroidManifest.xml-ben lévő csomagnévvel!

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import androidx.camera.lifecycle.ProcessCameraProvider
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var resultTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        resultTextView = findViewById(R.id.result_text)
        cameraExecutor = Executors.newSingleThreadExecutor()

        startCamera()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val analyzer = CameraAnalyzer { detectedPrice ->
                CurrencyConverter.convertCurrency(detectedPrice) { convertedValue ->
                    runOnUiThread { resultTextView.text = "Converted: $convertedValue" }
                }
            }
            cameraProvider.bindToLifecycle(this, analyzer)
        }, cameraExecutor)
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
