package cl.duocuc.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duocuc.model.EstadoProbador
import cl.duocuc.ui.components.TarjetaProbador
import cl.duocuc.viewmodel.ProbadoresViewModel

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun PantallaEstadoProbadores(viewModel: ProbadoresViewModel = viewModel()) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Rincón Sur")
                        Text("Estado de probadores", style = MaterialTheme.typography.labelMedium)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                ResumenEspera(
                    libres = estado.probadores.count { it.estado == EstadoProbador.LIBRE },
                    enEspera = estado.personasEnEspera,
                    minutos = estado.tiempoEstimado
                )
            }

            item {
                Text("Probadores", style = MaterialTheme.typography.titleLarge)
            }

            items(estado.probadores, key = { it.codigo }) { probador ->
                TarjetaProbador(
                    probador = probador,
                    onCambiarEstado = { nuevoEstado ->
                        viewModel.cambiarEstado(probador.codigo, nuevoEstado)
                    }
                )
            }
        }
    }
}

@Composable
private fun ResumenEspera(libres: Int, enEspera: Int, minutos: Int) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Información de espera", style = MaterialTheme.typography.titleMedium)
            Text("Probadores libres: $libres")
            Text("Personas en la fila: $enEspera")
            Text("Espera estimada: $minutos minutos")
        }
    }
}

