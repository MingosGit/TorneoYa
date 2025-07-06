package mingosgit.josecr.torneoya.ui.screens.usuario

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

@Composable
fun ConfirmarCorreoScreen(
    navController: NavController,
    correoElectronico: String,
    onVerificado: () -> Unit
) {
    var loading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Confirma tu correo",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Te hemos enviado un correo de confirmación a:\n$correoElectronico\n\nPor favor confirma y luego pulsa el botón de abajo.",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(24.dp))

        if (errorMsg != null) {
            Text(
                text = errorMsg ?: "",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        Button(
            enabled = !loading,
            onClick = {
                loading = true
                errorMsg = null
                // Recargar usuario y comprobar si ha verificado el correo
                val auth = FirebaseAuth.getInstance()
                val user = auth.currentUser
                if (user != null) {
                    user.reload()
                        .addOnCompleteListener { reloadTask ->
                            if (reloadTask.isSuccessful) {
                                if (user.isEmailVerified) {
                                    onVerificado()
                                } else {
                                    errorMsg = "Aún no has verificado tu correo."
                                }
                            } else {
                                errorMsg = reloadTask.exception?.localizedMessage ?: "Error al comprobar."
                            }
                            loading = false
                        }
                } else {
                    errorMsg = "Sesión expirada, vuelve a registrarte."
                    loading = false
                }
            }
        ) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp))
            } else {
                Text("Ya verificado")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedButton(
            onClick = {
                val auth = FirebaseAuth.getInstance()
                auth.currentUser?.sendEmailVerification()
                errorMsg = "Correo de verificación reenviado."
            },
        ) {
            Text("Reenviar correo de verificación")
        }
    }
}
