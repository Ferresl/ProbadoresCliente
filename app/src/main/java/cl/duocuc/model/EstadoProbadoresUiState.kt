package cl.duocuc.model

data class EstadoProbadoresUiState(
    val probadores: List<ProbadorUiState> = emptyList(),
    val personasEnEspera: Int = 0,
    val tiempoEstimado: Int = 0
)
