package mingosgit.josecr.torneoya.data.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await
import kotlin.math.min

class PartidoFirebaseRepository {
    private val db = FirebaseFirestore.getInstance()

    // Lista todos los partidos
    suspend fun listarPartidos(): List<PartidoFirebase> {
        val res = db.collection("partidos").get().await()
        return res.documents.mapNotNull {
            val partido = it.toObject(PartidoFirebase::class.java)
            partido?.copy(uid = it.id)
        }
    }

    // Lista solo partidos creados por el usuario o donde tiene acceso
    suspend fun listarPartidosPorUsuario(uid: String): List<PartidoFirebase> {
        val res = db.collection("partidos").get().await()
        val list = res.documents.mapNotNull {
            val partido = it.toObject(PartidoFirebase::class.java)?.copy(uid = it.id)
            if (partido != null) {
                val creadorOK = partido.creadorUid == uid
                val accesoOK = partido.usuariosConAcceso.any { user -> user == uid }
                if (creadorOK || accesoOK) partido else null
            } else null
        }
        return list
    }

    // Crea un equipo y devuelve su uid
    suspend fun crearEquipo(equipo: EquipoFirebase): String {
        val datos = hashMapOf(
            "nombre" to equipo.nombre
        )
        val doc = db.collection("equipos").add(datos).await()
        return doc.id
    }

