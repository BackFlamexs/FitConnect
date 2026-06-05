package com.example.fitconnect.feature.personal

import com.example.fitconnect.R
import com.example.fitconnect.data.model.*
import com.example.fitconnect.core.network.RetrofitClient
import com.example.fitconnect.feature.treino.CriacaoTreinoActivity
import com.example.fitconnect.feature.treino.TreinoAdapter
import com.example.fitconnect.feature.treino.Treino

import android.content.Intent
import android.os.Bundle
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

class PersonalAlunoTreinosActivity : AppCompatActivity() {

    private lateinit var rvTreinos: RecyclerView
    private var alunoId = 0
    private var alunoNome = "Aluno"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal_aluno_treinos)

        alunoId = intent.getIntExtra("ALUNO_ID", 0)
        alunoNome = intent.getStringExtra("ALUNO_NOME") ?: "Aluno"

        rvTreinos = findViewById(R.id.rv_treinos_aluno_personal)
        rvTreinos.layoutManager = LinearLayoutManager(this)

        findViewById<ImageView>(R.id.iv_voltar_treinos_aluno).setOnClickListener { finish() }
        findViewById<TextView>(R.id.tv_titulo_treinos_aluno).text = "Treinos de $alunoNome"
        findViewById<Button>(R.id.btn_montar_treino_aluno).setOnClickListener {
            if (alunoId == 0) {
                Toast.makeText(this, "Aluno invalido.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val intent = Intent(this, CriacaoTreinoActivity::class.java)
            intent.putExtra("USUARIO_ID_DESTINO", alunoId)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        carregarTreinos()
    }

    private fun carregarTreinos() {
        if (alunoId == 0) return
        RetrofitClient.api.buscarTreinosPorUsuario("eq.$alunoId")
            .enqueue(object : Callback<List<TreinoBanco>> {
                override fun onResponse(call: Call<List<TreinoBanco>>, response: Response<List<TreinoBanco>>) {
                    if (response.isSuccessful) {
                        val treinos = response.body()?.map { banco ->
                            Treino(
                                id = banco.id,
                                nome = banco.nome,
                                tagDia = banco.tag_dia,
                                diaSemana = banco.dia_semana,
                                detalhes = banco.detalhes
                            )
                        } ?: emptyList()
                        rvTreinos.adapter = TreinoAdapter(
                            treinos,
                            { treino -> confirmarExclusao(treino) },
                            permitirGerenciar = true
                        )
                    } else {
                        Toast.makeText(this@PersonalAlunoTreinosActivity, "Erro ao carregar treinos: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<TreinoBanco>>, t: Throwable) {
                    Toast.makeText(this@PersonalAlunoTreinosActivity, "Sem conexao com o servidor", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun confirmarExclusao(treino: Treino) {
        AlertDialog.Builder(this)
            .setTitle("Excluir treino")
            .setMessage("Deseja excluir \"${treino.nome}\" de $alunoNome?")
            .setPositiveButton("Excluir") { _, _ ->
                RetrofitClient.api.deletarTreino("eq.${treino.id}").enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        carregarTreinos()
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Toast.makeText(this@PersonalAlunoTreinosActivity, "Erro ao excluir treino", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
