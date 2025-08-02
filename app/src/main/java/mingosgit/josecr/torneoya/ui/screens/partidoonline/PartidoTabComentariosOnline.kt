package mingosgit.josecr.torneoya.ui.screens.partidoonline

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
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
import kotlinx.coroutines.launch
import mingosgit.josecr.torneoya.viewmodel.partidoonline.VisualizarPartidoOnlineViewModel

@Composable
fun PartidoTabComentariosOnline(vm: VisualizarPartidoOnlineViewModel, usuarioUid: String) {
    val state by vm.comentariosEncuestasState.collectAsState()
    var textoComentario by remember { mutableStateOf("") }
    val usuarioNombre = "Tú"
    var isLoading by remember { mutableStateOf(false) }
    val comentariosSize = state.comentarios.size
    val comentariosLoaded = remember(comentariosSize) { comentariosSize > 0 }
    val scope = rememberCoroutineScope()

    // PRIMERA CARGA
    LaunchedEffect(Unit) {
        isLoading = true
        scope.launch {
            vm.cargarComentariosEncuestas(usuarioUid)
            isLoading = false
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = textoComentario,
                onValueChange = { textoComentario = it },
                label = { Text("Escribe un comentario") },
                singleLine = false,
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (textoComentario.isNotBlank()) {
                        scope.launch {
                            isLoading = true
                            vm.agregarComentario(usuarioNombre, textoComentario, usuarioUid)
                            textoComentario = ""
                            vm.cargarComentariosEncuestas(usuarioUid)
                            isLoading = false
                        }
                    }
                })
            )
            IconButton(
                onClick = {
                    scope.launch {
                        isLoading = true
                        vm.cargarComentariosEncuestas(usuarioUid)
                        isLoading = false
                    }
                },
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refrescar comentarios"
                )
            }
        }
        Button(
            onClick = {
                if (textoComentario.isNotBlank()) {
                    scope.launch {
                        isLoading = true
                        vm.agregarComentario(usuarioNombre, textoComentario, usuarioUid)
                        textoComentario = ""
                        vm.cargarComentariosEncuestas(usuarioUid)
                        isLoading = false
                    }
                }
            },
            modifier = Modifier.align(Alignment.End).padding(end = 8.dp)
        ) {
            Text("Enviar")
        }

        if (isLoading && !comentariosLoaded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            // ORDENAR DE MÁS NUEVO A MÁS VIEJO
            val comentariosOrdenados = state.comentarios.sortedByDescending { it.comentario.fechaHora }

            LazyColumn(modifier = Modifier.fillMaxHeight()) {
                items(comentariosOrdenados) { comentarioConVotos ->
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
                                        vm.votarComentario(comentarioConVotos.comentario.uid, usuarioUid, 1)
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
                                        vm.votarComentario(comentarioConVotos.comentario.uid, usuarioUid, -1)
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
                if (comentariosOrdenados.isEmpty()) {
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
}
