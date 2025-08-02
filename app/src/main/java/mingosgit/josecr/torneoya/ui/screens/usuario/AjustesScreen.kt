package mingosgit.josecr.torneoya.ui.screens.ajustes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import mingosgit.josecr.torneoya.viewmodel.usuario.GlobalUserViewModel

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

    // Paleta oscura moderna
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
    val cardBg = Color(0xFF22243B)
    val cardShape = RoundedCornerShape(16.dp)

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
            blue = blue,
            background = Color(0xFF22294A),
            lightText = lightText,
            mutedText = mutedText
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
                .background(modernBackground)
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(opciones) { opcion ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(4.dp, cardShape)
                            .clip(cardShape)
                            .background(cardBg)
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
    background: Color,
    lightText: Color,
    mutedText: Color
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(19.dp),
            color = background,
            tonalElevation = 0.dp,
            shadowElevation = 12.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
        ) {
            Column(
                Modifier
                    .padding(horizontal = 26.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Inicia sesión",
                    color = lightText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Debes iniciar sesión o registrarte para acceder a tu cuenta online.",
                    color = mutedText,
                    fontSize = 15.sp
                )
                Spacer(Modifier.height(28.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = onLogin,
                        colors = ButtonDefaults.buttonColors(containerColor = blue),
                        shape = RoundedCornerShape(11.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Iniciar sesión", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(16.dp))
                    OutlinedButton(
                        onClick = onRegister,
                        border = ButtonDefaults.outlinedButtonBorder,
                        shape = RoundedCornerShape(11.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Registrarme", color = blue, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
