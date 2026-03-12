package com.matteo.cozyplans.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = SkyBlueDark,
    secondary = MintDark,
    tertiary = CoralDark,
    background = Night,
    surface = CardDark,
    surfaceVariant = CardDark.copy(alpha = 0.8f),
    primaryContainer = SkyBlueDark.copy(alpha = 0.22f),
    secondaryContainer = MintDark.copy(alpha = 0.22f),
    tertiaryContainer = CoralDark.copy(alpha = 0.28f),
    onPrimary = Ink,
    onSecondary = Ink,
    onTertiary = Ink,
    onPrimaryContainer = Snow,
    onSecondaryContainer = Snow,
    onTertiaryContainer = Ink,
    onBackground = Snow,
    onSurface = Snow
)

private val LightColorScheme = lightColorScheme(
    primary = SkyBlue,
    secondary = Mint,
    tertiary = Coral,
    background = Snow,
    surface = CardLight,
    surfaceVariant = Color(0xFFE9EEF8),
    primaryContainer = Color(0xFFDCE8FF),
    secondaryContainer = Color(0xFFD8F6F4),
    tertiaryContainer = Color(0xFFFFF1CC),
    onPrimary = CardLight,
    onSecondary = Ink,
    onTertiary = CardLight,
    onPrimaryContainer = Ink,
    onSecondaryContainer = Ink,
    onTertiaryContainer = Ink,
    onBackground = Ink,
    onSurface = Ink
)

@Composable
fun CozyPlansTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) DarkColorScheme else LightColorScheme
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
