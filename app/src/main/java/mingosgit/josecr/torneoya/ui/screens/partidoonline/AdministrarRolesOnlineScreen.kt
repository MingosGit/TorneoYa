package mingosgit.josecr.torneoya.ui.screens.partidoonline

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import mingosgit.josecr.torneoya.viewmodel.partidoonline.AdministrarRolesOnlineViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdministrarRolesOnlineScreen(
    partidoUid: String,
    navController: NavController,
    vm: AdministrarRolesOnlineViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return AdministrarRolesOnlineViewModel(partidoUid, mingosgit.josecr.torneoya.data.firebase.PartidoFirebaseRepository()) as T
            }
        }
    )
) {
    val administradores by vm.administradores.collectAsState()
    val usuariosConAcceso by vm.usuariosConAcceso.collectAsState()
    var showConfirmDialog by remember { mutableStateOf(false) }
    var usuarioABorrar: Pair<String, Boolean>? by remember { mutableStateOf(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Administrar Roles") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF181B26),
                            Color(0xFF1F2233)
                        )
                    )
                )
        ) {
            // ADMINISTRADORES
            item {
                SectionHeader("Administradores")
            }
            items(administradores, key = { it.uid }) { usuario ->
                UsuarioCard(
                    nombre = usuario.nombre,
                    colorBadge = Color(0xFF296DFF),
                    iconMain = Icons.Default.KeyboardArrowDown,
                    iconMainDesc = "Quitar admin",
                    onMainClick = { vm.quitarRolAdministrador(usuario.uid) },
                    iconDel = Icons.Default.Delete,
                    iconDelDesc = "Eliminar usuario completamente",
                    onDeleteClick = {
                        usuarioABorrar = usuario.uid to true
                        showConfirmDialog = true
                    },
                    admin = true
                )
            }

            // USUARIOS CON ACCESO
            item {
                SectionHeader("Usuarios con acceso")
            }
            items(usuariosConAcceso, key = { it.uid }) { usuario ->
                UsuarioCard(
                    nombre = usuario.nombre,
                    colorBadge = Color(0xFFFFB531),
                    iconMain = Icons.Default.KeyboardArrowUp,
                    iconMainDesc = "Dar rol admin",
                    onMainClick = { vm.darRolAdministrador(usuario.uid) },
                    iconDel = Icons.Default.Delete,
                    iconDelDesc = "Quitar acceso",
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
            title = { Text("Confirmar eliminación") },
            text = {
                Text(
                    if (usuarioABorrar?.second == true)
                        "¿Estás seguro de eliminar completamente a este administrador?"
                    else
                        "¿Estás seguro de quitar el acceso a este usuario?"
                )
            },
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
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showConfirmDialog = false
                        usuarioABorrar = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFFB7B7D1),
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@Composable
private fun UsuarioCard(
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
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 5.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f))
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        // Nombre y badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp, 28.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(colorBadge)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                nombre,
                fontSize = 16.sp,
                fontWeight = if (admin) FontWeight.Bold else FontWeight.Medium,
                color = if (admin) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
        IconButton(onClick = onMainClick) {
            Icon(iconMain, contentDescription = iconMainDesc, tint = colorBadge)
        }
        IconButton(onClick = onDeleteClick) {
            Icon(iconDel, contentDescription = iconDelDesc, tint = Color.Red)
        }
    }
}
