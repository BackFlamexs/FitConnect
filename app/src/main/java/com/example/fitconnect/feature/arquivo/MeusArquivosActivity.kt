package com.example.fitconnect.feature.arquivo

import com.example.fitconnect.R
import com.example.fitconnect.data.model.*
import com.example.fitconnect.core.network.RetrofitClient
import com.example.fitconnect.feature.home.HomeActivity
import com.example.fitconnect.feature.home.MenuActivity
import com.example.fitconnect.feature.exercicio.GaleriaExerciciosActivity
import com.example.fitconnect.feature.suporte.AjudaActivity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MeusArquivosActivity : AppCompatActivity() {

    private lateinit var adapter: ArquivoAdapter
    private var todosArquivos = mutableListOf<ArquivoBanco>()
    private var filtroAtual = "Todos"
    private var termoBusca = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meus_arquivos)

        val ivVoltar = findViewById<ImageView>(R.id.iv_voltar_arquivos)
        val etBusca = findViewById<EditText>(R.id.et_busca_arquivos)
        val rv = findViewById<RecyclerView>(R.id.rv_arquivos)
        val fabAdicionar = findViewById<LinearLayout>(R.id.fab_adicionar_arquivo)

        val filtroTodos = findViewById<TextView>(R.id.arq_filtro_todos)
        val filtroVideos = findViewById<TextView>(R.id.arq_filtro_videos)
        val filtroPdfs = findViewById<TextView>(R.id.arq_filtro_pdfs)
        val filtroImagens = findViewById<TextView>(R.id.arq_filtro_imagens)
        val chips = listOf(filtroTodos, filtroVideos, filtroPdfs, filtroImagens)

        val navInicio = findViewById<LinearLayout>(R.id.nav_inicio)
        val navGaleria = findViewById<LinearLayout>(R.id.nav_galeria)
        val navAjuda = findViewById<LinearLayout>(R.id.nav_ajuda)
        val navMenu = findViewById<LinearLayout>(R.id.nav_menu)

        adapter = ArquivoAdapter(todosArquivos) { arq ->
            AlertDialog.Builder(this)
                .setTitle("Excluir arquivo")
                .setMessage("Deseja excluir \"${arq.nome}\"?")
                .setPositiveButton("Excluir") { _, _ -> deletarArquivo(arq) }
                .setNegativeButton("Cancelar", null)
                .show()
        }
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        ivVoltar.setOnClickListener { finish() }

        fun selecionarFiltro(chip: TextView, filtro: String) {
            filtroAtual = filtro
            chips.forEach {
                it.setBackgroundResource(R.drawable.bg_pill_dark)
                it.setTextColor(0xFFA0A0A0.toInt())
            }
            chip.setBackgroundResource(R.drawable.bg_pill_green)
            chip.setTextColor(0xFFFFFFFF.toInt())
            aplicarFiltro()
        }

        filtroTodos.setOnClickListener { selecionarFiltro(filtroTodos, "Todos") }
        filtroVideos.setOnClickListener { selecionarFiltro(filtroVideos, "video") }
        filtroPdfs.setOnClickListener { selecionarFiltro(filtroPdfs, "pdf") }
        filtroImagens.setOnClickListener { selecionarFiltro(filtroImagens, "imagem") }

        etBusca.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                termoBusca = s.toString().trim()
                aplicarFiltro()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        fabAdicionar.setOnClickListener {
            startActivity(Intent(this, AdicionarArquivoActivity::class.java))
        }

        navInicio.setOnClickListener { startActivity(Intent(this, HomeActivity::class.java)); finish() }
        navGaleria.setOnClickListener { startActivity(Intent(this, GaleriaExerciciosActivity::class.java)) }
        navAjuda.setOnClickListener { startActivity(Intent(this, AjudaActivity::class.java)) }
        navMenu.setOnClickListener {
            val i = Intent(this, MenuActivity::class.java)
            i.putExtra("NOME_USUARIO", Sessao.obterNomeUsuario(this))
            startActivity(i)
        }
    }

    override fun onResume() {
        super.onResume()
        carregarArquivos()
    }

    private fun carregarArquivos() {
        val usuarioId = Sessao.obterUsuarioId(this)
        RetrofitClient.api.buscarArquivos("eq.$usuarioId").enqueue(object : Callback<List<ArquivoBanco>> {
            override fun onResponse(call: Call<List<ArquivoBanco>>, response: Response<List<ArquivoBanco>>) {
                if (response.isSuccessful) {
                    todosArquivos = (response.body() ?: emptyList()).toMutableList()
                    aplicarFiltro()
                }
            }
            override fun onFailure(call: Call<List<ArquivoBanco>>, t: Throwable) {
                Toast.makeText(this@MeusArquivosActivity, "Sem conexão", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun deletarArquivo(arq: ArquivoBanco) {
        RetrofitClient.api.deletarArquivo("eq.${arq.id}").enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                todosArquivos.remove(arq)
                aplicarFiltro()
                Toast.makeText(this@MeusArquivosActivity, "Arquivo excluído", Toast.LENGTH_SHORT).show()
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@MeusArquivosActivity, "Erro ao excluir", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun aplicarFiltro() {
        val resultado = todosArquivos.filter { arq ->
            val matchFiltro = filtroAtual == "Todos" || arq.tipo == filtroAtual
            val matchBusca = termoBusca.isEmpty() ||
                arq.nome.contains(termoBusca, ignoreCase = true)
            matchFiltro && matchBusca
        }
        adapter.atualizar(resultado)
    }
}
