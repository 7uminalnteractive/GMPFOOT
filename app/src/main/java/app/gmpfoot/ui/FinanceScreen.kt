package app.gmpfoot.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.gmpfoot.domain.Economy
import app.gmpfoot.domain.money

@Composable
fun FinanceScreen(vm: GameViewModel, onBack: () -> Unit) {
    val f = vm.finance
    val st = f.stadium
    var msg by remember { mutableStateOf("") }
    val dim = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        Modifier.fillMaxSize().padding(24.dp).statusBarsPadding().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TextButton(onClick = onBack) { Text("‹ Voltar") }
        Text("Finanças", style = MaterialTheme.typography.headlineSmall)
        Text(
            f.balance.money(),
            style = MaterialTheme.typography.displaySmall,
            color = if (f.balance < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
        )
        if (vm.lastDividend > 0) Text("Dividendo pago à SAF na virada: ${vm.lastDividend.money()}", color = dim)

        vm.lastRoundFinance?.let { r ->
            Spacer(Modifier.height(8.dp))
            Text("Última rodada", style = MaterialTheme.typography.titleMedium)
            Text(
                if (r.home) "Bilheteria: ${r.tickets.money()} (${r.attendance} pagantes)" else "Jogo fora: sem bilheteria",
                color = dim,
            )
            Text("Patrocínios: ${r.sponsors.money()}", color = dim)
            Text("Salários: -${r.wages.money()}", color = dim)
            Text("Saldo da rodada: ${r.net.money()}")
        }

        Spacer(Modifier.height(8.dp))
        Text("Estádio", style = MaterialTheme.typography.titleMedium)
        Text("${st.name} · ${st.capacity} lugares", color = dim)
        Text("Ingresso: €${st.ticketPrice}")
        Slider(
            value = st.ticketPrice.toFloat(),
            onValueChange = { vm.setTicketPrice(it.toInt()) },
            onValueChangeFinished = { vm.commitTicketPrice() },
            valueRange = 10f..150f,
        )
        Text("Preço alto reduz o público; time bem colocado enche mais.", color = dim,
            style = MaterialTheme.typography.bodySmall)
        Button(onClick = {
            msg = if (vm.expandStadium()) "Estádio ampliado." else "Sem caixa ou capacidade máxima."
        }, Modifier.fillMaxWidth()) {
            Text("Ampliar +${Economy.EXPAND_SEATS} lugares (${Economy.EXPAND_COST.money()})")
        }
        if (msg.isNotEmpty()) Text(msg, color = MaterialTheme.colorScheme.primary)

        Spacer(Modifier.height(8.dp))
        Text("Contratos ativos", style = MaterialTheme.typography.titleMedium)
        if (f.deals.isEmpty() && f.saf == null) Text("Nenhum contrato. Veja as propostas.", color = dim)
        f.deals.forEach {
            Text("${it.kind.label}: ${it.company} · ${it.perSeasonEur.money()}/temp · ${it.seasons} temp. restantes", color = dim)
        }
        f.saf?.let {
            Text("SAF: ${it.company} detém ${it.stakePercent}% do clube", color = dim)
        }
    }
}
