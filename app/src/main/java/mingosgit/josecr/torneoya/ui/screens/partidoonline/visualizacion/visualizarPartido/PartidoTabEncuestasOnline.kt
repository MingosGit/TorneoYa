package mingosgit.josecr.torneoya.ui.screens.partidoonline.visualizacion.visualizarPartido

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.yield
import mingosgit.josecr.torneoya.viewmodel.partidoonline.VisualizarPartidoOnlineViewModel
import mingosgit.josecr.torneoya.viewmodel.partidoonline.EncuestaOnlineConResultadosConAvatar
import mingosgit.josecr.torneoya.R
import mingosgit.josecr.torneoya.ui.theme.mutedText

@OptIn(ExperimentalMaterial3Api::class)
/**
 * Pestaña de encuestas en la visualización online:
 * - Lista encuestas con resultados y votos en tiempo real
 * - Permite votar opción única
 * - Sección para crear una nueva encuesta (pregunta + opciones)
 */
@Composable
fun PartidoTabEncuestasOnline(vm: VisualizarPartidoOnlineViewModel, usuarioUid: String) {
    val cs = MaterialTheme.colorScheme

    val state by vm.comentariosEncuestasState.collectAsState()
    var pregunta by remember { mutableStateOf("") }
    var opciones = remember { mutableStateListOf("", "") }
    var expandedCrear by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(false) }
    val encuestasSize = state.encuestas.size
    val encuestasLoaded = remember(encuestasSize) { encuestasSize > 0 }

    Box(modifier = Modifier.fillMaxSize()) {

        // Botón refrescar encuestas
        IconButton(
            onClick = {
                scope.launch {
                    isLoading = true
                    yield()
                    delay(150)
                    vm.cargarComentariosEncuestas(usuarioUid)
                    isLoading = false
                }
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 12.dp, end = 12.dp)
                .size(38.dp)
                .clip(CircleShape)
                .background(cs.surfaceVariant)
                .border(
                    width = 1.6.dp,
                    brush = Brush.horizontalGradient(listOf(cs.primary, cs.secondary)),
                    shape = CircleShape
                )
                .zIndex(2f)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = stringResource(id = R.string.ponlineenc_desc_refresh_surveys),
                tint = cs.secondary,
                modifier = Modifier.size(22.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {

            // Indicador de carga
            if (isLoading && !encuestasLoaded) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = cs.primary)
                }
            } else {
                // Lista de encuestas
                Box(modifier = Modifier.weight(1f)) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 4.dp),
                        contentPadding = PaddingValues(bottom = 12.dp)
                    ) {
                        items(state.encuestas) { encuestaConResultados: EncuestaOnlineConResultadosConAvatar ->
                            val encuesta = encuestaConResultados.encuesta
                            val opcionesList = encuesta.opciones
                            val votos = encuestaConResultados.votos
                            val totalVotos = votos.sum().coerceAtLeast(1)
                            var seleccionada by remember { mutableStateOf(-1) }

                            // Cargar voto previo del usuario
                            LaunchedEffect(encuesta.uid, usuarioUid) {
                                scope.launch {
                                    val sel = vm.getVotoUsuarioEncuesta(encuesta.uid, usuarioUid)
                                    seleccionada = sel ?: -1
                                }
                            }

                            // Tarjeta de encuesta
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 9.dp, horizontal = 5.dp)
                                    .shadow(6.dp, RoundedCornerShape(17.dp)),
                                shape = RoundedCornerShape(17.dp),
                                colors = CardDefaults.cardColors(containerColor = cs.surfaceVariant)
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 15.dp, horizontal = 15.dp)
                                ) {
                                    // Cabecera con avatar, pregunta y contador de votos
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        AvatarComentario(
                                            avatar = encuestaConResultados.avatar,
                                            nombre = encuesta.creadorNombre,
                                            background = Brush.horizontalGradient(
                                                listOf(cs.secondary, cs.primary)
                                            )
                                        )
                                        Spacer(Modifier.width(10.dp))
                                        Text(
                                            encuesta.pregunta,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 17.sp,
                                            color = cs.onSurface,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(7.dp))
                                                .background(cs.surface)
                                                .padding(horizontal = 11.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = stringResource(id = R.string.ponlineenc_text_votes, totalVotos),
                                                color = cs.tertiary,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(14.dp))
                                    // Opciones de la encuesta
                                    Column {
                                        opcionesList.forEachIndexed { idx, opcion ->
                                            val porcentaje = (votos.getOrNull(idx) ?: 0) * 100 / totalVotos
                                            val seleccionado = seleccionada == idx
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(bottom = 9.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .border(
                                                        width = 2.dp,
                                                        brush = if (seleccionado)
                                                            Brush.horizontalGradient(listOf(cs.secondary, cs.tertiary))
                                                        else Brush.horizontalGradient(listOf(cs.surfaceVariant, cs.surfaceVariant)),
                                                        shape = RoundedCornerShape(10.dp)
                                                    )
                                                    .background(cs.surfaceVariant)
                                                    .clickable {
                                                        if (seleccionada != idx) {
                                                            vm.votarUnicoEnEncuesta(encuesta.uid, idx, usuarioUid)
                                                            seleccionada = idx
                                                        }
                                                    }
                                                    .padding(vertical = 6.dp, horizontal = 11.dp)
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    RadioButton(
                                                        selected = seleccionado,
                                                        onClick = {
                                                            if (!seleccionado) {
                                                                vm.votarUnicoEnEncuesta(encuesta.uid, idx, usuarioUid)
                                                                seleccionada = idx
                                                            }
                                                        },
                                                        colors = RadioButtonDefaults.colors(
                                                            selectedColor = cs.secondary,
                                                            unselectedColor = cs.outline
                                                        ),
                                                        modifier = Modifier.size(22.dp)
                                                    )
                                                    Text(
                                                        opcion,
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .padding(start = 3.dp),
                                                        color = cs.onSurface,
                                                        fontSize = 16.sp,
                                                        fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Normal
                                                    )
                                                    Spacer(Modifier.width(10.dp))
                                                    Text(
                                                        "$porcentaje%",
                                                        color = cs.tertiary,
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                                // Barra de progreso con votos
                                                LinearProgressIndicator(
                                                    progress = (votos.getOrNull(idx)?.toFloat() ?: 0f) / totalVotos,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(7.dp)
                                                        .align(Alignment.BottomStart)
                                                        .padding(top = 5.dp)
                                                        .clip(RoundedCornerShape(6.dp)),
                                                    color = if (seleccionado) cs.secondary else cs.primary,
                                                    trackColor = cs.surface
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        // Texto si no hay encuestas
                        if (state.encuestas.isEmpty()) {
                            item {
                                Text(
                                    text = stringResource(id = R.string.ponlineenc_text_no_surveys),
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

                // Sección crear encuesta
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .animateContentSize(),
                    colors = CardDefaults.cardColors(containerColor = cs.surfaceVariant),
                    onClick = { expandedCrear = !expandedCrear }
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                stringResource(id = R.string.ponlineenc_label_create_survey),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = cs.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { expandedCrear = !expandedCrear }) {
                                Icon(
                                    imageVector = if (expandedCrear) Icons.Default.Close else Icons.Default.Add,
                                    contentDescription = stringResource(id = R.string.ponlineenc_desc_expand_collapse),
                                    tint = cs.secondary
                                )
                            }
                        }
                        if (expandedCrear) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                // Campo pregunta
                                OutlinedTextField(
                                    value = pregunta,
                                    onValueChange = { pregunta = it },
                                    label = { Text(stringResource(id = R.string.ponlineenc_label_survey_question)) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = cs.mutedText,
                                        unfocusedBorderColor = cs.mutedText,
                                        disabledBorderColor = cs.mutedText,
                                        errorBorderColor = MaterialTheme.colorScheme.error,
                                        focusedLabelColor = cs.mutedText,
                                        unfocusedLabelColor = cs.mutedText
                                    )
                                )
                                // Campos de opciones
                                opciones.forEachIndexed { idx, valor ->
                                    OutlinedTextField(
                                        value = valor,
                                        onValueChange = { opciones[idx] = it },
                                        label = { Text(stringResource(id = R.string.ponlineenc_label_option, idx + 1)) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 2.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = cs.mutedText,
                                            unfocusedBorderColor = cs.mutedText,
                                            disabledBorderColor = cs.mutedText,
                                            errorBorderColor = MaterialTheme.colorScheme.error,
                                            focusedLabelColor = cs.mutedText,
                                            unfocusedLabelColor = cs.mutedText
                                        )
                                    )
                                }
                                // Botones añadir/eliminar opción
                                Row(
                                    modifier = Modifier
                                        .padding(top = 10.dp)
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    if (opciones.size < 5)
                                        OutlinedColorButton(
                                            text = stringResource(id = R.string.ponlineenc_button_add_option),
                                            borderBrush = Brush.horizontalGradient(
                                                listOf(cs.secondary, cs.primary)
                                            ),
                                            onClick = { opciones.add("") }
                                        )
                                    if (opciones.size > 2)
                                        OutlinedColorButton(
                                            text = stringResource(id = R.string.ponlineenc_button_remove_option),
                                            borderBrush = Brush.horizontalGradient(
                                                listOf(cs.error, cs.tertiary)
                                            ),
                                            onClick = { opciones.removeAt(opciones.size - 1) }
                                        )
                                }
                                // Botón lanzar encuesta
                                OutlinedColorButton(
                                    text = stringResource(id = R.string.ponlineenc_button_launch_survey),
                                    borderBrush = Brush.horizontalGradient(
                                        listOf(cs.tertiary, cs.secondary)
                                    ),
                                    modifier = Modifier
                                        .align(Alignment.End)
                                        .padding(top = 16.dp, end = 2.dp),
                                    onClick = {
                                        if (
                                            pregunta.isNotBlank() &&
                                            opciones.all { it.isNotBlank() } &&
                                            opciones.size in 2..5
                                        ) {
                                            scope.launch {
                                                vm.agregarEncuesta(
                                                    pregunta = pregunta,
                                                    opciones = opciones.toList(),
                                                    usuarioUid = usuarioUid
                                                )
                                                pregunta = ""
                                                opciones.clear(); opciones.addAll(listOf("", ""))
                                                expandedCrear = false
                                                isLoading = true
                                                yield()
                                                delay(150)
                                                vm.cargarComentariosEncuestas(usuarioUid)
                                                isLoading = false
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Botón con borde de color personalizado para acciones de encuesta
 */
@Composable
fun OutlinedColorButton(
    text: String,
    borderBrush: Brush,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val cs = MaterialTheme.colorScheme

    Box(
        modifier = modifier
            .defaultMinSize(minHeight = 38.dp)
            .heightIn(min = 38.dp, max = 45.dp)
            .padding(2.dp)
            .clip(RoundedCornerShape(13.dp))
            .border(
                width = 2.dp,
                brush = borderBrush,
                shape = RoundedCornerShape(13.dp)
            )
            .background(Color.Transparent)
            .wrapContentSize(Alignment.Center)
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 7.dp)
    ) {
        Text(
            text = text,
            color = cs.secondary,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
    }
}
