package cl.duocuc.model

data class Usuario(
    val nombreCompleto: String,
    val correo: String,
    val rol: RolUsuario
)
