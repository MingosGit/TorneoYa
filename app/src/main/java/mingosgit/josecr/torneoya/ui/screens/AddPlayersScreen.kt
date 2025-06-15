package mingosgit.josecr.torneoya.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun AgregarJugadoresScreen(
    jugadores: List<String>,
    onJugadoresListo: (List<String>) -> Unit,
    navController: NavController,
    totalNecesario: Int
) {
    val viewModel = remember { AgregarJugadoresViewModel(jugadores) }
    var nombreNuevo by remember { mutableStateOf("") }

    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text("Agregar jugadores", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))

        Row {
            OutlinedTextField(
                value = nombreNuevo,
                onValueChange = { nombreNuevo = it },
                label = { Text("Nombre del jugador") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = {
                    val n = nombreNuevo.trim()
                    if (n.isNotBlank()) {
                        viewModel.agregar(n)
                        nombreNuevo = ""
                    }
                }
            ) {
                Text("Agregar")
            }
        }

        Spacer(Modifier.height(16.dp))

        Box(modifier = Modifier.weight(1f)) {
            LazyColumn {
                itemsIndexed(viewModel.nombres) { idx, nombre ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(nombre)
                        IconButton(onClick = { viewModel.borrar(idx) }) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.Delete,
                                contentDescription = "Eliminar"
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(
            enabled = viewModel.nombres.isNotEmpty(),
            onClick = {
                val listaActual = viewModel.getJugadores().toMutableList()
                val yaUsados = listaActual
                    .mapNotNull { nombre ->
                        val match = Regex("""Jugador(\d+)""").matchEntire(nombre)
                        match?.groupValues?.get(1)?.toIntOrNull()
                    }
                    .toMutableSet()
                var nextNum = 1
                while (listaActual.size < totalNecesario) {
                    while (yaUsados.contains(nextNum) || listaActual.contains("Jugador$nextNum")) {
                        nextNum++
                    }
                    listaActual.add("Jugador$nextNum")
                    yaUsados.add(nextNum)
                    nextNum++
                }
                onJugadoresListo(listaActual)
                navController.popBackStack()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Listo")
        }
    }
}
