package com.example.fitconnect.feature.home

import com.example.fitconnect.R
import com.example.fitconnect.data.model.*
import com.example.fitconnect.core.network.RetrofitClient
import com.example.fitconnect.feature.auth.MainActivity
import com.example.fitconnect.feature.treino.TreinosActivity
import com.example.fitconnect.feature.exercicio.GaleriaExerciciosActivity
import com.example.fitconnect.feature.arquivo.MeusArquivosActivity
import com.example.fitconnect.feature.feedback.HistoricoFeedbacksActivity
import com.example.fitconnect.feature.perfil.EditarPerfilActivity
import com.example.fitconnect.feature.pagamento.PagamentoProActivity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class MenuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        val btnVoltar = findViewById<ImageView>(R.id.iv_voltar_menu)
        val llEditarPerfil = findViewById<LinearLayout>(R.id.ll_editar_perfil_menu)
        val llTreinos = findViewById<LinearLayout>(R.id.ll_treinos_menu)
        val llMeusArquivos = findViewById<LinearLayout>(R.id.ll_meus_arquivos_menu)
        val llFeedbacks = findViewById<LinearLayout>(R.id.ll_feedbacks_menu)
        val llGaleria = findViewById<LinearLayout>(R.id.ll_galeria_menu)
        val llFitConnectPro = findViewById<LinearLayout>(R.id.ll_fitconnect_pro_menu)
        val btnAtualizarPro = findViewById<Button>(R.id.btn_atualizar_pro_menu)
        val btnSair = findViewById<Button>(R.id.btn_sair_menu)
        val tvNomeUsuario = findViewById<TextView>(R.id.tv_nome_usuario_menu)
        val vFecharMenu = findViewById<android.view.View>(R.id.v_fechar_menu)
        val ivFotoMenu = findViewById<ImageView>(R.id.iv_foto_menu)

        val nomeUsuario = intent.getStringExtra("NOME_USUARIO") ?: Sessao.obterNomeUsuario(this)
        tvNomeUsuario.text = nomeUsuario
        val fotoUrl = Sessao.obterFotoUrl(this)
        if (fotoUrl.isNotEmpty()) {
            Glide.with(this)
                .load(fotoUrl)
                .centerCrop()
                .placeholder(R.drawable.img_avatar)
                .error(R.drawable.img_avatar)
                .into(ivFotoMenu)
        }

        // Fechar menu clicando na área escura da direita
        vFecharMenu.setOnClickListener { finish() }

        // Voltar
        btnVoltar.setOnClickListener { finish() }

        // Editar Perfil
        llEditarPerfil.setOnClickListener {
            startActivity(Intent(this, EditarPerfilActivity::class.java))
        }

        // Treinos
        llTreinos.setOnClickListener {
            startActivity(Intent(this, TreinosActivity::class.java))
        }

        // Em desenvolvimento
        llMeusArquivos.setOnClickListener {
            startActivity(Intent(this, MeusArquivosActivity::class.java))
        }
        llFeedbacks.setOnClickListener {
            startActivity(Intent(this, HistoricoFeedbacksActivity::class.java))
        }
        llGaleria.setOnClickListener {
            startActivity(Intent(this, GaleriaExerciciosActivity::class.java))
        }
        llFitConnectPro.setOnClickListener {
            startActivity(Intent(this, PagamentoProActivity::class.java))
        }
        btnAtualizarPro.setOnClickListener {
            startActivity(Intent(this, PagamentoProActivity::class.java))
        }

        // Sair — limpa sessão e volta ao login
        btnSair.setOnClickListener {
            Sessao.limpar(this)
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}
