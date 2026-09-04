package com.vanikanoon.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = LegalDeepBlue,
    onPrimary = LegalSurfaceWhite,
    primaryContainer = LegalBlueLight,
    onPrimaryContainer = LegalSurfaceWhite,
    secondary = LegalGoldPrimary,
    onSecondary = LegalDeepBlue,
    secondaryContainer = LegalGoldContainer,
    onSecondaryContainer = LegalGoldDark,
    tertiary = LegalBlueHighlight,
    onTertiary = LegalSurfaceWhite,
    background = LegalBgLight,
    onBackground = LegalTextPrimary,
    surface = LegalSurfaceWhite,
    onSurface = LegalTextPrimary,
    surfaceVariant = LegalSurfaceVariant,
    onSurfaceVariant = LegalTextSecondary,
    outline = LegalBorder,
    error = LegalError,
    onError = LegalSurfaceWhite
)

private val DarkColorScheme = darkColorScheme(
    primary = LegalGoldPrimary,
    onPrimary = LegalBlueDark,
    primaryContainer = LegalDeepBlue,
    onPrimaryContainer = LegalGoldLight,
    secondary = LegalGoldLight,
    onSecondary = LegalBlueDark,
    secondaryContainer = LegalBlueLight,
    onSecondaryContainer = LegalGoldLight,
    tertiary = LegalGoldPrimary,
    onTertiary = LegalBlueDark,
    background = LegalBlueDark,
    onBackground = LegalSurfaceWhite,
    surface = LegalDeepBlue,
    onSurface = LegalSurfaceWhite,
    surfaceVariant = LegalBlueLight,
    onSurfaceVariant = LegalGoldLight,
    outline = LegalBlueHighlight,
    error = LegalError,
    onError = LegalSurfaceWhite
)

@Composable
fun VaniKanoonTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = LegalDeepBlue.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
