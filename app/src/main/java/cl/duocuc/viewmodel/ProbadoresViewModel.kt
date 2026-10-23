package cl.duocuc.viewmodel

import android.app.Application
import android.util.Patterns
import org.json.JSONArray
import org.json.JSONObject
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duocuc.model.EstadoProbador
import cl.duocuc.model.EstadoTurno
import cl.duocuc.model.CondicionCliente
import cl.duocuc.model.EstadoProbadoresUiState
import cl.duocuc.data.UsuarioDbHelper
import cl.duocuc.model.FormularioUsuarioUiState
import cl.duocuc.model.ProbadorUiState
import cl.duocuc.model.RolUsuario
import cl.duocuc.model.SesionUiState
import cl.duocuc.model.UsoProbador
import cl.duocuc.model.TurnoEsperaUiState
import cl.duocuc.model.Usuario
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ProbadoresViewModel(application: Application) : AndroidViewModel(application) {

    private val usuarioDb = UsuarioDbHelper(application)
    private val turnosPrefs = application.getSharedPreferences("numeros_turno", 0)
    private val _usuarios = MutableStateFlow(usuarioDb.listar())
    val usuarios: StateFlow<List<Usuario>> = _usuarios.asStateFlow()
    private val _formulario = MutableStateFlow(FormularioUsuarioUiState())
    val formulario: StateFlow<FormularioUsuarioUiState> = _formulario.asStateFlow()
    private var correoActual: String? = null

    private fun cargarTurnos(): List<TurnoEsperaUiState> {
        return try {
            val datos = JSONArray(turnosPrefs.getString("fila", "[]"))
            (0 until datos.length()).map { indice ->
                val dato = datos.getJSONObject(indice)
                TurnoEsperaUiState(
                    numero = dato.getInt("numero"),
                    condicion = CondicionCliente.valueOf(dato.getString("condicion")),
                    horaIngreso = dato.getLong("hora"),
                    estado = EstadoTurno.ESPERANDO
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun guardarTurnos(turnos: List<TurnoEsperaUiState>) {
        val datos = JSONArray()
        turnos.filter { it.estado == EstadoTurno.ESPERANDO }.forEach { turno ->
            datos.put(JSONObject().apply {
                put("numero", turno.numero)
                put("condicion", turno.condicion.name)
                put("hora", turno.horaIngreso)
            })
        }
        turnosPrefs.edit().putString("fila", datos.toString()).apply()
    }

    private val _uiState = MutableStateFlow(
        EstadoProbadoresUiState(
            turnos = cargarTurnos(),
            probadores = (1..8).map { numero ->
                ProbadorUiState(
                    codigo = "P-${numero.toString().padStart(2, '0')}",
                    ubicacion = when (numero) {
                        in 1..4 -> "Lado mesón"
                        in 5..7 -> "Lado espejo"
                        else -> "Al fondo del módulo"
                    },
                    accesible = numero == 8
                )
            }
        )
    )

    val uiState: StateFlow<EstadoProbadoresUiState> = _uiState.asStateFlow()
    private val _sesion = MutableStateFlow(SesionUiState())
    val sesion: StateFlow<SesionUiState> = _sesion.asStateFlow()
    private var siguienteIdUso = 1L
    private var siguienteNumeroTurno = turnosPrefs.getInt("siguiente", 1)

    fun sacarTurno(condicion: CondicionCliente): TurnoEsperaUiState {
        val actual = _uiState.value
        val enEspera = actual.turnos.filter { it.estado == EstadoTurno.ESPERANDO }
        val personasAntes = if (condicion.preferente) {
            enEspera.count { it.condicion.preferente }
        } else enEspera.size
        val habilitados = actual.probadores.count { it.estado != EstadoProbador.FUERA_SERVICIO }
            .coerceAtLeast(1)
        val minutos = ((personasAntes + 1) * (tiempoPromedioUso() + 1) + habilitados - 1) / habilitados
        val numero = siguienteNumeroTurno++
        turnosPrefs.edit().putInt("siguiente", siguienteNumeroTurno).apply()
        val turno = TurnoEsperaUiState(
            numero = numero,
            condicion = condicion,
            solicitaAccesible = condicion.preferente,
            personasAntes = personasAntes,
            minutosEstimados = minutos
        )
        _uiState.update { it.copy(turnos = it.turnos + turno) }
        guardarTurnos(_uiState.value.turnos)
        return turno
    }

    fun tiempoPromedioUso(): Int {
        val terminados = _uiState.value.usos.filter { it.horaTermino != null }
        if (terminados.size < 10) return 8
        return (terminados.map { ((it.horaTermino!! - it.horaInicio) / 60_000).toInt() }
            .average()).toInt().coerceAtLeast(1)
    }

    fun siguienteTurno(codigo: String): TurnoEsperaUiState? {
        val pendientes = _uiState.value.turnos.filter { it.estado == EstadoTurno.ESPERANDO }
        val preferentes = pendientes.filter { it.condicion.preferente }.minByOrNull { it.numero }
        val normales = pendientes.filter { !it.condicion.preferente }.minByOrNull { it.numero }
        val accesibleLibre = _uiState.value.probadores.any {
            it.accesible && it.estado == EstadoProbador.LIBRE
        }
        return if (codigo == "P-08" || !accesibleLibre) {
            preferentes ?: normales
        } else {
            normales ?: preferentes
        }
    }

    fun cambiarCorreo(valor: String) {
        _sesion.update { it.copy(correo = valor, error = null) }
    }

    fun cambiarContrasena(valor: String) {
        _sesion.update { it.copy(contrasena = valor, error = null) }
    }

    fun iniciarSesion() {
        val actual = _sesion.value
        val correo = actual.correo.trim().lowercase()
        val usuario = usuarioDb.iniciarSesion(correo, actual.contrasena)
        correoActual = usuario?.correo
        _sesion.update {
            if (usuario != null) {
                it.copy(correo = usuario.correo, rol = usuario.rol, contrasena = "", error = null)
            } else {
                it.copy(contrasena = "", error = "Correo o contraseña incorrectos")
            }
        }
    }

    fun cerrarSesion() {
        correoActual = null
        _sesion.value = SesionUiState()
    }

    fun cambiarNombreUsuario(valor: String) {
        _formulario.update { it.copy(nombreCompleto = valor, error = null) }
    }

    fun cambiarCorreoUsuario(valor: String) {
        _formulario.update { it.copy(correo = valor, error = null) }
    }

    fun cambiarContrasenaUsuario(valor: String) {
        _formulario.update { it.copy(contrasena = valor, error = null) }
    }

    fun cambiarRolUsuario(valor: RolUsuario) {
        _formulario.update { it.copy(rol = valor, error = null) }
    }

    fun registrarUsuario(): Boolean {
        if (_sesion.value.rol != RolUsuario.ADMIN) {
            _formulario.update { it.copy(error = "Solo el administrador puede registrar usuarios") }
            return false
        }
        val datos = _formulario.value
        val correo = datos.correo.trim().lowercase()
        val error = when {
            datos.nombreCompleto.isBlank() -> "Ingresa el nombre completo"
            !Patterns.EMAIL_ADDRESS.matcher(correo).matches() -> "Ingresa un correo válido"
            datos.contrasena.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
            else -> null
        }
        if (error != null) {
            _formulario.update { it.copy(error = error) }
            return false
        }
        val usuario = Usuario(datos.nombreCompleto.trim(), correo, datos.rol)
        if (!usuarioDb.registrar(usuario, datos.contrasena)) {
            _formulario.update { it.copy(error = "Ese correo ya está registrado") }
            return false
        }
        _usuarios.value = usuarioDb.listar()
        _formulario.value = FormularioUsuarioUiState()
        return true
    }

    fun eliminarUsuario(correo: String): String? {
        if (_sesion.value.rol != RolUsuario.ADMIN) return "Solo el administrador puede eliminar usuarios"
        if (correo == correoActual) return "No puedes eliminar tu propia cuenta"
        if (!usuarioDb.eliminar(correo)) return "No se encontró el usuario"
        _usuarios.value = usuarioDb.listar()
        return null
    }

    private fun requiereSesion(): String? =
        if (_sesion.value.rol == null) "Debes iniciar sesión" else null

    fun cambiarMantenimiento(codigo: String): String? {
        if (_sesion.value.rol != RolUsuario.ADMIN) return "Solo el administrador puede cambiar mantenimiento"
        val actual = _uiState.value
        val probador = actual.probadores.find { it.codigo == codigo }
            ?: return "El probador no existe"
        if (probador.estado == EstadoProbador.OCUPADO) return "Primero debe terminar el uso"
        val nuevoEstado = if (probador.estado == EstadoProbador.FUERA_SERVICIO) {
            EstadoProbador.LIBRE
        } else {
            EstadoProbador.FUERA_SERVICIO
        }
        val ahora = System.currentTimeMillis()
        _uiState.value = actual.copy(
            probadores = actual.probadores.map {
                if (it.codigo == codigo) it.copy(estado = nuevoEstado, estadoDesde = ahora) else it
            },
            ahora = ahora
        )
        return null
    }

    init {
        viewModelScope.launch {
            while (isActive) {
                delay(1_000)
                _uiState.update { it.copy(ahora = System.currentTimeMillis()) }
            }
        }
    }

    fun iniciarUso(codigo: String, prendas: Int): String? {
        requiereSesion()?.let { return it }
        val actual = _uiState.value
        val probador = actual.probadores.find { it.codigo == codigo }
            ?: return "El probador no existe"

        if (prendas !in 1..6) return "Debes ingresar entre 1 y 6 prendas"
        if (probador.estado != EstadoProbador.LIBRE) return "El probador ya no está libre"
        if (actual.usos.any { it.codigoProbador == codigo && it.horaTermino == null }) {
            return "Este probador ya tiene un uso activo"
        }

        val ahora = System.currentTimeMillis()
        val turno = siguienteTurno(codigo)
        val uso = UsoProbador(
            id = siguienteIdUso,
            codigoProbador = codigo,
            horaInicio = ahora,
            prendasIngresadas = prendas,
            numeroTurno = turno?.numero
        )

        _uiState.value = actual.copy(
            probadores = actual.probadores.map {
                if (it.codigo == codigo) {
                    it.copy(estado = EstadoProbador.OCUPADO, estadoDesde = ahora)
                } else it
            },
            usos = actual.usos + uso,
            turnos = actual.turnos.map {
                if (it.numero == turno?.numero) it.copy(estado = EstadoTurno.EN_USO) else it
            },
            ahora = ahora
        )
        guardarTurnos(_uiState.value.turnos)
        siguienteIdUso++
        return null
    }

    fun terminarUso(codigo: String, prendasDevueltas: Int): String? {
        requiereSesion()?.let { return it }
        val actual = _uiState.value
        val probador = actual.probadores.find { it.codigo == codigo }
            ?: return "El probador no existe"
        if (probador.estado != EstadoProbador.OCUPADO) return "El probador no está ocupado"

        val uso = actual.usos.find {
            it.codigoProbador == codigo && it.horaTermino == null
        } ?: return "No se encontró un uso activo"

        if (prendasDevueltas !in 0..uso.prendasIngresadas) {
            return "La devolución debe ser entre 0 y ${uso.prendasIngresadas} prendas"
        }

        val ahora = System.currentTimeMillis()
        if (ahora < uso.horaInicio) return "Revisa la fecha y hora del dispositivo"

        _uiState.value = actual.copy(
            probadores = actual.probadores.map {
                if (it.codigo == codigo) {
                    it.copy(estado = EstadoProbador.ORDENAMIENTO, estadoDesde = ahora)
                } else it
            },
            usos = actual.usos.map {
                if (it.id == uso.id) {
                    it.copy(horaTermino = ahora, prendasDevueltas = prendasDevueltas)
                } else it
            },
            turnos = actual.turnos.map {
                if (it.numero == uso.numeroTurno) it.copy(estado = EstadoTurno.FINALIZADO) else it
            },
            ahora = ahora
        )
        return null
    }

    fun finalizarOrdenamiento(codigo: String): String? {
        requiereSesion()?.let { return it }
        val actual = _uiState.value
        val probador = actual.probadores.find { it.codigo == codigo }
            ?: return "El probador no existe"
        if (probador.estado != EstadoProbador.ORDENAMIENTO) {
            return "Primero debe terminar el uso del probador"
        }

        val ahora = System.currentTimeMillis()
        _uiState.value = actual.copy(
            probadores = actual.probadores.map {
                if (it.codigo == codigo) {
                    it.copy(estado = EstadoProbador.LIBRE, estadoDesde = ahora)
                } else it
            },
            ahora = ahora
        )
        return null
    }
}
