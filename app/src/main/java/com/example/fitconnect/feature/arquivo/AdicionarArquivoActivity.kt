package com.example.fitconnect.feature.arquivo

import com.example.fitconnect.R
import com.example.fitconnect.data.model.*
import com.example.fitconnect.core.network.RetrofitClient

import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.io.IOException
import java.net.URLEncoder
import java.util.Locale

class AdicionarArquivoActivity : AppCompatActivity() {

    private val storageBucket = "arquivos"
    private val httpClient = OkHttpClient()
    private var tipoSelecionado = "pdf"
    private var arquivoSelecionadoUri: Uri? = null
    private var nomeSelecionado = ""
    private var tamanhoSelecionadoBytes = 0L
    private lateinit var etNome: EditText
    private lateinit var tvTituloArquivo: TextView
    private lateinit var tvDescricaoArquivo: TextView

    private val seletorArquivo = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri == null) return@registerForActivityResult
        arquivoSelecionadoUri = uri
        nomeSelecionado = obterNomeArquivo(uri)
        tamanhoSelecionadoBytes = obterTamanhoArquivo(uri)

        if (etNome.text.isNullOrBlank()) {
            etNome.setText(nomeSelecionado)
        }
        tipoSelecionado = detectarTipo(uri, nomeSelecionado)
        tvTituloArquivo.text = nomeSelecionado
        tvDescricaoArquivo.text = formatarTamanho(tamanhoSelecionadoBytes)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adicionar_arquivo)

        val ivVoltar = findViewById<ImageView>(R.id.iv_voltar_adicionar)
        val cardFoto = findViewById<LinearLayout>(R.id.card_tipo_foto)
        val cardVideo = findViewById<LinearLayout>(R.id.card_tipo_video)
        val cardDocumento = findViewById<LinearLayout>(R.id.card_tipo_documento)
        val btnSelecionar = findViewById<LinearLayout>(R.id.btn_selecionar_arquivo)
        val btnUpload = findViewById<LinearLayout>(R.id.btn_fazer_upload)

        etNome = findViewById(R.id.et_nome_arquivo)
        tvTituloArquivo = findViewById(R.id.tv_titulo_selecionar_arquivo)
        tvDescricaoArquivo = findViewById(R.id.tv_descricao_arquivo_selecionado)

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
            seletorArquivo.launch("*/*")
        }

        btnUpload.setOnClickListener {
            val uri = arquivoSelecionadoUri
            if (uri == null) {
                Toast.makeText(this, "Selecione um arquivo primeiro.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val nome = etNome.text.toString().trim().ifBlank { nomeSelecionado }
            if (nome.isEmpty()) {
                etNome.error = "Informe o nome do arquivo"
                etNome.requestFocus()
                return@setOnClickListener
            }

            fazerUpload(uri, nome)
        }
    }

    private fun fazerUpload(uri: Uri, nome: String) {
        val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
        if (bytes == null) {
            Toast.makeText(this, "Nao foi possivel ler o arquivo.", Toast.LENGTH_SHORT).show()
            return
        }

        val usuarioId = Sessao.obterUsuarioId(this)
        val nomeSeguro = normalizarNomeArquivo(nome)
        val caminho = "usuarios/$usuarioId/${System.currentTimeMillis()}-$nomeSeguro"
        val mime = contentResolver.getType(uri) ?: "application/octet-stream"
        val uploadUrl = "${RetrofitClient.BASE_URL}storage/v1/object/$storageBucket/${codificarCaminho(caminho)}"

        val request = Request.Builder()
            .url(uploadUrl)
            .addHeader("apikey", RetrofitClient.SUPABASE_KEY)
            .addHeader("Authorization", "Bearer ${RetrofitClient.SUPABASE_KEY}")
            .addHeader("x-upsert", "true")
            .post(RequestBody.create(MediaType.parse(mime), bytes))
            .build()

        Toast.makeText(this, "Enviando arquivo...", Toast.LENGTH_SHORT).show()
        httpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@AdicionarArquivoActivity, "Erro no upload: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (it.isSuccessful) {
                        val publicUrl = "${RetrofitClient.BASE_URL}storage/v1/object/public/$storageBucket/${codificarCaminho(caminho)}"
                        salvarRegistro(nome, publicUrl)
                    } else {
                        val erro = it.body()?.string().orEmpty()
                        runOnUiThread {
                            Toast.makeText(
                                this@AdicionarArquivoActivity,
                                "Erro no upload: ${it.code()}. $erro",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        })
    }

    private fun salvarRegistro(nome: String, arquivoUrl: String) {
        val novo = ArquivoCriacao(
            usuario_id = Sessao.obterUsuarioId(this),
            nome = nome,
            tipo = tipoSelecionado,
            tamanho_kb = (tamanhoSelecionadoBytes / 1024).toInt(),
            arquivo_url = arquivoUrl
        )

        RetrofitClient.api.criarArquivo(novo).enqueue(object : retrofit2.Callback<List<ArquivoBanco>> {
            override fun onResponse(
                call: retrofit2.Call<List<ArquivoBanco>>,
                response: retrofit2.Response<List<ArquivoBanco>>
            ) {
                if (response.isSuccessful) {
                    Toast.makeText(this@AdicionarArquivoActivity, "Arquivo salvo com sucesso!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@AdicionarArquivoActivity, "Upload feito, mas erro ao salvar registro: ${response.code()}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: retrofit2.Call<List<ArquivoBanco>>, t: Throwable) {
                Toast.makeText(this@AdicionarArquivoActivity, "Upload feito, mas sem conexao para salvar registro.", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun obterNomeArquivo(uri: Uri): String {
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0 && cursor.moveToFirst()) {
                return cursor.getString(index)
            }
        }
        return "arquivo"
    }

    private fun obterTamanhoArquivo(uri: Uri): Long {
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (index >= 0 && cursor.moveToFirst()) {
                return cursor.getLong(index)
            }
        }
        return 0L
    }

    private fun detectarTipo(uri: Uri, nome: String): String {
        val mime = contentResolver.getType(uri).orEmpty()
        val nomeLower = nome.lowercase(Locale.getDefault())
        return when {
            mime.startsWith("image/") -> "imagem"
            mime.startsWith("video/") -> "video"
            nomeLower.endsWith(".jpg") || nomeLower.endsWith(".jpeg") || nomeLower.endsWith(".png") || nomeLower.endsWith(".webp") -> "imagem"
            nomeLower.endsWith(".mp4") || nomeLower.endsWith(".mov") || nomeLower.endsWith(".avi") -> "video"
            else -> "pdf"
        }
    }

    private fun normalizarNomeArquivo(nome: String): String {
        return nome
            .trim()
            .replace(Regex("\\s+"), "_")
            .replace(Regex("[^A-Za-z0-9._-]"), "")
            .ifBlank { "arquivo" }
    }

    private fun codificarCaminho(caminho: String): String {
        return caminho.split("/")
            .joinToString("/") { URLEncoder.encode(it, "UTF-8").replace("+", "%20") }
    }

    private fun formatarTamanho(bytes: Long): String {
        if (bytes <= 0L) return "Arquivo selecionado"
        val kb = bytes / 1024.0
        return if (kb >= 1024) {
            String.format(Locale("pt", "BR"), "%.1f MB selecionado", kb / 1024.0)
        } else {
            "${kb.toInt()} KB selecionado"
        }
    }
}
