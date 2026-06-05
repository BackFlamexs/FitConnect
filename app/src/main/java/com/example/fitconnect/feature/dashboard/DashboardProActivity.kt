package com.example.fitconnect.feature.dashboard

import com.example.fitconnect.R
import com.example.fitconnect.data.model.*
import com.example.fitconnect.core.network.RetrofitClient

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DashboardProActivity : AppCompatActivity() {

    private lateinit var tvGreeting: TextView
    private lateinit var tvTreinosRealizados: TextView
    private lateinit var tvDiasConsecutivos: TextView
    private lateinit var tvTempoTotal: TextView
    private lateinit var tvExerciciosConcluidos: TextView
    private lateinit var tvEsteMes: TextView
    private lateinit var tvUltimoMes: TextView
    private lateinit var tvEvolucaoLabel: TextView
    private lateinit var tvMensagemMotivacional: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard_pro)

        tvGreeting = findViewById(R.id.tv_greeting_dashboard)
        tvTreinosRealizados = findViewById(R.id.tv_treinos_realizados)
        tvDiasConsecutivos = findViewById(R.id.tv_dias_consecutivos)
        tvTempoTotal = findViewById(R.id.tv_tempo_total)
        tvExerciciosConcluidos = findViewById(R.id.tv_exercicios_concluidos)
        tvEsteMes = findViewById(R.id.tv_este_mes)
        tvUltimoMes = findViewById(R.id.tv_ultimo_mes)
        tvEvolucaoLabel = findViewById(R.id.tv_evolucao_label)
        tvMensagemMotivacional = findViewById(R.id.tv_mensagem_motivacional)

        val nome = Sessao.obterNomeUsuario(this).replaceFirstChar { it.uppercase() }
        tvGreeting.text = "Seus resultados, $nome"

        findViewById<ImageView>(R.id.iv_voltar_dashboard).setOnClickListener { finish() }

        carregarEstatisticas()
    }

    private fun carregarEstatisticas() {
        val usuarioId = Sessao.obterUsuarioId(this)
        RetrofitClient.api.buscarFeedbacks("eq.$usuarioId")
            .enqueue(object : Callback<List<FeedbackBanco>> {
                override fun onResponse(call: Call<List<FeedbackBanco>>, response: Response<List<FeedbackBanco>>) {
                    val feedbacks = if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
                    carregarTreinos(usuarioId, feedbacks)
                }
                override fun onFailure(call: Call<List<FeedbackBanco>>, t: Throwable) {
                    carregarTreinos(usuarioId, emptyList())
                }
            })
    }

    private fun carregarTreinos(usuarioId: Int, feedbacks: List<FeedbackBanco>) {
        RetrofitClient.api.buscarTreinosPorUsuario("eq.$usuarioId")
            .enqueue(object : Callback<List<TreinoBanco>> {
                override fun onResponse(call: Call<List<TreinoBanco>>, response: Response<List<TreinoBanco>>) {
                    val treinos = if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
                    exibirEstatisticas(feedbacks, treinos)
                }
                override fun onFailure(call: Call<List<TreinoBanco>>, t: Throwable) {
                    exibirEstatisticas(feedbacks, emptyList())
                }
            })
    }

    private fun exibirEstatisticas(feedbacks: List<FeedbackBanco>, treinos: List<TreinoBanco>) {
        val treinosRealizados = feedbacks.size
        val tempoTotal = feedbacks.sumOf { it.duracao_min }
        val diasConsecutivos = calcularDiasConsecutivos(feedbacks)
        val exerciciosConcluidos = if (treinosRealizados > 0) treinosRealizados * 6 else treinos.size * 5

        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val agora = Calendar.getInstance()
        val mesAtual = sdf.format(agora.time)
        agora.add(Calendar.MONTH, -1)
        val mesAnterior = sdf.format(agora.time)

        val treinosEsteMes = feedbacks.count { it.criado_em.take(7) == mesAtual }
        val treinosUltimoMes = feedbacks.count { it.criado_em.take(7) == mesAnterior }

        tvTreinosRealizados.text = treinosRealizados.toString()
        tvDiasConsecutivos.text = diasConsecutivos.toString()
        tvTempoTotal.text = formatarTempo(tempoTotal)
        tvExerciciosConcluidos.text = exerciciosConcluidos.toString()
        tvEsteMes.text = treinosEsteMes.toString()
        tvUltimoMes.text = treinosUltimoMes.toString()

        tvEvolucaoLabel.text = when {
            treinosUltimoMes == 0 && treinosEsteMes > 0 -> "Ótimo começo este mês!"
            treinosUltimoMes == 0 -> "Comece a treinar para ver sua evolução!"
            treinosEsteMes > treinosUltimoMes -> {
                val pct = ((treinosEsteMes - treinosUltimoMes) * 100.0 / treinosUltimoMes).toInt()
                "+$pct% em relação ao mês anterior"
            }
            treinosEsteMes < treinosUltimoMes -> {
                val pct = ((treinosUltimoMes - treinosEsteMes) * 100.0 / treinosUltimoMes).toInt()
                "-$pct% em relação ao mês anterior"
            }
            else -> "Mesmo ritmo do mês anterior"
        }

        tvMensagemMotivacional.text = mensagemMotivacional(treinosRealizados, diasConsecutivos)
    }

    private fun calcularDiasConsecutivos(feedbacks: List<FeedbackBanco>): Int {
        if (feedbacks.isEmpty()) return 0
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val datasUnicas = feedbacks.mapNotNull {
            try { sdf.parse(it.criado_em.take(10)) } catch (e: Exception) { null }
        }.map {
            Calendar.getInstance().apply { time = it }.also { c ->
                c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0)
                c.set(Calendar.SECOND, 0); c.set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        }.distinct().sortedDescending()

        if (datasUnicas.isEmpty()) return 0

        val hoje = Calendar.getInstance().also {
            it.set(Calendar.HOUR_OF_DAY, 0); it.set(Calendar.MINUTE, 0)
            it.set(Calendar.SECOND, 0); it.set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val umDia = 86400000L

        var streak = 0
        var esperado = hoje
        for (data in datasUnicas) {
            if (data == esperado || data == esperado - umDia) {
                streak++
                esperado = data - umDia
            } else {
                break
            }
        }
        return streak
    }

    private fun formatarTempo(minutos: Int): String {
        return if (minutos >= 60) {
            val h = minutos / 60
            val m = minutos % 60
            if (m == 0) "${h}h" else "${h}h ${m}min"
        } else {
            "$minutos min"
        }
    }

    private fun mensagemMotivacional(treinos: Int, streak: Int): String {
        return when {
            treinos == 0 -> "Comece agora! Seu primeiro treino é o mais importante."
            streak >= 5 -> "Incrível! $streak dias consecutivos. Você é imparável!"
            streak >= 3 -> "Ótima sequência de $streak dias! Continue assim!"
            treinos >= 20 -> "Mais de $treinos treinos concluídos. Você é dedicado!"
            treinos >= 10 -> "$treinos treinos no histórico. Evolução constante!"
            else -> "Continue treinando! Cada treino conta para sua evolução."
        }
    }
}