package mingosgit.josecr.torneoya.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
    totalNecesario: Int,
    aleatorio: Boolean,
    onAleatorioChange: (Boolean) -> Unit
) {
    val jugadoresState = remember {
        mutableStateListOf<String>().apply {
            if (jugadores.isNotEmpty()) addAll(jugadores)
            while (size < totalNecesario) add("")
        }
    }

    // sincroniza cuando cambia el totalNecesario
    LaunchedEffect(totalNecesario) {
        while (jugadoresState.size < totalNecesario) jugadoresState.add("")
        while (jugadoresState.size > totalNecesario) jugadoresState.removeLast()
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text("Agregar jugadores", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))

        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Text("¿Equipos aleatorios?")
            Spacer(Modifier.width(8.dp))
            Switch(
                checked = aleatorio,
                onCheckedChange = { onAleatorioChange(it) }
            )
        }
        Spacer(Modifier.height(16.dp))

        Box(modifier = Modifier.weight(1f)) {
            if (aleatorio) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(jugadoresState.size) { idx ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            OutlinedTextField(
                                value = jugadoresState[idx],
                                onValueChange = { jugadoresState[idx] = it },
                                label = { Text("Jugador ${idx + 1}") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                enabled = jugadoresState.size > 1,
                                onClick = {
                                    jugadoresState.removeAt(idx)
                                    jugadoresState.add("")
                                }
                            ) {
                                Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Default.Delete,
                                    contentDescription = "Eliminar"
                                )
                            }
                        }
                    }
                }
            } else {
                val mitad = jugadoresState.size / 2
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        Text("Equipo 1:")
                        Spacer(Modifier.height(6.dp))
                    }
                    items(mitad) { idx ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            OutlinedTextField(
                                value = jugadoresState[idx],
                                onValueChange = { jugadoresState[idx] = it },
                                label = { Text("Jugador ${idx + 1}") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                enabled = jugadoresState.size > 1,
                                onClick = {
                                    jugadoresState.removeAt(idx)
                                    jugadoresState.add("")
                                }
                            ) {
                                Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Default.Delete,
                                    contentDescription = "Eliminar"
                                )
                            }
                        }
                    }
                    item {
                        Spacer(Modifier.height(16.dp))
                        Text("Equipo 2:")
                        Spacer(Modifier.height(6.dp))
                    }
                    items(jugadoresState.size - mitad) { idx ->
                        val realIdx = mitad + idx
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            OutlinedTextField(
                                value = jugadoresState[realIdx],
                                onValueChange = { jugadoresState[realIdx] = it },
                                label = { Text("Jugador ${realIdx - mitad + 1}") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                enabled = jugadoresState.size > 1,
                                onClick = {
                                    jugadoresState.removeAt(realIdx)
                                    jugadoresState.add("")
                                }
                            ) {
                                Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Default.Delete,
                                    contentDescription = "Eliminar"
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(
            enabled = jugadoresState.size == totalNecesario,
            onClick = {
                val listaFinal = jugadoresState.mapIndexed { i, nombre ->
                    if (nombre.isBlank()) "Jugador${i + 1}" else nombre
                }
                onJugadoresListo(listaFinal)
                navController.popBackStack()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Listo")
        }
    }
}
