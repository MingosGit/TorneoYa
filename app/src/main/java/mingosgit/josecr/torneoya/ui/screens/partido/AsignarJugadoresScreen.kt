package mingosgit.josecr.torneoya.ui.screens.partido

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import mingosgit.josecr.torneoya.viewmodel.partido.AsignarJugadoresViewModel

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
            val jugadores =
                if (vm.equipoSeleccionado == "A") vm.equipoAJugadores else vm.equipoBJugadores

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                itemsIndexed(jugadores + "") { idx, value ->
                    OutlinedTextField(
                        value = value,
                        onValueChange = { newValue ->
                            if (idx == jugadores.size) {
                                // Último campo: agregar jugador si se escribe algo
                                if (newValue.isNotBlank()) {
                                    if (vm.equipoSeleccionado == "A") vm.equipoAJugadores.add(newValue)
                                    else vm.equipoBJugadores.add(newValue)
                                }
                            } else {
                                // Si se borra el campo, eliminar el jugador
                                if (newValue.isEmpty()) {
                                    if (vm.equipoSeleccionado == "A") vm.equipoAJugadores.removeAt(idx)
                                    else vm.equipoBJugadores.removeAt(idx)
                                } else {
                                    if (vm.equipoSeleccionado == "A") vm.equipoAJugadores[idx] = newValue
                                    else vm.equipoBJugadores[idx] = newValue
                                }
                            }
                        },
                        label = { Text(if (idx == jugadores.size) "Agregar un jugador nuevo" else "Jugador ${idx + 1}") },
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
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                itemsIndexed(vm.listaNombres + "") { idx, value ->
                    OutlinedTextField(
                        value = value,
                        onValueChange = { newValue ->
                            if (idx == vm.listaNombres.size) {
                                if (newValue.isNotBlank()) {
                                    vm.listaNombres.add(newValue)
                                }
                            } else {
                                if (newValue.isEmpty()) {
                                    vm.listaNombres.removeAt(idx)
                                } else {
                                    vm.listaNombres[idx] = newValue
                                }
                            }
                        },
                        label = { Text(if (idx == vm.listaNombres.size) "Agregar un jugador nuevo" else "Jugador ${idx + 1}") },
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
                    if (nombresLimpios.size >= 2) {
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
