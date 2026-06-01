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

class EditarTreinoActivity : AppCompatActivity() {

    private val dias = arrayOf(
        "Segunda-feira", "Terça-feira", "Quarta-feira",
        "Quinta-feira", "Sexta-feira", "Sábado", "Domingo"
    )

    private val tagsMap = mapOf(
        "Segunda-feira" to Pair("SEG", "SEGUNDA-FEIRA"),
        "Terça-feira" to Pair("TER", "TERÇA-FEIRA"),
        "Quarta-feira" to Pair("QUA", "QUARTA-FEIRA"),
        "Quinta-feira" to Pair("QUI", "QUINTA-FEIRA"),
        "Sexta-feira" to Pair("SEX", "SEXTA-FEIRA"),
        "Sábado" to Pair("SÁB", "SÁBADO"),
        "Domingo" to Pair("DOM", "DOMINGO")
    )

    private lateinit var llContainer: LinearLayout
    private lateinit var tvCount: TextView
    private lateinit var tvDiaLabel: TextView

    private var treinoId = 0
    private var tagOriginal = ""
    private var diaSemanaOriginal = ""
    private var tagSelecionada = ""
    private var diaSemanaSelecionado = ""
    private var todosTreinos = listOf<TreinoBanco>()
    private var galeriaCache = listOf<GaleriaExercicioBanco>()
    private var galeriaCarregada = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_treino)

        val btnVoltar = findViewById<ImageView>(R.id.iv_voltar_editar)
        val etNome = findViewById<EditText>(R.id.et_nome_treino_editar)
        val etDescricao = findViewById<EditText>(R.id.et_descricao_treino_editar)
        val btnSalvar = findViewById<android.widget.Button>(R.id.btn_salvar_alteracoes)
        val tvAdicionar = findViewById<TextView>(R.id.tv_adicionar_exercicio_editar)
        val llDia = findViewById<LinearLayout>(R.id.ll_dia_semana_editar)

        llContainer = findViewById(R.id.ll_exercicios_container_editar)
        tvCount = findViewById(R.id.tv_count_exercicios_editar)
        tvDiaLabel = findViewById(R.id.tv_dia_label_editar)

        treinoId = intent.getIntExtra("TREINO_ID", 0)
        val nomeTreino = intent.getStringExtra("NOME_TREINO") ?: ""
        val detalhesTreino = intent.getStringExtra("DETALHES_TREINO") ?: ""
        tagOriginal = intent.getStringExtra("TAG_DIA") ?: ""
        diaSemanaOriginal = intent.getStringExtra("DIA_SEMANA") ?: ""
        tagSelecionada = tagOriginal
        diaSemanaSelecionado = diaSemanaOriginal
        etNome.setText(nomeTreino)
        etDescricao.setText(separarCategoriaDescricao(detalhesTreino).second)
        tvDiaLabel.text = diaSemanaSelecionado.ifEmpty { "Selecione o dia" }

        btnVoltar.setOnClickListener { finish() }
        tvAdicionar.setOnClickListener { mostrarDialogGaleria() }
        llDia.setOnClickListener { mostrarDialogDia() }

        btnSalvar.setOnClickListener {
            val novoNome = etNome.text.toString().trim()
            if (novoNome.isEmpty()) {
                Toast.makeText(this, "O nome do treino nao pode ficar vazio.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (diaSemanaSelecionado.isBlank() || tagSelecionada.isBlank()) {
                Toast.makeText(this, "Selecione o dia da semana.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            validarConflitoESalvar(novoNome, etDescricao.text.toString().trim())
        }

        carregarTreinos()
        carregarGaleria()
        carregarExerciciosAtuais()
    }

    private fun mostrarDialogDia() {
        AlertDialog.Builder(this)
            .setTitle("Selecione o dia")
            .setItems(dias) { _, index ->
                val escolhido = dias[index]
                val (tag, diaSemana) = tagsMap[escolhido] ?: return@setItems
                tagSelecionada = tag
                diaSemanaSelecionado = diaSemana
                tvDiaLabel.text = diaSemana
            }
            .show()
    }

    private fun carregarTreinos() {
        RetrofitClient.api.buscarTreinos().enqueue(object : Callback<List<TreinoBanco>> {
            override fun onResponse(call: Call<List<TreinoBanco>>, response: Response<List<TreinoBanco>>) {
                if (response.isSuccessful) {
                    todosTreinos = response.body() ?: emptyList()
                }
            }

            override fun onFailure(call: Call<List<TreinoBanco>>, t: Throwable) {}
        })
    }

    private fun carregarGaleria() {
        RetrofitClient.api.buscarGaleriaExercicios().enqueue(object : Callback<List<GaleriaExercicioBanco>> {
            override fun onResponse(call: Call<List<GaleriaExercicioBanco>>, response: Response<List<GaleriaExercicioBanco>>) {
                galeriaCache = removerDuplicados(response.body() ?: emptyList())
                galeriaCarregada = true
            }

            override fun onFailure(call: Call<List<GaleriaExercicioBanco>>, t: Throwable) {
                galeriaCarregada = true
            }
        })
    }

    private fun carregarExerciciosAtuais() {
        if (treinoId == 0) return
        RetrofitClient.api.buscarExercicios("eq.$treinoId").enqueue(object : Callback<List<ExercicioBanco>> {
            override fun onResponse(call: Call<List<ExercicioBanco>>, response: Response<List<ExercicioBanco>>) {
                if (response.isSuccessful) {
                    llContainer.removeAllViews()
                    removerDuplicadosExercicios(response.body() ?: emptyList()).forEach { exercicio ->
                        adicionarExercicioExistente(exercicio)
                    }
                    atualizarContador()
                }
            }

            override fun onFailure(call: Call<List<ExercicioBanco>>, t: Throwable) {
                Toast.makeText(this@EditarTreinoActivity, "Erro ao carregar exercicios", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun mostrarDialogGaleria() {
        if (!galeriaCarregada) {
            Toast.makeText(this, "Carregando exercicios, tente novamente.", Toast.LENGTH_SHORT).show()
            carregarGaleria()
            return
        }
        if (galeriaCache.isEmpty()) {
            Toast.makeText(this, "Nenhum exercicio na galeria.", Toast.LENGTH_SHORT).show()
            return
        }

        var listaFiltrada = filtrarNaoSelecionados(galeriaCache)

        val dialogView = layoutInflater.inflate(R.layout.dialog_selecionar_exercicio, null)
        val etBusca = dialogView.findViewById<EditText>(R.id.et_busca_galeria_dialog)
        val listView = dialogView.findViewById<ListView>(R.id.lv_galeria_dialog)
        val adapter = ArrayAdapter(this, R.layout.item_dialog_exercicio, listaFiltrada.map { corrigirNomeExercicio(it.nome) }.toMutableList())
        listView.adapter = adapter

        val dialog = AlertDialog.Builder(this)
            .setTitle("Selecionar Exercício")
            .setView(dialogView)
            .setNegativeButton("Cancelar", null)
            .create()

        etBusca.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val termo = s.toString().trim()
                val base = filtrarNaoSelecionados(galeriaCache)
                listaFiltrada = if (termo.isEmpty()) {
                    base
                } else {
                    base.filter {
                        it.nome.contains(termo, ignoreCase = true) ||
                            it.categoria.contains(termo, ignoreCase = true)
                    }
                }
                adapter.clear()
                adapter.addAll(listaFiltrada.map { corrigirNomeExercicio(it.nome) })
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
        if (exercicioJaAdicionado(exercicio.nome)) {
            Toast.makeText(this, "Exercicio ja adicionado.", Toast.LENGTH_SHORT).show()
            return
        }
        adicionarExercicioView(exercicio.nome, exercicio.categoria, exercicio.equipamento, "3", "10")
    }

    private fun adicionarExercicioExistente(exercicio: ExercicioBanco) {
        val partes = exercicio.series_reps.lowercase().split("x")
        val series = partes.getOrNull(0)?.filter { it.isDigit() }.orEmpty().ifEmpty { "3" }
        val reps = partes.getOrNull(1)?.filter { it.isDigit() }.orEmpty().ifEmpty { "10" }
        adicionarExercicioView(exercicio.nome, "Atual", "", series, reps)
    }

    private fun adicionarExercicioView(nome: String, categoria: String, equipamento: String, series: String, reps: String) {
        val view = layoutInflater.inflate(R.layout.item_exercicio_selecionado, llContainer, false)

        view.findViewById<TextView>(R.id.tv_nome_exerc_sel).text = corrigirNomeExercicio(nome)
        view.findViewById<TextView>(R.id.tv_categ_exerc_sel).text = listOf(categoria, equipamento)
            .filter { it.isNotBlank() }
            .joinToString(" • ")
            .ifEmpty { categoria }
        view.findViewById<EditText>(R.id.et_series_exerc).setText(series)
        view.findViewById<EditText>(R.id.et_reps_exerc).setText(reps)
        view.findViewById<ImageView>(R.id.iv_remover_exerc).setOnClickListener {
            llContainer.removeView(view)
            atualizarContador()
        }

        llContainer.addView(view)
        atualizarContador()
    }

    private fun validarConflitoESalvar(novoNome: String, descricao: String) {
        val mudouDia = diaSemanaSelecionado != diaSemanaOriginal
        val treinoConflitante = todosTreinos.firstOrNull {
            it.id != treinoId && it.dia_semana == diaSemanaSelecionado
        }

        if (mudouDia && treinoConflitante != null) {
            mostrarDialogTrocaDia(novoNome, descricao, treinoConflitante)
        } else {
            salvarAlteracoes(novoNome, descricao, null)
        }
    }

    private fun mostrarDialogTrocaDia(novoNome: String, descricao: String, treinoConflitante: TreinoBanco) {
        val view = layoutInflater.inflate(R.layout.dialog_trocar_dia_treino, null)
        val dialog = AlertDialog.Builder(this)
            .setView(view)
            .create()

        view.findViewById<TextView>(R.id.tv_mensagem_troca_dia).text =
            "\"${treinoConflitante.nome}\" já está em $diaSemanaSelecionado. Deseja trocar os dias entre os dois treinos?"

        view.findViewById<TextView>(R.id.btn_cancelar_troca_dia).setOnClickListener {
            dialog.dismiss()
        }

        view.findViewById<TextView>(R.id.btn_confirmar_troca_dia).setOnClickListener {
            dialog.dismiss()
            salvarAlteracoes(novoNome, descricao, treinoConflitante)
        }

        dialog.setOnShowListener {
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        }
        dialog.show()
    }

    private fun salvarAlteracoes(novoNome: String, descricao: String, treinoParaTrocar: TreinoBanco?) {
        RetrofitClient.api.atualizarTreino(
            "eq.$treinoId",
            TreinoAtualizar(
                nome = novoNome,
                tag_dia = tagSelecionada,
                dia_semana = diaSemanaSelecionado,
                detalhes = montarDetalhesAtualizados(descricao)
            )
        )
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        if (treinoParaTrocar != null) {
                            trocarDiaDoTreinoConflitante(treinoParaTrocar)
                        } else {
                            substituirExercicios()
                        }
                    } else {
                        Toast.makeText(this@EditarTreinoActivity, "Erro: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(this@EditarTreinoActivity, "Sem conexao com o servidor", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun trocarDiaDoTreinoConflitante(treino: TreinoBanco) {
        RetrofitClient.api.atualizarTreino(
            "eq.${treino.id}",
            TreinoAtualizar(
                nome = treino.nome,
                tag_dia = tagOriginal,
                dia_semana = diaSemanaOriginal
            )
        ).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    substituirExercicios()
                } else {
                    Toast.makeText(this@EditarTreinoActivity, "Erro ao trocar dia do outro treino: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@EditarTreinoActivity, "Erro ao trocar dia do outro treino", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun substituirExercicios() {
        RetrofitClient.api.deletarExercicioPorTreino("eq.$treinoId").enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                salvarExercicios()
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@EditarTreinoActivity, "Erro ao substituir exercicios", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun salvarExercicios() {
        val exercicios = coletarExercicios()
        if (exercicios.isEmpty()) {
            Toast.makeText(this, "Treino atualizado!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        var restantes = exercicios.size
        var sucesso = true
        exercicios.forEach { exercicio ->
            RetrofitClient.api.criarExercicio(exercicio).enqueue(object : Callback<List<ExercicioBanco>> {
                override fun onResponse(call: Call<List<ExercicioBanco>>, response: Response<List<ExercicioBanco>>) {
                    if (!response.isSuccessful) sucesso = false
                    restantes--
                    finalizarSeTerminou(restantes, sucesso)
                }

                override fun onFailure(call: Call<List<ExercicioBanco>>, t: Throwable) {
                    sucesso = false
                    restantes--
                    finalizarSeTerminou(restantes, sucesso)
                }
            })
        }
    }

    private fun finalizarSeTerminou(restantes: Int, sucesso: Boolean) {
        if (restantes == 0) {
            Toast.makeText(
                this,
                if (sucesso) "Treino atualizado!" else "Treino atualizado, mas alguns exercicios falharam.",
                Toast.LENGTH_SHORT
            ).show()
            finish()
        }
    }

    private fun coletarExercicios(): List<ExercicioCriacao> {
        val vistos = mutableSetOf<String>()
        val lista = mutableListOf<ExercicioCriacao>()
        for (i in 0 until llContainer.childCount) {
            val view = llContainer.getChildAt(i)
            val nome = corrigirNomeExercicio(view.findViewById<TextView>(R.id.tv_nome_exerc_sel).text.toString().trim())
            val chave = nome.lowercase()
            if (nome.isBlank() || !vistos.add(chave)) continue
            val series = view.findViewById<EditText>(R.id.et_series_exerc).text.toString().trim().ifEmpty { "3" }
            val reps = view.findViewById<EditText>(R.id.et_reps_exerc).text.toString().trim().ifEmpty { "10" }
            lista.add(ExercicioCriacao(treino_id = treinoId, nome = nome, series_reps = "${series}x${reps}"))
        }
        return lista
    }

    private fun filtrarNaoSelecionados(lista: List<GaleriaExercicioBanco>): List<GaleriaExercicioBanco> {
        return lista.filterNot { exercicioJaAdicionado(it.nome) }
    }

    private fun exercicioJaAdicionado(nome: String): Boolean {
        val chave = nome.trim().lowercase()
        for (i in 0 until llContainer.childCount) {
            val atual = llContainer.getChildAt(i)
                .findViewById<TextView>(R.id.tv_nome_exerc_sel)
                .text
                .toString()
                .trim()
                .lowercase()
            if (atual == chave) return true
        }
        return false
    }

    private fun removerDuplicados(lista: List<GaleriaExercicioBanco>): List<GaleriaExercicioBanco> {
        return lista.filter { it.nome.isNotBlank() }.distinctBy { it.nome.trim().lowercase() }
    }

    private fun removerDuplicadosExercicios(lista: List<ExercicioBanco>): List<ExercicioBanco> {
        return lista.filter { it.nome.isNotBlank() }.distinctBy { it.nome.trim().lowercase() }
    }

    private fun atualizarContador() {
        tvCount.text = "Exercícios (${llContainer.childCount})"
    }
    private fun montarDetalhesAtualizados(descricao: String): String {
        val categoria = separarCategoriaDescricao(intent.getStringExtra("DETALHES_TREINO") ?: "").first
        val descricaoFinal = descricao.ifBlank { "Descricao ainda nao informada para este treino." }
        return "$categoria • $descricaoFinal"
    }

    private fun separarCategoriaDescricao(detalhes: String): Pair<String, String> {
        val partes = detalhes.split("•", limit = 2).map { it.trim() }
        val categoria = partes.getOrNull(0)?.ifBlank { "Treino" } ?: "Treino"
        val descricao = partes.getOrNull(1)?.ifBlank { null } ?: ""
        return categoria to descricao
    }

    private fun corrigirNomeExercicio(nome: String): String {
        return when (nome.trim().lowercase()) {
            "afundo" -> "Agachamento"
            else -> nome
        }
    }
}
