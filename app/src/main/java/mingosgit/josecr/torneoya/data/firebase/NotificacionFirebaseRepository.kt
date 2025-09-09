package mingosgit.josecr.torneoya.data.firebase

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class NotificacionFirebaseRepository {
    private val db = FirebaseFirestore.getInstance()

    // Agrega una notificación a la colección "notificaciones"
    suspend fun agregarNotificacion(notificacion: NotificacionFirebase) {
        val datos = hashMapOf(
            "tipo" to notificacion.tipo,
            "titulo" to notificacion.titulo,
            "mensaje" to notificacion.mensaje,
            "fechaHora" to notificacion.fechaHora, // Timestamp
            "usuarioUid" to notificacion.usuarioUid
        )
        db.collection("notificaciones").add(datos).await()
    }

    // Obtiene notificaciones globales y del usuario, ordenadas por fecha desc
    suspend fun obtenerNotificaciones(usuarioUid: String): List<NotificacionFirebase> {
        val globalesTask = db.collection("notificaciones")
            .whereEqualTo("usuarioUid", null)
            .get()
        val personalesTask = db.collection("notificaciones")
            .whereEqualTo("usuarioUid", usuarioUid)
            .get()

        val globales = globalesTask.await().documents.map { it.toNotiSafe() }
        val personales = personalesTask.await().documents.map { it.toNotiSafe() }

        return (globales + personales).sortedByDescending { it.fechaHora.seconds }
    }

    // Obtiene todas las notificaciones, ordenadas por fecha desc
    suspend fun obtenerTodasNotificaciones(): List<NotificacionFirebase> {
        val res = db.collection("notificaciones").get().await()
        return res.documents.map { it.toNotiSafe() }
            .sortedByDescending { it.fechaHora.seconds }
    }

    // Borra una notificación por su uid de documento
    suspend fun borrarNotificacion(uid: String) {
        db.collection("notificaciones").document(uid).delete().await()
    }

    // --- Helpers ---

    // Convierte un DocumentSnapshot a NotificacionFirebase de forma segura
    private fun DocumentSnapshot.toNotiSafe(): NotificacionFirebase {
        val tipo = getString("tipo") ?: ""
        val titulo = getString("titulo") ?: ""
        val mensaje = getString("mensaje") ?: ""
        val fechaHora = getTimestamp("fechaHora") ?: Timestamp.now()
        val usuarioUid = getString("usuarioUid") // puede ser null (global)

        return NotificacionFirebase(
            uid = id,
            tipo = tipo,
            titulo = titulo,
            mensaje = mensaje,
            fechaHora = fechaHora,
            usuarioUid = usuarioUid
        )
    }
}
