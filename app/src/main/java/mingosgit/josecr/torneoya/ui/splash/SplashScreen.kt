package mingosgit.josecr.torneoya.ui.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import mingosgit.josecr.torneoya.R
import mingosgit.josecr.torneoya.ui.theme.TorneoYaPalette
import mingosgit.josecr.torneoya.ui.theme.mutedText

@Composable
fun SplashScreen(
    navController: NavHostController,
    setHomeLoaded: (nombreUsuario: String) -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val gradient = TorneoYaPalette.backgroundGradient
    val context = LocalContext.current
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null && user.isEmailVerified) {
                val db = FirebaseFirestore.getInstance()
                val usuarioSnap = db.collection("usuarios").document(user.uid).get().await()
                val nombreUsuario = usuarioSnap.getString("nombreUsuario") ?: "Usuario"
                setHomeLoaded(nombreUsuario)
                navController.navigate("home") {
                    popUpTo(0) { inclusive = true }
                }
            } else {
                setHomeLoaded("")
                navController.navigate("home") {
                    popUpTo(0) { inclusive = true }
                }
            }
        } catch (e: Exception) {
            error = context.getString(R.string.splash_error_conexion)
            delay(1200)
            navController.navigate("home") {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = context.getString(R.string.splash_app_name),
                fontWeight = FontWeight.Black,
                fontSize = 34.sp,
                color = cs.onBackground
            )
            Spacer(Modifier.height(30.dp))
            CircularProgressIndicator(
                color = cs.primary,
                strokeWidth = 4.dp
            )
            Spacer(Modifier.height(18.dp))
            Text(
                text = error ?: context.getString(R.string.splash_iniciando_sesion),
                color = cs.mutedText,
                fontSize = 17.sp
            )
        }
    }
}
