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

    // NUEVO: LISTAR SOLO PARTIDOS CREADOS O DONDE TENGO ACCESO
    suspend fun listarPartidosPorUsuario(uid: String): List<PartidoFirebase> {
        val partidosCreados = db.collection("partidos")
            .whereEqualTo("creadorUid", uid)
            .get().await()
            .documents.mapNotNull {
                it.toObject(PartidoFirebase::class.java)?.copy(uid = it.id)
            }

        val partidosAcceso = db.collection("partidos")
            .whereArrayContains("usuariosConAcceso", uid)
            .get().await()
            .documents.mapNotNull {
                it.toObject(PartidoFirebase::class.java)?.copy(uid = it.id)
            }

        return (partidosCreados + partidosAcceso).distinctBy { it.uid }
    }

    suspend fun crearEquipo(equipo: EquipoFirebase): String {
        val datos = hashMapOf(
            "nombre" to equipo.nombre
        )
        val doc = db.collection("equipos").add(datos).await()
        return doc.id
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
            "jugadoresEquipoA" to partido.jugadoresEquipoA,
            "jugadoresEquipoB" to partido.jugadoresEquipoB,
            "nombresManualEquipoA" to partido.nombresManualEquipoA,
            "nombresManualEquipoB" to partido.nombresManualEquipoB,
            "creadorUid" to partido.creadorUid,
            "isPublic" to partido.isPublic,
            "usuariosConAcceso" to partido.usuariosConAcceso // NUEVO
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
            "jugadoresEquipoA" to partido.jugadoresEquipoA,
            "jugadoresEquipoB" to partido.jugadoresEquipoB,
            "nombresManualEquipoA" to partido.nombresManualEquipoA,
            "nombresManualEquipoB" to partido.nombresManualEquipoB,
            "creadorUid" to partido.creadorUid,
            "isPublic" to partido.isPublic,
            "usuariosConAcceso" to partido.usuariosConAcceso // NUEVO
        )
        val doc = db.collection("partidos").add(datos).await()
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

    suspend fun actualizarJugadoresPartidoOnline(
        partidoUid: String,
        jugadoresEquipoA: List<String>,
        nombresManualEquipoA: List<String>,
        jugadoresEquipoB: List<String>,
        nombresManualEquipoB: List<String>
    ) {
        db.collection("partidos").document(partidoUid)
            .update(
                mapOf(
                    "jugadoresEquipoA" to jugadoresEquipoA,
                    "nombresManualEquipoA" to nombresManualEquipoA,
                    "jugadoresEquipoB" to jugadoresEquipoB,
                    "nombresManualEquipoB" to nombresManualEquipoB
                )
            )
            .await()
    }

    // NUEVO: Añadir usuario a acceso del partido
    suspend fun agregarUsuarioAAcceso(partidoUid: String, userUid: String) {
        val partidoRef = db.collection("partidos").document(partidoUid)
        partidoRef.update("usuariosConAcceso", com.google.firebase.firestore.FieldValue.arrayUnion(userUid)).await()
    }

    // ====================== ONLINE =========================

    suspend fun obtenerComentarios(partidoUid: String): List<ComentarioFirebase> {
        val res = db.collection("comentarios")
            .whereEqualTo("partidoUid", partidoUid)
            .get().await()
        return res.documents.mapNotNull {
            val c = it.toObject(ComentarioFirebase::class.java)
            c?.copy(uid = it.id)
        }
    }

    suspend fun obtenerVotosComentario(comentarioUid: String, tipo: Int): Int {
        val res = db.collection("comentario_votos")
            .whereEqualTo("comentarioUid", comentarioUid)
            .whereEqualTo("tipo", tipo)
            .get().await()
        return res.size()
    }

    suspend fun obtenerVotoUsuarioComentario(comentarioUid: String, usuarioUid: String): Int? {
        val res = db.collection("comentario_votos")
            .whereEqualTo("comentarioUid", comentarioUid)
            .whereEqualTo("usuarioUid", usuarioUid)
            .get().await()
        val doc = res.documents.firstOrNull()
        return doc?.getLong("tipo")?.toInt()
    }

    suspend fun agregarComentario(comentario: ComentarioFirebase) {
        val datos = hashMapOf(
            "partidoUid" to comentario.partidoUid,
            "usuarioUid" to comentario.usuarioUid,
            "usuarioNombre" to comentario.usuarioNombre,
            "texto" to comentario.texto,
            "fechaHora" to comentario.fechaHora
        )
        db.collection("comentarios").add(datos).await()
    }

    suspend fun votarComentario(comentarioUid: String, usuarioUid: String, tipo: Int) {
        val prev = db.collection("comentario_votos")
            .whereEqualTo("comentarioUid", comentarioUid)
            .whereEqualTo("usuarioUid", usuarioUid)
            .get().await()
        for (d in prev.documents) db.collection("comentario_votos").document(d.id).delete().await()
        val datos = hashMapOf(
            "comentarioUid" to comentarioUid,
            "usuarioUid" to usuarioUid,
            "tipo" to tipo
        )
        db.collection("comentario_votos").add(datos).await()
    }

    suspend fun obtenerEncuestas(partidoUid: String): List<EncuestaFirebase> {
        val res = db.collection("encuestas")
            .whereEqualTo("partidoUid", partidoUid)
            .get().await()
        return res.documents.mapNotNull {
            val e = it.toObject(EncuestaFirebase::class.java)
            e?.copy(uid = it.id)
        }
    }

    data class VotosOpcion(val opcionIndex: Int, val votos: Int)

    suspend fun obtenerVotosPorOpcionEncuesta(encuestaUid: String, numOpciones: Int): List<VotosOpcion> {
        val res = db.collection("encuesta_votos")
            .whereEqualTo("encuestaUid", encuestaUid)
            .get().await()
        val counts = IntArray(numOpciones)
        for (d in res.documents) {
            val idx = d.getLong("opcionIndex")?.toInt() ?: continue
            if (idx in counts.indices) counts[idx]++
        }
        return counts.mapIndexed { i, c -> VotosOpcion(i, c) }
    }

    suspend fun agregarEncuesta(encuesta: EncuestaFirebase) {
        val datos = hashMapOf(
            "partidoUid" to encuesta.partidoUid,
            "pregunta" to encuesta.pregunta,
            "opciones" to encuesta.opciones
        )
        db.collection("encuestas").add(datos).await()
    }

    suspend fun obtenerVotoUsuarioEncuesta(encuestaUid: String, usuarioUid: String): Int? {
        val res = db.collection("encuesta_votos")
            .whereEqualTo("encuestaUid", encuestaUid)
            .whereEqualTo("usuarioUid", usuarioUid)
            .get().await()
        val doc = res.documents.firstOrNull()
        return doc?.getLong("opcionIndex")?.toInt()
    }

    suspend fun votarEncuestaUnico(encuestaUid: String, opcionIndex: Int, usuarioUid: String) {
        val prev = db.collection("encuesta_votos")
            .whereEqualTo("encuestaUid", encuestaUid)
            .whereEqualTo("usuarioUid", usuarioUid)
            .get().await()
        for (d in prev.documents) db.collection("encuesta_votos").document(d.id).delete().await()
        val datos = hashMapOf(
            "encuestaUid" to encuestaUid,
            "opcionIndex" to opcionIndex,
            "usuarioUid" to usuarioUid
        )
        db.collection("encuesta_votos").add(datos).await()
    }
}
