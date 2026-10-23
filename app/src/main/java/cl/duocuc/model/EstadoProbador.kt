package cl.duocuc.model


enum class EstadoProbador(val etiqueta: String){
    LIBRE("Libre"),
    OCUPADO("Ocupado"),
    ORDENAMIENTO("Ordenamiento"),
    FUERA_SERVICIO("Fuera de servicio")
}
