package cl.duocuc.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.TextButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duocuc.model.EstadoProbador
import cl.duocuc.model.EstadoTurno
import cl.duocuc.R
import cl.duocuc.model.RolUsuario
import cl.duocuc.ui.components.DialogoPrendas
import cl.duocuc.ui.components.TarjetaProbador
import cl.duocuc.viewmodel.ProbadoresViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun PantallaEstadoProbadores(
    viewModel: ProbadoresViewModel = viewModel(),
    onVerUsuarios: () -> Unit = {},
    onCerrarSesion: () -> Unit = {}
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val sesion by viewModel.sesion.collectAsStateWithLifecycle()
    var codigoSeleccionado by rememberSaveable { mutableStateOf<String?>(null) }
    var iniciando by rememberSaveable { mutableStateOf(true) }
    var mensajeError by rememberSaveable { mutableStateOf<String?>(null) }
    val formatoHora = SimpleDateFormat("HH:mm:ss", Locale("es", "CL"))
    val usosCerrados = estado.usos.filter { it.horaTermino != null }.asReversed()

    Scaffold(
        topBar = {
            TopAppBar(title = {
                Column {
                    Text("Rincón Sur")
                    Text("RS-014 · ${sesion.rol?.nombre ?: ""}", style = MaterialTheme.typography.labelMedium)
                }
            }, actions = {
                if (sesion.rol == RolUsuario.ADMIN) {
                    TextButton(onClick = onVerUsuarios) {
                        Icon(painterResource(R.drawable.ic_usuarios), contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Usuarios")
                    }
                }
                TextButton(onClick = onCerrarSesion) {
                    Icon(painterResource(R.drawable.ic_salir), contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Salir")
                }
            })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Estado de la sección", style = MaterialTheme.typography.titleMedium)
                        Text("Libres: ${estado.probadores.count { it.estado == EstadoProbador.LIBRE }}")
                        Text("Ocupados: ${estado.probadores.count { it.estado == EstadoProbador.OCUPADO }}")
                        Text("En ordenamiento: ${estado.probadores.count { it.estado == EstadoProbador.ORDENAMIENTO }}")
                        Text("Fuera de servicio: ${estado.probadores.count { it.estado == EstadoProbador.FUERA_SERVICIO }}")
                        Text("Usos terminados: ${usosCerrados.size}")
                    }
                }
            }
            item {
                val pendientes = estado.turnos.filter { it.estado == EstadoTurno.ESPERANDO }
                    .sortedWith(compareBy({ !it.condicion.preferente }, { it.numero }))
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Fila de espera: ${pendientes.size}", style = MaterialTheme.typography.titleMedium)
                        pendientes.forEach { turno ->
                            Text("T-${turno.numero.toString().padStart(3, '0')} · ${turno.condicion.etiqueta}")
                        }
                        if (pendientes.isNotEmpty()) {
                            Text("El siguiente turno se asigna al iniciar un uso.")
                        }
                    }
                }
            }
            mensajeError?.let { mensaje ->
                item { Text(mensaje, color = MaterialTheme.colorScheme.error) }
            }
            items(estado.probadores, key = { it.codigo }) { probador ->
                TarjetaProbador(
                    probador = probador,
                    ahora = estado.ahora,
                    onIniciarUso = {
                        mensajeError = null
                        iniciando = true
                        codigoSeleccionado = probador.codigo
                    },
                    onTerminarUso = {
                        mensajeError = null
                        iniciando = false
                        codigoSeleccionado = probador.codigo
                    },
                    onFinalizarOrdenamiento = {
                        mensajeError = viewModel.finalizarOrdenamiento(probador.codigo)
                    },
                    puedeMantenimiento = sesion.rol == RolUsuario.ADMIN,
                    onCambiarMantenimiento = {
                        mensajeError = viewModel.cambiarMantenimiento(probador.codigo)
                    }
                )
            }
            item { Text("Usos terminados", style = MaterialTheme.typography.titleLarge) }
            if (usosCerrados.isEmpty()) {
                item { Text("Todavía no hay usos terminados") }
            }
            items(usosCerrados, key = { it.id }) { uso ->
                val termino = uso.horaTermino ?: uso.horaInicio
                val segundos = ((termino - uso.horaInicio) / 1_000).coerceAtLeast(0)
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("${uso.codigoProbador} · Uso ${uso.id}")
                        Text("Inicio: ${formatoHora.format(Date(uso.horaInicio))}")
                        Text("Término: ${formatoHora.format(Date(termino))}")
                        Text("Duración: ${segundos / 60} min ${segundos % 60} s")
                        Text("Ingresadas: ${uso.prendasIngresadas} · Devueltas: ${uso.prendasDevueltas}")
                    }
                }
            }
        }
    }

    codigoSeleccionado?.let { codigo ->
        val usoActivo = estado.usos.find {
            it.codigoProbador == codigo && it.horaTermino == null
        }
        DialogoPrendas(
            codigo = codigo,
            iniciando = iniciando,
            prendasIngresadas = usoActivo?.prendasIngresadas,
            numeroTurno = if (iniciando) viewModel.siguienteTurno(codigo)?.numero else null,
            onConfirmar = { cantidad ->
                if (iniciando) viewModel.iniciarUso(codigo, cantidad)
                else viewModel.terminarUso(codigo, cantidad)
            },
            onCerrar = { codigoSeleccionado = null }
        )
    }
}
