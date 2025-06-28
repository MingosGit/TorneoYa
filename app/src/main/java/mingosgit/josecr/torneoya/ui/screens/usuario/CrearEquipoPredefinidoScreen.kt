package mingosgit.josecr.torneoya.ui.screens.equipopredefinido

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import mingosgit.josecr.torneoya.data.entities.JugadorEntity
import mingosgit.josecr.torneoya.viewmodel.usuario.CrearEquipoPredefinidoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearEquipoPredefinidoScreen(
    navController: NavController,
    viewModel: CrearEquipoPredefinidoViewModel
) {
    val nombreEquipo by viewModel.nombreEquipo.collectAsState()
    val jugadoresSeleccionados by viewModel.jugadoresSeleccionados.collectAsState()
    val jugadoresExistentes by viewModel.jugadoresExistentes.collectAsState()
    val creando by viewModel.creando.collectAsState()
    val error by viewModel.error.collectAsState()

    var expanded by remember { mutableStateOf(false) }
    var nuevoJugadorNombre by remember { mutableStateOf("") }
    var selectedJugador by remember { mutableStateOf<JugadorEntity?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Crear equipo predefinido", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = nombreEquipo,
            onValueChange = viewModel::onNombreEquipoChanged,
            label = { Text("Nombre equipo") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))

        Text("Jugadores", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(jugadoresSeleccionados) { jugador ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(jugador.nombre, style = MaterialTheme.typography.bodyLarge)
                    IconButton(onClick = { viewModel.quitarJugador(jugador) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar jugador")
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedJugador?.nombre ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Seleccionar jugador") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    modifier = Modifier.weight(1f)
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    jugadoresExistentes.filter { j ->
                        jugadoresSeleccionados.none { it.id == j.id }
                    }.forEach { jugador ->
                        DropdownMenuItem(
                            text = { Text(jugador.nombre) },
                            onClick = {
                                selectedJugador = jugador
                                expanded = false
                            }
                        )
                    }
                }
            }
            Spacer(Modifier.width(8.dp))
            IconButton(
                onClick = {
                    selectedJugador?.let {
                        viewModel.agregarJugadorExistente(it)
                        selectedJugador = null
                    }
                },
                enabled = selectedJugador != null
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar existente")
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = nuevoJugadorNombre,
                onValueChange = { nuevoJugadorNombre = it },
                label = { Text("Nuevo jugador") },
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            IconButton(
                onClick = {
                    viewModel.agregarJugadorNuevo(nuevoJugadorNombre)
                    nuevoJugadorNombre = ""
                },
                enabled = nuevoJugadorNombre.isNotBlank()
            ) {
                Icon(Icons.Default.Add, contentDescription = "Crear y agregar")
            }
        }

        if (error.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(error, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { viewModel.crearEquipo(navController) },
            enabled = nombreEquipo.isNotBlank() && jugadoresSeleccionados.isNotEmpty() && !creando,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (creando) "Creando..." else "Crear equipo")
        }
    }
}
