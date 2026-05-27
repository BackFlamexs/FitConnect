package com.example.fitconnect

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.util.Locale

class EditarPerfilActivity : AppCompatActivity() {

    private var fotoSelecionadaUri: Uri? = null
    private lateinit var ivFotoPerfil: ImageView
    private lateinit var btnSalvar: Button

    private val seletorFoto = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            fotoSelecionadaUri = uri
            Glide.with(this)
                .load(uri)
                .centerCrop()
                .placeholder(R.drawable.img_avatar)
                .into(ivFotoPerfil)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_perfil)

        val btnVoltar = findViewById<ImageView>(R.id.iv_voltar_editar_perfil)
        val etNome = findViewById<EditText>(R.id.et_nome_editar)
        val etNomeUsuario = findViewById<EditText>(R.id.et_nome_usuario_editar)
        val etEmail = findViewById<EditText>(R.id.et_email_editar)
        val etSenha = findViewById<EditText>(R.id.et_senha_editar)
        val etPeso = findViewById<EditText>(R.id.et_peso_editar)
        val etAltura = findViewById<EditText>(R.id.et_altura_editar)
        val llSucesso = findViewById<LinearLayout>(R.id.ll_sucesso_salvar)
        val llAlterarFoto = findViewById<LinearLayout>(R.id.ll_alterar_foto)
        val tvAlterarFoto = findViewById<TextView>(R.id.tv_alterar_foto)

        btnSalvar = findViewById(R.id.btn_salvar_perfil)
        ivFotoPerfil = findViewById(R.id.iv_foto_perfil_editar)

        etNome.setText(Sessao.obterNome(this))
        etNomeUsuario.setText(Sessao.obterNomeUsuario(this))
        etEmail.setText(Sessao.obterEmail(this))
        Sessao.obterPeso(this)?.let { etPeso.setText(it.toString()) }
        Sessao.obterAltura(this)?.let { etAltura.setText(it.toString()) }
        carregarFotoAtual()

        btnVoltar.setOnClickListener { finish() }
        llAlterarFoto.setOnClickListener { seletorFoto.launch("image/*") }
        tvAlterarFoto.setOnClickListener { seletorFoto.launch("image/*") }

        btnSalvar.setOnClickListener {
            val nome = etNome.text.toString().trim()
            val nomeUsuario = etNomeUsuario.text.toString().trim()
            val email = etEmail.text.toString().trim().lowercase(Locale.ROOT)
            val senha = etSenha.text.toString()
            val peso = etPeso.text.toString().trim().replace(",", ".").toDoubleOrNull()
            val altura = etAltura.text.toString().trim().toIntOrNull()
            val usuarioId = Sessao.obterUsuarioId(this)

            if (nome.isEmpty()) {
                Toast.makeText(this, "O nome nao pode ficar vazio.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (nomeUsuario.isEmpty()) {
                Toast.makeText(this, "Informe como deseja ser chamado.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Informe um e-mail valido.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (etPeso.text.toString().isNotBlank() && (peso == null || peso <= 0.0)) {
                Toast.makeText(this, "Informe um peso valido.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (etAltura.text.toString().isNotBlank() && (altura == null || altura <= 0)) {
                Toast.makeText(this, "Informe uma altura valida.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (usuarioId == 0) {
                Toast.makeText(this, "Sessao expirada. Faca login novamente.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnSalvar.isEnabled = false
            val uri = fotoSelecionadaUri
            if (uri != null) {
                enviarFoto(uri, usuarioId) { fotoUrl ->
                    salvarPerfil(usuarioId, nome, nomeUsuario, email, senha, fotoUrl, peso, altura, llSucesso)
                }
            } else {
                salvarPerfil(usuarioId, nome, nomeUsuario, email, senha, Sessao.obterFotoUrl(this), peso, altura, llSucesso)
            }
        }
    }

    private fun carregarFotoAtual() {
        val fotoUrl = Sessao.obterFotoUrl(this)
        if (fotoUrl.isNotEmpty()) {
            Glide.with(this)
                .load(fotoUrl)
                .centerCrop()
                .placeholder(R.drawable.img_avatar)
                .error(R.drawable.img_avatar)
                .into(ivFotoPerfil)
        }
    }

    private fun enviarFoto(uri: Uri, usuarioId: Int, onSucesso: (String) -> Unit) {
        val bytes = prepararFoto(uri)
        if (bytes == null) {
            btnSalvar.isEnabled = true
            Toast.makeText(this, "Nao foi possivel processar a imagem.", Toast.LENGTH_SHORT).show()
            return
        }

        val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
        onSucesso("data:image/jpeg;base64,$base64")
    }

    private fun salvarPerfil(
        usuarioId: Int,
        nome: String,
        nomeUsuario: String,
        email: String,
        senha: String,
        fotoUrl: String,
        peso: Double?,
        altura: Int?,
        llSucesso: LinearLayout
    ) {
        val dadosAtualizados = UsuarioAtualizar(
            nome_completo = nome,
            nome_usuario = nomeUsuario,
            email = email,
            senha = senha.ifEmpty { null },
            foto_url = fotoUrl.ifEmpty { null },
            peso = peso,
            altura = altura
        )

        RetrofitClient.api.atualizarUsuario("eq.$usuarioId", dadosAtualizados)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    btnSalvar.isEnabled = true
                    if (response.isSuccessful) {
                        Sessao.salvar(this@EditarPerfilActivity, email, usuarioId, nome, fotoUrl, nomeUsuario, peso, altura)
                        fotoSelecionadaUri = null
                        llSucesso.visibility = View.VISIBLE
                    } else {
                        Toast.makeText(this@EditarPerfilActivity, "Erro: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    btnSalvar.isEnabled = true
                    Toast.makeText(this@EditarPerfilActivity, "Sem conexao com o servidor", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun prepararFoto(uri: Uri): ByteArray? {
        val original = contentResolver.openInputStream(uri)?.use { input ->
            BitmapFactory.decodeStream(input)
        } ?: return null

        val lado = minOf(original.width, original.height)
        val x = (original.width - lado) / 2
        val y = (original.height - lado) / 2
        val quadrada = Bitmap.createBitmap(original, x, y, lado, lado)
        val reduzida = Bitmap.createScaledBitmap(quadrada, 256, 256, true)

        if (quadrada != original) quadrada.recycle()
        if (reduzida != original) original.recycle()

        return ByteArrayOutputStream().use { output ->
            reduzida.compress(Bitmap.CompressFormat.JPEG, 80, output)
            reduzida.recycle()
            output.toByteArray()
        }
    }
}
