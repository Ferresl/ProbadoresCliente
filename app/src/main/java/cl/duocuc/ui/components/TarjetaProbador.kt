package cl.duocuc.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duocuc.model.EstadoProbador
import cl.duocuc.model.ProbadorUiState

@Composable
fun TarjetaProbador(
    probador: ProbadorUiState,
    onCambiarEstado: (EstadoProbador) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(probador.codigo, style = MaterialTheme.typography.titleMedium)
                Text(probador.estado.etiqueta)
            }

            Text(probador.ubicacion, style = MaterialTheme.typography.bodyMedium)

            if (probador.estado == EstadoProbador.OCUPADO) {
                Text("Tiempo de uso: ${probador.minutosUso} minutos")
            }

            Button(onClick = {
                val siguienteEstado = if (probador.estado == EstadoProbador.LIBRE) {
                    EstadoProbador.OCUPADO
                } else {
                    EstadoProbador.LIBRE
                }
                onCambiarEstado(siguienteEstado)
            }) {
                Text(if (probador.estado == EstadoProbador.LIBRE) "Marcar ocupado" else "Liberar")
            }
        }
    }
}
