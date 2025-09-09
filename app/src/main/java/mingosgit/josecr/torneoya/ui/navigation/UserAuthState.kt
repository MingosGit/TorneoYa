package mingosgit.josecr.torneoya.ui.navigation

import androidx.compose.runtime.*
import com.google.firebase.auth.FirebaseAuth

/**
 * Hook Compose que expone el UID del usuario autenticado en Firebase.
 * - Escucha cambios de sesión con AuthStateListener.
 * - Devuelve un State<String?> con el uid actual (o null si no hay user).
 */
@Composable
fun rememberFirebaseUserUid(): State<String?> {
    return produceState<String?>(initialValue = FirebaseAuth.getInstance().currentUser?.uid) {
        val auth = FirebaseAuth.getInstance()
        // Listener que actualiza el valor cuando cambia el estado de autenticación
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            value = firebaseAuth.currentUser?.uid
        }
        auth.addAuthStateListener(listener)
        // Limpia el listener cuando se libera el efecto
        awaitDispose { auth.removeAuthStateListener(listener) }
    }
}
