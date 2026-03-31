package com.example.fitconnect

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CadastroActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro)
        // Puxando os dados do activity_Cadastro
        val campoNome = findViewById<EditText>(R.id.et_nome_completo)
        val campoUsuario = findViewById<EditText>(R.id.et_nome_usuario)
        val campoEmail = findViewById<EditText>(R.id.et_email_cadastro)
        val campoConfirmaEmail = findViewById<EditText>(R.id.et_confirmar_email)
        val campoSenha = findViewById<EditText>(R.id.et_senha_cadastro)
        val campoTelefone = findViewById<EditText>(R.id.et_telefone)
        val grupoGenero = findViewById<RadioGroup>(R.id.rg_genero)
        val botaoCadastrar = findViewById<Button>(R.id.btn_cadastrar)

        botaoCadastrar.setOnClickListener {

            // Estou extraindo tudo aqui para poder fazer a logica
            val nome = campoNome.text.toString().trim()
            val usuario = campoUsuario.text.toString().trim()
            val email = campoEmail.text.toString().trim()
            val confirmaEmail = campoConfirmaEmail.text.toString().trim()
            val senha = campoSenha.text.toString()
            val telefone = campoTelefone.text.toString().trim()

            // Validando se o usuario colocou todos os dados que eu estou pedindo para ele
            if (nome.isEmpty() || usuario.isEmpty() || email.isEmpty() || senha.isEmpty() || telefone.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos obrigatórios!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // Para a execução aqui se faltar algo
            }
            // Confirmacao de email para ver se ele esta batendo com o email anterior que ele escreveu
            if (email != confirmaEmail) {
                Toast.makeText(this, "Os e-mails não coincidem!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // Criterio da senha se seguranca para nao ter somente 1 caractere, no momento esta 3 para base de teste. Mas depois vou mudar o tamanho para 8 que seria o normal
            if (senha.length < 3) {
                Toast.makeText(this, "A senha deve ter pelo menos 3 caracteres.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // Genero
            val idSelecionado = grupoGenero.checkedRadioButtonId
            val generoSelecionado = when (idSelecionado) {
                R.id.rb_masculino -> "Masculino"
                R.id.rb_feminino -> "Feminino"
                else -> "Não informado"
            }

            // Estou mandando todos os dados para o Usuario para subir para o supabase
            val novoUsuario = Usuario(nome, usuario, email, senha, telefone, generoSelecionado)

            // 6. Estou enviando para a nuvem
            RetrofitClient.api.cadastrarUsuario(novoUsuario).enqueue(object : Callback<List<Usuario>> {

                override fun onResponse(call: Call<List<Usuario>>, response: Response<List<Usuario>>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@CadastroActivity, "Conta criada com sucesso!", Toast.LENGTH_LONG).show()

                        // O SEGREDO DA BOA NAVEGAÇÃO:
                        // Destrói este ecrã de cadastro. Como o ecrã de Login estava por baixo,
                        // o utilizador volta imediatamente para lá sem gastar memória extra.
                        finish()

                    } else {
                        Toast.makeText(this@CadastroActivity, "Erro ao criar conta: ${response.code()}", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<List<Usuario>>, t: Throwable) {
                    Toast.makeText(this@CadastroActivity, "Erro de ligação: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
        }

        val botaoVoltar = findViewById<Button>(R.id.btn_voltar_menu)
        botaoVoltar.setOnClickListener {
            finish()
        }
    }
}