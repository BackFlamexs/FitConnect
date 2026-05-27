package com.example.fitconnect

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

interface SupabaseApi {

    // ── Usuários ──────────────────────────────────────────────────────────────

    @Headers("Prefer: return=representation")
    @POST("rest/v1/usuarios")
    fun cadastrarUsuario(@Body usuario: UsuarioCriacao): Call<List<Usuario>>

    @GET("rest/v1/usuarios")
    fun fazerLogin(
        @Query("email") email: String,
        @Query("senha") senha: String
    ): Call<List<Usuario>>

    @Headers("Prefer: return=minimal")
    @PATCH("rest/v1/usuarios")
    fun atualizarUsuario(
        @Query("email") email: String,
        @Body dados: UsuarioAtualizar
    ): Call<Void>

    // ── Treinos ───────────────────────────────────────────────────────────────

    @GET("rest/v1/treinos")
    fun buscarTreinos(
        @Query("select") select: String = "*",
        @Query("order") order: String = "id.asc"
    ): Call<List<TreinoBanco>>

    @Headers("Prefer: return=representation")
    @POST("rest/v1/treinos")
    fun criarTreino(@Body treino: TreinoCriacao): Call<List<TreinoBanco>>

    @Headers("Prefer: return=minimal")
    @PATCH("rest/v1/treinos")
    fun atualizarTreino(
        @Query("id") id: String,
        @Body dados: TreinoAtualizar
    ): Call<Void>

    @DELETE("rest/v1/treinos")
    fun deletarTreino(
        @Query("id") id: String
    ): Call<Void>

    // ── Exercícios ────────────────────────────────────────────────────────────

    @GET("rest/v1/exercicios")
    fun buscarExercicios(
        @Query("treino_id") treinoId: String,
        @Query("select") select: String = "*",
        @Query("order") order: String = "id.asc"
    ): Call<List<ExercicioBanco>>

    @Headers("Prefer: return=representation")
    @POST("rest/v1/exercicios")
    fun criarExercicio(@Body exercicio: ExercicioCriacao): Call<List<ExercicioBanco>>

    @DELETE("rest/v1/exercicios")
    fun deletarExercicioPorTreino(@Query("treino_id") treinoId: String): Call<Void>

    // ── Galeria de Exercícios ─────────────────────────────────────────────────

    @GET("rest/v1/galeria_exercicios")
    fun buscarGaleriaExercicios(
        @Query("select") select: String = "*",
        @Query("order") order: String = "nome.asc"
    ): Call<List<GaleriaExercicioBanco>>

    // ── Arquivos ──────────────────────────────────────────────────────────────

    @GET("rest/v1/arquivos")
    fun buscarArquivos(
        @Query("usuario_id") usuarioId: String,
        @Query("select") select: String = "*",
        @Query("order") order: String = "criado_em.desc"
    ): Call<List<ArquivoBanco>>

    @Headers("Prefer: return=representation")
    @POST("rest/v1/arquivos")
    fun criarArquivo(@Body arquivo: ArquivoCriacao): Call<List<ArquivoBanco>>

    @DELETE("rest/v1/arquivos")
    fun deletarArquivo(@Query("id") id: String): Call<Void>

    // ── Feedbacks ─────────────────────────────────────────────────────────────

    @Headers("Prefer: return=representation")
    @POST("rest/v1/feedbacks")
    fun criarFeedback(@Body feedback: FeedbackCriacao): Call<List<FeedbackBanco>>

    @GET("rest/v1/feedbacks")
    fun buscarFeedbacks(
        @Query("usuario_id") usuarioId: String,
        @Query("select") select: String = "*",
        @Query("order") order: String = "criado_em.desc"
    ): Call<List<FeedbackBanco>>
}
