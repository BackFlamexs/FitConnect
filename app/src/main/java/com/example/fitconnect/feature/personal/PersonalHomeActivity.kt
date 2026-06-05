package com.example.fitconnect.feature.personal

import com.example.fitconnect.R
import com.example.fitconnect.data.model.*
import com.example.fitconnect.core.network.RetrofitClient
import com.example.fitconnect.feature.auth.MainActivity

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.pow

class PersonalHomeActivity : AppCompatActivity() {

    private lateinit var adapter: PersonalAlunoAdapter
    private lateinit var tvAlunosVazio: TextView
    private lateinit var tvDadosAluno: TextView
    private lateinit var tvImcAluno: TextView
    private lateinit var tvBiografiaAluno: TextView
    private lateinit var tvResumoTreinosAluno: TextView
    private lateinit var resumoTreinosAdapter: ResumoTreinoAlunoAdapter
    private lateinit var rvResumoTreinosAluno: RecyclerView
    private var alunoSelecionado: PersonalAlunoVinculo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal_home)

        val tvNomePersonal = findViewById<TextView>(R.id.tv_nome_personal_home)
        val btnVincularAluno = findViewById<Button>(R.id.btn_vincular_aluno)
        val areaFeedbacks = findViewById<LinearLayout>(R.id.area_feedbacks_alunos)
        val areaMontarTreino = findViewById<LinearLayout>(R.id.area_montar_treino_aluno)
        val btnSair = findViewById<Button>(R.id.btn_sair_personal)
        val rvAlunos = findViewById<RecyclerView>(R.id.rv_alunos_vinculados)
        tvAlunosVazio = findViewById(R.id.tv_alunos_vazio)
        tvDadosAluno = findViewById(R.id.tv_dados_aluno_personal)
        tvImcAluno = findViewById(R.id.tv_imc_aluno_personal)
        tvBiografiaAluno = findViewById(R.id.tv_biografia_aluno_personal)
        tvResumoTreinosAluno = findViewById(R.id.tv_resumo_treinos_aluno)
        rvResumoTreinosAluno = findViewById(R.id.rv_resumo_treinos_aluno)

        tvNomePersonal.text = "Ola, ${Sessao.obterNomeUsuario(this)}"

        adapter = PersonalAlunoAdapter(emptyList()) { vinculo ->
            alunoSelecionado = vinculo
            val nome = obterNomeAluno(vinculo)
            atualizarDadosAluno(vinculo)
            carregarResumoTreinosAluno(vinculo.aluno_id)
            Toast.makeText(this, "$nome selecionado.", Toast.LENGTH_SHORT).show()
        }
        rvAlunos.layoutManager = LinearLayoutManager(this)
        rvAlunos.adapter = adapter

        resumoTreinosAdapter = ResumoTreinoAlunoAdapter(emptyList())
        rvResumoTreinosAluno.layoutManager = LinearLayoutManager(this)
        rvResumoTreinosAluno.adapter = resumoTreinosAdapter

        btnVincularAluno.setOnClickListener { mostrarDialogVincularAluno() }

        areaFeedbacks.setOnClickListener {
            val aluno = alunoSelecionado
            if (aluno == null) {
                Toast.makeText(this, "Selecione um aluno vinculado para ver feedbacks.", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this, PersonalAlunoFeedbacksActivity::class.java)
                intent.putExtra("ALUNO_ID", aluno.aluno_id)
                intent.putExtra("ALUNO_NOME", obterNomeAluno(aluno))
                startActivity(intent)
            }
        }

        areaMontarTreino.setOnClickListener {
            val aluno = alunoSelecionado
            if (aluno == null) {
                Toast.makeText(this, "Selecione um aluno vinculado para montar treino.", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this, PersonalAlunoTreinosActivity::class.java)
                intent.putExtra("ALUNO_ID", aluno.aluno_id)
                intent.putExtra("ALUNO_NOME", obterNomeAluno(aluno))
                startActivity(intent)
            }
        }

        btnSair.setOnClickListener {
            Sessao.limpar(this)
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        carregarAlunosVinculados()
    }

    private fun carregarAlunosVinculados() {
        val personalId = Sessao.obterUsuarioId(this)
        if (personalId == 0) {
            Toast.makeText(this, "Sessao expirada. Faca login novamente.", Toast.LENGTH_SHORT).show()
            return
        }

        RetrofitClient.api.buscarAlunosVinculados("eq.$personalId")
            .enqueue(object : Callback<List<PersonalAlunoVinculo>> {
                override fun onResponse(
                    call: Call<List<PersonalAlunoVinculo>>,
                    response: Response<List<PersonalAlunoVinculo>>
                ) {
                    if (response.isSuccessful) {
                        val vinculos = response.body() ?: emptyList()
                        adapter.atualizar(vinculos)
                        alunoSelecionado = alunoSelecionado?.takeIf { atual ->
                            vinculos.any { it.id == atual.id }
                        }
                        alunoSelecionado?.let {
                            atualizarDadosAluno(it)
                            carregarResumoTreinosAluno(it.aluno_id)
                        } ?: limparDadosAluno()
                        tvAlunosVazio.text = if (vinculos.isEmpty()) {
                            "Nenhum aluno vinculado ainda."
                        } else {
                            "Toque em um aluno para selecionar."
                        }
                    } else {
                        Toast.makeText(this@PersonalHomeActivity, "Erro ao carregar alunos: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<PersonalAlunoVinculo>>, t: Throwable) {
                    Toast.makeText(this@PersonalHomeActivity, "Sem conexao com o servidor", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun mostrarDialogVincularAluno() {
        val view = layoutInflater.inflate(R.layout.dialog_vincular_aluno, null)
        val etEmail = view.findViewById<EditText>(R.id.et_dialog_email_aluno)
        val btnCancelar = view.findViewById<android.widget.TextView>(R.id.btn_cancelar_vincular)
        val btnVincular = view.findViewById<android.widget.TextView>(R.id.btn_confirmar_vincular)

        val dialog = AlertDialog.Builder(this)
            .setView(view)
            .create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        btnCancelar.setOnClickListener { dialog.dismiss() }
        btnVincular.setOnClickListener {
            val email = etEmail.text.toString().trim().lowercase(Locale.ROOT)
            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Informe um e-mail valido.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            dialog.dismiss()
            buscarAlunoEVincular(email)
        }

        dialog.show()
    }

    private fun buscarAlunoEVincular(email: String) {
        RetrofitClient.api.buscarAlunoPorEmail("eq.$email")
            .enqueue(object : Callback<List<Usuario>> {
                override fun onResponse(call: Call<List<Usuario>>, response: Response<List<Usuario>>) {
                    if (!response.isSuccessful) {
                        Toast.makeText(this@PersonalHomeActivity, "Erro ao buscar aluno: ${response.code()}", Toast.LENGTH_SHORT).show()
                        return
                    }

                    val aluno = response.body()?.firstOrNull()
                    if (aluno == null || aluno.id == 0) {
                        Toast.makeText(this@PersonalHomeActivity, "Aluno nao encontrado com esse e-mail.", Toast.LENGTH_SHORT).show()
                        return
                    }

                    criarVinculo(aluno.id)
                }

                override fun onFailure(call: Call<List<Usuario>>, t: Throwable) {
                    Toast.makeText(this@PersonalHomeActivity, "Sem conexao com o servidor", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun criarVinculo(alunoId: Int) {
        val personalId = Sessao.obterUsuarioId(this)
        val vinculo = PersonalAlunoCriacao(personal_id = personalId, aluno_id = alunoId)

        RetrofitClient.api.criarVinculoAluno(vinculo)
            .enqueue(object : Callback<List<PersonalAlunoVinculo>> {
                override fun onResponse(
                    call: Call<List<PersonalAlunoVinculo>>,
                    response: Response<List<PersonalAlunoVinculo>>
                ) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@PersonalHomeActivity, "Aluno vinculado com sucesso.", Toast.LENGTH_SHORT).show()
                        carregarAlunosVinculados()
                    } else if (response.code() == 409) {
                        Toast.makeText(this@PersonalHomeActivity, "Esse aluno ja esta vinculado.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@PersonalHomeActivity, "Erro ao vincular: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<PersonalAlunoVinculo>>, t: Throwable) {
                    Toast.makeText(this@PersonalHomeActivity, "Sem conexao com o servidor", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun obterNomeAluno(vinculo: PersonalAlunoVinculo): String {
        val aluno = vinculo.usuarios
        return aluno?.nome_usuario?.takeIf { it.isNotBlank() }
            ?: aluno?.nome_completo?.takeIf { it.isNotBlank() }
            ?: "Aluno ${vinculo.aluno_id}"
    }

    private fun limparDadosAluno() {
        tvDadosAluno.text = "Selecione um aluno para visualizar nascimento, peso, altura e IMC."
        tvImcAluno.text = "IMC: --"
        tvBiografiaAluno.text = "Selecione um aluno para visualizar observacoes importantes."
        tvResumoTreinosAluno.text = "Selecione um aluno para visualizar os treinos da semana."
        tvResumoTreinosAluno.visibility = View.VISIBLE
        rvResumoTreinosAluno.visibility = View.GONE
        resumoTreinosAdapter.atualizar(emptyList())
    }

    private fun atualizarDadosAluno(vinculo: PersonalAlunoVinculo) {
        val aluno = vinculo.usuarios
        val peso = aluno?.peso
        val alturaCm = aluno?.altura
        val nascimentoTexto = formatarNascimento(aluno?.data_nascimento.orEmpty())
        val biografia = aluno?.biografia.orEmpty().trim()

        val pesoTexto = peso?.let { "${"%.1f".format(Locale.US, it)} kg" } ?: "Nao informado"
        val alturaTexto = alturaCm?.let { "$it cm" } ?: "Nao informada"
        tvDadosAluno.text = "Nascimento: $nascimentoTexto\nPeso: $pesoTexto\nAltura: $alturaTexto"
        tvBiografiaAluno.text = biografia.ifEmpty {
            "Aluno ainda nao preencheu a biografia."
        }

        val imc = if (peso != null && peso > 0.0 && alturaCm != null && alturaCm > 0) {
            val alturaM = alturaCm / 100.0
            peso / alturaM.pow(2.0)
        } else {
            null
        }

        tvImcAluno.text = if (imc != null) {
            "IMC: ${"%.1f".format(Locale.US, imc)} - ${classificarImc(imc)}"
        } else {
            "IMC: dados insuficientes"
        }
    }

    private fun classificarImc(imc: Double): String {
        return when {
            imc < 18.5 -> "Abaixo do peso"
            imc < 25.0 -> "Peso normal"
            imc < 30.0 -> "Sobrepeso"
            else -> "Obesidade"
        }
    }

    private fun formatarNascimento(dataBanco: String): String {
        if (dataBanco.isBlank()) return "Nao informada"
        return try {
            val entrada = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val data = entrada.parse(dataBanco.take(10)) ?: return "Nao informada"
            val saida = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
            "${saida.format(data)} (${calcularIdade(dataBanco)} anos)"
        } catch (e: Exception) {
            dataBanco
        }
    }

    private fun calcularIdade(dataBanco: String): Int {
        val partes = dataBanco.take(10).split("-")
        if (partes.size != 3) return 0
        val ano = partes[0].toIntOrNull() ?: return 0
        val mes = (partes[1].toIntOrNull() ?: return 0) - 1
        val dia = partes[2].toIntOrNull() ?: return 0
        val nascimento = Calendar.getInstance().apply { set(ano, mes, dia) }
        val hoje = Calendar.getInstance()
        var idade = hoje.get(Calendar.YEAR) - nascimento.get(Calendar.YEAR)
        if (hoje.get(Calendar.DAY_OF_YEAR) < nascimento.get(Calendar.DAY_OF_YEAR)) {
            idade--
        }
        return idade.coerceAtLeast(0)
    }

    private fun carregarResumoTreinosAluno(alunoId: Int) {
        RetrofitClient.api.buscarTreinosPorUsuario("eq.$alunoId")
            .enqueue(object : Callback<List<TreinoBanco>> {
                override fun onResponse(call: Call<List<TreinoBanco>>, response: Response<List<TreinoBanco>>) {
                    if (response.isSuccessful) {
                        val treinos = response.body() ?: emptyList()
                        if (treinos.isEmpty()) {
                            tvResumoTreinosAluno.text = "Este aluno ainda nao possui treinos montados."
                            tvResumoTreinosAluno.visibility = View.VISIBLE
                            rvResumoTreinosAluno.visibility = View.GONE
                            resumoTreinosAdapter.atualizar(emptyList())
                        } else {
                            tvResumoTreinosAluno.visibility = View.GONE
                            rvResumoTreinosAluno.visibility = View.VISIBLE
                            resumoTreinosAdapter.atualizar(treinos)
                        }
                    } else {
                        tvResumoTreinosAluno.text = "Nao foi possivel carregar os treinos do aluno."
                        tvResumoTreinosAluno.visibility = View.VISIBLE
                        rvResumoTreinosAluno.visibility = View.GONE
                    }
                }

                override fun onFailure(call: Call<List<TreinoBanco>>, t: Throwable) {
                    tvResumoTreinosAluno.text = "Sem conexao para carregar treinos do aluno."
                    tvResumoTreinosAluno.visibility = View.VISIBLE
                    rvResumoTreinosAluno.visibility = View.GONE
                }
            })
    }
}
