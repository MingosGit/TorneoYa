package mingosgit.josecr.torneoya.data.firebase

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class PartidoFirebase(
    val uid: String = "",
    val fecha: String = "",
    val horaInicio: String = "",
    val numeroPartes: Int = 2,
    val tiempoPorParte: Int = 25,
    val tiempoDescanso: Int = 5,
    val equipoAId: String = "",
    val equipoBId: String = "",
    val numeroJugadores: Int = 5,
    val estado: String = "PREVIA",
    val golesEquipoA: Int = 0,
    val golesEquipoB: Int = 0,
    val jugadoresEquipoA: List<String> = emptyList(), // UIDs
    val jugadoresEquipoB: List<String> = emptyList(), // UIDs
    val nombresManualEquipoA: List<String> = emptyList(), // NUEVO: nombres manuales (sin UID)
    val nombresManualEquipoB: List<String> = emptyList(), // NUEVO: nombres manuales (sin UID)
    val creadorUid: String = "",
    val isPublic: Boolean = true,
    val usuariosConAcceso: List<String> = emptyList(),
    val administradores: List<String> = emptyList()
)

@IgnoreExtraProperties
data class EquipoFirebase(
    @get:Exclude var uid: String = "",
    val nombre: String = ""
)

@IgnoreExtraProperties
data class JugadorFirebase(
    @get:Exclude var uid: String = "",
    val nombre: String = "",
    val email: String = "",
    val avatarUrl: String? = null
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
    val opciones: List<String> = emptyList(),
    val creadorNombre: String = ""
)

@IgnoreExtraProperties
data class NotificacionFirebase(
    @get:Exclude var uid: String = "",
    val tipo: String = "", // "parche" o "infraccion"
    val titulo: String = "",
    val mensaje: String = "",
    val fechaHora: String = "", // ISO 8601
    val usuarioUid: String? = null // si es para un usuario específico, si es null es global
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
    val tipo: String = "",
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
    val asistenciaJugadorUid: String? = null,
    val jugadorNombreManual: String? = null,
    val asistenciaNombreManual: String? = null
)



@IgnoreExtraProperties
data class PartidoEquipoJugadorFirebase(
    val partidoUid: String = "",
    val equipoUid: String = "",
    val jugadorUid: String = ""
)

@IgnoreExtraProperties
data class UsuarioFirebaseEntity(
    @get:Exclude var uid: String = "",
    val email: String = "",
    val nombreUsuario: String = "",
    val avatarUrl: String? = null
)
