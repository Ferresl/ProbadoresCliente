package cl.duocuc.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import cl.duocuc.model.SesionUiState
import cl.duocuc.R

@Composable
fun InicioSesionScreen(
    estado: SesionUiState,
    onCorreoCambia: (String) -> Unit,
    onContrasenaCambia: (String) -> Unit,
    onIngresar: () -> Unit,
    onVolver: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        TextButton(onClick = onVolver) {
            Icon(painterResource(R.drawable.ic_volver), contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Volver al inicio")
        }
        Text("Rincón Sur", style = MaterialTheme.typography.headlineMedium)
        Text("Ingreso del personal", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = estado.correo,
            onValueChange = onCorreoCambia,
            label = { Text("Correo") },
            leadingIcon = { Icon(painterResource(R.drawable.ic_correo), contentDescription = null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp)
        )
        OutlinedTextField(
            value = estado.contrasena,
            onValueChange = onContrasenaCambia,
            label = { Text("Contraseña") },
            leadingIcon = { Icon(painterResource(R.drawable.ic_candado), contentDescription = null) },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
        )
        estado.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
        }
        Button(
            onClick = onIngresar,
            modifier = Modifier.fillMaxWidth().padding(top = 20.dp)
        ) {
            Icon(painterResource(R.drawable.ic_ingresar), contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Iniciar sesión")
        }
    }
}
