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

/**
 * Pantalla de splash.
 * - Comprueba usuario logueado y verificado.
 * - Carga nombre de usuario desde Firestore si procede.
 * - Navega a "home" y pasa el nombre a la home mediante setHomeLoaded.
 */
@Composable
fun SplashScreen(
    navController: NavHostController,
    setHomeLoaded: (nombreUsuario: String) -> Unit
) {
    val cs = MaterialTheme.colorScheme                           // Paleta de colores
    val gradient: Brush = TorneoYaPalette.backgroundGradient     // Degradado de fondo
    val context = LocalContext.current
    var error by remember { mutableStateOf<String?>(null) }      // Mensaje de error opcional

    // Efecto de arranque: decide destino y datos iniciales
    LaunchedEffect(Unit) {
        try {
            val user = FirebaseAuth.getInstance().currentUser    // Usuario actual de Firebase
            if (user != null && user.isEmailVerified) {          // Si hay usuario y correo verificado
                val db = FirebaseFirestore.getInstance()
                // Lee documento del usuario y extrae nombre
                val usuarioSnap = db.collection("usuarios").document(user.uid).get().await()
                val nombreUsuario = usuarioSnap.getString("nombreUsuario") ?: "Usuario"
                setHomeLoaded(nombreUsuario)                      // Inyecta nombre a la home
                navController.navigate("home") {                 // Navega limpiando back stack
                    popUpTo(0) { inclusive = true }
                }
            } else {
                setHomeLoaded("")                                 // Sin sesión: limpia nombre
                navController.navigate("home") {                 // Navega a home igualmente
                    popUpTo(0) { inclusive = true }
                }
            }
        } catch (e: Exception) {
            // Si falla conexión/lectura, muestra error breve y navega a home tras pequeña espera
            error = context.getString(R.string.splash_error_conexion)
            delay(1200)
            navController.navigate("home") {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    // Contenedor principal centrado con fondo en degradado
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient),
        contentAlignment = Alignment.Center
    ) {
        // Columna con título, loader y texto de estado/error
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = context.getString(R.string.splash_app_name), // Nombre de la app
                fontWeight = FontWeight.Black,
                fontSize = 34.sp,
                color = cs.onBackground
            )
            Spacer(Modifier.height(30.dp))
            CircularProgressIndicator(                              // Indicador de carga
                color = cs.primary,
                strokeWidth = 4.dp
            )
            Spacer(Modifier.height(18.dp))
            Text(
                text = error ?: context.getString(R.string.splash_iniciando_sesion), // Mensaje dinámico
                color = cs.mutedText,
                fontSize = 17.sp
            )
        }
    }
}
