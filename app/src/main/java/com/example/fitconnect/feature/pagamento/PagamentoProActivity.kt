package com.example.fitconnect.feature.pagamento

import com.example.fitconnect.R
import com.example.fitconnect.data.model.*
import com.example.fitconnect.core.network.RetrofitClient
import com.example.fitconnect.feature.home.HomeActivity
import com.example.fitconnect.feature.dashboard.DashboardProActivity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PagamentoProActivity : AppCompatActivity() {

    private lateinit var btnConfirmar: Button
    private lateinit var pbLoading: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pagamento_pro)

        btnConfirmar = findViewById(R.id.btn_confirmar_pagamento_pro)
        pbLoading = findViewById(R.id.pb_loading_pagamento)

        findViewById<ImageView>(R.id.iv_voltar_pagamento_pro).setOnClickListener { finish() }

        btnConfirmar.setOnClickListener { iniciarAssinatura() }
    }

    private fun iniciarAssinatura() {
        btnConfirmar.isEnabled = false
        btnConfirmar.text = "Processando..."
        pbLoading.visibility = View.VISIBLE

        Handler(Looper.getMainLooper()).postDelayed({ ativarPro() }, 2000)
    }

    private fun ativarPro() {
        val usuarioId = Sessao.obterUsuarioId(this)
        RetrofitClient.api.atualizarPro("eq.$usuarioId", UsuarioProAtualizar(true))
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    finalizarAtivacao()
                }
                override fun onFailure(call: Call<Void>, t: Throwable) {
                    finalizarAtivacao()
                }
            })
    }

    private fun finalizarAtivacao() {
        Sessao.salvarPro(this, true)
        pbLoading.visibility = View.GONE

        AlertDialog.Builder(this)
            .setTitle("Pagamento aprovado com sucesso!")
            .setMessage("Você agora é membro PRO do FitConnect!\n\nAproveite treinos ilimitados, estatísticas avançadas e muito mais.")
            .setPositiveButton("Ver Dashboard PRO") { _, _ ->
                val intent = Intent(this, DashboardProActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Ir para Home") { _, _ ->
                val intent = Intent(this, HomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
                finish()
            }
            .setCancelable(false)
            .show()
    }
}