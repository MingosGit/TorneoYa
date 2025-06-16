package mingosgit.josecr.torneoya.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import mingosgit.josecr.torneoya.data.entities.PartidoEntity
import mingosgit.josecr.torneoya.viewmodel.CreatePartidoViewModel
import java.util.Calendar

@Composable
fun CreatePartidoScreen(
    navController: NavController,
    createPartidoViewModel: CreatePartidoViewModel
) {
    val context = LocalContext.current

    var equipoA by rememberSaveable { mutableStateOf("") }
    var equipoB by rememberSaveable { mutableStateOf("") }
    var fecha by rememberSaveable { mutableStateOf("") }
    var horaInicio by rememberSaveable { mutableStateOf("") }
    var numeroPartes by rememberSaveable { mutableStateOf("2") }
    var tiempoPorParte by rememberSaveable { mutableStateOf("25") }
    var numeroJugadores by rememberSaveable { mutableStateOf("5") }
    var partidoTempId by rememberSaveable { mutableStateOf(System.currentTimeMillis()) }

    var camposError by rememberSaveable { mutableStateOf(mapOf<String, Boolean>()) }
    var mostrarErrores by rememberSaveable { mutableStateOf(false) }

    val calendar = remember { Calendar.getInstance() }

    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                fecha = "%02d-%02d-%04d".format(dayOfMonth, month + 1, year)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    val timePickerDialog = remember {
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                horaInicio = "%02d:%02d".format(hourOfDay, minute)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )
    }

    fun validarCampos(): Boolean {
        val errores = mutableMapOf<String, Boolean>()

        // Nombre equipo A
        errores["equipoA"] = equipoA.isBlank()
        // Nombre equipo B
        errores["equipoB"] = equipoB.isBlank()
        // Fecha
        errores["fecha"] = fecha.isBlank()
        // Hora
        errores["horaInicio"] = horaInicio.isBlank()
        // Número de partes
        errores["numeroPartes"] = numeroPartes.isBlank() || numeroPartes.toIntOrNull() == null
        // Tiempo por parte
        errores["tiempoPorParte"] = tiempoPorParte.isBlank() || tiempoPorParte.toIntOrNull() == null
        // Número de jugadores
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
            Text("Crear Partido", fontSize = 28.sp, modifier = Modifier.padding(bottom = 24.dp))

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

            OutlinedTextField(
                value = equipoB,
                onValueChange = { equipoB = it },
                label = { Text("Nombre Equipo B") },
                singleLine = true,
                isError = mostrarErrores && camposError["equipoB"] == true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
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
                            if (mostrarErrores && camposError["horaInicio"] == true) Color(0xFFFFCDD2) else Color.Transparent
                        )
                ) {
                    Text(if (horaInicio.isBlank()) "Seleccionar hora" else horaInicio)
                }
            }
            if (mostrarErrores && (camposError["fecha"] == true || camposError["horaInicio"] == true)) {
                Row(Modifier.fillMaxWidth()) {
                    if (camposError["fecha"] == true)
                        Text("Falta la fecha", color = Color.Red, fontSize = 12.sp, modifier = Modifier.weight(1f))
                    if (camposError["horaInicio"] == true)
                        Text("Falta la hora", color = Color.Red, fontSize = 12.sp, modifier = Modifier.weight(1f))
                }
            }

            OutlinedTextField(
                value = numeroPartes,
                onValueChange = { numeroPartes = it.filter { c -> c.isDigit() } },
                label = { Text("Número de partes") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = mostrarErrores && camposError["numeroPartes"] == true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .background(
                        if (mostrarErrores && camposError["numeroPartes"] == true) Color(0xFFFFCDD2) else Color.Transparent
                    )
            )
            if (mostrarErrores && camposError["numeroPartes"] == true) {
                Text("Campo obligatorio o inválido", color = Color.Red, fontSize = 12.sp)
            }

            OutlinedTextField(
                value = tiempoPorParte,
                onValueChange = { tiempoPorParte = it.filter { c -> c.isDigit() } },
                label = { Text("Minutos por parte") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = mostrarErrores && camposError["tiempoPorParte"] == true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .background(
                        if (mostrarErrores && camposError["tiempoPorParte"] == true) Color(0xFFFFCDD2) else Color.Transparent
                    )
            )
            if (mostrarErrores && camposError["tiempoPorParte"] == true) {
                Text("Campo obligatorio o inválido", color = Color.Red, fontSize = 12.sp)
            }

            OutlinedTextField(
                value = numeroJugadores,
                onValueChange = { numeroJugadores = it.filter { c -> c.isDigit() } },
                label = { Text("Nº jugadores por equipo") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = mostrarErrores && camposError["numeroJugadores"] == true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .background(
                        if (mostrarErrores && camposError["numeroJugadores"] == true) Color(0xFFFFCDD2) else Color.Transparent
                    )
            )
            if (mostrarErrores && camposError["numeroJugadores"] == true) {
                Text("Campo obligatorio o inválido", color = Color.Red, fontSize = 12.sp)
            }

            Button(
                onClick = {
                    if (validarCampos()) {
                        navController.navigate("asignar_jugadores/$partidoTempId")
                    } else {
                        mostrarErrores = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
            ) {
                Text("Asignar Jugadores")
            }
        }

        Button(
            onClick = {
                mostrarErrores = true
                if (validarCampos()) {
                    val eqA = if (equipoA.isBlank()) "Equipo1" else equipoA
                    val eqB = if (equipoB.isBlank()) "Equipo2" else equipoB
                    val partido = PartidoEntity(
                        id = partidoTempId,
                        fecha = fecha,
                        horaInicio = horaInicio,
                        numeroPartes = numeroPartes.toIntOrNull() ?: 2,
                        tiempoPorParte = tiempoPorParte.toIntOrNull() ?: 25,
                        equipoA = eqA,
                        equipoB = eqB,
                        numeroJugadores = numeroJugadores.toIntOrNull() ?: 5
                    )
                    createPartidoViewModel.crearPartido(partido)
                    partidoTempId = System.currentTimeMillis()
                    mostrarErrores = false
                    navController.popBackStack()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            Text("Finalizar")
        }
    }
}
