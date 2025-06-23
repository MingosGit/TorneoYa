package mingosgit.josecr.torneoya.ui.screens.partido.visualizarpartidoscreen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mingosgit.josecr.torneoya.viewmodel.partido.VisualizarPartidoViewModel

@Composable
fun PartidoTabEncuestas(vm: VisualizarPartidoViewModel) {
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
