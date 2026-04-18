package com.example.wereables.ui.viewmodels


import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wereables.ui.network.RetrofitClient
import com.example.wereables.ui.network.models.LoginRequest
import kotlinx.coroutines.launch
import androidx.compose.material3.CircularProgressIndicator


class AuthViewModel : ViewModel() {

    var isLoading by mutableStateOf(false)
    private set

    var message by mutableStateOf<String?>(null)
        private set

    fun login(username: String, password: String, onLoginSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            message = null
            Log.d("API_AUTH", "🔐 Inten< tando login para usuario: $username")

            try {
                val creds = LoginRequest(username, password)
                val response = RetrofitClient.api.login(creds)

                if (response.success) {
                    Log.d("API_AUTH", "✅ Login exitoso: ${response.message}")
                    onLoginSuccess()
                } else {
                    Log.w("API_AUTH", "⚠️ Login fallido: ${response.message}")
                    message = response.message
                }
            } catch (e: Exception) {
                Log.e("API_AUTH", "❌ Error en la petición", e)
                message = when {
                    e.message?.contains("Failed to connect") == true -> "Error: No se pudo conectar al servidor"
                    else -> "Error: ${e.localizedMessage ?: "Desconocido"}"
                }
            } finally {
                isLoading = false
            }
        }

        fun register(username: String, password: String) {
            viewModelScope.launch {
                isLoading = true
                message = null
                try {
                    val creds = LoginRequest(username, password)
                    val response = RetrofitClient.api.register(creds) // Asegúrate de tener @POST("register") en tu interfaz

                    message = response.message // Mostrará "¡Registro exitoso!" o el error del server
                } catch (e: Exception) {
                    message = "Error al conectar"
                } finally {
                    isLoading = false
                }
            }
        }
    }
}







