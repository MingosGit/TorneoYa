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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import mingosgit.josecr.torneoya.R
import mingosgit.josecr.torneoya.data.firebase.EquipoFirebase
import mingosgit.josecr.torneoya.data.firebase.GoleadorFirebase
import mingosgit.josecr.torneoya.ui.theme.mutedText
import mingosgit.josecr.torneoya.viewmodel.partidoonline.VisualizarPartidoOnlineUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartidoTabEventosOnline(
    partidoUid: String,
    uiState: VisualizarPartidoOnlineUiState,
    reloadKey: Int = 0
) {
    val cs = MaterialTheme.colorScheme
    val descReloadGoals = stringResource(id = R.string.ponlineeve_desc_reload_goals)
    val iconGoal = stringResource(id = R.string.ponlineeve_icon_goal)
    val iconAssist = stringResource(id = R.string.ponlineeve_icon_assist)
    val textNoEvents = stringResource(id = R.string.ponlineeve_text_no_events)

    val scope = rememberCoroutineScope()
    var eventos by remember { mutableStateOf<List<GolEvento>>(emptyList()) }
    var equipoAUid by remember { mutableStateOf<String?>(null) }
    var equipoBUid by remember { mutableStateOf<String?>(null) }
    var nombreA by remember { mutableStateOf(uiState.nombreEquipoA) }
    var nombreB by remember { mutableStateOf(uiState.nombreEquipoB) }
    var isLoading by remember { mutableStateOf(true) }
    var triggerReload by remember { mutableStateOf(0) }
    val listState = rememberLazyListState()

    // Estado del diálogo para agregar evento
    var showAddDialog by remember { mutableStateOf(false) }
    var guardando by remember { mutableStateOf(false) }

    // Equipo seleccionado
    var expandedEquipo by remember { mutableStateOf(false) }
    var equipoSeleccionadoUid by remember { mutableStateOf<String?>(null) }
    var equipoSeleccionadoNombre by remember { mutableStateOf("") }

    // Jugadores por equipo (online + manual)
    data class JugadorOption(val uid: String?, val nombre: String, val esManual: Boolean)
    var jugadoresEquipoA by remember { mutableStateOf<List<JugadorOption>>(emptyList()) }
    var jugadoresEquipoB by remember { mutableStateOf<List<JugadorOption>>(emptyList()) }

    // Selección de jugador y asistente (obligatorio jugador, opcional asistencia)
    var expandedJugador by remember { mutableStateOf(false) }
    var expandedAsistente by remember { mutableStateOf(false) }
    var jugadorSeleccionado by remember { mutableStateOf<JugadorOption?>(null) }
    var asistenteSeleccionado by remember { mutableStateOf<JugadorOption?>(null) }

    // Minuto (OBLIGATORIO)
    var minutoTexto by remember { mutableStateOf("") }

    fun recargar() { triggerReload++ }

    // Carga eventos + nombres de equipos
    LaunchedEffect(partidoUid, reloadKey, triggerReload) {
        isLoading = true
        scope.launch {
            val db = FirebaseFirestore.getInstance()
            val snap = db.collection("partidos").document(partidoUid).get().await()
            equipoAUid = snap.getString("equipoAId")
            equipoBUid = snap.getString("equipoBId")

            equipoAUid?.let {
                val equipoA = db.collection("equipos").document(it).get().await().toObject(EquipoFirebase::class.java)
                if (equipoA?.nombre?.isNotBlank() == true) nombreA = equipoA.nombre
            }
            equipoBUid?.let {
                val equipoB = db.collection("equipos").document(it).get().await().toObject(EquipoFirebase::class.java)
                if (equipoB?.nombre?.isNotBlank() == true) nombreB = equipoB.nombre
            }

            val golesDocs = db.collection("goleadores")
                .whereEqualTo("partidoUid", partidoUid)
                .get().await()
                .documents

            val goles = golesDocs.mapNotNull { it.toObject(GoleadorFirebase::class.java)?.copy(uid = it.id) }

            // Mapa de nombres por UID (jugadores/usuarios)
            val jugadorUids = goles.mapNotNull { it.jugadorUid.takeIf { u -> !u.isNullOrBlank() } } +
                    goles.mapNotNull { it.asistenciaJugadorUid.takeIf { u -> !u.isNullOrBlank() } }
            val jugadoresDocs = jugadorUids.distinct()
                .filter { it.isNotBlank() }
                .mapNotNull { uid ->
                    val jugSnap = db.collection("jugadores").document(uid).get().await()
                    if (jugSnap.exists()) uid to (jugSnap.getString("nombre") ?: "") else null
                }
            val faltantes = jugadorUids.distinct().filter { it.isNotBlank() && jugadoresDocs.none { pair -> pair.first == it } }
            val usuariosDocs = faltantes.mapNotNull { uid ->
                val userSnap = db.collection("usuarios").document(uid).get().await()
                if (userSnap.exists()) uid to (userSnap.getString("nombreUsuario") ?: "") else null
            }
            val nombresMap = (jugadoresDocs + usuariosDocs).toMap()

            eventos = goles
                .sortedBy { it.minuto ?: Int.MIN_VALUE }
                .map { gol ->
                    val nombreJugador = when {
                        !gol.jugadorUid.isNullOrBlank() -> {
                            nombresMap[gol.jugadorUid]
                                ?: gol.jugadorNombreManual
                                ?: golesDocs.find { d -> d.id == gol.uid }?.getString("jugadorManual")
                                ?: textNoEvents
                        }
                        !gol.jugadorNombreManual.isNullOrBlank() -> gol.jugadorNombreManual
                        else -> golesDocs.find { d -> d.id == gol.uid }?.getString("jugadorManual") ?: textNoEvents
                    }
                    val nombreAsistente = when {
                        !gol.asistenciaJugadorUid.isNullOrBlank() -> {
                            nombresMap[gol.asistenciaJugadorUid]
                                ?: gol.asistenciaNombreManual
                                ?: golesDocs.find { d -> d.id == gol.uid }?.getString("asistenciaManual")
                                ?: textNoEvents
                        }
                        !gol.asistenciaNombreManual.isNullOrBlank() -> gol.asistenciaNombreManual
                        else -> golesDocs.find { d -> d.id == gol.uid }?.getString("asistenciaManual")
                    }
                    GolEvento(
                        equipoUid = gol.equipoUid,
                        jugador = nombreJugador ?: textNoEvents,
                        minuto = gol.minuto,
                        asistente = if (!nombreAsistente.isNullOrBlank()) nombreAsistente else null
                    )
                }
            isLoading = false
        }
    }

    // Carga roster de jugadores por equipo (online + manual) para el diálogo
    suspend fun cargarJugadoresPorEquipo(db: FirebaseFirestore, equipoId: String?): List<JugadorOption> {
        if (equipoId.isNullOrBlank()) return emptyList()
        val partidosSnap = db.collection("partidos").document(partidoUid).get().await()
        val jugadoresA = (partidosSnap.get("jugadoresEquipoA") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
        val jugadoresB = (partidosSnap.get("jugadoresEquipoB") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
        val manualA = (partidosSnap.get("nombresManualEquipoA") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
        val manualB = (partidosSnap.get("nombresManualEquipoB") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()

        val uids = if (equipoId == equipoAUid) jugadoresA else jugadoresB
        val manual = if (equipoId == equipoAUid) manualA else manualB

        // Resolver nombres por UID desde jugadores y usuarios
        val nombresPorUid = mutableMapOf<String, String>()
        for (uid in uids) {
            val jugSnap = db.collection("jugadores").document(uid).get().await()
            if (jugSnap.exists()) {
                jugSnap.getString("nombre")?.let { nombresPorUid[uid] = it }
                continue
            }
            val userSnap = db.collection("usuarios").document(uid).get().await()
            if (userSnap.exists()) {
                userSnap.getString("nombreUsuario")?.let { nombresPorUid[uid] = it }
            }
        }

        val online = uids.map { uid -> JugadorOption(uid = uid, nombre = nombresPorUid[uid] ?: uid, esManual = false) }
        val manualOpts = manual.map { nombre -> JugadorOption(uid = null, nombre = nombre, esManual = true) }
        return online + manualOpts
    }

    // Desplazamiento arriba si llegan nuevos eventos
    val eventosSize = eventos.size
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
                IconButton(onClick = { recargar() }) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = descReloadGoals,
                        tint = cs.primary
                    )
                }
            }

            if (isLoading) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(color = cs.primary) }
            } else {
                if (eventos.isEmpty()) {
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
                        items(eventos) { evento ->
                            val isEquipoA = evento.equipoUid == equipoAUid

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

        // FAB: abrir diálogo
        run {
            val cs = MaterialTheme.colorScheme
            val blue = mingosgit.josecr.torneoya.ui.theme.TorneoYaPalette.blue
            val violet = mingosgit.josecr.torneoya.ui.theme.TorneoYaPalette.violet

            // Botón estilo app (círculo con borde y fondo en gradiente)
            IconButton(
                onClick = {
                    val preUid = equipoAUid ?: equipoBUid
                    equipoSeleccionadoUid = preUid
                    equipoSeleccionadoNombre = when (preUid) {
                        equipoAUid -> nombreA
                        equipoBUid -> nombreB
                        else -> ""
                    }
                    jugadorSeleccionado = null
                    asistenteSeleccionado = null
                    minutoTexto = ""
                    showAddDialog = true

                    scope.launch {
                        val db = FirebaseFirestore.getInstance()
                        jugadoresEquipoA = cargarJugadoresPorEquipo(db, equipoAUid)
                        jugadoresEquipoB = cargarJugadoresPorEquipo(db, equipoBUid)
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(10.dp)
                    .size(56.dp)
                    .clip(CircleShape)
                    .border(
                        width = 2.dp,
                        brush = Brush.horizontalGradient(listOf(blue, violet)),
                        shape = CircleShape
                    )
                    .background(
                        Brush.horizontalGradient(
                            listOf(cs.surface, cs.surfaceVariant)
                        )
                    )
            ) {
                Text("＋", fontSize = 24.sp, color = cs.onBackground)
            }

            // Popup para agregar evento (estética con borde degradado y fondo suave)
            if (showAddDialog) {
                androidx.compose.ui.window.Dialog(onDismissRequest = { if (!guardando) showAddDialog = false }) {
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

                        // Equipo
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
                                if (!equipoAUid.isNullOrBlank()) {
                                    DropdownMenuItem(
                                        text = { Text(nombreA) },
                                        onClick = {
                                            equipoSeleccionadoUid = equipoAUid
                                            equipoSeleccionadoNombre = nombreA
                                            expandedEquipo = false
                                            jugadorSeleccionado = null
                                            asistenteSeleccionado = null
                                        }
                                    )
                                }
                                if (!equipoBUid.isNullOrBlank()) {
                                    DropdownMenuItem(
                                        text = { Text(nombreB) },
                                        onClick = {
                                            equipoSeleccionadoUid = equipoBUid
                                            equipoSeleccionadoNombre = nombreB
                                            expandedEquipo = false
                                            jugadorSeleccionado = null
                                            asistenteSeleccionado = null
                                        }
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(10.dp))

                        // Jugador (OBLIGATORIO)
                        val opcionesJugador = if (equipoSeleccionadoUid == equipoAUid) jugadoresEquipoA else jugadoresEquipoB
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

                        // Asistente (opcional)
                        val opcionesAsistente = (if (equipoSeleccionadoUid == equipoAUid) jugadoresEquipoA else jugadoresEquipoB)
                            .filter { it.nombre != jugadorSeleccionado?.nombre }
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

                        // Minuto (OBLIGATORIO)
                        OutlinedTextField(
                            value = minutoTexto,
                            onValueChange = { minutoTexto = it.filter { c -> c.isDigit() }.take(3) },
                            label = { Text("Minuto (obligatorio)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(16.dp))

                        // Botonera acorde al estilo (botón contorno y botón principal con borde degradado)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Cancelar (outline)
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
                                    .background(color = androidx.compose.ui.graphics.Color.Transparent)
                                    .let { base ->
                                        if (!guardando) base
                                        else base
                                    }
                                    .padding(horizontal = 0.dp)
                                    .then(Modifier),
                                contentAlignment = Alignment.Center
                            ) {
                                TextButton(
                                    enabled = !guardando,
                                    onClick = { showAddDialog = false }
                                ) { Text("Cancelar", fontWeight = FontWeight.Bold) }
                            }

                            Spacer(Modifier.width(12.dp))

                            // Guardar (fondo suave + borde degradado)
                            val puedeGuardar = !guardando &&
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
                                        scope.launch {
                                            guardando = true
                                            try {
                                                val db = FirebaseFirestore.getInstance()
                                                val doc = db.collection("goleadores").document()
                                                val minuto = minutoTexto.toIntOrNull()

                                                val data = hashMapOf<String, Any?>(
                                                    "uid" to doc.id,
                                                    "partidoUid" to partidoUid,
                                                    "equipoUid" to (equipoSeleccionadoUid ?: ""),
                                                    "minuto" to minuto,
                                                )

                                                if (jugadorSeleccionado?.uid.isNullOrBlank()) {
                                                    data["jugadorUid"] = ""
                                                    data["jugadorManual"] = jugadorSeleccionado?.nombre ?: ""
                                                } else {
                                                    data["jugadorUid"] = jugadorSeleccionado?.uid
                                                    data["jugadorManual"] = ""
                                                }

                                                if (asistenteSeleccionado == null) {
                                                    data["asistenciaJugadorUid"] = ""
                                                    data["asistenciaManual"] = ""
                                                } else if (asistenteSeleccionado?.uid.isNullOrBlank()) {
                                                    data["asistenciaJugadorUid"] = ""
                                                    data["asistenciaManual"] = asistenteSeleccionado?.nombre ?: ""
                                                } else {
                                                    data["asistenciaJugadorUid"] = asistenteSeleccionado?.uid
                                                    data["asistenciaManual"] = ""
                                                }

                                                doc.set(data).await()
                                                showAddDialog = false
                                                guardando = false
                                                recargar()
                                            } catch (_: Exception) {
                                                guardando = false
                                            }
                                        }
                                    }
                                ) { Text(if (guardando) "Guardando..." else "Guardar", fontWeight = FontWeight.Bold) }
                            }
                        }
                    }
                }
            }
        }

    }
}

data class GolEvento(
    val equipoUid: String,
    val jugador: String,
    val minuto: Int?,
    val asistente: String?
)
