package mingosgit.josecr.torneoya.ui.screens.partidoonline

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import mingosgit.josecr.torneoya.viewmodel.partidoonline.CreatePartidoOnlineViewModel

@Composable
fun CrearPartidoOnlineScreen(
    navController: NavController,
    viewModel: CreatePartidoOnlineViewModel
) {
    val context = LocalContext.current

    var equipoA by rememberSaveable { mutableStateOf("") }
    var equipoB by rememberSaveable { mutableStateOf("") }
    var fecha by rememberSaveable { mutableStateOf("") }
    var horaInicio by rememberSaveable { mutableStateOf("") }
    var numeroPartes by rememberSaveable { mutableStateOf("2") }
    var tiempoPorParte by rememberSaveable { mutableStateOf("25") }
    var tiempoDescanso by rememberSaveable { mutableStateOf("5") }
    var numeroJugadores by rememberSaveable { mutableStateOf("5") }
    var isPublic by rememberSaveable { mutableStateOf(true) }

    var camposError by rememberSaveable { mutableStateOf(mapOf<String, Boolean>()) }
    var mostrarErrores by rememberSaveable { mutableStateOf(false) }
    var guardando by remember { mutableStateOf(false) }

    val calendar = remember { java.util.Calendar.getInstance() }
    val scope = rememberCoroutineScope()

    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                fecha = "%02d-%02d-%04d".format(dayOfMonth, month + 1, year)
            },
            calendar.get(java.util.Calendar.YEAR),
            calendar.get(java.util.Calendar.MONTH),
            calendar.get(java.util.Calendar.DAY_OF_MONTH)
        )
    }

    val timePickerDialog = remember {
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                horaInicio = "%02d:%02d".format(hourOfDay, minute)
            },
            calendar.get(java.util.Calendar.HOUR_OF_DAY),
            calendar.get(java.util.Calendar.MINUTE),
            true
        )
    }

    fun validarCampos(): Boolean {
        val errores = mutableMapOf<String, Boolean>()
        errores["equipoA"] = equipoA.isBlank()
        errores["equipoB"] = equipoB.isBlank()
        errores["fecha"] = fecha.isBlank()
        errores["horaInicio"] = horaInicio.isBlank()
        errores["numeroPartes"] = numeroPartes.isBlank() || numeroPartes.toIntOrNull() == null
        errores["tiempoPorParte"] = tiempoPorParte.isBlank() || tiempoPorParte.toIntOrNull() == null
        errores["tiempoDescanso"] = tiempoDescanso.isBlank() || tiempoDescanso.toIntOrNull() == null
        errores["numeroJugadores"] = numeroJugadores.isBlank() || numeroJugadores.toIntOrNull() == null
        camposError = errores
        return !errores.values.any { it }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Crear Partido Online", fontSize = 28.sp, modifier = Modifier.padding(bottom = 24.dp))

            OutlinedTextField(
                value = equipoA,
                onValueChange = { equipoA = it },
                label = { Text("Nombre Equipo A") },
                singleLine = true,
                isError = mostrarErrores && camposError["equipoA"] == true,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (mostrarErrores && camposError["equipoA"] == true) Color(0xFFFFCDD2) else Color.Transparent
                    )
            )
            if (mostrarErrores && camposError["equipoA"] == true) {
                Text("Campo obligatorio", color = Color.Red, fontSize = 12.sp)
            }

            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = equipoB,
                onValueChange = { equipoB = it },
                label = { Text("Nombre Equipo B") },
                singleLine = true,
                isError = mostrarErrores && camposError["equipoB"] == true,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (mostrarErrores && camposError["equipoB"] == true) Color(0xFFFFCDD2) else Color.Transparent
                    )
            )
            if (mostrarErrores && camposError["equipoB"] == true) {
                Text("Campo obligatorio", color = Color.Red, fontSize = 12.sp)
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { datePickerDialog.show() },
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (mostrarErrores && camposError["fecha"] == true) Color(0xFFFFCDD2) else Color.Transparent
                        )
                ) {
                    Text(if (fecha.isBlank()) "Seleccionar fecha" else fecha)
                }
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedButton(
                    onClick = { timePickerDialog.show() },
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (mostrarErrores && camposError["horaInicio"] == true) Color(
                                0xFFFFCDD2
                            ) else Color.Transparent
                        )
                ) {
                    Text(if (horaInicio.isBlank()) "Seleccionar hora" else horaInicio)
                }
            }
            if (mostrarErrores && (camposError["fecha"] == true || camposError["horaInicio"] == true)) {
                Row(Modifier.fillMaxWidth()) {
                    if (camposError["fecha"] == true)
                        Text(
                            "Falta la fecha",
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier.weight(1f)
                        )
                    if (camposError["horaInicio"] == true)
                        Text(
                            "Falta la hora",
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier.weight(1f)
                        )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = numeroPartes,
                    onValueChange = { numeroPartes = it.filter { c -> c.isDigit() } },
                    label = { Text("Nº de partes") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = mostrarErrores && camposError["numeroPartes"] == true,
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (mostrarErrores && camposError["numeroPartes"] == true) Color(0xFFFFCDD2) else Color.Transparent
                        )
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = tiempoPorParte,
                    onValueChange = { tiempoPorParte = it.filter { c -> c.isDigit() } },
                    label = { Text("Min/parte") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = mostrarErrores && camposError["tiempoPorParte"] == true,
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (mostrarErrores && camposError["tiempoPorParte"] == true) Color(
                                0xFFFFCDD2
                            ) else Color.Transparent
                        )
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = tiempoDescanso,
                    onValueChange = { tiempoDescanso = it.filter { c -> c.isDigit() } },
                    label = { Text("Descanso (min)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = mostrarErrores && camposError["tiempoDescanso"] == true,
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (mostrarErrores && camposError["tiempoDescanso"] == true) Color(
                                0xFFFFCDD2
                            ) else Color.Transparent
                        )
                )
            }
            if (mostrarErrores && (camposError["numeroPartes"] == true || camposError["tiempoPorParte"] == true || camposError["tiempoDescanso"] == true)) {
                Row(Modifier.fillMaxWidth()) {
                    if (camposError["numeroPartes"] == true)
                        Text(
                            "Obligatorio o inválido",
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier.weight(1f)
                        )
                    if (camposError["tiempoPorParte"] == true)
                        Text(
                            "Obligatorio o inválido",
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier.weight(1f)
                        )
                    if (camposError["tiempoDescanso"] == true)
                        Text(
                            "Obligatorio o inválido",
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier.weight(1f)
                        )
                }
            }

            OutlinedTextField(
                value = numeroJugadores,
                onValueChange = { numeroJugadores = it.filter { c -> c.isDigit() } },
                label = { Text("Jugadores por equipo") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = mostrarErrores && camposError["numeroJugadores"] == true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .background(
                        if (mostrarErrores && camposError["numeroJugadores"] == true) Color(
                            0xFFFFCDD2
                        ) else Color.Transparent
                    )
            )
            if (mostrarErrores && camposError["numeroJugadores"] == true) {
                Text("Campo obligatorio o inválido", color = Color.Red, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 6.dp, bottom = 6.dp)
            ) {
                Checkbox(
                    checked = isPublic,
                    onCheckedChange = { isPublic = it }
                )
                Text("Partido público (compartible por link/UID)")
            }

            Button(
                onClick = {
                    if (validarCampos()) {
                        guardando = true
                        scope.launch {
                            viewModel.crearPartidoOnline(
                                equipoA = equipoA,
                                equipoB = equipoB,
                                fecha = fecha,
                                horaInicio = horaInicio,
                                numeroPartes = numeroPartes.toInt(),
                                tiempoPorParte = tiempoPorParte.toInt(),
                                tiempoDescanso = tiempoDescanso.toInt(),
                                numeroJugadores = numeroJugadores.toInt(),
                                isPublic = isPublic
                            ) { partidoUid, equipoAUid, equipoBUid ->
                                navController.navigate("asignar_jugadores_online/$partidoUid?equipoAUid=$equipoAUid&equipoBUid=$equipoBUid")
                            }
                            guardando = false
                        }
                        mostrarErrores = false
                    } else {
                        mostrarErrores = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                enabled = !guardando
            ) {
                Text("Crear y asignar jugadores")
            }
        }
    }
}
