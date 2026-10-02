# Auditoria do GMPFOOT (Fase 1)

## 1. Estrutura encontrada
Três projetos Android no mesmo repositório:

| Pasta | Tecnologia | Linhas | Papel |
|---|---|---|---|
| `app/` (raiz Gradle `:app`) | Kotlin + Compose + ViewModel | ~2.200 | **Projeto vivo.** É o único que o CI compila. |
| `app/Brasfoot/` → agora `legacy/Brasfoot/` | Java, XML, SQLite (DAO) | ~1.200 | Protótipo acadêmico (UDESC) |
| `app/FutManDDM/` → agora `legacy/FutManDDM/` | Java, XML, SQLite (DAO) | ~4.500 | Evolução do anterior (campeonato, patrocínio, estádio) |

O `app/` moderno já tem camadas `data/`, `domain/`, `ui/` e um único `GameViewModel`.

## 2. Funcionalidades que JÁ funcionam (app moderno)
Escolha de time (principal ou Sub-20) · escalação com 5 formações e 5 esquemas · motor de partida minuto a minuto · turno e returno · classificação · finanças (bilheteria, salários, patrocínio, naming rights, SAF, ampliação do estádio) · base procedural e promoção · academia · salvar/continuar (`save.json`, escrita atômica) · importação de `teams.json` · CI que gera o APK.

## 3. Problemas encontrados
1. **Mercado de jogadores e de técnicos usam dados de exemplo** (`SampleData`): um único jogador fixo; não alteram elenco nem caixa.
2. **Sem treinador na carreira.** O modelo `Manager` existe, mas só alimenta a tela de contrato de exemplo.
3. **Sem tela de elenco, perfil de jogador, táticas separadas, resultado pós-jogo ou notícias.**
4. Modelo de jogador com 4 atributos (técnica, físico, inteligência, motivação). Faltam número, valor/salário reais, contrato, moral, forma, lesões, cartões e estatísticas.
5. Sem artilharia, assistências, cartões ou calendário com datas (só rodadas).
6. `playRound()` joga **todas** as partidas da rodada de uma vez; a tela de partida só "reproduz" os eventos.
7. Navegação por rota-string e uma tela por botão; cada tela com estilo próprio.
8. `GameViewModel` mistura estado de UI, ciclo da temporada e persistência (269 linhas).
9. Save é um JSON único com Gson: sem versão de schema (mudar modelos pode quebrar saves antigos).
10. `data/players.csv` está vazio (só cabeçalho); `jogadores.txt` traz elencos de ~2016.
11. Dados de jogadores com nomes reais: atenção a direitos de imagem/licença.

## 4. Código duplicado
- `Brasfoot` e `FutManDDM`: entidades quase idênticas (`Esquema`, `Estadio`, `Jogador`, `Partida`, `Time`), mesmo `jogadores.txt` (o de FutManDDM é idêntico ao do app moderno).
- As regras do Brasfoot (`Esquema`, `Regras.getGols`) já foram **portadas** para `domain/Game.kt` (`Tactic`, `MatchEngine`).
- `Regras.java` do FutManDDM guarda tabelas de patrocínio/ingresso que o app moderno reimplementou em `Economy`.
- Dentro de `ui/`: tela de back/cabeçalho repetida em cada tela; `PlaceholderScreen` sem uso.

## 5. O que preservar
Todo `domain/` (motor, economia, negociações, base, troféus) · `SaveStore` (escrita atômica) · `SquadLoader` + `RemoteSource` + workflow de dados · CI · formatos de `Offer`, `Finance`, `Lineup`.

## 6. O que reorganizar
- Legados Java → `legacy/` (feito; **nada apagado**).
- `GameHubScreen` (menu de botões) → `CentralScreen` com categorias (feito).
- Telas soltas passam a viver dentro das 6 categorias.
- `GameViewModel` → dividir por responsabilidade (carreira, mercado, partida).

## 7. Arquitetura proposta (adaptada ao que existe)
```
data/        SaveStore, SquadLoader, RemoteSource        (já existe)
domain/      Game, Finance, Transfer, News, Youth...     (já existe)
ui/
  theme/     (hoje Theme.kt) cores, tipografia
  components/ GameCard, MenuTile, StatCard, ...           (criado)
  <feature>/ central, squad, match, market, season...     (migrar aos poucos)
```
Fluxo: `UI → ViewModel (estado) → domain → data/save`. Nenhuma lógica de simulação dentro de composables.
Mantido em **um módulo Gradle** (o projeto é pequeno; multi-módulo seria burocracia).

## 8. Plano
| Fase | Estado |
|---|---|
| 1 Auditoria | feita |
| 2 Arquitetura | definida (acima) |
| 3 Limpeza | legados movidos; hub antigo removido |
| 4 Sistema visual | tema + 10 componentes |
| 5 Navegação | 6 categorias em abas animadas |
| 6 Central | feita (próxima partida, forma, atenção, atividade, acesso rápido) |
| 7–19 | pendentes (Elenco, Perfil, Escalação visual, Táticas, Partida, Resultado, Mercado real, Temporada, Finanças, Estádio, Base, Notícias, Save) |
| 20 Testes | **pendente: o ambiente da auditoria não tem Gradle/SDK; é preciso compilar no CI ou no Android Studio** |
