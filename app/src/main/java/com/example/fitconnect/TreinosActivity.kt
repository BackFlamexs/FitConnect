package com.example.fitconnect

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TreinosActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
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
        val btnAdicionar = findViewById<Button>(R.id.btn_adicionar_treino)
        recyclerView = findViewById(R.id.rv_treinos)

        filtroTodos = findViewById(R.id.filtro_todos)
        filtroForca = findViewById(R.id.filtro_forca)
        filtroCardio = findViewById(R.id.filtro_cardio)
        filtroMusculacao = findViewById(R.id.filtro_musculacao)

        recyclerView.layoutManager = LinearLayoutManager(this)

        btnVoltar.setOnClickListener { finish() }
        btnAdicionar.visibility = View.GONE

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

    private fun aplicarFiltro() {
        val filtrado = when (filtroAtual) {
            "Força" -> todosTreinos.filter {
                it.detalhes.contains("Força", ignoreCase = true)
            }
            "Cardio" -> todosTreinos.filter {
                it.detalhes.contains("Cardio", ignoreCase = true)
            }
            "Musculação" -> todosTreinos.filter {
                it.detalhes.contains("Musculação", ignoreCase = true)
            }
            else -> todosTreinos
        }
        recyclerView.adapter = criarAdapter(filtrado)
    }

    private fun criarAdapter(lista: List<Treino>): TreinoAdapter {
        return TreinoAdapter(lista, { treino ->
            androidx.appcompat.app.AlertDialog.Builder(this)
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
                } else {
                    emptySet()
                }
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
