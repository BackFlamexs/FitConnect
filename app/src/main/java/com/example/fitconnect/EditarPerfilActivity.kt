package com.example.fitconnect

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditarPerfilActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_perfil)

        val btnVoltar = findViewById<ImageView>(R.id.iv_voltar_editar_perfil)
        val etNome = findViewById<EditText>(R.id.et_nome_editar)
        val etEmail = findViewById<EditText>(R.id.et_email_editar)
        val etSenha = findViewById<EditText>(R.id.et_senha_editar)
        val btnSalvar = findViewById<Button>(R.id.btn_salvar_perfil)
        val llSucesso = findViewById<LinearLayout>(R.id.ll_sucesso_salvar)

        // Pré-preenche com os dados da sessão
        etNome.setText(Sessao.obterNome(this))
        etEmail.setText(Sessao.obterEmail(this))

        btnVoltar.setOnClickListener { finish() }

        btnSalvar.setOnClickListener {
            val nome = etNome.text.toString().trim()
            val senha = etSenha.text.toString()
            val emailSessao = Sessao.obterEmail(this)

            if (nome.isEmpty()) {
                Toast.makeText(this, "O nome não pode ficar vazio.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (emailSessao.isEmpty()) {
                Toast.makeText(this, "Sessão expirada. Faça login novamente.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val dadosAtualizados = UsuarioAtualizar(
                nome_completo = nome,
                senha = if (senha.isNotEmpty()) senha else ""
            )

            RetrofitClient.api.atualizarUsuario("eq.$emailSessao", dadosAtualizados)
                .enqueue(object : Callback<Void> {

                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) {
                            // Atualiza o nome na sessão local
                            Sessao.salvar(this@EditarPerfilActivity, emailSessao, Sessao.obterUsuarioId(this@EditarPerfilActivity), nome)
                            llSucesso.visibility = View.VISIBLE
                        } else {
                            Toast.makeText(this@EditarPerfilActivity, "Erro: ${response.code()}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Toast.makeText(this@EditarPerfilActivity, "Sem conexão com o servidor", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }
}
