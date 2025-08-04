package mingosgit.josecr.torneoya.ui.screens.partidoonline

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import mingosgit.josecr.torneoya.data.firebase.*
import mingosgit.josecr.torneoya.viewmodel.partidoonline.AdministrarPartidoOnlineViewModel
import java.util.Calendar
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import mingosgit.josecr.torneoya.ui.theme.TorneoYaPalette

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdministrarPartidoOnlineScreen(
    partidoUid: String,
    navController: NavController? = null,
    viewModel: AdministrarPartidoOnlineViewModel,
    usuarioUid: String
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

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val calendar = Calendar.getInstance()

    var pickedDate by remember { mutableStateOf(fechaEditable) }
    var pickedHour by remember { mutableStateOf(horaEditable) }

    var esCreador by remember { mutableStateOf(false) }

    val jugadoresManualA = partido?.nombresManualEquipoA?.map { nombre ->
        JugadorFirebase(uid = "", nombre = nombre, email = "")
    } ?: emptyList()

    val jugadoresManualB = partido?.nombresManualEquipoB?.map { nombre ->
        JugadorFirebase(uid = "", nombre = nombre, email = "")
    } ?: emptyList()

    LaunchedEffect(partidoUid, usuarioUid) {
        val firestore = FirebaseFirestore.getInstance()
        val snap = firestore.collection("partidos").document(partidoUid).get().await()
        val creadorUid = snap.getString("creadorUid") ?: ""
        esCreador = usuarioUid == creadorUid
    }

    LaunchedEffect(partidoUid) {
        viewModel.recargarTodo()
    }

    if (loading) {
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.0f to Color(0xFF1B1D29),
                        0.28f to Color(0xFF212442),
                        0.58f to Color(0xFF191A23),
                        1.0f to Color(0xFF14151B)
                    )
                ),
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator(color = TorneoYaPalette.blue) }
        return
    }

    val nombreEquipoA = equipoA?.nombre ?: "Equipo A"
    val nombreEquipoB = equipoB?.nombre ?: "Equipo B"
    val fecha = partido?.fecha.orEmpty()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0.0f to Color(0xFF1B1D29),
                    0.28f to Color(0xFF212442),
                    0.58f to Color(0xFF191A23),
                    1.0f to Color(0xFF14151B)
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // HEADER
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, start = 9.dp, end = 9.dp, bottom = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (navController != null) {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF23273D), Color(0xFF1C1D25))
                                )
                            )
                            .border(
                                width = 2.dp,
                                brush = Brush.horizontalGradient(
                                    listOf(TorneoYaPalette.blue, TorneoYaPalette.violet)
                                ),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            Icons.Filled.KeyboardArrowLeft,
                            contentDescription = "Volver",
                            tint = TorneoYaPalette.violet,
                            modifier = Modifier.size(27.dp)
                        )
                    }
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    "Administrar Goles (Online)",
                    fontSize = 22.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Black
                )
                Spacer(Modifier.weight(1f))
                if (esCreador) {
                    IconButton(onClick = {
                        navController?.navigate("administrar_roles_online/$partidoUid")
                    }) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Administrar Roles",
                            tint = TorneoYaPalette.blue
                        )
                    }
                }
            }

            Spacer(Modifier.height(7.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Text(
                        text = "UID: $partidoUid | Fecha: $fecha",
                        color = Color(0xFFB7B7D1),
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                item {
                    Column(Modifier.fillMaxWidth()) {
                        // ------ FECHA ------
                        ModernMainButton(
                            onClick = { showDatePicker = true },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Seleccionar fecha: $fechaEditable") }

                        CustomDatePickerDialog(
                            show = showDatePicker,
                            initialDate = Calendar.getInstance().apply {
                                val parts = fechaEditable.split("-")
                                if (parts.size == 3) {
                                    set(Calendar.DAY_OF_MONTH, parts[0].toIntOrNull() ?: get(Calendar.DAY_OF_MONTH))
                                    set(Calendar.MONTH, (parts[1].toIntOrNull() ?: (get(Calendar.MONTH) + 1)) - 1)
                                    set(Calendar.YEAR, parts[2].toIntOrNull() ?: get(Calendar.YEAR))
                                }
                            },
                            onDismiss = { showDatePicker = false },
                            onDateSelected = { cal ->
                                val fecha = "%02d-%02d-%04d".format(
                                    cal.get(Calendar.DAY_OF_MONTH),
                                    cal.get(Calendar.MONTH) + 1,
                                    cal.get(Calendar.YEAR)
                                )
                                viewModel.setFechaEditable(fecha)
                            }
                        )
                        Spacer(Modifier.height(8.dp))

// ------ HORA ------
                        ModernMainButton(
                            onClick = { showTimePicker = true },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Seleccionar hora: $horaEditable") }

                        CustomTimePickerDialog(
                            show = showTimePicker,
                            initialHour = horaEditable.split(":").getOrNull(0)?.toIntOrNull() ?: 12,
                            initialMinute = horaEditable.split(":").getOrNull(1)?.toIntOrNull() ?: 0,
                            onDismiss = { showTimePicker = false },
                            onTimeSelected = { hour, min ->
                                val hora = "%02d:%02d".format(hour, min)
                                viewModel.setHoraEditable(hora)
                            }
                        )
                        Spacer(Modifier.height(8.dp))


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
                        ModernMainButton(
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
                        ModernMainButton(
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
                        ModernMainButton(
                            onClick = { viewModel.actualizarNombreEquipoB() },
                            enabled = nombreEquipoBEditable.isNotBlank() && nombreEquipoBEditable != nombreEquipoB
                        ) {
                            Text("Guardar")
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                item {
                    Text("Goles registrados", color = TorneoYaPalette.violet, fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(11.dp))
                            .background(Color(0xFF23273D))
                            .border(
                                width = 1.3.dp,
                                brush = Brush.horizontalGradient(
                                    listOf(TorneoYaPalette.blue, TorneoYaPalette.violet)
                                ),
                                shape = RoundedCornerShape(11.dp)
                            )
                            .padding(vertical = 8.dp, horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$equipoNombre - Jugador: $jugadorNombre" +
                                    (gol.minuto?.let { " (${it}') " } ?: "") +
                                    (asistenciaNombre?.let { "Asist: $asistenciaNombre" } ?: ""),
                            modifier = Modifier.weight(1f),
                            color = Color(0xFFF7F7FF)
                        )
                        IconButton(
                            onClick = {
                                viewModel.borrarGol(gol)
                            }
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Quitar gol", tint = Color(0xFFF25A6D))
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Divider(color = Color(0xFF353659))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Agregar gol", color = TorneoYaPalette.violet, fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
                    val jugadoresOnline = if (equipoSeleccionado == "A") jugadoresA else jugadoresB
                    val jugadoresManual = if (equipoSeleccionado == "A") jugadoresManualA else jugadoresManualB

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
                            if (jugadoresOnline.isNotEmpty()) {
                                jugadoresOnline.forEach { jugador ->
                                    DropdownMenuItem(
                                        text = { Text(jugador.nombre) },
                                        onClick = {
                                            jugadorSeleccionado = jugador
                                            asistenciaSeleccionada = null
                                            expandedJugador = false
                                        }
                                    )
                                }
                            }
                            if (jugadoresManual.isNotEmpty()) {
                                if (jugadoresOnline.isNotEmpty()) {
                                    Divider(color = Color(0xFF353659))
                                }
                                jugadoresManual.forEach { jugador ->
                                    DropdownMenuItem(
                                        text = { Text(jugador.nombre + " (manual)", color = Color(0xFFB7B7D1)) },
                                        onClick = {
                                            jugadorSeleccionado = jugador
                                            asistenciaSeleccionada = null
                                            expandedJugador = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    // Minuto y Asistencia juntos
                    val jugadoresOnline = if (equipoSeleccionado == "A") jugadoresA else jugadoresB
                    val jugadoresManual = if (equipoSeleccionado == "A") jugadoresManualA else jugadoresManualB
                    val asistentesPosiblesOnline = jugadoresOnline.filter { it.uid != jugadorSeleccionado?.uid }
                    val asistentesPosiblesManual = jugadoresManual.filter { it.nombre != jugadorSeleccionado?.nombre }
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
                            OutlinedTextField(
                                readOnly = true,
                                value = asistenciaSeleccionada?.nombre ?: "",
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
                                if (asistentesPosiblesOnline.isNotEmpty()) {
                                    asistentesPosiblesOnline.forEach { jugador ->
                                        DropdownMenuItem(
                                            text = { Text(jugador.nombre) },
                                            onClick = {
                                                asistenciaSeleccionada = jugador
                                                expandedAsistente = false
                                            }
                                        )
                                    }
                                }
                                if (asistentesPosiblesManual.isNotEmpty()) {
                                    if (asistentesPosiblesOnline.isNotEmpty()) {
                                        Divider(color = Color(0xFF353659))
                                    }
                                    asistentesPosiblesManual.forEach { jugador ->
                                        DropdownMenuItem(
                                            text = { Text(jugador.nombre + " (manual)", color = Color(0xFFB7B7D1)) },
                                            onClick = {
                                                asistenciaSeleccionada = jugador
                                                expandedAsistente = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    ModernMainButton(
                        onClick = {
                            val equipoUid = if (equipoSeleccionado == "A") equipoA?.uid else equipoB?.uid
                            if (equipoUid != null && jugadorSeleccionado != null) {
                                if (jugadorSeleccionado!!.uid.isNotBlank()) {
                                    viewModel.agregarGol(
                                        equipoUid = equipoUid,
                                        jugadorUid = jugadorSeleccionado!!.uid,
                                        minuto = minuto.toIntOrNull(),
                                        asistenciaUid = asistenciaSeleccionada?.uid.takeIf { asistenciaSeleccionada?.uid?.isNotBlank() == true }
                                    )
                                } else {
                                    viewModel.agregarGolManual(
                                        equipoUid = equipoUid,
                                        nombreJugadorManual = jugadorSeleccionado!!.nombre,
                                        minuto = minuto.toIntOrNull(),
                                        nombreAsistenteManual = asistenciaSeleccionada?.nombre
                                    )
                                }
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
                    ModernMainButton(
                        onClick = { showDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Guardar y salir")
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                item {
                    ModernOutlineButton(
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
                            title = { Text("Confirmar cambios", color = Color.White) },
                            text = { Text("¿Guardar y volver?", color = Color(0xFFB7B7D1)) },
                            containerColor = Color(0xFF1C1D25),
                            confirmButton = {
                                TextButton(
                                    onClick = { navController?.popBackStack() }
                                ) { Text("Guardar", color = TorneoYaPalette.blue) }
                            },
                            dismissButton = {
                                OutlinedButton(onClick = { showDialog = false }) {
                                    Text("Cancelar", color = TorneoYaPalette.violet)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

// -------- BOTONES MODERNOS --------

@Composable
fun ModernMainButton(
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Box(
        modifier = modifier
            .height(45.dp)
            .clip(RoundedCornerShape(13.dp))
            .border(
                width = 2.dp,
                brush = Brush.horizontalGradient(
                    listOf(TorneoYaPalette.blue, TorneoYaPalette.violet)
                ),
                shape = RoundedCornerShape(13.dp)
            )
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFF23273D), Color(0xFF1C1D25))
                )
            )
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CompositionLocalProvider(
                LocalContentColor provides if (enabled) Color.White else Color(0xFFB7B7D1),
                content = { content() }
            )
        }
    }
}

@Composable
fun ModernOutlineButton(
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Box(
        modifier = modifier
            .height(45.dp)
            .clip(RoundedCornerShape(13.dp))
            .border(
                width = 2.dp,
                brush = Brush.horizontalGradient(
                    listOf(TorneoYaPalette.blue, TorneoYaPalette.violet)
                ),
                shape = RoundedCornerShape(13.dp)
            )
            .background(Color.Transparent)
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CompositionLocalProvider(
                LocalContentColor provides TorneoYaPalette.blue.copy(alpha = if (enabled) 1f else 0.45f),
                content = { content() }
            )
        }
    }
}
