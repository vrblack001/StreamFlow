package dev.streamflow.android.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// ── Islamic Elegance Color Palette ──────────────────────────────────────────
// Primary: Deep Emerald Green (رمز الهوية الإسلامية والسكينة)
val IslamicEmerald       = Color(0xFF0D5C3A)
val IslamicEmeraldLight  = Color(0xFF1E8256)
val IslamicEmeraldDark   = Color(0xFF063B24)

// Secondary & Accents: Warm Gold (الذهب الأندلسي الهادئ)
val IslamicGold          = Color(0xFFD4AF37)
val IslamicGoldLight     = Color(0xFFE5C158)
val IslamicGoldContainer = Color(0xFF33290A)

// Surfaces & Backgrounds
val IslamicDarkBg        = Color(0xFF0B1410)
val IslamicDarkSurface   = Color(0xFF13201A)
val IslamicDarkSurface2  = Color(0xFF1B2D25)
val IslamicOnSurface     = Color(0xFFE2EBE5)

val IslamicLightBg       = Color(0xFFF6FAF7)
val IslamicLightSurface  = Color(0xFFFFFFFF)
val IslamicLightSurface2 = Color(0xFFEAF3ED)
val IslamicLightOnSurface= Color(0xFF13201A)

private val IslamicDarkColorScheme = darkColorScheme(
    primary              = IslamicEmeraldLight,
    onPrimary            = Color.White,
    primaryContainer     = IslamicEmeraldDark,
    onPrimaryContainer   = IslamicEmeraldLight,
    secondary            = IslamicGold,
    onSecondary          = Color.Black,
    secondaryContainer   = IslamicGoldContainer,
    onSecondaryContainer = IslamicGoldLight,
    background           = IslamicDarkBg,
    surface              = IslamicDarkSurface,
    onBackground         = IslamicOnSurface,
    onSurface            = IslamicOnSurface,
    surfaceVariant       = IslamicDarkSurface2,
    outline              = Color(0xFF2E443A)
)

private val IslamicLightColorScheme = lightColorScheme(
    primary              = IslamicEmerald,
    onPrimary            = Color.White,
    primaryContainer     = Color(0xFFD2E8DC),
    onPrimaryContainer   = IslamicEmeraldDark,
    secondary            = Color(0xFF9E7E19),
    onSecondary          = Color.White,
    secondaryContainer   = Color(0xFFF9EFCB),
    onSecondaryContainer = Color(0xFF3D3004),
    background           = IslamicLightBg,
    surface              = IslamicLightSurface,
    onBackground         = IslamicLightOnSurface,
    onSurface            = IslamicLightOnSurface,
    surfaceVariant       = IslamicLightSurface2,
    outline              = Color(0xFFC2D4CA)
)

@Composable
fun StreamFlowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> IslamicDarkColorScheme
        else      -> IslamicLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = StreamFlowTypography,
        content     = content
    )
}
