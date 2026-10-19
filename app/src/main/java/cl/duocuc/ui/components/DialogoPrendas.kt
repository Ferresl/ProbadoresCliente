package cl.duocuc.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun DialogoPrendas(
    codigo: String,
    iniciando: Boolean,
    prendasIngresadas: Int?,
    onConfirmar: (Int) -> String?,
    onCerrar: () -> Unit
) {
    var cantidad by rememberSaveable(codigo, iniciando) { mutableStateOf("") }
    var error by rememberSaveable(codigo, iniciando) { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text(if (iniciando) "Iniciar uso de $codigo" else "Terminar uso de $codigo") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    if (iniciando) "Ingresa entre 1 y 6 prendas"
                    else "Prendas ingresadas: ${prendasIngresadas ?: "Sin registro"}"
                )
                OutlinedTextField(
                    value = cantidad,
                    onValueChange = { cantidad = it; error = null },
                    label = { Text(if (iniciando) "Prendas ingresadas" else "Prendas devueltas") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = error != null,
                    supportingText = { error?.let { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )
                if (!iniciando) Text("Al confirmar, el probador pasará a ordenamiento")
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val numero = cantidad.toIntOrNull()
                if (numero == null) {
                    error = "Ingresa una cantidad entera válida"
                } else {
                    val resultado = onConfirmar(numero)
                    if (resultado == null) onCerrar() else error = resultado
                }
            }) { Text("Confirmar") }
        },
        dismissButton = { TextButton(onClick = onCerrar) { Text("Cancelar") } }
    )
}
