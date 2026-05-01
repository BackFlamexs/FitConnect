package com.example.fitconnect

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TreinosActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_treinos)

        val btnVoltar = findViewById<ImageView>(R.id.iv_voltar_treinos)
        val btnAdicionar = findViewById<Button>(R.id.btn_adicionar_treino)
        recyclerView = findViewById(R.id.rv_treinos)

        btnVoltar.setOnClickListener { finish() }
        btnAdicionar.setOnClickListener {
            startActivity(Intent(this, CriacaoTreinoActivity::class.java))
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    override fun onResume() {
        super.onResume()
        carregarTreinos()
    }

    private fun carregarTreinos() {
        RetrofitClient.api.buscarTreinos().enqueue(object : Callback<List<TreinoBanco>> {

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
                    recyclerView.adapter = TreinoAdapter(treinos) { treino ->
                        androidx.appcompat.app.AlertDialog.Builder(this@TreinosActivity)
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
                    }
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
