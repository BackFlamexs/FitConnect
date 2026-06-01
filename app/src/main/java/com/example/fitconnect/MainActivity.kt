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
import java.util.Locale

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val campoEmail = findViewById<EditText>(R.id.et_email)
        val campoSenha = findViewById<EditText>(R.id.et_password)
        val botaoEntrar = findViewById<Button>(R.id.btn_login)
        val botaoAluno = findViewById<Button>(R.id.btn_tipo_aluno)
        val botaoPersonal = findViewById<Button>(R.id.btn_tipo_personal)
        val textoCadastreSe = findViewById<TextView>(R.id.tv_sign_up)
        val textoEsqueciSenha = findViewById<TextView>(R.id.tv_forgot_password)
        var tipoContaSelecionado: String? = null

        fun selecionarTipoConta(tipo: String) {
            tipoContaSelecionado = tipo
            val alunoSelecionado = tipo == "student"
            botaoAluno.setBackgroundResource(if (alunoSelecionado) R.drawable.bg_btn_green else R.drawable.bg_btn_green_outline)
            botaoPersonal.setBackgroundResource(if (alunoSelecionado) R.drawable.bg_btn_green_outline else R.drawable.bg_btn_green)
            botaoAluno.setTextColor(if (alunoSelecionado) 0xFFFFFFFF.toInt() else 0xFF4CAF50.toInt())
            botaoPersonal.setTextColor(if (alunoSelecionado) 0xFF4CAF50.toInt() else 0xFFFFFFFF.toInt())
        }

        botaoAluno.setOnClickListener { selecionarTipoConta("student") }
        botaoPersonal.setOnClickListener { selecionarTipoConta("personal") }

        textoCadastreSe.setOnClickListener {
            val intent = Intent(this, CadastroActivity::class.java)
            tipoContaSelecionado?.let { intent.putExtra("ACCOUNT_TYPE", it) }
            startActivity(intent)
        }

        textoEsqueciSenha.setOnClickListener {
            startActivity(Intent(this, RecuperarSenhaActivity::class.java))
        }

        botaoEntrar.setOnClickListener {
            val emailDigitado = campoEmail.text.toString().trim().lowercase(Locale.ROOT)
            val senhaDigitada = campoSenha.text.toString()
            val tipoConta = tipoContaSelecionado

            if (emailDigitado.isEmpty() || senhaDigitada.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (tipoConta == null) {
                Toast.makeText(this, "Escolha se voce e Aluno ou Personal Trainer.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            RetrofitClient.api.fazerLogin("eq.$emailDigitado", "eq.$senhaDigitada", "eq.$tipoConta")
                .enqueue(object : Callback<List<Usuario>> {

                    override fun onResponse(call: Call<List<Usuario>>, response: Response<List<Usuario>>) {
                        if (response.isSuccessful) {
                            val lista = response.body()
                            if (!lista.isNullOrEmpty()) {
                                val usuario = lista[0]
                                val nomeCompleto = usuario.nome_completo.orEmpty().ifEmpty { "Atleta" }
                                val nomeUsuario = usuario.nome_usuario.orEmpty().ifEmpty { nomeCompleto }
                                val emailUsuario = usuario.email.orEmpty().ifEmpty { emailDigitado }
                                val fotoUrl = usuario.foto_url.orEmpty().ifEmpty {
                                    Sessao.obterFotoUrl(this@MainActivity)
                                }
                                val peso = usuario.peso ?: Sessao.obterPeso(this@MainActivity)
                                val altura = usuario.altura ?: Sessao.obterAltura(this@MainActivity)
                                val accountType = usuario.account_type.orEmpty().ifEmpty { tipoConta }
                                val dataNascimento = usuario.data_nascimento.orEmpty()
                                val biografia = usuario.biografia.orEmpty()

                                Sessao.salvar(
                                    this@MainActivity,
                                    emailUsuario,
                                    usuario.id,
                                    nomeCompleto,
                                    fotoUrl,
                                    nomeUsuario,
                                    peso,
                                    altura,
                                    accountType,
                                    dataNascimento,
                                    biografia
                                )

                                val destino = if (accountType == "personal") {
                                    PersonalHomeActivity::class.java
                                } else {
                                    HomeActivity::class.java
                                }
                                val intent = Intent(this@MainActivity, destino)
                                intent.putExtra("NOME_USUARIO", nomeUsuario)
                                startActivity(intent)
                                finish()
                            } else {
                                Toast.makeText(this@MainActivity, "E-mail, senha ou tipo de conta incorretos.", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            Toast.makeText(this@MainActivity, "Erro no servidor: ${response.code()}", Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun onFailure(call: Call<List<Usuario>>, t: Throwable) {
                        Toast.makeText(this@MainActivity, "Falha na conexao: ${t.message}", Toast.LENGTH_LONG).show()
                    }
                })
        }
    }
}
