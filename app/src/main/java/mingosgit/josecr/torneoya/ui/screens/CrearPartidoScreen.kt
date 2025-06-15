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
    horaSeteadaState: MutableState<Boolean>
) {
    val maxIntegrantes = 24
    val maxEquipos = 4

    var tiempo by rememberSaveable { mutableStateOf(10) }
    var aleatorio by rememberSaveable { mutableStateOf(true) }
    var equiposManuales by rememberSaveable(numEquipos, numIntegrantes, aleatorio) {
        mutableStateOf(List(numEquipos) { List(numIntegrantes) { "" } })
    }
    var showError by rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current

    val calendar = Calendar.getInstance()
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

    // AGREGADO: Permitir scroll general en pantalla (útil en pantallas pequeñas)
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text("Crear Partido", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))

        // Selector de fecha
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
                },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.width(70.dp)
            )
        }
        Spacer(Modifier.height(8.dp))

        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Text("Cantidad de equipos:")
            Spacer(Modifier.width(8.dp))
            OutlinedTextField(
                value = numEquipos.toString(),
                onValueChange = {
                    val v = it.toIntOrNull() ?: 2
                    val safeV = v.coerceIn(2, maxEquipos)
                    onNumEquiposChange(safeV)
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
                onValueChange = { tiempo = (it.toIntOrNull() ?: 1).coerceIn(1, 240) },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.width(90.dp)
            )
        }
        Spacer(Modifier.height(12.dp))

        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Text("¿Equipos aleatorios?")
            Spacer(Modifier.width(8.dp))
            Switch(
                checked = aleatorio,
                onCheckedChange = { aleatorio = it }
            )
        }

        Spacer(Modifier.height(16.dp))

        // ENTRADA DE NOMBRES DE EQUIPOS: Scroll horizontal
        Column(
            modifier = Modifier
                .horizontalScroll(rememberScrollState()) // <--- Scroll horizontal
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
                    },
                    label = { Text("Equipo ${equipoIdx + 1}") },
                    singleLine = true,
                    modifier = Modifier
                        .width(220.dp) // Ancho fijo para facilitar swipe horizontal
                        .padding(vertical = 2.dp)
                )
            }
        }
        Spacer(Modifier.height(16.dp))

        // Para la asignación manual, permitir también scroll horizontal si hay muchos equipos
        Box(
            modifier = Modifier
                .weight(1f, fill = false)
                .horizontalScroll(rememberScrollState()) // <--- Scroll horizontal equipos
        ) {
            if (aleatorio) {
                Text("Total de jugadores: ${jugadores.size}")
            } else {
                Column {
                    Text("Integrantes de cada equipo:")
                    Spacer(Modifier.height(8.dp))
                    repeat(numEquipos) { equipoIdx ->
                        Text("Equipo ${equipoIdx + 1}", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(4.dp))
                        repeat(numIntegrantes) { integranteIdx ->
                            OutlinedTextField(
                                value = equiposManuales[equipoIdx][integranteIdx],
                                onValueChange = { newValue ->
                                    equiposManuales = equiposManuales.toMutableList().also { equiposList ->
                                        equiposList[equipoIdx] = equiposList[equipoIdx].toMutableList().also { intList ->
                                            intList[integranteIdx] = newValue
                                        }
                                    }
                                },
                                label = { Text("Integrante ${integranteIdx + 1}") },
                                singleLine = true,
                                modifier = Modifier
                                    .width(220.dp) // Ancho fijo para facilitar scroll
                                    .padding(vertical = 2.dp)
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }

        if (showError) {
            Text("¡Faltan datos obligatorios o cantidad incorrecta!", color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(16.dp))

        // BOTÓN PARA CREAR PARTIDO: más visible y siempre presente
        Button(
            onClick = {
                val fechaFinalMillis = fechaMillisState.value
                if (jugadores.size != numIntegrantes * numEquipos || fechaFinalMillis == null) {
                    showError = true
                    return@Button
                }
                val nombresFinal = nombresEquipos.mapIndexed { idx, name ->
                    if (name.trim().isEmpty()) "Equipo${idx + 1}" else name.trim()
                }
                if (aleatorio) {
                    showError = false
                    val jugadoresMezclados = jugadores.shuffled()
                    val equiposFinales = List(numEquipos) { i ->
                        jugadoresMezclados.drop(i * numIntegrantes).take(numIntegrantes)
                    }
                    onPartidoCreado(equiposFinales, tiempo, fechaFinalMillis, nombresFinal)
                } else {
                    if (equiposManuales.flatten().any { it.isBlank() }) {
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

    // DatePickerDialog y TimePickerDialog
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
            },
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            true
        ).apply { show() }
        showTimePicker = false
    }
}
