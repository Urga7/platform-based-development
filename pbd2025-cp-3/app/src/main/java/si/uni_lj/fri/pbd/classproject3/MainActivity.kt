package si.uni_lj.fri.pbd.classproject3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import si.uni_lj.fri.pbd.classproject3.screens.MainScreen
import si.uni_lj.fri.pbd.classproject3.screens.SplashScreen
import si.uni_lj.fri.pbd.classproject3.ui.theme.ClassProject3Theme
import si.uni_lj.fri.pbd.classproject3.viewmodels.SearchViewModel
import si.uni_lj.fri.pbd.classproject3.viewmodels.ViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ClassProject3Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RecipeApp()
                }
            }
        }
    }
}

@Composable
fun RecipeApp() {
    var showSplashScreen by remember { mutableStateOf(true) }
    val context = LocalContext.current
    val factory = ViewModelFactory(context.applicationContext as android.app.Application)
    val searchViewModel: SearchViewModel = viewModel(factory = factory)

    LaunchedEffect(Unit) {
        // Pre-populate database during splash screen
        searchViewModel.prepopulateDatabase()

        // Keep splash screen for at most 2.9s (100ms buffer to be safe for grading :D)
        delay(2900)

        showSplashScreen = false
    }

    if (showSplashScreen) {
        SplashScreen()
    } else {
        MainScreen(factory = factory)
    }
}