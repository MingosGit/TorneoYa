package mingosgit.josecr.torneoya.ui.screens.partidoonline.administración

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import mingosgit.josecr.torneoya.viewmodel.partidoonline.AdministrarRolesOnlineViewModel
import mingosgit.josecr.torneoya.ui.theme.TorneoYaPalette
import mingosgit.josecr.torneoya.R
import mingosgit.josecr.torneoya.data.firebase.PartidoFirebaseRepository
import mingosgit.josecr.torneoya.ui.theme.mutedText
import mingosgit.josecr.torneoya.ui.theme.text

@OptIn(ExperimentalMaterial3Api::class)
/**
 * Pantalla de administración de roles de un partido online.
 * - Muestra admins y usuarios con acceso
 * - Permite promover/degradar y eliminar acceso
 * - Incluye confirmación al eliminar
 */
@Composable
fun AdministrarRolesOnlineScreen(
    partidoUid: String,
    navController: NavController,
    vm: AdministrarRolesOnlineViewModel = viewModel(
        // Factory para inyectar repo con el uid del partido
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return AdministrarRolesOnlineViewModel(
                    partidoUid,
                    PartidoFirebaseRepository()
                ) as T
            }
        }
    )
) {
    // Estados de VM: listas de admins y de usuarios con acceso
    val administradores by vm.administradores.collectAsState()
    val usuariosConAcceso by vm.usuariosConAcceso.collectAsState()

    // Estado del diálogo de confirmación y usuario objetivo (uid + si es admin)
    var showConfirmDialog by remember { mutableStateOf(false) }
    var usuarioABorrar: Pair<String, Boolean>? by remember { mutableStateOf(null) }

    // Colores y pinceles de la UI
    val cs = MaterialTheme.colorScheme
    val gradientPrimary = Brush.horizontalGradient(listOf(cs.primary, cs.secondary))
    val gradientDestructive = Brush.horizontalGradient(listOf(cs.error, cs.secondary))
    val cardBg = Brush.horizontalGradient(listOf(cs.surfaceVariant, cs.surface))

    // Contenedor raíz con fondo
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TorneoYaPalette.backgroundGradient)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // HEADER: botón volver + título
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, start = 9.dp, end = 9.dp, bottom = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() }, // navega atrás
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

            // Lista con dos secciones: Administradores y Usuarios con acceso
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 0.dp, vertical = 6.dp)
            ) {
                // Sección de administradores
                item {
                    SectionHeader(stringResource(id = R.string.adminroles_administradores))
                }
                items(administradores, key = { it.uid }) { usuario ->
                    // Tarjeta de usuario admin: degradar a acceso o eliminar completamente
                    UsuarioCard(
                        uid = usuario.uid,
                        nombre = usuario.nombre,
                        colorBadge = cs.primary,
                        iconMain = Icons.Default.KeyboardArrowDown,
                        iconMainDesc = stringResource(id = R.string.adminroles_quitar_admin),
                        onMainClick = { vm.quitarRolAdministrador(usuario.uid) }, // quita rol admin
                        iconDel = Icons.Default.Delete,
                        iconDelDesc = stringResource(id = R.string.adminroles_eliminar_usuario_completo),
                        onDeleteClick = {
                            usuarioABorrar = usuario.uid to true
                            showConfirmDialog = true
                        },
                        admin = true
                    )
                }

                // Sección de usuarios con acceso
                item {
                    SectionHeader(stringResource(id = R.string.adminroles_usuarios_con_acceso))
                }
                items(usuariosConAcceso, key = { it.uid }) { usuario ->
                    // Tarjeta de usuario acceso: promover a admin o quitar acceso
                    UsuarioCard(
                        uid = usuario.uid,
                        nombre = usuario.nombre,
                        colorBadge = cs.tertiary,
                        iconMain = Icons.Default.KeyboardArrowUp,
                        iconMainDesc = stringResource(id = R.string.adminroles_dar_rol_admin),
                        onMainClick = { vm.darRolAdministrador(usuario.uid) }, // da rol admin
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

        // Diálogo de confirmación de eliminación de usuario/acceso
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
                                vm.eliminarUsuarioCompletamente(uid) // borra admin por completo
                            } else {
                                vm.quitarUsuarioDeAcceso(uid) // quita solo acceso
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

/**
 * Cabecera de sección (texto en negrita y color tenue)
 */
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

@OptIn(ExperimentalFoundationApi::class)
/**
 * Tarjeta de usuario con:
 * - Badge de color según rol
 * - Nombre
 * - Copiar UID (click largo o botón)
 * - Acción principal (promover/degradar)
 * - Acción destructiva (eliminar acceso/usuario)
 */
@Composable
private fun UsuarioCard(
    uid: String,
    nombre: String,
    colorBadge: Color,
    iconMain: ImageVector,
    iconMainDesc: String,
    onMainClick: () -> Unit,
    iconDel: ImageVector,
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
            // Click largo copia el UID; click simple sin acción
            .combinedClickable(
                onClick = {},
                onLongClick = {
                    clipboard.setText(AnnotatedString(uid))
                    Toast
                        .makeText(
                            context,
                            context.getString(R.string.gen_uid_copiado),
                            Toast.LENGTH_SHORT
                        )
                        .show()
                }
            )
            .padding(horizontal = 16.dp, vertical = 9.dp)
    ) {
        // Badge lateral de rol
        Box(
            modifier = Modifier
                .size(10.dp, 29.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(colorBadge)
        )
        Spacer(Modifier.width(13.dp))
        // Nombre del usuario (más marcado si es admin)
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

        // Acción principal (promover/degradar)
        IconButton(onClick = onMainClick) {
            Icon(iconMain, contentDescription = iconMainDesc, tint = colorBadge)
        }
        // Acción de borrado (quitar acceso/eliminar completamente)
        IconButton(onClick = onDeleteClick) {
            Icon(iconDel, contentDescription = iconDelDesc, tint = cs.error)
        }
    }
}
