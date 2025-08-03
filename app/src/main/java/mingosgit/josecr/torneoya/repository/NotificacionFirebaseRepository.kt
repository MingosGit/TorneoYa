package mingosgit.josecr.torneoya.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import mingosgit.josecr.torneoya.data.firebase.NotificacionFirebase

class NotificacionFirebaseRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun agregarNotificacion(notificacion: NotificacionFirebase) {
        val datos = hashMapOf(
            "tipo" to notificacion.tipo,
            "titulo" to notificacion.titulo,
            "mensaje" to notificacion.mensaje,
            "fechaHora" to notificacion.fechaHora,
            "usuarioUid" to notificacion.usuarioUid
        )
        db.collection("notificaciones").add(datos).await()
    }

    // Para usuario normal: trae todas las globales + las dirigidas al usuario
    suspend fun obtenerNotificaciones(usuarioUid: String): List<NotificacionFirebase> {
        val globalesTask = db.collection("notificaciones")
            .whereEqualTo("usuarioUid", null)
            .get()
        val personalesTask = db.collection("notificaciones")
            .whereEqualTo("usuarioUid", usuarioUid)
            .get()

        val globales = globalesTask.await().documents.mapNotNull {
            it.toObject(NotificacionFirebase::class.java)?.copy(uid = it.id)
        }
        val personales = personalesTask.await().documents.mapNotNull {
            it.toObject(NotificacionFirebase::class.java)?.copy(uid = it.id)
        }
        return (globales + personales).sortedByDescending { it.fechaHora }
    }

    // Para admin: trae todas (opcional)
    suspend fun obtenerTodasNotificaciones(): List<NotificacionFirebase> {
        val res = db.collection("notificaciones").get().await()
        return res.documents.mapNotNull {
            it.toObject(NotificacionFirebase::class.java)?.copy(uid = it.id)
        }
    }

    suspend fun borrarNotificacion(uid: String) {
        db.collection("notificaciones").document(uid).delete().await()
    }
}
