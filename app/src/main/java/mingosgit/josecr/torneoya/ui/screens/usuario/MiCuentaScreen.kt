package mingosgit.josecr.torneoya.ui.screens.ajustes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextOverflow
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

    // Colores y fondo igual que en AjustesScreen
    val modernBackground = Brush.verticalGradient(
        0.0f to Color(0xFF181B26),
        0.25f to Color(0xFF22263B),
        0.6f to Color(0xFF1A1E29),
        1.0f to Color(0xFF161622)
    )
    val blue = Color(0xFF296DFF)
    val violet = Color(0xFF8F5CFF)
    val lightText = Color(0xFFF7F7FF)
    val mutedText = Color(0xFFB7B7D1)

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
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Mi Cuenta",
                fontSize = 29.sp,
                color = lightText,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier
                    .padding(bottom = 32.dp, top = 18.dp)
                    .align(Alignment.CenterHorizontally)
            )

            OutlinedButton(
                onClick = {},
                enabled = false,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(94.dp),
                border = BorderStroke(2.dp, blue),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = blue
                ),
                elevation = null
            ) {
                Row(
                    Modifier
                        .fillMaxSize()
                        .padding(start = 8.dp, end = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(
                                brush = Brush.linearGradient(listOf(blue, violet)),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (nombreUsuario.isNotEmpty()) nombreUsuario.take(1).uppercase() else "",
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            nombreUsuario,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = blue,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.Email,
                                contentDescription = "Email",
                                tint = blue,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                email,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                                color = blue.copy(alpha = 0.73f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            AnimatedVisibility(
                visible = !editandoNombre,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                OutlinedButton(
                    onClick = {
                        editandoNombre = true
                        nuevoNombre = TextFieldValue(nombreUsuario)
                    },
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    border = BorderStroke(2.dp, blue),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = blue
                    ),
                    elevation = null
                ) {
                    Row(
                        Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = "Editar", modifier = Modifier.size(26.dp), tint = blue)
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Nombre de usuario", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = blue)
                            Text(
                                text = nombreUsuario,
                                fontSize = 14.sp,
                                color = blue.copy(alpha = 0.80f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Text("Cambiar", fontSize = 15.sp, color = blue, fontWeight = FontWeight.Medium)
                    }
                }
            }
            AnimatedVisibility(
                visible = editandoNombre,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = nuevoNombre,
                        onValueChange = { nuevoNombre = it },
                        label = { Text("Nuevo nombre de usuario", color = mutedText) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            containerColor = Color.Transparent,
                            unfocusedBorderColor = blue,
                            focusedBorderColor = blue,
                            cursorColor = blue,
                        )
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
                        horizontalArrangement = Arrangement.spacedBy(18.dp),
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                editandoNombre = false
                                viewModel.resetErrorCambioNombre()
                            },
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, mutedText),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.Transparent
                            )
                        ) {
                            Text("Cancelar", color = mutedText)
                        }
                        Button(
                            onClick = {
                                viewModel.cambiarNombreUsuario(nuevoNombre.text)
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = blue)
                        ) {
                            Text("Guardar", color = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            AccountMenuButton(
                title = if (resetTimer > 0) "Restablecer contraseña (${resetTimer}s)" else "Restablecer contraseña",
                icon = Icons.Filled.Email,
                description = "Te enviaremos un correo de restablecimiento",
                borderColor = Color(0xFF388E3C),
                onClick = { viewModel.enviarCorreoResetPassword() },
                enabled = resetTimer == 0,
                lightText = lightText
            )
            AnimatedVisibility(
                visible = showMensajeReset,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Text(
                    "Correo de restablecimiento de contraseña enviado.",
                    color = Color(0xFF207A39),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            Spacer(modifier = Modifier.height(18.dp))

            AccountMenuButton(
                title = "Cerrar sesión",
                icon = Icons.Filled.Logout,
                description = "Salir de tu cuenta y de todos tus dispositivos",
                borderColor = violet,
                onClick = { viewModel.confirmarCerrarSesionDialog(true) },
                lightText = lightText
            )
            Spacer(modifier = Modifier.height(18.dp))
            AccountMenuButton(
                title = "Eliminar cuenta",
                icon = Icons.Filled.Delete,
                description = "Esta acción es irreversible. Todos tus datos serán eliminados.",
                borderColor = Color(0xFFF44336),
                onClick = { viewModel.confirmarEliminarCuentaDialog(true) },
                lightText = lightText
            )
        }

        if (confirmarCerrarSesion) {
            AlertDialog(
                onDismissRequest = { viewModel.confirmarCerrarSesionDialog(false) },
                title = { Text("Cerrar sesión", color = lightText) },
                text = { Text("¿Estás seguro de que quieres cerrar sesión?", color = mutedText) },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.cerrarSesion()
                        viewModel.confirmarCerrarSesionDialog(false)
                    }) {
                        Text("Sí", color = blue)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        viewModel.confirmarCerrarSesionDialog(false)
                    }) {
                        Text("Cancelar", color = mutedText)
                    }
                },
                containerColor = Color(0xFF22243B)
            )
        }

        if (confirmarEliminarCuenta) {
            AlertDialog(
                onDismissRequest = { viewModel.confirmarEliminarCuentaDialog(false) },
                title = { Text("Eliminar cuenta", color = lightText) },
                text = { Text("Se eliminarán todos tus partidos creados. ¿Deseas continuar?", color = mutedText) },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.eliminarCuentaYDatos()
                        viewModel.confirmarEliminarCuentaDialog(false)
                    }) {
                        Text("Eliminar", color = Color(0xFFF44336))
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        viewModel.confirmarEliminarCuentaDialog(false)
                    }) {
                        Text("Cancelar", color = mutedText)
                    }
                },
                containerColor = Color(0xFF22243B)
            )
        }
    }
}

@Composable
fun AccountMenuButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    borderColor: Color,
    onClick: () -> Unit,
    enabled: Boolean = true,
    lightText: Color = Color.White
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(78.dp),
        border = BorderStroke(2.dp, borderColor),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = borderColor
        ),
        elevation = null
    ) {
        Row(
            Modifier
                .fillMaxSize()
                .padding(start = 8.dp, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = title,
                tint = borderColor,
                modifier = Modifier
                    .size(30.dp)
                    .padding(end = 16.dp)
            )
            Column(
                Modifier.weight(1f)
            ) {
                Text(
                    title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = borderColor
                )
                Text(
                    text = description,
                    fontSize = 13.sp,
                    color = lightText.copy(alpha = 0.65f),
                    maxLines = 2
                )
            }
        }
    }
}
