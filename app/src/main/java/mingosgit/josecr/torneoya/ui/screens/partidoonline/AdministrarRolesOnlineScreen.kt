package mingosgit.josecr.torneoya.ui.screens.partidoonline

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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

    // Para la confirmación de borrado
    var showConfirmDialog by remember { mutableStateOf(false) }
    var usuarioABorrar: Pair<String, Boolean>? by remember { mutableStateOf(null) }
    // (uid, esAdmin)

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
        ) {
            // ----- ADMINISTRADORES ARRIBA -----
            item {
                Text(
                    text = "Administradores",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
            items(administradores, key = { it.uid }) { usuario ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 16.dp)
                ) {
                    Text(usuario.nombre, modifier = Modifier.weight(1f))
                    IconButton(
                        onClick = { vm.quitarRolAdministrador(usuario.uid) }
                    ) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Quitar rol admin")
                    }
                    IconButton(
                        onClick = {
                            usuarioABorrar = usuario.uid to true
                            showConfirmDialog = true
                        }
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar usuario completamente")
                    }
                }
                HorizontalDivider()
            }

            // ----- USUARIOS CON ACCESO ABAJO -----
            item {
                Text(
                    text = "Usuarios con acceso",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
            items(usuariosConAcceso, key = { it.uid }) { usuario ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 16.dp)
                ) {
                    Text(usuario.nombre, modifier = Modifier.weight(1f))
                    IconButton(
                        onClick = { vm.darRolAdministrador(usuario.uid) }
                    ) {
                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Dar rol administrador")
                    }
                    IconButton(
                        onClick = {
                            usuarioABorrar = usuario.uid to false
                            showConfirmDialog = true
                        }
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Quitar acceso")
                    }
                }
                HorizontalDivider()
            }
        }
    }

    // Diálogo de confirmación para borrar usuarios
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
