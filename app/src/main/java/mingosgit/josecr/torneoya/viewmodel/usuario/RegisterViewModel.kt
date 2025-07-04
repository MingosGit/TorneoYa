package mingosgit.josecr.torneoya.viewmodel.usuario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mingosgit.josecr.torneoya.repository.UsuarioAuthRepository
import mingosgit.josecr.torneoya.data.entities.UsuarioFirebaseEntity

class RegisterViewModel(
    private val repository: UsuarioAuthRepository
) : ViewModel() {

    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Empty)
    val registerState: StateFlow<RegisterState> = _registerState

    fun register(email: String, password: String, nombreUsuario: String) {
        _registerState.value = RegisterState.Loading
        viewModelScope.launch {
            val result = repository.register(email, password, nombreUsuario)
            _registerState.value = if (result.isSuccess) {
                RegisterState.Success(result.getOrThrow())
            } else {
                RegisterState.Error(result.exceptionOrNull()?.message ?: "Error desconocido")
            }
        }
    }

    fun clearState() {
        _registerState.value = RegisterState.Empty
    }
}

sealed class RegisterState {
    object Empty : RegisterState()
    object Loading : RegisterState()
    data class Success(val usuario: UsuarioFirebaseEntity) : RegisterState()
    data class Error(val message: String) : RegisterState()
}
