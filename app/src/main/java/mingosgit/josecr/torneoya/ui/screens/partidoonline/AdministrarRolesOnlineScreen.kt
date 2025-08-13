package mingosgit.josecr.torneoya.ui.screens.partidoonline

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import mingosgit.josecr.torneoya.viewmodel.partidoonline.AdministrarRolesOnlineViewModel
import mingosgit.josecr.torneoya.ui.theme.TorneoYaPalette
import mingosgit.josecr.torneoya.R
import mingosgit.josecr.torneoya.ui.theme.mutedText
import mingosgit.josecr.torneoya.ui.theme.text

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdministrarRolesOnlineScreen(
    partidoUid: String,
    navController: NavController,
    vm: AdministrarRolesOnlineViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return AdministrarRolesOnlineViewModel(
                    partidoUid,
                    mingosgit.josecr.torneoya.data.firebase.PartidoFirebaseRepository()
                ) as T
            }
        }
    )
) {
    val administradores by vm.administradores.collectAsState()
    val usuariosConAcceso by vm.usuariosConAcceso.collectAsState()
    var showConfirmDialog by remember { mutableStateOf(false) }
    var usuarioABorrar: Pair<String, Boolean>? by remember { mutableStateOf(null) }

    val cs = MaterialTheme.colorScheme
    val gradientPrimary = Brush.horizontalGradient(listOf(cs.primary, cs.secondary))
    val gradientDestructive = Brush.horizontalGradient(listOf(cs.error, cs.secondary))
    val cardBg = Brush.horizontalGradient(listOf(cs.surfaceVariant, cs.surface))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TorneoYaPalette.backgroundGradient)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // HEADER
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, start = 9.dp, end = 9.dp, bottom = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(cardBg, CircleShape)
                        .border(2.dp, gradientPrimary, CircleShape)
                ) {
                    Icon(
                        Icons.Default.KeyboardArrowLeft,
                        contentDescription = stringResource(id = R.string.gen_volver),
                        tint = cs.secondary,
                        modifier = Modifier.size(27.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    stringResource(id = R.string.adminroles_title),
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Black,
                    color = cs.text
                )
            }

            Spacer(Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 0.dp, vertical = 6.dp)
            ) {
                // ADMINISTRADORES
                item {
                    SectionHeader(stringResource(id = R.string.adminroles_administradores))
                }
                items(administradores, key = { it.uid }) { usuario ->
                    UsuarioCard(
                        uid = usuario.uid,
                        nombre = usuario.nombre,
                        colorBadge = cs.primary,
                        iconMain = Icons.Default.KeyboardArrowDown,
                        iconMainDesc = stringResource(id = R.string.adminroles_quitar_admin),
                        onMainClick = { vm.quitarRolAdministrador(usuario.uid) },
                        iconDel = Icons.Default.Delete,
                        iconDelDesc = stringResource(id = R.string.adminroles_eliminar_usuario_completo),
                        onDeleteClick = {
                            usuarioABorrar = usuario.uid to true
                            showConfirmDialog = true
                        },
                        admin = true
                    )
                }

                // USUARIOS CON ACCESO
                item {
                    SectionHeader(stringResource(id = R.string.adminroles_usuarios_con_acceso))
                }
                items(usuariosConAcceso, key = { it.uid }) { usuario ->
                    UsuarioCard(
                        uid = usuario.uid,
                        nombre = usuario.nombre,
                        colorBadge = cs.tertiary,
                        iconMain = Icons.Default.KeyboardArrowUp,
                        iconMainDesc = stringResource(id = R.string.adminroles_dar_rol_admin),
                        onMainClick = { vm.darRolAdministrador(usuario.uid) },
                        iconDel = Icons.Default.Delete,
                        iconDelDesc = stringResource(id = R.string.adminroles_quitar_acceso),
                        onDeleteClick = {
                            usuarioABorrar = usuario.uid to false
                            showConfirmDialog = true
                        },
                        admin = false
                    )
                }
            }
        }

        if (showConfirmDialog && usuarioABorrar != null) {
            AlertDialog(
                onDismissRequest = { showConfirmDialog = false },
                title = { Text(stringResource(id = R.string.adminroles_confirmar_eliminacion), color = cs.text) },
                text = {
                    Text(
                        if (usuarioABorrar?.second == true)
                            stringResource(id = R.string.adminroles_confirmar_eliminacion_admin)
                        else
                            stringResource(id = R.string.adminroles_confirmar_eliminacion_usuario),
                        color = cs.mutedText
                    )
                },
                containerColor = cs.surface,
                confirmButton = {
                    TextButton(
                        onClick = {
                            val (uid, esAdmin) = usuarioABorrar!!
                            if (esAdmin) {
                                vm.eliminarUsuarioCompletamente(uid)
                            } else {
                                vm.quitarUsuarioDeAcceso(uid)
                            }
                            showConfirmDialog = false
                            usuarioABorrar = null
                        }
                    ) {
                        Text(stringResource(id = R.string.gen_eliminar), color = cs.error)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showConfirmDialog = false
                            usuarioABorrar = null
                        }
                    ) {
                        Text(stringResource(id = R.string.gen_cancelar), color = cs.secondary)
                    }
                }
            )
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    val cs = MaterialTheme.colorScheme
    Text(
        text = text,
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold,
        color = cs.mutedText,
        modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
    )
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun UsuarioCard(
    uid: String,
    nombre: String,
    colorBadge: Color,
    iconMain: androidx.compose.ui.graphics.vector.ImageVector,
    iconMainDesc: String,
    onMainClick: () -> Unit,
    iconDel: androidx.compose.ui.graphics.vector.ImageVector,
    iconDelDesc: String,
    onDeleteClick: () -> Unit,
    admin: Boolean
) {
    val cs = MaterialTheme.colorScheme
    val borderBrush = Brush.horizontalGradient(
        listOf(if (admin) cs.primary else cs.tertiary, cs.secondary)
    )
    val bgBrush = Brush.horizontalGradient(listOf(cs.surfaceVariant, cs.surface))

    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(14.dp))
            .border(2.dp, borderBrush, RoundedCornerShape(14.dp))
            .background(bgBrush)
            .combinedClickable(
                onClick = {},
                onLongClick = {
                    clipboard.setText(AnnotatedString(uid))
                    Toast
                        .makeText(context, context.getString(R.string.gen_uid_copiado), Toast.LENGTH_SHORT)                        .show()
                }
            )
            .padding(horizontal = 16.dp, vertical = 9.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp, 29.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(colorBadge)
        )
        Spacer(Modifier.width(13.dp))
        Text(
            nombre,
            fontSize = 16.sp,
            fontWeight = if (admin) FontWeight.Bold else FontWeight.Medium,
            color = if (admin) cs.primary else cs.text
        )
        Spacer(Modifier.weight(1f))

        // Botón para copiar UID explícitamente
        IconButton(
            onClick = {
                clipboard.setText(AnnotatedString(uid))
                Toast
                    .makeText(context, "UID copiado al portapapeles", Toast.LENGTH_SHORT)
                    .show()
            }
        ) {
            Icon(
                imageVector = Icons.Filled.ContentCopy,
                contentDescription = "Copiar UID",
                tint = cs.secondary
            )
        }

        IconButton(onClick = onMainClick) {
            Icon(iconMain, contentDescription = iconMainDesc, tint = colorBadge)
        }
        IconButton(onClick = onDeleteClick) {
            Icon(iconDel, contentDescription = iconDelDesc, tint = cs.error)
        }
    }
}
