package mingosgit.josecr.torneoya.ui.screens.ajustes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import mingosgit.josecr.torneoya.viewmodel.usuario.MiCuentaViewModel
import mingosgit.josecr.torneoya.viewmodel.usuario.GlobalUserViewModel
import mingosgit.josecr.torneoya.ui.theme.TorneoYaPalette

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiCuentaScreen(
    viewModel: MiCuentaViewModel = viewModel(),
    globalUserViewModel: GlobalUserViewModel
) {
    val email by viewModel.email.collectAsState()
    val nombreUsuario by viewModel.nombreUsuario.collectAsState()
    val confirmarCerrarSesion by viewModel.confirmarCerrarSesion.collectAsState()
    val confirmarEliminarCuenta by viewModel.confirmarEliminarCuenta.collectAsState()
    val errorNombre by viewModel.errorCambioNombre.collectAsState()
    val cambioExitoso by viewModel.cambioNombreExitoso.collectAsState()
    val resetTimer by viewModel.resetTimer.collectAsState()
    val showMensajeReset by viewModel.showMensajeReset.collectAsState()

    val avatar by globalUserViewModel.avatar.collectAsState()
    val context = LocalContext.current

    var editandoNombre by remember { mutableStateOf(false) }
    var nuevoNombre by remember { mutableStateOf(TextFieldValue("")) }

    val modernBackground = Brush.verticalGradient(
        0.0f to Color(0xFF1B1D29),
        0.25f to Color(0xFF22263B),
        0.6f to Color(0xFF1A1E29),
        1.0f to Color(0xFF161622)
    )
    val blue = TorneoYaPalette.blue
    val violet = TorneoYaPalette.violet
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
            .background(modernBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 22.dp, vertical = 15.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Mi Cuenta",
                fontSize = 29.sp,
                color = lightText,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier
                    .padding(bottom = 32.dp, top = 16.dp)
                    .align(Alignment.CenterHorizontally)
            )

            // Avatar y datos de usuario
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(
                        width = 2.dp,
                        brush = Brush.horizontalGradient(listOf(blue, violet)),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF23273D), Color(0xFF1C1D25))
                        )
                    )
                    .padding(0.dp)
                    .height(94.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    Modifier
                        .fillMaxSize()
                        .padding(start = 18.dp, end = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // AVATAR VISUAL
                    val avatarRes = if (avatar == null || avatar == 0) {
                        context.resources.getIdentifier("avatar_placeholder", "drawable", context.packageName)
                    } else {
                        context.resources.getIdentifier("avatar_$avatar", "drawable", context.packageName)
                    }
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .border(
                                width = 2.dp,
                                brush = Brush.horizontalGradient(listOf(blue, violet)),
                                shape = CircleShape
                            )
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(Color(0xFF23273D), Color(0xFF1B1D29)),
                                    radius = 90f
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = avatarRes),
                            contentDescription = "Avatar de usuario",
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                        )
                    }
                    Spacer(Modifier.width(18.dp))
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

            Spacer(modifier = Modifier.height(26.dp))

            // CAMBIO NOMBRE USUARIO
            AnimatedVisibility(
                visible = !editandoNombre,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(17.dp))
                        .border(
                            width = 2.dp,
                            brush = Brush.horizontalGradient(listOf(blue, violet)),
                            shape = RoundedCornerShape(17.dp)
                        )
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF23273D), Color(0xFF1C1D25))
                            )
                        )
                        .clickable {
                            editandoNombre = true
                            nuevoNombre = TextFieldValue(nombreUsuario)
                        }
                        .height(64.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = "Editar", modifier = Modifier.size(25.dp), tint = blue)
                        Spacer(Modifier.width(15.dp))
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
                        Spacer(Modifier.width(9.dp))
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
                        .clip(RoundedCornerShape(17.dp))
                        .border(
                            width = 2.dp,
                            brush = Brush.horizontalGradient(listOf(blue, violet)),
                            shape = RoundedCornerShape(17.dp)
                        )
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF23273D), Color(0xFF1C1D25))
                            )
                        )
                        .padding(vertical = 15.dp, horizontal = 15.dp)
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

            Spacer(modifier = Modifier.height(24.dp))

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
            Spacer(modifier = Modifier.height(16.dp))

            AccountMenuButton(
                title = "Cerrar sesión",
                icon = Icons.Filled.Logout,
                description = "Salir de tu cuenta y de todos tus dispositivos",
                borderColor = violet,
                onClick = { viewModel.confirmarCerrarSesionDialog(true) },
                lightText = lightText
            )
            Spacer(modifier = Modifier.height(16.dp))
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
            CustomCerrarSesionDialog(
                onConfirm = {
                    viewModel.cerrarSesion()
                    viewModel.confirmarCerrarSesionDialog(false)
                },
                onDismiss = {
                    viewModel.confirmarCerrarSesionDialog(false)
                },
                blue = TorneoYaPalette.blue,
                violet = TorneoYaPalette.violet,
                rojo = Color(0xFFFF2D55),
                background = Color(0xFF22243B),
                lightText = Color(0xFFF7F7FF),
                mutedText = Color(0xFFB7B7D1)
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
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(78.dp)
            .clip(RoundedCornerShape(17.dp))
            .border(
                width = 2.dp,
                brush = Brush.horizontalGradient(
                    listOf(borderColor, TorneoYaPalette.violet)
                ),
                shape = RoundedCornerShape(17.dp)
            )
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFF23273D), Color(0xFF1C1D25))
                )
            )
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            Modifier
                .fillMaxSize()
                .padding(start = 15.dp, end = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = title,
                tint = borderColor,
                modifier = Modifier
                    .size(29.dp)
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
@Composable
private fun CustomCerrarSesionDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    blue: Color,
    violet: Color,
    rojo: Color,
    background: Color,
    lightText: Color,
    mutedText: Color
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .border(
                    width = 2.dp,
                    brush = Brush.horizontalGradient(listOf(blue, violet)),
                    shape = RoundedCornerShape(18.dp)
                )
                .clip(RoundedCornerShape(18.dp))
                .background(background)
        ) {
            Column(
                Modifier
                    .padding(horizontal = 22.dp, vertical = 26.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "¿Cerrar sesión?",
                    color = lightText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Spacer(Modifier.height(11.dp))
                Text(
                    text = "¿Estás seguro que quieres cerrar sesión?",
                    color = mutedText,
                    fontSize = 15.sp
                )
                Spacer(Modifier.height(25.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    OutlinedButton(
                        onClick = onConfirm,
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            width = 2.dp,
                            brush = Brush.horizontalGradient(listOf(rojo, violet))
                        ),
                        shape = RoundedCornerShape(11.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            "SI",
                            color = rojo,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    OutlinedButton(
                        onClick = onDismiss,
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            width = 2.dp,
                            brush = Brush.horizontalGradient(listOf(blue, violet))
                        ),
                        shape = RoundedCornerShape(11.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            "Cancelar",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}