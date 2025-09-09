package mingosgit.josecr.torneoya.data.firebase

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.Timestamp

// Representa un partido almacenado en Firebase
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
    val jugadoresEquipoA: List<String> = emptyList(), // Jugadores Online equipo A
    val jugadoresEquipoB: List<String> = emptyList(), // jugadores Online equipo B
    val nombresManualEquipoA: List<String> = emptyList(), // Nombres manuales equipo A
    val nombresManualEquipoB: List<String> = emptyList(), // Nombres manuales equipo B
    val creadorUid: String = "",
    val isPublic: Boolean = true,
    val usuariosConAcceso: List<String> = emptyList(),
    val administradores: List<String> = emptyList()
)

// Representa un equipo en Firebase
@IgnoreExtraProperties
data class EquipoFirebase(
    @get:Exclude var uid: String = "",
    val nombre: String = ""
)

// Representa un jugador en Firebase
@IgnoreExtraProperties
data class JugadorFirebase(
    @get:Exclude var uid: String = "",
    val nombre: String = "",
    val email: String = "",
    val avatar: Int? = null // null = sin foto de perfil, 1..20 = avatar local
)

// Representa un comentario en Firebase
@IgnoreExtraProperties
data class ComentarioFirebase(
    @get:Exclude var uid: String = "",
    val partidoUid: String = "",
    val usuarioUid: String = "",
    val usuarioNombre: String = "",
    val texto: String = "",
    val fechaHora: String = "" // ISO 8601
)

// Representa una encuesta en Firebase
@IgnoreExtraProperties
data class EncuestaFirebase(
    @get:Exclude var uid: String = "",
    val partidoUid: String = "",
    val pregunta: String = "",
    val opciones: List<String> = emptyList(),
    val creadorNombre: String = "",
    val creadorUid: String = ""
)

// Representa una notificación en Firebase
@IgnoreExtraProperties
data class NotificacionFirebase(
    @get:Exclude var uid: String = "",
    val tipo: String = "",
    val titulo: String = "",
    val mensaje: String = "",
    val fechaHora: Timestamp = Timestamp.now(), // Fecha y hora de Firebase
    val usuarioUid: String? = null
)

// Representa un goleador en Firebase
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

// Representa un usuario en Firebase
data class UsuarioFirebaseEntity(
    val uid: String = "",
    val email: String = "",
    val nombreUsuario: String = "", // nombre de usuario único online
    val avatar: Int? = null, // null = sin foto de perfil, 1..20 = avatar local
    val partidosJugados: Int = 0,

    // Datos sobre aceptación de privacidad
    val acceptedPrivacy: Boolean = false,           // si aceptó
    val acceptedPrivacyAt: Timestamp? = null,       // cuándo aceptó
    val privacyVersion: String? = null,             // versión aceptada
    val privacyUrl: String? = null                  // URL pública de privacidad
)
