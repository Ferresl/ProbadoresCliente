package cl.duocuc.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import cl.duocuc.R
import cl.duocuc.model.CondicionCliente
import cl.duocuc.model.EstadoProbador
import cl.duocuc.model.EstadoProbadoresUiState
import cl.duocuc.model.EstadoTurno
import cl.duocuc.model.TurnoEsperaUiState
import kotlin.math.ceil

@Composable
fun ClientesScreen(
    estado: EstadoProbadoresUiState,
    minutosPorUso: Int,
    onSacarTurno: (CondicionCliente) -> TurnoEsperaUiState,
    onVolver: () -> Unit
) {
    var condicion by rememberSaveable { mutableStateOf(CondicionCliente.NORMAL) }
    var numeroEntregado by rememberSaveable { mutableIntStateOf(0) }
    val esperando = estado.turnos.count { it.estado == EstadoTurno.ESPERANDO }
    val libres = estado.probadores.count { it.estado == EstadoProbador.LIBRE }
    val habilitados = estado.probadores.count { it.estado != EstadoProbador.FUERA_SERVICIO }
    val espera = if (habilitados == 0) null
        else ceil((esperando + 1).toDouble() * (minutosPorUso + 1) / habilitados).toInt()

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextButton(onClick = onVolver) {
            Icon(painterResource(R.drawable.ic_volver), contentDescription = null)
            Text("Volver al inicio")
        }
        Text("Probadores Rincón Sur", style = MaterialTheme.typography.headlineSmall)
        Text("Libres: $libres · Personas esperando: $esperando")
        Text(
            if (espera == null) "Sin tiempo estimado: todos los probadores están fuera de servicio"
            else "Espera aproximada para un nuevo turno: $espera min"
        )
        Text("Los tiempos son orientativos y pueden cambiar.", style = MaterialTheme.typography.bodySmall)

        estado.probadores.forEach { probador ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(painterResource(R.drawable.ic_probador), contentDescription = null)
                    Column {
                        Text(probador.codigo + if (probador.accesible) " · Accesible" else "")
                        Text(probador.estado.etiqueta)
                        when (probador.estado) {
                            EstadoProbador.OCUPADO -> {
                                val minutosTranscurridos = ((estado.ahora - probador.estadoDesde) / 60_000).toInt()
                                val restantes = (minutosPorUso + 1 - minutosTranscurridos).coerceAtLeast(1)
                                Text("Disponibilidad estimada: $restantes min o más")
                            }
                            EstadoProbador.ORDENAMIENTO -> Text("Disponibilidad estimada: 1 min")
                            EstadoProbador.LIBRE -> Text("Disponible ahora")
                            EstadoProbador.FUERA_SERVICIO -> Text("Sin disponibilidad estimada")
                        }
                    }
                }
            }
        }

        Text("Sacar un turno", style = MaterialTheme.typography.titleLarge)
        Text("Selecciona tu condición. No se solicitan datos personales.")
        CondicionCliente.entries.forEach { opcion ->
            Row {
                RadioButton(selected = condicion == opcion, onClick = { condicion = opcion })
                TextButton(onClick = { condicion = opcion }) { Text(opcion.etiqueta) }
            }
        }
        Button(
            onClick = { numeroEntregado = onSacarTurno(condicion).numero },
            enabled = numeroEntregado == 0 && habilitados > 0
        ) {
            Text("Obtener número")
        }
        if (numeroEntregado > 0) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Tu número: T-${numeroEntregado.toString().padStart(3, '0')}",
                        style = MaterialTheme.typography.titleLarge)
                    Text("Conserva este número y espera el llamado del personal.")
                    OutlinedButton(onClick = {
                        numeroEntregado = 0
                        condicion = CondicionCliente.NORMAL
                    }) { Text("Siguiente cliente") }
                }
            }
        }
    }
}
