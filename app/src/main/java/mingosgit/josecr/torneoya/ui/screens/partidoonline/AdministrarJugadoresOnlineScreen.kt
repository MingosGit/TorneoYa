package mingosgit.josecr.torneoya.ui.screens.partidoonline

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import mingosgit.josecr.torneoya.viewmodel.partidoonline.AdministrarJugadoresOnlineViewModel
import mingosgit.josecr.torneoya.data.firebase.JugadorFirebase
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AdministrarJugadoresOnlineScreen(
    navController: NavController,
    vm: AdministrarJugadoresOnlineViewModel
) {
    LaunchedEffect(Unit) {
        vm.cargarJugadoresExistentes()
    }

    val miUid = FirebaseAuth.getInstance().currentUser?.uid
    var idxParaEliminar by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text(
            "Administrar jugadores",
            fontSize = 24.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { vm.equipoSeleccionado.value = "A" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (vm.equipoSeleccionado.value == "A") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                ),
                modifier = Modifier.weight(1f)
            ) { Text("Equipo A") }
            Spacer(Modifier.width(10.dp))
            Button(
                onClick = { vm.equipoSeleccionado.value = "B" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (vm.equipoSeleccionado.value == "B") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                ),
                modifier = Modifier.weight(1f)
            ) { Text("Equipo B") }
        }

        Spacer(Modifier.height(10.dp))
        Text(
            if (vm.equipoSeleccionado.value == "A") "Jugadores Equipo A" else "Jugadores Equipo B",
            fontSize = 18.sp,
            modifier = Modifier
                .padding(top = 12.dp, bottom = 4.dp)
                .align(Alignment.CenterHorizontally)
        )
        val jugadores = if (vm.equipoSeleccionado.value == "A") vm.equipoAJugadores else vm.equipoBJugadores

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            itemsIndexed(jugadores + listOf(JugadorFirebase())) { idx, value ->
                var expanded by remember { mutableStateOf(false) }
                var searchQuery by remember { mutableStateOf("") }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = value.nombre,
                        onValueChange = { newValue ->
                            if (idx == jugadores.size) {
                                if (newValue.isNotBlank()) {
                                    vm.agregarJugador(JugadorFirebase(nombre = newValue), vm.equipoSeleccionado.value)
                                }
                            } else {
                                if (newValue.isEmpty()) {
                                    idxParaEliminar = idx
                                } else {
                                    vm.cambiarNombreJugador(idx, vm.equipoSeleccionado.value, newValue)
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
                        vm.jugadoresDisponiblesManual(vm.equipoSeleccionado.value, idx)
                            .filter { it.nombre.contains(searchQuery, ignoreCase = true) }
                            .forEach { jugador ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(jugador.nombre)
                                            if (miUid != null && jugador.uid == miUid) {
                                                Text(" /Tú", color = MaterialTheme.colorScheme.primary, fontSize = 14.sp, modifier = Modifier.padding(start = 4.dp))
                                            }
                                        }
                                    },
                                    onClick = {
                                        if (idx == jugadores.size) {
                                            vm.agregarJugador(jugador, vm.equipoSeleccionado.value)
                                        } else {
                                            vm.cambiarNombreJugador(idx, vm.equipoSeleccionado.value, jugador.nombre)
                                        }
                                        expanded = false
                                        searchQuery = ""
                                    }
                                )
                            }
                    }
                    if (idx != jugadores.size) {
                        IconButton(
                            onClick = { idxParaEliminar = idx },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = Color.Transparent,
                                contentColor = Color.Red
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar jugador",
                                tint = Color.Red
                            )
                        }
                    }
                }

                // Popup de confirmación para eliminar
                if (idxParaEliminar == idx) {
                    AlertDialog(
                        onDismissRequest = { idxParaEliminar = null },
                        title = { Text("¿Eliminar jugador?") },
                        text = { Text("¿Estás seguro de que quieres eliminar a este jugador?") },
                        confirmButton = {
                            TextButton(onClick = {
                                vm.eliminarJugador(idx, vm.equipoSeleccionado.value)
                                idxParaEliminar = null
                            }) {
                                Text("Eliminar", color = Color.Red)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { idxParaEliminar = null }) {
                                Text("Cancelar")
                            }
                        }
                    )
                }
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
            Text("Guardar y volver")
        }
    }
}
