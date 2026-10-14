package cl.duocuc.model

//estados posibles de los probadores de la sucursal
enum class EstadoProbador(val etiqueta: String){
    LIBRE("Libre"),
    OCUPADO("Ocupado"),
    ORDENAMIENTO("Ordenamiento"),
    FUERA_SERVICIO("Fuera de servicio")
}
