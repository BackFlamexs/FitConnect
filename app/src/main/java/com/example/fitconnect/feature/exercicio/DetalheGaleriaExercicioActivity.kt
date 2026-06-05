package com.example.fitconnect.feature.exercicio

import com.example.fitconnect.R
import com.example.fitconnect.data.model.*
import com.example.fitconnect.core.network.RetrofitClient

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

class DetalheGaleriaExercicioActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalhe_galeria_exercicio)

        val nome = intent.getStringExtra("EXERCICIO_NOME") ?: ""
        val categoria = intent.getStringExtra("EXERCICIO_CATEGORIA") ?: ""
        val dificuldade = intent.getStringExtra("EXERCICIO_DIFICULDADE") ?: ""
        val equipamento = intent.getStringExtra("EXERCICIO_EQUIPAMENTO") ?: ""
        val gifUrl = intent.getStringExtra("EXERCICIO_GIF_URL") ?: ""
        val instrucoes = intent.getStringExtra("EXERCICIO_INSTRUCOES") ?: ""

        findViewById<ImageView>(R.id.iv_voltar_detalhe_galeria).setOnClickListener { finish() }

        findViewById<TextView>(R.id.tv_nome_detalhe).text = nome
        findViewById<TextView>(R.id.tv_categoria_detalhe).text = categoria
        findViewById<TextView>(R.id.tv_dificuldade_detalhe).text = dificuldade
        findViewById<TextView>(R.id.tv_equipamento_detalhe).text = equipamento

        val instrucoesTxt = instrucoes.ifEmpty { "Sem instruções disponíveis." }
        findViewById<TextView>(R.id.tv_instrucoes_detalhe).text = instrucoesTxt

        val ivGif = findViewById<ImageView>(R.id.iv_gif_detalhe)
        val layoutSemGif = findViewById<LinearLayout>(R.id.layout_sem_gif)

        if (gifUrl.isNotEmpty()) {
            layoutSemGif.visibility = View.GONE
            ivGif.visibility = View.VISIBLE
            Glide.with(this)
                .load(gifUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.ic_fitness_topic)
                .error(R.drawable.ic_fitness_topic)
                .into(ivGif)
        } else {
            ivGif.visibility = View.GONE
            layoutSemGif.visibility = View.VISIBLE
        }
    }
}