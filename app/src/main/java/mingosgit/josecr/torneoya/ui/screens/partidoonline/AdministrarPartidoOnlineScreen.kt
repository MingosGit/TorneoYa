package mingosgit.josecr.torneoya.ui.screens.partidoonline

import android.app.TimePickerDialog
import android.widget.NumberPicker
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import mingosgit.josecr.torneoya.data.firebase.*
import mingosgit.josecr.torneoya.viewmodel.partidoonline.AdministrarPartidoOnlineViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdministrarPartidoOnlineScreen(
    partidoUid: String,
    navController: NavController? = null,
    viewModel: AdministrarPartidoOnlineViewModel

) {
    val loading by viewModel.loading.collectAsState()
    val partido by viewModel.partido.collectAsState()
    val equipoA by viewModel.equipoA.collectAsState()
    val equipoB by viewModel.equipoB.collectAsState()
    val jugadoresA by viewModel.jugadoresA.collectAsState()
    val jugadoresB by viewModel.jugadoresB.collectAsState()
    val goles by viewModel.goles.collectAsState()
    val nombreEquipoAEditable by viewModel.nombreEquipoAEditable.collectAsState()
    val nombreEquipoBEditable by viewModel.nombreEquipoBEditable.collectAsState()

    // NUEVO - Campos editables
    val fechaEditable by viewModel.fechaEditable.collectAsState()
    val horaEditable by viewModel.horaEditable.collectAsState()
    val numeroPartesEditable by viewModel.numeroPartesEditable.collectAsState()
    val tiempoPorParteEditable by viewModel.tiempoPorParteEditable.collectAsState()
    val tiempoDescansoEditable by viewModel.tiempoDescansoEditable.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var equipoSeleccionado by remember { mutableStateOf("A") }
    var jugadorSeleccionado by remember { mutableStateOf<JugadorFirebase?>(null) }
    var asistenciaSeleccionada by remember { mutableStateOf<JugadorFirebase?>(null) }
    var minuto by remember { mutableStateOf("") }
    var expandedEquipo by remember { mutableStateOf(false) }
    var expandedJugador by remember { mutableStateOf(false) }
    var expandedAsistente by remember { mutableStateOf(false) }

    // --- PICKERS STATE ---
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val calendar = Calendar.getInstance()

    var pickedDate by remember { mutableStateOf(fechaEditable) }
    var pickedHour by remember { mutableStateOf(horaEditable) }

    LaunchedEffect(partidoUid) {
        viewModel.recargarTodo()
    }

    if (loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    val nombreEquipoA = equipoA?.nombre ?: "Equipo A"
    val nombreEquipoB = equipoB?.nombre ?: "Equipo B"
    val fecha = partido?.fecha.orEmpty()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Administrar Goles (Online)") },
                navigationIcon = {
                    if (navController != null) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Filled.Remove, contentDescription = "Volver")
                        }
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Text(text = "UID: $partidoUid | Fecha: $fecha", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(12.dp))
            }

            item {
                Column(Modifier.fillMaxWidth()) {

                    // --- FECHA ---
                    var datePickerState = rememberDatePickerState()
                    Button(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Seleccionar fecha: $fechaEditable") }

                    if (showDatePicker) {
                        DatePickerDialog(
                            onDismissRequest = { showDatePicker = false },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        val selMillis = datePickerState.selectedDateMillis
                                        if (selMillis != null) {
                                            calendar.timeInMillis = selMillis
                                            val day = calendar.get(Calendar.DAY_OF_MONTH)
                                            val month = calendar.get(Calendar.MONTH) + 1
                                            val year = calendar.get(Calendar.YEAR)
                                            val fecha = "%02d-%02d-%04d".format(day, month, year)
                                            viewModel.setFechaEditable(fecha)
                                        }
                                        showDatePicker = false
                                    }
                                ) { Text("OK") }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
                            }
                        ) {
                            DatePicker(state = datePickerState)
                        }
                    }
                    Spacer(Modifier.height(8.dp))

                    // --- HORA ---
                    val context = LocalContext.current
                    Button(
                        onClick = { showTimePicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Seleccionar hora: $horaEditable") }

                    if (showTimePicker) {
                        val horaActual = horaEditable.split(":").getOrNull(0)?.toIntOrNull() ?: calendar.get(Calendar.HOUR_OF_DAY)
                        val minutoActual = horaEditable.split(":").getOrNull(1)?.toIntOrNull() ?: calendar.get(Calendar.MINUTE)

                        // Solo ejecuta una vez al abrir el picker
                        LaunchedEffect(showTimePicker) {
                            if (showTimePicker) {
                                TimePickerDialog(
                                    context,
                                    { _, hour: Int, minute: Int ->
                                        val sel = "%02d:%02d".format(hour, minute)
                                        viewModel.setHoraEditable(sel)
                                        showTimePicker = false
                                    },
                                    horaActual,
                                    minutoActual,
                                    true
                                ).show()
                            }
                        }
                    }


                    Spacer(Modifier.height(8.dp))

                    // Nº PARTES
                    OutlinedTextField(
                        value = numeroPartesEditable.toString(),
                        onValueChange = { s -> s.toIntOrNull()?.let { viewModel.setNumeroPartesEditable(it) } },
                        label = { Text("Nº Partes") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                    )
                    Spacer(Modifier.height(8.dp))

                    // Minutos por parte
                    OutlinedTextField(
                        value = tiempoPorParteEditable.toString(),
                        onValueChange = { s -> s.toIntOrNull()?.let { viewModel.setTiempoPorParteEditable(it) } },
                        label = { Text("Minutos por parte") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                    )
                    Spacer(Modifier.height(8.dp))

                    // Minutos descanso
                    OutlinedTextField(
                        value = tiempoDescansoEditable.toString(),
                        onValueChange = { s -> s.toIntOrNull()?.let { viewModel.setTiempoDescansoEditable(it) } },
                        label = { Text("Minutos descanso") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.actualizarDatosPartido() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Guardar cambios")
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            item {
                // ----- NOMBRE EQUIPO A -----
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = nombreEquipoAEditable,
                        onValueChange = { viewModel.setNombreEquipoAEditable(it) },
                        label = { Text("Nombre Equipo A") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { viewModel.actualizarNombreEquipoA() },
                        enabled = nombreEquipoAEditable.isNotBlank() && nombreEquipoAEditable != nombreEquipoA
                    ) {
                        Text("Guardar")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                // ----- NOMBRE EQUIPO B -----
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = nombreEquipoBEditable,
                        onValueChange = { viewModel.setNombreEquipoBEditable(it) },
                        label = { Text("Nombre Equipo B") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { viewModel.actualizarNombreEquipoB() },
                        enabled = nombreEquipoBEditable.isNotBlank() && nombreEquipoBEditable != nombreEquipoB
                    ) {
                        Text("Guardar")
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            item {
                Text("Goles registrados", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(goles) { gol ->
                val equipoNombre = when (gol.equipoUid) {
                    equipoA?.uid -> nombreEquipoA
                    equipoB?.uid -> nombreEquipoB
                    else -> "Equipo"
                }
                val jugadorNombre = (jugadoresA + jugadoresB).find { it.uid == gol.jugadorUid }?.nombre ?: ""
                val asistenciaNombre = (jugadoresA + jugadoresB).find { it.uid == gol.asistenciaJugadorUid }?.nombre
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$equipoNombre - Jugador: $jugadorNombre" +
                                (gol.minuto?.let { " (${it}') " } ?: "") +
                                (asistenciaNombre?.let { "Asist: $asistenciaNombre" } ?: ""),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = {
                            viewModel.borrarGol(gol)
                        }
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Quitar gol")
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))
                Text("Agregar gol", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                // Equipo
                ExposedDropdownMenuBox(
                    expanded = expandedEquipo,
                    onExpandedChange = { expandedEquipo = !expandedEquipo }
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = if (equipoSeleccionado == "A") nombreEquipoA else nombreEquipoB,
                        onValueChange = {},
                        label = { Text("Equipo") },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedEquipo,
                        onDismissRequest = { expandedEquipo = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(nombreEquipoA) },
                            onClick = {
                                equipoSeleccionado = "A"
                                jugadorSeleccionado = null
                                asistenciaSeleccionada = null
                                expandedEquipo = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(nombreEquipoB) },
                            onClick = {
                                equipoSeleccionado = "B"
                                jugadorSeleccionado = null
                                asistenciaSeleccionada = null
                                expandedEquipo = false
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                // Jugador (solo del equipo seleccionado)
                val jugadoresActual = if (equipoSeleccionado == "A") jugadoresA else jugadoresB
                ExposedDropdownMenuBox(
                    expanded = expandedJugador,
                    onExpandedChange = { expandedJugador = !expandedJugador }
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = jugadorSeleccionado?.nombre ?: "",
                        onValueChange = {},
                        label = { Text("Jugador") },
                        placeholder = { Text("Seleccionar jugador") },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedJugador,
                        onDismissRequest = { expandedJugador = false }
                    ) {
                        jugadoresActual.forEach { jugador ->
                            DropdownMenuItem(
                                text = { Text(jugador.nombre) },
                                onClick = {
                                    jugadorSeleccionado = jugador
                                    if (asistenciaSeleccionada?.uid == jugador.uid) asistenciaSeleccionada = null
                                    expandedJugador = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                // Minuto y Asistencia juntos
                val jugadoresActual = if (equipoSeleccionado == "A") jugadoresA else jugadoresB
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = minuto,
                        onValueChange = { minuto = it.filter { c -> c.isDigit() }.take(3) },
                        label = { Text("Min") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    ExposedDropdownMenuBox(
                        expanded = expandedAsistente,
                        onExpandedChange = { expandedAsistente = !expandedAsistente }
                    ) {
                        val asistentesPosibles = jugadoresActual.filter { it.uid != jugadorSeleccionado?.uid }
                        OutlinedTextField(
                            readOnly = true,
                            value = asistentesPosibles.find { it.uid == asistenciaSeleccionada?.uid }?.nombre ?: "",
                            onValueChange = {},
                            label = { Text("Asistencia (opcional)") },
                            placeholder = { Text("Sin asistencia") },
                            enabled = jugadorSeleccionado != null,
                            modifier = Modifier.menuAnchor().weight(1f)
                        )
                        ExposedDropdownMenu(
                            expanded = expandedAsistente,
                            onDismissRequest = { expandedAsistente = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Sin asistencia") },
                                onClick = {
                                    asistenciaSeleccionada = null
                                    expandedAsistente = false
                                }
                            )
                            asistentesPosibles.forEach { jugador ->
                                DropdownMenuItem(
                                    text = { Text(jugador.nombre) },
                                    onClick = {
                                        asistenciaSeleccionada = jugador
                                        expandedAsistente = false
                                    }
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Button(
                    onClick = {
                        val equipoUid = if (equipoSeleccionado == "A") equipoA?.uid else equipoB?.uid
                        if (equipoUid != null && jugadorSeleccionado != null) {
                            viewModel.agregarGol(
                                equipoUid = equipoUid,
                                jugadorUid = jugadorSeleccionado!!.uid,
                                minuto = minuto.toIntOrNull(),
                                asistenciaUid = asistenciaSeleccionada?.uid
                            )
                            jugadorSeleccionado = null
                            asistenciaSeleccionada = null
                            minuto = ""
                        }
                    },
                    enabled = jugadorSeleccionado != null && minuto.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Agregar gol")
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Button(
                    onClick = { showDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Guardar y salir")
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            item {
                Button(
                    onClick = { navController?.navigate("administrar_jugadores_online/$partidoUid/${equipoA?.uid}/${equipoB?.uid}") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Editar jugadores")
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (showDialog) {
                item {
                    AlertDialog(
                        onDismissRequest = { showDialog = false },
                        title = { Text("Confirmar cambios") },
                        text = { Text("¿Guardar y volver?") },
                        confirmButton = {
                            TextButton(
                                onClick = { navController?.popBackStack() }
                            ) { Text("Guardar") }
                        },
                        dismissButton = {
                            OutlinedButton(onClick = { showDialog = false }) {
                                Text("Cancelar")
                            }
                        }
                    )
                }
            }

        }
    }

}

@Composable
fun SimpleNumberPicker(value: Int, range: IntRange, onValueChange: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(
            onClick = { if (value > range.first) onValueChange(value - 1) }
        ) { Text("-") }
        Text("%02d".format(value), modifier = Modifier.padding(horizontal = 8.dp))
        IconButton(
            onClick = { if (value < range.last) onValueChange(value + 1) }
        ) { Text("+") }
    }
}

