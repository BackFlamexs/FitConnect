package com.example.fitconnect.feature.perfil

import com.example.fitconnect.R
import com.example.fitconnect.data.model.*
import com.example.fitconnect.core.network.RetrofitClient

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Base64
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
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

    private val maxBiografiaChars = 1000
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
        val etBiografia = findViewById<EditText>(R.id.et_biografia_editar)
        val tvContadorBiografia = findViewById<TextView>(R.id.tv_contador_biografia_editar)
        val etDataNascimento = findViewById<EditText>(R.id.et_data_nascimento_editar)
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
        etBiografia.filters = arrayOf(InputFilter.LengthFilter(maxBiografiaChars))
        etBiografia.setText(Sessao.obterBiografia(this))
        atualizarContadorBiografia(etBiografia, tvContadorBiografia)
        val dataNascimentoAtual = Sessao.obterDataNascimento(this)
        if (dataNascimentoAtual.isNotBlank()) {
            etDataNascimento.setText(formatarDataTela(dataNascimentoAtual))
            etDataNascimento.tag = dataNascimentoAtual
        }
        Sessao.obterPeso(this)?.let { etPeso.setText(it.toString()) }
        Sessao.obterAltura(this)?.let { etAltura.setText(it.toString()) }
        carregarFotoAtual()

        btnVoltar.setOnClickListener { finish() }
        etDataNascimento.addTextChangedListener(mascaraData(etDataNascimento))
        etBiografia.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                atualizarContadorBiografia(etBiografia, tvContadorBiografia)
            }
        })
        llAlterarFoto.setOnClickListener { seletorFoto.launch("image/*") }
        tvAlterarFoto.setOnClickListener { seletorFoto.launch("image/*") }

        btnSalvar.setOnClickListener {
            val nome = etNome.text.toString().trim()
            val nomeUsuario = etNomeUsuario.text.toString().trim()
            val email = etEmail.text.toString().trim().lowercase(Locale.ROOT)
            val senha = etSenha.text.toString()
            val biografia = etBiografia.text.toString().trim()
            val dataNascimento = etDataNascimento.tag as? String ?: Sessao.obterDataNascimento(this)
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
                    salvarPerfil(usuarioId, nome, nomeUsuario, email, senha, fotoUrl, peso, altura, dataNascimento, biografia, llSucesso)
                }
            } else {
                salvarPerfil(usuarioId, nome, nomeUsuario, email, senha, Sessao.obterFotoUrl(this), peso, altura, dataNascimento, biografia, llSucesso)
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
        dataNascimento: String,
        biografia: String,
        llSucesso: LinearLayout
    ) {
        val dadosAtualizados = UsuarioAtualizar(
            nome_completo = nome,
            nome_usuario = nomeUsuario,
            email = email,
            senha = senha.ifEmpty { null },
            foto_url = fotoUrl.ifEmpty { null },
            peso = peso,
            altura = altura,
            data_nascimento = dataNascimento.ifEmpty { null },
            biografia = biografia.ifEmpty { null }
        )

        RetrofitClient.api.atualizarUsuario("eq.$usuarioId", dadosAtualizados)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    btnSalvar.isEnabled = true
                    if (response.isSuccessful) {
                        Sessao.salvar(
                            this@EditarPerfilActivity,
                            email,
                            usuarioId,
                            nome,
                            fotoUrl,
                            nomeUsuario,
                            peso,
                            altura,
                            Sessao.obterAccountType(this@EditarPerfilActivity),
                            dataNascimento,
                            biografia
                        )
                        fotoSelecionadaUri = null
                        llSucesso.visibility = View.VISIBLE
                        findViewById<ScrollView>(R.id.sv_editar_perfil).smoothScrollTo(0, 0)
                        Toast.makeText(this@EditarPerfilActivity, "Perfil salvo com sucesso!", Toast.LENGTH_SHORT).show()
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

    private fun mascaraData(campo: EditText): TextWatcher {
        return object : TextWatcher {
            private var atualizando = false
            private val mascara = "##/##/####"

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (atualizando) return
                atualizando = true
                val digitos = s?.filter { it.isDigit() }?.toString() ?: ""
                val resultado = StringBuilder()
                var i = 0
                for (c in mascara) {
                    if (i >= digitos.length) break
                    if (c == '#') resultado.append(digitos[i++]) else resultado.append(c)
                }
                campo.setText(resultado.toString())
                campo.setSelection(resultado.length)
                campo.tag = if (resultado.length == 10) parsearDataParaBanco(resultado.toString()) else null
                atualizando = false
            }
        }
    }

    private fun parsearDataParaBanco(dataTela: String): String? {
        val partes = dataTela.split("/")
        if (partes.size != 3) return null
        val (dia, mes, ano) = partes
        if (dia.length != 2 || mes.length != 2 || ano.length != 4) return null
        return "$ano-$mes-$dia"
    }

    private fun formatarDataTela(dataBanco: String): String {
        val partes = dataBanco.take(10).split("-")
        return if (partes.size == 3) "${partes[2]}/${partes[1]}/${partes[0]}" else dataBanco
    }

    private fun atualizarContadorBiografia(campo: EditText, contador: TextView) {
        contador.text = "${campo.text.length}/$maxBiografiaChars"
    }
}
