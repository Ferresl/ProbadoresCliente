package cl.duocuc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import cl.duocuc.ui.screens.PantallaEstadoProbadores
import cl.duocuc.ui.theme.RinconSurTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RinconSurTheme {
                PantallaEstadoProbadores()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PantallaEstadoProbadoresPreview() {
    RinconSurTheme {
        PantallaEstadoProbadores()
    }
}
