# Rollback — Modo Offline + RecyclerView Real

> Criado em: 2026-04-29  
> Objetivo: reverter as alterações de offline mode e integração do RecyclerView caso algo quebre.

---

## Como usar este arquivo

Se algo der errado após as alterações, substitua o conteúdo de cada arquivo pelo código abaixo.  
Os arquivos que **não aparecem aqui não foram tocados** e não precisam de reversão.

---

## 1. `app/src/main/java/com/example/fitconnect/MainActivity.kt`

```kotlin
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
            val intent = Intent(this, CadastroActivity::class.java)
            startActivity(intent)
        }

        textoEsqueciSenha.setOnClickListener {
            val intent = Intent(this, RecuperarSenhaActivity::class.java)
            startActivity(intent)
        }

        botaoEntrar.setOnClickListener {
            val emailDigitado = campoEmail.text.toString().trim()
            val senhaDigitada = campoSenha.text.toString()

            if (emailDigitado.isEmpty() || senhaDigitada.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val buscaEmail = "eq.$emailDigitado"
            val buscaSenha = "eq.$senhaDigitada"

            RetrofitClient.api.fazerLogin(buscaEmail, buscaSenha).enqueue(object : Callback<List<Usuario>> {

                override fun onResponse(call: Call<List<Usuario>>, response: Response<List<Usuario>>) {
                    if (response.isSuccessful) {
                        val listaUsuarios = response.body()

                        if (listaUsuarios != null && listaUsuarios.isNotEmpty()) {
                            Toast.makeText(this@MainActivity, "Login com sucesso! Bem-vindo!", Toast.LENGTH_LONG).show()

                            val usuarioLogado = listaUsuarios[0]
                            val intent = Intent(this@MainActivity, HomeActivity::class.java)
                            intent.putExtra("NOME_USUARIO", usuarioLogado.nome_completo)
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
```

---

## 2. `app/src/main/java/com/example/fitconnect/SupabaseApi.kt`

```kotlin
package com.example.fitconnect

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface SupabaseApi {

    @Headers("Prefer: return=representation")
    @POST("rest/v1/usuarios")
    fun cadastrarUsuario(@Body usuario: Usuario): Call<List<Usuario>>

    @GET("rest/v1/usuarios")
    fun fazerLogin(
        @Query("email") email: String,
        @Query("senha") senha: String
    ): Call<List<Usuario>>

}
```

---

## 3. `app/src/main/java/com/example/fitconnect/TreinosActivity.kt`

```kotlin
package com.example.fitconnect

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class TreinosActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_treinos)

        val btnVoltar = findViewById<ImageView>(R.id.iv_voltar_treinos)
        val btnAdicionar = findViewById<Button>(R.id.btn_adicionar_treino)
        val recyclerView = findViewById<RecyclerView>(R.id.rv_treinos)

        btnVoltar.setOnClickListener { finish() }

        btnAdicionar.setOnClickListener {
            startActivity(Intent(this, CriacaoTreinoActivity::class.java))
        }

        val listaDeTreinos = listOf(
            Treino("Peito e Triceps", "HOJE", "TERÇA-FEIRA", "Intermediário • 6 exercícios • 50 min"),
            Treino("Costas e Biceps", "AMANHÃ", "QUARTA-FEIRA", "Avançado • 8 exercícios • 65 min"),
            Treino("Pernas e Glúteos", "QUINTA", "QUINTA-FEIRA", "Intermediário • 6 exercícios • 60 min")
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = TreinoAdapter(listaDeTreinos)
    }
}
```

---

## 4. `app/src/main/java/com/example/fitconnect/DetalheTreinoActivity.kt`

```kotlin
package com.example.fitconnect

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class DetalheTreinoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalhe_treino)

        val btnVoltar = findViewById<ImageView>(R.id.iv_voltar_detalhe)
        val tvTitulo = findViewById<TextView>(R.id.tv_titulo_detalhe)
        val btnIniciar = findViewById<Button>(R.id.btn_iniciar_treino)
        val recyclerView = findViewById<RecyclerView>(R.id.rv_exercicios_detalhe)

        val nomeTreino = intent.getStringExtra("NOME_TREINO") ?: "Pernas e Glúteos"
        tvTitulo.text = nomeTreino

        btnVoltar.setOnClickListener { finish() }

        btnIniciar.setOnClickListener {
            val intent = Intent(this, ExecucaoTreinoActivity::class.java)
            intent.putExtra("NOME_TREINO", nomeTreino)
            startActivity(intent)
        }

        val listaDeExercicios = listOf(
            Exercicio("Agachamento Livre", "4 séries x 10 repetições"),
            Exercicio("Leg Press 45°", "4 séries x 12 repetições"),
            Exercicio("Cadeira Extensora", "3 séries x 15 repetições"),
            Exercicio("Stiff com Barra", "3 séries x 12 repetições"),
            Exercicio("Elevação Pélvica", "4 séries x 10 repetições"),
            Exercicio("Panturrilha em Pé", "4 séries x 15 repetições")
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = ExercicioDetalheAdapter(listaDeExercicios)
    }
}
```

---

## Arquivos que NÃO foram modificados (não precisam de rollback)

- `TreinoAdapter.kt`
- `ExercicioDetalheAdapter.kt`
- `RetrofitClient.kt`
- `HomeActivity.kt`
- `Usuario.kt`
- `MenuActivity.kt`
- `CriacaoTreinoActivity.kt`
- `EditarTreinoActivity.kt`
- `ExecucaoTreinoActivity.kt`
