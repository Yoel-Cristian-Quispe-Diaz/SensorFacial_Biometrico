package com.example.sensorfacial_biometrico;

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import java.util.concurrent.Executor

class MainActivity : AppCompatActivity() {

    private lateinit var tvTitle: TextView
    private lateinit var btnAuthenticate: Button
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Conectar vistas usando findViewById
        tvTitle = findViewById(R.id.tvTitle)
        btnAuthenticate = findViewById(R.id.btnAuthenticate)

        // Inicializar executor
        executor = ContextCompat.getMainExecutor(this)

        // Configurar BiometricPrompt
        setupBiometricPrompt()

        // Verificar disponibilidad biométrica
        checkBiometricAvailability()

        // Configurar listener del botón
        btnAuthenticate.setOnClickListener {
            authenticateUser()
        }
    }

    private fun setupBiometricPrompt() {
        biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                showToast("Error de autenticación: $errString")
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                // Cambiar el texto cuando la autenticación es exitosa
                tvTitle.text = "¡Acceso concedido!"
                tvTitle.setTextColor(ContextCompat.getColor(this@MainActivity, android.R.color.holo_green_dark))
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                showToast("Huella no reconocida")
            }
        })

        // Configurar información del prompt
        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Autenticación biométrica")
            .setSubtitle("Usa tu huella digital para autenticarte")
            .setDescription("Coloca tu dedo en el sensor de huellas dactilares")
            .setNegativeButtonText("Cancelar")
            .build()
    }

    private fun checkBiometricAvailability() {
        val biometricManager = BiometricManager.from(this)

        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                // El dispositivo puede usar autenticación biométrica
                btnAuthenticate.isEnabled = true
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                showToast("El dispositivo no tiene sensor biométrico")
                btnAuthenticate.isEnabled = false
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                showToast("El sensor biométrico no está disponible")
                btnAuthenticate.isEnabled = false
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                showToast("No hay huellas dactilares registradas")
                btnAuthenticate.isEnabled = false
            }
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> {
                showToast("Se requiere actualización de seguridad")
                btnAuthenticate.isEnabled = false
            }
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> {
                showToast("Autenticación biométrica no soportada")
                btnAuthenticate.isEnabled = false
            }
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> {
                showToast("Estado biométrico desconocido")
                btnAuthenticate.isEnabled = false
            }
        }
    }

    private fun authenticateUser() {
        // Mostrar el prompt biométrico
        biometricPrompt.authenticate(promptInfo)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}