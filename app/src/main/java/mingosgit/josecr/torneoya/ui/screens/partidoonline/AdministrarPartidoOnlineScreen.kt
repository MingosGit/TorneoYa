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
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import mingosgit.josecr.torneoya.ui.theme.text
import mingosgit.josecr.torneoya.ui.theme.mutedText

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
                .background(TorneoYaPalette.backgroundGradient),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    val nombreEquipoA = equipoA?.nombre ?: stringResource(R.string.adminp_label_nombre_equipo_a)
    val nombreEquipoB = equipoB?.nombre ?: stringResource(R.string.adminp_label_nombre_equipo_b)
    val fecha = partido?.fecha.orEmpty()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TorneoYaPalette.backgroundGradient)
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
                                    listOf(
                                        MaterialTheme.colorScheme.surfaceVariant,
                                        MaterialTheme.colorScheme.surface
                                    )
                                )
                            )
                            .border(
                                width = 2.dp,
                                brush = Brush.horizontalGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.secondary
                                    )
                                ),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            Icons.Filled.KeyboardArrowLeft,
                            contentDescription = stringResource(R.string.gen_volver),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.adminp_title),
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.text,
                    fontWeight = FontWeight.Black
                )
                Spacer(Modifier.weight(1f))
                if (esCreador) {
                    IconButton(
                        onClick = {
                            navController?.navigate("administrar_roles_online/$partidoUid")
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.surfaceVariant,
                                        MaterialTheme.colorScheme.surface
                                    )
                                )
                            )
                            .border(
                                width = 2.dp,
                                brush = Brush.horizontalGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.secondary
                                    )
                                ),
                                shape = CircleShape
                            )
                    ) {
                        Text(
                            text = "👤",
                            fontSize = 22.sp
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
                        color = MaterialTheme.colorScheme.mutedText,
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
                                val fechaSel = "%02d-%02d-%04d".format(
                                    cal.get(Calendar.DAY_OF_MONTH),
                                    cal.get(Calendar.MONTH) + 1,
                                    cal.get(Calendar.YEAR)
                                )
                                viewModel.setFechaEditable(fechaSel)
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
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = MaterialTheme.colorScheme.mutedText,
                                unfocusedLabelColor = MaterialTheme.colorScheme.mutedText
                            )
                        )

                        Spacer(Modifier.height(8.dp))

                        // Minutos por parte
                        OutlinedTextField(
                            value = tiempoPorParteEditable.toString(),
                            onValueChange = { s -> s.toIntOrNull()?.let { viewModel.setTiempoPorParteEditable(it) } },
                            label = { Text(stringResource(R.string.adminp_label_minutos_por_parte),color = MaterialTheme.colorScheme.mutedText) },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = MaterialTheme.colorScheme.mutedText,
                                unfocusedLabelColor = MaterialTheme.colorScheme.mutedText
                            )
                        )
                        Spacer(Modifier.height(8.dp))

                        // Minutos descanso
                        OutlinedTextField(
                            value = tiempoDescansoEditable.toString(),
                            onValueChange = { s -> s.toIntOrNull()?.let { viewModel.setTiempoDescansoEditable(it) } },
                            label = { Text(stringResource(R.string.adminp_label_minutos_descanso),color = MaterialTheme.colorScheme.mutedText) },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = MaterialTheme.colorScheme.mutedText,
                                unfocusedLabelColor = MaterialTheme.colorScheme.mutedText
                            )
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
                            label = { Text(stringResource(R.string.adminp_label_nombre_equipo_a),color = MaterialTheme.colorScheme.mutedText) },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = MaterialTheme.colorScheme.mutedText,
                                unfocusedLabelColor = MaterialTheme.colorScheme.mutedText
                            )
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
                            label = { Text(stringResource(R.string.adminp_label_nombre_equipo_b),color = MaterialTheme.colorScheme.mutedText) },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = MaterialTheme.colorScheme.mutedText,
                                unfocusedLabelColor = MaterialTheme.colorScheme.mutedText
                            )
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
                    Text(
                        stringResource(R.string.adminp_goles_registrados),
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
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
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(
                                width = 1.3.dp,
                                brush = Brush.horizontalGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.secondary
                                    )
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
                            color = MaterialTheme.colorScheme.text
                        )
                        IconButton(
                            onClick = { viewModel.borrarGol(gol) }
                        ) {
                            Icon(
                                Icons.Default.Remove,
                                contentDescription = stringResource(R.string.adminp_btn_quitar_gol),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Divider(color = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.adminp_btn_agregar_gol),
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
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
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = MaterialTheme.colorScheme.mutedText,
                                unfocusedLabelColor = MaterialTheme.colorScheme.mutedText
                            ),
                            label = { Text(stringResource(R.string.adminp_label_equipo),color = MaterialTheme.colorScheme.mutedText) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedEquipo) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()

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
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = MaterialTheme.colorScheme.mutedText,
                                unfocusedLabelColor = MaterialTheme.colorScheme.mutedText
                            ),
                            label = { Text(stringResource(R.string.adminp_label_jugador),color = MaterialTheme.colorScheme.mutedText) },
                            placeholder = { Text(stringResource(R.string.adminp_placeholder_seleccionar_jugador)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedJugador) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
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
                                    Divider(color = MaterialTheme.colorScheme.outline)
                                }
                                jugadoresManual.forEach { jugador ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                jugador.nombre + " (manual)",
                                                color = MaterialTheme.colorScheme.mutedText
                                            )
                                        },
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
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = MaterialTheme.colorScheme.mutedText,
                                unfocusedLabelColor = MaterialTheme.colorScheme.mutedText
                            ),
                            onValueChange = { minuto = it.filter { c -> c.isDigit() }.take(3) },
                            label = {
                                Text(
                                    stringResource(R.string.adminp_label_minuto),
                                    color = MaterialTheme.colorScheme.mutedText
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                            ),
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
                                label = {
                                    Text(
                                        stringResource(R.string.adminp_label_asistencia_opcional),
                                        color = MaterialTheme.colorScheme.mutedText
                                    )
                                },
                                placeholder = {
                                    Text(
                                        stringResource(R.string.adminp_placeholder_sin_asistencia),
                                        color = MaterialTheme.colorScheme.mutedText
                                    )
                                },
                                enabled = jugadorSeleccionado != null,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAsistente) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = MaterialTheme.colorScheme.mutedText,
                                    unfocusedLabelColor = MaterialTheme.colorScheme.mutedText
                                )
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
                                        Divider(color = MaterialTheme.colorScheme.outline)
                                    }
                                    asistentesPosiblesManual.forEach { jugador ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    jugador.nombre + " (manual)",
                                                    color = MaterialTheme.colorScheme.mutedText
                                                )
                                            },
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
                        Divider(color = MaterialTheme.colorScheme.outline)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = stringResource(R.string.adminp_eliminar_partido),
                            color = MaterialTheme.colorScheme.error,
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
                            Text(if (deleting) "Eliminando..." else stringResource(R.string.adminp_eliminar_partido))
                        }
                        Spacer(Modifier.height(24.dp))
                    }
                }

                if (showDialog) {
                    item {
                        AlertDialog(
                            onDismissRequest = { showDialog = false },
                            title = { Text(stringResource(R.string.adminp_dialog_confirmar_cambios_titulo), color = MaterialTheme.colorScheme.text) },
                            text = { Text(stringResource(R.string.adminp_dialog_confirmar_cambios_mensaje), color = MaterialTheme.colorScheme.mutedText) },
                            containerColor = MaterialTheme.colorScheme.surface,
                            confirmButton = {
                                TextButton(
                                    onClick = { navController?.popBackStack() }
                                ) { Text(stringResource(R.string.gen_guardar), color = MaterialTheme.colorScheme.primary) }
                            },
                            dismissButton = {
                                OutlinedButton(onClick = { showDialog = false }) {
                                    Text(stringResource(R.string.gen_cancelar), color = MaterialTheme.colorScheme.secondary)
                                }
                            }
                        )
                    }
                }
            }

            DeleteMatchDialog(
                visible = showDeleteDialog && esCreador,
                deleting = deleting,
                onDismiss = { if (!deleting) showDeleteDialog = false },
                onConfirm = {
                    if (!esCreador) return@DeleteMatchDialog
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
                }
            )
        }
    }
}

