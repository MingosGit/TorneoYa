package mingosgit.josecr.torneoya.data.firebase

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties

// Representa un partido en Firebase, con UID automático generado por Firestore
@IgnoreExtraProperties
data class PartidoFirebase(
    val uid: String = "",
    val fecha: String = "",
    val horaInicio: String = "",
    val numeroPartes: Int = 2,
    val tiempoPorParte: Int = 25,
    val tiempoDescanso: Int = 5,
    val equipoAId: String = "", // <-- SIEMPRE String
    val equipoBId: String = "", // <-- SIEMPRE String
    val numeroJugadores: Int = 5,
    val estado: String = "PREVIA",
    val golesEquipoA: Int = 0,
    val golesEquipoB: Int = 0
)


@IgnoreExtraProperties
data class EquipoFirebase(
    @get:Exclude var uid: String = "", // UID auto de Firestore
    val nombre: String = ""
)

@IgnoreExtraProperties
data class JugadorFirebase(
    @get:Exclude var uid: String = "",
    val nombre: String = "",
    val email: String = "",
    val avatarUrl: String? = null // Si tienes avatars online
)

@IgnoreExtraProperties
data class ComentarioFirebase(
    @get:Exclude var uid: String = "",
    val partidoUid: String = "",
    val usuarioUid: String = "",
    val usuarioNombre: String = "",
    val texto: String = "",
    val fechaHora: String = "" // ISO 8601
)

@IgnoreExtraProperties
data class ComentarioVotoFirebase(
    @get:Exclude var uid: String = "",
    val comentarioUid: String = "",
    val usuarioUid: String = "",
    val tipo: Int = 0 // 1=like, -1=dislike
)

@IgnoreExtraProperties
data class EncuestaFirebase(
    @get:Exclude var uid: String = "",
    val partidoUid: String = "",
    val pregunta: String = "",
    val opciones: List<String> = emptyList() // máx 5
)

@IgnoreExtraProperties
data class EncuestaVotoFirebase(
    @get:Exclude var uid: String = "",
    val encuestaUid: String = "",
    val opcionIndex: Int = 0,
    val usuarioUid: String = ""
)

@IgnoreExtraProperties
data class EventoFirebase(
    @get:Exclude var uid: String = "",
    val partidoUid: String = "",
    val tipo: String = "", // "GOL", etc.
    val minuto: Int? = null,
    val equipoUid: String = "",
    val jugadorUid: String = "",
    val asistenteUid: String? = null,
    val fechaHora: String = ""
)

@IgnoreExtraProperties
data class GoleadorFirebase(
    @get:Exclude var uid: String = "",
    val partidoUid: String = "",
    val equipoUid: String = "",
    val jugadorUid: String = "",
    val minuto: Int? = null,
    val asistenciaJugadorUid: String? = null
)

@IgnoreExtraProperties
data class PartidoEquipoJugadorFirebase(
    val partidoUid: String = "",
    val equipoUid: String = "",
    val jugadorUid: String = ""
)

// Si quieres mantener compatibilidad de usuario Firebase
@IgnoreExtraProperties
data class UsuarioFirebaseEntity(
    @get:Exclude var uid: String = "",
    val email: String = "",
    val nombreUsuario: String = "",
    val avatarUrl: String? = null
)
