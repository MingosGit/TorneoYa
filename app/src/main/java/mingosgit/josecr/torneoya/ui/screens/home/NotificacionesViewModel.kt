package mingosgit.josecr.torneoya.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mingosgit.josecr.torneoya.data.firebase.NotificacionFirebase
import mingosgit.josecr.torneoya.data.firebase.NotificacionFirebaseRepository

class NotificacionesViewModel(
    private val usuarioUid: String,
    private val repo: NotificacionFirebaseRepository = NotificacionFirebaseRepository()
) : ViewModel() {
    private val _notificaciones = MutableStateFlow<List<NotificacionFirebase>>(emptyList())
    val notificaciones: StateFlow<List<NotificacionFirebase>> = _notificaciones.asStateFlow()

    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando.asStateFlow()

    init {
        cargarNotificaciones()
    }

    fun cargarNotificaciones() {
        viewModelScope.launch {
            _cargando.value = true
            try {
                _notificaciones.value = repo.obtenerNotificaciones(usuarioUid)
            } catch (e: Exception) {
                _notificaciones.value = emptyList()
            } finally {
                _cargando.value = false
            }
        }
    }
}
