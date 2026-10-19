package cl.duocuc.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
    ahora: Long,
    onIniciarUso: () -> Unit,
    onTerminarUso: () -> Unit,
    onFinalizarOrdenamiento: () -> Unit,
    modifier: Modifier = Modifier
) {
    val segundos = ((ahora - probador.estadoDesde).coerceAtLeast(0) / 1_000)

    Card(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(probador.codigo, style = MaterialTheme.typography.titleMedium)
            Text(probador.ubicacion)
            Text(if (probador.accesible) "Probador accesible" else "Probador estándar")
            Text("Estado: ${probador.estado.etiqueta}")
            Text("Tiempo en este estado: ${segundos / 60} min ${segundos % 60} s")

            when (probador.estado) {
                EstadoProbador.LIBRE -> {
                    Button(onClick = onIniciarUso) { Text("Iniciar uso") }
                }
                EstadoProbador.OCUPADO -> {
                    Button(onClick = onTerminarUso) { Text("Terminar uso") }
                }
                EstadoProbador.ORDENAMIENTO -> {
                    Text("El probador estará disponible cuando termine el ordenamiento")
                    Button(onClick = onFinalizarOrdenamiento) { Text("Finalizar ordenamiento") }
                }
                EstadoProbador.FUERA_SERVICIO -> {
                    Text("No disponible para asignación")
                }
            }
        }
    }
}
