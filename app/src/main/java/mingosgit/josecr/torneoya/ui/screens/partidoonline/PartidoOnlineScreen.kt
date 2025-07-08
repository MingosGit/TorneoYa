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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ContentPaste
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

enum class EstadoPartido(val display: String) {
    TODOS("Todos"),
    PREVIA("Previa"),
    JUGANDO("Jugando"),
    FINALIZADO("Finalizado")
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PartidoOnlineScreen(
    navController: NavController,
    partidoViewModel: PartidoOnlineViewModel
) {
    val scope = rememberCoroutineScope()
    // ¡¡SOLO ESTA LINEA: fuerza la recarga SIEMPRE al entrar!!
    LaunchedEffect(Unit) {
        partidoViewModel.cargarPartidosConNombres()
    }
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val partidos by partidoViewModel.partidosConNombres.collectAsState()

    var sortOption by remember { mutableStateOf("Nombre") }
    var ascending by remember { mutableStateOf(true) }
    var expanded by remember { mutableStateOf(false) }

    var estadoSeleccionado by remember { mutableStateOf(EstadoPartido.TODOS) }
    var expandedEstado by remember { mutableStateOf(false) }

    var showOptionsSheet by remember { mutableStateOf(false) }
    var partidoSeleccionado by remember { mutableStateOf<PartidoConNombresOnline?>(null) }

    var showConfirmDialog by remember { mutableStateOf(false) }

    // ---- BUSCADOR POR UID ----
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
        val fecha = parseFecha(partido.fecha)
        val horaInicio = parseHora(partido.horaInicio)
        val horaFin = parseHora(partido.horaFin)

        if (fecha == null || horaInicio == null || horaFin == null) {
            return EstadoPartido.PREVIA
        }

        return when {
            fecha.isBefore(hoy) -> EstadoPartido.FINALIZADO
            fecha.isAfter(hoy) -> EstadoPartido.PREVIA
            else -> {
                when {
                    ahora.isBefore(horaInicio) -> EstadoPartido.PREVIA
                    (ahora == horaInicio || (ahora.isAfter(horaInicio) && ahora.isBefore(horaFin))) -> EstadoPartido.JUGANDO
                    else -> EstadoPartido.FINALIZADO
                }
            }
        }
    }

    val partidosFiltrados = remember(partidos, estadoSeleccionado) {
        if (estadoSeleccionado == EstadoPartido.TODOS) {
            partidos
        } else {
            partidos.filter { partido ->
                obtenerEstadoPartido(partido) == estadoSeleccionado
            }
        }
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

    // --- Sheet con opciones Duplicar/Eliminar ---
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
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showOptionsSheet = false
                            scope.launch {
                                partidoViewModel.duplicarPartido(partidoSeleccionado!!.uid)
                            }
                        }
                )
                ListItem(
                    headlineContent = { Text("Eliminar", fontWeight = FontWeight.Medium, color = Color.Red) },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = null,
                            tint = Color.Red
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showOptionsSheet = false
                            showConfirmDialog = true
                        }
                )
            }
        }
    }

    // --- Confirmación para eliminar ---
    if (showConfirmDialog && partidoSeleccionado != null) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("¿Eliminar partido?", fontWeight = FontWeight.Bold) },
            text = { Text("¿Estás seguro que deseas eliminar el partido \"${partidoSeleccionado!!.nombreEquipoA} vs ${partidoSeleccionado!!.nombreEquipoB}\"?") },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    onClick = {
                        showConfirmDialog = false
                        scope.launch {
                            partidoViewModel.eliminarPartido(partidoSeleccionado!!.uid)
                        }
                    }
                ) {
                    Text("Eliminar", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // --- Confirmación para agregar por UID ---
    if (showAddConfirmDialog != null) {
        AlertDialog(
            onDismissRequest = { showAddConfirmDialog = null },
            title = { Text("¿Agregar partido?", fontWeight = FontWeight.Bold) },
            text = {
                Text("¿Seguro que deseas agregar el partido \"${showAddConfirmDialog!!.nombreEquipoA} vs ${showAddConfirmDialog!!.nombreEquipoB}\" a tu lista?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            partidoViewModel.agregarPartidoALista(showAddConfirmDialog!!)
                            partidoViewModel.cargarPartidosConNombres()
                        }
                        showAddConfirmDialog = null
                        searchText = ""
                        searchResults = emptyList()
                        showSearchDropdown = false
                    }
                ) {
                    Text("Agregar")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddConfirmDialog = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                onClick = {
                    navController.navigate("crear_partido_online")
                }
            ) {
                Icon(Icons.Default.ArrowUpward, contentDescription = "Agregar", tint = Color.White)
            }
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

            // ---- BUSCADOR POR UID + BOTÓN PEGAR Y BUSCAR----
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp)
            ) {
                Button(
                    onClick = { showSearchDropdown = !showSearchDropdown },
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    ),
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(Icons.Default.Search, contentDescription = "Buscar UID", modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Buscar por UID")
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
                                    // Pegar desde portapapeles
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
                                        .clickable {
                                            showAddConfirmDialog = p
                                        }
                                )
                            }
                        }
                    }
                }
            }
            // --------- RESTO DE LOS FILTROS Y LISTADO NORMAL ----------
            Card(
                elevation = CardDefaults.cardElevation(4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Text("Estado: ", fontSize = 15.sp)
                    Box {
                        OutlinedButton(
                            shape = RoundedCornerShape(16.dp),
                            onClick = { expandedEstado = true },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(estadoSeleccionado.display)
                        }
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
                    }
                    Spacer(modifier = Modifier.width(20.dp))
                    Text("Ordenar: ", fontSize = 15.sp)
                    Box {
                        OutlinedButton(
                            shape = RoundedCornerShape(16.dp),
                            onClick = { expanded = true },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(sortOption)
                            Icon(
                                imageVector = if (ascending) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Nombre") },
                                onClick = {
                                    sortOption = "Nombre"
                                    expanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Fecha") },
                                onClick = {
                                    sortOption = "Fecha"
                                    expanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(if (ascending) "Descendente" else "Ascendente") },
                                onClick = {
                                    ascending = !ascending
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
            LazyColumn {
                items(sortedPartidos) { partido ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
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
                                    text = "${partido.nombreEquipoA}  vs  ${partido.nombreEquipoB}",
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Fecha: ${partido.fecha}  -  Inicio: ${partido.horaInicio}  -  Fin: ${partido.horaFin}",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                val estado = obtenerEstadoPartido(partido)
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
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 12.sp
                                        )
                                    },
                                    colors = when (estado) {
                                        EstadoPartido.PREVIA -> AssistChipDefaults.assistChipColors(containerColor = Color(0xFF81C784))
                                        EstadoPartido.JUGANDO -> AssistChipDefaults.assistChipColors(containerColor = Color(0xFFFFF176))
                                        EstadoPartido.FINALIZADO -> AssistChipDefaults.assistChipColors(containerColor = Color(0xFFE57373))
                                        else -> AssistChipDefaults.assistChipColors()
                                    },
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
