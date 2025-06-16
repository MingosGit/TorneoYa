package mingosgit.josecr.torneoya.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import mingosgit.josecr.torneoya.data.entities.PartidoEntity
import mingosgit.josecr.torneoya.viewmodel.CreatePartidoViewModel

@Composable
fun CreatePartidoScreen(
    navController: NavController,
    createPartidoViewModel: CreatePartidoViewModel
) {
    val context = LocalContext.current

    var equipoA by remember { mutableStateOf("") }
    var equipoB by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var horaInicio by remember { mutableStateOf("") }
    var numeroPartes by remember { mutableStateOf("2") }
    var tiempoPorParte by remember { mutableStateOf("25") }
    var numeroJugadores by remember { mutableStateOf("5") }

    val showError = remember { mutableStateOf(false) }
    val errorMsg = remember { mutableStateOf("") }

    fun validarCampos(): Boolean {
        if (equipoA.isBlank() || equipoB.isBlank()) {
            errorMsg.value = "Rellena ambos equipos."
            return false
        }
        if (fecha.isBlank() || horaInicio.isBlank()) {
            errorMsg.value = "Pon fecha y hora."
            return false
        }
        return true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Crear Partido", fontSize = 28.sp, modifier = Modifier.padding(bottom = 24.dp))

        OutlinedTextField(
            value = equipoA,
            onValueChange = { equipoA = it },
            label = { Text("Nombre Equipo A") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = equipoB,
            onValueChange = { equipoB = it },
            label = { Text("Nombre Equipo B") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        )
        OutlinedTextField(
            value = fecha,
            onValueChange = { fecha = it },
            label = { Text("Fecha (YYYY-MM-DD)") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        )
        OutlinedTextField(
            value = horaInicio,
            onValueChange = { horaInicio = it },
            label = { Text("Hora (HH:mm)") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        )
        OutlinedTextField(
            value = numeroPartes,
            onValueChange = { numeroPartes = it.filter { c -> c.isDigit() } },
            label = { Text("Número de partes") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        )
        OutlinedTextField(
            value = tiempoPorParte,
            onValueChange = { tiempoPorParte = it.filter { c -> c.isDigit() } },
            label = { Text("Minutos por parte") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        )
        OutlinedTextField(
            value = numeroJugadores,
            onValueChange = { numeroJugadores = it.filter { c -> c.isDigit() } },
            label = { Text("Nº jugadores por equipo") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        )

        if (showError.value) {
            Text(
                text = errorMsg.value,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 10.dp)
            )
        }

        Button(
            onClick = {
                if (!validarCampos()) {
                    showError.value = true
                    return@Button
                }
                showError.value = false

                val id = System.currentTimeMillis()

                val partido = PartidoEntity(
                    id = id,
                    fecha = fecha,
                    horaInicio = horaInicio,
                    numeroPartes = numeroPartes.toIntOrNull() ?: 2,
                    tiempoPorParte = tiempoPorParte.toIntOrNull() ?: 25,
                    equipoA = equipoA,
                    equipoB = equipoB,
                    numeroJugadores = numeroJugadores.toIntOrNull() ?: 5
                )
                createPartidoViewModel.crearPartido(partido)

                navController.navigate("asignar_jugadores/$id")
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
        ) {
            Text("Continuar")
        }
    }
}
