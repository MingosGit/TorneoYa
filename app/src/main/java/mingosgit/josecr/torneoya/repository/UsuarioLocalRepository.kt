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
        // Siempre guarda con id = 1 para mantener la unicidad
        usuarioLocalDao.insertUsuario(usuario.copy(id = 1))
    }

    suspend fun actualizarUsuario(usuario: UsuarioLocalEntity) = withContext(Dispatchers.IO) {
        // Asegura que siempre se actualice el usuario con id = 1
        usuarioLocalDao.insertUsuario(usuario.copy(id = 1))
    }
}
