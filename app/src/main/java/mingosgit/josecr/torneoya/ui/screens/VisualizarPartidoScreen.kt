package mingosgit.josecr.torneoya.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import mingosgit.josecr.torneoya.viewmodel.VisualizarPartidoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisualizarPartidoScreen(
    partidoId: Long,
    navController: NavController,
    vm: VisualizarPartidoViewModel
) {
    LaunchedEffect(partidoId) {
        vm.cargarDatos()
    }

    val uiState by vm.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    val eliminado by vm.eliminado.collectAsState()

    val navBackStackEntry = navController.currentBackStackEntryAsState()
    LaunchedEffect(navBackStackEntry.value) {
        val recargar = navController.previousBackStackEntry?.arguments?.getBoolean("reload_partido") == true
        if (recargar) {
            vm.cargarDatos()
            navController.previousBackStackEntry?.arguments?.remove("reload_partido")
        }
    }

    LaunchedEffect(eliminado) {
        if (eliminado) {
            navController.navigate("partido") {
                popUpTo("partido") { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Visualizar Partido",
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                navController.currentBackStackEntry?.arguments?.putBoolean("reload_partido", true)
                                navController.navigate("editar_partido/$partidoId")
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(
                            onClick = { showDeleteDialog = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Nombres de equipos deslizable horizontal si es largo, "VS" SIEMPRE al centro
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Nombre equipo A con scroll lateral si es muy largo
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(IntrinsicSize.Min)
                        .padding(end = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier
                            .horizontalScroll(rememberScrollState())
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = uiState.nombreEquipoA,
                            fontSize = 22.sp,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier
                                .fillMaxWidth()
                        )
                    }
                }
                // "VS" SIEMPRE centrado y fijo
                Text(
                    text = "  VS  ",
                    fontSize = 18.sp,
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .align(Alignment.CenterVertically),
                    textAlign = TextAlign.Center
                )
                // Nombre equipo B con scroll lateral si es muy largo
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(IntrinsicSize.Min)
                        .padding(start = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier
                            .horizontalScroll(rememberScrollState())
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = uiState.nombreEquipoB,
                            fontSize = 22.sp,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier
                                .fillMaxWidth()
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Jugadores",
                        fontSize = 16.sp,
                        style = MaterialTheme.typography.titleSmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        items(uiState.jugadoresEquipoA) { jugador ->
                            Text(
                                text = jugador,
                                modifier = Modifier
                                    .padding(vertical = 4.dp)
                                    .fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                        if (uiState.jugadoresEquipoA.isEmpty()) {
                            item {
                                Text(
                                    text = "Sin jugadores asignados",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .padding(vertical = 8.dp)
                                        .fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Jugadores",
                        fontSize = 16.sp,
                        style = MaterialTheme.typography.titleSmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        items(uiState.jugadoresEquipoB) { jugador ->
                            Text(
                                text = jugador,
                                modifier = Modifier
                                    .padding(vertical = 4.dp)
                                    .fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                        if (uiState.jugadoresEquipoB.isEmpty()) {
                            item {
                                Text(
                                    text = "Sin jugadores asignados",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .padding(vertical = 8.dp)
                                        .fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteDialog = false
                            vm.eliminarPartido()
                        }
                    ) {
                        Text("Eliminar", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancelar")
                    }
                },
                title = { Text("Eliminar Partido") },
                text = { Text("¿Seguro que deseas eliminar este partido? Esta acción no se puede deshacer.") }
            )
        }
    }
}
