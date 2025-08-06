package mingosgit.josecr.torneoya.ui.screens.partidoonline

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import mingosgit.josecr.torneoya.viewmodel.partidoonline.AdministrarRolesOnlineViewModel
import mingosgit.josecr.torneoya.ui.theme.TorneoYaPalette
import mingosgit.josecr.torneoya.R

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

    val modernBackground = Brush.verticalGradient(
        0.0f to Color(0xFF1B1D29),
        0.28f to Color(0xFF212442),
        0.58f to Color(0xFF191A23),
        1.0f to Color(0xFF14151B)
    )

    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(modernBackground)
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
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF23273D), Color(0xFF1C1D25))
                            )
                        )
                        .border(
                            width = 2.dp,
                            brush = Brush.horizontalGradient(
                                listOf(TorneoYaPalette.blue, TorneoYaPalette.violet)
                            ),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        Icons.Default.KeyboardArrowLeft,
                        contentDescription = stringResource(id = R.string.gen_volver),
                        tint = TorneoYaPalette.violet,
                        modifier = Modifier.size(27.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    stringResource(id = R.string.adminroles_title),
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
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
                        nombre = usuario.nombre,
                        colorBadge = TorneoYaPalette.blue,
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
                        nombre = usuario.nombre,
                        colorBadge = Color(0xFFFFB531),
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
                title = { Text(stringResource(id = R.string.adminroles_confirmar_eliminacion), color = Color.White) },
                text = {
                    Text(
                        if (usuarioABorrar?.second == true)
                            stringResource(id = R.string.adminroles_confirmar_eliminacion_admin)
                        else
                            stringResource(id = R.string.adminroles_confirmar_eliminacion_usuario),
                        color = Color(0xFFB7B7D1)
                    )
                },
                containerColor = Color(0xFF1C1D25),
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
                        Text(stringResource(id = R.string.gen_eliminar), color = Color(0xFFF25A6D))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showConfirmDialog = false
                            usuarioABorrar = null
                        }
                    ) {
                        Text(stringResource(id = R.string.gen_cancelar), color = TorneoYaPalette.violet)
                    }
                }
            )
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFFB7B7D1),
        modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
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
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(14.dp))
            .border(
                width = 2.dp,
                brush = Brush.horizontalGradient(
                    listOf(
                        if (admin) TorneoYaPalette.blue else Color(0xFFFFB531),
                        TorneoYaPalette.violet
                    )
                ),
                shape = RoundedCornerShape(14.dp)
            )
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFF23273D), Color(0xFF1C1D25))
                )
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
            color = if (admin) TorneoYaPalette.blue else Color(0xFFF7F7FF)
        )
        Spacer(Modifier.weight(1f))
        IconButton(onClick = onMainClick) {
            Icon(iconMain, contentDescription = iconMainDesc, tint = colorBadge)
        }
        IconButton(onClick = onDeleteClick) {
            Icon(iconDel, contentDescription = iconDelDesc, tint = Color(0xFFF25A6D))
        }
    }
}
