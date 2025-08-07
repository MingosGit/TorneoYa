package mingosgit.josecr.torneoya.ui.screens.ajustes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import mingosgit.josecr.torneoya.R
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
                text = stringResource(id = R.string.micuenta_title),
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
                    .height(104.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    Modifier
                        .fillMaxSize()
                        .padding(start = 22.dp, end = 9.dp),
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
                            .size(70.dp)
                            .offset(x = (-20).dp)
                            .zIndex(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        GradientCircleIcon(
                            borderColor = blue,
                            iconRes = avatarRes,
                            size = 70.dp,
                            iconSize = 50.dp
                        )
                    }
                    Spacer(Modifier.width(10.dp))
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
                                contentDescription = stringResource(id = R.string.micuenta_email_content_description),
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
                        GradientCircleIcon(
                            borderColor = blue,
                            iconVector = Icons.Filled.Edit,
                            size = 41.dp,
                            iconSize = 25.dp
                        )
                        Spacer(Modifier.width(15.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                stringResource(id = R.string.micuenta_nombre_de_usuario_label),
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = blue
                            )
                            Text(
                                text = nombreUsuario,
                                fontSize = 14.sp,
                                color = blue.copy(alpha = 0.80f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(Modifier.width(9.dp))
                        Text(
                            stringResource(id = R.string.micuenta_cambiar_label),
                            fontSize = 15.sp,
                            color = blue,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = editandoNombre,
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
                        .padding(vertical = 20.dp, horizontal = 15.dp)
                        .animateContentSize()
                ) {
                    Column {
                        OutlinedTextField(
                            value = nuevoNombre,
                            onValueChange = { nuevoNombre = it },
                            label = {
                                Text(
                                    stringResource(id = R.string.micuenta_nuevo_nombre_de_usuario_label),
                                    color = mutedText
                                )
                            },
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
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    editandoNombre = false
                                    viewModel.resetErrorCambioNombre()
                                },
                                border = BorderStroke(2.dp, mutedText),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.Transparent)
                            ) {
                                Text(
                                    stringResource(id = R.string.gen_cancelar),
                                    color = mutedText,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            OutlinedButton(
                                onClick = {
                                    viewModel.cambiarNombreUsuario(nuevoNombre.text)
                                },
                                border = BorderStroke(2.dp, Brush.horizontalGradient(listOf(blue, violet))),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.Transparent)
                            ) {
                                Text(
                                    stringResource(id = R.string.gen_guardar),
                                    color = blue,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            AccountMenuButton(
                title = if (resetTimer > 0)
                    "${stringResource(id = R.string.micuenta_restablecer_contraseña_label)} ($resetTimer s)"
                else stringResource(id = R.string.micuenta_restablecer_contraseña_label),
                icon = Icons.Filled.Email,
                description = stringResource(id = R.string.micuenta_restablecer_contraseña_enviando),
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
                    stringResource(id = R.string.micuenta_restablecer_contraseña_mensaje),
                    color = Color(0xFF207A39),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            AccountMenuButton(
                title = stringResource(id = R.string.micuenta_cerrar_sesion_label),
                icon = Icons.Filled.Logout,
                description = stringResource(id = R.string.micuenta_cerrar_sesion_descripcion),
                borderColor = violet,
                onClick = { viewModel.confirmarCerrarSesionDialog(true) },
                lightText = lightText
            )
            Spacer(modifier = Modifier.height(16.dp))
            AccountMenuButton(
                title = stringResource(id = R.string.micuenta_eliminar_cuenta_label),
                icon = Icons.Filled.Delete,
                description = stringResource(id = R.string.micuenta_eliminar_cuenta_descripcion),
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
            Dialog(onDismissRequest = { viewModel.confirmarEliminarCuentaDialog(false) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp)
                        .border(
                            width = 2.dp,
                            brush = Brush.horizontalGradient(listOf(Color(0xFFF44336), violet)),
                            shape = RoundedCornerShape(18.dp)
                        )
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFF22243B))
                ) {
                    Column(
                        Modifier
                            .padding(horizontal = 22.dp, vertical = 26.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(id = R.string.micuenta_eliminar_cuenta_dialog_title),
                            color = Color(0xFFF44336),
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Spacer(Modifier.height(11.dp))
                        Text(
                            text = stringResource(id = R.string.micuenta_eliminar_cuenta_dialog_message),
                            color = mutedText,
                            fontSize = 15.sp
                        )
                        Spacer(Modifier.height(25.dp))
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            GradientBorderButton(
                                text = stringResource(id = R.string.gen_eliminar),
                                onClick = {
                                    viewModel.eliminarCuentaYDatos()
                                    viewModel.confirmarEliminarCuentaDialog(false)
                                },
                                borderGradient = Brush.horizontalGradient(listOf(Color(0xFFF44336), violet)),
                                textColor = Color(0xFFF44336)
                            )
                            Spacer(Modifier.width(14.dp))
                            GradientBorderButton(
                                text = stringResource(id = R.string.gen_cancelar),
                                onClick = { viewModel.confirmarEliminarCuentaDialog(false) },
                                borderGradient = Brush.horizontalGradient(listOf(blue, violet)),
                                textColor = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

// REUTILIZABLE: Icono rodeado de borde gradiente circular, puede ser vectorial o resource
@Composable
fun GradientCircleIcon(
    borderColor: Color,
    iconVector: androidx.compose.ui.graphics.vector.ImageVector? = null,
    iconRes: Int? = null,
    size: Dp = 41.dp,
    iconSize: Dp = 25.dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .border(
                width = 2.5.dp,
                brush = Brush.sweepGradient(listOf(borderColor, borderColor.copy(alpha = 0.5f), borderColor)),
                shape = CircleShape
            )
            .background(
                color = Color(0xFF181A23),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (iconVector != null) {
            Icon(
                imageVector = iconVector,
                contentDescription = null,
                tint = borderColor,
                modifier = Modifier.size(iconSize)
            )
        } else if (iconRes != null) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(iconSize).clip(CircleShape)
            )
        }
    }
}

@Composable
fun GradientBorderButton(
    text: String,
    onClick: () -> Unit,
    borderGradient: Brush,
    textColor: Color,
    background: Color = Color.Transparent
) {
    OutlinedButton(
        onClick = onClick,
        border = BorderStroke(2.dp, borderGradient),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = background)
    ) {
        Text(text, color = textColor, fontWeight = FontWeight.SemiBold)
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
                .padding(start = 18.dp, end = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GradientCircleIcon(
                borderColor = borderColor,
                iconVector = icon,
                size = 41.dp,
                iconSize = 23.dp
            )
            Spacer(Modifier.width(18.dp))
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
                    text = stringResource(id = R.string.micuenta_confirmar_cerrar_sesion_title),
                    color = lightText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Spacer(Modifier.height(11.dp))
                Text(
                    text = stringResource(id = R.string.micuenta_confirmar_cerrar_sesion_message),
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
                            stringResource(id = R.string.micuenta_si_button),
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
                            stringResource(id = R.string.gen_cancelar),
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
