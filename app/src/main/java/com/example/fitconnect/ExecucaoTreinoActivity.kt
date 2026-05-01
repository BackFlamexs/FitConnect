package com.example.fitconnect

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ExecucaoTreinoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_execucao_treino)

        val btnVoltar = findViewById<ImageView>(R.id.iv_voltar_execucao)
        val tvNome = findViewById<TextView>(R.id.tv_nome_execucao)
        val btnFinalizar = findViewById<Button>(R.id.btn_finalizar_treino)

        val nomeTreino = intent.getStringExtra("NOME_TREINO") ?: "Treino de Peito e Tríceps"
        tvNome.text = nomeTreino

        btnVoltar.setOnClickListener { finish() }

        btnFinalizar.setOnClickListener {
            Toast.makeText(this, "Treino Finalizado! Bom trabalho!", Toast.LENGTH_LONG).show()
            finish()
        }
    }
}
