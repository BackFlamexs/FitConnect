package com.example.fitconnect

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar
import java.util.Locale

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
        val campoDataNascimento = findViewById<EditText>(R.id.et_data_nascimento_cadastro)
        val grupoGenero = findViewById<RadioGroup>(R.id.rg_genero)
        val grupoTipoConta = findViewById<RadioGroup>(R.id.rg_tipo_conta)
        val botaoCadastrar = findViewById<Button>(R.id.btn_cadastrar)
        val tipoContaInicial = intent.getStringExtra("ACCOUNT_TYPE") ?: "student"

        grupoTipoConta.check(
            if (tipoContaInicial == "personal") R.id.rb_tipo_personal else R.id.rb_tipo_aluno
        )

        campoDataNascimento.setOnClickListener {
            mostrarSeletorData(campoDataNascimento)
        }

        botaoCadastrar.setOnClickListener {

            // Estou extraindo tudo aqui para poder fazer a logica
            val nome = campoNome.text.toString().trim()
            val usuario = campoUsuario.text.toString().trim()
            val email = campoEmail.text.toString().trim().lowercase(Locale.ROOT)
            val confirmaEmail = campoConfirmaEmail.text.toString().trim().lowercase(Locale.ROOT)
            val senha = campoSenha.text.toString()
            val telefone = campoTelefone.text.toString().trim()
            val dataNascimento = campoDataNascimento.tag as? String

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
            val accountType = when (grupoTipoConta.checkedRadioButtonId) {
                R.id.rb_tipo_personal -> "personal"
                else -> "student"
            }
            if (accountType == "student" && dataNascimento.isNullOrBlank()) {
                Toast.makeText(this, "Informe a data de nascimento do aluno.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Estou mandando todos os dados para o Usuario para subir para o supabase
            val novoUsuario = UsuarioCriacao(nome, usuario, email, senha, telefone, generoSelecionado, accountType, dataNascimento)

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

    private fun mostrarSeletorData(campo: EditText) {
        val calendario = Calendar.getInstance().apply {
            add(Calendar.YEAR, -18)
        }
        DatePickerDialog(
            this,
            { _, ano, mes, dia ->
                val dataBanco = "%04d-%02d-%02d".format(ano, mes + 1, dia)
                val dataTela = "%02d/%02d/%04d".format(dia, mes + 1, ano)
                campo.setText(dataTela)
                campo.tag = dataBanco
            },
            calendario.get(Calendar.YEAR),
            calendario.get(Calendar.MONTH),
            calendario.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
}
