package mingosgit.josecr.torneoya.ui.screens.partido

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
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
    // SOLO CARGA JUGADORES EXISTENTES UNA VEZ
    LaunchedEffect(Unit) {
        vm.cargarJugadoresExistentes()
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
                    var expanded by remember { mutableStateOf(false) }
                    var searchQuery by remember { mutableStateOf("") }

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = value,
                            onValueChange = { newValue ->
                                if (idx == jugadores.size) {
                                    if (newValue.isNotBlank()) {
                                        if (vm.equipoSeleccionado == "A") vm.equipoAJugadores.add(newValue)
                                        else vm.equipoBJugadores.add(newValue)
                                    }
                                } else {
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
                                .weight(1f)
                                .padding(bottom = 4.dp)
                        )
                        IconButton(
                            onClick = { expanded = true }
                        ) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Elegir jugador")
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                label = { Text("Buscar") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                            vm.jugadoresDisponiblesManual(vm.equipoSeleccionado, idx)
                                .filter { it.nombre.contains(searchQuery, ignoreCase = true) }
                                .forEach { jugador ->
                                    DropdownMenuItem(
                                        text = { Text(jugador.nombre) },
                                        onClick = {
                                            if (idx == jugadores.size) {
                                                if (vm.equipoSeleccionado == "A") vm.equipoAJugadores.add(jugador.nombre)
                                                else vm.equipoBJugadores.add(jugador.nombre)
                                            } else {
                                                if (vm.equipoSeleccionado == "A") vm.equipoAJugadores[idx] = jugador.nombre
                                                else vm.equipoBJugadores[idx] = jugador.nombre
                                            }
                                            expanded = false
                                            searchQuery = ""
                                        }
                                    )
                                }
                        }
                    }
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
                    var expanded by remember { mutableStateOf(false) }
                    var searchQuery by remember { mutableStateOf("") }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
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
                                .weight(1f)
                                .padding(bottom = 4.dp)
                        )
                        IconButton(
                            onClick = { expanded = true }
                        ) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Elegir jugador")
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                label = { Text("Buscar") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                            vm.jugadoresDisponiblesAleatorio(idx)
                                .filter { it.nombre.contains(searchQuery, ignoreCase = true) }
                                .forEach { jugador ->
                                    DropdownMenuItem(
                                        text = { Text(jugador.nombre) },
                                        onClick = {
                                            if (idx == vm.listaNombres.size) {
                                                vm.listaNombres.add(jugador.nombre)
                                            } else {
                                                vm.listaNombres[idx] = jugador.nombre
                                            }
                                            expanded = false
                                            searchQuery = ""
                                        }
                                    )
                                }
                        }
                    }
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
