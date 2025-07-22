package mingosgit.josecr.torneoya.data.entities

enum class PermisoPartido {
    CREADOR,
    ADMINISTRADOR,
    ESPECTADOR
}

fun obtenerPermisoUsuarioEnPartido(
    partido: mingosgit.josecr.torneoya.data.firebase.PartidoFirebase,
    usuarioUid: String,
    administradores: List<String>
): PermisoPartido {
    return when {
        partido.creadorUid == usuarioUid -> PermisoPartido.CREADOR
        administradores.contains(usuarioUid) -> PermisoPartido.ADMINISTRADOR
        partido.usuariosConAcceso.contains(usuarioUid) -> PermisoPartido.ESPECTADOR
        else -> PermisoPartido.ESPECTADOR // fallback: puede que quieras devolver null o lanzar excepción
    }
}
