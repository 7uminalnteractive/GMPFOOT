package app.gmpfoot.data

import app.gmpfoot.domain.Team

/**
 * Fonte de dados do editor de times. Implementações plugáveis:
 * API (API-Football, football-data.org), arquivo JSON importado, etc.
 */
interface TeamDataSource {
    val name: String
    suspend fun fetchTeam(teamId: String): Team
    suspend fun searchTeams(query: String): List<Team>
}
