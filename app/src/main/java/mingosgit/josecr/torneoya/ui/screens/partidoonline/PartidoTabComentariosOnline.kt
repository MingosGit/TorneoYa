package mingosgit.josecr.torneoya.ui.screens.partidoonline

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import mingosgit.josecr.torneoya.ui.theme.TorneoYaPalette
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
    val listState = rememberLazyListState()

    // PRIMERA CARGA
    LaunchedEffect(Unit) {
        isLoading = true
        scope.launch {
            vm.cargarComentariosEncuestas(usuarioUid)
            isLoading = false
        }
    }

    // AUTO SCROLL AL AGREGAR NUEVO COMENTARIO
    val oldComentariosSize = remember { mutableStateOf(0) }
    LaunchedEffect(comentariosSize) {
        if (comentariosSize > oldComentariosSize.value) {
            // Scroll al último comentario (el más nuevo está arriba si sortedByDescending, abajo si sortedBy)
            listState.animateScrollToItem(0)
        }
        oldComentariosSize.value = comentariosSize
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Input, Recargar y Enviar, todos en el mismo Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp, top = 12.dp, bottom = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = textoComentario,
                onValueChange = { textoComentario = it },
                label = { Text("Escribe un comentario") },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 6.dp)
                    .defaultMinSize(minHeight = 48.dp),
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
                modifier = Modifier
                    .size(42.dp)
                    .padding(horizontal = 3.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refrescar comentarios",
                    tint = TorneoYaPalette.blue
                )
            }
            // BOTÓN ENVIAR OUTLINED MODERNO
            OutlinedIconSendButton(
                enabled = textoComentario.isNotBlank() && !isLoading,
                onClick = {
                    scope.launch {
                        isLoading = true
                        vm.agregarComentario(usuarioNombre, textoComentario, usuarioUid)
                        textoComentario = ""
                        vm.cargarComentariosEncuestas(usuarioUid)
                        isLoading = false
                    }
                }
            )
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
            val comentariosOrdenados = state.comentarios.sortedByDescending { it.comentario.fechaHora }

            LazyColumn(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(bottom = 8.dp),
                contentPadding = PaddingValues(bottom = 20.dp),
                state = listState,
                reverseLayout = false
            ) {
                items(comentariosOrdenados) { comentarioConVotos ->
                    val votoColor = when {
                        comentarioConVotos.miVoto == 1 -> TorneoYaPalette.blue
                        comentarioConVotos.miVoto == -1 -> Color(0xFFFA6767)
                        else -> Color(0xFF23273D)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 7.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        // Avatar circular con inicial
                        Box(
                            modifier = Modifier
                                .size(39.dp)
                                .clip(CircleShape)
                                .background(
                                    brush = Brush.verticalGradient(
                                        listOf(
                                            votoColor.copy(alpha = 0.88f),
                                            TorneoYaPalette.violet.copy(alpha = 0.80f)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = comentarioConVotos.comentario.usuarioNombre.take(1).uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(3.dp, RoundedCornerShape(17.dp)),
                            shape = RoundedCornerShape(17.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF20243B)),
                            border = BorderStroke(
                                2.dp,
                                Brush.horizontalGradient(
                                    listOf(votoColor, TorneoYaPalette.violet, Color(0xFF20243B))
                                )
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(start = 11.dp, end = 10.dp, top = 10.dp, bottom = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = comentarioConVotos.comentario.usuarioNombre,
                                        fontWeight = FontWeight.Bold,
                                        color = TorneoYaPalette.accent,
                                        fontSize = 15.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = comentarioConVotos.comentario.fechaHora,
                                        fontSize = 11.sp,
                                        color = TorneoYaPalette.mutedText,
                                        textAlign = TextAlign.End
                                    )
                                }
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = comentarioConVotos.comentario.texto,
                                    fontSize = 16.sp,
                                    color = Color.White,
                                    modifier = Modifier.padding(top = 1.dp, bottom = 3.dp)
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .padding(top = 2.dp, bottom = 3.dp)
                                ) {
                                    IconButton(
                                        onClick = {
                                            if (comentarioConVotos.miVoto != 1) {
                                                vm.votarComentario(
                                                    comentarioConVotos.comentario.uid,
                                                    usuarioUid,
                                                    1
                                                )
                                            }
                                        },
                                        modifier = Modifier.size(29.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.ThumbUp,
                                            contentDescription = "Like",
                                            tint = if (comentarioConVotos.miVoto == 1)
                                                TorneoYaPalette.blue
                                            else
                                                TorneoYaPalette.mutedText,
                                            modifier = Modifier.size(19.dp)
                                        )
                                    }
                                    Text(
                                        text = comentarioConVotos.likes.toString(),
                                        color = if (comentarioConVotos.miVoto == 1)
                                            TorneoYaPalette.blue
                                        else
                                            TorneoYaPalette.mutedText,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    IconButton(
                                        onClick = {
                                            if (comentarioConVotos.miVoto != -1) {
                                                vm.votarComentario(
                                                    comentarioConVotos.comentario.uid,
                                                    usuarioUid,
                                                    -1
                                                )
                                            }
                                        },
                                        modifier = Modifier.size(29.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.ThumbDown,
                                            contentDescription = "Dislike",
                                            tint = if (comentarioConVotos.miVoto == -1)
                                                Color(0xFFFA6767)
                                            else
                                                TorneoYaPalette.mutedText,
                                            modifier = Modifier.size(19.dp)
                                        )
                                    }
                                    Text(
                                        text = comentarioConVotos.dislikes.toString(),
                                        color = if (comentarioConVotos.miVoto == -1)
                                            Color(0xFFFA6767)
                                        else
                                            TorneoYaPalette.mutedText,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
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

@Composable
fun OutlinedIconSendButton(
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .defaultMinSize(minHeight = 38.dp)
            .height(42.dp)
            .padding(start = 3.dp)
            .border(
                width = 2.dp,
                brush = Brush.horizontalGradient(listOf(TorneoYaPalette.blue, TorneoYaPalette.violet)),
                shape = RoundedCornerShape(13.dp)
            )
            .background(Color.Transparent)
            .wrapContentSize(Alignment.Center)
            .clip(RoundedCornerShape(13.dp))
    ) {
        IconButton(
            enabled = enabled,
            onClick = onClick,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Send,
                contentDescription = "Enviar",
                tint = if (enabled) TorneoYaPalette.violet else TorneoYaPalette.mutedText,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
