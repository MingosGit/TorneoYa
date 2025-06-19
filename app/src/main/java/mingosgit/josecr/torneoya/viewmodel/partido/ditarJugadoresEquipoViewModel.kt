package mingosgit.josecr.torneoya.viewmodel.partido

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mingosgit.josecr.torneoya.data.entities.PartidoEquipoJugadorEntity
import mingosgit.josecr.torneoya.repository.JugadorRepository
import mingosgit.josecr.torneoya.repository.PartidoEquipoJugadorRepository
import mingosgit.josecr.torneoya.repository.EquipoRepository
import kotlin.random.Random

class EditarJugadoresEquipoViewModel(
    private val partidoId: Long,
    private val equipoAId: Long,
    private val equipoBId: Long,
    private val jugadorRepository: JugadorRepository,
    private val relacionRepository: PartidoEquipoJugadorRepository,
    private val equipoRepository: EquipoRepository
) : ViewModel() {

    private val _equipoA = MutableStateFlow<String?>(null)
    val equipoA: StateFlow<String?> = _equipoA

    private val _equipoB = MutableStateFlow<String?>(null)
    val equipoB: StateFlow<String?> = _equipoB

    private val _nombresA = MutableStateFlow<List<String>>(listOf(""))
    val nombresA: StateFlow<List<String>> = _nombresA

    private val _nombresB = MutableStateFlow<List<String>>(listOf(""))
    val nombresB: StateFlow<List<String>> = _nombresB

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _guardado = MutableStateFlow(false)
    val guardado: StateFlow<Boolean> = _guardado

    init {
        cargar()
    }

    fun onNombreAChange(idx: Int, valor: String) {
        val actual = _nombresA.value.toMutableList()
        if (valor.isEmpty()) {
            // Eliminar recuadro si se borra el nombre y hay más de uno
            if (actual.size > 1) {
                actual.removeAt(idx)
            } else {
                actual[idx] = ""
            }
        } else {
            actual[idx] = valor
            // Si es el último, agregar otro vacío
            if (idx == actual.size - 1) {
                actual.add("")
            }
        }
        _nombresA.value = actual
    }

    fun onNombreBChange(idx: Int, valor: String) {
        val actual = _nombresB.value.toMutableList()
        if (valor.isEmpty()) {
            // Eliminar recuadro si se borra el nombre y hay más de uno
            if (actual.size > 1) {
                actual.removeAt(idx)
            } else {
                actual[idx] = ""
            }
        } else {
            actual[idx] = valor
            // Si es el último, agregar otro vacío
            if (idx == actual.size - 1) {
                actual.add("")
            }
        }
        _nombresB.value = actual
    }

    fun randomizar() {
        val todos = (_nombresA.value + _nombresB.value)
            .map { it.trim() }
            .filter { it.isNotBlank() }
        val barajado = todos.shuffled(Random(System.currentTimeMillis()))
        val n = barajado.size
        val mitad = n / 2
        if (n == 0) {
            _nombresA.value = listOf("")
            _nombresB.value = listOf("")
            return
        }
        if (n % 2 == 0) {
            _nombresA.value = barajado.take(mitad) + listOf("")
            _nombresB.value = barajado.drop(mitad) + listOf("")
        } else {
            val extraA = Random.nextBoolean()
            if (extraA) {
                _nombresA.value = barajado.take(mitad + 1) + listOf("")
                _nombresB.value = barajado.drop(mitad + 1) + listOf("")
            } else {
                _nombresA.value = barajado.take(mitad) + listOf("")
                _nombresB.value = barajado.drop(mitad) + listOf("")
            }
        }
    }

    fun guardar() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val listaA = _nombresA.value.map { it.trim() }.filter { it.isNotBlank() }
                val listaB = _nombresB.value.map { it.trim() }.filter { it.isNotBlank() }
                relacionRepository.eliminarJugadoresDeEquipo(partidoId, equipoAId)
                relacionRepository.eliminarJugadoresDeEquipo(partidoId, equipoBId)
                val jugadoresIdA = mutableListOf<Long>()
                val jugadoresIdB = mutableListOf<Long>()
                for (nombre in listaA) {
                    if (nombre.isNotBlank()) {
                        val id = jugadorRepository.getOrCreateJugador(nombre)
                        jugadoresIdA.add(id)
                    }
                }
                for (nombre in listaB) {
                    if (nombre.isNotBlank()) {
                        val id = jugadorRepository.getOrCreateJugador(nombre)
                        jugadoresIdB.add(id)
                    }
                }
                jugadoresIdA.forEach { jugadorId ->
                    relacionRepository.insert(
                        PartidoEquipoJugadorEntity(
                            partidoId = partidoId,
                            equipoId = equipoAId,
                            jugadorId = jugadorId
                        )
                    )
                }
                jugadoresIdB.forEach { jugadorId ->
                    relacionRepository.insert(
                        PartidoEquipoJugadorEntity(
                            partidoId = partidoId,
                            equipoId = equipoBId,
                            jugadorId = jugadorId
                        )
                    )
                }
                _guardado.value = true
            } catch (e: Exception) {
                _error.value = "Error al guardar jugadores: ${e.message}"
            }
            _loading.value = false
        }
    }

    private fun cargar() {
        _loading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                val nombreA = equipoRepository.getById(equipoAId)?.nombre ?: "Equipo A"
                val nombreB = equipoRepository.getById(equipoBId)?.nombre ?: "Equipo B"
                val relA = relacionRepository.getJugadoresDeEquipoEnPartido(partidoId, equipoAId)
                val relB = relacionRepository.getJugadoresDeEquipoEnPartido(partidoId, equipoBId)
                val listaA = relA.mapNotNull { jugadorRepository.getById(it.jugadorId)?.nombre }
                val listaB = relB.mapNotNull { jugadorRepository.getById(it.jugadorId)?.nombre }
                _equipoA.value = nombreA
                _equipoB.value = nombreB
                _nombresA.value = (listaA + listOf("")).ifEmpty { listOf("") }
                _nombresB.value = (listaB + listOf("")).ifEmpty { listOf("") }
                _loading.value = false
            } catch (e: Exception) {
                _error.value = "Error al cargar jugadores: ${e.message}"
                _loading.value = false
            }
        }
    }
}
