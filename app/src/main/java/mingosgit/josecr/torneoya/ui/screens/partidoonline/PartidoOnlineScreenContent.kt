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
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import mingosgit.josecr.torneoya.R
import mingosgit.josecr.torneoya.viewmodel.partidoonline.PartidoOnlineViewModel
import mingosgit.josecr.torneoya.viewmodel.partidoonline.PartidoConNombresOnline
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import mingosgit.josecr.torneoya.ui.theme.TorneoYaPalette

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
        0.0f to Color(0xFF1B1D29),
        0.28f to Color(0xFF212442),
        0.58f to Color(0xFF191A23),
        1.0f to Color(0xFF14151B)
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

    if (showOptionsSheet && partidoSeleccionado != null) {
        ModalBottomSheet(
            onDismissRequest = { showOptionsSheet = false },
            dragHandle = null,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            containerColor = Color(0xFF23273D)
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp, horizontal = 24.dp)
            ) {
                ListItem(
                    headlineContent = { Text(stringResource(id = R.string.ponline_duplicar), fontWeight = FontWeight.Medium, color = Color(0xFF8F5CFF)) },
                    leadingContent = {
                        Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = Color(0xFF8F5CFF))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showOptionsSheet = false
                            partidoSeleccionado?.uid?.let { uid -> scope.launch { partidoViewModel.duplicarPartido(uid) } }
                        }
                )
            }
        }
    }

    if (showConfirmDialog && partidoSeleccionado != null) {
        val nombreA = partidoSeleccionado?.nombreEquipoA ?: "Equipo A"
        val nombreB = partidoSeleccionado?.nombreEquipoB ?: "Equipo B"
        val uid = partidoSeleccionado?.uid
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text(stringResource(id = R.string.ponline_eliminar_partido_titulo), fontWeight = FontWeight.Bold) },
            text = { Text(String.format(stringResource(id = R.string.ponline_eliminar_partido_confirmacion), nombreA, nombreB)) },
            confirmButton = {
                Button(colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    onClick = {
                        showConfirmDialog = false
                        uid?.let { scope.launch { partidoViewModel.eliminarPartido(it) } }
                    }) { Text(stringResource(id = R.string.gen_eliminar), color = Color.White) }
            },
            dismissButton = {
                OutlinedButton(onClick = { showConfirmDialog = false }) { Text(stringResource(id = R.string.gen_cancelar)) }
            }
        )
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
                searchError = R.string.ponline_ya_tienes_acceso.toString()
            }
        } else if (partidoAdd != null) {
            AlertDialog(
                onDismissRequest = { showAddConfirmDialog = null },
                title = { Text(stringResource(id = R.string.ponline_agregar_partido_titulo), fontWeight = FontWeight.Bold) },
                text = { Text(String.format(stringResource(id = R.string.ponline_agregar_partido_confirmacion), nombreA, nombreB)) },
                confirmButton = {
                    Button(onClick = {
                        scope.launch { partidoViewModel.agregarPartidoALista(partidoAdd) }
                        showAddConfirmDialog = null
                        searchText = ""
                        searchResults = emptyList()
                        showSearchDropdown = false
                    }) { Text(stringResource(id = R.string.ponline_agregar)) }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showAddConfirmDialog = null }) { Text(stringResource(id = R.string.gen_cancelar)) }
                }
            )
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            Box(
                modifier = Modifier
                    .padding(bottom = 1.dp, end = 1.dp)
                    .clip(RoundedCornerShape(17.dp))
                    .background(Color(0xCC23273D))
                    .border(
                        width = 2.dp,
                        brush = Brush.horizontalGradient(
                            listOf(TorneoYaPalette.blue, TorneoYaPalette.violet)
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
                        tint = Color(0xFF296DFF),
                        modifier = Modifier.size(29.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        stringResource(id = R.string.ponline_crear_partido),
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF296DFF),
                        fontSize = 16.sp
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(modernBackground)
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
                color = Color.White,
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
                    border = BorderStroke(2.dp, Brush.horizontalGradient(listOf(TorneoYaPalette.blue, TorneoYaPalette.violet))),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Brush.horizontalGradient(listOf(Color(0xFF23273D), Color(0xFF1C1D25))).toBrushColor(),
                        contentColor = Color(0xFF296DFF)
                    ),
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .height(44.dp)
                        .defaultMinSize(minWidth = 140.dp)
                ) {
                    Icon(Icons.Default.Search, contentDescription = stringResource(id = R.string.gen_buscar), modifier = Modifier.size(22.dp), tint = Color(0xFF296DFF))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(id = R.string.ponline_buscar_por_uid), color = Color(0xFF296DFF), fontWeight = FontWeight.Bold)
                }

                DropdownMenu(
                    expanded = showSearchDropdown,
                    onDismissRequest = { showSearchDropdown = false },
                    modifier = Modifier
                        .fillMaxWidth(0.98f)
                        .background(Color(0xFF23273D))
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
                                placeholder = { Text(stringResource(id = R.string.ponline_uid_del_partido), color = Color(0xFFB7B7D1), fontSize = 16.sp) },
                                singleLine = true,
                                colors = TextFieldDefaults.textFieldColors(
                                    containerColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    cursorColor = TorneoYaPalette.blue
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(13.dp))
                                    .border(
                                        width = 2.dp,
                                        brush = Brush.horizontalGradient(listOf(TorneoYaPalette.blue, TorneoYaPalette.violet)),
                                        shape = RoundedCornerShape(13.dp)
                                    )
                                    .background(
                                        Brush.horizontalGradient(listOf(Color(0xFF23273D), Color(0xFF1C1D25)))
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
                                    .background(Color(0xFF23273D))
                                    .border(
                                        width = 1.6.dp,
                                        brush = Brush.horizontalGradient(listOf(TorneoYaPalette.blue, TorneoYaPalette.violet)),
                                        shape = RoundedCornerShape(11.dp)
                                    )
                            ) {
                                Icon(Icons.Default.ContentPaste, contentDescription = stringResource(id = R.string.gen_pegar_uid), tint = Color(0xFF8F5CFF), modifier = Modifier.size(24.dp))
                            }
                            Spacer(Modifier.width(7.dp))
                            IconButton(
                                onClick = {
                                    if (searchText.isNotBlank()) {
                                        searchLoading = true
                                        scope.launch {
                                            val partido = runCatching { partidoViewModel.buscarPartidoPorUid(searchText) }.getOrNull()
                                            searchResults = if (partido != null) listOf(partido) else emptyList()
                                            searchError = if (partido == null) (R.string.ponline_no_existe_uid.toString()) else null
                                            searchLoading = false
                                        }

                                        }

                                },
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(11.dp))
                                    .background(Color(0xFF23273D))
                                    .border(
                                        width = 1.6.dp,
                                        brush = Brush.horizontalGradient(listOf(TorneoYaPalette.blue, TorneoYaPalette.violet)),
                                        shape = RoundedCornerShape(11.dp)
                                    )
                            ) {
                                Icon(Icons.Default.Search, contentDescription = stringResource(id = R.string.gen_buscar), tint = Color(0xFF8F5CFF), modifier = Modifier.size(24.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        if (searchLoading) {
                            CircularProgressIndicator(
                                color = TorneoYaPalette.blue,
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .size(22.dp)
                            )
                        }
                        if (searchError != null) {
                            Text(
                                searchError ?: "",
                                color = Color.Red,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        if (searchResults.isNotEmpty()) {
                            searchResults.forEach { p ->
                                ListItem(
                                    headlineContent = {
                                        Text("${p.nombreEquipoA} vs ${p.nombreEquipoB}", fontWeight = FontWeight.SemiBold, color = Color.White)
                                    },
                                    supportingContent = {
                                        Text("UID: ${p.uid}", fontSize = 13.sp, color = Color(0xFFB7B7D1))
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
                                    EstadoPartido.TODOS -> TorneoYaPalette.mutedText
                                    EstadoPartido.PREVIA -> Color(0xFF97E993)
                                    EstadoPartido.JUGANDO -> Color(0xFFFFB531)
                                    EstadoPartido.FINALIZADO -> Color(0xFFF97373)
                                }
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                stringResource(id = estadoSeleccionado.displayRes),
                                fontWeight = FontWeight.Bold,
                                color = when (estadoSeleccionado) {
                                    EstadoPartido.TODOS -> TorneoYaPalette.mutedText
                                    EstadoPartido.PREVIA -> Color(0xFF97E993)
                                    EstadoPartido.JUGANDO -> Color(0xFFFFB531)
                                    EstadoPartido.FINALIZADO -> Color(0xFFF97373)
                                }
                            )
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = Color(0xFF22243A),
                        selectedContainerColor = Color(0xFF23273D),
                        labelColor = Color.White
                    ),
                    border = BorderStroke(1.2.dp, when (estadoSeleccionado) {
                        EstadoPartido.TODOS -> TorneoYaPalette.mutedText
                        EstadoPartido.PREVIA -> Color(0xFF97E993)
                        EstadoPartido.JUGANDO -> Color(0xFFFFB531)
                        EstadoPartido.FINALIZADO -> Color(0xFFF97373)
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
                                tint = TorneoYaPalette.blue
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "${stringResource(id = sortOption)} ${if (ascending) stringResource(id = R.string.ponline_ascendente) else stringResource(id = R.string.ponline_descendente)}",
                                fontWeight = FontWeight.Bold,
                                color = TorneoYaPalette.blue
                            )
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = Color(0xFF22243A),
                        selectedContainerColor = Color(0xFF23273D),
                        labelColor = Color.White
                    ),
                    border = BorderStroke(1.2.dp, TorneoYaPalette.blue),
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
                            color = Color(0xFF296DFF),
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(46.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = stringResource(id = R.string.ponline_cargando_partidos),
                            color = Color(0xFFB7B7D1),
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
                                brush = Brush.horizontalGradient(listOf(TorneoYaPalette.blue, TorneoYaPalette.violet)),
                                shape = RoundedCornerShape(17.dp)
                            )
                            .background(
                                Brush.horizontalGradient(listOf(Color(0xFF23273D), Color(0xFF1C1D25)))
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
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "${stringResource(id = R.string.ponline_fecha)}: $fecha  -  Inicio: $horaInicio  -  Fin: $horaFin",
                                    fontSize = 13.sp,
                                    color = Color(0xFFB7B7D1)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                val estado = obtenerEstadoPartido(partido)
                                val (chipBg, chipText, chipBorder, chipLabel) = when (estado) {
                                    EstadoPartido.PREVIA -> Quadruple(Color(0xFF222F1C), Color(0xFF97E993), Color(0xFF97E993), stringResource(id = R.string.ponline_previa))
                                    EstadoPartido.JUGANDO -> Quadruple(Color(0xFF352D15), Color(0xFFFFB531), Color(0xFFFFB531), stringResource(id = R.string.ponline_en_juego))
                                    EstadoPartido.FINALIZADO -> Quadruple(Color(0xFF2F2322), Color(0xFFF97373), Color(0xFFF97373), stringResource(id = R.string.ponline_finalizado))
                                    else -> Quadruple(Color(0xFF23273D), Color(0xFFB7B7D1), Color.Transparent, "")
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
                                tint = Color(0xFF8F5CFF),
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
