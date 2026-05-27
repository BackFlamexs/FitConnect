package com.example.fitconnect

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HistoricoFeedbacksActivity : AppCompatActivity() {

    private lateinit var adapter: FeedbackHistoricoAdapter
    private var todosFeedbacks = listOf<FeedbackBanco>()
    private var filtroAtual = "Todos"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historico_feedbacks)

        val ivVoltar = findViewById<ImageView>(R.id.iv_voltar_historico)
        val rv = findViewById<RecyclerView>(R.id.rv_feedbacks)
        val tvVazio = findViewById<TextView>(R.id.tv_vazio)
        val navInicio = findViewById<LinearLayout>(R.id.nav_inicio)
        val navGaleria = findViewById<LinearLayout>(R.id.nav_galeria)
        val navAjuda = findViewById<LinearLayout>(R.id.nav_ajuda)
        val navMenu = findViewById<LinearLayout>(R.id.nav_menu)

        val filtroTodos = findViewById<TextView>(R.id.filtro_todos)
        val filtroIntenso = findViewById<TextView>(R.id.filtro_intenso)
        val filtroModerado = findViewById<TextView>(R.id.filtro_moderado)
        val filtroFacil = findViewById<TextView>(R.id.filtro_facil)
        val filtros = listOf(filtroTodos, filtroIntenso, filtroModerado, filtroFacil)

        adapter = FeedbackHistoricoAdapter(emptyList())
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        ivVoltar.setOnClickListener { finish() }

        fun aplicarFiltro(filtro: String, tvAtivo: TextView) {
            filtroAtual = filtro
            filtros.forEach {
                it.setBackgroundResource(R.drawable.bg_pill_dark)
                it.setTextColor(0xFFA0A0A0.toInt())
            }
            tvAtivo.setBackgroundResource(R.drawable.bg_pill_green)
            tvAtivo.setTextColor(0xFFFFFFFF.toInt())

            val filtrado = when (filtro) {
                "Intenso" -> todosFeedbacks.filter { it.intensidade == "Intenso" }
                "Moderado" -> todosFeedbacks.filter { it.intensidade == "Moderado" }
                "Fácil" -> todosFeedbacks.filter { it.intensidade == "Leve" }
                else -> todosFeedbacks
            }
            adapter.atualizar(filtrado)
            rv.visibility = if (filtrado.isEmpty()) View.GONE else View.VISIBLE
            tvVazio.visibility = if (filtrado.isEmpty()) View.VISIBLE else View.GONE
        }

        filtroTodos.setOnClickListener { aplicarFiltro("Todos", filtroTodos) }
        filtroIntenso.setOnClickListener { aplicarFiltro("Intenso", filtroIntenso) }
        filtroModerado.setOnClickListener { aplicarFiltro("Moderado", filtroModerado) }
        filtroFacil.setOnClickListener { aplicarFiltro("Fácil", filtroFacil) }

        navInicio.setOnClickListener { startActivity(Intent(this, HomeActivity::class.java)); finish() }
        navGaleria.setOnClickListener { startActivity(Intent(this, GaleriaExerciciosActivity::class.java)) }
        navAjuda.setOnClickListener { startActivity(Intent(this, AjudaActivity::class.java)) }
        navMenu.setOnClickListener {
            val i = Intent(this, MenuActivity::class.java)
            i.putExtra("NOME_USUARIO", Sessao.obterNomeUsuario(this))
            startActivity(i)
        }

        carregarFeedbacks(rv, tvVazio)
    }

    private fun carregarFeedbacks(rv: RecyclerView, tvVazio: TextView) {
        val usuarioId = Sessao.obterUsuarioId(this)
        RetrofitClient.api.buscarFeedbacks("eq.$usuarioId").enqueue(object : Callback<List<FeedbackBanco>> {
            override fun onResponse(call: Call<List<FeedbackBanco>>, response: Response<List<FeedbackBanco>>) {
                if (response.isSuccessful) {
                    todosFeedbacks = response.body() ?: emptyList()
                    adapter.atualizar(todosFeedbacks)
                    rv.visibility = if (todosFeedbacks.isEmpty()) View.GONE else View.VISIBLE
                    tvVazio.visibility = if (todosFeedbacks.isEmpty()) View.VISIBLE else View.GONE
                } else {
                    tvVazio.visibility = View.VISIBLE
                    rv.visibility = View.GONE
                }
            }
            override fun onFailure(call: Call<List<FeedbackBanco>>, t: Throwable) {
                Toast.makeText(this@HistoricoFeedbacksActivity,
                    "Erro ao carregar feedbacks", Toast.LENGTH_SHORT).show()
                tvVazio.visibility = View.VISIBLE
                rv.visibility = View.GONE
            }
        })
    }
}
