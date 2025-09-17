package com.example.sensorfacial_biometrico

import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.random.Random

class FaceAuthActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var tvInstructions: TextView
    private lateinit var tvStatus: TextView
    private lateinit var btnCancel: Button
    private lateinit var btnCapture: Button

    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService
    private var isProcessing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_face_auth)

        // Conectar vistas
        previewView = findViewById(R.id.previewView)
        tvInstructions = findViewById(R.id.tvInstructions)
        tvStatus = findViewById(R.id.tvStatus)
        btnCancel = findViewById(R.id.btnCancel)
        btnCapture = findViewById(R.id.btnCapture)

        // Configurar botones
        btnCancel.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }

        btnCapture.setOnClickListener {
            if (!isProcessing) {
                simulateFaceRecognition()
            }
        }

        // Inicializar executor para cámara
        cameraExecutor = Executors.newSingleThreadExecutor()

        // Verificar permisos e iniciar cámara
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            Toast.makeText(this, "Permisos de cámara requeridos", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            // ImageCapture
            imageCapture = ImageCapture.Builder().build()

            // Seleccionar cámara frontal
            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                // Desvincular casos de uso anteriores
                cameraProvider.unbindAll()

                // Vincular casos de uso a la cámara
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )

            } catch (exc: Exception) {
                Log.e("FaceAuth", "Error al iniciar cámara", exc)
                Toast.makeText(this, "Error al iniciar cámara", Toast.LENGTH_SHORT).show()
                finish()
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun simulateFaceRecognition() {
        if (isProcessing) return

        isProcessing = true
        tvStatus.visibility = View.VISIBLE
        tvStatus.text = "Analizando rostro..."
        tvInstructions.text = "Procesando..."

        btnCapture.isEnabled = false
        btnCancel.isEnabled = false

        // Simular procesamiento de reconocimiento facial
        Handler(Looper.getMainLooper()).postDelayed({

            // Simular resultado (80% de éxito para demo)
            val success = Random.nextFloat() < 0.8f

            if (success) {
                tvStatus.text = "¡Rostro reconocido!"
                tvStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark))

                Handler(Looper.getMainLooper()).postDelayed({
                    setResult(RESULT_OK)
                    finish()
                }, 1000)

            } else {
                tvStatus.text = "Rostro no reconocido"
                tvStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
                tvInstructions.text = "Intenta de nuevo"

                Handler(Looper.getMainLooper()).postDelayed({
                    resetUI()
                }, 2000)
            }

        }, 2000) // Simular 2 segundos de procesamiento
    }

    private fun resetUI() {
        isProcessing = false
        tvStatus.visibility = View.GONE
        tvInstructions.text = "Posiciona tu rostro dentro del marco"
        btnCapture.isEnabled = true
        btnCancel.isEnabled = true
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private val REQUIRED_PERMISSIONS = mutableListOf(
            android.Manifest.permission.CAMERA
        ).toTypedArray()
    }
}