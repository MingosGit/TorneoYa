package mingosgit.josecr.torneoya.ui.screens.equipopredefinido

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import mingosgit.josecr.torneoya.viewmodel.equipopredefinido.EquiposPredefinidosViewModel
import mingosgit.josecr.torneoya.data.dao.EquipoPredefinidoConJugadores
import mingosgit.josecr.torneoya.data.entities.EquipoPredefinidoEntity

@Composable
fun EquiposPredefinidosScreen(
    navController: NavController,
    viewModel: EquiposPredefinidosViewModel
) {
    val equipos by viewModel.equipos.collectAsState()
    var nombreNuevoEquipo by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.recargarEquipos()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Equipos predefinidos", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))

        Row {
            OutlinedTextField(
                value = nombreNuevoEquipo,
                onValueChange = { nombreNuevoEquipo = it },
                label = { Text("Nombre equipo") },
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = {
                    navController.navigate("crear_equipo_predefinido") {
                        popUpTo("equipos_predefinidos") { inclusive = false }
                    }
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Crear equipo")
                Spacer(Modifier.width(4.dp))
                Text("Crear")
            }
        }

        Spacer(Modifier.height(16.dp))
        LazyColumn {
            items(equipos.size) { idx ->
                EquipoPredefinidoItem(
                    equipoConJugadores = equipos[idx],
                    onDelete = { viewModel.eliminarEquipo(it) }
                )
                Divider()
            }
        }
    }
}

@Composable
fun EquipoPredefinidoItem(
    equipoConJugadores: EquipoPredefinidoConJugadores,
    onDelete: (EquipoPredefinidoEntity) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(equipoConJugadores.equipo.nombre, style = MaterialTheme.typography.titleMedium)
            IconButton(onClick = { onDelete(equipoConJugadores.equipo) }) {
                Icon(Icons.Default.Delete, contentDescription = "Borrar equipo")
            }
        }
        if (equipoConJugadores.jugadores.isNotEmpty()) {
            Text(
                "Jugadores: " +
                        equipoConJugadores.jugadores.joinToString { it.nombre },
                style = MaterialTheme.typography.bodySmall
            )
        } else {
            Text("Sin jugadores", style = MaterialTheme.typography.bodySmall)
        }
    }
}
