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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import mingosgit.josecr.torneoya.R
import mingosgit.josecr.torneoya.data.firebase.*
import mingosgit.josecr.torneoya.viewmodel.partidoonline.AdministrarPartidoOnlineViewModel
import java.util.Calendar
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import mingosgit.josecr.torneoya.ui.theme.TorneoYaPalette


private fun NavController.popBackStack(times: Int) {
    repeat(times) { if (!popBackStack()) return }
}
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

    // NUEVO: diálogo y estado para eliminar partido
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val repo = remember { PartidoFirebaseRepository() }

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

    val nombreEquipoA = equipoA?.nombre ?: stringResource(R.string.adminp_label_nombre_equipo_a)
    val nombreEquipoB = equipoB?.nombre ?: stringResource(R.string.adminp_label_nombre_equipo_b)
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
                            contentDescription = stringResource(R.string.gen_volver),
                            tint = TorneoYaPalette.violet,
                            modifier = Modifier.size(27.dp)
                        )
                    }
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.adminp_title),
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
                            contentDescription = stringResource(R.string.adminp_administrar_roles_desc),
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
                        text = stringResource(R.string.adminp_uid_fecha, partidoUid, fecha),
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
                        ) { Text(stringResource(R.string.adminp_btn_seleccionar_fecha, fechaEditable)) }

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
                        ) { Text(stringResource(R.string.adminp_btn_seleccionar_hora, horaEditable)) }

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

                        // Nº PARTES
                        OutlinedTextField(
                            value = numeroPartesEditable.toString(),
                            onValueChange = { s -> s.toIntOrNull()?.let { viewModel.setNumeroPartesEditable(it) } },
                            label = { Text(stringResource(R.string.adminp_label_num_partes)) },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                        )
                        Spacer(Modifier.height(8.dp))

                        // Minutos por parte
                        OutlinedTextField(
                            value = tiempoPorParteEditable.toString(),
                            onValueChange = { s -> s.toIntOrNull()?.let { viewModel.setTiempoPorParteEditable(it) } },
                            label = { Text(stringResource(R.string.adminp_label_minutos_por_parte)) },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                        )
                        Spacer(Modifier.height(8.dp))

                        // Minutos descanso
                        OutlinedTextField(
                            value = tiempoDescansoEditable.toString(),
                            onValueChange = { s -> s.toIntOrNull()?.let { viewModel.setTiempoDescansoEditable(it) } },
                            label = { Text(stringResource(R.string.adminp_label_minutos_descanso)) },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                        )
                        Spacer(Modifier.height(8.dp))
                        ModernMainButton(
                            onClick = { viewModel.actualizarDatosPartido() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.adminp_btn_guardar_cambios))
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
                            label = { Text(stringResource(R.string.adminp_label_nombre_equipo_a)) },
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(8.dp))
                        ModernMainButton(
                            onClick = { viewModel.actualizarNombreEquipoA() },
                            enabled = nombreEquipoAEditable.isNotBlank() && nombreEquipoAEditable != nombreEquipoA
                        ) {
                            Text(stringResource(R.string.gen_guardar))
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
                            label = { Text(stringResource(R.string.adminp_label_nombre_equipo_b)) },
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(8.dp))
                        ModernMainButton(
                            onClick = { viewModel.actualizarNombreEquipoB() },
                            enabled = nombreEquipoBEditable.isNotBlank() && nombreEquipoBEditable != nombreEquipoB
                        ) {
                            Text(stringResource(R.string.gen_guardar))
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                item {
                    Text(stringResource(R.string.adminp_goles_registrados), color = TorneoYaPalette.violet, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(goles) { gol ->
                    val equipoNombre = when (gol.equipoUid) {
                        equipoA?.uid -> nombreEquipoA
                        equipoB?.uid -> nombreEquipoB
                        else -> stringResource(R.string.adminp_label_equipo)
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
                            text = "$equipoNombre - ${stringResource(R.string.adminp_label_jugador)}: $jugadorNombre" +
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
                            Icon(Icons.Default.Remove, contentDescription = stringResource(R.string.adminp_btn_quitar_gol), tint = Color(0xFFF25A6D))
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Divider(color = Color(0xFF353659))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(stringResource(R.string.adminp_btn_agregar_gol), color = TorneoYaPalette.violet, fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
                            label = { Text(stringResource(R.string.adminp_label_equipo)) },
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
                            label = { Text(stringResource(R.string.adminp_label_jugador)) },
                            placeholder = { Text(stringResource(R.string.adminp_placeholder_seleccionar_jugador)) },
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
                            label = { Text(stringResource(R.string.adminp_label_minuto)) },
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
                                label = { Text(stringResource(R.string.adminp_label_asistencia_opcional)) },
                                placeholder = { Text(stringResource(R.string.adminp_placeholder_sin_asistencia)) },
                                enabled = jugadorSeleccionado != null,
                                modifier = Modifier.menuAnchor().weight(1f)
                            )
                            ExposedDropdownMenu(
                                expanded = expandedAsistente,
                                onDismissRequest = { expandedAsistente = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.adminp_placeholder_sin_asistencia)) },
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
                        Text(stringResource(R.string.adminp_btn_agregar_gol))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    ModernMainButton(
                        onClick = { showDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.adminp_btn_guardar_y_salir))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                item {
                    ModernOutlineButton(
                        onClick = { navController?.navigate("administrar_jugadores_online/$partidoUid/${equipoA?.uid}/${equipoB?.uid}") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.adminp_btn_editar_jugadores))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // ======== NUEVA SECCIÓN: ELIMINAR PARTIDO (SOLO CREADOR) ========
                if (esCreador) {
                    item {
                        Divider(color = Color(0xFF353659))
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "Eliminar Partido",
                            color = Color(0xFFF25A6D),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Spacer(Modifier.height(8.dp))
                        ModernDangerButton(
                            onClick = { showDeleteDialog = true },
                            enabled = !deleting,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (deleting) "Eliminando..." else "Eliminar Partido")
                        }
                        Spacer(Modifier.height(24.dp))
                    }
                }



                if (showDialog) {
                    item {
                        AlertDialog(
                            onDismissRequest = { showDialog = false },
                            title = { Text(stringResource(R.string.adminp_dialog_confirmar_cambios_titulo), color = Color.White) },
                            text = { Text(stringResource(R.string.adminp_dialog_confirmar_cambios_mensaje), color = Color(0xFFB7B7D1)) },
                            containerColor = Color(0xFF1C1D25),
                            confirmButton = {
                                TextButton(
                                    onClick = { navController?.popBackStack() }
                                ) { Text(stringResource(R.string.gen_guardar), color = TorneoYaPalette.blue) }
                            },
                            dismissButton = {
                                OutlinedButton(onClick = { showDialog = false }) {
                                    Text(stringResource(R.string.gen_cancelar), color = TorneoYaPalette.violet)
                                }
                            }
                        )
                    }
                }

                if (showDeleteDialog && esCreador) {
                    item {
                        AlertDialog(
                            onDismissRequest = { if (!deleting) showDeleteDialog = false },
                            title = { Text("Eliminar partido", color = Color.White, fontWeight = FontWeight.Bold) },
                            text = { Text("Se eliminará el partido, todos los jugadores manuales, comentarios, encuestas y votos asociados. Esta acción no se puede deshacer.", color = Color(0xFFB7B7D1)) },
                            containerColor = Color(0xFF1C1D25),
                            confirmButton = {
                                TextButton(
                                    enabled = !deleting,
                                    onClick = {
                                        if (!esCreador) return@TextButton
                                        deleting = true
                                        scope.launch {
                                            try {
                                                repo.eliminarPartidoCompleto(partidoUid, usuarioUid)
                                                showDeleteDialog = false
                                                navController?.popBackStack(2)
                                            } catch (e: Exception) {
                                                deleting = false
                                                // Manejar error si es necesario
                                            }
                                        }
                                    }
                                ) { Text("Eliminar", color = Color(0xFFF25A6D)) }
                            },
                            dismissButton = {
                                OutlinedButton(enabled = !deleting, onClick = { showDeleteDialog = false }) {
                                    Text("Cancelar", color = TorneoYaPalette.violet)
                                }
                            }
                        )
                    }
                }
            }
            if (showDeleteDialog && esCreador) {
                androidx.compose.ui.window.Dialog(
                    onDismissRequest = { if (!deleting) showDeleteDialog = false }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(
                                width = 2.dp,
                                brush = Brush.horizontalGradient(listOf(TorneoYaPalette.blue, TorneoYaPalette.violet)),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .background(
                                Brush.verticalGradient(
                                    0f to Color(0xFF1C1D25),
                                    1f to Color(0xFF14151B)
                                )
                            )
                            .padding(18.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "Eliminar partido",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp
                            )
                            Text(
                                text = "Se eliminará el partido, todos los jugadores manuales, comentarios, encuestas y votos asociados. Esta acción no se puede deshacer.",
                                color = Color(0xFFB7B7D1),
                                fontSize = 14.sp
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(45.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // CANCELAR (BORDE DEGRADADO AZUL->MORADO)
                                ModernOutlineButton(
                                    enabled = !deleting,
                                    onClick = { showDeleteDialog = false },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Cancelar")
                                }

                                // ELIMINAR (BORDE DEGRADADO ROJO->MORADO, TEXTO ROJO)
                                ModernDangerOutlineButton(
                                    enabled = !deleting,
                                    onClick = {
                                        if (!esCreador) return@ModernDangerOutlineButton
                                        deleting = true
                                        scope.launch {
                                            try {
                                                repo.eliminarPartidoCompleto(partidoUid, usuarioUid)
                                                showDeleteDialog = false
                                                navController?.popBackStack(2)
                                            } catch (_: Exception) {
                                                deleting = false
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(if (deleting) "Eliminando..." else "Eliminar")
                                }
                            }
                        }
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

// Botón de peligro (rojo)
@Composable
fun ModernDangerButton(
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
                    listOf(Color(0xFFF25A6D), Color(0xFFFF8DA1))
                ),
                shape = RoundedCornerShape(13.dp)
            )
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFF3A1E25), Color(0xFF2A171C))
                )
            )
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CompositionLocalProvider(
                LocalContentColor provides if (enabled) Color(0xFFFFDCE2) else Color(0x66FFDCE2),
                content = { content() }
            )
        }
    }
}
@Composable
fun ModernDangerOutlineButton(
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
                    listOf(Color(0xFFF25A6D), TorneoYaPalette.violet)
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
                LocalContentColor provides if (enabled) Color(0xFFF25A6D) else Color(0x66F25A6D),
                content = { content() }
            )
        }
    }
}
