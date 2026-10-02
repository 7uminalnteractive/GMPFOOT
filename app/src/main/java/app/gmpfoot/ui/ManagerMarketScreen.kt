package app.gmpfoot.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.gmpfoot.data.SampleData
import app.gmpfoot.domain.*

@Composable
fun ManagerMarketScreen(onBack: () -> Unit) {
    val manager = SampleData.managers.first()
    val club = SampleData.clubs.first()
    val negotiation = remember { ContractNegotiation(manager, club) }

    var salary by remember { mutableFloatStateOf(70_000f) }
    var years by remember { mutableFloatStateOf(3f) }
    var releaseM by remember { mutableFloatStateOf(10f) }
    var budgetM by remember { mutableFloatStateOf(30f) }
    var youth by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("Monte sua proposta para ${club.name}.") }

    fun offer() = ContractOffer(
        weeklySalaryEur = salary.toInt(),
        years = years.toInt(),
        winBonusEur = 20_000,
        releaseClauseEur = (releaseM * 1_000_000).toInt(),
        transferBudgetEur = (budgetM * 1_000_000).toLong(),
        youthPromise = youth,
    )

    Column(
        Modifier.fillMaxSize().padding(24.dp).statusBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TextButton(onClick = onBack) { Text("‹ Voltar") }
        Text("${manager.name} × ${club.name}", style = MaterialTheme.typography.titleLarge)
        Text("Rodada ${negotiation.round}", color = MaterialTheme.colorScheme.onSurfaceVariant)

        Text("Salário semanal: €${salary.toInt()}")
        Slider(salary, { salary = it }, valueRange = 20_000f..150_000f)
        Text("Duração: ${years.toInt()} anos")
        Slider(years, { years = it }, valueRange = 1f..5f, steps = 3)
        Text("Multa rescisória: €${releaseM.toInt()} mi")
        Slider(releaseM, { releaseM = it }, valueRange = 0f..50f)
        Text("Verba de transferências: €${budgetM.toInt()} mi")
        Slider(budgetM, { budgetM = it }, valueRange = 0f..100f)
        Row {
            Checkbox(youth, { youth = it })
            Text("Prometer foco na base", Modifier.padding(top = 12.dp))
        }

        Button(onClick = {
            status = when (val r = negotiation.propose(offer())) {
                NegotiationResult.Accepted -> "Proposta aceita! Contrato fechado."
                is NegotiationResult.Counter -> {
                    salary = r.offer.weeklySalaryEur.toFloat()
                    years = r.offer.years.toFloat()
                    releaseM = r.offer.releaseClauseEur / 1_000_000f
                    budgetM = r.offer.transferBudgetEur / 1_000_000f
                    r.message
                }
                is NegotiationResult.Rejected -> r.message
            }
        }, Modifier.fillMaxWidth()) { Text("Enviar proposta") }

        Text(status, color = MaterialTheme.colorScheme.primary)
    }
}
