package cl.duocuc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duocuc.ui.screens.InicioSesionScreen
import cl.duocuc.ui.screens.PantallaEstadoProbadores
import cl.duocuc.ui.screens.UsuariosScreen
import cl.duocuc.model.RolUsuario
import cl.duocuc.ui.theme.RinconSurTheme
import cl.duocuc.viewmodel.ProbadoresViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RinconSurTheme {
                AplicacionProbadores()
            }
        }
    }
}

@Composable
private fun AplicacionProbadores(viewModel: ProbadoresViewModel = viewModel()) {
    val sesion by viewModel.sesion.collectAsStateWithLifecycle()
    val usuarios by viewModel.usuarios.collectAsStateWithLifecycle()
    val formulario by viewModel.formulario.collectAsStateWithLifecycle()
    var mostrarUsuarios by rememberSaveable { mutableStateOf(false) }
    if (sesion.rol == null) {
        InicioSesionScreen(
            estado = sesion,
            onCorreoCambia = viewModel::cambiarCorreo,
            onContrasenaCambia = viewModel::cambiarContrasena,
            onIngresar = viewModel::iniciarSesion
        )
    } else if (mostrarUsuarios && sesion.rol == RolUsuario.ADMIN) {
        UsuariosScreen(
            usuarios = usuarios,
            formulario = formulario,
            onNombreCambia = viewModel::cambiarNombreUsuario,
            onCorreoCambia = viewModel::cambiarCorreoUsuario,
            onContrasenaCambia = viewModel::cambiarContrasenaUsuario,
            onRolCambia = viewModel::cambiarRolUsuario,
            onRegistrar = viewModel::registrarUsuario,
            onEliminar = viewModel::eliminarUsuario,
            onVolver = { mostrarUsuarios = false }
        )
    } else {
        PantallaEstadoProbadores(
            viewModel = viewModel,
            onVerUsuarios = { mostrarUsuarios = true }
        )
    }
}
