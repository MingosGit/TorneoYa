package mingosgit.josecr.torneoya.viewmodel.usuario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mingosgit.josecr.torneoya.repository.UsuarioAuthRepository
import mingosgit.josecr.torneoya.data.entities.UsuarioFirebaseEntity

class LoginViewModel(
    private val repository: UsuarioAuthRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Empty)
    val loginState: StateFlow<LoginState> = _loginState

    fun login(email: String, password: String) {
        _loginState.value = LoginState.Loading
        viewModelScope.launch {
            val result = repository.login(email, password)
            _loginState.value = if (result.isSuccess) {
                LoginState.Success(result.getOrThrow())
            } else {
                LoginState.Error(result.exceptionOrNull()?.message ?: "Error desconocido")
            }
        }
    }

    fun clearState() {
        _loginState.value = LoginState.Empty
    }
}

sealed class LoginState {
    object Empty : LoginState()
    object Loading : LoginState()
    data class Success(val usuario: UsuarioFirebaseEntity) : LoginState()
    data class Error(val message: String) : LoginState()
}
