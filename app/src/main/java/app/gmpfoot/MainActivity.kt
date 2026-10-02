package app.gmpfoot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.gmpfoot.ui.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GMPFootTheme {
                Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                    val vm: GameViewModel = viewModel()
                    val nav = rememberNavController()
                    val back: () -> Unit = { nav.popBackStack() }
                    NavHost(nav, startDestination = "home") {
                        composable("home") {
                            HomeScreen(vm.hasSave) { route ->
                                if (route == "continue") {
                                    if (vm.loadSave()) nav.navigate("hub")
                                } else nav.navigate(route)
                            }
                        }
                        composable("select/pro") {
                            TeamSelectScreen(vm, live = false, onStart = {
                                nav.navigate("hub") { popUpTo("home") }
                            }, onBack = back)
                        }
                        composable("select/live") {
                            TeamSelectScreen(vm, live = true, onStart = {
                                nav.navigate("hub") { popUpTo("home") }
                            }, onBack = back)
                        }
                        composable("hub") { GameHubScreen(vm, onOpen = { nav.navigate(it) }, onBack = { nav.popBackStack("home", false) }) }
                        composable("lineup") { LineupScreen(vm, back) }
                        composable("match") { MatchScreen(vm, onDone = back) }
                        composable("standings") { StandingsScreen(vm, back) }
                        composable("finance") { FinanceScreen(vm, back) }
                        composable("offers") { OffersScreen(vm, back) }
                        composable("youth") { YouthScreen(vm, back) }
                        composable("transfer") { TransferScreen(back) }
                        composable("managers") { ManagerMarketScreen(back) }
                        composable("trophies") { TrophyRoomScreen(vm.trophies, back) }
                        composable("academy") { AcademyScreen(vm, back) }
                        composable("editor") { DataSourceScreen(vm, back) }
                    }
                }
            }
        }
    }
}
