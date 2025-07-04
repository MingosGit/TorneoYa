package mingosgit.josecr.torneoya.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import mingosgit.josecr.torneoya.data.entities.UsuarioFirebaseEntity

class UsuarioAuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    suspend fun isNombreUsuarioDisponible(nombreUsuario: String): Boolean {
        val query = firestore.collection("usuarios")
            .whereEqualTo("nombreUsuario", nombreUsuario)
            .get()
            .await()
        return query.isEmpty
    }

    suspend fun register(email: String, password: String, nombreUsuario: String): Result<Unit> {
        return try {
            if (!isNombreUsuarioDisponible(nombreUsuario)) {
                return Result.failure(Exception("El nombre de usuario ya existe"))
            }
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = authResult.user?.uid ?: throw Exception("No UID encontrado")
            val usuario = UsuarioFirebaseEntity(uid, email, nombreUsuario)
            firestore.collection("usuarios").document(uid).set(usuario).await()
            Result.success(Unit)
        } catch (e: Exception) {
            println("Register ERROR: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Result<UsuarioFirebaseEntity> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val uid = authResult.user?.uid ?: throw Exception("No UID encontrado")
            val usuarioSnap = firestore.collection("usuarios").document(uid).get().await()
            val usuario = usuarioSnap.toObject(UsuarioFirebaseEntity::class.java)
                ?: throw Exception("No se encontró el usuario en Firestore")
            Result.success(usuario)
        } catch (e: Exception) {
            println("Login ERROR: ${e.message}")
            Result.failure(e)
        }
    }
}
