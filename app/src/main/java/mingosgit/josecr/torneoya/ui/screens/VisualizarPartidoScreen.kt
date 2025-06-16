package mingosgit.josecr.torneoya.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import mingosgit.josecr.torneoya.viewmodel.VisualizarPartidoViewModel

@Composable
fun VisualizarPartidoScreen(
    partidoId: Long,
    navController: NavController,
    vm: VisualizarPartidoViewModel
) {
    val partido by vm.partido.collectAsStateWithLifecycle()
    val nombreEquipoA by vm.nombreEquipoA.collectAsStateWithLifecycle()
    val nombreEquipoB by vm.nombreEquipoB.collectAsStateWithLifecycle()
    val jugadoresEquipoA by vm.jugadoresEquipoA.collectAsStateWithLifecycle()
    val jugadoresEquipoB by vm.jugadoresEquipoB.collectAsStateWithLifecycle()
    val cargando by vm.cargando.collectAsStateWithLifecycle()

    if (cargando) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    if (partido == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No se encontró el partido.", color = MaterialTheme.colorScheme.error)
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Detalles del Partido", fontSize = 28.sp, modifier = Modifier.padding(bottom = 24.dp))
            Text("Fecha: ${partido!!.fecha}    Hora: ${partido!!.horaInicio}", fontSize = 16.sp)

            Spacer(modifier = Modifier.height(24.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(nombreEquipoA, fontSize = 20.sp, modifier = Modifier.padding(bottom = 8.dp))
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 200.dp)
                    ) {
                        items(jugadoresEquipoA) { nombre ->
                            Text(nombre, fontSize = 16.sp, modifier = Modifier.padding(2.dp))
                        }
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(nombreEquipoB, fontSize = 20.sp, modifier = Modifier.padding(bottom = 8.dp))
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 200.dp)
                    ) {
                        items(jugadoresEquipoB) { nombre ->
                            Text(nombre, fontSize = 16.sp, modifier = Modifier.padding(2.dp))
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = {
                navController.navigate("editar_partido/${partidoId}")
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(32.dp)
        ) {
            Text("Editar")
        }
    }
}
