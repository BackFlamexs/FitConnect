package com.example.fitconnect

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

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
        val btnSair = findViewById<Button>(R.id.btn_sair_menu)
        val tvNomeUsuario = findViewById<TextView>(R.id.tv_nome_usuario_menu)
        val vFecharMenu = findViewById<android.view.View>(R.id.v_fechar_menu)

        val nomeUsuario = intent.getStringExtra("NOME_USUARIO") ?: Sessao.obterNome(this)
        tvNomeUsuario.text = nomeUsuario

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
            Toast.makeText(this, "Meus Arquivos — em breve", Toast.LENGTH_SHORT).show()
        }
        llFeedbacks.setOnClickListener {
            Toast.makeText(this, "Feedbacks — em breve", Toast.LENGTH_SHORT).show()
        }
        llGaleria.setOnClickListener {
            Toast.makeText(this, "Galeria de Treinos — em breve", Toast.LENGTH_SHORT).show()
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
