package com.example.wereables.ui.screens

import android.text.format.DateUtils.isToday
import android.util.Log
import java.time.LocalDate
import java.time.ZoneId



import androidx.compose.animation.core.Animatable
//import androidx.compose.animation.core.tweenimport
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import com.example.wereables.ui.viewmodels.HealthViewModel

import kotlinx.coroutines.launch
import java.time.Instant
import java.time.temporal.ChronoUnit
import androidx.compose.ui.text.input.PasswordVisualTransformation
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthDashboardScreen(
    client: HealthConnectClient,
    viewModel: HealthViewModel // ✅ ViewModel conectado
) {
    // ESTADOS
    var steps by remember { mutableStateOf<List<StepsRecord>?>(null) }
    var heartRate by remember { mutableStateOf<List<HeartRateRecord>?>(null) }
    var distance by remember { mutableStateOf<List<DistanceRecord>?>(null) }
    var activeCalories by remember { mutableStateOf<List<ActiveCaloriesBurnedRecord>?>(null) }
    var totalCalories by remember { mutableStateOf<List<TotalCaloriesBurnedRecord>?>(null) }

    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // CÁLCULOS DE DATOS (Los sacamos aquí para que el botón los vea)
    val today = LocalDate.now(ZoneId.systemDefault())
    val todaySteps = steps?.filter { it.startTime.atZone(ZoneId.systemDefault()).toLocalDate() == today }?.sumOf { it.count } ?: 0L

    val todayHeartRateSamples = heartRate?.filter { isToday(it.startTime) }?.flatMap { it.samples } ?: emptyList()
    val avgHeartRate = if (todayHeartRateSamples.isNotEmpty()) todayHeartRateSamples.map { it.beatsPerMinute }.average() else 0

    val latestHeartRate = if (todayHeartRateSamples.isNotEmpty()) {
        todayHeartRateSamples.maxByOrNull { it.time }?.beatsPerMinute?.toDouble() ?: 0.0
    } else 0.0

    val todayDistanceKm = (distance?.filter { isToday(it.startTime) }?.sumOf { it.distance.inMeters } ?: 0.0) / 1000.0
    val todayTotalCal = (totalCalories?.filter { isToday(it.startTime) }?.sumOf { it.energy.inCalories } ?: 0.0) / 1000.0
    val todayActiveCal = activeCalories?.filter { isToday(it.startTime) }?.sumOf { it.energy.inCalories } ?: 0.0
    val animatedSteps by animateIntAsState(targetValue = todaySteps.toInt(), label = "steps")
    val animLatestHR = remember { Animatable(0f) }
    val animAvgHR = remember { Animatable(0f) }


    LaunchedEffect(latestHeartRate, avgHeartRate) {
        launch { animLatestHR.animateTo(latestHeartRate.toFloat(), tween(1500)) }
        launch { animAvgHR.animateTo(avgHeartRate.toFloat(), tween(1500)) }
    }


    /*
    LaunchedEffect(latestHeartRate, avgHeartRate) {
        launch { animLatestHR.animateTo(latestHeartRate.toFloat(), tween(1500)) }
        launch { animAvgHR.animateTo(avgHeartRate.toFloat(), tween(1500)) }
    }
*/
    // CARGA INICIAL
    LaunchedEffect(Unit) {
        try {
            val range = TimeRangeFilter.between(Instant.now().minus(7, ChronoUnit.DAYS), Instant.now())

            steps = client.readRecords(ReadRecordsRequest(StepsRecord::class, range)).records
            heartRate = client.readRecords(ReadRecordsRequest(HeartRateRecord::class, range)).records
            distance = client.readRecords(ReadRecordsRequest(DistanceRecord::class, range)).records
            activeCalories = client.readRecords(ReadRecordsRequest(ActiveCaloriesBurnedRecord::class, range)).records
            totalCalories = client.readRecords(ReadRecordsRequest(TotalCaloriesBurnedRecord::class, range)).records

            isLoading = false
        } catch (e: Exception) {
            isLoading = false
            errorMessage = e.message
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(colors = listOf(Color(0xFFBBDEFB), Color(0xFF1976D2))))) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Mi Band Dashboard") },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1565C0), titleContentColor = Color.White)
                )
            },
            containerColor = Color.Transparent,
            floatingActionButton = {
                if (!isLoading && errorMessage == null) {
                    ExtendedFloatingActionButton(
                        onClick = {
                            viewModel.enviarDatos(

                                pasos = todaySteps.toInt(),
                                   ritmo = latestHeartRate,
                                distancia = todayDistanceKm,
                                caloriasActivas = todayActiveCal,
                                caloriasTotales = todayTotalCal
                            )
                        },
                        containerColor = Color(0xFF43A047),
                        contentColor = Color.White,
                        icon = { Icon(Icons.Default.CloudUpload, null) },
                        text = { Text("Sincronizar DB") }
                    )
                }
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {

                    viewModel.estadoEnvio?.let { mensaje ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (mensaje.contains("✅")) Color(0xFFC8E6C9) else Color(0xFFFFCDD2)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = mensaje,
                                modifier = Modifier.padding(12.dp),
                                color = if (mensaje.contains("✅")) Color(0xFF2E7D32) else Color(0xFFB71C1C),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }


                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White)
                    } else if (errorMessage != null) {
                        Text("Error: $errorMessage", color = Color.Red)
                    } else {
                        HealthMetricCard("👣 Pasos hoy", animatedSteps.toString(), Color(0xFF43A047))
                        HealthMetricCard("❤️ Ritmo cardíaco (Actual)", "${animLatestHR.value.toInt()} bpm", Color(0xFFE53935))
                        HealthMetricCard("❤️ Ritmo cardíaco (Promedio)", "${animAvgHR.value.toInt()} bpm", Color(0xFFE53935))
                        HealthMetricCard("📏 Distancia hoy", "%.2f km".format(todayDistanceKm), Color(0xFF2196F3))
                        HealthMetricCard("🔥 Calorías activas", "%.0f cal".format(todayActiveCal), Color(0xFFFF9800))
                        HealthMetricCard("🔥 Calorías totales", "%.0f kcal".format(todayTotalCal), Color(0xFFFF9800))

                        viewModel.estadoEnvio?.let {
                            Text(
                                text = it,
                                color = Color.White
                            )
                        }



                    }
                }
            }
        }
    }
}

@Composable
fun HealthMetricCard(title: String, value: String, valueColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = Color.Gray)
            Text(text = value, style = MaterialTheme.typography.headlineMedium, color = valueColor, modifier = Modifier.padding(top = 4.dp))
        }
    }
}




fun isToday(instant: Instant): Boolean {
    val recordDate = instant.atZone(ZoneId.systemDefault()).toLocalDate()
    return recordDate == LocalDate.now(ZoneId.systemDefault())
}