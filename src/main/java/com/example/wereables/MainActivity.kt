package com.example.wereables

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.lifecycle.lifecycleScope
import com.example.wereables.ui.theme.WereablesTheme
import com.example.wereables.ui.screens.HealthDashboardScreen

import kotlinx.coroutines.launch
//bd
//import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import com.example.wereables.ui.viewmodels.HealthViewModel
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height

//
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.wereables.ui.screens.LoginScreen

class MainActivity : ComponentActivity() {
    private lateinit var healthConnectClient: HealthConnectClient
    private val healthViewModel: HealthViewModel by viewModels()

    private val PERMISSIONS = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(DistanceRecord::class),
        HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class),
        HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class) // ← CAMBIADO


    )

    // Nuevo launcher para el contrato de permisos
    private val requestPermissionLauncher =
        registerForActivityResult(PermissionController.createRequestPermissionResultContract()) { grantedPermissions ->
            if (grantedPermissions.containsAll(PERMISSIONS)) {
                // Permisos concedidos
                showDashboard()
            } else {
                // Permisos no concedidos
                showPermissionNotGrantedScreen()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val providerPackageName = "com.google.android.apps.healthdata"
        val availabilityStatus = HealthConnectClient.getSdkStatus(this, providerPackageName)
        when (availabilityStatus) {
            HealthConnectClient.SDK_UNAVAILABLE -> {
                setContent {
                    WereablesTheme {
                        Text("Health Connect no soportado en este dispositivo")
                    }
                }
            }
            HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> {
                setContent {
                    WereablesTheme {
                        Column {
                            Text("Instala o actualiza Health Connect desde Play Store")
                            Button(onClick = {
                                startActivity(Intent(Intent.ACTION_VIEW).apply {
                                    setPackage("com.android.vending")
                                    data = Uri.parse("market://details?id=$providerPackageName")
                                })
                            }) {
                                Text("Abrir Play Store")
                            }
                        }
                    }
                }
            }
            else -> {
                try {
                    healthConnectClient = HealthConnectClient.getOrCreate(this)
                    lifecycleScope.launch {
                        checkPermissionsAndNavigate()
                    }
                } catch (e: Exception) {
                    setContent {
                        WereablesTheme {
                            Text("Error al iniciar Health Connect: ${e.message}")
                        }
                    }
                }
            }
        }
    }

    private suspend fun checkPermissionsAndNavigate() {
        val grantedPermissions = healthConnectClient.permissionController.getGrantedPermissions()
        if (grantedPermissions.containsAll(PERMISSIONS)) {
            showDashboard()
        } else {
            // Solicitar permisos de Health Connect usando el nuevo launcher
            requestPermissionLauncher.launch(PERMISSIONS)
        }
    }

    private fun showDashboard() {
        setContent {
            WereablesTheme {
                // Estado para saber si el usuario ya entró
                var isLoggedIn by remember { mutableStateOf(false) }

                if (!isLoggedIn) {
                    // Pasamos LoginScreen como la puerta de entrada
                    LoginScreen(onLoginSuccess = {
                        isLoggedIn = true
                    })
                } else {
                    // Una vez logueado, se muestra el Dashboard original
                    HealthDashboardScreen(
                        client = healthConnectClient,
                        viewModel = healthViewModel
                    )
                }
            }
        }
    }






  /*  private fun showDashboard() {
        setContent {
            WereablesTheme {
                HealthDashboardScreen(client = healthConnectClient)
            }
        }
    }*/


    private fun showPermissionNotGrantedScreen() {
        setContent {
            WereablesTheme {
                Column {
                    Text("Permisos de Health Connect no otorgados")
                    Button(onClick = {
                        // Reintentar solicitud de permisos
                        requestPermissionLauncher.launch(PERMISSIONS)
                    }) {
                        Text("Reintentar permisos")
                    }
                }
            }
        }
    }
}


@Composable
fun HealthDashboardScreen(
    client: HealthConnectClient,
    viewModel: HealthViewModel // Agregamos el "enchufe" para el ViewModel
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Panel de Health Connect Activo")

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = {
            // mandas datos a MariaDB
            viewModel.enviarDatos(pasos = 8000, ritmo  = 72.67, distancia = 13.45, caloriasActivas = 434.87, caloriasTotales = 1.8 )
        }) {
            Text("Enviar datos a MariaDB")
        }
    }
}




/*class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            enableEdgeToEdge()
        }

        //por ahora esta hardcoedeado
        val apiKey = "123456"
        val baseUrl = "https://api.example.com"


        println(" Key: $apiKey")
            println(" URL: $baseUrl")

            initHealthConnect()


    setContent {
            WereablesTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}


private fun initHealthConnect() {
    // Aquí deberías implementar la lógica para inicializar la conexión con Health Connect
    // Esto es solo un ejemplo de cómo podrías estructurarlo
    println("Inicializando Health Connect...")
}



@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    WereablesTheme {
        Greeting("Android")
    }
}


*/