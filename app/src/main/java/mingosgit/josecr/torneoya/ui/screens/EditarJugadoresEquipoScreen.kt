package mingosgit.josecr.torneoya.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp) // espacio para los botones fijos abajo
        ) {
            Text(
                text = "Editar Jugadores",
                fontSize = 24.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Equipo A
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier
                            .horizontalScroll(rememberScrollState()),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = equipoA ?: "Equipo A",
                            fontSize = 20.sp,
                        )
                    }
                }
                // Espaciado entre los nombres
                Spacer(modifier = Modifier.width(8.dp))
                // Equipo B
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier
                            .horizontalScroll(rememberScrollState()),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = equipoB ?: "Equipo B",
                            fontSize = 20.sp,
                        )
                    }
                }
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    itemsIndexed(nombresA) { i, value ->
                        OutlinedTextField(
                            value = value,
                            onValueChange = { vm.onNombreAChange(i, it) },
                            label = { Text(if (i == nombresA.lastIndex) "Agregar Jugador" else "Jugador ${i + 1}") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    itemsIndexed(nombresB) { i, value ->
                        OutlinedTextField(
                            value = value,
                            onValueChange = { vm.onNombreBChange(i, it) },
                            label = { Text(if (i == nombresB.lastIndex) "Agregar Jugador" else "Jugador ${i + 1}") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        )
                    }
                }
            }
            if (error != null) {
                Spacer(Modifier.height(8.dp))
                Text(error!!, color = Color.Red, fontSize = 14.sp)
            }
        }
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
        ) {
            Button(
                onClick = { vm.randomizar() },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
            ) { Text("Randomizar") }
            Button(
                onClick = { scope.launch { vm.guardar() } },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
            ) { Text("Guardar") }
        }
    }
}
