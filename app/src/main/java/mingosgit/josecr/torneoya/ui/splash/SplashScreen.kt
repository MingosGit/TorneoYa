package mingosgit.josecr.torneoya.ui.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import mingosgit.josecr.torneoya.viewmodel.usuario.GlobalUserViewModel

@Composable
fun SplashScreen(
    navController: NavController,
    globalUserViewModel: GlobalUserViewModel
) {
    val modernBackground = Brush.verticalGradient(
        0.0f to Color(0xFF1B1D29),
        0.28f to Color(0xFF212442),
        0.58f to Color(0xFF191A23),
        1.0f to Color(0xFF14151B)
    )
    val nombreUsuarioOnline by globalUserViewModel.nombreUsuarioOnline.collectAsState()
    var iniciadoCarga by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!iniciadoCarga) {
            iniciadoCarga = true
            globalUserViewModel.cargarNombreUsuarioOnlineSiSesionActiva()
        }
    }
    // Cuando ya hay nombre de usuario o está claro que no hay sesión, continúa
    LaunchedEffect(nombreUsuarioOnline) {
        if (nombreUsuarioOnline != null || nombreUsuarioOnline == null) {
            delay(700) // Un pequeño delay visual, opcional
            navController.navigate("home") {
                popUpTo(0)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = modernBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                shape = CircleShape,
                color = Color(0xFF296DFF).copy(alpha = 0.13f),
                shadowElevation = 0.dp,
                modifier = Modifier.size(85.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Logo",
                    tint = Color(0xFF296DFF),
                    modifier = Modifier.padding(22.dp)
                )
            }
            Spacer(Modifier.height(20.dp))
            Text(
                text = "Bienvenido a TorneoYa",
                fontSize = 27.sp,
                color = Color.White
            )
            Spacer(Modifier.height(36.dp))
            CircularProgressIndicator(
                color = Color(0xFF8F5CFF),
                strokeWidth = 3.dp,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}
