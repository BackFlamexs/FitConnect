package com.example.fitconnect.feature.suporte

import com.example.fitconnect.R
import com.example.fitconnect.data.model.*
import com.example.fitconnect.core.network.RetrofitClient
import com.example.fitconnect.feature.home.HomeActivity
import com.example.fitconnect.feature.home.MenuActivity
import com.example.fitconnect.feature.treino.TreinosActivity
import com.example.fitconnect.feature.exercicio.GaleriaExerciciosActivity
import com.example.fitconnect.feature.perfil.EditarPerfilActivity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class AjudaActivity : AppCompatActivity() {

    private var faqsExtrasVisiveis = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ajuda)

        val ivVoltar = findViewById<ImageView>(R.id.iv_voltar_ajuda)
        val btnEmail = findViewById<LinearLayout>(R.id.btn_enviar_email)
        val tvVerTudo = findViewById<TextView>(R.id.tv_ver_tudo)
        val grupoExtras = findViewById<LinearLayout>(R.id.grupo_faqs_extras)
        val cardConta = findViewById<LinearLayout>(R.id.card_topico_conta)
        val cardTreinos = findViewById<LinearLayout>(R.id.card_topico_treinos)

        val navInicio = findViewById<LinearLayout>(R.id.nav_inicio)
        val navGaleria = findViewById<LinearLayout>(R.id.nav_galeria)
        val navAjuda = findViewById<LinearLayout>(R.id.nav_ajuda)
        val navMenu = findViewById<LinearLayout>(R.id.nav_menu)

        ivVoltar.setOnClickListener { finish() }

        cardConta.setOnClickListener { startActivity(Intent(this, EditarPerfilActivity::class.java)) }
        cardTreinos.setOnClickListener { startActivity(Intent(this, TreinosActivity::class.java)) }

        btnEmail.setOnClickListener {
            startActivity(Intent(this, ContatoSuporteActivity::class.java))
        }

        // Ver tudo — mostra/esconde FAQs extras
        tvVerTudo.setOnClickListener {
            faqsExtrasVisiveis = !faqsExtrasVisiveis
            grupoExtras.visibility = if (faqsExtrasVisiveis) View.VISIBLE else View.GONE
            tvVerTudo.text = if (faqsExtrasVisiveis) "Ver menos" else "Ver tudo"
        }

        // FAQs expand/collapse
        configurarFaq(R.id.faq_item_1, R.id.tv_resposta_1, R.id.ic_faq_1)
        configurarFaq(R.id.faq_item_2, R.id.tv_resposta_2, R.id.ic_faq_2)
        configurarFaq(R.id.faq_item_3, R.id.tv_resposta_3, R.id.ic_faq_3)
        configurarFaq(R.id.faq_item_4, R.id.tv_resposta_4, R.id.ic_faq_4)
        configurarFaq(R.id.faq_item_5, R.id.tv_resposta_5, R.id.ic_faq_5)
        configurarFaq(R.id.faq_item_6, R.id.tv_resposta_6, R.id.ic_faq_6)
        configurarFaq(R.id.faq_item_7, R.id.tv_resposta_7, R.id.ic_faq_7)
        configurarFaq(R.id.faq_item_8, R.id.tv_resposta_8, R.id.ic_faq_8)

        navInicio.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
        navGaleria.setOnClickListener {
            startActivity(Intent(this, GaleriaExerciciosActivity::class.java))
        }
        navAjuda.setOnClickListener { /* já está na tela de ajuda */ }
        navMenu.setOnClickListener {
            val intent = Intent(this, MenuActivity::class.java)
            intent.putExtra("NOME_USUARIO", Sessao.obterNomeUsuario(this))
            startActivity(intent)
        }
    }

    private fun configurarFaq(itemId: Int, respostaId: Int, iconeId: Int) {
        val item = findViewById<LinearLayout>(itemId)
        val resposta = findViewById<TextView>(respostaId)
        val icone = findViewById<ImageView>(iconeId)

        item.setOnClickListener {
            val expandido = resposta.visibility == View.VISIBLE
            resposta.visibility = if (expandido) View.GONE else View.VISIBLE
            icone.rotation = if (expandido) 0f else 180f
        }
    }
}
