package mingosgit.josecr.torneoya.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import mingosgit.josecr.torneoya.viewmodel.VisualizarPartidoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisualizarPartidoScreen(
    partidoId: Long,
    navController: NavController,
    vm: VisualizarPartidoViewModel
) {
    // 👇 Fuerza recarga al cambiar partidoId
    LaunchedEffect(partidoId) {
        vm.cargarDatos()
    }

    val uiState by vm.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Visualizar Partido") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = uiState.nombreEquipoA,
                    fontSize = 22.sp,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "  VS  ",
                    fontSize = 18.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Text(
                    text = uiState.nombreEquipoB,
                    fontSize = 22.sp,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    Text(
                        text = "Jugadores de ${uiState.nombreEquipoA}",
                        fontSize = 16.sp,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                    LazyColumn {
                        items(uiState.jugadoresEquipoA) { jugador ->
                            Text(
                                text = jugador,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                        if (uiState.jugadoresEquipoA.isEmpty()) {
                            item {
                                Text(
                                    text = "Sin jugadores asignados",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                ) {
                    Text(
                        text = "Jugadores de ${uiState.nombreEquipoB}",
                        fontSize = 16.sp,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                    LazyColumn {
                        items(uiState.jugadoresEquipoB) { jugador ->
                            Text(
                                text = jugador,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                        if (uiState.jugadoresEquipoB.isEmpty()) {
                            item {
                                Text(
                                    text = "Sin jugadores asignados",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
