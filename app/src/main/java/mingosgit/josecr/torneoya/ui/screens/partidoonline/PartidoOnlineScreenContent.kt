package mingosgit.josecr.torneoya.ui.screens.partidoonline

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import mingosgit.josecr.torneoya.R
import mingosgit.josecr.torneoya.ui.theme.TorneoYaPalette
import mingosgit.josecr.torneoya.viewmodel.partidoonline.PartidoOnlineViewModel
import mingosgit.josecr.torneoya.viewmodel.partidoonline.PartidoConNombresOnline
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import mingosgit.josecr.torneoya.ui.theme.mutedText

enum class EstadoPartido(val displayRes: Int) {
    TODOS(R.string.ponline_todos),
    PREVIA(R.string.ponline_previa),
    JUGANDO(R.string.ponline_en_juego),
    FINALIZADO(R.string.ponline_finalizado)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PartidoOnlineScreenContent(
    navController: NavController,
    partidoViewModel: PartidoOnlineViewModel
) {
    val cs = MaterialTheme.colorScheme

    val cargandoPartidos by partidoViewModel.cargandoPartidos.collectAsState()
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) { partidoViewModel.cargarPartidosConNombres() }
    val clipboardManager = LocalClipboardManager.current

    val partidos by partidoViewModel.partidosConNombres.collectAsState()

    var sortOption by remember { mutableIntStateOf(R.string.ponline_nombre) }
    var ascending by remember { mutableStateOf(true) }
    var expandedSort by remember { mutableStateOf(false) }
    var estadoSeleccionado by remember { mutableStateOf(EstadoPartido.TODOS) }
    var expandedEstado by remember { mutableStateOf(false) }
    var showOptionsSheet by remember { mutableStateOf(false) }
    var partidoSeleccionado by remember { mutableStateOf<PartidoConNombresOnline?>(null) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showSearchDropdown by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf(listOf<PartidoConNombresOnline>()) }
    var searchLoading by remember { mutableStateOf(false) }
    var searchError by remember { mutableStateOf<String?>(null) }
    var showAddConfirmDialog by remember { mutableStateOf<PartidoConNombresOnline?>(null) }

    // Obtener strings fuera de lógica para evitar error de @Composable
    val errorNoExisteUid = stringResource(id = R.string.ponline_no_existe_uid)
    val errorYaTienesAcceso = stringResource(id = R.string.ponline_ya_tienes_acceso)

    fun parseFecha(fecha: String): LocalDate? {
        val patronesFecha = listOf("yyyy-MM-dd", "dd/MM/yyyy", "yyyy/MM/dd", "dd-MM-yyyy")
        for (pf in patronesFecha) {
            try {
                val formatter = DateTimeFormatter.ofPattern(pf)
                return LocalDate.parse(fecha, formatter)
            } catch (_: Exception) {}
        }
        return null
    }
    fun parseHora(hora: String): LocalTime? {
        val patronesHora = listOf("HH:mm", "H:mm")
        for (ph in patronesHora) {
            try {
                val formatter = DateTimeFormatter.ofPattern(ph)
                return LocalTime.parse(hora, formatter)
            } catch (_: Exception) {}
        }
        return null
    }
    fun obtenerEstadoPartido(partido: PartidoConNombresOnline): EstadoPartido {
        val hoy = LocalDate.now()
        val ahora = LocalTime.now()
        val fecha = parseFecha(partido.fecha ?: "")
        val horaInicio = parseHora(partido.horaInicio ?: "")
        val horaFin = parseHora(partido.horaFin ?: "")

        if (fecha == null || horaInicio == null || horaFin == null) return EstadoPartido.PREVIA

        return when {
            fecha.isBefore(hoy) -> EstadoPartido.FINALIZADO
            fecha.isAfter(hoy) -> EstadoPartido.PREVIA
            else -> when {
                ahora.isBefore(horaInicio) -> EstadoPartido.PREVIA
                (ahora == horaInicio || (ahora.isAfter(horaInicio) && ahora.isBefore(horaFin))) -> EstadoPartido.JUGANDO
                else -> EstadoPartido.FINALIZADO
            }
        }
    }

