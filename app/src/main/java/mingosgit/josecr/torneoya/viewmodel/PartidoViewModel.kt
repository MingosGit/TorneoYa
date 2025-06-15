package mingosgit.josecr.torneoya.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mingosgit.josecr.torneoya.data.entities.PartidoEntity
import mingosgit.josecr.torneoya.data.entities.EquipoEntity
import mingosgit.josecr.torneoya.data.entities.IntegranteEntity
import mingosgit.josecr.torneoya.repository.PartidosRepository
import mingosgit.josecr.torneoya.repository.EquiposRepository
import mingosgit.josecr.torneoya.repository.IntegrantesRepository

data class PartidoCompletoUI(
    val partido: PartidoEntity,
    val nombreEquipoLocal: String,
    val nombreEquipoVisitante: String,
    val integrantesLocal: List<String>,
    val integrantesVisitante: List<String>
)

class PartidoViewModel(
    private val partidosRepo: PartidosRepository,
    private val equiposRepo: EquiposRepository,
    private val integrantesRepo: IntegrantesRepository
) : ViewModel() {

    private val _ui = MutableStateFlow<PartidoCompletoUI?>(null)
    val ui: StateFlow<PartidoCompletoUI?> = _ui

    fun cargarPartidoCompleto(partidoId: Long) {
        viewModelScope.launch {
            val partido = partidosRepo.getPartidoById(partidoId) ?: return@launch
            val equipoLocal = equiposRepo.getEquipoById(partido.equipoLocalId)
            val equipoVisitante = equiposRepo.getEquipoById(partido.equipoVisitanteId)
            val integrantesLocal = equipoLocal?.let { integrantesRepo.getIntegrantesByEquipoId(it.id) } ?: emptyList()
            val integrantesVisitante = equipoVisitante?.let { integrantesRepo.getIntegrantesByEquipoId(it.id) } ?: emptyList()
            _ui.value = PartidoCompletoUI(
                partido = partido,
                nombreEquipoLocal = equipoLocal?.nombre ?: "Equipo local",
                nombreEquipoVisitante = equipoVisitante?.nombre ?: "Equipo visitante",
                integrantesLocal = integrantesLocal.map { it.nombre },
                integrantesVisitante = integrantesVisitante.map { it.nombre }
            )
        }
    }

    fun crearPartidoConIntegrantes(
        nombreEquipoLocal: String,
        nombresIntegrantesLocal: List<String>,
        nombreEquipoVisitante: String,
        nombresIntegrantesVisitante: List<String>,
        fecha: Long,
        torneoId: Long? = null,
        onFinish: () -> Unit
    ) {
        viewModelScope.launch {
            val equipoLocalId = equiposRepo.insertEquipo(EquipoEntity(nombre = nombreEquipoLocal))
            val equipoVisitanteId = equiposRepo.insertEquipo(EquipoEntity(nombre = nombreEquipoVisitante))
            val partido = PartidoEntity(
                equipoLocalId = equipoLocalId,
                equipoVisitanteId = equipoVisitanteId,
                fecha = fecha,
                torneoId = torneoId
            )
            partidosRepo.insertPartido(partido)
            val integrantesLocal = nombresIntegrantesLocal.map { nombre ->
                IntegranteEntity(equipoId = equipoLocalId, nombre = nombre)
            }
            val integrantesVisitante = nombresIntegrantesVisitante.map { nombre ->
                IntegranteEntity(equipoId = equipoVisitanteId, nombre = nombre)
            }
            integrantesRepo.insertIntegrantes(integrantesLocal)
            integrantesRepo.insertIntegrantes(integrantesVisitante)
            onFinish()
        }
    }
    fun actualizarNombresIntegrantes(
        nuevosLocal: List<String>,
        nuevosVisitante: List<String>,
        onFinish: () -> Unit
    ) {
        viewModelScope.launch {
            val ui = _ui.value ?: return@launch
            // Actualiza los nombres en la base de datos
            val equipoLocalId = ui.partido.equipoLocalId
            val equipoVisitanteId = ui.partido.equipoVisitanteId

            // Cargar los integrantes
            val antiguosLocal = integrantesRepo.getIntegrantesByEquipoId(equipoLocalId)
            val antiguosVisitante = integrantesRepo.getIntegrantesByEquipoId(equipoVisitanteId)

            // Borra todos y vuelve a insertar con los nuevos nombres (sencillo)
            // Si quieres hacer actualización individual, puedes hacerlo también.
            // Aquí eliminamos y agregamos.
            if (antiguosLocal.isNotEmpty()) {
                antiguosLocal.forEach { antiguo ->
                    integrantesRepo.eliminarIntegrante(antiguo)
                }
            }
            if (antiguosVisitante.isNotEmpty()) {
                antiguosVisitante.forEach { antiguo ->
                    integrantesRepo.eliminarIntegrante(antiguo)
                }
            }

            val nuevosLocalEntities = nuevosLocal.map { nombre ->
                mingosgit.josecr.torneoya.data.entities.IntegranteEntity(
                    equipoId = equipoLocalId,
                    nombre = nombre
                )
            }
            val nuevosVisitanteEntities = nuevosVisitante.map { nombre ->
                mingosgit.josecr.torneoya.data.entities.IntegranteEntity(
                    equipoId = equipoVisitanteId,
                    nombre = nombre
                )
            }

            integrantesRepo.insertIntegrantes(nuevosLocalEntities)
            integrantesRepo.insertIntegrantes(nuevosVisitanteEntities)

            cargarPartidoCompleto(ui.partido.id)
            onFinish()
        }
    }

    fun eliminarPartidoActual(onFinish: () -> Unit) {
        viewModelScope.launch {
            val ui = _ui.value ?: return@launch
            partidosRepo.deletePartido(ui.partido)
            onFinish()
        }
    }
}
