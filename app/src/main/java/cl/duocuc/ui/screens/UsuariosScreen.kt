package cl.duocuc.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import cl.duocuc.model.FormularioUsuarioUiState
import cl.duocuc.R
import cl.duocuc.model.RolUsuario
import cl.duocuc.model.Usuario

@Composable
fun UsuariosScreen(
    usuarios: List<Usuario>,
    formulario: FormularioUsuarioUiState,
    onNombreCambia: (String) -> Unit,
    onCorreoCambia: (String) -> Unit,
    onContrasenaCambia: (String) -> Unit,
    onRolCambia: (RolUsuario) -> Unit,
    onRegistrar: () -> Boolean,
    onEliminar: (String) -> String?,
    onVolver: () -> Unit
) {
    var correoEliminar by remember { mutableStateOf<String?>(null) }
    var mensaje by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextButton(onClick = onVolver) {
            Icon(painterResource(R.drawable.ic_volver), contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Volver a probadores")
        }
        Text("Usuarios del personal", style = MaterialTheme.typography.headlineSmall)
        Text("Nuevo usuario", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = formulario.nombreCompleto, onValueChange = onNombreCambia,
            label = { Text("Nombre completo") },
            leadingIcon = { Icon(painterResource(R.drawable.ic_persona), contentDescription = null) },
            singleLine = true, modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = formulario.correo, onValueChange = onCorreoCambia,
            label = { Text("Correo") },
            leadingIcon = { Icon(painterResource(R.drawable.ic_correo), contentDescription = null) },
            singleLine = true, modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = formulario.contrasena, onValueChange = onContrasenaCambia,
            label = { Text("Contraseña") },
            leadingIcon = { Icon(painterResource(R.drawable.ic_candado), contentDescription = null) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth()
        )
        Text("Puesto de trabajo", style = MaterialTheme.typography.titleMedium)
        RolUsuario.entries.forEach { rol ->
            Row {
                RadioButton(selected = formulario.rol == rol, onClick = { onRolCambia(rol) })
                TextButton(onClick = { onRolCambia(rol) }) { Text(rol.nombre) }
            }
        }
        formulario.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Button(onClick = {
            mensaje = if (onRegistrar()) "Usuario registrado" else null
        }) {
            Icon(painterResource(R.drawable.ic_agregar), contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Registrar usuario")
        }
        mensaje?.let { Text(it) }

        Text("Usuarios registrados", style = MaterialTheme.typography.titleMedium)
        usuarios.forEach { usuario ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text(usuario.nombreCompleto, style = MaterialTheme.typography.titleMedium)
                    Text(usuario.correo)
                    Text(usuario.rol.nombre)
                    TextButton(onClick = { correoEliminar = usuario.correo }) {
                        Icon(painterResource(R.drawable.ic_eliminar), contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Eliminar")
                    }
                }
            }
        }
    }

    correoEliminar?.let { correo ->
        AlertDialog(
            onDismissRequest = { correoEliminar = null },
            title = { Text("Eliminar usuario") },
            text = { Text("¿Eliminar la cuenta $correo?") },
            confirmButton = {
                TextButton(onClick = {
                    mensaje = onEliminar(correo) ?: "Usuario eliminado"
                    correoEliminar = null
                }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { correoEliminar = null }) { Text("Cancelar") }
            }
        )
    }
}
