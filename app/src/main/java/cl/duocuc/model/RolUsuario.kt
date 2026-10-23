package cl.duocuc.model

enum class RolUsuario(val nombre: String) {
    ADMIN("Administrador"),
    SUPERVISOR("Supervisor"),
    ASISTENTE("Asistente"),
    VENDEDOR("Vendedor")
}
