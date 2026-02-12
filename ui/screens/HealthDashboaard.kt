package com.example.wereables.ui.screens

import android.text.format.DateUtils.isToday
import android.util.Log
import java.time.LocalDate
import java.time.ZoneId



import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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

import kotlinx.coroutines.launch
import java.time.Instant
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthDashboardScreen(client: HealthConnectClient) {
    // Estados para datos, carga y errores
    var steps by remember { mutableStateOf<List<StepsRecord>?>(null) }
    var heartRate by remember { mutableStateOf<List<HeartRateRecord>?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var distance by remember { mutableStateOf<List<DistanceRecord>?>(null) }
    var activeCalories by remember { mutableStateOf<List<ActiveCaloriesBurnedRecord>?>(null) }
    var totalCalories by remember { mutableStateOf<List<TotalCaloriesBurnedRecord>?>(null) } // ← calorias

    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    // Leer datos al iniciar
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val stepsResponse = client.readRecords(
                    ReadRecordsRequest(
                        recordType = StepsRecord::class,
                        timeRangeFilter = TimeRangeFilter.between(
                            Instant.now().minus(7, ChronoUnit.DAYS),
                            Instant.now()
                        )
                    )
                )
                steps = stepsResponse.records
                Log.d("HealthDashboard", "Pasos obtenidos: ${steps?.size ?: 0}")


                // ✅ CALORÍAS TOTALES (CORREGIDO)
                val caloriesResponse = client.readRecords(
                    ReadRecordsRequest(
                        recordType = TotalCaloriesBurnedRecord::class, // ← TOTALES
                        timeRangeFilter = TimeRangeFilter.between(
                            Instant.now().minus(7, ChronoUnit.DAYS),
                            Instant.now()
                        )
                    )
                )
                totalCalories = caloriesResponse.records

                isLoading = false




                val heartRateResponse = client.readRecords(
                    ReadRecordsRequest(
                        recordType = HeartRateRecord::class,
                        timeRangeFilter = TimeRangeFilter.between(
                            Instant.now().minus(7, ChronoUnit.DAYS),
                            Instant.now()
                        )
                    )
                )
                heartRate = heartRateResponse.records
                Log.d("HealthDashboard", "Ritmo cardíaco obtenido: ${heartRate?.size ?: 0}")

                isLoading = false
            } catch (e: SecurityException) {
                isLoading = false
                errorMessage = "Permisos de Health Connect no otorgados."
            } catch (e: Exception) {
                isLoading = false
                errorMessage = "Error al leer datos: ${e.message}. Usando datos simulados."
                steps = listOf(StepsRecord(count = 7500, startTime = Instant.now(), endTime = Instant.now(), startZoneOffset = null, endZoneOffset = null))
                heartRate = listOf(
                    HeartRateRecord(
                        startTime = Instant.now(),
                        endTime = Instant.now(),
                        startZoneOffset = null,
                        endZoneOffset = null,
                        samples = listOf(HeartRateRecord.Sample(Instant.now(), 72))
                    )
                )
            }
        }
    }



    // Fondo degradado
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFBBDEFB), Color(0xFF1976D2))
                )
            )
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Mi Band Dashboard") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF1565C0),
                        titleContentColor = Color.White
                    )
                )
            },
            containerColor = Color.Transparent
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    when {
                        isLoading -> {
                            CircularProgressIndicator(color = Color.White)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Cargando datos...", color = Color.White)
                        }
                        errorMessage != null -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Error",
                                    tint = Color.Red
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = errorMessage ?: "Error desconocido",
                                    color = Color.Red,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        isLoading = true
                                        errorMessage = null
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                                ) {
                                    Text("Reintentar", color = Color(0xFF1976D2))
                                }
                            }
                        }
                        else -> {


                            //val totalSteps = steps?.sumOf { it.count } ?: 0L
                            val todaySteps = steps?.filter { record ->
                                val recordDate = record.startTime.atZone(ZoneId.systemDefault()).toLocalDate()
                                val today = LocalDate.now(ZoneId.systemDefault())
                                recordDate == today
                            }?.sumOf { it.count } ?: 0L


                            //val animatedSteps by animateIntAsState(targetValue = totalSteps.toInt(), label = "animatedSteps")
                            val animatedSteps by animateIntAsState(
                                targetValue = todaySteps.toInt(),
                                label = "animatedSteps"
                            )

                            HealthMetricCard("👣 Pasos hoy", animatedSteps.toString(), Color(0xFF43A047))

                            val todayHeartRate = heartRate?.filter { record ->
                                isToday(record.startTime) // ← Solo registros de hoy
                            }?.flatMap { record ->
                                record.samples // ← Muestras de hoy
                            }?.map { sample ->
                                sample.beatsPerMinute // ← Valores BPM de hoy
                            }?.average()?.toInt() ?: 0
                            //animacion
                            val animatedtodayHeartRate= remember { Animatable(0f) }

                            val todayHeartRateSamples = heartRate?.filter { record ->
                                val recordDate = record.startTime.atZone(ZoneId.systemDefault()).toLocalDate()
                                val today = LocalDate.now(ZoneId.systemDefault())
                                recordDate == today
                            }?.flatMap { it.samples } ?: emptyList()

                            val latestHeartRate = if (todayHeartRateSamples.isNotEmpty()) {
                                todayHeartRateSamples.maxByOrNull { it.time }?.beatsPerMinute ?: 0
                            } else {
                                0

                            }

                           /* val avgHeartRate = heartRate?.flatMap { it.samples }
                                ?.map { it.beatsPerMinute }
                                ?.average()?.toInt() ?: 0*/


                            //animacion
                            val animatedHeartRate = remember { Animatable(0f) }

                            //calorias totales - corregiod
                            val todayCaloriess = totalCalories?.filter { record ->
                                // Filtramos de los 7 días cargados, solo los registros de hoy
                                val recordDate = record.startTime.atZone(ZoneId.systemDefault()).toLocalDate()
                                val today = LocalDate.now(ZoneId.systemDefault())
                                recordDate == today
                            }?.sumOf { record ->
                                // ✅ CORRECCIÓN CLAVE: Dividir por 1000.0 para pasar de 'cal' a 'kcal'
                                record.energy.inCalories / 1000.0
                            } ?: 0.0

                            val formattedCaloriess = "%.0f".format(todayCaloriess)

                           HealthMetricCard("🔥 Calorías totales", "${formattedCaloriess} kcal", Color(0xFFFF9800))




                            LaunchedEffect(latestHeartRate) {
                                animatedHeartRate.snapTo(0f) // opcional: forzar inicio en 0
                                animatedHeartRate.animateTo(
                                    targetValue = latestHeartRate.toFloat(),
                                    animationSpec = tween(durationMillis = 12000) // duración personalizada
                                )
                            }

                                //hoy
                            LaunchedEffect(todayHeartRate) {
                                animatedtodayHeartRate .snapTo(0f) // opcional: forzar inicio en 0
                                animatedtodayHeartRate.animateTo(
                                    targetValue = todayHeartRate.toFloat(),
                                    animationSpec = tween(durationMillis = 12000) // duración personalizada
                                )
                            }


                         // val animatedHeartRate by animateIntAsState(targetValue = avgHeartRate)
                            HealthMetricCard("❤️ Ritmo cardíaco (actual)", "${animatedtodayHeartRate.value.toInt()} bpm", Color(0xFFE53935))

                            HealthMetricCard("❤️ Ritmo cardíaco (promedio)", "${animatedHeartRate.value.toInt()} bpm", Color(0xFFE53935))





                                            // ✅ NUEVO: DISTANCIA (metros a kilómetros)
                                            val todayDistance = distance?.filter { record ->
                                                val recordDate = record.startTime.atZone(ZoneId.systemDefault()).toLocalDate()
                                                val today = LocalDate.now(ZoneId.systemDefault())
                                                recordDate == today
                                            }?.sumOf { it.distance.inMeters } ?: 0.0

                                            val distanceKm = todayDistance / 1000.0
                                            val formattedDistance = "%.2f".format(distanceKm)
                                            HealthMetricCard("📏 Distancia hoy", "${formattedDistance}km", Color(0xFF2196F3))



                                            // ✅ NUEVO: CALORÍAS ACTIVAS
                                            val todayCalories = activeCalories?.filter { record ->
                                                val recordDate = record.startTime.atZone(ZoneId.systemDefault()).toLocalDate()
                                                val today = LocalDate.now(ZoneId.systemDefault())
                                                recordDate == today

                                            }?.sumOf { record -> // Usamos 'record' para mayor claridad
                                                // 💡 CORRECCIÓN APLICADA: Dividir por 1000.0 para pasar a kcal
                                                record.energy.inCalories / 1.0
                                            } ?: 0.0

                            val formattedCalories = "%.1f".format(todayCalories)
                                            HealthMetricCard("🔥 Calorías activas", "${formattedCalories} cal", Color(0xFFFF9800))



                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    isLoading = true
                                    errorMessage = null
                                    scope.launch {
                                        try {
                                            val stepsResponse = client.readRecords(
                                                ReadRecordsRequest(
                                                    recordType = StepsRecord::class,
                                                    timeRangeFilter = TimeRangeFilter.between(
                                                        Instant.now().minus(7, ChronoUnit.DAYS),
                                                        Instant.now()
                                                    )
                                                )
                                            )
                                            steps = stepsResponse.records
                                            val heartRateResponse = client.readRecords(
                                                ReadRecordsRequest(
                                                    recordType = HeartRateRecord::class,
                                                    timeRangeFilter = TimeRangeFilter.between(
                                                        Instant.now().minus(7, ChronoUnit.DAYS),
                                                        Instant.now()
                                                    )
                                                )
                                            )
                                            heartRate = heartRateResponse.records


                                            // ✅ NUEVO: Distancia
                                            val distanceResponse = client.readRecords(
                                                ReadRecordsRequest(
                                                    recordType = DistanceRecord::class,
                                                    timeRangeFilter = TimeRangeFilter.between(
                                                        Instant.now().minus(7, ChronoUnit.DAYS),
                                                        Instant.now()
                                                    )
                                                )
                                            )
                                            // ✅ NUEVO: Distancia

                                            distance = distanceResponse.records
                                            // ✅ NUEVO: Calorías activas

                                            val caloriesResponse = client.readRecords(
                                                ReadRecordsRequest(
                                                    recordType = ActiveCaloriesBurnedRecord::class,
                                                    timeRangeFilter = TimeRangeFilter.between(
                                                        Instant.now().minus(1, ChronoUnit.DAYS),
                                                        Instant.now()
                                                    )
                                                )
                                            )
                                            activeCalories = caloriesResponse.records




                                            isLoading = false
                                        } catch (e: Exception) {
                                            isLoading = false
                                            errorMessage = "Error al recargar: ${e.message}"
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                            ) {
                                Text("Actualizar datos", color = Color(0xFF1976D2))
                            }
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f))
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.Gray
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = valueColor
            )
        }
    }
}

private fun isToday(instant: Instant): Boolean {
    val recordDate = instant.atZone(ZoneId.systemDefault()).toLocalDate()
    val today = LocalDate.now(ZoneId.systemDefault())
    return recordDate == today
}
