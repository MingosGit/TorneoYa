package mingosgit.josecr.torneoya.ui.screens.usuario.ajustes

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.runBlocking
import mingosgit.josecr.torneoya.viewmodel.usuario.AdministrarPartidosViewModel
import mingosgit.josecr.torneoya.data.database.AppDatabase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartidosListaBusquedaScreen(
    viewModel: AdministrarPartidosViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val equipoDao = db.equipoDao()
    val partidos by viewModel.partidos.collectAsState()
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }

    val equipoNombres = remember { mutableStateMapOf<Long, String>() }

    fun getNombreEquipo(equipoId: Long): String {
        if (equipoId == -1L) return "Equipo desconocido"
        equipoNombres[equipoId]?.let { return it }
        val nombre = runBlocking { equipoDao.getNombreById(equipoId) ?: "Equipo #$equipoId" }
        equipoNombres[equipoId] = nombre
        return nombre
    }

    LaunchedEffect(Unit) {
        viewModel.cargarPartidos()
    }

    // Paleta moderna: dark mode friendly, azul intenso + gris oscuro, acentos violetas, todo contrastado y con elegancia.
    val modernBackground = Brush.verticalGradient(
        0.0f to Color(0xFF181B26),
        0.25f to Color(0xFF22263B),
        0.6f to Color(0xFF1A1E29),
        1.0f to Color(0xFF161622)
    )
    val cardShape = RoundedCornerShape(19.dp)
    val fieldShape = RoundedCornerShape(13.dp)
    val blue = Color(0xFF296DFF)
    val violet = Color(0xFF8F5CFF)
    val accent = Color(0xFFFFB531)
    val lightText = Color(0xFFF7F7FF)
    val mutedText = Color(0xFFB7B7D1)
    val chipBg = Color(0xFF24294A)
    val shadowColor = Color(0x66152C4A)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(modernBackground)
    ) {
        Spacer(Modifier.height(22.dp))
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                viewModel.setBusqueda(it.text)
            },
            label = { Text("Buscar por fecha o ID", fontSize = 16.sp, color = mutedText) },
            singleLine = true,
            shape = fieldShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = lightText,
                unfocusedTextColor = lightText,
                focusedBorderColor = blue,
                unfocusedBorderColor = blue.copy(alpha = 0.22f),
                cursorColor = blue,
                focusedLabelColor = blue,
                unfocusedLabelColor = mutedText,
                focusedContainerColor = Color(0xFF191B27),
                unfocusedContainerColor = Color(0xFF191B27)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .shadow(2.dp, fieldShape)
        )
        Spacer(Modifier.height(4.dp))

        if (partidos.isEmpty()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(36.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.SportsSoccer,
                        contentDescription = null,
                        tint = blue,
                        modifier = Modifier.size(52.dp)
                    )
                    Spacer(Modifier.height(14.dp))
                    Text(
                        "No hay partidos",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 18.sp,
                            color = mutedText,
                            letterSpacing = 0.1.sp
                        )
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                items(partidos) { partido ->
                    var pressed by remember { mutableStateOf(false) }
                    val animatedCardColor by animateColorAsState(
                        if (pressed)
                            Color(0xFF222B49)
                        else
                            Color(0xFF21243B),
                        label = ""
                    )
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp)
                            .shadow(
                                elevation = if (pressed) 18.dp else 10.dp,
                                shape = cardShape,
                                ambientColor = shadowColor,
                                spotColor = shadowColor
                            )
                            .clip(cardShape)
                            .background(animatedCardColor)
                            .clickable(
                                onClick = {
                                    navController.navigate("administrar_partido_goles/${partido.id}")
                                },
                                onClickLabel = "Administrar partido"
                            )
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = {
                                        pressed = true
                                        tryAwaitRelease()
                                        pressed = false
                                    }
                                )
                            }
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 20.dp, vertical = 18.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // Avatar A
                                Box(
                                    Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Brush.linearGradient(listOf(blue, violet))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        getNombreEquipo(partido.equipoAId).take(2).uppercase(),
                                        color = Color.White,
                                        fontSize = 19.sp,
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    getNombreEquipo(partido.equipoAId),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontSize = 16.sp,
                                        color = lightText,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1.1f)
                                )
                                Text(
                                    "vs",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontSize = 14.sp,
                                        color = violet,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    modifier = Modifier.padding(horizontal = 10.dp)
                                )
                                Text(
                                    getNombreEquipo(partido.equipoBId),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontSize = 16.sp,
                                        color = lightText,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1.1f)
                                )
                                Spacer(Modifier.width(12.dp))
                                // Avatar B
                                Box(
                                    Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Brush.linearGradient(listOf(violet, blue))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        getNombreEquipo(partido.equipoBId).take(2).uppercase(),
                                        color = Color.White,
                                        fontSize = 19.sp,
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                }
                            }
                            Spacer(Modifier.height(11.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    "Fecha: ${partido.fecha}",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 13.5.sp,
                                        color = mutedText
                                    ),
                                    modifier = Modifier.weight(1.1f)
                                )
                                Box(
                                    Modifier
                                        .padding(horizontal = 7.dp)
                                        .background(chipBg, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 13.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        "Goles: ${partido.golesEquipoA} - ${partido.golesEquipoB}",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = accent
                                        )
                                    )
                                }
                                Text(
                                    "ID: ${partido.id}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.5.sp,
                                        color = mutedText
                                    ),
                                    modifier = Modifier.padding(start = 10.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
