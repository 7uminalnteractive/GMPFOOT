# GMPFoot (Brasfoot moderno)

Android nativo (Kotlin + Jetpack Compose). Build: GitHub Actions → **Android CI** → Artifacts → `GMPFoot-debug-apk`.

## O que tem
- Escolha de time, escalação (5 formações, 5 esquemas), partida narrada, campeonato turno/returno, classificação
- Salvar/continuar jogo (`save.json` no armazenamento do app)
- Finanças: bilheteria, salários, patrocínio master, fornecedor, naming rights, ampliação do estádio, SAF
- Clube de base (Sub-20) como carreira própria e promoção de talentos
- Academia de Treinadores (tutorial com aulas e licença)
- Mercado de jogadores com cláusulas e mercado de técnicos (ainda com dados de exemplo)

## Dados reais
1. Coloque seus dados em `data/players.csv` (colunas em `scripts/build_teams_json.py`) ou defina a variável `DATA_CSV_URL` do repositório.
2. O workflow **Atualizar dados** gera `data/teams.json` (manual ou toda segunda).
3. No app: **Editor de Times** → cole a URL raw do `teams.json` → Importar → nova carreira.

Atenção: a licença da fonte dos dados é sua responsabilidade (scraping de sites como ogol/Transfermarkt costuma ser proibido pelos termos).

## Estrutura do repositório
- `app/` — app Android atual (Kotlin + Compose). É o que o CI compila.
- `legacy/` — protótipos Java antigos (Brasfoot, FutManDDM), mantidos só como referência de regras.
- `docs/AUDITORIA.md` — auditoria, arquitetura proposta e plano por fases.
