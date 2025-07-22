package mingosgit.josecr.torneoya.data.entities

fun PermisoPartido.puedeGestionarPartido() = this == PermisoPartido.CREADOR || this == PermisoPartido.ADMINISTRADOR
fun PermisoPartido.puedeVerEncuestasYComentar() = true
fun PermisoPartido.puedeAgregarEliminarMiembros() = this == PermisoPartido.CREADOR || this == PermisoPartido.ADMINISTRADOR
fun PermisoPartido.puedeGestionarEventos() = this == PermisoPartido.CREADOR || this == PermisoPartido.ADMINISTRADOR
fun PermisoPartido.puedeVerAdministradores() = this == PermisoPartido.CREADOR
