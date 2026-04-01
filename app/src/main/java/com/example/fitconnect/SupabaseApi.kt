package com.example.fitconnect

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface SupabaseApi {

    // Rota de usuario, que ele vai usar para puxar os dados no banco
    @Headers("Prefer: return=representation")
    @POST("rest/v1/usuarios")
    fun cadastrarUsuario(@Body usuario: Usuario): Call<List<Usuario>>

    // Rota de login, que ele vai usar para puxar os dados no banco
    @GET("rest/v1/usuarios")
    fun fazerLogin(
        @Query("email") email: String,
        @Query("senha") senha: String
    ): Call<List<Usuario>>

}