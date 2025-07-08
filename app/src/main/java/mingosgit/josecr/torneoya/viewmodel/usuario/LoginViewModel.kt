package mingosgit.josecr.torneoya.viewmodel.usuario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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

    private val _resetPasswordState = MutableStateFlow<ResetPasswordState>(ResetPasswordState.Empty)
    val resetPasswordState: StateFlow<ResetPasswordState> = _resetPasswordState

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

    fun enviarCorreoRestablecerPassword(email: String) {
        _resetPasswordState.value = ResetPasswordState.Loading
        viewModelScope.launch {
            val result = repository.enviarCorreoRestablecerPassword(email)
            _resetPasswordState.value = if (result.isSuccess) {
                ResetPasswordState.Success
            } else {
                ResetPasswordState.Error(result.exceptionOrNull()?.message ?: "Error desconocido")
            }
        }
    }

    fun clearState() {
        _loginState.value = LoginState.Empty
        _resetPasswordState.value = ResetPasswordState.Empty
    }

    class Factory(
        private val repository: UsuarioAuthRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return LoginViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

sealed class LoginState {
    object Empty : LoginState()
    object Loading : LoginState()
    data class Success(val usuario: UsuarioFirebaseEntity) : LoginState()
    data class Error(val message: String) : LoginState()
}

sealed class ResetPasswordState {
    object Empty : ResetPasswordState()
    object Loading : ResetPasswordState()
    object Success : ResetPasswordState()
    data class Error(val message: String) : ResetPasswordState()
}
