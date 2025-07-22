package mingosgit.josecr.torneoya.ui.navigation

import androidx.compose.runtime.*
import com.google.firebase.auth.FirebaseAuth

@Composable
fun rememberFirebaseUserUid(): State<String?> {
    return produceState<String?>(initialValue = FirebaseAuth.getInstance().currentUser?.uid) {
        val auth = FirebaseAuth.getInstance()
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            value = firebaseAuth.currentUser?.uid
        }
        auth.addAuthStateListener(listener)
        awaitDispose { auth.removeAuthStateListener(listener) }
    }
}
