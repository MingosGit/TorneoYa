package mingosgit.josecr.torneoya.data.entities

data class UsuarioFirebaseEntity(
    val uid: String = "",
    val email: String = "",
    val nombreUsuario: String = "", // nombre de usuario único online
    val avatar: Int? = null, // null = sin foto de perfil, 1..20 = avatar local
    val partidosJugados: Int = 0
)