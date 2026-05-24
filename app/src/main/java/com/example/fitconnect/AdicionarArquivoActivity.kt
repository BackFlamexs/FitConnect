package com.example.fitconnect

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AdicionarArquivoActivity : AppCompatActivity() {

    private var tipoSelecionado = "pdf"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adicionar_arquivo)

        val ivVoltar = findViewById<ImageView>(R.id.iv_voltar_adicionar)
        val cardFoto = findViewById<LinearLayout>(R.id.card_tipo_foto)
        val cardVideo = findViewById<LinearLayout>(R.id.card_tipo_video)
        val cardDocumento = findViewById<LinearLayout>(R.id.card_tipo_documento)
        val etNome = findViewById<EditText>(R.id.et_nome_arquivo)
        val btnSelecionar = findViewById<LinearLayout>(R.id.btn_selecionar_arquivo)
        val btnUpload = findViewById<LinearLayout>(R.id.btn_fazer_upload)

        ivVoltar.setOnClickListener { finish() }

        fun selecionarTipo(tipo: String, tipoLabel: String, ativo: LinearLayout, outros: List<LinearLayout>) {
            tipoSelecionado = tipo
            ativo.setBackgroundResource(R.drawable.bg_intensidade_selecionado)
            outros.forEach { it.setBackgroundResource(R.drawable.bg_topic_card) }
            if (etNome.text.isNullOrEmpty()) {
                etNome.hint = "Ex: arquivo.$tipoLabel"
            }
        }

        cardFoto.setOnClickListener {
            selecionarTipo("imagem", "jpg", cardFoto, listOf(cardVideo, cardDocumento))
        }
        cardVideo.setOnClickListener {
            selecionarTipo("video", "mp4", cardVideo, listOf(cardFoto, cardDocumento))
        }
        cardDocumento.setOnClickListener {
            selecionarTipo("pdf", "pdf", cardDocumento, listOf(cardFoto, cardVideo))
        }

        btnSelecionar.setOnClickListener {
            Toast.makeText(this, "Seleção de arquivo — em breve", Toast.LENGTH_SHORT).show()
        }

        btnUpload.setOnClickListener {
            val nome = etNome.text.toString().trim()
            if (nome.isEmpty()) {
                etNome.error = "Informe o nome do arquivo"
                etNome.requestFocus()
                return@setOnClickListener
            }

            val usuarioId = Sessao.obterUsuarioId(this)
            val novo = ArquivoCriacao(
                usuario_id = usuarioId,
                nome = nome,
                tipo = tipoSelecionado
            )

            RetrofitClient.api.criarArquivo(novo).enqueue(object : Callback<List<ArquivoBanco>> {
                override fun onResponse(call: Call<List<ArquivoBanco>>, response: Response<List<ArquivoBanco>>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@AdicionarArquivoActivity,
                            "Arquivo salvo com sucesso!", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@AdicionarArquivoActivity,
                            "Erro ao salvar arquivo", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<List<ArquivoBanco>>, t: Throwable) {
                    Toast.makeText(this@AdicionarArquivoActivity,
                        "Sem conexão", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}