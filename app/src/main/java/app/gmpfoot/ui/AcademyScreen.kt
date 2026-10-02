package app.gmpfoot.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private data class Lesson(
    val title: String,
    val text: String,
    val question: String,
    val options: List<String>,
    val answer: Int,
)

private val lessons = listOf(
    Lesson(
        "1. Escalação",
        "Você escala 11 titulares: 1 goleiro e 10 jogadores de linha. A formação (como 4-4-2) define quantos zagueiros, meias e atacantes entram.",
        "Quantos titulares formam a escalação?",
        listOf("9", "11", "12"), 1,
    ),
    Lesson(
        "2. Esquemas táticos",
        "O esquema muda o equilíbrio do time. Ataque Total aumenta o poder ofensivo e enfraquece a defesa; Defesa Total faz o contrário.",
        "Qual esquema mais reforça a defesa?",
        listOf("Ataque Total", "Balanceado", "Defesa Total"), 2,
    ),
    Lesson(
        "3. Finanças",
        "Você ganha com bilheteria (só em casa), patrocínios e naming rights, e paga os salários a cada rodada. Preço de ingresso alto reduz o público.",
        "Num jogo fora de casa, entra bilheteria?",
        listOf("Sim", "Não", "Só se ganhar"), 1,
    ),
    Lesson(
        "4. Patrocínio e naming rights",
        "Patrocínio master e fornecedor pagam por temporada, às vezes com bônus por vitória. No naming rights, uma empresa paga para dar nome ao estádio por vários anos.",
        "No naming rights, quem paga e o que recebe?",
        listOf("A torcida paga e escolhe o nome", "A empresa paga e o estádio leva o nome dela", "A federação paga"), 1,
    ),
    Lesson(
        "5. SAF e base",
        "Na SAF (Sociedade Anônima do Futebol), um investidor aporta dinheiro em troca de parte do clube e recebe uma fatia do lucro. A base forma jogadores que você pode promover ao profissional.",
        "O que o investidor da SAF recebe?",
        listOf("Os troféus", "Uma participação no clube", "O estádio inteiro"), 1,
    ),
)

@Composable
fun AcademyScreen(vm: GameViewModel, onBack: () -> Unit) {
    val done = vm.academyLessons
    var current by remember { mutableIntStateOf(done.coerceAtMost(lessons.size)) }
    var feedback by remember { mutableStateOf("") }

    Column(
        Modifier.fillMaxSize().padding(24.dp).statusBarsPadding().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TextButton(onClick = onBack) { Text("‹ Voltar") }
        Text("Academia de Treinadores", style = MaterialTheme.typography.headlineSmall)

        if (current >= lessons.size) {
            Text("Licença concedida", style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary)
            Text("Você concluiu as ${lessons.size} aulas. Bom trabalho, professor.")
            OutlinedButton(onClick = { current = 0; feedback = "" }) { Text("Rever aulas") }
        } else {
            val l = lessons[current]
            Text("Aula ${current + 1} de ${lessons.size}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(l.title, style = MaterialTheme.typography.titleLarge)
            Text(l.text)
            Spacer(Modifier.height(8.dp))
            Text(l.question, style = MaterialTheme.typography.titleMedium)
            l.options.forEachIndexed { i, opt ->
                OutlinedButton(
                    onClick = {
                        if (i == l.answer) {
                            vm.completeLesson(current + 1)
                            feedback = ""
                            current++
                        } else {
                            feedback = "Resposta errada. Releia a aula e tente de novo."
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(opt) }
            }
            if (feedback.isNotEmpty()) Text(feedback, color = MaterialTheme.colorScheme.error)
        }
    }
}
