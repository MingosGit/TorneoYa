package mingosgit.josecr.torneoya.ui.screens

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
import mingosgit.josecr.torneoya.viewmodel.AsignarJugadoresViewModel

@Composable
fun AsignarJugadoresScreen(
    navController: NavController,
    vm: AsignarJugadoresViewModel
) {
    var modoAleatorio by remember { mutableStateOf(false) }
    var listaNombres by remember { mutableStateOf(List(vm.numJugadores * 2) { "" }) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text("Asignar jugadores", fontSize = 24.sp, modifier = Modifier.align(Alignment.CenterHorizontally))

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
            Text("Equipo A", fontSize = 18.sp, modifier = Modifier.padding(top = 12.dp, bottom = 4.dp))
            LazyColumn {
                itemsIndexed(vm.equipoAJugadores) { idx, nombre ->
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { vm.equipoAJugadores[idx] = it },
                        label = { Text("Jugador ${idx + 1}") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp)
                    )
                }
            }
            Text("Equipo B", fontSize = 18.sp, modifier = Modifier.padding(top = 12.dp, bottom = 4.dp))
            LazyColumn {
                itemsIndexed(vm.equipoBJugadores) { idx, nombre ->
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { vm.equipoBJugadores[idx] = it },
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
            Text("Pon todos los nombres de jugadores, se asignarán aleatoriamente", fontSize = 16.sp, modifier = Modifier.padding(vertical = 8.dp))
            LazyColumn {
                itemsIndexed(listaNombres) { idx, nombre ->
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { listaNombres = listaNombres.toMutableList().also { it[idx] = it[idx].replace(it[idx], nombre) } },
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
                    // reparte random
                    val nombresLimpios = listaNombres.filter { it.isNotBlank() }
                    if (nombresLimpios.size >= vm.numJugadores * 2) {
                        vm.asignarAleatorio(nombresLimpios)
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
