package mingosgit.josecr.torneoya.ui.screens.home

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*
import mingosgit.josecr.torneoya.R
import mingosgit.josecr.torneoya.data.firebase.NotificacionFirebase
import mingosgit.josecr.torneoya.ui.theme.TorneoYaPalette
import mingosgit.josecr.torneoya.ui.theme.mutedText
import mingosgit.josecr.torneoya.viewmodel.home.NotificacionesViewModel

@Composable
fun NotificacionesScreen( // Pantalla de listado de notificaciones (no leídas/leídas) con acciones
    usuarioUid: String
) {
    val cs = MaterialTheme.colorScheme
    val context = LocalContext.current
    val app = context.applicationContext as Application

    // VM con factory manual para inyectar uid y Application
    val viewModel: NotificacionesViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return NotificacionesViewModel(usuarioUid, app) as T
            }
        }
    )

    val noLeidas by viewModel.noLeidas.collectAsState() // Lista de no leídas
    val leidas by viewModel.leidas.collectAsState()     // Lista de leídas
    val cargando by viewModel.cargando.collectAsState() // Loader global
    var mostrarLeidas by remember { mutableStateOf(false) } // Toggle pestaña

    var borrarUid by remember { mutableStateOf<String?>(null) }     // UID a borrar
    var borrarTitulo by remember { mutableStateOf<String?>(null) }  // Título para el diálogo

    val background = TorneoYaPalette.backgroundGradient
    val gradientPrimarySecondary = remember(cs.primary, cs.secondary) {
        Brush.horizontalGradient(listOf(cs.primary, cs.secondary)) // Degradado para bordes
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .padding(horizontal = 18.dp, vertical = 18.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Cabecera con título y botón para cambiar entre no leídas/leídas
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (!mostrarLeidas) stringResource(R.string.notisc_title_notificaciones) else stringResource(R.string.notisc_title_leidas),
                    color = cs.mutedText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 27.sp,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!mostrarLeidas) {
                        TextButton(onClick = { mostrarLeidas = true }) {
                            Text(stringResource(R.string.notisc_button_ver_leidas), color = cs.secondary)
                        }
                    } else {
                        TextButton(onClick = { mostrarLeidas = false }) {
                            Text(stringResource(R.string.notisc_button_no_leidas), color = cs.secondary)
                        }
                    }
                }
            }
            // Contenedor principal (loader, vacío o lista)
            Box(modifier = Modifier.weight(1f)) {
                when {
                    cargando -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                color = cs.secondary,
                                strokeWidth = 2.3.dp,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }
                    (!mostrarLeidas && noLeidas.isEmpty()) || (mostrarLeidas && leidas.isEmpty()) -> {
                        // Estado vacío con icono y textos
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(RoundedCornerShape(24.dp))
                                        .border(
                                            width = 2.5.dp,
                                            brush = gradientPrimarySecondary,
                                            shape = RoundedCornerShape(24.dp)
                                        )
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(cs.surfaceVariant, cs.surface)
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.MarkEmailRead,
                                        contentDescription = stringResource(R.string.notisc_icon_desc_sin_notificaciones),
                                        tint = cs.secondary,
                                        modifier = Modifier.size(40.dp)
                                    )
                                }
                                Spacer(Modifier.height(18.dp))
                                Text(
                                    text = if (!mostrarLeidas) stringResource(R.string.notisc_sin_notificaciones) else stringResource(R.string.notisc_sin_leidas),
                                    color = cs.secondary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 21.sp
                                )
                                Spacer(Modifier.height(7.dp))
                                Text(
                                    text = if (!mostrarLeidas)
                                        stringResource(R.string.notisc_estasyalday)
                                    else
                                        stringResource(R.string.notisc_no_hay_leidas),
                                    color = cs.mutedText,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 20.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp)
                                )
                            }
                        }
                    }
                    else -> {
                        // Lista de notificaciones según pestaña activa
                        val lista = if (!mostrarLeidas) noLeidas else leidas
                        val listState = rememberLazyListState()
                        LazyColumn(
                            state = listState,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(vertical = 4.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(
                                items = lista,
                                key = { it.uid }
                            ) { noti ->
                                NotificacionCard( // Tarjeta con título, mensaje, fecha y acciones
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
                            item { Spacer(modifier = Modifier.height(4.dp)) }
                        }
                    }
                }
            }
        }

        // Diálogo de confirmación para borrar notificación
        if (borrarUid != null && borrarTitulo != null) {
            val titulo = borrarTitulo!!
            CustomGradientDialog(
                title = stringResource(R.string.notisc_eliminar_notificacion_title),
                message = stringResource(R.string.notisc_eliminar_notificacion_message, titulo),
                confirmText = stringResource(R.string.gen_eliminar),
                dismissText = stringResource(R.string.gen_cancelar),
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
fun CustomGradientDialog( // Diálogo con borde degradado y dos botones
    title: String,
    message: String,
    confirmText: String,
    dismissText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val gradientPrimarySecondary = remember(cs.primary, cs.secondary) {
        Brush.horizontalGradient(listOf(cs.primary, cs.secondary))
    }
    val gradientErrorSecondary = remember(cs.error, cs.secondary) {
        Brush.horizontalGradient(listOf(cs.error, cs.secondary))
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        AlertDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                GradientButton( // Botón confirmar (degradado error)
                    text = confirmText,
                    gradient = gradientErrorSecondary,
                    textColor = cs.mutedText,
                    onClick = onConfirm
                )
            },
            dismissButton = {
                GradientButton( // Botón cancelar (degradado primario-secundario)
                    text = dismissText,
                    gradient = gradientPrimarySecondary,
                    textColor = cs.mutedText,
                    onClick = onDismiss
                )
            },
            title = {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = cs.mutedText
                )
            },
            text = {
                Text(
                    text = message,
                    color = cs.mutedText,
                    fontSize = 16.sp
                )
            },
            modifier = Modifier
                .padding(24.dp)
                .clip(RoundedCornerShape(22.dp))
                .border(
                    width = 3.dp,
                    brush = gradientPrimarySecondary,
                    shape = RoundedCornerShape(22.dp)
                )
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            cs.surfaceVariant,
                            cs.surface
                        )
                    )
                )
        )
    }
}

