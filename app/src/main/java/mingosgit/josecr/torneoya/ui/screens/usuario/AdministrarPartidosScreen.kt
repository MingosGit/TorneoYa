package mingosgit.josecr.torneoya.ui.screens.usuario

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.KeyboardType
import androidx.navigation.NavController
import mingosgit.josecr.torneoya.viewmodel.usuario.AdministrarPartidosViewModel
import mingosgit.josecr.torneoya.data.entities.PartidoEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdministrarPartidosScreen(
    partido: PartidoEntity,
    viewModel: AdministrarPartidosViewModel,
    navController: NavController,
    equipoAId: Long,
    equipoBId: Long,
    jugadoresEquipoA: List<Pair<Long, String>>,
    jugadoresEquipoB: List<Pair<Long, String>>,
    nombreEquipoA: String, // <--- Añade este parámetro
    nombreEquipoB: String  // <--- Añade este parámetro
) {
    val goleadores by viewModel.goleadores.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var equipoSeleccionado by remember { mutableStateOf(equipoAId) }
    var jugadorSeleccionado by remember { mutableStateOf<Long?>(null) }
    var asistenciaSeleccionada by remember { mutableStateOf<Long?>(null) }
    var minuto by remember { mutableStateOf("") }
    var expandedEquipo by remember { mutableStateOf(false) }
    var expandedJugador by remember { mutableStateOf(false) }
    var expandedAsistente by remember { mutableStateOf(false) }

    LaunchedEffect(partido.id) {
        viewModel.cargarGoleadores(partido.id)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Administrar Goles del Partido") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.Remove, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "ID: ${partido.id} | Fecha: ${partido.fecha}",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Lista de goles registrados
            Text("Goles registrados", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(goleadores) { gol ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val equipoNombre = when (gol.equipoId) {
                            equipoAId -> nombreEquipoA
                            equipoBId -> nombreEquipoB
                            else -> "Equipo"
                        }
                        Text(
                            text = "$equipoNombre - Jugador: ${jugadoresEquipoA.plus(jugadoresEquipoB).find { it.first == gol.jugadorId }?.second ?: ""} " +
                                    (gol.minuto?.let { " (${it}') " } ?: "") +
                                    (gol.asistenciaJugadorId?.let { "Asist: ${jugadoresEquipoA.plus(jugadoresEquipoB).find { j -> j.first == it }?.second ?: ""}" } ?: ""),
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { viewModel.borrarGol(gol) }
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Quitar gol")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Divider()
            Spacer(modifier = Modifier.height(8.dp))
            Text("Agregar gol", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            // SELECCIÓN EQUIPO, JUGADOR, MINUTO Y ASISTENTE

            // Equipo
            ExposedDropdownMenuBox(
                expanded = expandedEquipo,
                onExpandedChange = { expandedEquipo = !expandedEquipo }
            ) {
                OutlinedTextField(
                    readOnly = true,
                    value = if (equipoSeleccionado == equipoAId) nombreEquipoA else nombreEquipoB,
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
                            equipoSeleccionado = equipoAId
                            jugadorSeleccionado = null
                            asistenciaSeleccionada = null
                            expandedEquipo = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(nombreEquipoB) },
                        onClick = {
                            equipoSeleccionado = equipoBId
                            jugadorSeleccionado = null
                            asistenciaSeleccionada = null
                            expandedEquipo = false
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Jugador (solo los del equipo seleccionado)
            val jugadoresActual = if (equipoSeleccionado == equipoAId) jugadoresEquipoA else jugadoresEquipoB
            ExposedDropdownMenuBox(
                expanded = expandedJugador,
                onExpandedChange = { expandedJugador = !expandedJugador }
            ) {
                OutlinedTextField(
                    readOnly = true,
                    value = jugadoresActual.find { it.first == jugadorSeleccionado }?.second ?: "",
                    onValueChange = {},
                    label = { Text("Jugador") },
                    placeholder = { Text("Seleccionar jugador") },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedJugador,
                    onDismissRequest = { expandedJugador = false }
                ) {
                    jugadoresActual.forEach { (id, nombre) ->
                        DropdownMenuItem(
                            text = { Text(nombre) },
                            onClick = {
                                jugadorSeleccionado = id
                                if (asistenciaSeleccionada == id) asistenciaSeleccionada = null
                                expandedJugador = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Minuto y Asistencia juntos
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = minuto,
                    onValueChange = { minuto = it.filter { c -> c.isDigit() }.take(3) },
                    label = { Text("Min") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                ExposedDropdownMenuBox(
                    expanded = expandedAsistente,
                    onExpandedChange = { expandedAsistente = !expandedAsistente }
                ) {
                    val asistentesPosibles = jugadoresActual.filter { it.first != jugadorSeleccionado }
                    OutlinedTextField(
                        readOnly = true,
                        value = asistentesPosibles.find { it.first == asistenciaSeleccionada }?.second ?: "",
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
                        asistentesPosibles.forEach { (id, nombre) ->
                            DropdownMenuItem(
                                text = { Text(nombre) },
                                onClick = {
                                    asistenciaSeleccionada = id
                                    expandedAsistente = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    viewModel.agregarGol(
                        partidoId = partido.id,
                        equipoId = equipoSeleccionado,
                        jugadorId = jugadorSeleccionado!!,
                        minuto = minuto.toIntOrNull(),
                        asistenciaJugadorId = asistenciaSeleccionada
                    )
                    jugadorSeleccionado = null
                    asistenciaSeleccionada = null
                    minuto = ""
                },
                enabled = jugadorSeleccionado != null && minuto.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Agregar gol")
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { showDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar y salir")
            }

            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Confirmar cambios") },
                    text = { Text("¿Guardar y volver?") },
                    confirmButton = {
                        TextButton(
                            onClick = { navController.popBackStack() }
                        ) { Text("Guardar") }
                    },
                    dismissButton = {
                        OutlinedButton(onClick = { showDialog = false }) {
                            Text("Cancelar")
                        }
                    }
                )
            }
        }
    }
}
