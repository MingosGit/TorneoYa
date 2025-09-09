package mingosgit.josecr.torneoya.ui.screens.usuario.ajustes.local

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    nombreEquipoA: String,
    nombreEquipoB: String
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

    val gradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.09f),
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f)
        )
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Administrar Goles",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.large),
                    tonalElevation = 3.dp,
                    shadowElevation = 4.dp,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Text(
                            text = "Partido #${partido.id}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Fecha: ${partido.fecha}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Goles registrados",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(MaterialTheme.shapes.extraLarge),
                    tonalElevation = 1.dp,
                    color = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    if (goleadores.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Sin goles todavía", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.padding(8.dp)
                        ) {
                            items(goleadores) { gol ->
                                val equipoNombre = when (gol.equipoId) {
                                    equipoAId -> nombreEquipoA
                                    equipoBId -> nombreEquipoB
                                    else -> "Equipo"
                                }
                                Surface(
                                    shape = MaterialTheme.shapes.extraLarge,
                                    tonalElevation = 2.dp,
                                    color = if (gol.equipoId == equipoAId)
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.09f)
                                    else
                                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.09f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                                    ) {
                                        Box(
                                            Modifier
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (gol.equipoId == equipoAId)
                                                        MaterialTheme.colorScheme.primaryContainer
                                                    else
                                                        MaterialTheme.colorScheme.secondaryContainer
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                equipoNombre.firstOrNull()?.toString() ?: "",
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                            )
                                        }
                                        Spacer(Modifier.width(10.dp))
                                        Column(Modifier.weight(1f)) {
                                            val jugador = jugadoresEquipoA.plus(jugadoresEquipoB).find { it.first == gol.jugadorId }?.second ?: ""
                                            val asistencia = gol.asistenciaJugadorId?.let {
                                                jugadoresEquipoA.plus(jugadoresEquipoB).find { j -> j.first == it }?.second
                                            }
                                            Text(
                                                text = "$jugador ${gol.minuto?.let { "(${it}') " } ?: ""}",
                                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                                            )
                                            if (asistencia != null) {
                                                Text(
                                                    text = "Asist: $asistencia",
                                                    style = MaterialTheme.typography.labelMedium.copy(color = Color.Gray)
                                                )
                                            }
                                        }
                                        IconButton(
                                            onClick = { viewModel.borrarGol(gol) }
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Quitar gol")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    "Agregar gol",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.align(Alignment.Start)
                )
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
                        label = { Text("Equipo", fontWeight = FontWeight.SemiBold) },
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
                        label = { Text("Jugador", fontWeight = FontWeight.SemiBold) },
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
                        label = { Text("Min", fontWeight = FontWeight.SemiBold) },
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
                            label = { Text("Asistencia (opcional)", fontWeight = FontWeight.SemiBold) },
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

                Spacer(modifier = Modifier.height(18.dp))
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Text("Agregar gol", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { showDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Text("Guardar y salir", fontWeight = FontWeight.Bold)
                }

                if (showDialog) {
                    AlertDialog(
                        onDismissRequest = { showDialog = false },
                        title = { Text("Confirmar cambios", fontWeight = FontWeight.Bold) },
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
}
