package mingosgit.josecr.torneoya.viewmodel.usuario

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mingosgit.josecr.torneoya.R
import mingosgit.josecr.torneoya.repository.UsuarioAuthRepository

private const val MIN_PASSWORD_LENGTH = 6

// ViewModel para alta de usuarios (registro) usando Firebase Auth
class RegisterViewModel(
    private val repository: UsuarioAuthRepository
) : ViewModel() {

    // Estado de registro expuesto a la UI
    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Empty)
    val registerState: StateFlow<RegisterState> = _registerState

    // Intenta registrar al usuario y actualiza el estado según validaciones y resultado del repo
    fun register(
        email: String,
        password: String,
        nombreUsuario: String,
        acceptedPrivacy: Boolean,
        privacyVersion: String,
        privacyUrl: String
    ) {
        if (!acceptedPrivacy) {
            _registerState.value = RegisterState.Error(R.string.register_privacy_required)
            return
        }
        if (!isPasswordValid(password)) {
            _registerState.value = RegisterState.Error(R.string.auth_password_requirements_generic)
            return
        }
        _registerState.value = RegisterState.Loading
        viewModelScope.launch {
            val result = repository.register(
                email = email,
                password = password,
                nombreUsuario = nombreUsuario,
                acceptedPrivacy = acceptedPrivacy,
                privacyVersion = privacyVersion,
                privacyUrl = privacyUrl
            )
            _registerState.value = if (result.isSuccess) {
                RegisterState.Success
            } else {
                RegisterState.Error(mapAuthExceptionToStringRes(result.exceptionOrNull()))
            }
        }
    }

    // Pone el estado a vacío para resetear mensajes en pantalla
    fun clearState() {
        _registerState.value = RegisterState.Empty
    }

    // Valida requisitos mínimos de contraseña
    private fun isPasswordValid(pwd: String): Boolean {
        if (pwd.length < MIN_PASSWORD_LENGTH) return false
        if (!pwd.any { it.isUpperCase() }) return false
        if (!pwd.any { it.isLowerCase() }) return false
        if (!pwd.any { it.isDigit() }) return false
        return true
    }

    // Traduce excepciones de Firebase a recursos de string para mostrar al usuario
    @StringRes
    private fun mapAuthExceptionToStringRes(throwable: Throwable?): Int {
        if (throwable == null) return R.string.auth_unknown_error

        return when (throwable) {
            is FirebaseAuthWeakPasswordException ->
                R.string.auth_password_requirements_generic
            is FirebaseAuthUserCollisionException ->
                R.string.auth_email_already_in_use
            is FirebaseAuthInvalidCredentialsException -> {
                when ((throwable as? FirebaseAuthException)?.errorCode) {
                    "ERROR_INVALID_EMAIL" -> R.string.auth_invalid_email
                    else -> R.string.auth_invalid_credentials
                }
            }
            is FirebaseAuthInvalidUserException ->
                R.string.auth_invalid_user
            is FirebaseAuthException -> {
                when (throwable.errorCode) {
                    "ERROR_NETWORK_REQUEST_FAILED" -> R.string.auth_network_error
                    "ERROR_TOO_MANY_REQUESTS" -> R.string.auth_too_many_requests
                    "ERROR_OPERATION_NOT_ALLOWED" -> R.string.auth_operation_not_allowed
                    else -> R.string.auth_generic_error
                }
            }
            else -> R.string.auth_unknown_error
        }
    }

    // Factory para inyectar el repositorio al crear el ViewModel
    class Factory(
        private val repository: UsuarioAuthRepository
    ) : ViewModelProvider.Factory {
        // Crea RegisterViewModel si coincide el tipo solicitado
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return RegisterViewModel(repository) as T
            }
            // Error si se pide un ViewModel desconocido
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

// Estados de la pantalla de registro
sealed class RegisterState {
    object Empty : RegisterState()   // Sin acción activa
    object Loading : RegisterState() // Operación en curso
    object Success : RegisterState() // Registro correcto
    data class Error(@StringRes val resId: Int) : RegisterState() // Error mapeado a string
}
