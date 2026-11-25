package dev.streamflow.android.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// ── Colour palette ───────────────────────────────────────────────────────────
private val StreamRed    = Color(0xFFFF0000)
private val StreamDark   = Color(0xFF0F0F0F)
private val StreamSurface = Color(0xFF1A1A1A)
private val StreamOnSurface = Color(0xFFE0E0E0)

private val DarkColorScheme = darkColorScheme(
    primary          = StreamRed,
    onPrimary        = Color.White,
    primaryContainer = Color(0xFF8B0000),
    secondary        = Color(0xFFCF6679),
    background       = StreamDark,
    surface          = StreamSurface,
    onBackground     = Color.White,
    onSurface        = StreamOnSurface,
    surfaceVariant   = Color(0xFF282828),
    outline          = Color(0xFF3A3A3A)
)

private val LightColorScheme = lightColorScheme(
    primary          = StreamRed,
    onPrimary        = Color.White,
    primaryContainer = Color(0xFFFFDAD6),
    secondary        = Color(0xFF904A4A),
    background       = Color(0xFFFFFBFF),
    surface          = Color.White,
    onBackground     = Color(0xFF1C1B1F),
    onSurface        = Color(0xFF1C1B1F)
)

@Composable
fun StreamFlowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        // Material You dynamic color — Android 12+
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else      -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = StreamFlowTypography,
        content     = content
    )
}
