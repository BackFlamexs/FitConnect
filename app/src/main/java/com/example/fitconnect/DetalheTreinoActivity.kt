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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalhe_treino)

        val btnVoltar = findViewById<ImageView>(R.id.iv_voltar_detalhe)
        val tvTitulo = findViewById<TextView>(R.id.tv_titulo_detalhe)
        val btnIniciar = findViewById<Button>(R.id.btn_iniciar_treino)
        val recyclerView = findViewById<RecyclerView>(R.id.rv_exercicios_detalhe)

        val nomeTreino = intent.getStringExtra("NOME_TREINO") ?: ""
        val treinoId = intent.getIntExtra("TREINO_ID", 0)

        tvTitulo.text = nomeTreino

        btnVoltar.setOnClickListener { finish() }

        btnIniciar.setOnClickListener {
            val intent = Intent(this, ExecucaoTreinoActivity::class.java)
            intent.putExtra("NOME_TREINO", nomeTreino)
            startActivity(intent)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)

        RetrofitClient.api.buscarExercicios("eq.$treinoId").enqueue(object : Callback<List<ExercicioBanco>> {

            override fun onResponse(call: Call<List<ExercicioBanco>>, response: Response<List<ExercicioBanco>>) {
                if (response.isSuccessful) {
                    val exercicios = response.body()?.map { banco ->
                        Exercicio(
                            nome = banco.nome,
                            seriesReps = banco.series_reps
                        )
                    } ?: emptyList()

                    recyclerView.adapter = ExercicioDetalheAdapter(exercicios)
                } else {
                    Toast.makeText(this@DetalheTreinoActivity, "Erro ao buscar exercícios: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<ExercicioBanco>>, t: Throwable) {
                Toast.makeText(this@DetalheTreinoActivity, "Sem conexão com o servidor", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
