package mingosgit.josecr.torneoya.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CrearPartidoScreen(
    onPartidoCreado: (List<List<String>>, Int, Long, List<String>) -> Unit,
    jugadores: List<String>,
    onAgregarJugadores: () -> Unit,
    onNumIntegrantesChange: (Int) -> Unit,
    onNumEquiposChange: (Int) -> Unit,
    numIntegrantes: Int,
    numEquipos: Int,
    fechaMillisState: MutableState<Long?>,
    nombresEquiposState: MutableState<List<String>>,
    horaSeteadaState: MutableState<Boolean>,
    aleatorio: Boolean,
    equiposManuales: List<List<String>>,
    onEquiposManualesChange: (List<List<String>>) -> Unit
) {
    val maxIntegrantes = 24

    var tiempo by rememberSaveable { mutableStateOf(10) }
    var showError by rememberSaveable { mutableStateOf(false) }
    var errorTrigger by remember { mutableStateOf(0) }

    val context = LocalContext.current
    val fechaMillis = fechaMillisState.value

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val fechaString = fechaMillis?.let {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(it))
    } ?: ""
    val horaString = fechaMillis?.let {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(it))
    } ?: ""

    var nombresEquipos by remember { nombresEquiposState }
    val nombresInit = remember(numEquipos) { mutableStateOf(false) }
    if (!nombresInit.value) {
        if (nombresEquipos.size != numEquipos) {
            nombresEquipos = List(numEquipos) { "" }
            nombresEquiposState.value = nombresEquipos
        }
        nombresInit.value = true
    }

    LaunchedEffect(numEquipos) {
        if (nombresEquipos.size != numEquipos) {
            val nuevos = nombresEquipos.toMutableList()
            while (nuevos.size < numEquipos) nuevos.add("")
            while (nuevos.size > numEquipos) nuevos.removeAt(nuevos.size - 1)
            nombresEquipos = nuevos
            nombresEquiposState.value = nuevos
        }
    }

    // RESETEAR ERROR SI CAMBIA ALGO
    LaunchedEffect(
        jugadores,
        numIntegrantes,
        numEquipos,
        fechaMillis,
        equiposManuales,
        nombresEquipos,
        tiempo,
        aleatorio,
        errorTrigger
    ) {
        showError = false
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text("Crear Partido", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = fechaString,
            onValueChange = {},
            label = { Text("Fecha") },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDatePicker = true },
            enabled = false,
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(Icons.Default.DateRange, contentDescription = "Seleccionar fecha")
                }
            }
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = horaString,
            onValueChange = {},
            label = { Text("Hora") },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showTimePicker = true },
            enabled = false,
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { showTimePicker = true }) {
                    Icon(Icons.Default.AccessTime, contentDescription = "Seleccionar hora")
                }
            }
        )
        Spacer(Modifier.height(12.dp))

        Button(
            onClick = { onAgregarJugadores() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Agregar jugadores (${jugadores.size})")
        }
        Spacer(Modifier.height(8.dp))
        if (jugadores.isNotEmpty()) {
            Text("Jugadores: " + jugadores.joinToString(", "), style = MaterialTheme.typography.bodyMedium)
        } else {
            Text("No hay jugadores agregados.", style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(Modifier.height(12.dp))

        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Text("Integrantes/equipo:")
            Spacer(Modifier.width(8.dp))
            OutlinedTextField(
                value = numIntegrantes.toString(),
                onValueChange = {
                    val v = it.toIntOrNull() ?: 1
                    val safeV = v.coerceIn(1, maxIntegrantes)
                    onNumIntegrantesChange(safeV)
                    errorTrigger++
                },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.width(70.dp)
            )
        }
        Spacer(Modifier.height(8.dp))

        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Text("Minutos por partido:")
            Spacer(Modifier.width(8.dp))
            OutlinedTextField(
                value = tiempo.toString(),
                onValueChange = {
                    tiempo = (it.toIntOrNull() ?: 1).coerceIn(1, 240)
                    errorTrigger++
                },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.width(90.dp)
            )
        }
        Spacer(Modifier.height(12.dp))

        Spacer(Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
        ) {
            Text("Nombre de los equipos:")
            Spacer(Modifier.height(4.dp))
            repeat(numEquipos) { equipoIdx ->
                OutlinedTextField(
                    value = nombresEquipos.getOrElse(equipoIdx) { "" },
                    onValueChange = { newValue ->
                        val nuevos = nombresEquipos.toMutableList()
                        nuevos[equipoIdx] = newValue
                        nombresEquipos = nuevos
                        nombresEquiposState.value = nuevos
                        errorTrigger++
                    },
                    label = { Text("Equipo ${equipoIdx + 1}") },
                    singleLine = true,
                    modifier = Modifier
                        .width(220.dp)
                        .padding(vertical = 2.dp)
                )
            }
        }
        Spacer(Modifier.height(16.dp))

        if (showError) {
            Text("¡Faltan datos obligatorios o cantidad incorrecta!", color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                val fechaFinalMillis = fechaMillisState.value
                val nombresFinal = nombresEquipos.mapIndexed { idx, name ->
                    if (name.trim().isEmpty()) "Equipo${idx + 1}" else name.trim()
                }
                if (fechaFinalMillis == null) {
                    showError = true
                    return@Button
                }
                if (aleatorio) {
                    if (jugadores.size != numIntegrantes * numEquipos || jugadores.any { it.isBlank() }) {
                        showError = true
                        return@Button
                    }
                    showError = false
                    val jugadoresMezclados = jugadores.shuffled()
                    val equiposFinales = List(numEquipos) { i ->
                        jugadoresMezclados.drop(i * numIntegrantes).take(numIntegrantes)
                    }
                    onPartidoCreado(equiposFinales, tiempo, fechaFinalMillis, nombresFinal)
                } else {
                    // VALIDACIÓN MODO MANUAL CORREGIDA
                    // Debe estar equiposManuales.size == numEquipos Y CADA EQUIPO size == numIntegrantes Y NINGÚN NOMBRE VACÍO
                    val esValido = equiposManuales.size == numEquipos &&
                            equiposManuales.all { it.size == numIntegrantes && it.all { nombre -> nombre.isNotBlank() } }
                    if (!esValido) {
                        showError = true
                        return@Button
                    }
                    showError = false
                    onPartidoCreado(equiposManuales, tiempo, fechaFinalMillis, nombresFinal)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Text("Crear Partido")
        }
    }

    if (showDatePicker) {
        val cal = Calendar.getInstance()
        fechaMillis?.let { cal.timeInMillis = it }
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val c = Calendar.getInstance()
                c.timeInMillis = fechaMillis ?: System.currentTimeMillis()
                c.set(Calendar.YEAR, year)
                c.set(Calendar.MONTH, month)
                c.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                if (!horaSeteadaState.value) {
                    c.set(Calendar.HOUR_OF_DAY, 12)
                    c.set(Calendar.MINUTE, 0)
                }
                fechaMillisState.value = c.timeInMillis
                showDatePicker = false
                errorTrigger++
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).apply { show() }
        showDatePicker = false
    }

    if (showTimePicker) {
        val cal = Calendar.getInstance()
        fechaMillis?.let { cal.timeInMillis = it }
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                val c = Calendar.getInstance()
                c.timeInMillis = fechaMillis ?: System.currentTimeMillis()
                c.set(Calendar.HOUR_OF_DAY, hourOfDay)
                c.set(Calendar.MINUTE, minute)
                fechaMillisState.value = c.timeInMillis
                horaSeteadaState.value = true
                showTimePicker = false
                errorTrigger++
            },
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            true
        ).apply { show() }
        showTimePicker = false
    }
}
