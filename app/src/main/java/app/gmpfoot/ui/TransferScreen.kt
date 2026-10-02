package app.gmpfoot.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.gmpfoot.data.SampleData
import app.gmpfoot.domain.*

@Composable
fun TransferScreen(onBack: () -> Unit) {
    val target = SampleData.transferTarget
    val negotiation = remember { TransferNegotiation(target) }

    var feeM by remember { mutableFloatStateOf(10f) }
    var installments by remember { mutableFloatStateOf(1f) }
    var sellOn by remember { mutableFloatStateOf(0f) }
    var bonusM by remember { mutableFloatStateOf(0f) }
    var buyBack by remember { mutableStateOf(false) }
    var loan by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("Monte sua proposta por ${target.playerName}.") }

    fun offer() = TransferOffer(
        feeEur = (feeM * 1_000_000).toLong(),
        installments = installments.toInt(),
        sellOnPercent = sellOn.toInt(),
        performanceBonusEur = (bonusM * 1_000_000).toLong(),
        buyBackEur = if (buyBack) (target.marketValueEur * 1.5).toLong() else null,
        loanWithOption = loan,
    )

    Column(
        Modifier.fillMaxSize().padding(24.dp).statusBarsPadding().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TextButton(onClick = onBack) { Text("‹ Voltar") }
        Text(target.playerName, style = MaterialTheme.typography.titleLarge)
        Text(
            "${target.sellerName} · valor de mercado €${target.marketValueEur / 1_000_000} mi",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text("Rodada ${negotiation.round}", color = MaterialTheme.colorScheme.onSurfaceVariant)

        Text("Valor: €${feeM.toInt()} mi")
        Slider(feeM, { feeM = it }, valueRange = 1f..40f)
        Text("Parcelas: ${installments.toInt()}")
        Slider(installments, { installments = it }, valueRange = 1f..4f, steps = 2)
        Text("% de futura venda para o vendedor: ${sellOn.toInt()}%")
        Slider(sellOn, { sellOn = it }, valueRange = 0f..30f)
        Text("Bônus por desempenho: €${"%.1f".format(bonusM)} mi")
        Slider(bonusM, { bonusM = it }, valueRange = 0f..5f)
        Row {
            Checkbox(buyBack, { buyBack = it })
            Text("Opção de recompra para o vendedor", Modifier.padding(top = 12.dp))
        }
        Row {
            Checkbox(loan, { loan = it })
            Text("Empréstimo com opção de compra", Modifier.padding(top = 12.dp))
        }

        Button(onClick = {
            status = when (val r = negotiation.propose(offer())) {
                is TransferResult.Accepted -> r.message
                is TransferResult.Counter -> {
                    feeM = r.offer.feeEur / 1_000_000f
                    installments = r.offer.installments.toFloat()
                    r.message
                }
                is TransferResult.Rejected -> r.message
            }
        }, Modifier.fillMaxWidth()) { Text("Enviar proposta") }

        Text(status, color = MaterialTheme.colorScheme.primary)
    }
}
