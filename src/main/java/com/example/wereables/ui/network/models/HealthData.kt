package com.example.wereables.ui.network.models
import com.google.gson.annotations.SerializedName

data class HealthData(
    val steps: Int,
    @SerializedName("heartRate") // Coincide con la columna de la DB
    val heartRate: Double,
    val distance: Double,
    val active_calories: Double,
    //val latesthearthrate : Double,
    val total_calories: Double

    )

data class LoginRequest(val username: String, val password: String)

data class LoginResponse(val success: Boolean, val message: String)