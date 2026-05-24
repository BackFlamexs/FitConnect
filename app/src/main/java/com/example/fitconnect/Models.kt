package com.example.fitconnect

import android.content.Context

// Modelos que espelham as tabelas do Supabase
data class TreinoBanco(
    val id: Int = 0,
    val usuario_id: Int = 0,
    val nome: String = "",
    val tag_dia: String = "",
    val dia_semana: String = "",
    val detalhes: String = ""
)

data class ExercicioBanco(
    val id: Int = 0,
    val treino_id: Int = 0,
    val nome: String = "",
    val series_reps: String = ""
)

// Corpo para criar um novo treino (POST)
data class TreinoCriacao(
    val usuario_id: Int,
    val nome: String,
    val tag_dia: String = "EM BREVE",
    val dia_semana: String = "A DEFINIR",
    val detalhes: String = "Personalizado"
)

// Corpo para atualizar um treino (PATCH)
data class TreinoAtualizar(
    val nome: String
)

// Corpo para atualizar dados do usuário (PATCH)
data class UsuarioAtualizar(
    val nome_completo: String,
    val senha: String
)

// Galeria de exercícios (catálogo global)
data class GaleriaExercicioBanco(
    val id: Int = 0,
    val nome: String = "",
    val categoria: String = "",
    val dificuldade: String = "",
    val equipamento: String = "",
    val gif_url: String = "",
    val instrucoes: String = ""
)

// Arquivos do usuário
data class ArquivoBanco(
    val id: Int = 0,
    val usuario_id: Int = 0,
    val nome: String = "",
    val tipo: String = "",
    val tamanho_kb: Int = 0,
    val criado_em: String = ""
)

data class ArquivoCriacao(
    val usuario_id: Int,
    val nome: String,
    val tipo: String,
    val tamanho_kb: Int = 0
)

data class ExercicioCriacao(
    val treino_id: Int,
    val nome: String,
    val series_reps: String
)

// Feedback pós-treino
data class FeedbackBanco(
    val id: Int = 0,
    val usuario_id: Int = 0,
    val treino_nome: String = "",
    val intensidade: String = "",
    val duracao_min: Int = 0,
    val observacoes: String = "",
    val criado_em: String = ""
)

data class FeedbackCriacao(
    val usuario_id: Int,
    val treino_nome: String,
    val intensidade: String,
    val duracao_min: Int,
    val observacoes: String
)

// Sessão do usuário logado via SharedPreferences
object Sessao {
    private const val PREFS = "fitconnect_sessao"

    fun salvar(context: Context, email: String, usuarioId: Int, nome: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString("email", email)
            .putInt("usuario_id", usuarioId)
            .putString("nome", nome)
            .apply()
    }

    fun obterEmail(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString("email", "") ?: ""

    fun obterUsuarioId(context: Context): Int =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt("usuario_id", 0)

    fun obterNome(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString("nome", "Atleta") ?: "Atleta"

    fun limpar(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().apply()
}
