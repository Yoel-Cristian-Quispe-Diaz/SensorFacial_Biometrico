package com.example.sensorfacial_biometrico;

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.concurrent.Executor

class MainActivity : AppCompatActivity() {

    private lateinit var tvTitle: TextView
    private lateinit var btnFingerprint: Button
    private lateinit var btnFace: Button
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var fingerprintPromptInfo: BiometricPrompt.PromptInfo
    private var currentAuthMethod = ""

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 100
        private const val FACE_AUTH_REQUEST_CODE = 200
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Conectar vistas usando findViewById
        tvTitle = findViewById(R.id.tvTitle)
        btnFingerprint = findViewById(R.id.btnAuthenticate)
        btnFace = findViewById(R.id.btnFace)

        // Inicializar executor
        executor = ContextCompat.getMainExecutor(this)

        // Configurar BiometricPrompt solo para huella
        setupBiometricPrompt()

        // Verificar disponibilidad
        checkAvailability()

        // Configurar listeners de los botones
        btnFingerprint.setOnClickListener {
            currentAuthMethod = "Huella dactilar"
            authenticateWithFingerprint()
        }

        btnFace.setOnClickListener {
            currentAuthMethod = "Reconocimiento facial"
            authenticateWithFace()
        }
    }

    private fun setupBiometricPrompt() {
        biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                if (errorCode != BiometricPrompt.ERROR_USER_CANCELED && errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                    showToast("Error de autenticación: $errString")
                }
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                navigateToWelcomeScreen()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                showToast("Huella no reconocida")
            }
        })

        // Configurar información del prompt para huella dactilar
        fingerprintPromptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Autenticación con huella")
            .setSubtitle("Usa tu huella digital para autenticarte")
            .setDescription("Coloca tu dedo en el sensor de huellas dactilares")
            .setNegativeButtonText("Cancelar")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK)
            .build()
    }

    private fun checkAvailability() {
        val biometricManager = BiometricManager.from(this)

        // Verificar huella dactilar
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                btnFingerprint.isEnabled = true
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                btnFingerprint.isEnabled = false
                btnFingerprint.text = "Sin sensor de huella"
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                btnFingerprint.isEnabled = false
                btnFingerprint.text = "Sin huellas registradas"
            }
            else -> {
                btnFingerprint.isEnabled = false
                btnFingerprint.text = "Huella no disponible"
            }
        }

        // Para reconocimiento facial, verificar permisos de cámara
        if (checkCameraPermission()) {
            btnFace.isEnabled = true
            btnFace.text = "Autenticar con Rostro"
        } else {
            btnFace.isEnabled = true
            btnFace.text = "Permitir cámara para rostro"
        }
    }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST_CODE
        )
    }

    private fun authenticateWithFingerprint() {
        biometricPrompt.authenticate(fingerprintPromptInfo)
    }

    private fun authenticateWithFace() {
        if (!checkCameraPermission()) {
            requestCameraPermission()
            return
        }

        // Abrir actividad de reconocimiento facial personalizada
        val intent = Intent(this, FaceAuthActivity::class.java)
        startActivityForResult(intent, FACE_AUTH_REQUEST_CODE)
    }

    private fun navigateToWelcomeScreen() {
        val intent = Intent(this, WelcomeActivity::class.java)
        intent.putExtra("AUTH_METHOD", currentAuthMethod)
        startActivity(intent)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            CAMERA_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    btnFace.text = "Autenticar con Rostro"
                    showToast("Permiso de cámara concedido")
                } else {
                    showToast("Se necesita permiso de cámara para reconocimiento facial")
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            FACE_AUTH_REQUEST_CODE -> {
                when (resultCode) {
                    RESULT_OK -> {
                        // Autenticación facial exitosa
                        currentAuthMethod = "Reconocimiento facial"
                        navigateToWelcomeScreen()
                    }
                    RESULT_CANCELED -> {
                        showToast("Reconocimiento facial cancelado")
                    }
                    else -> {
                        showToast("Rostro no reconocido")
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        tvTitle.text = "Autenticación biométrica"
        tvTitle.setTextColor(ContextCompat.getColor(this, android.R.color.holo_blue_bright))
    }
}