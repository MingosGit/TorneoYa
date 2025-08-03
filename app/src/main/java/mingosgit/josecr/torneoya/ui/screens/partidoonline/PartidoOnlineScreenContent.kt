package mingosgit.josecr.torneoya.ui.screens.partidoonline

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import mingosgit.josecr.torneoya.viewmodel.partidoonline.PartidoOnlineViewModel
import mingosgit.josecr.torneoya.viewmodel.partidoonline.PartidoConNombresOnline
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import mingosgit.josecr.torneoya.ui.theme.TorneoYaPalette
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.draw.shadow

enum class EstadoPartido(val display: String) {
    TODOS("Todos"),
    PREVIA("Previa"),
    JUGANDO("Jugando"),
    FINALIZADO("Finalizado")
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PartidoOnlineScreenContent(
    navController: NavController,
    partidoViewModel: PartidoOnlineViewModel
) {
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) { partidoViewModel.cargarPartidosConNombres() }
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val partidos by partidoViewModel.partidosConNombres.collectAsState()

    var sortOption by remember { mutableStateOf("Nombre") }
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

    val partidosFiltrados = remember(partidos, estadoSeleccionado) {
        if (estadoSeleccionado == EstadoPartido.TODOS) partidos
        else partidos.filter { obtenerEstadoPartido(it) == estadoSeleccionado }
    }
    val sortedPartidos = remember(partidosFiltrados, sortOption, ascending) {
        when (sortOption) {
            "Nombre" -> if (ascending) partidosFiltrados.sortedBy { it.nombreEquipoA } else partidosFiltrados.sortedByDescending { it.nombreEquipoA }
            "Fecha" -> {
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
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp, horizontal = 24.dp)
            ) {
                ListItem(
                    headlineContent = { Text("Duplicar", fontWeight = FontWeight.Medium) },
                    leadingContent = {
                        Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
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
            title = { Text("¿Eliminar partido?", fontWeight = FontWeight.Bold) },
            text = { Text("¿Estás seguro que deseas eliminar el partido \"$nombreA vs $nombreB\"?") },
            confirmButton = {
                Button(colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    onClick = {
                        showConfirmDialog = false
                        uid?.let { scope.launch { partidoViewModel.eliminarPartido(it) } }
                    }) { Text("Eliminar", color = Color.White) }
            },
            dismissButton = {
                OutlinedButton(onClick = { showConfirmDialog = false }) { Text("Cancelar") }
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
                searchError = "Ya tienes acceso a este partido"
            }
        } else if (partidoAdd != null) {
            AlertDialog(
                onDismissRequest = { showAddConfirmDialog = null },
                title = { Text("¿Agregar partido?", fontWeight = FontWeight.Bold) },
                text = { Text("¿Seguro que deseas agregar el partido \"$nombreA vs $nombreB\" a tu lista?") },
                confirmButton = {
                    Button(onClick = {
                        scope.launch { partidoViewModel.agregarPartidoALista(partidoAdd) }
                        showAddConfirmDialog = null
                        searchText = ""
                        searchResults = emptyList()
                        showSearchDropdown = false
                    }) { Text("Agregar") }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showAddConfirmDialog = null }) { Text("Cancelar") }
                }
            )
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate("crear_partido_online") },
                icon = {
                    Icon(Icons.Default.AddCircle, contentDescription = "Crear partido", tint = TorneoYaPalette.accent, modifier = Modifier.size(28.dp))
                },
                text = {
                    Text("Crear partido", fontWeight = FontWeight.Bold, color = TorneoYaPalette.accent, fontSize = 18.sp)
                },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = TorneoYaPalette.accent,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .padding(bottom = 8.dp, end = 4.dp)
                    .shadow(7.dp, RoundedCornerShape(18.dp))
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = "Partidos Online",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
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
                    shape = RoundedCornerShape(7.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.6.dp, brush = Brush.horizontalGradient(listOf(TorneoYaPalette.accent, TorneoYaPalette.blue))),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = TorneoYaPalette.accent
                    ),
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .height(44.dp)
                        .defaultMinSize(minWidth = 140.dp)
                ) {
                    Icon(Icons.Default.Search, contentDescription = "Buscar UID", modifier = Modifier.size(22.dp), tint = TorneoYaPalette.accent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Buscar por UID", color = TorneoYaPalette.accent, fontWeight = FontWeight.Bold)
                }

                DropdownMenu(
                    expanded = showSearchDropdown,
                    onDismissRequest = { showSearchDropdown = false },
                    modifier = Modifier
                        .fillMaxWidth(0.98f)
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = searchText,
                                onValueChange = {
                                    searchText = it
                                    searchError = null
                                },
                                label = { Text("UID del partido") },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 4.dp)
                            )
                            IconButton(
                                onClick = {
                                    val clipboardText = clipboardManager.getText()?.text
                                    if (!clipboardText.isNullOrBlank()) {
                                        searchText = clipboardText
                                        searchError = null
                                    }
                                }
                            ) {
                                Icon(Icons.Default.ContentPaste, contentDescription = "Pegar UID")
                            }
                            IconButton(
                                onClick = {
                                    if (searchText.isNotBlank()) {
                                        searchLoading = true
                                        searchError = null
                                        scope.launch {
                                            try {
                                                val partido = partidoViewModel.buscarPartidoPorUid(searchText)
                                                searchResults = if (partido != null) listOf(partido) else emptyList()
                                                searchError = if (partido == null) "No existe ese UID" else null
                                            } catch (e: Exception) {
                                                searchResults = emptyList()
                                                searchError = "Error en búsqueda"
                                            }
                                            searchLoading = false
                                        }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Search, contentDescription = "Buscar UID")
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        if (searchLoading) {
                            CircularProgressIndicator(
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
                                        Text("${p.nombreEquipoA} vs ${p.nombreEquipoB}", fontWeight = FontWeight.SemiBold)
                                    },
                                    supportingContent = {
                                        Text("UID: ${p.uid}", fontSize = 13.sp)
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

            // --------- FILTROS MEJORADOS ----------
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
                                when (estadoSeleccionado) {
                                    EstadoPartido.TODOS -> "Todos"
                                    else -> estadoSeleccionado.display
                                },
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
                        containerColor = MaterialTheme.colorScheme.surface,
                        selectedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = MaterialTheme.colorScheme.onSurface
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
                            text = { Text(estado.display) },
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
                                "${sortOption} ${if (ascending) "↑" else "↓"}",
                                fontWeight = FontWeight.Bold,
                                color = TorneoYaPalette.blue
                            )
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        selectedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = MaterialTheme.colorScheme.onSurface
                    ),
                    border = BorderStroke(1.2.dp, TorneoYaPalette.blue),
                    modifier = Modifier.height(38.dp)
                )
                DropdownMenu(
                    expanded = expandedSort,
                    onDismissRequest = { expandedSort = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Nombre") },
                        onClick = {
                            sortOption = "Nombre"
                            expandedSort = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Fecha") },
                        onClick = {
                            sortOption = "Fecha"
                            expandedSort = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(if (ascending) "Descendente" else "Ascendente") },
                        onClick = {
                            ascending = !ascending
                            expandedSort = false
                        }
                    )
                }
            }


            LazyColumn {
                items(sortedPartidos) { partido ->
                    val nombreA = partido.nombreEquipoA ?: "Equipo A"
                    val nombreB = partido.nombreEquipoB ?: "Equipo B"
                    val fecha = partido.fecha ?: "-"
                    val horaInicio = partido.horaInicio ?: "-"
                    val horaFin = partido.horaFin ?: "-"

                    Card(
                        shape = RoundedCornerShape(13.dp),
                        elevation = CardDefaults.cardElevation(2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
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
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                            Color.Transparent
                                        )
                                    )
                                )
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
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Fecha: $fecha  -  Inicio: $horaInicio  -  Fin: $horaFin",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                val estado = obtenerEstadoPartido(partido)

                                // CHIP MODERNO: color fondo/texto/borde según estado
                                val (chipBg, chipText, chipBorder) = when (estado) {
                                    EstadoPartido.PREVIA -> Triple(Color(0xFF222F1C), Color(0xFF97E993), Color(0xFF97E993))
                                    EstadoPartido.JUGANDO -> Triple(Color(0xFF352D15), Color(0xFFFFB531), Color(0xFFFFB531))
                                    EstadoPartido.FINALIZADO -> Triple(Color(0xFF2F2322), Color(0xFFF97373), Color(0xFFF97373))
                                    else -> Triple(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, Color.Transparent)
                                }
                                AssistChip(
                                    onClick = { },
                                    label = {
                                        Text(
                                            when (estado) {
                                                EstadoPartido.PREVIA -> "Previa"
                                                EstadoPartido.JUGANDO -> "En juego"
                                                EstadoPartido.FINALIZADO -> "Finalizado"
                                                EstadoPartido.TODOS -> ""
                                            },
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
                                contentDescription = "Ver",
                                tint = MaterialTheme.colorScheme.primary,
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



