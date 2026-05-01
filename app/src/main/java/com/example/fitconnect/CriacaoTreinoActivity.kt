package com.example.fitconnect

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
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

    private var diaSelecionado: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_criacao_treino)

        val btnVoltar = findViewById<ImageView>(R.id.iv_voltar_criacao)
        val etNome = findViewById<EditText>(R.id.et_nome_treino_criar)
        val tvDia = findViewById<TextView>(R.id.tv_dia_semana_criar)
        val btnSalvar = findViewById<Button>(R.id.btn_salvar_treino_criacao)

        btnVoltar.setOnClickListener { finish() }

        // Abre o seletor de dia ao clicar
        tvDia.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Selecione o dia")
                .setItems(dias) { _, index ->
                    diaSelecionado = dias[index]
                    tvDia.text = diaSelecionado
                    tvDia.setTextColor(getColor(android.R.color.white))
                }
                .show()
        }

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

            val (tag, diaSemana) = tagsMap[diaSelecionado!!]!!
            val usuarioId = Sessao.obterUsuarioId(this)

            val novoTreino = TreinoCriacao(
                usuario_id = usuarioId,
                nome = nome,
                tag_dia = tag,
                dia_semana = diaSemana,
                detalhes = "Personalizado"
            )

            RetrofitClient.api.criarTreino(novoTreino).enqueue(object : Callback<List<TreinoBanco>> {

                override fun onResponse(call: Call<List<TreinoBanco>>, response: Response<List<TreinoBanco>>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@CriacaoTreinoActivity, "Treino criado!", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        val erro = response.errorBody()?.string() ?: "sem detalhe"
                        Toast.makeText(this@CriacaoTreinoActivity, "${response.code()}: $erro", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<List<TreinoBanco>>, t: Throwable) {
                    Toast.makeText(this@CriacaoTreinoActivity, "Sem conexão com o servidor", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