@Composable
fun GradientButton( // Botón transparente con borde degradado reutilizable
    text: String,
    gradient: Brush,
    textColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
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
                color = androidx.compose.ui.graphics.Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .background(androidx.compose.ui.graphics.Color.Transparent)
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
fun NotificacionCard( // Tarjeta de notificación con acciones de archivar/borrar
    noti: NotificacionFirebase,
    onArchivar: (() -> Unit)? = null, // Si no es null, muestra botón de marcar leída
    onBorrar: (() -> Unit)? = null    // Si no es null, muestra botón de eliminar
) {
    val cs = MaterialTheme.colorScheme
    val colorAcento = when (noti.tipo) {
        "parche" -> cs.primary
        "infraccion" -> cs.error
        else -> cs.secondary
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = 2.dp,
                brush = Brush.horizontalGradient(
                    listOf(colorAcento, cs.secondary)
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .background(
                Brush.horizontalGradient(
                    listOf(cs.surfaceVariant, cs.surface)
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
                Text( // Título de la notificación
                    text = noti.titulo,
                    color = colorAcento,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    maxLines = 2,
                    modifier = Modifier.weight(1f)
                )
                // Acción contextual: borrar o archivar
                if (onBorrar != null) {
                    IconButton(
                        onClick = onBorrar,
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = stringResource(R.string.gen_eliminar),
                            tint = cs.secondary
                        )
                    }
                } else if (onArchivar != null) {
                    IconButton(
                        onClick = onArchivar,
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Done,
                            contentDescription = stringResource(R.string.notisc_icon_desc_marcar_leida),
                            tint = cs.secondary
                        )
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            Text( // Mensaje de la notificación
                text = noti.mensaje,
                color = cs.mutedText,
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal
            )
            if (noti.fechaHora != null) {
                Spacer(Modifier.height(7.dp))
                Text( // Fecha formateada
                    text = formatTimestamp(noti.fechaHora),
                    color = cs.secondary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

fun formatTimestamp(timestamp: Timestamp?): String { // Formatea Timestamp a "yyyy-MM-dd HH:mm"
    if (timestamp == null) return ""
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp.seconds * 1000))
}
