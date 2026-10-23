package cl.duocuc.model

enum class EstadoTurno(val etiqueta: String){
    ESPERANDO("Esperando"),
    ASIGNADO("Probador asignado"),
    EN_USO("En uso"),
    FINALIZADO("Finalizado"),
    ABANDONO("Abandono")
}

enum class CondicionCliente(val etiqueta: String) {
    NORMAL("Normal"),
    DISCAPACIDAD("Discapacidad"),
    EMBARAZO("Embarazo");

    val preferente: Boolean get() = this != NORMAL
}

data class TurnoEsperaUiState(
    val numero: Int = 0,
    val condicion: CondicionCliente = CondicionCliente.NORMAL,
    val horaIngreso: Long = System.currentTimeMillis(),
    val cantidadPrendas: Int = 0,
    val solicitaAccesible: Boolean = false,
    val estado: EstadoTurno = EstadoTurno.ESPERANDO,
    val personasAntes: Int = 0,
    val minutosEstimados: Int = 0
)
