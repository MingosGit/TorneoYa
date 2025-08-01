package mingosgit.josecr.torneoya.ui.screens.partido.visualizarpartidoscreen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mingosgit.josecr.torneoya.viewmodel.partido.VisualizarPartidoViewModel

@Composable
fun PartidoTabComentarios(vm: VisualizarPartidoViewModel, usuarioId: Long) {
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
                    // Recargar comentarios con usuarioId para mostrar el voto actualizado
                    vm.cargarComentariosEncuestas(usuarioId)
                }
            })
        )
        Button(
            onClick = {
                if (textoComentario.isNotBlank()) {
                    vm.agregarComentario(usuarioNombre, textoComentario)
                    textoComentario = ""
                    vm.cargarComentariosEncuestas(usuarioId)
                }
            },
            modifier = Modifier.align(Alignment.End).padding(end = 8.dp)
        ) {
            Text("Enviar")
        }
        LazyColumn(modifier = Modifier.fillMaxHeight()) {
            items(state.comentarios) { comentarioConVotos ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp, horizontal = 8.dp)
                ) {
                    Text(
                        text = comentarioConVotos.comentario.usuarioNombre,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = comentarioConVotos.comentario.texto,
                        fontSize = 16.sp
                    )
                    Text(
                        text = comentarioConVotos.comentario.fechaHora,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        IconButton(
                            onClick = {
                                if (comentarioConVotos.miVoto != 1) {
                                    vm.votarComentario(comentarioConVotos.comentario.id, usuarioId, 1)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ThumbUp,
                                contentDescription = "Like",
                                tint = if (comentarioConVotos.miVoto == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(text = comentarioConVotos.likes.toString())
                        Spacer(modifier = Modifier.width(16.dp))
                        IconButton(
                            onClick = {
                                if (comentarioConVotos.miVoto != -1) {
                                    vm.votarComentario(comentarioConVotos.comentario.id, usuarioId, -1)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ThumbDown,
                                contentDescription = "Dislike",
                                tint = if (comentarioConVotos.miVoto == -1) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(text = comentarioConVotos.dislikes.toString())
                    }
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
