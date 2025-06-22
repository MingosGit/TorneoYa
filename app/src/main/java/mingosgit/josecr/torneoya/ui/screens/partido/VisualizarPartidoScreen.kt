package mingosgit.josecr.torneoya.ui.screens.partido

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import mingosgit.josecr.torneoya.viewmodel.partido.VisualizarPartidoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisualizarPartidoScreen(
    partidoId: Long,
    navController: NavController,
    vm: VisualizarPartidoViewModel
) {
    LaunchedEffect(partidoId) { vm.cargarDatos() }

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
                Text(
                    text = "  VS  ",
                    fontSize = 18.sp,
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .align(Alignment.CenterVertically),
                    textAlign = TextAlign.Center
                )
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

            // MOSTRAR GOLES DE CADA EQUIPO
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${uiState.golesEquipoA}",
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "-",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "${uiState.golesEquipoB}",
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .background(
                        when (uiState.estado) {
                            "Finalizado" -> Color(0xFFE0E0E0)
                            "Jugando" -> Color(0xFFB3E5FC)
                            "Descanso" -> Color(0xFFFFF59D)
                            else -> Color(0xFFEEEEEE)
                        }
                    )
                    .height(48.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Estado: ${uiState.estado}",
                    modifier = Modifier.padding(start = 16.dp),
                    fontSize = 16.sp,
                    color = Color.Black
                )
                Text(
                    text = if (uiState.estado == "Jugando") "${uiState.minutoActual}" else if (uiState.estado == "Descanso") "Descanso" else "",
                    modifier = Modifier.padding(end = 16.dp),
                    fontSize = 16.sp,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Menú horizontal con pestañas
            var selectedTabIndex by remember { mutableStateOf(0) }
            val tabTitles = listOf("Jugadores", "Eventos", "Comentarios", "Encuestas")

            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier.fillMaxWidth(),
                edgePadding = 0.dp
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title, fontSize = 16.sp) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            when (selectedTabIndex) {
                0 -> { // Jugadores
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
                                text = uiState.nombreEquipoA,
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
                                text = uiState.nombreEquipoB,
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
                1 -> { // Eventos
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Sin eventos",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                2 -> { // Comentarios
                    val state by vm.comentariosEncuestasState.collectAsState()
                    var textoComentario by remember { mutableStateOf("") }
                    val usuarioNombre = "Tú"
                    Column(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = textoComentario,
                            onValueChange = { textoComentario = it },
                            label = { Text("Escribe un comentario") },
                            singleLine = false,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(onSend = {
                                if (textoComentario.isNotBlank()) {
                                    vm.agregarComentario(usuarioNombre, textoComentario)
                                    textoComentario = ""
                                }
                            })
                        )
                        Button(
                            onClick = {
                                if (textoComentario.isNotBlank()) {
                                    vm.agregarComentario(usuarioNombre, textoComentario)
                                    textoComentario = ""
                                }
                            },
                            modifier = Modifier.align(Alignment.End).padding(end = 8.dp)
                        ) {
                            Text("Enviar")
                        }

                        LazyColumn(modifier = Modifier.fillMaxHeight()) {
                            items(state.comentarios) { comentario ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp, horizontal = 8.dp)
                                ) {
                                    Text(
                                        text = comentario.usuarioNombre,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = comentario.texto,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = comentario.fechaHora,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Divider()
                            }
                            if (state.comentarios.isEmpty()) {
                                item {
                                    Text(
                                        text = "Sin comentarios",
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier
                                            .padding(vertical = 32.dp)
                                            .fillMaxWidth(),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
                3 -> { // Encuestas
                    val state by vm.comentariosEncuestasState.collectAsState()
                    var pregunta by remember { mutableStateOf("") }
                    var opciones = remember { mutableStateListOf("", "") }
                    Column(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = pregunta,
                            onValueChange = { pregunta = it },
                            label = { Text("Pregunta de la encuesta") },
                            modifier = Modifier.fillMaxWidth().padding(8.dp)
                        )
                        opciones.forEachIndexed { idx, valor ->
                            OutlinedTextField(
                                value = valor,
                                onValueChange = { opciones[idx] = it },
                                label = { Text("Opción ${idx + 1}") },
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                        Row(modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)) {
                            if (opciones.size < 5)
                                Button(onClick = { opciones.add("") }, modifier = Modifier.padding(end = 8.dp)) {
                                    Text("+ Opción")
                                }
                            if (opciones.size > 2)
                                Button(onClick = { opciones.removeAt(opciones.size - 1) }) {
                                    Text("- Opción")
                                }
                        }
                        Button(
                            onClick = {
                                if (pregunta.isNotBlank() && opciones.all { it.isNotBlank() } && opciones.size in 2..5) {
                                    vm.agregarEncuesta(pregunta, opciones.toList())
                                    pregunta = ""
                                    opciones.clear(); opciones.addAll(listOf("", ""))
                                }
                            },
                            modifier = Modifier.align(Alignment.End).padding(end = 8.dp, bottom = 8.dp)
                        ) {
                            Text("Crear encuesta")
                        }

                        LazyColumn {
                            items(state.encuestas) { encuestaConResultados ->
                                val encuesta = encuestaConResultados.encuesta
                                val opcionesTxt = encuesta.opciones.split("|")
                                val votos = encuestaConResultados.votos
                                val totalVotos = votos.sum().coerceAtLeast(1)
                                var seleccion by remember { mutableStateOf(-1) }

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp, horizontal = 8.dp)
                                ) {
                                    Text(encuesta.pregunta, fontWeight = FontWeight.Bold)
                                    opcionesTxt.forEachIndexed { idx, opcion ->
                                        val porcentaje = (votos.getOrNull(idx) ?: 0) * 100 / totalVotos
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            RadioButton(
                                                selected = seleccion == idx,
                                                onClick = {
                                                    seleccion = idx
                                                    vm.votarEnEncuesta(encuesta.id, idx)
                                                }
                                            )
                                            Text(opcion, modifier = Modifier.weight(1f))
                                            Text("$porcentaje%", fontSize = 12.sp)
                                        }
                                        LinearProgressIndicator(
                                            progress = (votos.getOrNull(idx)?.toFloat() ?: 0f) / totalVotos,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(8.dp)
                                        )
                                    }
                                    Text("Total votos: $totalVotos", fontSize = 12.sp)
                                }
                                Divider()
                            }
                            if (state.encuestas.isEmpty()) {
                                item {
                                    Text(
                                        text = "Sin encuestas",
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier
                                            .padding(vertical = 32.dp)
                                            .fillMaxWidth(),
                                        textAlign = TextAlign.Center
                                    )
                                }
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
