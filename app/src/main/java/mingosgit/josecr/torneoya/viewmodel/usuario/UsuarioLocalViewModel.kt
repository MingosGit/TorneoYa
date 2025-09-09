package mingosgit.josecr.torneoya.viewmodel.usuario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import mingosgit.josecr.torneoya.data.entities.UsuarioLocalEntity
import mingosgit.josecr.torneoya.repository.UsuarioLocalRepository

// ViewModel para gestionar el usuario local (nombre y foto)
class UsuarioLocalViewModel(
    private val repository: UsuarioLocalRepository
) : ViewModel() {

    // Estado: usuario local cargado (o nulo si aún no se ha creado)
    private val _usuario = MutableStateFlow<UsuarioLocalEntity?>(null)
    val usuario: StateFlow<UsuarioLocalEntity?> = _usuario

    // Carga el usuario local; si no existe, crea uno por defecto y lo guarda
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

    // Actualiza solo la ruta de la foto manteniendo el nombre existente (o uno por defecto)
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
