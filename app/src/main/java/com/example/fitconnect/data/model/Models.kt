package com.example.fitconnect.data.model

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
    val nome: String,
    val tag_dia: String? = null,
    val dia_semana: String? = null,
    val detalhes: String? = null
)

// Corpo para atualizar dados do usuário (PATCH)
data class UsuarioAtualizar(
    val nome_completo: String,
    val nome_usuario: String,
    val email: String,
    val senha: String? = null,
    val foto_url: String? = null,
    val peso: Double? = null,
    val altura: Int? = null,
    val data_nascimento: String? = null,
    val biografia: String? = null
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
    val criado_em: String = "",
    val arquivo_url: String = ""
)

data class ArquivoCriacao(
    val usuario_id: Int,
    val nome: String,
    val tipo: String,
    val tamanho_kb: Int = 0,
    val arquivo_url: String = ""
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
    val criado_em: String = "",
    val resposta_personal: String? = null
)

data class FeedbackCriacao(
    val usuario_id: Int,
    val treino_nome: String,
    val intensidade: String,
    val duracao_min: Int,
    val observacoes: String
)

data class FeedbackRespostaAtualizar(
    val resposta_personal: String
)

data class PersonalAlunoVinculo(
    val id: Int = 0,
    val personal_id: Int = 0,
    val aluno_id: Int = 0,
    val criado_em: String = "",
    val usuarios: Usuario? = null
)

data class PersonalAlunoCriacao(
    val personal_id: Int,
    val aluno_id: Int
)

// Sessão do usuário logado via SharedPreferences
object Sessao {
    private const val PREFS = "fitconnect_sessao"

    fun salvar(
        context: Context,
        email: String,
        usuarioId: Int,
        nome: String,
        fotoUrl: String = "",
        nomeUsuario: String = nome,
        peso: Double? = null,
        altura: Int? = null,
        accountType: String = "student",
        dataNascimento: String? = null,
        biografia: String? = null
    ) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString("email", email)
            .putInt("usuario_id", usuarioId)
            .putString("nome", nome)
            .putString("nome_usuario", nomeUsuario)
            .putString("foto_url", fotoUrl)
            .putString("peso", peso?.toString().orEmpty())
            .putInt("altura", altura ?: 0)
            .putString("account_type", accountType)
            .putString("data_nascimento", dataNascimento.orEmpty())
            .putString("biografia", biografia.orEmpty())
            .apply()
    }

    fun obterEmail(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString("email", "") ?: ""

    fun obterUsuarioId(context: Context): Int =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt("usuario_id", 0)

    fun obterNome(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString("nome", "Atleta") ?: "Atleta"

    fun obterNomeUsuario(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString("nome_usuario", obterNome(context)) ?: obterNome(context)

    fun obterFotoUrl(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString("foto_url", "") ?: ""

    fun obterPeso(context: Context): Double? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString("peso", "")
            ?.toDoubleOrNull()

    fun obterAltura(context: Context): Int? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getInt("altura", 0)
            .takeIf { it > 0 }

    fun obterAccountType(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString("account_type", "student") ?: "student"

    fun obterDataNascimento(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString("data_nascimento", "") ?: ""

    fun obterBiografia(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString("biografia", "") ?: ""

    fun limpar(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().apply()
}
