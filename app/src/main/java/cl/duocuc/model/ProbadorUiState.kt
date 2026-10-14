package cl.duocuc.model

//informacion que se mostrara de cada probador en la pantalla
data class ProbadorUiState(
    val codigo: String,
    val ubicacion: String,
    val estado: EstadoProbador = EstadoProbador.LIBRE,
    val minutosUso: Int = 0
)
