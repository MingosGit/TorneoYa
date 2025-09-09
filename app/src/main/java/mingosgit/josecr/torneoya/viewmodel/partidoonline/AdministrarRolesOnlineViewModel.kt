package mingosgit.josecr.torneoya.viewmodel.partidoonline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mingosgit.josecr.torneoya.data.firebase.PartidoFirebaseRepository
import mingosgit.josecr.torneoya.data.firebase.UsuarioFirebaseEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

// Modelo de usuario para la UI con rol (normal o admin)
data class UsuarioConRolUi(
    val uid: String,
    val nombre: String,
    val avatar: Int?, // avatar en vez de avatarUrl
    val esAdmin: Boolean
)

// ViewModel para administrar roles y accesos de un partido online
class AdministrarRolesOnlineViewModel(
    private val partidoUid: String,
    private val repo: PartidoFirebaseRepository
) : ViewModel() {

    // Estado: lista de usuarios con acceso (no admins)
    private val _usuariosConAcceso = MutableStateFlow<List<UsuarioConRolUi>>(emptyList())
    val usuariosConAcceso: StateFlow<List<UsuarioConRolUi>> = _usuariosConAcceso

    // Estado: lista de administradores
    private val _administradores = MutableStateFlow<List<UsuarioConRolUi>>(emptyList())
    val administradores: StateFlow<List<UsuarioConRolUi>> = _administradores

    init {
        cargarUsuarios() // Al iniciar, carga usuarios y separa por rol
    }

    // Carga el partido, separa uids de acceso y admins, y construye las listas enriquecidas con nombre/avatar
    fun cargarUsuarios() {
        viewModelScope.launch {
            val partido = repo.obtenerPartido(partidoUid)
            val accesoUids = partido?.usuariosConAcceso ?: emptyList()
            val adminUids = partido?.administradores ?: emptyList()

            val usuariosConAcceso = accesoUids
                .filter { it.isNotBlank() && !adminUids.contains(it) }
                .map { uid -> getUsuarioInfo(uid, false) }

            val administradores = adminUids
                .filter { it.isNotBlank() }
                .map { uid -> getUsuarioInfo(uid, true) }

            _usuariosConAcceso.value = usuariosConAcceso.mapNotNull { it }
            _administradores.value = administradores.mapNotNull { it }
        }
    }

    // Consulta Firestore para obtener nombre y avatar de un uid y lo empaqueta con el rol
    private suspend fun getUsuarioInfo(uid: String, esAdmin: Boolean): UsuarioConRolUi? {
        val snap = FirebaseFirestore.getInstance().collection("usuarios").document(uid).get().await()
        val entity = snap.toObject(UsuarioFirebaseEntity::class.java) ?: return null
        return UsuarioConRolUi(uid, entity.nombreUsuario, entity.avatar, esAdmin)
    }

    // Quita a un usuario de la lista de acceso del partido y refresca
    fun quitarUsuarioDeAcceso(uid: String) {
        viewModelScope.launch {
            repo.quitarUsuarioDeAcceso(partidoUid, uid)
            cargarUsuarios()
        }
    }

    // Concede rol de administrador a un usuario y refresca
    fun darRolAdministrador(uid: String) {
        viewModelScope.launch {
            repo.agregarAdministrador(partidoUid, uid)
            cargarUsuarios()
        }
    }

    // Revoca rol de administrador a un usuario y refresca
    fun quitarRolAdministrador(uid: String) {
        viewModelScope.launch {
            repo.eliminarAdministrador(partidoUid, uid)
            cargarUsuarios()
        }
    }

    // Elimina completamente a un usuario del partido (si es admin lo quita también) y refresca
    fun eliminarUsuarioCompletamente(uid: String) {
        viewModelScope.launch {
            repo.eliminarAdministrador(partidoUid, uid) // por si es admin
            repo.quitarUsuarioDeAcceso(partidoUid, uid)
            cargarUsuarios()
        }
    }
}
