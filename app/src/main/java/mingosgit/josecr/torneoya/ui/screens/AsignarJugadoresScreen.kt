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
    LaunchedEffect(vm.numJugadores) {
        vm.setNumJugadoresPorEquipo(vm.numJugadores)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text(
            "Asignar jugadores",
            fontSize = 24.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(Modifier.height(8.dp))

        if (!vm.modoAleatorio) {
            Row(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { vm.equipoSeleccionado = "A" },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (vm.equipoSeleccionado == "A") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                    ),
                    modifier = Modifier.weight(1f)
                ) { Text("Equipo A") }
                Spacer(Modifier.width(10.dp))
                Button(
                    onClick = { vm.equipoSeleccionado = "B" },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (vm.equipoSeleccionado == "B") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
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
                onClick = { vm.cambiarModo(false) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!vm.modoAleatorio) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                ),
                modifier = Modifier.weight(1f)
            ) { Text("Manual") }
            Spacer(Modifier.width(10.dp))
            Button(
                onClick = { vm.cambiarModo(true) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (vm.modoAleatorio) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                ),
                modifier = Modifier.weight(1f)
            ) { Text("Aleatorio") }
        }

        if (!vm.modoAleatorio) {
            Text(
                if (vm.equipoSeleccionado == "A") "Jugadores Equipo A" else "Jugadores Equipo B",
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
                    if (vm.equipoSeleccionado == "A") vm.equipoAJugadores else vm.equipoBJugadores
                jugadores.forEachIndexed { idx, nombre ->
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = {
                            if (vm.equipoSeleccionado == "A") vm.equipoAJugadores[idx] = it
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
                vm.listaNombres.forEachIndexed { idx, nombre ->
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = {
                            vm.listaNombres[idx] = it
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
        }

        Button(
            onClick = {
                if (vm.modoAleatorio) {
                    val nombresLimpios = vm.listaNombres.filter { it.isNotBlank() }
                    if (nombresLimpios.size >= 2) { // Debe haber al menos 2 jugadores
                        vm.repartirAleatoriamente(nombresLimpios)
                        vm.cambiarModo(false)
                    }
                }
                vm.guardarEnBD {
                    navController.popBackStack()
                    navController.navigate("partido")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
        ) {
            Text("Guardar asignación")
        }
    }
}
