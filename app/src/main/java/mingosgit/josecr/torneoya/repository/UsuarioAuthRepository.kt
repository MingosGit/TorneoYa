package mingosgit.josecr.torneoya.repository

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import mingosgit.josecr.torneoya.data.entities.UsuarioFirebaseEntity

class UsuarioAuthRepository(
    val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    suspend fun isNombreUsuarioDisponible(nombreUsuario: String): Boolean {
        val query = firestore.collection("usuarios")
            .whereEqualTo("nombreUsuario", nombreUsuario)
            .get()
            .await()
        return query.isEmpty
    }

    suspend fun getUsuarioByUid(uid: String): UsuarioFirebaseEntity? {
        val snap = firestore.collection("usuarios").document(uid).get().await()
        return snap.toObject(UsuarioFirebaseEntity::class.java)
    }

    /**
     * REGISTRO con trazabilidad de aceptación de privacidad.
     * Debes pasar:
     * - acceptedPrivacy=true si el usuario marcó la casilla.
     * - privacyVersion: por ejemplo "2025-08-11" (sincroniza con tu HTML).
     * - privacyUrl: por ejemplo "https://mingosgit.github.io/privacy-policy.html".
     */
    suspend fun register(
        email: String,
        password: String,
        nombreUsuario: String,
        acceptedPrivacy: Boolean,
        privacyVersion: String,
        privacyUrl: String
    ): Result<Unit> {
        return try {
            if (!isNombreUsuarioDisponible(nombreUsuario)) {
                return Result.failure(Exception("El nombre de usuario ya existe"))
            }
            if (!acceptedPrivacy) {
                return Result.failure(Exception("Debes aceptar la Política de Privacidad"))
            }

            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = authResult.user?.uid ?: throw Exception("No UID encontrado")

            val usuario = UsuarioFirebaseEntity(
                uid = uid,
                email = email,
                nombreUsuario = nombreUsuario,
                avatar = null,
                partidosJugados = 0,
                acceptedPrivacy = true,
                acceptedPrivacyAt = Timestamp.now(),
                privacyVersion = privacyVersion,
                privacyUrl = privacyUrl
            )

            firestore.collection("usuarios").document(uid).set(usuario).await()

            // ENVÍA CORREO DE VERIFICACIÓN AL USUARIO
            authResult.user?.sendEmailVerification()?.await()

            Result.success(Unit)
        } catch (e: Exception) {
            println("Register ERROR: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Result<UsuarioFirebaseEntity> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val user = authResult.user
            if (user != null && !user.isEmailVerified) {
                auth.signOut()
                throw Exception("Debes verificar tu correo antes de iniciar sesión. Revisa tu email.")
            }
            val uid = user?.uid ?: throw Exception("No UID encontrado")
            val usuarioSnap = firestore.collection("usuarios").document(uid).get().await()
            val usuario = usuarioSnap.toObject(UsuarioFirebaseEntity::class.java)
                ?: throw Exception("No se encontró el usuario en Firestore")
            Result.success(usuario)
        } catch (e: Exception) {
            println("Login ERROR: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun enviarCorreoRestablecerPassword(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
