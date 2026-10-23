package cl.duocuc.model

data class UsoProbador(
    val id: Long,
    val codigoProbador: String,
    val horaInicio: Long,
    val prendasIngresadas: Int,
    val numeroTurno: Int? = null,
    val horaTermino: Long? = null,
    val prendasDevueltas: Int? = null
)
