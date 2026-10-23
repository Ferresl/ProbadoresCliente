package cl.duocuc.model

data class SesionUiState(
    val correo: String = "",
    val contrasena: String = "",
    val rol: RolUsuario? = null,
    val error: String? = null
)
