package app.gmpfoot.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DataSourceScreen(vm: GameViewModel, onBack: () -> Unit) {
    var url by remember {
        mutableStateOf("https://raw.githubusercontent.com/SEU_USUARIO/SEU_REPO/main/data/teams.json")
    }
    var status by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }

    Column(
        Modifier.fillMaxSize().padding(24.dp).statusBarsPadding().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        TextButton(onClick = onBack) { Text("‹ Voltar") }
        Text("Editor de Times", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Importe elencos, valores de mercado e base de um teams.json gerado pelo workflow do GitHub.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text("${vm.proTeams.size} times carregados agora.")
        OutlinedTextField(url, { url = it }, Modifier.fillMaxWidth(), label = { Text("URL do teams.json") })
        Button(
            onClick = {
                busy = true
                status = "Baixando..."
                vm.importData(url.trim()) { status = it; busy = false }
            },
            enabled = !busy,
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Importar") }
        OutlinedButton(
            onClick = { vm.resetData(); status = "Voltou aos dados do Brasfoot base." },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Voltar aos dados base") }
        if (status.isNotEmpty()) Text(status, color = MaterialTheme.colorScheme.primary)
    }
}
