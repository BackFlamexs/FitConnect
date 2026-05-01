package com.example.fitconnect

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditarTreinoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_treino)

        val btnVoltar = findViewById<ImageView>(R.id.iv_voltar_editar)
        val etNome = findViewById<EditText>(R.id.et_nome_treino_editar)
        val btnSalvar = findViewById<Button>(R.id.btn_salvar_alteracoes)

        val treinoId = intent.getIntExtra("TREINO_ID", 0)
        val nomeTreino = intent.getStringExtra("NOME_TREINO") ?: ""

        // Pré-preenche o campo com o nome atual
        etNome.setText(nomeTreino)

        btnVoltar.setOnClickListener { finish() }

        btnSalvar.setOnClickListener {
            val novoNome = etNome.text.toString().trim()

            if (novoNome.isEmpty()) {
                Toast.makeText(this, "O nome do treino não pode ficar vazio.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            RetrofitClient.api.atualizarTreino("eq.$treinoId", TreinoAtualizar(novoNome))
                .enqueue(object : Callback<Void> {

                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@EditarTreinoActivity, "Treino atualizado!", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(this@EditarTreinoActivity, "Erro: ${response.code()}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Toast.makeText(this@EditarTreinoActivity, "Sem conexão com o servidor", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }
}
