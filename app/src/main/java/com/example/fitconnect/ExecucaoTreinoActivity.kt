package com.example.fitconnect

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ExecucaoTreinoActivity : AppCompatActivity() {

    private var secondsElapsed = 0
    private var timerRunning = true
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var tvTimer: TextView

    private val timerRunnable = object : Runnable {
        override fun run() {
            if (timerRunning) {
                secondsElapsed++
                val m = secondsElapsed / 60
                val s = secondsElapsed % 60
                tvTimer.text = String.format("%02d:%02d", m, s)
                handler.postDelayed(this, 1000)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_execucao_treino)

        val btnVoltar = findViewById<ImageView>(R.id.iv_voltar_execucao)
        val tvNome = findViewById<TextView>(R.id.tv_nome_execucao)
        val btnFinalizar = findViewById<Button>(R.id.btn_finalizar_treino)
        tvTimer = findViewById(R.id.tv_timer_execucao)

        val nomeTreino = intent.getStringExtra("NOME_TREINO") ?: "Treino"
        tvNome.text = nomeTreino

        handler.post(timerRunnable)

        btnVoltar.setOnClickListener {
            timerRunning = false
            finish()
        }

        btnFinalizar.setOnClickListener {
            timerRunning = false
            val duracaoMin = secondsElapsed / 60
            val intent = Intent(this, FeedbackPosTreinoActivity::class.java).apply {
                putExtra("NOME_TREINO", nomeTreino)
                putExtra("DURACAO_MIN", duracaoMin)
            }
            startActivity(intent)
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timerRunning = false
        handler.removeCallbacks(timerRunnable)
    }
}