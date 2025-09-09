package mingosgit.josecr.torneoya.viewmodel.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import mingosgit.josecr.torneoya.data.firebase.NotificacionFirebase
import mingosgit.josecr.torneoya.data.firebase.NotificacionFirebaseRepository
import mingosgit.josecr.torneoya.data.database.AppDatabase
import mingosgit.josecr.torneoya.data.entities.NotificacionArchivadaEntity
import mingosgit.josecr.torneoya.data.entities.NotificacionBorradaEntity

class NotificacionesViewModel(
    private val usuarioUid: String,
    app: Application,
    private val repo: NotificacionFirebaseRepository = NotificacionFirebaseRepository()
) : AndroidViewModel(app) {

    private val _noLeidas = MutableStateFlow<List<NotificacionFirebase>>(emptyList())
    val noLeidas: StateFlow<List<NotificacionFirebase>> = _noLeidas.asStateFlow()

    private val _leidas = MutableStateFlow<List<NotificacionFirebase>>(emptyList())
    val leidas: StateFlow<List<NotificacionFirebase>> = _leidas.asStateFlow()

    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando.asStateFlow()

    private val dao = AppDatabase.getInstance(app).notificacionArchivadaDao()
    private val borradasDao = AppDatabase.getInstance(app).notificacionBorradaDao()

    init {
        cargarNotificaciones()
    }

    fun cargarNotificaciones() {
        viewModelScope.launch {
            _cargando.value = true
            try {
                val todas = repo.obtenerNotificaciones(usuarioUid)
                val archivadasUids = dao.getArchivadasUids().toSet()
                val borradasUids = borradasDao.getBorradasUids().toSet()
                _noLeidas.value = todas.filter { it.uid !in archivadasUids && it.uid !in borradasUids }
                _leidas.value = todas.filter { it.uid in archivadasUids && it.uid !in borradasUids }
            } catch (e: Exception) {
                _noLeidas.value = emptyList()
                _leidas.value = emptyList()
            } finally {
                _cargando.value = false
            }
        }
    }

    fun archivarNotificacion(uid: String) {
        viewModelScope.launch {
            dao.archivar(NotificacionArchivadaEntity(notificacionUid = uid))
            cargarNotificaciones()
        }
    }

    fun borrarNotificacion(uid: String) {
        viewModelScope.launch {
            borradasDao.borrar(NotificacionBorradaEntity(notificacionUid = uid))
            dao.limpiarNotificacion(uid)
            cargarNotificaciones()
        }
    }
}
