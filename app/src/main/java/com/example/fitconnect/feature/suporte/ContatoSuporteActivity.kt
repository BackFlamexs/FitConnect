package com.example.fitconnect.feature.suporte

import com.example.fitconnect.R
import com.example.fitconnect.data.model.*
import com.example.fitconnect.core.network.RetrofitClient

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class ContatoSuporteActivity : AppCompatActivity() {

    private val maxChars = 300

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contato_suporte)

        val ivVoltar = findViewById<ImageView>(R.id.iv_voltar_contato)
        val etEmail = findViewById<EditText>(R.id.et_email_contato)
        val etMensagem = findViewById<EditText>(R.id.et_mensagem_contato)
        val tvContador = findViewById<TextView>(R.id.tv_contador_contato)
        val btnEnviar = findViewById<LinearLayout>(R.id.btn_enviar_contato)

        ivVoltar.setOnClickListener { finish() }

        etMensagem.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val len = s?.length ?: 0
                tvContador.text = "$len/$maxChars"
                if (len > maxChars) {
                    etMensagem.setText(s?.substring(0, maxChars))
                    etMensagem.setSelection(maxChars)
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        btnEnviar.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val mensagem = etMensagem.text.toString().trim()

            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.error = "Informe um e-mail válido (ex: nome@email.com)"
                etEmail.requestFocus()
                return@setOnClickListener
            }
            if (mensagem.isEmpty()) {
                etMensagem.error = "Digite sua mensagem"
                etMensagem.requestFocus()
                return@setOnClickListener
            }

            AlertDialog.Builder(this)
                .setTitle("E-mail enviado!")
                .setMessage("Sua mensagem foi enviada com sucesso. Nossa equipe responderá em breve.")
                .setPositiveButton("OK") { _, _ -> finish() }
                .setCancelable(false)
                .show()
        }
    }
}