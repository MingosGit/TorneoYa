package mingosgit.josecr.torneoya.ui.screens.usuario

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
    jugadoresEquipoB: List<Pair<Long, String>>
) {
    val goleadores by viewModel.goleadores.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var equipoSeleccionado by remember { mutableStateOf(equipoAId) }
    var jugadorSeleccionado by remember { mutableStateOf<Long?>(null) }
    var asistenciaSeleccionada by remember { mutableStateOf<Long?>(null) }
    var minuto by remember { mutableStateOf("") }

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
                        Text(
                            text = "Equipo ${if (gol.equipoId == equipoAId) "A" else "B"} - Jugador: ${jugadoresEquipoA.plus(jugadoresEquipoB).find { it.first == gol.jugadorId }?.second ?: ""} " +
                                    (gol.minuto?.let { " (${it}') " } ?: "") +
                                    (gol.asistenciaJugadorId?.let { "Asist: ${jugadoresEquipoA.plus(jugadoresEquipoB).find { j -> j.first == it }?.second ?: ""}" } ?: ""),
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { viewModel.borrarGol(gol) },
                            colors = IconButtonDefaults.iconButtonColors(contentColor = Color.Red)
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

            // SELECCIÓN EQUIPO Y JUGADOR
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Selección equipo (A o B)
                var expandedEquipo by remember { mutableStateOf(false) }
                TextButton(onClick = { expandedEquipo = true }) {
                    Text(if (equipoSeleccionado == equipoAId) "Equipo A" else "Equipo B")
                }
                DropdownMenu(
                    expanded = expandedEquipo,
                    onDismissRequest = { expandedEquipo = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Equipo A") },
                        onClick = {
                            equipoSeleccionado = equipoAId
                            jugadorSeleccionado = null
                            asistenciaSeleccionada = null
                            expandedEquipo = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Equipo B") },
                        onClick = {
                            equipoSeleccionado = equipoBId
                            jugadorSeleccionado = null
                            asistenciaSeleccionada = null
                            expandedEquipo = false
                        }
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Selección jugador
                var expandedJugador by remember { mutableStateOf(false) }
                TextButton(
                    onClick = { expandedJugador = true },
                    enabled = true
                ) {
                    Text(jugadoresEquipoA.plus(jugadoresEquipoB).find { it.first == jugadorSeleccionado }?.second ?: "Seleccionar jugador")
                }
                DropdownMenu(
                    expanded = expandedJugador,
                    onDismissRequest = { expandedJugador = false }
                ) {
                    val jugadores = if (equipoSeleccionado == equipoAId) jugadoresEquipoA else jugadoresEquipoB
                    jugadores.forEach { (id, nombre) ->
                        DropdownMenuItem(
                            text = { Text(nombre) },
                            onClick = {
                                jugadorSeleccionado = id
                                expandedJugador = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Minuto y asistencia
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = minuto,
                    onValueChange = { minuto = it.filter { c -> c.isDigit() } },
                    label = { Text("Minuto") },
                    modifier = Modifier.width(100.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                var expandedAsistente by remember { mutableStateOf(false) }
                TextButton(
                    onClick = { expandedAsistente = true },
                    enabled = jugadorSeleccionado != null
                ) {
                    Text(jugadoresEquipoA.plus(jugadoresEquipoB).find { it.first == asistenciaSeleccionada }?.second ?: "Asistente (opcional)")
                }
                DropdownMenu(
                    expanded = expandedAsistente,
                    onDismissRequest = { expandedAsistente = false }
                ) {
                    val jugadores = if (equipoSeleccionado == equipoAId) jugadoresEquipoA else jugadoresEquipoB
                    DropdownMenuItem(
                        text = { Text("Sin asistencia") },
                        onClick = {
                            asistenciaSeleccionada = null
                            expandedAsistente = false
                        }
                    )
                    jugadores.filter { it.first != jugadorSeleccionado }.forEach { (id, nombre) ->
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
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (jugadorSeleccionado != null) {
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
                    }
                },
                enabled = jugadorSeleccionado != null
            ) {
                Text("Agregar gol")
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    showDialog = true
                }
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
                            onClick = {
                                navController.popBackStack()
                            }
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
