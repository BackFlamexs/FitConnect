package com.example.fitconnect.data.model

data class UsuarioCriacao(
    val nome_completo: String,
    val nome_usuario: String,
    val email: String,
    val senha: String,
    val telefone: String,
    val genero: String,
    val account_type: String,
    val data_nascimento: String? = null
)

data class Usuario(
    val nome_completo: String? = "",
    val nome_usuario: String? = "",
    val email: String? = "",
    val senha: String? = "",
    val telefone: String? = "",
    val genero: String? = "",
    val id: Int = 0,
    val foto_url: String? = "",
    val peso: Double? = null,
    val altura: Int? = null,
    val account_type: String? = "student",
    val data_nascimento: String? = null,
    val biografia: String? = null
)
