package com.example.wereables.ui.network
import androidx.activity.viewModels
import com.example.wereables.ui.network.models.HealthData
import com.example.wereables.ui.network.models.LoginRequest
import com.example.wereables.ui.network.models.LoginResponse


import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.Response


interface ApiService {

    @POST("health")
    suspend fun sendData(@Body data: HealthData): Response<Unit> // O una clase de respuesta

    //suspend fun sendData(@Body data: HealthData)

    @GET("health")
    suspend fun getData(): List<HealthData>

    @POST("login")
    suspend fun login(@Body credentials: LoginRequest): LoginResponse


    @POST("register")
    suspend fun register(@Body credentials: LoginRequest): LoginResponse

}