@Composable
private fun DeleteMatchDialog(
    visible: Boolean,
    deleting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    if (!visible) return

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(
                    width = 2.dp,
                    brush = Brush.horizontalGradient(
                        listOf(
                            MaterialTheme.colorScheme.error,
                            MaterialTheme.colorScheme.secondary
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .background(MaterialTheme.colorScheme.surface)
                .padding(20.dp)
        ) {
            Text(
                text = stringResource(R.string.adminp_eliminar_partido),
                color = MaterialTheme.colorScheme.text,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.adminp_desc_eliminar_partido),
                color = MaterialTheme.colorScheme.mutedText,
                fontSize = 14.sp
            )

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // IZQUIERDA: CANCELAR
                ModernOutlineButton(
                    enabled = !deleting,
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(45.dp)
                ) {
                    Text(
                        text = stringResource(R.string.gen_cancelar),
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.width(12.dp))
                // DERECHA: ELIMINAR
                ModernDangerOutlineButton(
                    enabled = !deleting,
                    onClick = onConfirm,
                    modifier = Modifier
                        .weight(1f)
                        .height(45.dp)
                ) {
                    Text(
                        text = if (deleting) "Eliminando..." else stringResource(R.string.gen_eliminar),
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
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
                    listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary
                    )
                ),
                shape = RoundedCornerShape(13.dp)
            )
            .background(
                Brush.horizontalGradient(
                    listOf(
                        MaterialTheme.colorScheme.surfaceVariant,
                        MaterialTheme.colorScheme.surface
                    )
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
                androidx.compose.material3.LocalContentColor provides if (enabled)
                    MaterialTheme.colorScheme.text
                else
                    MaterialTheme.colorScheme.mutedText,
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
                    listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary
                    )
                ),
                shape = RoundedCornerShape(13.dp)
            )
            .background(androidx.compose.ui.graphics.Color.Transparent)
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CompositionLocalProvider(
                androidx.compose.material3.LocalContentColor provides MaterialTheme.colorScheme.primary.copy(
                    alpha = if (enabled) 1f else 0.45f
                ),
                content = { content() }
            )
        }
    }
}

