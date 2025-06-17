package mingosgit.josecr.torneoya.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import mingosgit.josecr.torneoya.viewmodel.EditarJugadoresEquipoViewModel

@Composable
fun EditarJugadoresEquipoScreen(
    partidoId: Long,
    equipoAId: Long,
    equipoBId: Long,
    navController: NavController,
    vm: EditarJugadoresEquipoViewModel
) {
    val equipoA by vm.equipoA.collectAsState()
    val equipoB by vm.equipoB.collectAsState()
    val nombresA by vm.nombresA.collectAsState()
    val nombresB by vm.nombresB.collectAsState()
    val loading by vm.loading.collectAsState()
    val error by vm.error.collectAsState()
    val guardado by vm.guardado.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(guardado) {
        if (guardado) navController.popBackStack()
    }

    if (loading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator() }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "Editar Jugadores",
            fontSize = 24.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = equipoA ?: "Equipo A",
                fontSize = 20.sp,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = equipoB ?: "Equipo B",
                fontSize = 20.sp,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth()) {
            Column(Modifier.weight(1f)) {
                LazyColumn {
                    itemsIndexed(nombresA) { i, value ->
                        OutlinedTextField(
                            value = value,
                            onValueChange = { vm.onNombreAChange(i, it) },
                            label = { Text("Jugador ${i + 1}") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                LazyColumn {
                    itemsIndexed(nombresB) { i, value ->
                        OutlinedTextField(
                            value = value,
                            onValueChange = { vm.onNombreBChange(i, it) },
                            label = { Text("Jugador ${i + 1}") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { vm.randomizar() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) { Text("Randomizar") }
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { scope.launch { vm.guardar() } },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) { Text("Guardar") }
        if (error != null) {
            Text(error!!, color = Color.Red, fontSize = 14.sp)
        }
    }
}
