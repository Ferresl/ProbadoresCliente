package cl.duocuc.model

enum class EstadoTurno(val etiqueta: String){
    ESPERANDO("Esperando"),
    ASIGNADO("Probador asignado"),
    EN_USO("En uso"),
    FINALIZADO("Finalizado"),
    ABANDONO("Abandono")
}

data class TurnoEsperaUiState(
    val numero: Int = 0,
    val cantidadPrendas: Int = 0,
    val solicitaAccesible: Boolean = false,
    val estado: EstadoTurno = EstadoTurno.ESPERANDO,
    val personasAntes: Int = 0,
    val minutosEstimados: Int = 0
)
