package mingosgit.josecr.torneoya.ui.screens.partido

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import mingosgit.josecr.torneoya.viewmodel.partido.PartidoViewModel
import mingosgit.josecr.torneoya.repository.EquipoRepository
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun PartidoScreen(
    navController: NavController,
    partidoViewModel: PartidoViewModel,
    equipoRepository: EquipoRepository
) {
    LaunchedEffect(Unit) {
        partidoViewModel.cargarPartidosConNombres(equipoRepository)
    }

    val partidos by partidoViewModel.partidosConNombres.collectAsState()
    val needReload = remember { mutableStateOf(false) }

    LaunchedEffect(navController) {
        navController.addOnDestinationChangedListener { controller, destination, _ ->
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

    var sortOption by remember { mutableStateOf("Nombre") }
    var ascending by remember { mutableStateOf(true) }
    var expanded by remember { mutableStateOf(false) }

    fun parseFechaHora(fecha: String, hora: String): LocalDateTime? {
        val patronesFecha = listOf("yyyy-MM-dd", "dd/MM/yyyy", "yyyy/MM/dd")
        val patronesHora = listOf("HH:mm", "H:mm")
        for (pf in patronesFecha) {
            for (ph in patronesHora) {
                try {
                    val formatter = DateTimeFormatter.ofPattern("$pf $ph")
                    return LocalDateTime.parse("$fecha $hora", formatter)
                } catch (_: Exception) {}
            }
            // Por si no hay hora válida, usar 00:00
            try {
                val formatter = DateTimeFormatter.ofPattern("$pf HH:mm")
                return LocalDateTime.parse("$fecha 00:00", formatter)
            } catch (_: Exception) {}
        }
        return null
    }

    val sortedPartidos = remember(partidos, sortOption, ascending) {
        when (sortOption) {
            "Nombre" -> if (ascending) partidos.sortedBy { it.nombreEquipoA } else partidos.sortedByDescending { it.nombreEquipoA }
            "Fecha" -> {
                val lista = partidos.sortedWith(compareBy(
                    { parseFechaHora(it.fecha, it.horaInicio) ?: LocalDateTime.MIN },
                    { it.horaInicio },
                    { it.nombreEquipoA }
                ))
                if (ascending) lista else lista.reversed()
            }
            else -> partidos
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
                modifier = Modifier.padding(bottom = 10.dp)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            ) {
                Text("Ordenar por: ", fontSize = 15.sp)
                Box {
                    Button(
                        onClick = { expanded = true },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(sortOption)
                        Icon(
                            imageVector = if (ascending) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Nombre") },
                            onClick = {
                                sortOption = "Nombre"
                                expanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Fecha") },
                            onClick = {
                                sortOption = "Fecha"
                                expanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(if (ascending) "Descendente" else "Ascendente") },
                            onClick = {
                                ascending = !ascending
                                expanded = false
                            }
                        )
                    }
                }
            }
            LazyColumn {
                items(sortedPartidos) { partido ->
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
                            text = "Fecha: ${partido.fecha} - Inicio: ${partido.horaInicio} - Fin: ${partido.horaFin}",
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
