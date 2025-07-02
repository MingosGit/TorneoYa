package mingosgit.josecr.torneoya.util

import android.app.Activity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object FirebaseUtils {
    fun provideAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    fun provideFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    fun loginUser(
        email: String,
        password: String,
        onResult: (Boolean, String?, FirebaseUser?) -> Unit
    ) {
        val auth = provideAuth()
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                onResult(true, null, it.user)
            }
            .addOnFailureListener { e ->
                onResult(false, e.message, null)
            }
    }

    fun registerUser(
        username: String,
        email: String,
        password: String,
        onResult: (Boolean, String?, FirebaseUser?) -> Unit
    ) {
        val auth = provideAuth()
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val user = result.user
                if (user != null) {
                    val profileUpdate = UserProfileChangeRequest.Builder()
                        .setDisplayName(username)
                        .build()
                    user.updateProfile(profileUpdate)
                        .addOnCompleteListener {
                            onResult(it.isSuccessful, if (!it.isSuccessful) "No se pudo actualizar nombre" else null, user)
                        }
                } else {
                    onResult(false, "Error inesperado", null)
                }
            }
            .addOnFailureListener { e ->
                onResult(false, e.message, null)
            }
    }

    fun updateFirebaseUserName(user: FirebaseUser, name: String) {
        val profileUpdate = UserProfileChangeRequest.Builder()
            .setDisplayName(name)
            .build()
        user.updateProfile(profileUpdate)
    }

    fun saveUsernameToFirestore(uid: String, username: String) {
        val db = provideFirestore()
        db.collection("users").document(uid).set(mapOf("username" to username))
    }

    suspend fun getUsernameFromFirestore(uid: String): String? {
        val db = provideFirestore()
        val snap = db.collection("users").document(uid).get().await()
        return snap.getString("username")
    }
}
