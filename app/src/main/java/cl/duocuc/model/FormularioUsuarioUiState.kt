package cl.duocuc.model

data class FormularioUsuarioUiState(
    val nombreCompleto: String = "",
    val correo: String = "",
    val contrasena: String = "",
    val rol: RolUsuario = RolUsuario.ASISTENTE,
    val error: String? = null
)
