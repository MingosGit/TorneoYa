package mingosgit.josecr.torneoya.ui.screens.ajustes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
    val resetTimer by viewModel.resetTimer.collectAsState()
    val showMensajeReset by viewModel.showMensajeReset.collectAsState()

    var editandoNombre by remember { mutableStateOf(false) }
    var nuevoNombre by remember { mutableStateOf(TextFieldValue("")) }

    LaunchedEffect(Unit) { viewModel.cargarDatos() }
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFF5F7FA), Color(0xFFEEF2F6))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 0.dp, vertical = 0.dp)
        ) {
            // HEADER
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent)
                    .padding(top = 34.dp, start = 22.dp, end = 22.dp, bottom = 10.dp)
            ) {
                Column {
                    Text(
                        "MI CUENTA",
                        color = Color(0xFF20222E),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(2.dp))
                    Divider(color = Color(0xFFD5D9E0), thickness = 1.dp)
                }
            }

            Spacer(Modifier.height(10.dp))

            // DATOS
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp)
            ) {
                // MAIL
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Icon(
                        Icons.Filled.Email,
                        contentDescription = "Email",
                        tint = Color(0xFF3677E0),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(7.dp))
                    Text(
                        email,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        color = Color(0xFF384358)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                // USUARIO
                AnimatedVisibility(
                    visible = !editandoNombre,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Nombre de usuario:",
                            fontSize = 15.sp,
                            color = Color(0xFF7D8591)
                        )
                        Spacer(Modifier.width(7.dp))
                        Text(
                            nombreUsuario,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color(0xFF1B2333)
                        )
                        Spacer(Modifier.weight(1f))
                        TextButton(
                            onClick = {
                                editandoNombre = true
                                nuevoNombre = TextFieldValue(nombreUsuario)
                            },
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Filled.Edit, contentDescription = "Editar", modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(3.dp))
                            Text("Cambiar", fontSize = 14.sp)
                        }
                    }
                }

                AnimatedVisibility(
                    visible = editandoNombre,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(Modifier.fillMaxWidth()) {
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
                                fontSize = 14.sp,
                                modifier = Modifier.padding(top = 2.dp, start = 2.dp)
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.align(Alignment.End).padding(top = 8.dp)
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
                }

                Spacer(modifier = Modifier.height(22.dp))
                Divider(color = Color(0xFFE5E7ED), thickness = 1.dp)
                Spacer(modifier = Modifier.height(10.dp))

                // BOTÓN: RESTABLECER PASSWORD
                Button(
                    onClick = { viewModel.enviarCorreoResetPassword() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = resetTimer == 0,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4767C3)),
                    shape = MaterialTheme.shapes.small
                ) {
                    if (resetTimer > 0) {
                        Text(
                            "Restablecer contraseña (${resetTimer}s)",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    } else {
                        Text(
                            "Restablecer contraseña",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Mensaje de éxito
                AnimatedVisibility(
                    visible = showMensajeReset,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Text(
                        "Correo de restablecimiento de contraseña enviado.",
                        color = Color(0xFF207A39),
                        fontSize = 15.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // BOTÓN: CERRAR SESIÓN
                Button(
                    onClick = { viewModel.confirmarCerrarSesionDialog(true) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4767C3)),
                    shape = MaterialTheme.shapes.small
                ) {
                    Icon(Icons.Filled.Logout, contentDescription = "Cerrar sesión", tint = Color.White)
                    Spacer(Modifier.width(6.dp))
                    Text("Cerrar sesión", color = Color.White, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // BOTÓN: ELIMINAR CUENTA
                Button(
                    onClick = { viewModel.confirmarEliminarCuentaDialog(true) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF74B4B)),
                    shape = MaterialTheme.shapes.small
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = "Eliminar cuenta", tint = Color.White)
                    Spacer(Modifier.width(6.dp))
                    Text("Eliminar cuenta", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }

    // DIALOGOS
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
