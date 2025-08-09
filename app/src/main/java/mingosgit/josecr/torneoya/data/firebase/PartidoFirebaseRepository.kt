package mingosgit.josecr.torneoya.data.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await
import kotlin.math.min

class PartidoFirebaseRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun listarPartidos(): List<PartidoFirebase> {
        val res = db.collection("partidos").get().await()
        return res.documents.mapNotNull {
            val partido = it.toObject(PartidoFirebase::class.java)
            partido?.copy(uid = it.id)
        }
    }

    // LISTA SOLO PARTIDOS CREADOS O DONDE TENGO ACCESO Y QUE ESTÁN EN PREVIA
    suspend fun listarPartidosPorUsuario(uid: String): List<PartidoFirebase> {
        val res = db.collection("partidos").get().await()
        val list = res.documents.mapNotNull {
            val partido = it.toObject(PartidoFirebase::class.java)?.copy(uid = it.id)
            if (partido != null) {
                android.util.Log.d("FIRE_PARTIDO", "creadorUid='${partido.creadorUid}' usuariosConAcceso=${partido.usuariosConAcceso}  (tu uid='$uid')")
                val creadorOK = partido.creadorUid == uid
                val accesoOK = partido.usuariosConAcceso.any { user -> user == uid }
                if (creadorOK || accesoOK) partido else null
            } else null
        }
        android.util.Log.d("FIRE_PARTIDO", "TOTAL ENCONTRADOS: ${list.size}")
        return list
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
            "usuariosConAcceso" to partido.usuariosConAcceso,
            "administradores" to partido.administradores // NUEVO CAMPO
        )
        db.collection("partidos").add(datos).await()
    }

    suspend fun quitarUsuarioDeAcceso(partidoUid: String, usuarioUid: String) {
        db.collection("partidos").document(partidoUid)
            .update("usuariosConAcceso", com.google.firebase.firestore.FieldValue.arrayRemove(usuarioUid))
            .await()
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
            "usuariosConAcceso" to partido.usuariosConAcceso,
            "administradores" to partido.administradores // NUEVO CAMPO
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

    // Añadir usuario a acceso del partido
    suspend fun agregarUsuarioAAcceso(partidoUid: String, userUid: String) {
        val partidoRef = db.collection("partidos").document(partidoUid)
        partidoRef.update(
            mapOf(
                "usuariosConAcceso" to com.google.firebase.firestore.FieldValue.arrayUnion(userUid)
            )
        ).await()
    }

    // Permisos: Administradores

    suspend fun agregarAdministrador(partidoUid: String, adminUid: String) {
        val partidoRef = db.collection("partidos").document(partidoUid)
        partidoRef.update(
            mapOf(
                "administradores" to com.google.firebase.firestore.FieldValue.arrayUnion(adminUid)
            )
        ).await()
    }

    suspend fun eliminarAdministrador(partidoUid: String, adminUid: String) {
        val partidoRef = db.collection("partidos").document(partidoUid)
        partidoRef.update(
            mapOf(
                "administradores" to com.google.firebase.firestore.FieldValue.arrayRemove(adminUid)
            )
        ).await()
    }

    suspend fun obtenerAdministradores(partidoUid: String): List<String> {
        val snap = db.collection("partidos").document(partidoUid).get().await()
        return snap.get("administradores") as? List<String> ?: emptyList()
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
            "opciones" to encuesta.opciones,
            "creadorNombre" to encuesta.creadorNombre
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

    // ====================== ELIMINACIÓN COMPLETA =========================

    /**
     * Solo el creador puede eliminar definitivamente el partido.
     * Elimina el partido y TODO su contenido asociado:
     * - Comentarios y sus votos
     * - Encuestas y sus votos
     * - Eventos y goleadores relacionados (si existen)
     * - Finalmente el documento del partido
     */
    suspend fun eliminarPartidoCompleto(partidoUid: String, solicitanteUid: String) {
        // Verificar que el solicitante es el creador
        val partidoSnap = db.collection("partidos").document(partidoUid).get().await()
        val creadorUid = partidoSnap.getString("creadorUid") ?: ""
        if (creadorUid.isBlank() || creadorUid != solicitanteUid) {
            throw SecurityException("Solo el creador del partido puede eliminarlo.")
        }

        // 1) Borrar comentarios y sus votos
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

        // 2) Borrar encuestas y sus votos
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

        // 3) Borrar eventos del partido si existen
        val eventos = db.collection("eventos")
            .whereEqualTo("partidoUid", partidoUid)
            .get().await()
        deleteDocsInChunks(eventos.documents.map { it.reference.path })

//        // 4) Borrar goleadores del partido si existen
//        val goles = db.collection("goleadores")
//            .whereEqualTo("partidoUid", partidoUid)
//            .get().await()
//        deleteDocsInChunks(goles.documents.map { it.reference.path })

        // 5) Borrar el partido
        db.collection("partidos").document(partidoUid).delete().await()
    }

    /**
     * Permite eliminar un comentario si lo solicita su autor o el creador del partido.
     * También elimina todos los votos asociados al comentario.
     */
    suspend fun eliminarComentarioSiAutorizado(comentarioUid: String, solicitanteUid: String) {
        // Obtener comentario
        val comentarioSnap = db.collection("comentarios").document(comentarioUid).get().await()
        if (!comentarioSnap.exists()) return

        val autorComentarioUid = comentarioSnap.getString("usuarioUid") ?: ""
        val partidoUid = comentarioSnap.getString("partidoUid") ?: ""

        // Obtener creador del partido
        val partidoSnap = db.collection("partidos").document(partidoUid).get().await()
        val creadorPartidoUid = partidoSnap.getString("creadorUid") ?: ""

        // Verificación de permisos: autor del comentario o creador del partido
        if (solicitanteUid != autorComentarioUid && solicitanteUid != creadorPartidoUid) {
            throw SecurityException("No tienes permisos para eliminar este comentario.")
        }

        // Borrar votos del comentario
        val votos = db.collection("comentario_votos")
            .whereEqualTo("comentarioUid", comentarioUid)
            .get().await()
        deleteDocsInChunks(votos.documents.map { it.reference.path })

        // Borrar comentario
        db.collection("comentarios").document(comentarioUid).delete().await()
    }

    // --- Helpers ---

    /**
     * Borra documentos a partir de sus rutas absolutas, troceando en lotes de 450 (límite de Firestore: 500 por batch).
     */
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
