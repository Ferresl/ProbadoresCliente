package cl.duocuc.model

data class EstadoProbadoresUiState(
    val probadores: List<ProbadorUiState> = emptyList(),
    val usos: List<UsoProbador> = emptyList(),
    val turnos: List<TurnoEsperaUiState> = emptyList(),
    val ahora: Long = System.currentTimeMillis()
)