    // Crea un partido sin retorno de uid
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
            "usuariosConAcceso" to partido.usuariosConAcceso,
            "administradores" to partido.administradores
        )
        db.collection("partidos").add(datos).await()
    }

    // Quita un usuario de la lista de acceso de un partido
    suspend fun quitarUsuarioDeAcceso(partidoUid: String, usuarioUid: String) {
        db.collection("partidos").document(partidoUid)
            .update("usuariosConAcceso", com.google.firebase.firestore.FieldValue.arrayRemove(usuarioUid))
            .await()
    }

    // Crea un partido y devuelve su uid
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
            "usuariosConAcceso" to partido.usuariosConAcceso,
            "administradores" to partido.administradores
        )
        val doc = db.collection("partidos").add(datos).await()
        return doc.id
    }

    // Borra un partido por uid
    suspend fun borrarPartido(uid: String) {
        db.collection("partidos").document(uid).delete().await()
    }

    // Obtiene un partido por uid
    suspend fun obtenerPartido(uid: String): PartidoFirebase? {
        val snap = db.collection("partidos").document(uid).get().await()
        return snap.toObject(PartidoFirebase::class.java)?.copy(uid = snap.id)
    }

    // Obtiene un equipo por uid
    suspend fun obtenerEquipo(uid: String): EquipoFirebase? {
        val snap = db.collection("equipos").document(uid).get().await()
        return snap.toObject(EquipoFirebase::class.java)?.copy(uid = snap.id)
    }

    // Obtiene todos los jugadores
    suspend fun obtenerJugadores(): List<JugadorFirebase> {
        val res = db.collection("jugadores").get().await()
        return res.documents.mapNotNull {
            val jugador = it.toObject(JugadorFirebase::class.java)
            jugador?.copy(uid = it.id)
        }
    }

    // Actualiza los jugadores de un partido online
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

    // Agrega usuario a la lista de acceso de un partido
    suspend fun agregarUsuarioAAcceso(partidoUid: String, userUid: String) {
        val partidoRef = db.collection("partidos").document(partidoUid)
        partidoRef.update(
            mapOf(
                "usuariosConAcceso" to com.google.firebase.firestore.FieldValue.arrayUnion(userUid)
            )
        ).await()
    }

    // Agrega un administrador al partido
    suspend fun agregarAdministrador(partidoUid: String, adminUid: String) {
        val partidoRef = db.collection("partidos").document(partidoUid)
        partidoRef.update(
            mapOf(
                "administradores" to com.google.firebase.firestore.FieldValue.arrayUnion(adminUid)
            )
        ).await()
    }

    // Elimina un administrador del partido
    suspend fun eliminarAdministrador(partidoUid: String, adminUid: String) {
        val partidoRef = db.collection("partidos").document(partidoUid)
        partidoRef.update(
            mapOf(
                "administradores" to com.google.firebase.firestore.FieldValue.arrayRemove(adminUid)
            )
        ).await()
    }

    // Obtiene la lista de administradores de un partido
    suspend fun obtenerAdministradores(partidoUid: String): List<String> {
        val snap = db.collection("partidos").document(partidoUid).get().await()
        return snap.get("administradores") as? List<String> ?: emptyList()
    }

    // Obtiene los comentarios de un partido
    suspend fun obtenerComentarios(partidoUid: String): List<ComentarioFirebase> {
        val res = db.collection("comentarios")
            .whereEqualTo("partidoUid", partidoUid)
            .get().await()
        return res.documents.mapNotNull {
            val c = it.toObject(ComentarioFirebase::class.java)
            c?.copy(uid = it.id)
        }
    }

    // Obtiene el número de votos de un comentario por tipo
    suspend fun obtenerVotosComentario(comentarioUid: String, tipo: Int): Int {
        val res = db.collection("comentario_votos")
            .whereEqualTo("comentarioUid", comentarioUid)
            .whereEqualTo("tipo", tipo)
            .get().await()
        return res.size()
    }

    // Obtiene el voto de un usuario en un comentario
    suspend fun obtenerVotoUsuarioComentario(comentarioUid: String, usuarioUid: String): Int? {
        val res = db.collection("comentario_votos")
            .whereEqualTo("comentarioUid", comentarioUid)
            .whereEqualTo("usuarioUid", usuarioUid)
            .get().await()
        val doc = res.documents.firstOrNull()
        return doc?.getLong("tipo")?.toInt()
    }

    // Agrega un comentario
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

    // Vota un comentario, eliminando votos previos del mismo usuario
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

    // Obtiene las encuestas de un partido
    suspend fun obtenerEncuestas(partidoUid: String): List<EncuestaFirebase> {
        val res = db.collection("encuestas")
            .whereEqualTo("partidoUid", partidoUid)
            .get().await()
        return res.documents.mapNotNull {
            val e = it.toObject(EncuestaFirebase::class.java)
            e?.copy(uid = it.id)
        }
    }

    // Datos de conteo de votos por opción en encuesta
    data class VotosOpcion(val opcionIndex: Int, val votos: Int)

    // Obtiene los votos por opción de una encuesta
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

    // Agrega una encuesta
    suspend fun agregarEncuesta(encuesta: EncuestaFirebase) {
        val datos = hashMapOf(
            "partidoUid" to encuesta.partidoUid,
            "pregunta" to encuesta.pregunta,
            "opciones" to encuesta.opciones,
            "creadorNombre" to encuesta.creadorNombre
        )
        db.collection("encuestas").add(datos).await()
    }

    // Obtiene el voto de un usuario en una encuesta
    suspend fun obtenerVotoUsuarioEncuesta(encuestaUid: String, usuarioUid: String): Int? {
        val res = db.collection("encuesta_votos")
            .whereEqualTo("encuestaUid", encuestaUid)
            .whereEqualTo("usuarioUid", usuarioUid)
            .get().await()
        val doc = res.documents.firstOrNull()
        return doc?.getLong("opcionIndex")?.toInt()
    }

    // Vota en una encuesta, eliminando votos previos del mismo usuario
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

    // Elimina por completo un partido y sus datos asociados
    suspend fun eliminarPartidoCompleto(partidoUid: String, solicitanteUid: String) {
        val partidoSnap = db.collection("partidos").document(partidoUid).get().await()
        val creadorUid = partidoSnap.getString("creadorUid") ?: ""
        if (creadorUid.isBlank() || creadorUid != solicitanteUid) {
            throw SecurityException("Solo el creador del partido puede eliminarlo.")
        }

        val comentarios = db.collection("comentarios")
            .whereEqualTo("partidoUid", partidoUid)
            .get().await()
        for (comentario in comentarios.documents) {
            val comentarioId = comentario.id
            val votos = db.collection("comentario_votos")
                .whereEqualTo("comentarioUid", comentarioId)
                .get().await()
            deleteDocsInChunks(votos.documents.map { it.reference.path })
            comentario.reference.delete().await()
        }

        val encuestas = db.collection("encuestas")
            .whereEqualTo("partidoUid", partidoUid)
            .get().await()
        for (encuesta in encuestas.documents) {
            val encuestaId = encuesta.id
            val votos = db.collection("encuesta_votos")
                .whereEqualTo("encuestaUid", encuestaId)
                .get().await()
            deleteDocsInChunks(votos.documents.map { it.reference.path })
            encuesta.reference.delete().await()
        }

        val eventos = db.collection("eventos")
            .whereEqualTo("partidoUid", partidoUid)
            .get().await()
        deleteDocsInChunks(eventos.documents.map { it.reference.path })

        db.collection("partidos").document(partidoUid).delete().await()
    }

    // Elimina un comentario si lo solicita su autor o el creador del partido
    suspend fun eliminarComentarioSiAutorizado(comentarioUid: String, solicitanteUid: String) {
        val comentarioSnap = db.collection("comentarios").document(comentarioUid).get().await()
        if (!comentarioSnap.exists()) return

        val autorComentarioUid = comentarioSnap.getString("usuarioUid") ?: ""
        val partidoUid = comentarioSnap.getString("partidoUid") ?: ""

        val partidoSnap = db.collection("partidos").document(partidoUid).get().await()
        val creadorPartidoUid = partidoSnap.getString("creadorUid") ?: ""

        if (solicitanteUid != autorComentarioUid && solicitanteUid != creadorPartidoUid) {
            throw SecurityException("No tienes permisos para eliminar este comentario.")
        }

        val votos = db.collection("comentario_votos")
            .whereEqualTo("comentarioUid", comentarioUid)
            .get().await()
        deleteDocsInChunks(votos.documents.map { it.reference.path })

        db.collection("comentarios").document(comentarioUid).delete().await()
    }

    // Borra documentos en lotes para cumplir con el límite de Firestore
    private suspend fun deleteDocsInChunks(paths: List<String>) {
        var start = 0
        val chunkSize = 450
        while (start < paths.size) {
            val end = min(start + chunkSize, paths.size)
            val batch = db.batch()
            for (p in paths.subList(start, end)) {
                batch.delete(db.document(p))
            }
            batch.commit().await()
            start = end
        }
    }
}
