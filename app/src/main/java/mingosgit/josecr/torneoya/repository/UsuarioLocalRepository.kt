package mingosgit.josecr.torneoya.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mingosgit.josecr.torneoya.data.dao.UsuarioLocalDao
import mingosgit.josecr.torneoya.data.entities.UsuarioLocalEntity

class UsuarioLocalRepository(private val usuarioLocalDao: UsuarioLocalDao) {

    suspend fun getUsuario(): UsuarioLocalEntity? = withContext(Dispatchers.IO) {
        usuarioLocalDao.getUsuario()
    }

    suspend fun guardarUsuario(usuario: UsuarioLocalEntity) = withContext(Dispatchers.IO) {
        usuarioLocalDao.insertUsuario(usuario.copy(id = 1))
    }

    suspend fun actualizarUsuario(usuario: UsuarioLocalEntity) = withContext(Dispatchers.IO) {
        usuarioLocalDao.insertUsuario(usuario.copy(id = 1))
    }
}
