// PartidoTabEventosOnline.kt
package mingosgit.josecr.torneoya.ui.screens.partidoonline.visualizacion.visualizarPartido

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import mingosgit.josecr.torneoya.R
import mingosgit.josecr.torneoya.ui.theme.mutedText
import mingosgit.josecr.torneoya.viewmodel.partidoonline.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartidoTabEventosOnline(
    partidoUid: String,
    uiState: VisualizarPartidoOnlineUiState,
    reloadKey: Int = 0,
    viewModel: VisualizarPartidoOnlineViewModel
) {
    val cs = MaterialTheme.colorScheme
    val descReloadGoals = stringResource(id = R.string.ponlineeve_desc_reload_goals)
    val iconGoal = stringResource(id = R.string.ponlineeve_icon_goal)
    val iconAssist = stringResource(id = R.string.ponlineeve_icon_assist)
    val textNoEvents = stringResource(id = R.string.ponlineeve_text_no_events)

    val listState = rememberLazyListState()
    val eventosUi by viewModel.uiStateEventos.collectAsState()

    // Mantén nombres que ya tenías en uiState (pero si VM los resuelve, los usa)
    val nombreA by remember { mutableStateOf(uiState.nombreEquipoA) }
    val nombreB by remember { mutableStateOf(uiState.nombreEquipoB) }

    // Diálogo de “Agregar evento”
    var showAddDialog by remember { mutableStateOf(false) }
    var expandedEquipo by remember { mutableStateOf(false) }
    var expandedJugador by remember { mutableStateOf(false) }
    var expandedAsistente by remember { mutableStateOf(false) }

    var equipoSeleccionadoUid by remember { mutableStateOf<String?>(null) }
    var equipoSeleccionadoNombre by remember { mutableStateOf("") }
    var jugadorSeleccionado by remember { mutableStateOf<JugadorOption?>(null) }
    var asistenteSeleccionado by remember { mutableStateOf<JugadorOption?>(null) }
    var minutoTexto by remember { mutableStateOf("") }

    // Cargar (o recargar) desde VM
    LaunchedEffect(partidoUid, reloadKey, eventosUi.reloadToken) {
        viewModel.cargarEventosYEquipos(partidoUid)
    }

    // Auto scroll arriba cuando llegan nuevos eventos
    val eventosSize = eventosUi.eventos.size
    val oldEventosSize = remember { mutableStateOf(0) }
    LaunchedEffect(eventosSize) {
        if (eventosSize > oldEventosSize.value) {
            listState.animateScrollToItem(0)
        }
        oldEventosSize.value = eventosSize
    }

    Box(
        Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Column(Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = { viewModel.recargar() }) {
                    Icon(Icons.Default.Refresh, contentDescription = descReloadGoals, tint = cs.primary)
                }
            }

            if (eventosUi.isLoading) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(color = cs.primary) }
            } else {
                if (eventosUi.eventos.isEmpty()) {
                    Text(
                        text = textNoEvents,
                        style = MaterialTheme.typography.bodyMedium,
                        color = cs.mutedText,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 0.dp),
                        state = listState
                    ) {
                        items(eventosUi.eventos) { evento ->
                            val isEquipoA = evento.equipoUid == eventosUi.equipoAUid

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isEquipoA) {
                                    Box(
                                        modifier = Modifier
                                            .weight(4f)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(
                                                brush = Brush.horizontalGradient(
                                                    listOf(cs.primary.copy(alpha = 0.18f), cs.surface)
                                                ),
                                                shape = RoundedCornerShape(16.dp)
                                            )
                                            .border(
                                                width = 2.dp,
                                                brush = Brush.horizontalGradient(listOf(cs.primary, cs.secondary)),
                                                shape = RoundedCornerShape(16.dp)
                                            )
                                            .padding(vertical = 11.dp, horizontal = 16.dp)
                                    ) {
                                        Column(horizontalAlignment = Alignment.Start) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(text = iconGoal, fontSize = 20.sp, modifier = Modifier.padding(end = 6.dp))
                                                Text(
                                                    text = evento.jugador,
                                                    fontWeight = FontWeight.Bold,
                                                    color = cs.primary,
                                                    fontSize = 16.sp,
                                                    modifier = Modifier.padding(end = 4.dp)
                                                )
                                            }
                                            if (!evento.asistente.isNullOrBlank()) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.padding(top = 2.dp)
                                                ) {
                                                    Text(text = iconAssist, fontSize = 16.sp, modifier = Modifier.padding(end = 4.dp))
                                                    Text(
                                                        text = evento.asistente!!,
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        color = cs.tertiary
                                                    )
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    Spacer(modifier = Modifier.weight(4f))
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(2f)
                                        .fillMaxHeight(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    evento.minuto?.let {
                                        Text(
                                            text = "${it}'",
                                            color = cs.mutedText,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 15.sp,
                                            modifier = Modifier
                                                .background(
                                                    color = cs.surfaceVariant,
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                                .padding(horizontal = 7.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                if (!isEquipoA) {
                                    Box(
                                        modifier = Modifier
                                            .weight(4f)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(
                                                brush = Brush.horizontalGradient(
                                                    listOf(cs.surface, cs.secondary.copy(alpha = 0.18f))
                                                ),
                                                shape = RoundedCornerShape(16.dp)
                                            )
                                            .border(
                                                width = 2.dp,
                                                brush = Brush.horizontalGradient(listOf(cs.secondary, cs.primary)),
                                                shape = RoundedCornerShape(16.dp)
                                            )
                                            .padding(vertical = 11.dp, horizontal = 16.dp)
                                    ) {
                                        Column(horizontalAlignment = Alignment.End) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = evento.jugador,
                                                    fontWeight = FontWeight.Bold,
                                                    color = cs.secondary,
                                                    fontSize = 16.sp,
                                                    modifier = Modifier.padding(end = 4.dp)
                                                )
                                                Text(text = iconGoal, fontSize = 20.sp, modifier = Modifier.padding(start = 6.dp))
                                            }
                                            if (!evento.asistente.isNullOrBlank()) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.End,
                                                    modifier = Modifier
                                                        .padding(top = 2.dp)
                                                        .fillMaxWidth()
                                                ) {
                                                    Text(
                                                        text = evento.asistente!!,
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        color = cs.tertiary
                                                    )
                                                    Text(text = iconAssist, fontSize = 16.sp, modifier = Modifier.padding(start = 4.dp))
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    Spacer(modifier = Modifier.weight(4f))
                                }
                            }
                        }
                    }
                }
            }
        }

        // FAB: abrir diálogo (solo si el VM indica permiso)
        if (eventosUi.puedeEditar) {
            val blue = mingosgit.josecr.torneoya.ui.theme.TorneoYaPalette.blue
            val violet = mingosgit.josecr.torneoya.ui.theme.TorneoYaPalette.violet

            FloatingActionButton(
                onClick = {
                    val preUid = eventosUi.equipoAUid ?: eventosUi.equipoBUid
                    equipoSeleccionadoUid = preUid
                    equipoSeleccionadoNombre = when (preUid) {
                        eventosUi.equipoAUid -> eventosUi.nombreEquipoA ?: nombreA
                        eventosUi.equipoBUid -> eventosUi.nombreEquipoB ?: nombreB
                        else -> ""
                    }
                    jugadorSeleccionado = null
                    asistenteSeleccionado = null
                    minutoTexto = ""
                    showAddDialog = true
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(14.dp)
                    .size(60.dp)
                    .border(
                        width = 2.dp,
                        brush = Brush.horizontalGradient(listOf(blue, violet)),
                        shape = CircleShape
                    )
                    .background(
                        brush = Brush.horizontalGradient(listOf(cs.surface, cs.surfaceVariant)),
                        shape = CircleShape
                    ),
                containerColor = androidx.compose.ui.graphics.Color.Transparent,
                contentColor = cs.onBackground,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
            ) {
                Text("＋", fontSize = 26.sp)
            }
        }

        if (showAddDialog) {
            Dialog(onDismissRequest = { if (!eventosUi.guardando) showAddDialog = false }) {
                val blue = mingosgit.josecr.torneoya.ui.theme.TorneoYaPalette.blue
                val violet = mingosgit.josecr.torneoya.ui.theme.TorneoYaPalette.violet
                val opcionesJugador =
                    if (equipoSeleccionadoUid == eventosUi.equipoAUid) eventosUi.jugadoresEquipoA else eventosUi.jugadoresEquipoB
                val opcionesAsistente =
                    (if (equipoSeleccionadoUid == eventosUi.equipoAUid) eventosUi.jugadoresEquipoA else eventosUi.jugadoresEquipoB)
                        .filter { it.nombre != jugadorSeleccionado?.nombre }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(
                            width = 2.dp,
                            brush = Brush.horizontalGradient(listOf(blue, violet)),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .background(
                            Brush.horizontalGradient(
                                listOf(cs.surface, cs.surfaceVariant)
                            )
                        )
                        .padding(18.dp)
                ) {
                    Text(
                        text = "Agregar evento",
                        color = cs.onBackground,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp
                    )
                    Spacer(Modifier.height(12.dp))

                    ExposedDropdownMenuBox(
                        expanded = expandedEquipo,
                        onExpandedChange = { expandedEquipo = !expandedEquipo }
                    ) {
                        OutlinedTextField(
                            readOnly = true,
                            value = equipoSeleccionadoNombre,
                            onValueChange = {},
                            label = { Text("Equipo") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedEquipo) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedEquipo,
                            onDismissRequest = { expandedEquipo = false }
                        ) {
                            if (!eventosUi.equipoAUid.isNullOrBlank()) {
                                DropdownMenuItem(
                                    text = { Text(eventosUi.nombreEquipoA ?: nombreA) },
                                    onClick = {
                                        equipoSeleccionadoUid = eventosUi.equipoAUid
                                        equipoSeleccionadoNombre = eventosUi.nombreEquipoA ?: nombreA
                                        expandedEquipo = false
                                        jugadorSeleccionado = null
                                        asistenteSeleccionado = null
                                    }
                                )
                            }
                            if (!eventosUi.equipoBUid.isNullOrBlank()) {
                                DropdownMenuItem(
                                    text = { Text(eventosUi.nombreEquipoB ?: nombreB) },
                                    onClick = {
                                        equipoSeleccionadoUid = eventosUi.equipoBUid
                                        equipoSeleccionadoNombre = eventosUi.nombreEquipoB ?: nombreB
                                        expandedEquipo = false
                                        jugadorSeleccionado = null
                                        asistenteSeleccionado = null
                                    }
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))

                    ExposedDropdownMenuBox(
                        expanded = expandedJugador,
                        onExpandedChange = { expandedJugador = !expandedJugador }
                    ) {
                        OutlinedTextField(
                            readOnly = true,
                            value = jugadorSeleccionado?.nombre ?: "",
                            onValueChange = {},
                            label = { Text("Jugador (obligatorio)") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedJugador) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedJugador,
                            onDismissRequest = { expandedJugador = false }
                        ) {
                            opcionesJugador.forEach { j ->
                                DropdownMenuItem(
                                    text = { Text(if (j.esManual) "${j.nombre} (manual)" else j.nombre) },
                                    onClick = {
                                        jugadorSeleccionado = j
                                        if (asistenteSeleccionado?.nombre == j.nombre) asistenteSeleccionado = null
                                        expandedJugador = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))

                    ExposedDropdownMenuBox(
                        expanded = expandedAsistente,
                        onExpandedChange = { expandedAsistente = !expandedAsistente }
                    ) {
                        OutlinedTextField(
                            readOnly = true,
                            value = asistenteSeleccionado?.nombre ?: "",
                            onValueChange = {},
                            label = { Text("Asistencia (opcional)") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAsistente) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedAsistente,
                            onDismissRequest = { expandedAsistente = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Sin asistencia") },
                                onClick = {
                                    asistenteSeleccionado = null
                                    expandedAsistente = false
                                }
                            )
                            opcionesAsistente.forEach { j ->
                                DropdownMenuItem(
                                    text = { Text(if (j.esManual) "${j.nombre} (manual)" else j.nombre) },
                                    onClick = {
                                        asistenteSeleccionado = j
                                        expandedAsistente = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = minutoTexto,
                        onValueChange = { minutoTexto = it.filter { c -> c.isDigit() }.take(3) },
                        label = { Text("Minuto (obligatorio)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(45.dp)
                                .clip(RoundedCornerShape(13.dp))
                                .border(
                                    width = 2.dp,
                                    brush = Brush.horizontalGradient(listOf(blue, violet)),
                                    shape = RoundedCornerShape(13.dp)
                                )
                                .background(androidx.compose.ui.graphics.Color.Transparent),
                            contentAlignment = Alignment.Center
                        ) {
                            TextButton(
                                enabled = !eventosUi.guardando,
                                onClick = { showAddDialog = false }
                            ) { Text("Cancelar", fontWeight = FontWeight.Bold) }
                        }

                        Spacer(Modifier.width(12.dp))

                        val puedeGuardar = !eventosUi.guardando &&
                                !equipoSeleccionadoUid.isNullOrBlank() &&
                                jugadorSeleccionado != null &&
                                minutoTexto.isNotBlank()

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(45.dp)
                                .clip(RoundedCornerShape(13.dp))
                                .border(
                                    width = 2.dp,
                                    brush = Brush.horizontalGradient(listOf(blue, violet)),
                                    shape = RoundedCornerShape(13.dp)
                                )
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(cs.surfaceVariant, cs.surface)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            TextButton(
                                enabled = puedeGuardar,
                                onClick = {
                                    viewModel.agregarEvento(
                                        partidoUid = partidoUid,
                                        equipoUid = equipoSeleccionadoUid!!,
                                        jugador = jugadorSeleccionado!!,
                                        minuto = minutoTexto.toInt(),
                                        asistente = asistenteSeleccionado
                                    ) {
                                        // onSuccess
                                        showAddDialog = false
                                    }
                                }
                            ) { Text(if (eventosUi.guardando) "Guardando..." else "Guardar", fontWeight = FontWeight.Bold) }
                        }
                    }
                }
            }
        }
    }
}
