package mingosgit.josecr.torneoya.ui.screens.ajustes

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import mingosgit.josecr.torneoya.viewmodel.usuario.MiCuentaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiCuentaScreen(
    viewModel: MiCuentaViewModel = viewModel()
) {
    val email by viewModel.email.collectAsState()
    val nombreUsuario by viewModel.nombreUsuario.collectAsState()
    val confirmarCerrarSesion by viewModel.confirmarCerrarSesion.collectAsState()
    val confirmarEliminarCuenta by viewModel.confirmarEliminarCuenta.collectAsState()
    val errorNombre by viewModel.errorCambioNombre.collectAsState()
    val cambioExitoso by viewModel.cambioNombreExitoso.collectAsState()

    var editandoNombre by remember { mutableStateOf(false) }
    var nuevoNombre by remember { mutableStateOf(TextFieldValue("")) }

    LaunchedEffect(Unit) {
        viewModel.cargarDatos()
    }

    LaunchedEffect(nombreUsuario) {
        if (!editandoNombre && nombreUsuario.isNotBlank()) {
            nuevoNombre = TextFieldValue(nombreUsuario)
        }
    }

    LaunchedEffect(cambioExitoso) {
        if (cambioExitoso) {
            editandoNombre = false
            viewModel.resetCambioNombreExitoso()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Mi cuenta") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Email: $email", fontSize = 16.sp)

            if (!editandoNombre) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Nombre de usuario: $nombreUsuario", fontSize = 16.sp)
                    TextButton(onClick = {
                        editandoNombre = true
                        nuevoNombre = TextFieldValue(nombreUsuario)
                    }) {
                        Text("Cambiar")
                    }
                }
            } else {
                OutlinedTextField(
                    value = nuevoNombre,
                    onValueChange = { nuevoNombre = it },
                    label = { Text("Nuevo nombre de usuario") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (!errorNombre.isNullOrBlank()) {
                    Text(
                        text = errorNombre ?: "",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 14.sp
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    TextButton(onClick = {
                        editandoNombre = false
                        viewModel.resetErrorCambioNombre()
                    }) {
                        Text("Cancelar")
                    }
                    Button(onClick = {
                        viewModel.cambiarNombreUsuario(nuevoNombre.text)
                    }) {
                        Text("Guardar")
                    }
                }
            }

            Divider()

            Button(
                onClick = { /* Sin funcionalidad aún */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Restablecer contraseña")
            }

            Button(
                onClick = { viewModel.confirmarCerrarSesionDialog(true) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cerrar sesión")
            }

            Button(
                onClick = { viewModel.confirmarEliminarCuentaDialog(true) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Eliminar cuenta")
            }
        }
    }

    if (confirmarCerrarSesion) {
        AlertDialog(
            onDismissRequest = { viewModel.confirmarCerrarSesionDialog(false) },
            title = { Text("Cerrar sesión") },
            text = { Text("¿Estás seguro de que quieres cerrar sesión?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.cerrarSesion()
                    viewModel.confirmarCerrarSesionDialog(false)
                }) {
                    Text("Sí")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.confirmarCerrarSesionDialog(false)
                }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (confirmarEliminarCuenta) {
        AlertDialog(
            onDismissRequest = { viewModel.confirmarEliminarCuentaDialog(false) },
            title = { Text("Eliminar cuenta") },
            text = { Text("Se eliminarán todos tus partidos creados. ¿Deseas continuar?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.eliminarCuentaYDatos()
                    viewModel.confirmarEliminarCuentaDialog(false)
                }) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.confirmarEliminarCuentaDialog(false)
                }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
