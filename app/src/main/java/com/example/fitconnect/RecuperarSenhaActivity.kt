package com.example.fitconnect

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RecuperarSenhaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recuperar_senha)

        val campoEmail = findViewById<EditText>(R.id.et_email_recuperar)
        val botaoRecuperar = findViewById<Button>(R.id.btn_recuperar_senha)
        val textoVoltar = findViewById<TextView>(R.id.tv_voltar_login)

        // Voltar para a pagina principal
        textoVoltar.setOnClickListener {
            finish()
        }

        // Lógica do botão Recuperar
        botaoRecuperar.setOnClickListener {
            val emailDigitado = campoEmail.text.toString().trim()

            if (emailDigitado.isEmpty()) {
                Toast.makeText(this, "Por favor, digite seu e-mail.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Instruções enviadas para $emailDigitado!", Toast.LENGTH_LONG).show()
                finish() // Finge que enviou e volta para o Login
            }
        }
    }
}