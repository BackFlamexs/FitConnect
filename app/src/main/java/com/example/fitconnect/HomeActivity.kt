package com.example.fitconnect

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Encontra o texto onde vamos colocar o nome
        val tvNomeUsuario = findViewById<TextView>(R.id.tv_nome_usuario_home)

        // Encontra o novo botão de Sair lá no final da tela
        val botaoSair = findViewById<Button>(R.id.btn_sair_home)

        // 1. Recebe o nome que veio da tela de Login (Intent)
        val nomeLogado = intent.getStringExtra("NOME_USUARIO") ?: "Atleta"

        // 2. Escreve "Olá, [Nome]" na tela
        tvNomeUsuario.text = "Olá, $nomeLogado"

        // 3. Lógica para deslogar
        botaoSair.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}