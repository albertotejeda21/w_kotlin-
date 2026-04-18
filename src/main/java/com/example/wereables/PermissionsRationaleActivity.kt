package com.example.wereables

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.wereables.ui.theme.WereablesTheme

class PermissionsRationaleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WereablesTheme {
                RationaleScreen(onCloseClicked = { finish() })
            }
        }
    }
}

@Composable
fun RationaleScreen(onCloseClicked: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Política de Privacidad de Health Connect",
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Para poder brindarte una experiencia completa, esta aplicación necesita acceder a tus datos de pasos y frecuencia cardíaca a través de Health Connect. Los datos son usados para X y no se comparten con terceros. Puedes revisar nuestra política de privacidad completa en [enlace].",
            textAlign = TextAlign.Justify
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onCloseClicked,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Entendido")
        }
    }
}
