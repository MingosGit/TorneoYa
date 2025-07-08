package mingosgit.josecr.torneoya.data.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await

class PartidoFirebaseRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun listarPartidos(): List<PartidoFirebase> {
        val res = db.collection("partidos").get().await()
        return res.documents.mapNotNull {
            val partido = it.toObject(PartidoFirebase::class.java)
            partido?.copy(uid = it.id)
        }
    }

    suspend fun crearPartido(partido: PartidoFirebase) {
        val datos = hashMapOf(
            "fecha" to partido.fecha,
            "horaInicio" to partido.horaInicio,
            "numeroPartes" to partido.numeroPartes,
            "tiempoPorParte" to partido.tiempoPorParte,
            "tiempoDescanso" to partido.tiempoDescanso,
            "equipoAId" to partido.equipoAId,
            "equipoBId" to partido.equipoBId,
            "numeroJugadores" to partido.numeroJugadores,
            "estado" to partido.estado,
            "golesEquipoA" to partido.golesEquipoA,
            "golesEquipoB" to partido.golesEquipoB,
            "jugadoresUids" to partido.jugadoresUids,
            "creadorUid" to partido.creadorUid,
            "isPublic" to partido.isPublic
        )
        db.collection("partidos").add(datos).await()
    }

    suspend fun crearPartidoConRetornoUid(partido: PartidoFirebase): String {
        val datos = hashMapOf(
            "fecha" to partido.fecha,
            "horaInicio" to partido.horaInicio,
            "numeroPartes" to partido.numeroPartes,
            "tiempoPorParte" to partido.tiempoPorParte,
            "tiempoDescanso" to partido.tiempoDescanso,
            "equipoAId" to partido.equipoAId,
            "equipoBId" to partido.equipoBId,
            "numeroJugadores" to partido.numeroJugadores,
            "estado" to partido.estado,
            "golesEquipoA" to partido.golesEquipoA,
            "golesEquipoB" to partido.golesEquipoB,
            "jugadoresUids" to partido.jugadoresUids,
            "creadorUid" to partido.creadorUid,
            "isPublic" to partido.isPublic
        )
        val doc = db.collection("partidos").add(datos).await()
        return doc.id
    }

    suspend fun crearEquipo(equipo: EquipoFirebase): String {
        val datos = hashMapOf(
            "nombre" to equipo.nombre
        )
        val doc = db.collection("equipos").add(datos).await()
        return doc.id
    }

    suspend fun borrarPartido(uid: String) {
        db.collection("partidos").document(uid).delete().await()
    }

    suspend fun obtenerPartido(uid: String): PartidoFirebase? {
        val snap = db.collection("partidos").document(uid).get().await()
        return snap.toObject(PartidoFirebase::class.java)?.copy(uid = snap.id)
    }

    suspend fun obtenerEquipo(uid: String): EquipoFirebase? {
        val snap = db.collection("equipos").document(uid).get().await()
        return snap.toObject(EquipoFirebase::class.java)?.copy(uid = snap.id)
    }

    suspend fun obtenerJugadores(): List<JugadorFirebase> {
        val res = db.collection("jugadores").get().await()
        return res.documents.mapNotNull {
            val jugador = it.toObject(JugadorFirebase::class.java)
            jugador?.copy(uid = it.id)
        }
    }

    suspend fun actualizarJugadoresPartido(partidoUid: String, jugadoresUids: List<String>) {
        db.collection("partidos").document(partidoUid)
            .update("jugadoresUids", jugadoresUids)
            .await()
    }
}
