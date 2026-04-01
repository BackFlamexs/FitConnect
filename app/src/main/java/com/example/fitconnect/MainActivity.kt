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

        // Puxando os dados de ID activity_main
        val campoEmail = findViewById<EditText>(R.id.et_email)
        val campoSenha = findViewById<EditText>(R.id.et_password)
        val botaoEntrar = findViewById<Button>(R.id.btn_login)

        // Criei as variaveis para puxar os botoes que irao ser apertados
        val textoCadastreSe = findViewById<TextView>(R.id.tv_sign_up)
        val textoEsqueciSenha = findViewById<TextView>(R.id.tv_forgot_password)

        // Abrindo a tela cadastro, somente se clicar no botao
        textoCadastreSe.setOnClickListener {
            val intent = Intent(this, CadastroActivity::class.java)
            startActivity(intent)
        }

        // Abrindo a tela de recuperacao de senha, somente se clicar no botao
        textoEsqueciSenha.setOnClickListener {
            val intent = Intent(this, RecuperarSenhaActivity::class.java)
            startActivity(intent)
        }

        botaoEntrar.setOnClickListener {
            val emailDigitado = campoEmail.text.toString().trim()
            val senhaDigitada = campoSenha.text.toString()

            // Verificando se o usuario nao deixou nada em branco para nao dar b.o no banco de dados
            if (emailDigitado.isEmpty() || senhaDigitada.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // Para a execução aqui
            }

            // O pulo do gato do Supabase para fazer buscas: adicionar o "eq." (igual a)
            val buscaEmail = "eq.$emailDigitado"
            val buscaSenha = "eq.$senhaDigitada"

            // Usando o retrofit
            RetrofitClient.api.fazerLogin(buscaEmail, buscaSenha).enqueue(object : Callback<List<Usuario>> {

                // Se a internet funcionou e o servidor respondeu
                override fun onResponse(call: Call<List<Usuario>>, response: Response<List<Usuario>>) {
                    if (response.isSuccessful) {
                        val listaUsuarios = response.body()

                        // Aqui nos verificamos os dados do cliente estao corretos ou nao
                        if (listaUsuarios != null && listaUsuarios.isNotEmpty()) {
                            Toast.makeText(this@MainActivity, "Login com sucesso! Bem-vindo!", Toast.LENGTH_LONG).show()

                            // === O CÓDIGO CORRIGIDO ESTÁ AQUI ===

                            // 1. Pegamos o primeiro usuário da lista que o banco de dados retornou
                            val usuarioLogado = listaUsuarios[0]

                            // 2. Criamos a intenção de ir para a HomeActivity
                            val intent = Intent(this@MainActivity, HomeActivity::class.java)

                            // 3. Colocamos o NOME REAL do usuário (que veio do banco) na "mala" para a Home usar
                            intent.putExtra("NOME_USUARIO", usuarioLogado.nome_completo)

                            // 4. Damos a ordem para abrir a tela
                            startActivity(intent)

                            // 5. Destruímos a tela de login para impedir que o usuário volte para ela sem querer apertando a seta de voltar
                            finish()

                        } else {
                            Toast.makeText(this@MainActivity, "E-mail ou senha incorretos.", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(this@MainActivity, "Erro no servidor: ${response.code()}", Toast.LENGTH_LONG).show()
                    }
                }

                // Aqui seria uma validacao de internet, se ele estiver off vai ter esse retorno no APP
                override fun onFailure(call: Call<List<Usuario>>, t: Throwable) {
                    Toast.makeText(this@MainActivity, "Falha na conexão: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
        }
    }
}