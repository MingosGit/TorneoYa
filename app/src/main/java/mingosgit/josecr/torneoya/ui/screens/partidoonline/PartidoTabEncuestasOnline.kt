package mingosgit.josecr.torneoya.ui.screens.partidoonline

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import mingosgit.josecr.torneoya.viewmodel.partidoonline.VisualizarPartidoOnlineViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartidoTabEncuestasOnline(vm: VisualizarPartidoOnlineViewModel, usuarioUid: String) {
    val state by vm.comentariosEncuestasState.collectAsState()
    var pregunta by remember { mutableStateOf("") }
    var opciones = remember { mutableStateListOf("", "") }
    var expandedCrear by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(false) }
    val encuestasSize = state.encuestas.size
    val encuestasLoaded = remember(encuestasSize) { encuestasSize > 0 }

    LaunchedEffect(Unit) {
        isLoading = true
        vm.cargarComentariosEncuestas(usuarioUid)
        isLoading = false
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(
                onClick = {
                    isLoading = true
                    vm.cargarComentariosEncuestas(usuarioUid)
                    isLoading = false
                },
                modifier = Modifier.padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refrescar encuestas"
                )
            }
        }

        if (isLoading && !encuestasLoaded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            // Encuestas existentes
            Box(modifier = Modifier.weight(1f)) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(state.encuestas) { encuestaConResultados ->
                        val encuesta = encuestaConResultados.encuesta
                        val opcionesList = encuesta.opciones
                        val votos = encuestaConResultados.votos
                        val totalVotos = votos.sum().coerceAtLeast(1)
                        var seleccionada by remember { mutableStateOf(-1) }
                        LaunchedEffect(encuesta.uid, usuarioUid) {
                            scope.launch {
                                val sel = vm.getVotoUsuarioEncuesta(encuesta.uid, usuarioUid)
                                seleccionada = sel ?: -1
                            }
                        }
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp, horizontal = 8.dp)
                        ) {
                            Text(encuesta.pregunta, fontWeight = FontWeight.Bold)
                            opcionesList.forEachIndexed { idx, opcion ->
                                val porcentaje = (votos.getOrNull(idx) ?: 0) * 100 / totalVotos
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    RadioButton(
                                        selected = seleccionada == idx,
                                        onClick = {
                                            if (seleccionada != idx) {
                                                vm.votarUnicoEnEncuesta(encuesta.uid, idx, usuarioUid)
                                                seleccionada = idx
                                            }
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
            // Card para crear encuesta
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                onClick = { expandedCrear = !expandedCrear }
            ) {
                Column(
                    modifier = Modifier
                        .padding(12.dp)
                        .animateContentSize()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Crear nueva encuesta",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { expandedCrear = !expandedCrear }) {
                            Icon(
                                imageVector = if (expandedCrear) Icons.Default.Close else Icons.Default.Add,
                                contentDescription = "Expandir/Colapsar"
                            )
                        }
                    }
                    if (expandedCrear) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = pregunta,
                                onValueChange = { pregunta = it },
                                label = { Text("Pregunta de la encuesta") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            )
                            opciones.forEachIndexed { idx, valor ->
                                OutlinedTextField(
                                    value = valor,
                                    onValueChange = { opciones[idx] = it },
                                    label = { Text("Opción ${idx + 1}") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp)
                                )
                            }
                            Row(modifier = Modifier.padding(top = 8.dp)) {
                                if (opciones.size < 5)
                                    Button(
                                        onClick = { opciones.add("") },
                                        modifier = Modifier.padding(end = 8.dp)
                                    ) {
                                        Text("+ Opción")
                                    }
                                if (opciones.size > 2)
                                    Button(onClick = { opciones.removeAt(opciones.size - 1) }) {
                                        Text("- Opción")
                                    }
                            }
                            Button(
                                onClick = {
                                    if (
                                        pregunta.isNotBlank() &&
                                        opciones.all { it.isNotBlank() } &&
                                        opciones.size in 2..5
                                    ) {
                                        vm.agregarEncuesta(pregunta, opciones)
                                        pregunta = ""
                                        opciones.clear(); opciones.addAll(listOf("", ""))
                                        expandedCrear = false
                                    }
                                },
                                modifier = Modifier
                                    .align(Alignment.End)
                                    .padding(top = 8.dp)
                            ) {
                                Text("Crear encuesta")
                            }
                        }
                    }
                }
            }
        }
    }
}
