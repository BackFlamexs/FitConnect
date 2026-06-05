package com.example.fitconnect.feature.personal

import com.example.fitconnect.R
import com.example.fitconnect.data.model.*
import com.example.fitconnect.core.network.RetrofitClient

import android.os.Bundle
import android.view.View
import android.widget.EditText
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

class PersonalAlunoFeedbacksActivity : AppCompatActivity() {

    private lateinit var adapter: PersonalFeedbackAdapter
    private var alunoId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal_aluno_feedbacks)

        alunoId = intent.getIntExtra("ALUNO_ID", 0)
        val alunoNome = intent.getStringExtra("ALUNO_NOME") ?: "Aluno"
        val rv = findViewById<RecyclerView>(R.id.rv_feedbacks_aluno_personal)
        val tvVazio = findViewById<TextView>(R.id.tv_feedbacks_aluno_vazio)

        findViewById<ImageView>(R.id.iv_voltar_feedbacks_aluno).setOnClickListener { finish() }
        findViewById<TextView>(R.id.tv_titulo_feedbacks_aluno).text = "Feedbacks de $alunoNome"

        adapter = PersonalFeedbackAdapter(emptyList()) { feedback -> mostrarDialogResposta(feedback) }
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        if (alunoId == 0) {
            tvVazio.visibility = View.VISIBLE
            rv.visibility = View.GONE
            return
        }

        carregarFeedbacks(rv, tvVazio)
    }

    private fun carregarFeedbacks(rv: RecyclerView, tvVazio: TextView) {
        RetrofitClient.api.buscarFeedbacks("eq.$alunoId")
            .enqueue(object : Callback<List<FeedbackBanco>> {
                override fun onResponse(call: Call<List<FeedbackBanco>>, response: Response<List<FeedbackBanco>>) {
                    if (response.isSuccessful) {
                        val feedbacks = response.body() ?: emptyList()
                        adapter.atualizar(feedbacks)
                        rv.visibility = if (feedbacks.isEmpty()) View.GONE else View.VISIBLE
                        tvVazio.visibility = if (feedbacks.isEmpty()) View.VISIBLE else View.GONE
                    } else {
                        Toast.makeText(this@PersonalAlunoFeedbacksActivity, "Erro ao carregar feedbacks: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<FeedbackBanco>>, t: Throwable) {
                    Toast.makeText(this@PersonalAlunoFeedbacksActivity, "Sem conexao com o servidor", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun mostrarDialogResposta(feedback: FeedbackBanco) {
        val view = layoutInflater.inflate(R.layout.dialog_responder_feedback_personal, null)
        val input = view.findViewById<EditText>(R.id.et_dialog_resposta_personal)
        val tvContexto = view.findViewById<TextView>(R.id.tv_dialog_feedback_contexto)
        val tvObservacao = view.findViewById<TextView>(R.id.tv_dialog_feedback_observacao)
        val btnCancelar = view.findViewById<TextView>(R.id.btn_cancelar_resposta_personal)
        val btnEnviar = view.findViewById<TextView>(R.id.btn_enviar_resposta_personal)

        tvContexto.text = "${feedback.treino_nome} • ${feedback.intensidade} • ${feedback.duracao_min} min"
        tvObservacao.text = if (feedback.observacoes.isBlank()) {
            "Sem comentarios registrados pelo aluno."
        } else {
            "\"${feedback.observacoes}\""
        }
        input.setText(feedback.resposta_personal.orEmpty())
        input.setSelection(input.text.length)

        val dialog = AlertDialog.Builder(this)
            .setView(view)
            .create()

        btnCancelar.setOnClickListener { dialog.dismiss() }
        btnEnviar.setOnClickListener {
            val resposta = input.text.toString().trim()
            if (resposta.isEmpty()) {
                Toast.makeText(this, "Digite uma resposta.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            dialog.dismiss()
            enviarResposta(feedback.id, resposta)
        }

        dialog.setOnShowListener {
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        }
        dialog.show()
    }

    private fun enviarResposta(feedbackId: Int, resposta: String) {
        RetrofitClient.api.responderFeedback("eq.$feedbackId", FeedbackRespostaAtualizar(resposta))
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@PersonalAlunoFeedbacksActivity, "Resposta enviada ao aluno.", Toast.LENGTH_SHORT).show()
                        recreate()
                    } else {
                        Toast.makeText(this@PersonalAlunoFeedbacksActivity, "Erro ao responder: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(this@PersonalAlunoFeedbacksActivity, "Sem conexao com o servidor", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
