package mingosgit.josecr.torneoya.data.entities

import com.google.firebase.Timestamp

data class UsuarioFirebaseEntity(
    val uid: String = "",
    val email: String = "",
    val nombreUsuario: String = "", // nombre de usuario único online
    val avatar: Int? = null, // null = sin foto de perfil, 1..20 = avatar local
    val partidosJugados: Int = 0,

    val acceptedPrivacy: Boolean = false,           // ¿aceptó?
    val acceptedPrivacyAt: Timestamp? = null,       // cuándo aceptó (servidor)
    val privacyVersion: String? = null,             // versión del texto aceptado (ej. "2025-08-11")
    val privacyUrl: String? = null                  // URL pública mostrada al aceptar
)