    val modernBackground = Brush.verticalGradient(
        0.0f to cs.background,
        0.28f to cs.surface,
        0.58f to cs.surfaceVariant,
        1.0f to cs.background
    )

    val partidosFiltrados = remember(partidos, estadoSeleccionado) {
        if (estadoSeleccionado == EstadoPartido.TODOS) partidos
        else partidos.filter { obtenerEstadoPartido(it) == estadoSeleccionado }
    }
    val sortedPartidos = remember(partidosFiltrados, sortOption, ascending) {
        when (sortOption) {
            R.string.ponline_nombre -> if (ascending) partidosFiltrados.sortedBy { it.nombreEquipoA } else partidosFiltrados.sortedByDescending { it.nombreEquipoA }
            R.string.ponline_fecha  -> {
                val patronesFecha = listOf("yyyy-MM-dd", "dd/MM/yyyy", "yyyy/MM/dd", "dd-MM-yyyy")
                partidosFiltrados.sortedBy { p ->
                    var fecha: LocalDate? = null
                    for (pf in patronesFecha) {
                        try {
                            val formatter = DateTimeFormatter.ofPattern(pf)
                            fecha = LocalDate.parse(p.fecha, formatter)
                            break
                        } catch (_: Exception) {}
                    }
                    fecha ?: LocalDate.MIN
                }.let { if (ascending) it else it.reversed() }
            }
            else -> partidosFiltrados
        }
    }


