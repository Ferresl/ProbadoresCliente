package cl.duocuc.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import cl.duocuc.R

@Composable
fun PantallaInicioScreen(
    onClientes: () -> Unit,
    onPersonal: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Rincón Sur", style = MaterialTheme.typography.headlineMedium)
        Text("Probadores RS-014", style = MaterialTheme.typography.titleMedium)
        Text(
            "Elige cómo quieres entrar",
            modifier = Modifier.padding(top = 24.dp, bottom = 16.dp)
        )
        Button(onClick = onClientes, modifier = Modifier.fillMaxWidth()) {
            Icon(painterResource(R.drawable.ic_probador), contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Soy cliente")
        }
        OutlinedButton(
            onClick = onPersonal,
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
        ) {
            Icon(painterResource(R.drawable.ic_persona), contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Soy trabajador")
        }
    }
}
