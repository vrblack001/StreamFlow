package dev.streamflow.android.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.AndroidEntryPoint
import dev.streamflow.android.ui.theme.StreamFlowTheme
import dev.streamflow.android.ui.navigation.StreamFlowNavGraph
import dev.streamflow.android.utils.PreferencesManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var prefsManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Handle shared YouTube URL
        val sharedVideoId = intent?.getStringExtra(Intent.EXTRA_TEXT)
            ?.let { extractVideoId(it) }

        setContent {
            val themeMode by prefsManager.themeMode.collectAsState(initial = "system")
            val dynamicColor by prefsManager.dynamicColor.collectAsState(initial = true)

            val isDark = when (themeMode) {
                "dark"  -> true
                "light" -> false
                else    -> isSystemInDarkTheme()
            }

            StreamFlowTheme(darkTheme = isDark, dynamicColor = dynamicColor) {
                StreamFlowNavGraph(startVideoId = sharedVideoId)
            }
        }
    }

    private fun extractVideoId(url: String): String? {
        // Handles: youtu.be/ID, youtube.com/watch?v=ID, youtube.com/shorts/ID
        val patterns = listOf(
            Regex("youtu\\.be/([A-Za-z0-9_-]{11})"),
            Regex("[?&]v=([A-Za-z0-9_-]{11})"),
            Regex("shorts/([A-Za-z0-9_-]{11})")
        )
        return patterns.firstNotNullOfOrNull { it.find(url)?.groupValues?.get(1) }
    }
}
