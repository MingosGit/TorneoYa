package mingosgit.josecr.torneoya.data.entities

data class UsuarioFirebaseEntity(
    val uid: String = "",
    val email: String = "",
    val nombreUsuario: String = "", // nombre de usuario único online
    val avatarUrl: String? = null,
    val partidosJugados: Int = 0,   // NUEVO CAMPO
)