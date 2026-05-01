package com.example.fitconnect

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val tvNomeUsuario = findViewById<TextView>(R.id.tv_nome_usuario_home)
        val fotoPerfil = findViewById<View>(R.id.v_profile_home)

        // Cards principais
        val cardTreinos = findViewById<RelativeLayout>(R.id.card_treinos)
        val cardAjuda = findViewById<RelativeLayout>(R.id.card_ajuda)
        val cardMeusArquivos = findViewById<RelativeLayout>(R.id.card_meus_arquivos)
        val cardFeedbacks = findViewById<RelativeLayout>(R.id.card_feedbacks)
        val cardGaleria = findViewById<RelativeLayout>(R.id.card_galeria)

        // Bottom nav
        val navInicio = findViewById<LinearLayout>(R.id.nav_inicio)
        val navGaleria = findViewById<LinearLayout>(R.id.nav_galeria)
        val navAjuda = findViewById<LinearLayout>(R.id.nav_ajuda)
        val navMenu = findViewById<LinearLayout>(R.id.nav_menu)

        val nomeLogado = intent.getStringExtra("NOME_USUARIO") ?: Sessao.obterNome(this)
        tvNomeUsuario.text = "Olá, $nomeLogado"

        // Foto de perfil / bottom nav MENU → abre menu lateral
        fotoPerfil.setOnClickListener { abrirMenu(nomeLogado) }
        navMenu.setOnClickListener { abrirMenu(nomeLogado) }

        // Bottom nav INÍCIO → já está na home, não faz nada
        navInicio.setOnClickListener { /* já está na tela inicial */ }

        // Bottom nav GALERIA e AJUDA → em breve
        navGaleria.setOnClickListener {
            Toast.makeText(this, "Galeria — em breve", Toast.LENGTH_SHORT).show()
        }
        navAjuda.setOnClickListener {
            Toast.makeText(this, "Ajuda — em breve", Toast.LENGTH_SHORT).show()
        }

        // Card Treinos → abre lista de treinos
        cardTreinos.setOnClickListener {
            startActivity(Intent(this, TreinosActivity::class.java))
        }

        // Cards em desenvolvimento
        cardAjuda.setOnClickListener {
            Toast.makeText(this, "Ajuda — em breve", Toast.LENGTH_SHORT).show()
        }
        cardMeusArquivos.setOnClickListener {
            Toast.makeText(this, "Meus Arquivos — em breve", Toast.LENGTH_SHORT).show()
        }
        cardFeedbacks.setOnClickListener {
            Toast.makeText(this, "Feedbacks — em breve", Toast.LENGTH_SHORT).show()
        }
        cardGaleria.setOnClickListener {
            Toast.makeText(this, "Galeria de Treinos — em breve", Toast.LENGTH_SHORT).show()
        }
    }

    private fun abrirMenu(nomeUsuario: String) {
        val intent = Intent(this, MenuActivity::class.java)
        intent.putExtra("NOME_USUARIO", nomeUsuario)
        startActivity(intent)
    }
}
