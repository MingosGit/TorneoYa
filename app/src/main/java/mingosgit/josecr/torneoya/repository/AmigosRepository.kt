package mingosgit.josecr.torneoya.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import mingosgit.josecr.torneoya.data.entities.AmigoFirebaseEntity
import mingosgit.josecr.torneoya.data.entities.UsuarioFirebaseEntity

class AmigosRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    // Listar amigos
    suspend fun getAmigos(miUid: String): List<AmigoFirebaseEntity> {
        val snapshot = firestore.collection("usuarios")
            .document(miUid)
            .collection("amigos")
            .get()
            .await()
        return snapshot.toObjects(AmigoFirebaseEntity::class.java)
    }

    // Listar solicitudes recibidas
    suspend fun getSolicitudes(miUid: String): List<UsuarioFirebaseEntity> {
        val snapshot = firestore.collection("usuarios")
            .document(miUid)
            .collection("solicitudes_amistad")
            .get()
            .await()
        return snapshot.toObjects(UsuarioFirebaseEntity::class.java)
    }

    // Aceptar solicitud (agrega a ambos como amigos y borra solicitud)
    suspend fun aceptarSolicitud(miUid: String, amigo: UsuarioFirebaseEntity) {
        val miDoc = firestore.collection("usuarios").document(miUid).get().await()
        val miUsuario = miDoc.toObject(UsuarioFirebaseEntity::class.java)
        // Añadir cada uno a la lista del otro
        firestore.collection("usuarios").document(miUid)
            .collection("amigos").document(amigo.uid)
            .set(AmigoFirebaseEntity(amigo.uid, amigo.nombreUsuario)).await()
        firestore.collection("usuarios").document(amigo.uid)
            .collection("amigos").document(miUid)
            .set(AmigoFirebaseEntity(miUid, miUsuario?.nombreUsuario ?: "")).await()
        // Eliminar solicitud
        firestore.collection("usuarios")
            .document(miUid)
            .collection("solicitudes_amistad")
            .document(amigo.uid)
            .delete().await()
    }

    // Rechazar solicitud (solo borra la solicitud)
    suspend fun rechazarSolicitud(miUid: String, amigoUid: String) {
        firestore.collection("usuarios")
            .document(miUid)
            .collection("solicitudes_amistad")
            .document(amigoUid)
            .delete()
            .await()
    }

    // Eliminar amigo
    suspend fun eliminarAmigo(miUid: String, amigoUid: String) {
        firestore.collection("usuarios")
            .document(miUid)
            .collection("amigos")
            .document(amigoUid)
            .delete()
            .await()
        firestore.collection("usuarios")
            .document(amigoUid)
            .collection("amigos")
            .document(miUid)
            .delete()
            .await()
    }
}
