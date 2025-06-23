package mingosgit.josecr.torneoya.ui.screens.partido.visualizarpartidoscreen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import kotlinx.coroutines.runBlocking
import androidx.compose.material.icons.filled.Add
import mingosgit.josecr.torneoya.viewmodel.partido.VisualizarPartidoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartidoTabEncuestas(vm: VisualizarPartidoViewModel, usuarioId: Long) {
    val state by vm.comentariosEncuestasState.collectAsState()
    val uiState by vm.uiState.collectAsState()
    var pregunta by remember { mutableStateOf("") }
    var opciones = remember { mutableStateListOf<String?>("", "") }
    var seleccionados by remember { mutableStateOf(setOf<String>()) }
    var expandedCrear by remember { mutableStateOf(false) }
    val jugadores = (uiState.jugadoresEquipoA + uiState.jugadoresEquipoB).distinct()

    Column(modifier = Modifier.fillMaxSize()) {
        // Área de visualización de encuestas (ocupa casi todo el espacio)
        Box(modifier = Modifier.weight(1f)) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(state.encuestas) { encuestaConResultados ->
                    val encuesta = encuestaConResultados.encuesta
                    val opcionesTxt = encuesta.opciones.split("|")
                    val votos = encuestaConResultados.votos
                    val totalVotos = votos.sum().coerceAtLeast(1)
                    var seleccion by remember {
                        mutableStateOf(
                            runBlocking { vm.getVotoUsuarioEncuesta(encuesta.id, usuarioId) }
                        )
                    }
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
                                        if (seleccion != idx) {
                                            vm.votarUnicoEnEncuesta(encuesta.id, idx, usuarioId)
                                            seleccion = idx
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

        // Card expandible para crear encuesta (ocupa poco espacio cuando está colapsada)
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
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 280.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        OutlinedTextField(
                            value = pregunta,
                            onValueChange = { pregunta = it },
                            label = { Text("Pregunta de la encuesta") },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 8.dp)
                        )
                        opciones.forEachIndexed { idx, valor ->
                            val opcionesFiltradas = jugadores.filter { j -> j == valor || !seleccionados.contains(j) }
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                                var expanded by remember { mutableStateOf(false) }
                                ExposedDropdownMenuBox(
                                    expanded = expanded,
                                    onExpandedChange = { expanded = !expanded },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    OutlinedTextField(
                                        value = valor ?: "",
                                        onValueChange = {},
                                        label = { Text("Opción ${idx + 1}") },
                                        readOnly = true,
                                        modifier = Modifier.menuAnchor().fillMaxWidth().clickable { expanded = true }
                                    )
                                    ExposedDropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false }
                                    ) {
                                        opcionesFiltradas.forEach { jugador ->
                                            DropdownMenuItem(
                                                text = { Text(jugador) },
                                                onClick = {
                                                    val anterior = opciones[idx]
                                                    if (!anterior.isNullOrEmpty()) {
                                                        seleccionados = seleccionados - anterior
                                                    }
                                                    opciones[idx] = jugador
                                                    seleccionados = seleccionados + jugador
                                                    expanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                                if (valor?.isNotBlank() == true) {
                                    IconButton(onClick = {
                                        seleccionados = seleccionados - (opciones[idx] ?: "")
                                        opciones[idx] = ""
                                    }) {
                                        Icon(Icons.Default.Close, contentDescription = "Eliminar selección")
                                    }
                                }
                            }
                        }
                        Row(modifier = Modifier.padding(top = 8.dp)) {
                            if (opciones.size < 5)
                                Button(onClick = { opciones.add(""); }, modifier = Modifier.padding(end = 8.dp)) {
                                    Text("+ Opción")
                                }
                            if (opciones.size > 2)
                                Button(onClick = {
                                    val last = opciones.last()
                                    if (!last.isNullOrEmpty()) seleccionados = seleccionados - last!!
                                    opciones.removeAt(opciones.size - 1)
                                }) {
                                    Text("- Opción")
                                }
                        }
                        Button(
                            onClick = {
                                if (
                                    pregunta.isNotBlank() &&
                                    opciones.all { !it.isNullOrBlank() } &&
                                    opciones.size in 2..5
                                ) {
                                    vm.agregarEncuesta(pregunta, opciones.filterNotNull())
                                    pregunta = ""
                                    seleccionados = setOf()
                                    opciones.clear(); opciones.addAll(listOf("", ""))
                                    expandedCrear = false
                                }
                            },
                            modifier = Modifier.align(Alignment.End).padding(top = 8.dp)
                        ) {
                            Text("Crear encuesta")
                        }
                    }
                }

            }
        }
    }
}
