package com.example.fitconnect.feature.exercicio

import com.example.fitconnect.R
import com.example.fitconnect.data.model.*
import com.example.fitconnect.core.network.RetrofitClient
import com.example.fitconnect.feature.home.HomeActivity
import com.example.fitconnect.feature.home.MenuActivity
import com.example.fitconnect.feature.suporte.AjudaActivity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitconnect.feature.pagamento.PagamentoProActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GaleriaExerciciosActivity : AppCompatActivity() {

    private lateinit var adapter: ExercicioGaleriaAdapter
    private var todosExercicios = listOf<GaleriaExercicioBanco>()
    private var exerciciosVisiveis = listOf<GaleriaExercicioBanco>()
    private var categoriaAtual = "Todos"
    private var termoBusca = ""
    private var isFreeUser = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_galeria_exercicios)

        val isPro = Sessao.obterPro(this)
        val isPersonal = Sessao.obterAccountType(this) == "personal"
        isFreeUser = !isPro && !isPersonal

        val ivVoltar = findViewById<ImageView>(R.id.iv_voltar_galeria)
        val etBusca = findViewById<EditText>(R.id.et_busca_galeria)
        val rv = findViewById<RecyclerView>(R.id.rv_exercicios_galeria)
        val llBannerFree = findViewById<LinearLayout>(R.id.ll_banner_free)
        val tvBannerAssinar = findViewById<TextView>(R.id.tv_banner_assinar)

        if (isFreeUser) {
            llBannerFree.visibility = View.VISIBLE
            tvBannerAssinar.setOnClickListener {
                startActivity(Intent(this, PagamentoProActivity::class.java))
            }
        }

        val catTodos = findViewById<TextView>(R.id.cat_todos)
        val catPeito = findViewById<TextView>(R.id.cat_peito)
        val catCostas = findViewById<TextView>(R.id.cat_costas)
        val catPernas = findViewById<TextView>(R.id.cat_pernas)
        val catBiceps = findViewById<TextView>(R.id.cat_biceps)
        val catCardio = findViewById<TextView>(R.id.cat_cardio)
        val catOmbros = findViewById<TextView>(R.id.cat_ombros)
        val catTriceps = findViewById<TextView>(R.id.cat_triceps)
        val chips = listOf(catTodos, catPeito, catCostas, catPernas, catBiceps, catCardio, catOmbros, catTriceps)

        val navInicio = findViewById<LinearLayout>(R.id.nav_inicio)
        val navGaleria = findViewById<LinearLayout>(R.id.nav_galeria)
        val navAjuda = findViewById<LinearLayout>(R.id.nav_ajuda)
        val navMenu = findViewById<LinearLayout>(R.id.nav_menu)

        adapter = ExercicioGaleriaAdapter(emptyList()) { exercicio ->
            val intent = Intent(this, DetalheGaleriaExercicioActivity::class.java).apply {
                putExtra("EXERCICIO_NOME", exercicio.nome)
                putExtra("EXERCICIO_CATEGORIA", exercicio.categoria)
                putExtra("EXERCICIO_DIFICULDADE", exercicio.dificuldade)
                putExtra("EXERCICIO_EQUIPAMENTO", exercicio.equipamento)
                putExtra("EXERCICIO_GIF_URL", exercicio.gif_url)
                putExtra("EXERCICIO_INSTRUCOES", exercicio.instrucoes)
            }
            startActivity(intent)
        }
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        ivVoltar.setOnClickListener { finish() }

        fun selecionarCategoria(chip: TextView, categoria: String) {
            categoriaAtual = categoria
            chips.forEach {
                it.setBackgroundResource(R.drawable.bg_pill_dark)
                it.setTextColor(0xFFA0A0A0.toInt())
            }
            chip.setBackgroundResource(R.drawable.bg_pill_green)
            chip.setTextColor(0xFFFFFFFF.toInt())
            aplicarFiltro()
        }

        catTodos.setOnClickListener { selecionarCategoria(catTodos, "Todos") }
        catPeito.setOnClickListener { selecionarCategoria(catPeito, "Peito") }
        catCostas.setOnClickListener { selecionarCategoria(catCostas, "Costas") }
        catPernas.setOnClickListener { selecionarCategoria(catPernas, "Pernas") }
        catBiceps.setOnClickListener { selecionarCategoria(catBiceps, "Bíceps") }
        catCardio.setOnClickListener { selecionarCategoria(catCardio, "Cardio") }
        catOmbros.setOnClickListener { selecionarCategoria(catOmbros, "Ombros") }
        catTriceps.setOnClickListener { selecionarCategoria(catTriceps, "Tríceps") }

        etBusca.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                termoBusca = s.toString().trim()
                aplicarFiltro()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        navInicio.setOnClickListener { startActivity(Intent(this, HomeActivity::class.java)); finish() }
        navGaleria.setOnClickListener { }
        navAjuda.setOnClickListener { startActivity(Intent(this, AjudaActivity::class.java)) }
        navMenu.setOnClickListener {
            val i = Intent(this, MenuActivity::class.java)
            i.putExtra("NOME_USUARIO", Sessao.obterNomeUsuario(this))
            startActivity(i)
        }

        carregarExercicios()
    }

    private fun carregarExercicios() {
        RetrofitClient.api.buscarGaleriaExercicios().enqueue(object : Callback<List<GaleriaExercicioBanco>> {
            override fun onResponse(call: Call<List<GaleriaExercicioBanco>>, response: Response<List<GaleriaExercicioBanco>>) {
                if (response.isSuccessful) {
                    todosExercicios = removerDuplicados(response.body() ?: emptyList())
                    exerciciosVisiveis = if (isFreeUser) filtrarParaFree(todosExercicios) else todosExercicios
                    aplicarFiltro()
                } else {
                    Toast.makeText(this@GaleriaExerciciosActivity,
                        "Erro ao carregar exercícios", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<List<GaleriaExercicioBanco>>, t: Throwable) {
                Toast.makeText(this@GaleriaExerciciosActivity,
                    "Sem conexão com o servidor", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun removerDuplicados(lista: List<GaleriaExercicioBanco>): List<GaleriaExercicioBanco> {
        return lista
            .filter { it.nome.isNotBlank() }
            .distinctBy { it.nome.trim().lowercase() }
    }

    private fun filtrarParaFree(lista: List<GaleriaExercicioBanco>): List<GaleriaExercicioBanco> {
        val categorias = listOf("Peito", "Costas", "Pernas", "Bíceps", "Cardio", "Ombros", "Tríceps")
        return categorias.mapNotNull { cat -> lista.firstOrNull { it.categoria == cat } }
    }

    private fun aplicarFiltro() {
        val base = exerciciosVisiveis
        val resultado = base.filter { ex ->
            val matchCategoria = categoriaAtual == "Todos" || ex.categoria == categoriaAtual
            val matchBusca = termoBusca.isEmpty() ||
                ex.nome.contains(termoBusca, ignoreCase = true) ||
                ex.categoria.contains(termoBusca, ignoreCase = true) ||
                ex.equipamento.contains(termoBusca, ignoreCase = true)
            matchCategoria && matchBusca
        }
        adapter.atualizar(resultado)
    }
}
