package com.example.wereables.ui.network


import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit



object RetrofitClient {
   // private const val BASE_URL = "http://10.0.2.2:3000/"
   const val BASE_URL = "https://backend-prueba-sy65.onrender.com"
       //"https://tranquil-domestic-steersman.ngrok-free.dev "
    //"http://172.24.176.1:3000/"

    //cliente Okhttp
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(1, TimeUnit.MINUTES)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)   // Espera hasta 60s para enviar datos

        .followRedirects(true)
        .followSslRedirects(true)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("ngrok-skip-browser-warning", "true")
                .build()
            chain.proceed(request)
        }
        .build()


    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}