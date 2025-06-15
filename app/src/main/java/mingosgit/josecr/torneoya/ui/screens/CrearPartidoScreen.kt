package mingosgit.josecr.torneoya.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun CrearPartidoScreen(
    onPartidoCreado: (List<List<String>>, Int) -> Unit
) {
    val maxIntegrantes = 24
    val maxEquipos = 4

    var numIntegrantes by remember { mutableStateOf(2) }
    var numEquipos by remember { mutableStateOf(2) }
    var tiempo by remember { mutableStateOf(10) }
    var aleatorio by remember { mutableStateOf(true) }
    var nombresJugadores by remember { mutableStateOf(List(numIntegrantes * numEquipos) { "" }) }
    var equiposManuales by remember { mutableStateOf(List(numEquipos) { List(numIntegrantes) { "" } }) }
    var showError by remember { mutableStateOf(false) }

    // Reinicia los datos si cambia la cantidad de equipos/integrantes o modo
    LaunchedEffect(numIntegrantes, numEquipos, aleatorio) {
        nombresJugadores = List(numIntegrantes * numEquipos) { "" }
        equiposManuales = List(numEquipos) { List(numIntegrantes) { "" } }
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text("Crear Partido", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))

        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Text("Integrantes/equipo:")
            Spacer(Modifier.width(8.dp))
            OutlinedTextField(
                value = numIntegrantes.toString(),
                onValueChange = {
                    val v = it.toIntOrNull() ?: 1
                    numIntegrantes = v.coerceIn(1, maxIntegrantes)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.width(70.dp)
            )
        }
        Spacer(Modifier.height(8.dp))

        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Text("Cantidad de equipos:")
            Spacer(Modifier.width(8.dp))
            OutlinedTextField(
                value = numEquipos.toString(),
                onValueChange = {
                    val v = it.toIntOrNull() ?: 2
                    numEquipos = v.coerceIn(2, maxEquipos)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.width(70.dp)
            )
        }
        Spacer(Modifier.height(8.dp))

        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Text("Minutos por partido:")
            Spacer(Modifier.width(8.dp))
            OutlinedTextField(
                value = tiempo.toString(),
                onValueChange = { tiempo = (it.toIntOrNull() ?: 1).coerceIn(1, 240) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.width(90.dp)
            )
        }
        Spacer(Modifier.height(12.dp))

        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Text("¿Equipos aleatorios?")
            Spacer(Modifier.width(8.dp))
            Switch(
                checked = aleatorio,
                onCheckedChange = { aleatorio = it }
            )
        }

        Spacer(Modifier.height(16.dp))

        Box(modifier = Modifier.weight(1f)) {
            if (aleatorio) {
                LazyColumn {
                    item {
                        Text("Nombres de los jugadores:")
                        Spacer(Modifier.height(8.dp))
                    }
                    itemsIndexed(nombresJugadores) { idx, nombre ->
                        OutlinedTextField(
                            value = nombre,
                            onValueChange = {
                                nombresJugadores = nombresJugadores.toMutableList().also { list -> list[idx] = it }
                            },
                            label = { Text("Jugador ${idx + 1}") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                        )
                    }
                }
            } else {
                LazyColumn {
                    item {
                        Text("Integrantes de cada equipo:")
                        Spacer(Modifier.height(8.dp))
                    }
                    items(numEquipos) { equipoIdx ->
                        Text("Equipo ${equipoIdx + 1}", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(4.dp))
                        repeat(numIntegrantes) { integranteIdx ->
                            OutlinedTextField(
                                value = equiposManuales[equipoIdx][integranteIdx],
                                onValueChange = { newValue ->
                                    equiposManuales = equiposManuales.toMutableList().also { equiposList ->
                                        equiposList[equipoIdx] = equiposList[equipoIdx].toMutableList().also { intList ->
                                            intList[integranteIdx] = newValue
                                        }
                                    }
                                },
                                label = { Text("Integrante ${integranteIdx + 1}") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }

        if (showError) {
            Text("¡Faltan nombres!", color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                if (aleatorio) {
                    if (nombresJugadores.any { it.isBlank() }) {
                        showError = true
                        return@Button
                    }
                    showError = false
                    val jugadores = nombresJugadores.shuffled()
                    val equiposFinales = List(numEquipos) { i ->
                        jugadores.drop(i * numIntegrantes).take(numIntegrantes)
                    }
                    onPartidoCreado(equiposFinales, tiempo)
                } else {
                    if (equiposManuales.flatten().any { it.isBlank() }) {
                        showError = true
                        return@Button
                    }
                    showError = false
                    onPartidoCreado(equiposManuales, tiempo)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Crear Partido")
        }
    }
}

