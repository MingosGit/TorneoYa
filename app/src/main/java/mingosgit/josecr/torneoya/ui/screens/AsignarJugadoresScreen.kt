package mingosgit.josecr.torneoya.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import mingosgit.josecr.torneoya.viewmodel.AsignarJugadoresViewModel

@Composable
fun AsignarJugadoresScreen(
    navController: NavController,
    vm: AsignarJugadoresViewModel
) {
    var modoAleatorio by remember { mutableStateOf(false) }
    var listaNombres by remember { mutableStateOf(List(vm.numJugadores * 2) { "" }) }
    var equipoSeleccionado by remember { mutableStateOf("A") }

    LaunchedEffect(vm.numJugadores) {
        vm.setNumJugadoresPorEquipo(vm.numJugadores)
        if (listaNombres.size != vm.numJugadores * 2) {
            listaNombres = List(vm.numJugadores * 2) { idx -> listaNombres.getOrNull(idx) ?: "" }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        // SIEMPRE FIJO ARRIBA
        Text(
            "Asignar jugadores",
            fontSize = 24.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(Modifier.height(8.dp))

        // EQUIPO SELECCIONADO SI NO MODO ALEATORIO
        if (!modoAleatorio) {
            Row(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { equipoSeleccionado = "A" },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (equipoSeleccionado == "A") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                    ),
                    modifier = Modifier.weight(1f)
                ) { Text("Equipo A") }
                Spacer(Modifier.width(10.dp))
                Button(
                    onClick = { equipoSeleccionado = "B" },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (equipoSeleccionado == "B") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                    ),
                    modifier = Modifier.weight(1f)
                ) { Text("Equipo B") }
            }
        }

        Row(
            modifier = Modifier
                .padding(vertical = 16.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            Button(
                onClick = { modoAleatorio = false },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!modoAleatorio) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                ),
                modifier = Modifier.weight(1f)
            ) { Text("Manual") }
            Spacer(Modifier.width(10.dp))
            Button(
                onClick = { modoAleatorio = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (modoAleatorio) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                ),
                modifier = Modifier.weight(1f)
            ) { Text("Aleatorio") }
        }

        if (!modoAleatorio) {
            Text(
                if (equipoSeleccionado == "A") "Jugadores Equipo A" else "Jugadores Equipo B",
                fontSize = 18.sp,
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .align(Alignment.CenterHorizontally)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                val jugadores =
                    if (equipoSeleccionado == "A") vm.equipoAJugadores else vm.equipoBJugadores
                jugadores.forEachIndexed { idx, nombre ->
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = {
                            if (equipoSeleccionado == "A") vm.equipoAJugadores[idx] = it
                            else vm.equipoBJugadores[idx] = it
                        },
                        label = { Text("Jugador ${idx + 1}") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp)
                    )
                }
            }
        } else {
            Text(
                "Pon todos los nombres de jugadores, se asignarán aleatoriamente",
                fontSize = 16.sp,
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .align(Alignment.CenterHorizontally)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                listaNombres.forEachIndexed { idx, nombre ->
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = {
                            listaNombres = listaNombres.toMutableList().apply { set(idx, it) }
                        },
                        label = { Text("Jugador ${idx + 1}") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp)
                    )
                }
            }
            Button(
                onClick = {
                    val nombresLimpios = listaNombres.filter { it.isNotBlank() }
                    if (nombresLimpios.size >= vm.numJugadores * 2) {
                        vm.asignarAleatorio(nombresLimpios)
                        modoAleatorio = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
            ) {
                Text("Asignar aleatorio")
            }
        }

        Button(
            onClick = {
                vm.guardarEnBD {
                    navController.popBackStack()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
        ) {
            Text("Guardar")
        }
    }
}
