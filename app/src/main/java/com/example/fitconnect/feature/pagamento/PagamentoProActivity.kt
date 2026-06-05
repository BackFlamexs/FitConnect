package com.example.fitconnect.feature.pagamento

import com.example.fitconnect.R
import com.example.fitconnect.data.model.*
import com.example.fitconnect.core.network.RetrofitClient
import com.example.fitconnect.feature.home.HomeActivity
import com.example.fitconnect.feature.dashboard.DashboardProActivity

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
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

        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_pagamento_aprovado)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.88).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.setCancelable(false)

        dialog.findViewById<Button>(R.id.btn_ver_dashboard).setOnClickListener {
            dialog.dismiss()
            val intent = Intent(this, DashboardProActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }
        dialog.findViewById<Button>(R.id.btn_ir_home).setOnClickListener {
            dialog.dismiss()
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        dialog.show()
    }
}