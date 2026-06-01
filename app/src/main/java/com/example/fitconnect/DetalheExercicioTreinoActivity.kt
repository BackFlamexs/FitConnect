package com.example.fitconnect

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

class DetalheExercicioTreinoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalhe_exercicio_treino)

        val nome = intent.getStringExtra("EXERCICIO_NOME") ?: ""
        val seriesReps = intent.getStringExtra("EXERCICIO_SERIES_REPS") ?: ""
        val categoria = intent.getStringExtra("EXERCICIO_CATEGORIA") ?: ""
        val dificuldade = intent.getStringExtra("EXERCICIO_DIFICULDADE") ?: ""
        val equipamento = intent.getStringExtra("EXERCICIO_EQUIPAMENTO") ?: ""
        val gifUrl = intent.getStringExtra("EXERCICIO_GIF_URL") ?: ""
        val instrucoes = intent.getStringExtra("EXERCICIO_INSTRUCOES") ?: ""

        findViewById<ImageView>(R.id.iv_voltar_exercicio_treino).setOnClickListener { finish() }
        findViewById<Button>(R.id.btn_voltar_iniciar_treino).setOnClickListener { finish() }

        findViewById<TextView>(R.id.tv_nome_exercicio_treino).text = nome.ifBlank { "Exercicio" }
        findViewById<TextView>(R.id.tv_series_exercicio_treino).text = seriesReps.ifBlank { "Series nao informadas" }
        findViewById<TextView>(R.id.tv_categoria_exercicio_treino).text = categoria.ifBlank { "Categoria nao informada" }
        findViewById<TextView>(R.id.tv_dificuldade_exercicio_treino).text = dificuldade.ifBlank { "Nivel nao informado" }
        findViewById<TextView>(R.id.tv_equipamento_exercicio_treino).text = equipamento.ifBlank { "Nao informado" }
        findViewById<TextView>(R.id.tv_instrucoes_exercicio_treino).text =
            instrucoes.ifBlank { "Sem instrucoes disponiveis para este exercicio." }

        val ivGif = findViewById<ImageView>(R.id.iv_gif_exercicio_treino)
        val layoutSemGif = findViewById<LinearLayout>(R.id.layout_sem_gif_exercicio_treino)

        if (gifUrl.isNotBlank()) {
            layoutSemGif.visibility = View.GONE
            ivGif.visibility = View.VISIBLE
            Glide.with(this)
                .load(gifUrl)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .placeholder(R.drawable.ic_fitness_topic)
                .error(R.drawable.ic_fitness_topic)
                .into(ivGif)
        } else {
            ivGif.visibility = View.GONE
            layoutSemGif.visibility = View.VISIBLE
        }
    }
}
