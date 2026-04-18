package com.example.wereables.ui.viewmodels

//ACA ESTA EL ERROR
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

import  com.example.wereables.ui.network.RetrofitClient
import com.example.wereables.ui.network.models.HealthData
import  com.example.wereables.ui.screens.HealthDashboardScreen

import kotlinx.coroutines.launch

/*     val heartRate: Int,5
    val distance: Double,
    val active_calories: Int,
    val total_calories: Int
*/
class HealthViewModel : ViewModel()  {




   var estadoEnvio by mutableStateOf<String?>(null)
        private set

    fun enviarDatos(pasos: Int, ritmo:Double, distancia: Double, caloriasActivas: Double, caloriasTotales: Double   ) {
        viewModelScope.launch {

            val ritmoFinal = String.format("%.2f", ritmo).toDouble()
            val distanciaFinal = String.format("%.2f", distancia).toDouble()
            val total_caloriesFinal = String.format("%.2f", caloriasTotales).toDouble()
            val active__caloriesFinal = String.format("%.2f", caloriasActivas).toDouble()


            estadoEnvio = "enviando"

            try {
                Log.d("API", "📤 Enviando datos...")

                // Creamos el objeto con los datos
                val datos = HealthData(steps = pasos,
                    heartRate = ritmoFinal,
                    distance = distanciaFinal,
                    active_calories = active__caloriesFinal,
                    total_calories = total_caloriesFinal
                )

                //Log.d("API", "Payload: $datos")
               // RetrofitClient.api.sendData(datos)


                val response = RetrofitClient.api.sendData(datos)
                Log.d("API", "✅ Respuesta OK: $response")
                estadoEnvio = "✅ Enviado correctamente"


                // Llamamos a la API (Retrofit ya lo hace en hilo secundario)
               //RetrofitClient.api.sendData(datos)

                println("✅ ÉXITO: Datos enviados a MariaDB")
            } catch (e: Exception) {

                estadoEnvio = when {

                    e.message?.contains("Cleartext") == true -> "❌ Error: Configura 'usesCleartextTraffic' en el Manifest"
                    e.message?.contains("timeout") == true -> "❌ Error: Tiempo de espera agotado (Revisa Firewall)"
                    e.message?.contains("Failed to connect") == true -> "❌ Error: No se pudo conectar a ${RetrofitClient.BASE_URL}"
                    else -> "❌  Error: ${e.localizedMessage ?: "Error desconocido"}"

                }
                // Si el servidor Node.js está caído o no hay internet
                //Log.e("API", "❌ Error enviando datos", e)
              //  e.printStackTrace()
            }


        }
    }

    }