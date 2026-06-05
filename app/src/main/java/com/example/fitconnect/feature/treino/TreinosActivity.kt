package com.example.fitconnect.feature.treino

import com.example.fitconnect.R
import com.example.fitconnect.data.model.*
import com.example.fitconnect.core.network.RetrofitClient
import com.example.fitconnect.feature.pagamento.PagamentoProActivity

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TreinosActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var btnAdicionar: Button
    private var todosTreinos = listOf<Treino>()
    private var filtroAtual = "Todos"

    private lateinit var filtroTodos: TextView
    private lateinit var filtroForca: TextView
    private lateinit var filtroCardio: TextView
    private lateinit var filtroMusculacao: TextView
    private var treinosConcluidos = emptySet<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_treinos)

        val btnVoltar = findViewById<ImageView>(R.id.iv_voltar_treinos)
        btnAdicionar = findViewById(R.id.btn_adicionar_treino)
        recyclerView = findViewById(R.id.rv_treinos)

        filtroTodos = findViewById(R.id.filtro_todos)
        filtroForca = findViewById(R.id.filtro_forca)
        filtroCardio = findViewById(R.id.filtro_cardio)
        filtroMusculacao = findViewById(R.id.filtro_musculacao)

        recyclerView.layoutManager = LinearLayoutManager(this)

        btnVoltar.setOnClickListener { finish() }

        val isPro = Sessao.obterPro(this)
        val isPersonal = Sessao.obterAccountType(this) == "personal"

        if (isPersonal) {
            btnAdicionar.visibility = View.GONE
        } else {
            btnAdicionar.visibility = View.VISIBLE
            btnAdicionar.setOnClickListener { tentarCriarTreino() }
        }

        val todosChips = listOf(filtroTodos, filtroForca, filtroCardio, filtroMusculacao)

        fun selecionarFiltro(chip: TextView, filtro: String) {
            filtroAtual = filtro
            todosChips.forEach {
                it.setBackgroundResource(R.drawable.bg_pill_dark)
                it.setTextColor(0xFFA0A0A0.toInt())
            }
            chip.setBackgroundResource(R.drawable.bg_pill_green)
            chip.setTextColor(0xFFFFFFFF.toInt())
            aplicarFiltro()
        }

        filtroTodos.setOnClickListener { selecionarFiltro(filtroTodos, "Todos") }
        filtroForca.setOnClickListener { selecionarFiltro(filtroForca, "Força") }
        filtroCardio.setOnClickListener { selecionarFiltro(filtroCardio, "Cardio") }
        filtroMusculacao.setOnClickListener { selecionarFiltro(filtroMusculacao, "Musculação") }
    }

    override fun onResume() {
        super.onResume()
        carregarTreinos()
    }

    private fun tentarCriarTreino() {
        val isPro = Sessao.obterPro(this)
        if (!isPro && todosTreinos.size >= 3) {
            val dialog = Dialog(this)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.dialog_limite_treinos)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.window?.setLayout(
                (resources.displayMetrics.widthPixels * 0.88).toInt(),
                WindowManager.LayoutParams.WRAP_CONTENT
            )
            dialog.setCancelable(true)

            dialog.findViewById<Button>(R.id.btn_assinar_pro).setOnClickListener {
                dialog.dismiss()
                startActivity(Intent(this, PagamentoProActivity::class.java))
            }
            dialog.findViewById<Button>(R.id.btn_cancelar_limite).setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        } else {
            startActivity(Intent(this, CriacaoTreinoActivity::class.java))
        }
    }

    private fun atualizarBotaoAdicionar() {
        val isPro = Sessao.obterPro(this)
        val isPersonal = Sessao.obterAccountType(this) == "personal"
        if (isPersonal) return

        val bloqueado = !isPro && todosTreinos.size >= 3
        btnAdicionar.text = if (bloqueado) "LIMITE ATINGIDO — PRO" else "+ NOVO TREINO"
        btnAdicionar.alpha = if (bloqueado) 0.7f else 1.0f
    }

    private fun aplicarFiltro() {
        val filtrado = when (filtroAtual) {
            "Força" -> todosTreinos.filter { it.detalhes.contains("Força", ignoreCase = true) }
            "Cardio" -> todosTreinos.filter { it.detalhes.contains("Cardio", ignoreCase = true) }
            "Musculação" -> todosTreinos.filter { it.detalhes.contains("Musculação", ignoreCase = true) }
            else -> todosTreinos
        }
        recyclerView.adapter = criarAdapter(filtrado)
    }

    private fun criarAdapter(lista: List<Treino>): TreinoAdapter {
        return TreinoAdapter(lista, { treino ->
            AlertDialog.Builder(this)
                .setTitle("Excluir treino")
                .setMessage("Deseja excluir \"${treino.nome}\"?")
                .setPositiveButton("Excluir") { _, _ ->
                    RetrofitClient.api.deletarTreino("eq.${treino.id}").enqueue(object : Callback<Void> {
                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                            if (response.isSuccessful) {
                                Toast.makeText(this@TreinosActivity, "Treino excluído.", Toast.LENGTH_SHORT).show()
                                carregarTreinos()
                            } else {
                                Toast.makeText(this@TreinosActivity, "Erro ao excluir: ${response.code()}", Toast.LENGTH_SHORT).show()
                            }
                        }
                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            Toast.makeText(this@TreinosActivity, "Sem conexão com o servidor", Toast.LENGTH_SHORT).show()
                        }
                    })
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }, permitirGerenciar = false)
    }

    private fun carregarTreinos() {
        val usuarioId = Sessao.obterUsuarioId(this)
        RetrofitClient.api.buscarFeedbacks("eq.$usuarioId").enqueue(object : Callback<List<FeedbackBanco>> {
            override fun onResponse(call: Call<List<FeedbackBanco>>, response: Response<List<FeedbackBanco>>) {
                treinosConcluidos = if (response.isSuccessful) {
                    (response.body() ?: emptyList())
                        .map { it.treino_nome.trim().lowercase() }
                        .toSet()
                } else emptySet()
                carregarTreinosDoUsuario(usuarioId)
            }
            override fun onFailure(call: Call<List<FeedbackBanco>>, t: Throwable) {
                treinosConcluidos = emptySet()
                carregarTreinosDoUsuario(usuarioId)
            }
        })
    }

    private fun carregarTreinosDoUsuario(usuarioId: Int) {
        RetrofitClient.api.buscarTreinosPorUsuario("eq.$usuarioId").enqueue(object : Callback<List<TreinoBanco>> {
            override fun onResponse(call: Call<List<TreinoBanco>>, response: Response<List<TreinoBanco>>) {
                if (response.isSuccessful) {
                    todosTreinos = response.body()?.map { banco ->
                        Treino(
                            id = banco.id,
                            nome = banco.nome,
                            tagDia = banco.tag_dia,
                            diaSemana = banco.dia_semana,
                            detalhes = banco.detalhes,
                            concluido = banco.nome.trim().lowercase() in treinosConcluidos
                        )
                    } ?: emptyList()
                    aplicarFiltro()
                    atualizarBotaoAdicionar()
                } else {
                    Toast.makeText(this@TreinosActivity, "Erro: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<List<TreinoBanco>>, t: Throwable) {
                Toast.makeText(this@TreinosActivity, "Sem conexão com o servidor", Toast.LENGTH_SHORT).show()
            }
        })
    }
}