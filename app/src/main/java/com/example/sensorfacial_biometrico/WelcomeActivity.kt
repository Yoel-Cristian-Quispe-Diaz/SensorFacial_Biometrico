package com.example.sensorfacial_biometrico
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class WelcomeActivity : AppCompatActivity() {

    private lateinit var tvAuthMethod: TextView
    private lateinit var btnLogout: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        // Conectar vistas
        tvAuthMethod = findViewById(R.id.tvAuthMethod)
        btnLogout = findViewById(R.id.btnLogout)

        // Obtener el método de autenticación del Intent
        val authMethod = intent.getStringExtra("AUTH_METHOD") ?: "Desconocido"
        tvAuthMethod.text = "Método: $authMethod"

        // Configurar botón de cerrar sesión
        btnLogout.setOnClickListener {
            // Volver a la pantalla principal
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }

    // Bloquear el botón de retroceso para mayor seguridad
    override fun onBackPressed() {
        // No hacer nada o mostrar un diálogo de confirmación
        // super.onBackPressed()
    }
}