package mingosgit.josecr.torneoya.ui.screens.home

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import mingosgit.josecr.torneoya.data.firebase.NotificacionFirebase
import mingosgit.josecr.torneoya.ui.theme.TorneoYaPalette
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NotificacionesScreen(
    usuarioUid: String
) {
    val context = LocalContext.current
    val app = context.applicationContext as Application

    val viewModel: NotificacionesViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return NotificacionesViewModel(usuarioUid, app) as T
            }
        }
    )

    val noLeidas by viewModel.noLeidas.collectAsState()
    val leidas by viewModel.leidas.collectAsState()
    val cargando by viewModel.cargando.collectAsState()
    var mostrarLeidas by remember { mutableStateOf(false) }

    var borrarUid by remember { mutableStateOf<String?>(null) }
    var borrarTitulo by remember { mutableStateOf<String?>(null) }

    val background = Brush.verticalGradient(
        0.0f to Color(0xFF1B1D29),
        0.28f to Color(0xFF212442),
        0.58f to Color(0xFF191A23),
        1.0f to Color(0xFF14151B)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .padding(horizontal = 18.dp, vertical = 18.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (!mostrarLeidas) "Notificaciones" else "Leídas",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 27.sp,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!mostrarLeidas) {
                        TextButton(
                            onClick = { mostrarLeidas = true }
                        ) {
                            Text("Ver leídas", color = Color(0xFF8F5CFF))
                        }
                    } else {
                        TextButton(
                            onClick = { mostrarLeidas = false }
                        ) {
                            Text("No leídas", color = Color(0xFF8F5CFF))
                        }
                    }
                }
            }
            Box(modifier = Modifier.weight(1f)) {
                when {
                    cargando -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                color = Color(0xFF8F5CFF),
                                strokeWidth = 2.3.dp,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }
                    (!mostrarLeidas && noLeidas.isEmpty()) || (mostrarLeidas && leidas.isEmpty()) -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(RoundedCornerShape(24.dp))
                                        .border(
                                            width = 2.5.dp,
                                            brush = Brush.horizontalGradient(
                                                listOf(Color(0xFF296DFF), TorneoYaPalette.violet)
                                            ),
                                            shape = RoundedCornerShape(24.dp)
                                        )
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(Color(0xFF23273D), Color(0xFF1C1D25))
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.MarkEmailRead,
                                        contentDescription = "Sin notificaciones",
                                        tint = Color(0xFF8F5CFF),
                                        modifier = Modifier.size(40.dp)
                                    )
                                }
                                Spacer(Modifier.height(18.dp))
                                Text(
                                    text = if (!mostrarLeidas) "Sin notificaciones" else "Sin leídas",
                                    color = Color(0xFF8F5CFF),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 21.sp
                                )
                                Spacer(Modifier.height(7.dp))
                                Text(
                                    text = if (!mostrarLeidas)
                                        "¡Estás al día! Cuando tengas novedades,\naparecerán aquí."
                                    else
                                        "No hay notificaciones leídas por mostrar.",
                                    color = Color(0xFFB7B7D1),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 20.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp)
                                )
                            }
                        }
                    }
                    else -> {
                        val lista = if (!mostrarLeidas) noLeidas else leidas
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            lista.forEach { noti ->
                                NotificacionCard(
                                    noti = noti,
                                    onArchivar = {
                                        viewModel.archivarNotificacion(noti.uid)
                                    },
                                    onBorrar = if (mostrarLeidas && noti.tipo != "infraccion") {
                                        {
                                            borrarUid = noti.uid
                                            borrarTitulo = noti.titulo
                                        }
                                    } else null
                                )
                            }
                        }
                    }
                }
            }
        }

        // Popup custom con botones gradiente
        if (borrarUid != null && borrarTitulo != null) {
            CustomGradientDialog(
                title = "Eliminar notificación",
                message = "¿Seguro que quieres eliminar \"${borrarTitulo}\"? Esta acción no se puede deshacer.",
                confirmText = "Eliminar",
                dismissText = "Cancelar",
                onConfirm = {
                    viewModel.borrarNotificacion(borrarUid!!)
                    borrarUid = null
                    borrarTitulo = null
                },
                onDismiss = {
                    borrarUid = null
                    borrarTitulo = null
                }
            )
        }
    }
}

@Composable
fun CustomGradientDialog(
    title: String,
    message: String,
    confirmText: String,
    dismissText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        AlertDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                GradientButton(
                    text = confirmText,
                    gradient = Brush.horizontalGradient(
                        listOf(Color(0xFFFF2E63), Color(0xFF8F5CFF))
                    ),
                    textColor = Color.White,
                    onClick = onConfirm
                )
            },
            dismissButton = {
                GradientButton(
                    text = dismissText,
                    gradient = Brush.horizontalGradient(
                        listOf(Color(0xFF296DFF), Color(0xFF8F5CFF))
                    ),
                    textColor = Color.White,
                    onClick = onDismiss
                )
            },
            title = {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.White
                )
            },
            text = {
                Text(
                    text = message,
                    color = Color(0xFFB7B7D1),
                    fontSize = 16.sp
                )
            },
            modifier = Modifier
                .padding(24.dp)
                .clip(RoundedCornerShape(22.dp))
                .border(
                    width = 3.dp,
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFF296DFF), Color(0xFF8F5CFF))
                    ),
                    shape = RoundedCornerShape(22.dp)
                )
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1C2130),
                            Color(0xFF191A23)
                        )
                    )
                )
        )
    }
}

@Composable
fun GradientButton(
    text: String,
    gradient: Brush,
    textColor: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = textColor
        ),
        contentPadding = PaddingValues(),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .defaultMinSize(minHeight = 38.dp)
            .height(38.dp)
            .border(
                width = 2.2.dp,
                brush = gradient,
                shape = RoundedCornerShape(16.dp)
            )
            .background(
                color = Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .background(Color.Transparent)
                .padding(horizontal = 16.dp, vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = textColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun NotificacionCard(
    noti: NotificacionFirebase,
    onArchivar: (() -> Unit)? = null,
    onBorrar: (() -> Unit)? = null
) {
    val color = when (noti.tipo) {
        "parche" -> Color(0xFF296DFF)
        "infraccion" -> Color(0xFFFF7675)
        else -> Color(0xFF8F5CFF)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = 2.dp,
                brush = Brush.horizontalGradient(
                    listOf(color, TorneoYaPalette.violet)
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFF23273D), Color(0xFF1C1D25))
                )
            )
            .padding(vertical = 18.dp, horizontal = 16.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = noti.titulo,
                    color = color,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    maxLines = 2,
                    modifier = Modifier.weight(1f)
                )
                if (onBorrar != null) {
                    IconButton(
                        onClick = onBorrar,
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Eliminar",
                            tint = Color(0xFF8F5CFF)
                        )
                    }
                } else if (onArchivar != null) {
                    IconButton(
                        onClick = onArchivar,
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Done,
                            contentDescription = "Marcar como leída",
                            tint = Color(0xFF8F5CFF)
                        )
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = noti.mensaje,
                color = Color(0xFFB7B7D1),
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal
            )
            if (noti.fechaHora != null) {
                Spacer(Modifier.height(7.dp))
                Text(
                    text = formatTimestamp(noti.fechaHora),
                    color = Color(0xFF8F5CFF),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

fun formatTimestamp(timestamp: Timestamp?): String {
    if (timestamp == null) return ""
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp.seconds * 1000))
}
