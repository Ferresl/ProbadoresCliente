package cl.duocuc.model

data class ProbadorUiState(
    val codigo: String,
    val ubicacion: String,
    val estado: EstadoProbador = EstadoProbador.LIBRE,
    val accesible: Boolean = false,
    val estadoDesde: Long = System.currentTimeMillis()
)
