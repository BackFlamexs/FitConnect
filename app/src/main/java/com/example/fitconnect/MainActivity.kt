package com.example.fitconnect

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val campoEmail = findViewById<EditText>(R.id.et_email)
        val campoSenha = findViewById<EditText>(R.id.et_password)
        val botaoEntrar = findViewById<Button>(R.id.btn_login)
        val textoCadastreSe = findViewById<TextView>(R.id.tv_sign_up)
        val textoEsqueciSenha = findViewById<TextView>(R.id.tv_forgot_password)

        textoCadastreSe.setOnClickListener {
            startActivity(Intent(this, CadastroActivity::class.java))
        }

        textoEsqueciSenha.setOnClickListener {
            startActivity(Intent(this, RecuperarSenhaActivity::class.java))
        }

        botaoEntrar.setOnClickListener {
            val emailDigitado = campoEmail.text.toString().trim()
            val senhaDigitada = campoSenha.text.toString()

            if (emailDigitado.isEmpty() || senhaDigitada.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            RetrofitClient.api.fazerLogin("eq.$emailDigitado", "eq.$senhaDigitada")
                .enqueue(object : Callback<List<Usuario>> {

                    override fun onResponse(call: Call<List<Usuario>>, response: Response<List<Usuario>>) {
                        if (response.isSuccessful) {
                            val lista = response.body()
                            if (!lista.isNullOrEmpty()) {
                                val usuario = lista[0]

                                // Salva sessão para uso em qualquer tela
                                Sessao.salvar(this@MainActivity, usuario.email, usuario.id, usuario.nome_completo)

                                val intent = Intent(this@MainActivity, HomeActivity::class.java)
                                intent.putExtra("NOME_USUARIO", usuario.nome_completo)
                                startActivity(intent)
                                finish()
                            } else {
                                Toast.makeText(this@MainActivity, "E-mail ou senha incorretos.", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            Toast.makeText(this@MainActivity, "Erro no servidor: ${response.code()}", Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun onFailure(call: Call<List<Usuario>>, t: Throwable) {
                        Toast.makeText(this@MainActivity, "Falha na conexão: ${t.message}", Toast.LENGTH_LONG).show()
                    }
                })
        }
    }
}
