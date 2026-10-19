package cl.duocuc.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duocuc.model.EstadoProbador
import cl.duocuc.model.EstadoProbadoresUiState
import cl.duocuc.model.ProbadorUiState
import cl.duocuc.model.UsoProbador
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ProbadoresViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        EstadoProbadoresUiState(
            probadores = (1..8).map { numero ->
                ProbadorUiState(
                    codigo = "P-${numero.toString().padStart(2, '0')}",
                    ubicacion = when (numero) {
                        in 1..4 -> "Lado mesón"
                        in 5..7 -> "Lado espejo"
                        else -> "Al fondo del módulo"
                    },
                    accesible = numero == 8
                )
            }
        )
    )

    val uiState: StateFlow<EstadoProbadoresUiState> = _uiState.asStateFlow()
    private var siguienteIdUso = 1L

    init {
        viewModelScope.launch {
            while (isActive) {
                delay(1_000)
                _uiState.update { it.copy(ahora = System.currentTimeMillis()) }
            }
        }
    }

    fun iniciarUso(codigo: String, prendas: Int): String? {
        val actual = _uiState.value
        val probador = actual.probadores.find { it.codigo == codigo }
            ?: return "El probador no existe"

        if (prendas !in 1..6) return "Debes ingresar entre 1 y 6 prendas"
        if (probador.estado != EstadoProbador.LIBRE) return "El probador ya no está libre"
        if (actual.usos.any { it.codigoProbador == codigo && it.horaTermino == null }) {
            return "Este probador ya tiene un uso activo"
        }

        val ahora = System.currentTimeMillis()
        val uso = UsoProbador(
            id = siguienteIdUso,
            codigoProbador = codigo,
            horaInicio = ahora,
            prendasIngresadas = prendas
        )

        _uiState.value = actual.copy(
            probadores = actual.probadores.map {
                if (it.codigo == codigo) {
                    it.copy(estado = EstadoProbador.OCUPADO, estadoDesde = ahora)
                } else it
            },
            usos = actual.usos + uso,
            ahora = ahora
        )
        siguienteIdUso++
        return null
    }

    fun terminarUso(codigo: String, prendasDevueltas: Int): String? {
        val actual = _uiState.value
        val probador = actual.probadores.find { it.codigo == codigo }
            ?: return "El probador no existe"
        if (probador.estado != EstadoProbador.OCUPADO) return "El probador no está ocupado"

        val uso = actual.usos.find {
            it.codigoProbador == codigo && it.horaTermino == null
        } ?: return "No se encontró un uso activo"

        if (prendasDevueltas !in 0..uso.prendasIngresadas) {
            return "La devolución debe ser entre 0 y ${uso.prendasIngresadas} prendas"
        }

        val ahora = System.currentTimeMillis()
        if (ahora < uso.horaInicio) return "Revisa la fecha y hora del dispositivo"

        _uiState.value = actual.copy(
            probadores = actual.probadores.map {
                if (it.codigo == codigo) {
                    it.copy(estado = EstadoProbador.ORDENAMIENTO, estadoDesde = ahora)
                } else it
            },
            usos = actual.usos.map {
                if (it.id == uso.id) {
                    it.copy(horaTermino = ahora, prendasDevueltas = prendasDevueltas)
                } else it
            },
            ahora = ahora
        )
        return null
    }

    fun finalizarOrdenamiento(codigo: String): String? {
        val actual = _uiState.value
        val probador = actual.probadores.find { it.codigo == codigo }
            ?: return "El probador no existe"
        if (probador.estado != EstadoProbador.ORDENAMIENTO) {
            return "Primero debe terminar el uso del probador"
        }

        val ahora = System.currentTimeMillis()
        _uiState.value = actual.copy(
            probadores = actual.probadores.map {
                if (it.codigo == codigo) {
                    it.copy(estado = EstadoProbador.LIBRE, estadoDesde = ahora)
                } else it
            },
            ahora = ahora
        )
        return null
    }
}
