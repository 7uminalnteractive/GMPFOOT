package app.gmpfoot.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.gmpfoot.domain.Offer
import app.gmpfoot.domain.OfferKind
import app.gmpfoot.domain.money

private fun Offer.describe(): String = when (kind) {
    OfferKind.SAF ->
        "Aporte de ${investmentEur.money()} à vista por $stakePercent% do clube. " +
            "Ao fim de cada temporada com lucro, o investidor recebe $stakePercent% dele."
    OfferKind.NAMING ->
        "${perSeasonEur.money()} por temporada por $seasons temporadas. Estádio passa a se chamar \"$stadiumName\"."
    else ->
        "${perSeasonEur.money()} por temporada por $seasons temporada(s)" +
            if (bonusEur > 0) " + ${bonusEur.money()} por vitória." else "."
}

@Composable
fun OffersScreen(vm: GameViewModel, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp).statusBarsPadding()) {
        TextButton(onClick = onBack) { Text("‹ Voltar") }
        Text("Propostas", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Patrocínio, naming rights e SAF. Propostas não aceitas expiram.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(12.dp))
        if (vm.offers.isEmpty()) Text("Nenhuma proposta no momento.")
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(vm.offers) { o ->
                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                ) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(o.kind.label, color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelLarge)
                        Text(o.company, style = MaterialTheme.typography.titleMedium)
                        Text(o.describe(), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { vm.acceptOffer(o) }) { Text("Aceitar") }
                            OutlinedButton(onClick = { vm.declineOffer(o) }) { Text("Recusar") }
                        }
                    }
                }
            }
        }
    }
}
