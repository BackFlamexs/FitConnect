package com.example.fitconnect.feature.pagamento

import com.example.fitconnect.R
import com.example.fitconnect.data.model.*
import com.example.fitconnect.core.network.RetrofitClient

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class PagamentoProActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pagamento_pro)

        findViewById<ImageView>(R.id.iv_voltar_pagamento_pro).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.btn_confirmar_pagamento_pro).setOnClickListener {
            Toast.makeText(this, "Pagamento Pro em desenvolvimento", Toast.LENGTH_SHORT).show()
        }
    }
}
