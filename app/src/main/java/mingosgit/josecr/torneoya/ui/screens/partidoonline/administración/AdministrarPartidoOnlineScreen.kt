package mingosgit.josecr.torneoya.ui.screens.partidoonline.administración

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
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import mingosgit.josecr.torneoya.R
import mingosgit.josecr.torneoya.data.firebase.*
import mingosgit.josecr.torneoya.viewmodel.partidoonline.AdministrarPartidoOnlineViewModel
import java.util.Calendar
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import mingosgit.josecr.torneoya.ui.screens.partidoonline.visualizacion.CustomDatePickerDialog
import mingosgit.josecr.torneoya.ui.screens.partidoonline.visualizacion.CustomTimePickerDialog
import mingosgit.josecr.torneoya.ui.theme.TorneoYaPalette
import mingosgit.josecr.torneoya.ui.theme.text
import mingosgit.josecr.torneoya.ui.theme.mutedText

// Extensión para navegar atrás N veces
private fun NavController.popBackStack(times: Int) {
    repeat(times) { if (!popBackStack()) return }
}

@OptIn(ExperimentalMaterial3Api::class)
// Pantalla principal para administrar un partido online (fecha/hora, equipos, goles, borrado)
@Composable
fun AdministrarPartidoOnlineScreen(
    partidoUid: String,
    navController: NavController? = null,
    viewModel: AdministrarPartidoOnlineViewModel,
    usuarioUid: String
) {
    // ---- Estados expuestos por el ViewModel ----
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

    // ---- Estados locales de la pantalla ----
    var showDialog by remember { mutableStateOf(false) } // diálogo de confirmar guardado/salida
    var equipoSeleccionado by remember { mutableStateOf("A") } // equipo A/B para alta de gol
    var jugadorSeleccionado by remember { mutableStateOf<JugadorFirebase?>(null) } // goleador seleccionado
    var asistenciaSeleccionada by remember { mutableStateOf<JugadorFirebase?>(null) } // asistente seleccionado
    var minuto by remember { mutableStateOf("") } // minuto del gol
    var expandedEquipo by remember { mutableStateOf(false) } // menú desplegable equipo
    var expandedJugador by remember { mutableStateOf(false) } // menú desplegable jugador
    var expandedAsistente by remember { mutableStateOf(false) } // menú desplegable asistente

    var showDatePicker by remember { mutableStateOf(false) } // diálogo de fecha
    var showTimePicker by remember { mutableStateOf(false) } // diálogo de hora
    val calendar = Calendar.getInstance()

    var pickedDate by remember { mutableStateOf(fechaEditable) } // copia local de fecha
    var pickedHour by remember { mutableStateOf(horaEditable) } // copia local de hora

    var esCreador by remember { mutableStateOf(false) } // si el usuario es creador del partido

    // diálogo y estado para eliminar partido
    var showDeleteDialog by remember { mutableStateOf(false) } // visibilidad del diálogo de borrado
    var deleting by remember { mutableStateOf(false) } // estado de borrado en curso
    val scope = rememberCoroutineScope() // scope para corrutinas locales
    val repo = remember { PartidoFirebaseRepository() } // repo para eliminar partido

    // Mapea nombres manuales a JugadorFirebase "dummy" para listarlos
    val jugadoresManualA = partido?.nombresManualEquipoA?.map { nombre ->
        JugadorFirebase(uid = "", nombre = nombre, email = "")
    } ?: emptyList()

    val jugadoresManualB = partido?.nombresManualEquipoB?.map { nombre ->
        JugadorFirebase(uid = "", nombre = nombre, email = "")
    } ?: emptyList()

    // Comprueba si el usuario es creador del partido
    LaunchedEffect(partidoUid, usuarioUid) {
        val firestore = FirebaseFirestore.getInstance()
        val snap = firestore.collection("partidos").document(partidoUid).get().await()
        val creadorUid = snap.getString("creadorUid") ?: ""
        esCreador = usuarioUid == creadorUid
    }

    // Carga/recarga datos del partido al entrar
    LaunchedEffect(partidoUid) {
        viewModel.recargarTodo()
    }

    // Pantalla de carga centrada
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

    // ---- Derivados visuales ----
    val nombreEquipoA = equipoA?.nombre ?: stringResource(R.string.adminp_label_nombre_equipo_a)
    val nombreEquipoB = equipoB?.nombre ?: stringResource(R.string.adminp_label_nombre_equipo_b)
    val fecha = partido?.fecha.orEmpty()

    // Raíz de la pantalla con degradado de fondo
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TorneoYaPalette.backgroundGradient)
    ) {
        // Columna principal apilando header y contenido
        Column(modifier = Modifier.fillMaxSize()) {
            // HEADER: back, título y acceso a roles
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, start = 9.dp, end = 9.dp, bottom = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (navController != null) {
                    // Botón volver
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
                // Título de sección
                Text(
                    text = stringResource(R.string.adminp_title),
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.text,
                    fontWeight = FontWeight.Black
                )
                Spacer(Modifier.weight(1f))
                // Botón roles (solo visible si es creador)
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

            // Lista desplazable con toda la edición
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    // Subtítulo con uid y fecha
                    Text(
                        text = stringResource(R.string.adminp_uid_fecha, partidoUid, fecha),
                        color = MaterialTheme.colorScheme.mutedText,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                item {
                    // ---- Bloque: fecha, hora y tiempos ----
                    Column(Modifier.fillMaxWidth()) {
                        // ------ FECHA ------
                        ModernMainButton(
                            onClick = { showDatePicker = true },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(stringResource(R.string.adminp_btn_seleccionar_fecha, fechaEditable)) }

                        // Diálogo selector de fecha
                        CustomDatePickerDialog(
                            show = showDatePicker,
                            initialDate = Calendar.getInstance().apply {
                                val parts = fechaEditable.split("-")
                                if (parts.size == 3) {
                                    set(
                                        Calendar.DAY_OF_MONTH,
                                        parts[0].toIntOrNull() ?: get(Calendar.DAY_OF_MONTH)
                                    )
                                    set(
                                        Calendar.MONTH,
                                        (parts[1].toIntOrNull() ?: (get(Calendar.MONTH) + 1)) - 1
                                    )
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
                                viewModel.setFechaEditable(fechaSel) // guarda fecha en VM
                            }
                        )
                        Spacer(Modifier.height(8.dp))

                        // ------ HORA ------
                        ModernMainButton(
                            onClick = { showTimePicker = true },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(stringResource(R.string.adminp_btn_seleccionar_hora, horaEditable)) }

                        // Diálogo selector de hora
                        CustomTimePickerDialog(
                            show = showTimePicker,
                            initialHour = horaEditable.split(":").getOrNull(0)?.toIntOrNull() ?: 12,
                            initialMinute = horaEditable.split(":").getOrNull(1)?.toIntOrNull()
                                ?: 0,
                            onDismiss = { showTimePicker = false },
                            onTimeSelected = { hour, min ->
                                val hora = "%02d:%02d".format(hour, min)
                                viewModel.setHoraEditable(hora) // guarda hora en VM
                            }
                        )
                        Spacer(Modifier.height(8.dp))

                        // Nº PARTES
                        OutlinedTextField(
                            value = numeroPartesEditable.toString(),
                            onValueChange = { s -> s.toIntOrNull()?.let { viewModel.setNumeroPartesEditable(it) } },
                            label = { Text(stringResource(R.string.adminp_label_num_partes)) },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
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
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
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
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = MaterialTheme.colorScheme.mutedText,
                                unfocusedLabelColor = MaterialTheme.colorScheme.mutedText
                            )
                        )
                        Spacer(Modifier.height(8.dp))
                        // Botón guardar parámetros del partido
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
                        // Guardar nombre A
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
                        // Guardar nombre B
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
                    // ---- Cabecera de lista de goles ----
                    Text(
                        stringResource(R.string.adminp_goles_registrados),
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // ---- Ítems de goles existentes con opción de borrado ----
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
                        // Texto del gol con equipo, jugador, minuto y asistencia
                        Text(
                            text = "$equipoNombre - ${stringResource(R.string.adminp_label_jugador)}: $jugadorNombre" +
                                    (gol.minuto?.let { " (${it}') " } ?: "") +
                                    (asistenciaNombre?.let { "Asist: $asistenciaNombre" } ?: ""),
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.text
                        )
                        // Botón eliminar gol
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
                    // Cabecera para añadir gol
                    Text(
                        stringResource(R.string.adminp_btn_agregar_gol),
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    // Selector de equipo para el nuevo gol
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
                    // Selector de jugador (según equipo elegido) incluyendo manuales
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
                    // Fila: campo minuto y selector de asistente (opcional)
                    val jugadoresOnline = if (equipoSeleccionado == "A") jugadoresA else jugadoresB
                    val jugadoresManual = if (equipoSeleccionado == "A") jugadoresManualA else jugadoresManualB
                    val asistentesPosiblesOnline = jugadoresOnline.filter { it.uid != jugadorSeleccionado?.uid }
                    val asistentesPosiblesManual = jugadoresManual.filter { it.nombre != jugadorSeleccionado?.nombre }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        // Campo numérico de minuto
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
                                keyboardType = KeyboardType.Number
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        // Desplegable de asistente
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
                    // Botón que añade el gol (online o manual) y limpia campos
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
                    // Guardar y volver
                    ModernMainButton(
                        onClick = { showDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.adminp_btn_guardar_y_salir))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                item {
                    // Navegar a gestión de jugadores
                    ModernOutlineButton(
                        onClick = { navController?.navigate("administrar_jugadores_online/$partidoUid/${equipoA?.uid}/${equipoB?.uid}") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.adminp_btn_editar_jugadores))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // ======== Sección: eliminar partido (solo creador) ========
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
                        // Botón rojo de eliminar
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

                // Diálogo confirmar guardado y volver
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

            // Diálogo de eliminación con acciones
            DeleteMatchDialog(
                visible = showDeleteDialog && esCreador,
                deleting = deleting,
                onDismiss = { if (!deleting) showDeleteDialog = false },
                onConfirm = {
                    if (!esCreador) return@DeleteMatchDialog
                    deleting = true
                    scope.launch {
                        try {
                            repo.eliminarPartidoCompleto(partidoUid, usuarioUid) // borra partido y dependencias
                            showDeleteDialog = false
                            navController?.popBackStack(2) // vuelve dos pantallas
                        } catch (_: Exception) {
                            deleting = false
                        }
                    }
                }
            )
        }
    }
}

// Diálogo personalizado para confirmar la eliminación de un partido
@Composable
private fun DeleteMatchDialog(
    visible: Boolean,
    deleting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    if (!visible) return

    Dialog(onDismissRequest = onDismiss) {
        // Tarjeta con título, descripción y acciones Cancelar/Eliminar
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
                // Botón cancelar
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
                // Botón eliminar (contorno de peligro)
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

// Botón principal con relleno degradado y borde; contiene el slot de contenido
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
            // Propaga color según estado habilitado
            CompositionLocalProvider(
                LocalContentColor provides if (enabled)
                    MaterialTheme.colorScheme.text
                else
                    MaterialTheme.colorScheme.mutedText,
                content = { content() }
            )
        }
    }
}

// Botón secundario con solo borde; contiene el slot de contenido
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
            .background(Color.Transparent)
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Color principal atenuado si está deshabilitado
            CompositionLocalProvider(
                LocalContentColor provides MaterialTheme.colorScheme.primary.copy(
                    alpha = if (enabled) 1f else 0.45f
                ),
                content = { content() }
            )
        }
    }
}

// Botón de peligro (fondo tenue rojo) para acciones destructivas
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
            // Color de texto según habilitado
            CompositionLocalProvider(
                LocalContentColor provides if (enabled)
                    MaterialTheme.colorScheme.text
                else
                    MaterialTheme.colorScheme.text.copy(alpha = 0.4f),
                content = { content() }
            )
        }
    }
}

// Botón de peligro con solo borde (para confirmaciones)
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
            .background(Color.Transparent)
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Usa color de error; atenuado si deshabilitado
            CompositionLocalProvider(
                LocalContentColor provides if (enabled) error else error.copy(alpha = 0.4f),
                content = { content() }
            )
        }
    }
}
