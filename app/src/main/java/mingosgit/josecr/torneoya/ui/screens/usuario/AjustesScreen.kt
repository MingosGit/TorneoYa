package mingosgit.josecr.torneoya.ui.screens.usuario

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.window.Dialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.unit.Dp
import androidx.navigation.NavController
import mingosgit.josecr.torneoya.R
import mingosgit.josecr.torneoya.ui.theme.TorneoYaPalette
import mingosgit.josecr.torneoya.viewmodel.usuario.GlobalUserViewModel

private val cardShape = RoundedCornerShape(16.dp)

private val leftColors = listOf(
    Color(0xFF2ecc71),
    Color(0xFF3498db),
    Color(0xFFf1c40f),
    Color(0xFFe67e22),
    Color(0xFF9b59b6),
    Color(0xFF34495e),
    Color(0xFF16a085),
    Color(0xFFe74c3c),
    Color(0xFFf39c12),
)
private val rightColor = Color(0xFF8F5CFF)

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

    val lightText = Color(0xFFF7F7FF)
    val mutedText = Color(0xFFB7B7D1)
    val cardBg = Color(0xFF22243B)
    val blue = Color(0xFF296DFF)

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
            blue = TorneoYaPalette.blue,
            violet = TorneoYaPalette.violet,
            background = Color(0xFF22294A),
            lightText = Color(0xFFF7F7FF),
            mutedText = Color(0xFFB7B7D1)
        )
    }
    val modernBackground = Brush.verticalGradient(
        0.0f to Color(0xFF1B1D29),
        0.28f to Color(0xFF212442),
        0.58f to Color(0xFF191A23),
        1.0f to Color(0xFF14151B)
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
                            gradient = Brush.horizontalGradient(listOf(Color(0xFF296DFF), Color(0xFF8F5CFF)))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            stringResource(id = R.string.ajustes_title),
                            color = lightText,
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
                        val leftColor = leftColors[i % leftColors.size]
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(4.dp, cardShape)
                                .clip(cardShape)
                                .background(cardBg)
                                .border(
                                    width = 2.dp,
                                    brush = Brush.horizontalGradient(listOf(leftColor, rightColor)),
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
                                        miCuentaLocalStr -> {
                                            navController.navigate("cuenta_local")
                                        }
                                        idiomaStr -> {
                                            navController.navigate("idioma_screen")
                                        }
                                        // Otros casos pueden añadirse aquí
                                    }
                                },
                            color = cardBg,
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
                                    color = lightText,
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
            tint = Color.White,
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