    if (showConfirmDialog && partidoSeleccionado != null) {
        val partidoSel = partidoSeleccionado
        if (partidoSel != null) {
            val nombreA = partidoSel.nombreEquipoA ?: "Equipo A"
            val nombreB = partidoSel.nombreEquipoB ?: "Equipo B"
            val uid = partidoSel.uid
            AlertDialog(
                onDismissRequest = { showConfirmDialog = false },
                title = { Text(stringResource(id = R.string.ponline_eliminar_partido_titulo), fontWeight = FontWeight.Bold, color = cs.onSurface) },
                text = { Text(String.format(stringResource(id = R.string.ponline_eliminar_partido_confirmacion), nombreA, nombreB), color = cs.onSurfaceVariant) },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = cs.error, contentColor = cs.onPrimary),
                        onClick = {
                            showConfirmDialog = false
                            scope.launch { partidoViewModel.eliminarPartido(uid) }
                        }
                    ) { Text(stringResource(id = R.string.gen_eliminar)) }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showConfirmDialog = false }) { Text(stringResource(id = R.string.gen_cancelar)) }
                },
                containerColor = cs.surfaceVariant
            )
        }
    }

    if (showAddConfirmDialog != null) {
        val partidoAdd = showAddConfirmDialog
        val nombreA = partidoAdd?.nombreEquipoA ?: "Equipo A"
        val nombreB = partidoAdd?.nombreEquipoB ?: "Equipo B"
        val uid = partidoAdd?.uid
        val yaExiste = sortedPartidos.any { it.uid == uid }

        if (yaExiste) {
            LaunchedEffect(showAddConfirmDialog) {
                showAddConfirmDialog = null
                searchError = errorYaTienesAcceso
            }
        } else if (partidoAdd != null) {
            Dialog(onDismissRequest = { showAddConfirmDialog = null }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp)
                        .border(
                            width = 2.dp,
                            brush = Brush.horizontalGradient(listOf(cs.primary, cs.secondary)),
                            shape = RoundedCornerShape(18.dp)
                        )
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.surface,
                                    MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        )
                ) {
                    Column(
                        Modifier.padding(horizontal = 22.dp, vertical = 26.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(id = R.string.ponline_agregar_partido_titulo),
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Spacer(Modifier.height(11.dp))
                        Text(
                            text = String.format(
                                stringResource(id = R.string.ponline_agregar_partido_confirmacion),
                                nombreA, nombreB
                            ),
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f),
                            fontSize = 15.sp
                        )
                        uid?.let {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "UID: $it",
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                fontSize = 13.sp
                            )
                        }
                        Spacer(Modifier.height(22.dp))
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            OutlinedButton(
                                onClick = {
                                    scope.launch { partidoViewModel.agregarPartidoALista(partidoAdd) }
                                    showAddConfirmDialog = null
                                    searchText = ""
                                    searchResults = emptyList()
                                    showSearchDropdown = false
                                },
                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                    width = 2.dp,
                                    brush = Brush.horizontalGradient(listOf(cs.primary, cs.secondary))
                                ),
                                shape = RoundedCornerShape(11.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = stringResource(id = R.string.ponline_agregar),
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(Modifier.width(14.dp))
                            OutlinedButton(
                                onClick = { showAddConfirmDialog = null },
                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                    width = 2.dp,
                                    brush = Brush.horizontalGradient(listOf(cs.primary, cs.secondary))
                                ),
                                shape = RoundedCornerShape(11.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = stringResource(id = R.string.gen_cancelar),
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            Box(
                modifier = Modifier
                    .padding(bottom = 1.dp, end = 1.dp)
                    .clip(RoundedCornerShape(17.dp))
                    .background(cs.surfaceVariant.copy(alpha = 0.8f))
                    .border(
                        width = 2.dp,
                        brush = Brush.horizontalGradient(
                            listOf(cs.primary, cs.secondary)
                        ),
                        shape = RoundedCornerShape(17.dp)
                    )
                    .clickable { navController.navigate("crear_partido_online") }
                    .height(56.dp)
                    .widthIn(min = 56.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SportsSoccer,
                        contentDescription = stringResource(id = R.string.ponline_crear_partido),
                        tint = cs.primary,
                        modifier = Modifier.size(29.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        stringResource(id = R.string.ponline_crear_partido),
                        fontWeight = FontWeight.Bold,
                        color = cs.primary,
                        fontSize = 16.sp
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(TorneoYaPalette.backgroundGradient)
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 16.dp,
                    top = 26.dp
                )
        ) {
            Text(
                text = stringResource(id = R.string.gen_partidos_online),
                fontSize = 27.sp,
                fontWeight = FontWeight.Black,
                color = cs.onBackground,
                modifier = Modifier.padding(bottom = 18.dp)
            )

            // ---- BUSCADOR POR UID ----
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp)
            ) {
                OutlinedButton(
                    onClick = { showSearchDropdown = !showSearchDropdown },
                    shape = RoundedCornerShape(17.dp),
                    border = BorderStroke(2.dp, Brush.horizontalGradient(listOf(cs.primary, cs.secondary))),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = cs.primary
                    ),
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .height(44.dp)
                        .defaultMinSize(minWidth = 140.dp)
                ) {
                    Icon(Icons.Default.Search, contentDescription = stringResource(id = R.string.gen_buscar), modifier = Modifier.size(22.dp), tint = cs.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(id = R.string.ponline_buscar_por_uid), color = cs.primary, fontWeight = FontWeight.Bold)
                }

                DropdownMenu(
                    expanded = showSearchDropdown,
                    onDismissRequest = { showSearchDropdown = false },
                    modifier = Modifier
                        .fillMaxWidth(0.98f)
                        .background(cs.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextField(
                                value = searchText,
                                onValueChange = {
                                    searchText = it
                                    searchError = null
                                },
                                placeholder = { Text(stringResource(id = R.string.ponline_uid_del_partido), color = cs.mutedText, fontSize = 16.sp) },
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    cursorColor = cs.primary,
                                    focusedTextColor = cs.onSurface,
                                    unfocusedTextColor = cs.onSurface
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(13.dp))
                                    .border(
                                        width = 2.dp,
                                        brush = Brush.horizontalGradient(listOf(cs.primary, cs.secondary)),
                                        shape = RoundedCornerShape(13.dp)
                                    )
                                    .background(
                                        Brush.horizontalGradient(listOf(cs.surfaceVariant, cs.surface))
                                    )
                                    .padding(horizontal = 10.dp)
                            )
                            Spacer(Modifier.width(11.dp))
                            IconButton(
                                onClick = {
                                    val clipboardText = clipboardManager.getText()?.text
                                    if (!clipboardText.isNullOrBlank()) {
                                        searchText = clipboardText
                                        searchError = null
                                    }
                                },
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(11.dp))
                                    .background(cs.surfaceVariant)
                                    .border(
                                        width = 1.6.dp,
                                        brush = Brush.horizontalGradient(listOf(cs.primary, cs.secondary)),
                                        shape = RoundedCornerShape(11.dp)
                                    )
                            ) {
                                Icon(Icons.Default.ContentPaste, contentDescription = stringResource(id = R.string.gen_pegar_uid), tint = cs.secondary, modifier = Modifier.size(24.dp))
                            }
                            Spacer(Modifier.width(7.dp))
                            IconButton(
                                onClick = {
                                    if (searchText.isNotBlank()) {
                                        searchLoading = true
                                        scope.launch {
                                            val partido = runCatching { partidoViewModel.buscarPartidoPorUid(searchText) }.getOrNull()
                                            searchResults = if (partido != null) listOf(partido) else emptyList()
                                            searchError = if (partido == null) errorNoExisteUid else null
                                            searchLoading = false
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(11.dp))
                                    .background(cs.surfaceVariant)
                                    .border(
                                        width = 1.6.dp,
                                        brush = Brush.horizontalGradient(listOf(cs.primary, cs.secondary)),
                                        shape = RoundedCornerShape(11.dp)
                                    )
                            ) {
                                Icon(Icons.Default.Search, contentDescription = stringResource(id = R.string.gen_buscar), tint = cs.secondary, modifier = Modifier.size(24.dp))
                            }

                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        if (searchLoading) {
                            CircularProgressIndicator(
                                color = cs.primary,
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .size(22.dp),
                                strokeWidth = 3.dp
                            )
                        }
                        if (searchError != null) {
                            Text(
                                searchError ?: "",
                                color = cs.error,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        if (searchResults.isNotEmpty()) {
                            searchResults.forEach { p ->
                                ListItem(
                                    headlineContent = {
                                        Text("${p.nombreEquipoA} vs ${p.nombreEquipoB}", fontWeight = FontWeight.SemiBold, color = cs.onSurface)
                                    },
                                    supportingContent = {
                                        Text("UID: ${p.uid}", fontSize = 13.sp, color = cs.onSurfaceVariant)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showAddConfirmDialog = p }
                                )
                            }
                        }
                    }
                }
            }

            // --------- FILTROS --------
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                // Estado
                FilterChip(
                    selected = true,
                    onClick = { expandedEstado = true },
                    enabled = true,
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.FilterList,
                                contentDescription = null,
                                modifier = Modifier.size(19.dp),
                                tint = when (estadoSeleccionado) {
                                    EstadoPartido.TODOS -> cs.mutedText
                                    EstadoPartido.PREVIA -> cs.primary
                                    EstadoPartido.JUGANDO -> cs.tertiary
                                    EstadoPartido.FINALIZADO -> cs.error
                                }
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                stringResource(id = estadoSeleccionado.displayRes),
                                fontWeight = FontWeight.Bold,
                                color = when (estadoSeleccionado) {
                                    EstadoPartido.TODOS -> cs.mutedText
                                    EstadoPartido.PREVIA -> cs.primary
                                    EstadoPartido.JUGANDO -> cs.tertiary
                                    EstadoPartido.FINALIZADO -> cs.error
                                }
                            )
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = cs.surfaceVariant,
                        selectedContainerColor = cs.surfaceVariant,
                        labelColor = cs.onSurface
                    ),
                    border = BorderStroke(1.2.dp, when (estadoSeleccionado) {
                        EstadoPartido.TODOS -> cs.mutedText
                        EstadoPartido.PREVIA -> cs.primary
                        EstadoPartido.JUGANDO -> cs.tertiary
                        EstadoPartido.FINALIZADO -> cs.error
                    }),
                    modifier = Modifier.height(38.dp)
                )
                DropdownMenu(
                    expanded = expandedEstado,
                    onDismissRequest = { expandedEstado = false }
                ) {
                    EstadoPartido.values().forEach { estado ->
                        DropdownMenuItem(
                            text = {
                                Text(stringResource(id = estado.displayRes))
                            },
                            onClick = {
                                estadoSeleccionado = estado
                                expandedEstado = false
                            }
                        )
                    }
                }

                // Ordenar
                FilterChip(
                    selected = true,
                    onClick = { expandedSort = true },
                    enabled = true,
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Sort,
                                contentDescription = null,
                                modifier = Modifier.size(19.dp),
                                tint = cs.primary
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "${stringResource(id = sortOption)} ${if (ascending) stringResource(id = R.string.ponline_ascendente) else stringResource(id = R.string.ponline_descendente)}",
                                fontWeight = FontWeight.Bold,
                                color = cs.primary
                            )
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = cs.surfaceVariant,
                        selectedContainerColor = cs.surfaceVariant,
                        labelColor = cs.onSurface
                    ),
                    border = BorderStroke(1.2.dp, cs.primary),
                    modifier = Modifier.height(38.dp)
                )
                DropdownMenu(
                    expanded = expandedSort,
                    onDismissRequest = { expandedSort = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(id = R.string.ponline_nombre)) },
                        onClick = {
                            sortOption = R.string.ponline_nombre
                            expandedSort = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(id = R.string.ponline_fecha)) },
                        onClick = {
                            sortOption = R.string.ponline_fecha
                            expandedSort = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(if (ascending) stringResource(id = R.string.ponline_descendente) else stringResource(id = R.string.ponline_ascendente)) },
                        onClick = {
                            ascending = !ascending
                            expandedSort = false
                        }
                    )
                }
            }
            if (cargandoPartidos) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 38.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            color = cs.primary,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(46.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = stringResource(id = R.string.ponline_cargando_partidos),
                            color = cs.mutedText,
                            fontSize = 17.sp
                        )
                    }
                }
                return@Column
            }
            // ---- CARD DE PARTIDO ----
            LazyColumn {
                items(sortedPartidos) { partido ->
                    val nombreA = partido.nombreEquipoA ?: "Equipo A"
                    val nombreB = partido.nombreEquipoB ?: "Equipo B"
                    val fecha = partido.fecha ?: "-"
                    val horaInicio = partido.horaInicio ?: "-"
                    val horaFin = partido.horaFin ?: "-"

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 7.dp)
                            .clip(RoundedCornerShape(17.dp))
                            .border(
                                width = 2.dp,
                                brush = Brush.horizontalGradient(listOf(cs.primary, cs.secondary)),
                                shape = RoundedCornerShape(17.dp)
                            )
                            .background(
                                Brush.horizontalGradient(listOf(cs.surfaceVariant, cs.surface))
                            )
                            .combinedClickable(
                                onClick = {
                                    navController.navigate("visualizar_partido_online/${partido.uid}")
                                },
                                onLongClick = {
                                    partidoSeleccionado = partido
                                    showOptionsSheet = true
                                }
                            )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "$nombreA  vs  $nombreB",
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = cs.onSurface
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "${stringResource(id = R.string.ponline_fecha)}: $fecha  -  Inicio: $horaInicio  -  Fin: $horaFin",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.mutedText
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                val estado = obtenerEstadoPartido(partido)
                                val (chipBg, chipText, chipBorder, chipLabel) = when (estado) {
                                    EstadoPartido.PREVIA -> Quadruple(cs.surface, cs.primary, cs.primary, stringResource(id = R.string.ponline_previa))
                                    EstadoPartido.JUGANDO -> Quadruple(cs.surface, cs.tertiary, cs.tertiary, stringResource(id = R.string.ponline_en_juego))
                                    EstadoPartido.FINALIZADO -> Quadruple(cs.surface, cs.error, cs.error, stringResource(id = R.string.ponline_finalizado))
                                    else -> Quadruple(cs.surfaceVariant, cs.mutedText, Color.Transparent, "")
                                }
                                AssistChip(
                                    onClick = { },
                                    label = {
                                        Text(
                                            chipLabel,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp,
                                            color = chipText
                                        )
                                    },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = chipBg,
                                        labelColor = chipText
                                    ),
                                    border = if (chipBorder != Color.Transparent) BorderStroke(1.2.dp, chipBorder) else null,
                                    modifier = Modifier
                                        .padding(top = 2.dp)
                                        .height(26.dp)
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = stringResource(id = R.string.ponline_ver),
                                tint = cs.secondary,
                                modifier = Modifier
                                    .size(30.dp)
                                    .padding(start = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// Helper para devolver 4 valores (como el destructuring de chipBg, chipText, chipBorder, chipLabel)
data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

// Utilidad para usar Brush horizontal como color de fondo de botón
fun Brush.toBrushColor(): Color {
    return Color.Transparent
}