// Botón de peligro (usa error del esquema)
@Composable
fun ModernDangerButton(
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    val error = MaterialTheme.colorScheme.error
    Box(
        modifier = modifier
            .height(45.dp)
            .clip(RoundedCornerShape(13.dp))
            .border(
                width = 2.dp,
                brush = Brush.horizontalGradient(
                    listOf(error, error.copy(alpha = 0.6f))
                ),
                shape = RoundedCornerShape(13.dp)
            )
            .background(
                Brush.horizontalGradient(
                    listOf(
                        error.copy(alpha = 0.25f),
                        error.copy(alpha = 0.18f)
                    )
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
                androidx.compose.material3.LocalContentColor provides if (enabled)
                    MaterialTheme.colorScheme.text
                else
                    MaterialTheme.colorScheme.text.copy(alpha = 0.4f),
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
    val error = MaterialTheme.colorScheme.error
    Box(
        modifier = modifier
            .height(45.dp)
            .clip(RoundedCornerShape(13.dp))
            .border(
                width = 2.dp,
                brush = Brush.horizontalGradient(
                    listOf(error, MaterialTheme.colorScheme.secondary)
                ),
                shape = RoundedCornerShape(13.dp)
            )
            .background(androidx.compose.ui.graphics.Color.Transparent)
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CompositionLocalProvider(
                androidx.compose.material3.LocalContentColor provides if (enabled) error else error.copy(alpha = 0.4f),
                content = { content() }
            )
        }
    }
}
