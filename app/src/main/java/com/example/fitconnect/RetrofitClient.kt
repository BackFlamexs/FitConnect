package com.example.fitconnect


import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    //A URL do projeto.
    private const val BASE_URL = "https://gltouzhsqtvoinphhbmv.supabase.co/"

    //Publishable Key.
    private const val SUPABASE_KEY = "sb_publishable__uZ70sEC2kZUtHrIBbUY2w_FlLYMJbY"

    private val httpClient = OkHttpClient.Builder().apply {
        addInterceptor(Interceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Authorization", "Bearer $SUPABASE_KEY")
                .addHeader("Content-Type", "application/json")
                .build()
            chain.proceed(request)
        })
    }.build()

    val api: SupabaseApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SupabaseApi::class.java)
    }
}