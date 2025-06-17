package mingosgit.josecr.torneoya.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import mingosgit.josecr.torneoya.viewmodel.PartidoViewModel
import mingosgit.josecr.torneoya.viewmodel.PartidoConNombres

@Composable
fun PartidoScreen(
    navController: NavController,
    partidoViewModel: PartidoViewModel,
    equipoRepository: mingosgit.josecr.torneoya.repository.EquipoRepository
) {
    // Cambia este LaunchedEffect para que vuelva a cargar los partidos cada vez que esta pantalla sea mostrada.
    LaunchedEffect(Unit) {
        partidoViewModel.cargarPartidosConNombres(equipoRepository)
    }

    val partidos by partidoViewModel.partidosConNombres.collectAsState()

    // NUEVO: Recarga partidos al volver de editar o crear (trigger por navigationResult)
    // Este hack funciona porque al navegar con launchSingleTop=true, el backstack no se duplica
    // y LaunchedEffect(Unit) no se dispara si ya estaba montado, así que debemos forzar el reload.

    // Aquí nos suscribimos a un resultado simple (navigation result) que dispara reload:
    val needReload = remember {
        mutableStateOf(false)
    }

    // Listener para reload manual por navegación (coloca esto al inicio del Composable)
    LaunchedEffect(navController) {
        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            // Solo recarga cuando vuelves a PartidoScreen y hay un trigger en backStack
            val entry = controller.previousBackStackEntry
            if (destination.route == "partido" &&
                entry?.arguments?.containsKey("reload_partidos") == true
            ) {
                needReload.value = true
                entry.arguments?.remove("reload_partidos")
            }
        }
    }

    LaunchedEffect(needReload.value) {
        if (needReload.value) {
            partidoViewModel.cargarPartidosConNombres(equipoRepository)
            needReload.value = false
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate("crear_partido")
            }) {
                Text("+")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = "Partidos",
                fontSize = 24.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            LazyColumn {
                items(partidos) { partido ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable {
                                navController.navigate("visualizar_partido/${partido.id}")
                            }
                    ) {
                        Text(
                            text = "${partido.nombreEquipoA} vs ${partido.nombreEquipoB}",
                            fontSize = 18.sp
                        )
                        Text(
                            text = partido.fecha,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Divider(modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            }
        }
    }
}
