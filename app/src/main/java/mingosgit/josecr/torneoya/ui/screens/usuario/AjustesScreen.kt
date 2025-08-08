package mingosgit.josecr.torneoya.ui.screens.usuario

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import mingosgit.josecr.torneoya.R
import mingosgit.josecr.torneoya.viewmodel.usuario.GlobalUserViewModel

private val cardShape = RoundedCornerShape(16.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjustesScreen(
    navController: NavController,
    globalUserViewModel: GlobalUserViewModel
) {
    val miCuentaStr = stringResource(id = R.string.ajustes_mi_cuenta)
    val miCuentaLocalStr = stringResource(id = R.string.ajustes_mi_cuenta_local)
    val idiomaStr = stringResource(id = R.string.ajustes_idioma)
    val notificacionesStr = stringResource(id = R.string.ajustes_notificaciones)
    val temaAppStr = stringResource(id = R.string.ajustes_tema_app)
    val datosPrivacidadStr = stringResource(id = R.string.ajustes_datos_privacidad)
    val ayudaStr = stringResource(id = R.string.ajustes_ayuda)
    val creditosStr = stringResource(id = R.string.ajustes_creditos)
    val sobreAppStr = stringResource(id = R.string.ajustes_sobre_aplicacion)

    val opciones = listOf(
        miCuentaStr,
        miCuentaLocalStr,
        idiomaStr,
        notificacionesStr,
        temaAppStr,
        datosPrivacidadStr,
        ayudaStr,
        creditosStr,
        sobreAppStr
    )

    val sesionOnlineActiva by globalUserViewModel.sesionOnlineActiva.collectAsState()
    var mostrarAlerta by remember { mutableStateOf(false) }

    val colorScheme = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    if (mostrarAlerta) {
        CustomAjustesAlertDialog(
            onDismiss = { mostrarAlerta = false },
            onLogin = {
                mostrarAlerta = false
                navController.navigate("login")
            },
            onRegister = {
                mostrarAlerta = false
                navController.navigate("register")
            },
            blue = colorScheme.primary,
            violet = colorScheme.secondary,
            background = colorScheme.surfaceVariant,
            lightText = colorScheme.onSurface,
            mutedText = colorScheme.onSurfaceVariant
        )
    }

    val modernBackground = Brush.verticalGradient(
        0.0f to colorScheme.background,
        1.0f to colorScheme.surface
    )

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        GradientBorderedIconButton(
                            icon = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            onClick = { navController.popBackStack() },
                            gradient = Brush.horizontalGradient(listOf(colorScheme.primary, colorScheme.secondary))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            stringResource(id = R.string.ajustes_title),
                            color = colorScheme.onBackground,
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(modernBackground)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding())
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    itemsIndexed(opciones) { i, opcion ->
                        val leftColor = colorScheme.primary.copy(alpha = 0.8f)
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(4.dp, cardShape)
                                .clip(cardShape)
                                .background(colorScheme.surfaceVariant)
                                .border(
                                    width = 2.dp,
                                    brush = Brush.horizontalGradient(listOf(leftColor, colorScheme.secondary)),
                                    shape = cardShape
                                )
                                .clickable {
                                    when (opcion) {
                                        miCuentaStr -> {
                                            if (sesionOnlineActiva) {
                                                navController.navigate("mi_cuenta")
                                            } else {
                                                mostrarAlerta = true
                                            }
                                        }
                                        creditosStr -> navController.navigate("creditos_screen")
                                        miCuentaLocalStr -> navController.navigate("cuenta_local")
                                        idiomaStr -> navController.navigate("idioma_screen")
                                        temaAppStr -> navController.navigate("theme")
                                    }
                                },
                            color = colorScheme.surfaceVariant,
                            tonalElevation = 0.dp
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 22.dp, horizontal = 20.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = opcion,
                                    fontSize = 17.sp,
                                    color = colorScheme.onSurface,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GradientBorderedIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    gradient: Brush,
    size: Dp = 38.dp,
    iconSize: Dp = 21.dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .border(
                width = 2.5.dp,
                brush = gradient,
                shape = CircleShape
            )
            .background(Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(iconSize)
        )
    }
}

@Composable
private fun CustomAjustesAlertDialog(
    onDismiss: () -> Unit,
    onLogin: () -> Unit,
    onRegister: () -> Unit,
    blue: Color,
    violet: Color,
    background: Color,
    lightText: Color,
    mutedText: Color
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(background)
        ) {
            Column(
                Modifier
                    .padding(horizontal = 22.dp, vertical = 26.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    stringResource(id = R.string.ajustes_dialog_login_title),
                    color = lightText,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
                Spacer(Modifier.height(11.dp))
                Text(
                    stringResource(id = R.string.ajustes_dialog_login_message),
                    color = mutedText,
                    fontSize = 15.sp
                )
                Spacer(Modifier.height(25.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    OutlinedButton(
                        onClick = onLogin,
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            width = 2.dp,
                            brush = Brush.horizontalGradient(listOf(violet, blue))
                        ),
                        shape = RoundedCornerShape(11.dp),
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text(stringResource(id = R.string.gen_iniciar_sesion), color = violet, fontWeight = FontWeight.Bold)
                        }
                    }
                    OutlinedButton(
                        onClick = onRegister,
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            width = 2.dp,
                            brush = Brush.horizontalGradient(listOf(blue, violet))
                        ),
                        shape = RoundedCornerShape(11.dp),
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text(stringResource(id = R.string.ajustes_register), color = blue, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}
