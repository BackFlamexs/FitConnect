package com.example.fitconnect

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CriacaoTreinoActivity : AppCompatActivity() {

    private val dias = arrayOf(
        "Segunda-feira", "Terça-feira", "Quarta-feira",
        "Quinta-feira", "Sexta-feira", "Sábado", "Domingo"
    )

    private val tagsMap = mapOf(
        "Segunda-feira" to Pair("SEG", "SEGUNDA-FEIRA"),
        "Terça-feira"   to Pair("TER", "TERÇA-FEIRA"),
        "Quarta-feira"  to Pair("QUA", "QUARTA-FEIRA"),
        "Quinta-feira"  to Pair("QUI", "QUINTA-FEIRA"),
        "Sexta-feira"   to Pair("SEX", "SEXTA-FEIRA"),
        "Sábado"        to Pair("SÁB", "SÁBADO"),
        "Domingo"       to Pair("DOM", "DOMINGO")
    )

    private val categorias = arrayOf("Força", "Cardio", "Musculação", "Funcional", "HIIT")
    private var diaSelecionado: String? = null
    private var categoriaSelecionada: String? = null

    private lateinit var llContainer: LinearLayout
    private lateinit var tvCount: TextView

    private var galeriaCache = listOf<GaleriaExercicioBanco>()
    private var galeriaCarregada = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_criacao_treino)

        val ivVoltar = findViewById<ImageView>(R.id.iv_voltar_criacao)
        val etNome = findViewById<EditText>(R.id.et_nome_treino_criar)
        val llDia = findViewById<LinearLayout>(R.id.ll_dia_semana)
        val tvDiaLabel = findViewById<TextView>(R.id.tv_dia_label)
        val llCategoria = findViewById<LinearLayout>(R.id.ll_categoria)
        val tvCategLabel = findViewById<TextView>(R.id.tv_categ_label)
        val btnAdicionar = findViewById<LinearLayout>(R.id.btn_adicionar_exercicio)
        val btnSalvar = findViewById<LinearLayout>(R.id.btn_salvar_treino_criacao)
        llContainer = findViewById(R.id.ll_exercicios_container)
        tvCount = findViewById(R.id.tv_count_exercicios)

        ivVoltar.setOnClickListener { finish() }

        llDia.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Selecione o dia")
                .setItems(dias) { _, index ->
                    diaSelecionado = dias[index]
                    tvDiaLabel.text = diaSelecionado
                    tvDiaLabel.setTextColor(0xFFFFFFFF.toInt())
                }
                .show()
        }

        llCategoria.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Selecione a categoria")
                .setItems(categorias) { _, index ->
                    categoriaSelecionada = categorias[index]
                    tvCategLabel.text = categoriaSelecionada
                    tvCategLabel.setTextColor(0xFFFFFFFF.toInt())
                }
                .show()
        }

        btnAdicionar.setOnClickListener { mostrarDialogGaleria() }

        btnSalvar.setOnClickListener {
            val nome = etNome.text.toString().trim()
            if (nome.isEmpty()) {
                Toast.makeText(this, "Digite o nome do treino.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (diaSelecionado == null) {
                Toast.makeText(this, "Selecione o dia da semana.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (categoriaSelecionada == null) {
                Toast.makeText(this, "Selecione a categoria.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            salvarTreino(nome)
        }

        carregarGaleria()
    }

    private fun carregarGaleria() {
        RetrofitClient.api.buscarGaleriaExercicios().enqueue(object : Callback<List<GaleriaExercicioBanco>> {
            override fun onResponse(call: Call<List<GaleriaExercicioBanco>>, response: Response<List<GaleriaExercicioBanco>>) {
                galeriaCache = response.body() ?: emptyList()
                galeriaCarregada = true
            }
            override fun onFailure(call: Call<List<GaleriaExercicioBanco>>, t: Throwable) {
                galeriaCarregada = true
            }
        })
    }

    private fun mostrarDialogGaleria() {
        if (!galeriaCarregada) {
            Toast.makeText(this, "Carregando exercícios, tente novamente.", Toast.LENGTH_SHORT).show()
            carregarGaleria()
            return
        }
        if (galeriaCache.isEmpty()) {
            Toast.makeText(this, "Nenhum exercício na galeria. Adicione no banco de dados.", Toast.LENGTH_LONG).show()
            return
        }

        var listaFiltrada = galeriaCache.toMutableList()

        val dialogView = layoutInflater.inflate(R.layout.dialog_selecionar_exercicio, null)
        val etBusca = dialogView.findViewById<EditText>(R.id.et_busca_galeria_dialog)
        val listView = dialogView.findViewById<ListView>(R.id.lv_galeria_dialog)

        val adapter = ArrayAdapter(
            this,
            R.layout.item_dialog_exercicio,
            listaFiltrada.map { it.nome }.toMutableList()
        )
        listView.adapter = adapter

        val dialog = AlertDialog.Builder(this)
            .setTitle("Selecionar Exercício")
            .setView(dialogView)
            .setNegativeButton("Cancelar", null)
            .create()

        etBusca.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val termo = s.toString().trim()
                listaFiltrada = if (termo.isEmpty()) {
                    galeriaCache.toMutableList()
                } else {
                    galeriaCache.filter {
                        it.nome.contains(termo, ignoreCase = true) ||
                        it.categoria.contains(termo, ignoreCase = true)
                    }.toMutableList()
                }
                adapter.clear()
                adapter.addAll(listaFiltrada.map { it.nome })
                adapter.notifyDataSetChanged()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        listView.setOnItemClickListener { _, _, position, _ ->
            if (position < listaFiltrada.size) {
                adicionarExercicio(listaFiltrada[position])
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun adicionarExercicio(exercicio: GaleriaExercicioBanco) {
        val view = layoutInflater.inflate(R.layout.item_exercicio_selecionado, llContainer, false)

        view.findViewById<TextView>(R.id.tv_nome_exerc_sel).text = exercicio.nome

        val info = listOfNotNull(
            exercicio.categoria.ifEmpty { null },
            exercicio.equipamento.ifEmpty { null }
        ).joinToString(" • ")
        view.findViewById<TextView>(R.id.tv_categ_exerc_sel).text = info.ifEmpty { exercicio.categoria }

        view.findViewById<ImageView>(R.id.iv_remover_exerc).setOnClickListener {
            llContainer.removeView(view)
            atualizarContador()
        }

        llContainer.addView(view)
        atualizarContador()
    }

    private fun atualizarContador() {
        tvCount.text = "Exercícios (${llContainer.childCount})"
    }

    private fun salvarTreino(nome: String) {
        val (tag, diaSemana) = tagsMap[diaSelecionado!!]!!
        val usuarioId = Sessao.obterUsuarioId(this)

        val novoTreino = TreinoCriacao(
            usuario_id = usuarioId,
            nome = nome,
            tag_dia = tag,
            dia_semana = diaSemana,
            detalhes = categoriaSelecionada!!
        )

        RetrofitClient.api.criarTreino(novoTreino).enqueue(object : Callback<List<TreinoBanco>> {
            override fun onResponse(call: Call<List<TreinoBanco>>, response: Response<List<TreinoBanco>>) {
                if (response.isSuccessful) {
                    val treinoId = response.body()?.firstOrNull()?.id ?: 0
                    if (treinoId > 0 && llContainer.childCount > 0) {
                        salvarExercicios(treinoId)
                    } else {
                        Toast.makeText(this@CriacaoTreinoActivity, "Treino criado!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } else {
                    Toast.makeText(this@CriacaoTreinoActivity, "Erro ao criar treino: ${response.code()}", Toast.LENGTH_LONG).show()
                }
            }
            override fun onFailure(call: Call<List<TreinoBanco>>, t: Throwable) {
                Toast.makeText(this@CriacaoTreinoActivity, "Sem conexão com o servidor", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun salvarExercicios(treinoId: Int) {
        val exerciciosList = mutableListOf<ExercicioCriacao>()

        for (i in 0 until llContainer.childCount) {
            val v = llContainer.getChildAt(i)
            val nome = v.findViewById<TextView>(R.id.tv_nome_exerc_sel).text.toString()
            val series = v.findViewById<EditText>(R.id.et_series_exerc).text.toString().trim().ifEmpty { "3" }
            val reps = v.findViewById<EditText>(R.id.et_reps_exerc).text.toString().trim().ifEmpty { "10" }
            exerciciosList.add(ExercicioCriacao(treino_id = treinoId, nome = nome, series_reps = "${series}x${reps}"))
        }

        var remaining = exerciciosList.size
        var saved = true

        for (ex in exerciciosList) {
            RetrofitClient.api.criarExercicio(ex).enqueue(object : Callback<List<ExercicioBanco>> {
                override fun onResponse(call: Call<List<ExercicioBanco>>, response: Response<List<ExercicioBanco>>) {
                    remaining--
                    if (remaining == 0) {
                        val msg = if (saved) "Treino salvo com sucesso!" else "Treino salvo, mas alguns exercícios falharam."
                        Toast.makeText(this@CriacaoTreinoActivity, msg, Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
                override fun onFailure(call: Call<List<ExercicioBanco>>, t: Throwable) {
                    saved = false
                    remaining--
                    if (remaining == 0) {
                        Toast.makeText(this@CriacaoTreinoActivity, "Treino salvo, mas alguns exercícios falharam.", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            })
        }
    }
}