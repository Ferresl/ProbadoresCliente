package cl.duocuc.viewmodel

import androidx.lifecycle.ViewModel
import cl.duocuc.model.EstadoProbador
import cl.duocuc.model.EstadoProbadoresUiState
import cl.duocuc.model.ProbadorUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ProbadoresViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        EstadoProbadoresUiState(
            probadores = listOf(
                ProbadorUiState("P-01", "Vestuario dama", EstadoProbador.LIBRE),
                ProbadorUiState("P-02", "Vestuario dama", EstadoProbador.OCUPADO, 8),
                ProbadorUiState("P-03", "Vestuario hombre", EstadoProbador.LIBRE),
                ProbadorUiState("P-04", "Vestuario hombre", EstadoProbador.ORDENAMIENTO),
                ProbadorUiState("P-05", "Vestuario infantil", EstadoProbador.OCUPADO, 14)
            ),
            personasEnEspera = 3,
            tiempoEstimado = 12
        )
    )

    val uiState: StateFlow<EstadoProbadoresUiState> = _uiState.asStateFlow()

    fun cambiarEstado(codigo: String, estado: EstadoProbador) {
        _uiState.update { actual ->
            actual.copy(
                probadores = actual.probadores.map { probador ->
                    if (probador.codigo == codigo) {
                        probador.copy(
                            estado = estado,
                            minutosUso = if (estado == EstadoProbador.OCUPADO) 1 else 0
                        )
                    } else {
                        probador
                    }
                }
            )
        }
    }
}
