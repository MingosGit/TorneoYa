package mingosgit.josecr.torneoya.ui.screens.partidoonline

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.ui.graphics.Brush
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import mingosgit.josecr.torneoya.viewmodel.partidoonline.PartidoOnlineViewModel
import mingosgit.josecr.torneoya.ui.theme.TorneoYaPalette

@Composable
fun PartidoOnlineScreen(
    navController: NavController,
    partidoViewModel: PartidoOnlineViewModel
) {
    val currentUser = FirebaseAuth.getInstance().currentUser

    if (currentUser == null) {
        LoginOrRegisterPrompt(
            navController = navController
        )
    } else {
        PartidoOnlineScreenContent(
            navController = navController,
            partidoViewModel = partidoViewModel
        )
    }
}

@Composable
private fun LoginOrRegisterPrompt(
    navController: NavController
) {
    Scaffold { innerPadding ->
        Column(
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
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Debes iniciar sesión o crear una cuenta para ver los partidos online.",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 32.dp)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { navController.navigate("login") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(11.dp) // Menos redondeado
                ) {
                    Text("Iniciar sesión")
                }
                OutlinedButton(
                    onClick = { navController.navigate("register") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(11.dp) // Menos redondeado
                ) {
                    Text("Crear cuenta")
                }
            }
        }
    }
}

@Composable
fun BuscarPorUidButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Botón menos redondo, border más marcado y color llamativo
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = TorneoYaPalette.yellow
        ),
        border = ButtonDefaults.outlinedButtonBorder.copy(
            width = 1.5.dp,
            brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                listOf(TorneoYaPalette.yellow, TorneoYaPalette.violet)
            )
        )
    ) {
        Text("Buscar por UID", color = TorneoYaPalette.yellow, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
    }
}

@Composable
fun EstadoPartidoChip(
    estado: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, borderColor, textColor) = when (estado) {
        "PREVIA" -> Triple(Color(0x22FFD84C), TorneoYaPalette.yellow, TorneoYaPalette.yellow)
        "EN_CURSO" -> Triple(Color(0x22296DFF), TorneoYaPalette.blue, TorneoYaPalette.blue)
        "FINALIZADO" -> Triple(Color(0x22B7B7D1), Color(0xFFB7B7D1), Color(0xFFB7B7D1))
        else -> Triple(Color.Transparent, TorneoYaPalette.mutedText, TorneoYaPalette.mutedText)
    }

    Surface(
        color = bgColor,
        contentColor = textColor,
        shape = RoundedCornerShape(7.dp),
        border = BorderStroke(1.4.dp, borderColor),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = when (estado) {
                    "PREVIA" -> "PREVIA"
                    "EN_CURSO" -> "EN CURSO"
                    "FINALIZADO" -> "FINALIZADO"
                    else -> estado
                },
                color = textColor,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                fontSize = MaterialTheme.typography.labelLarge.fontSize
            )
        }
    }
}
