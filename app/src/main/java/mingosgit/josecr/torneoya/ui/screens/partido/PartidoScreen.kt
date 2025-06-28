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
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

enum class EstadoPartido(val display: String) {
    PREVIA("Previa"),
    JUGANDO("Jugando"),
    FINALIZADO("Finalizado")
}

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

    var estadoSeleccionado by remember { mutableStateOf(EstadoPartido.PREVIA) }
    var expandedEstado by remember { mutableStateOf(false) }

    fun parseFecha(fecha: String): LocalDate? {
        val patronesFecha = listOf("yyyy-MM-dd", "dd/MM/yyyy", "yyyy/MM/dd", "dd-MM-yyyy")
        for (pf in patronesFecha) {
            try {
                val formatter = DateTimeFormatter.ofPattern(pf)
                return LocalDate.parse(fecha, formatter)
            } catch (_: Exception) {}
        }
        return null
    }

    fun parseHora(hora: String): LocalTime? {
        val patronesHora = listOf("HH:mm", "H:mm")
        for (ph in patronesHora) {
            try {
                val formatter = DateTimeFormatter.ofPattern(ph)
                return LocalTime.parse(hora, formatter)
            } catch (_: Exception) {}
        }
        return null
    }

    fun obtenerEstadoPartido(partido: mingosgit.josecr.torneoya.viewmodel.partido.PartidoConNombres): EstadoPartido {
        val hoy = LocalDate.now()
        val ahora = LocalTime.now()
        val fecha = parseFecha(partido.fecha)
        val horaInicio = parseHora(partido.horaInicio)
        val horaFin = parseHora(partido.horaFin)

        // DEBUG: Puedes borrar estos println cuando ya funcione
        println("Partido ID: ${partido.id}, fechaRaw: ${partido.fecha}, horaInicioRaw: ${partido.horaInicio}, horaFinRaw: ${partido.horaFin}")
        println("fecha: $fecha, horaInicio: $horaInicio, horaFin: $horaFin")

        if (fecha == null || horaInicio == null || horaFin == null) {
            println("Estado PREVIA por datos nulos/incorrectos")
            return EstadoPartido.PREVIA
        }

        return when {
            fecha.isBefore(hoy) -> EstadoPartido.FINALIZADO
            fecha.isAfter(hoy) -> EstadoPartido.PREVIA
            else -> { // Es el mismo día
                when {
                    ahora.isBefore(horaInicio) -> EstadoPartido.PREVIA
                    (ahora == horaInicio || (ahora.isAfter(horaInicio) && ahora.isBefore(horaFin))) -> EstadoPartido.JUGANDO
                    else -> EstadoPartido.FINALIZADO
                }
            }
        }
    }

    val partidosFiltrados = remember(partidos, estadoSeleccionado) {
        partidos.filter { partido ->
            obtenerEstadoPartido(partido) == estadoSeleccionado
        }
    }

    val sortedPartidos = remember(partidosFiltrados, sortOption, ascending) {
        when (sortOption) {
            "Nombre" -> if (ascending) partidosFiltrados.sortedBy { it.nombreEquipoA } else partidosFiltrados.sortedByDescending { it.nombreEquipoA }
            "Fecha" -> {
                val patronesFecha = listOf("yyyy-MM-dd", "dd/MM/yyyy", "yyyy/MM/dd", "dd-MM-yyyy")
                partidosFiltrados.sortedBy { p ->
                    var fecha: LocalDate? = null
                    for (pf in patronesFecha) {
                        try {
                            val formatter = DateTimeFormatter.ofPattern(pf)
                            fecha = LocalDate.parse(p.fecha, formatter)
                            break
                        } catch (_: Exception) {}
                    }
                    fecha ?: LocalDate.MIN
                }.let { if (ascending) it else it.reversed() }
            }
            else -> partidosFiltrados
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
                // FILTRO ESTADO
                Text("Estado: ", fontSize = 15.sp)
                Box {
                    Button(
                        onClick = { expandedEstado = true },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(estadoSeleccionado.display)
                    }
                    DropdownMenu(
                        expanded = expandedEstado,
                        onDismissRequest = { expandedEstado = false }
                    ) {
                        EstadoPartido.values().forEach { estado ->
                            DropdownMenuItem(
                                text = { Text(estado.display) },
                                onClick = {
                                    estadoSeleccionado = estado
                                    expandedEstado = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                // ORDENAR
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
