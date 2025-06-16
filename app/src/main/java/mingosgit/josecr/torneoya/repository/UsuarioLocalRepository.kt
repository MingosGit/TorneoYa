package mingosgit.josecr.torneoya.repository

import mingosgit.josecr.torneoya.data.dao.UsuarioLocalDao
import mingosgit.josecr.torneoya.data.entities.UsuarioLocalEntity

class UsuarioLocalRepository(private val dao: UsuarioLocalDao) {
    suspend fun getUsuario(): UsuarioLocalEntity? = dao.getUsuario()
    suspend fun guardarUsuario(usuario: UsuarioLocalEntity) = dao.insertUsuario(usuario)
    suspend fun actualizarUsuario(usuario: UsuarioLocalEntity) = dao.updateUsuario(usuario)
}
