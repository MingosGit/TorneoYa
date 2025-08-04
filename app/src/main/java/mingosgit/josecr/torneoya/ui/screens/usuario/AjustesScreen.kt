package mingosgit.josecr.torneoya.ui.screens.ajustes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import mingosgit.josecr.torneoya.ui.theme.TorneoYaPalette
import mingosgit.josecr.torneoya.viewmodel.usuario.GlobalUserViewModel

private val cardShape = RoundedCornerShape(16.dp)

// Paleta ordenada para las opciones
private val leftColors = listOf(
    Color(0xFF2ecc71), // Verde
    Color(0xFF3498db), // Azul
    Color(0xFFf1c40f), // Amarillo
    Color(0xFFe67e22), // Naranja
    Color(0xFF9b59b6), // Violeta
    Color(0xFF34495e), // Azul oscuro
    Color(0xFF16a085), // Verde azulado
    Color(0xFFe74c3c), // Rojo
    Color(0xFFf39c12), // Amarillo naranja
)
private val rightColor = Color(0xFF8F5CFF) // Violeta institucional

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjustesScreen(
    navController: NavController,
    globalUserViewModel: GlobalUserViewModel
) {
    val opciones = listOf(
        "Mi cuenta",
        "Mi cuenta local",
        "Idioma",
        "Notificaciones",
        "Tema de la app",
        "Datos y privacidad",
        "Ayuda",
        "Créditos",
        "Sobre la aplicación"
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


    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Ajustes", color = lightText, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { innerPadding ->
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
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
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
                                    "Mi cuenta" -> {
                                        if (sesionOnlineActiva) {
                                            navController.navigate("mi_cuenta")
                                        } else {
                                            mostrarAlerta = true
                                        }
                                    }
                                    "Mi cuenta local" -> {
                                        navController.navigate("cuenta_local")
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
                    text = "Inicia sesión",
                    color = lightText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Spacer(Modifier.height(11.dp))
                Text(
                    text = "Debes iniciar sesión o registrarte para acceder a tu cuenta online.",
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
                        contentPadding = PaddingValues(0.dp) // Elimina padding interno
                    ) {
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("Iniciar sesión", color = violet, fontWeight = FontWeight.Bold)
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
                            Text("Registrarme", color = blue, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}


