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
    fun cadastrarUsuario(@Body usuario: Usuario): Call<List<Usuario>>

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
}
