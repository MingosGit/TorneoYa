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
import mingosgit.josecr.torneoya.data.entities.UsuarioFirebaseEntity
import mingosgit.josecr.torneoya.repository.UsuarioAuthRepository

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
                LoginState.Error(mapAuthExceptionToStringRes(result.exceptionOrNull()))
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
                ResetPasswordState.Error(mapAuthExceptionToStringRes(result.exceptionOrNull()))
            }
        }
    }

    fun clearState() {
        _loginState.value = LoginState.Empty
        _resetPasswordState.value = ResetPasswordState.Empty
    }

    @StringRes
    private fun mapAuthExceptionToStringRes(throwable: Throwable?): Int {
        if (throwable == null) return R.string.auth_unknown_error

        return when (throwable) {
            is FirebaseAuthWeakPasswordException ->
                R.string.auth_weak_password
            is FirebaseAuthInvalidCredentialsException -> {
                when ((throwable as? FirebaseAuthException)?.errorCode) {
                    "ERROR_INVALID_EMAIL" -> R.string.auth_invalid_email
                    "ERROR_WRONG_PASSWORD" -> R.string.auth_wrong_password
                    "ERROR_INVALID_VERIFICATION_CODE" -> R.string.auth_invalid_verification_code
                    "ERROR_INVALID_VERIFICATION_ID" -> R.string.auth_invalid_verification_id
                    else -> R.string.auth_invalid_credentials
                }
            }
            is FirebaseAuthInvalidUserException -> {
                when (throwable.errorCode) {
                    "ERROR_USER_DISABLED" -> R.string.auth_user_disabled
                    "ERROR_USER_NOT_FOUND" -> R.string.auth_user_not_found
                    "ERROR_USER_TOKEN_EXPIRED" -> R.string.auth_user_token_expired
                    else -> R.string.auth_invalid_user
                }
            }
            is FirebaseAuthUserCollisionException ->
                R.string.auth_user_collision
            is FirebaseAuthException -> {
                when (throwable.errorCode) {
                    "ERROR_NETWORK_REQUEST_FAILED" -> R.string.auth_network_error
                    "ERROR_TOO_MANY_REQUESTS" -> R.string.auth_too_many_requests
                    "ERROR_OPERATION_NOT_ALLOWED" -> R.string.auth_operation_not_allowed
                    "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL" ->
                        R.string.auth_account_exists_with_different_credential
                    else -> R.string.auth_generic_error
                }
            }
            else -> R.string.auth_unknown_error
        }
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
    data class Error(@StringRes val resId: Int) : LoginState()
}

sealed class ResetPasswordState {
    object Empty : ResetPasswordState()
    object Loading : ResetPasswordState()
    object Success : ResetPasswordState()
    data class Error(@StringRes val resId: Int) : ResetPasswordState()
}
