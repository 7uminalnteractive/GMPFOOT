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
                    val nav = rememberNavController()
                    val back = { nav.popBackStack(); Unit }
                    NavHost(nav, startDestination = "home") {
                        composable("home") { HomeScreen { nav.navigate(it) } }
                        composable("managers") { ManagerMarketScreen(back) }
                        composable("pro") { PlaceholderScreen("Carreira Profissional", "Em construção.", back) }
                        composable("live") { PlaceholderScreen("Carreira Ao Vivo", "Em construção.", back) }
                        composable("editor") { PlaceholderScreen("Editor de Times", "Em construção: importação via TeamDataSource.", back) }
                    }
                }
            }
        }
    }
}
