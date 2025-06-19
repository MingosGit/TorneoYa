package mingosgit.josecr.torneoya.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import mingosgit.josecr.torneoya.data.entities.UsuarioLocalEntity
import mingosgit.josecr.torneoya.repository.UsuarioLocalRepository

class UsuarioLocalViewModel(
    private val repository: UsuarioLocalRepository
) : ViewModel() {

    private val _usuario = MutableStateFlow<UsuarioLocalEntity?>(null)
    val usuario: StateFlow<UsuarioLocalEntity?> = _usuario

    fun cargarUsuario() {
        viewModelScope.launch {
            var user = repository.getUsuario()
            if (user == null) {
                user = UsuarioLocalEntity(id = 1, nombre = "Usuario1", fotoPerfilPath = null)
                repository.guardarUsuario(user)
            }
            _usuario.value = user
        }
    }

    fun cambiarNombre(nuevoNombre: String) {
        viewModelScope.launch {
            val actual = _usuario.value
            val user = UsuarioLocalEntity(
                id = 1,
                nombre = nuevoNombre,
                fotoPerfilPath = actual?.fotoPerfilPath // mantiene la foto si existe
            )
            repository.actualizarUsuario(user)
            _usuario.value = user
        }
    }

    fun cambiarFotoPerfil(path: String) {
        viewModelScope.launch {
            val actual = _usuario.value
            val user = UsuarioLocalEntity(
                id = 1,
                nombre = actual?.nombre ?: "Usuario1",
                fotoPerfilPath = path
            )
            repository.actualizarUsuario(user)
            _usuario.value = user
        }
    }
}
