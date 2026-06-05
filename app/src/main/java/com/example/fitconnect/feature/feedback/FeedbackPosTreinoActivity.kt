package com.example.fitconnect.feature.feedback

import com.example.fitconnect.R
import com.example.fitconnect.data.model.*
import com.example.fitconnect.core.network.RetrofitClient
import com.example.fitconnect.feature.treino.TreinosActivity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FeedbackPosTreinoActivity : AppCompatActivity() {

    private var intensidadeSelecionada = "Moderado"
    private val maxChars = 200

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback_pos_treino)

        val nomeTreino = intent.getStringExtra("NOME_TREINO") ?: "Treino"
        val duracaoSeg = intent.getIntExtra("DURACAO_SEG", 0)
        val duracaoMin = ((duracaoSeg + 59) / 60).coerceAtLeast(1)

        val ivVoltar = findViewById<ImageView>(R.id.iv_voltar_feedback)
        val tvCancelar = findViewById<TextView>(R.id.tv_cancelar)
        val tvNome = findViewById<TextView>(R.id.tv_nome_treino_feedback)
        val etDuracao = findViewById<EditText>(R.id.et_duracao)
        val etObs = findViewById<EditText>(R.id.et_observacoes)
        val tvContador = findViewById<TextView>(R.id.tv_contador)
        val btnEnviar = findViewById<LinearLayout>(R.id.btn_enviar_feedback)

        val cardLeve = findViewById<LinearLayout>(R.id.card_leve)
        val cardModerado = findViewById<LinearLayout>(R.id.card_moderado)
        val cardIntenso = findViewById<LinearLayout>(R.id.card_intenso)
        val tvLeve = findViewById<TextView>(R.id.tv_leve)
        val tvModerado = findViewById<TextView>(R.id.tv_moderado)
        val tvIntenso = findViewById<TextView>(R.id.tv_intenso)
        val icLeve = findViewById<ImageView>(R.id.ic_leve)
        val icModerado = findViewById<ImageView>(R.id.ic_moderado)
        val icIntenso = findViewById<ImageView>(R.id.ic_intenso)

        tvNome.text = "Treino \"$nomeTreino\" finalizado em ${formatarDuracao(duracaoSeg)}. Registre a intensidade para acompanhar sua próxima sessão."
        etDuracao.setText(duracaoMin.toString())
        etDuracao.isEnabled = false

        // Selecionar Moderado por padrão
        selecionarIntensidade(cardModerado, tvModerado, icModerado,
            listOf(cardLeve, cardIntenso), listOf(tvLeve, tvIntenso), listOf(icLeve, icIntenso))

        cardLeve.setOnClickListener {
            intensidadeSelecionada = "Leve"
            selecionarIntensidade(cardLeve, tvLeve, icLeve,
                listOf(cardModerado, cardIntenso), listOf(tvModerado, tvIntenso), listOf(icModerado, icIntenso))
        }
        cardModerado.setOnClickListener {
            intensidadeSelecionada = "Moderado"
            selecionarIntensidade(cardModerado, tvModerado, icModerado,
                listOf(cardLeve, cardIntenso), listOf(tvLeve, tvIntenso), listOf(icLeve, icIntenso))
        }
        cardIntenso.setOnClickListener {
            intensidadeSelecionada = "Intenso"
            selecionarIntensidade(cardIntenso, tvIntenso, icIntenso,
                listOf(cardLeve, cardModerado), listOf(tvLeve, tvModerado), listOf(icLeve, icModerado))
        }

        // Contador de caracteres
        etObs.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val restantes = maxChars - (s?.length ?: 0)
                tvContador.text = restantes.toString()
            }
        })

        ivVoltar.setOnClickListener { finish() }
        tvCancelar.setOnClickListener { finish() }

        btnEnviar.setOnClickListener { enviarFeedback(nomeTreino, etDuracao, etObs) }
    }

    private fun selecionarIntensidade(
        cardSel: LinearLayout, tvSel: TextView, icSel: ImageView,
        cardsOff: List<LinearLayout>, tvsOff: List<TextView>, icsOff: List<ImageView>
    ) {
        cardSel.setBackgroundResource(R.drawable.bg_intensidade_selecionado)
        tvSel.setTextColor(0xFF4CAF50.toInt())
        icSel.setColorFilter(0xFF4CAF50.toInt())

        cardsOff.forEach { it.setBackgroundResource(R.drawable.bg_topic_card) }
        tvsOff.forEach { it.setTextColor(0xFFA0A0A0.toInt()) }
        icsOff.forEach { it.clearColorFilter() }
    }

    private fun formatarDuracao(segundos: Int): String {
        val total = segundos.coerceAtLeast(0)
        val minutos = total / 60
        val seg = total % 60
        return if (minutos > 0) "${minutos}min ${seg}s" else "${seg}s"
    }

    private fun enviarFeedback(nomeTreino: String, etDuracao: EditText, etObs: EditText) {
        val usuarioId = Sessao.obterUsuarioId(this)
        if (usuarioId == 0) {
            Toast.makeText(this, "Sessão expirada. Faça login novamente.", Toast.LENGTH_SHORT).show()
            return
        }

        val duracao = etDuracao.text.toString().toIntOrNull() ?: 0
        val obs = etObs.text.toString().trim()

        val feedback = FeedbackCriacao(
            usuario_id = usuarioId,
            treino_nome = nomeTreino,
            intensidade = intensidadeSelecionada,
            duracao_min = duracao,
            observacoes = obs
        )

        RetrofitClient.api.criarFeedback(feedback).enqueue(object : Callback<List<FeedbackBanco>> {
            override fun onResponse(call: Call<List<FeedbackBanco>>, response: Response<List<FeedbackBanco>>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@FeedbackPosTreinoActivity,
                        "Feedback enviado! Bom trabalho!", Toast.LENGTH_LONG).show()
                    val intent = Intent(this@FeedbackPosTreinoActivity, TreinosActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@FeedbackPosTreinoActivity,
                        "Erro ao salvar feedback (${response.code()})", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<List<FeedbackBanco>>, t: Throwable) {
                Toast.makeText(this@FeedbackPosTreinoActivity,
                    "Erro de conexão: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
