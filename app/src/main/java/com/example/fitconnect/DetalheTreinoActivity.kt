package com.example.fitconnect

import android.content.Intent
import android.os.Bundle
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

class DetalheTreinoActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private var exerciciosBanco = listOf<ExercicioBanco>()
    private var galeriaBanco = listOf<GaleriaExercicioBanco>()
    private lateinit var tvDescricao: TextView
    private lateinit var tvResumo: TextView
    private lateinit var tvCategoria: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalhe_treino)

        val btnVoltar = findViewById<ImageView>(R.id.iv_voltar_detalhe)
        val tvTitulo = findViewById<TextView>(R.id.tv_titulo_detalhe)
        val btnIniciar = findViewById<Button>(R.id.btn_iniciar_treino)
        recyclerView = findViewById(R.id.rv_exercicios_detalhe)
        tvDescricao = findViewById(R.id.tv_descricao_treino_detalhe)
        tvResumo = findViewById(R.id.tv_resumo_treino_detalhe)
        tvCategoria = findViewById(R.id.tv_categoria_treino_detalhe)

        val nomeTreino = intent.getStringExtra("NOME_TREINO") ?: ""
        val treinoId = intent.getIntExtra("TREINO_ID", 0)
        val detalhesTreino = intent.getStringExtra("DETALHES_TREINO") ?: ""

        tvTitulo.text = nomeTreino
        val (categoria, descricao) = separarCategoriaDescricao(detalhesTreino)
        tvCategoria.text = categoria
        tvDescricao.text = descricao
        tvResumo.text = "Carregando exercicios..."
        recyclerView.layoutManager = LinearLayoutManager(this)

        btnVoltar.setOnClickListener { finish() }
        btnIniciar.setOnClickListener {
            val intent = Intent(this, ExecucaoTreinoActivity::class.java)
            intent.putExtra("NOME_TREINO", nomeTreino)
            startActivity(intent)
        }

        carregarGaleria()
        carregarExercicios(treinoId)
    }

    private fun carregarExercicios(treinoId: Int) {
        RetrofitClient.api.buscarExercicios("eq.$treinoId")
            .enqueue(object : Callback<List<ExercicioBanco>> {
                override fun onResponse(
                    call: Call<List<ExercicioBanco>>,
                    response: Response<List<ExercicioBanco>>
                ) {
                    if (response.isSuccessful) {
                        exerciciosBanco = response.body() ?: emptyList()
                        atualizarListaExercicios()
                    } else {
                        Toast.makeText(
                            this@DetalheTreinoActivity,
                            "Erro ao buscar exercicios: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<List<ExercicioBanco>>, t: Throwable) {
                    Toast.makeText(
                        this@DetalheTreinoActivity,
                        "Sem conexao com o servidor",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun carregarGaleria() {
        RetrofitClient.api.buscarGaleriaExercicios()
            .enqueue(object : Callback<List<GaleriaExercicioBanco>> {
                override fun onResponse(
                    call: Call<List<GaleriaExercicioBanco>>,
                    response: Response<List<GaleriaExercicioBanco>>
                ) {
                    if (response.isSuccessful) {
                        galeriaBanco = response.body() ?: emptyList()
                        atualizarListaExercicios()
                    }
                }

                override fun onFailure(call: Call<List<GaleriaExercicioBanco>>, t: Throwable) {
                    galeriaBanco = emptyList()
                }
            })
    }

    private fun atualizarListaExercicios() {
        val galeriaPorNome = galeriaBanco.associateBy { normalizarNome(it.nome) }
        val exercicios = exerciciosBanco.map { banco ->
            val nomeCorrigido = corrigirNomeExercicio(banco.nome)
            val nomeExercicio = normalizarNome(nomeCorrigido)
            val galeria = galeriaPorNome[nomeExercicio]
                ?: galeriaBanco.firstOrNull {
                    val nomeGaleria = normalizarNome(it.nome)
                    nomeExercicio.contains(nomeGaleria) || nomeGaleria.contains(nomeExercicio)
                }

            Exercicio(
                nome = nomeCorrigido,
                seriesReps = banco.series_reps,
                gifUrl = galeria?.gif_url.orEmpty(),
                categoria = galeria?.categoria.orEmpty(),
                dificuldade = galeria?.dificuldade.orEmpty(),
                equipamento = galeria?.equipamento.orEmpty(),
                instrucoes = galeria?.instrucoes.orEmpty()
            )
        }

        tvResumo.text = "${exercicios.size} exercicios neste treino"
        recyclerView.adapter = ExercicioDetalheAdapter(exercicios)
    }

    private fun separarCategoriaDescricao(detalhes: String): Pair<String, String> {
        val partes = detalhes.split("•", limit = 2).map { it.trim() }
        val categoria = partes.getOrNull(0)?.ifBlank { "Treino" } ?: "Treino"
        val descricao = partes.getOrNull(1)?.ifBlank { null }
            ?: "Descricao ainda nao informada para este treino."
        return categoria to descricao
    }

    private fun corrigirNomeExercicio(nome: String): String {
        return when (normalizarNome(nome)) {
            "afundo" -> "Agachamento"
            else -> nome
        }
    }

    private fun normalizarNome(nome: String): String {
        return nome
            .trim()
            .lowercase()
            .replace("á", "a")
            .replace("à", "a")
            .replace("â", "a")
            .replace("ã", "a")
            .replace("é", "e")
            .replace("ê", "e")
            .replace("í", "i")
            .replace("ó", "o")
            .replace("ô", "o")
            .replace("õ", "o")
            .replace("ú", "u")
            .replace("ç", "c")
    }
}
