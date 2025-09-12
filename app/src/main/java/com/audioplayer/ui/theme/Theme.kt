package com.audioplayer.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = MusicPrimary,
    onPrimary = Color.White,
    primaryContainer = MusicPrimaryDark,
    onPrimaryContainer = Color.White,
    secondary = MusicSecondary,
    onSecondary = Color.White,
    secondaryContainer = MusicSecondaryDark,
    onSecondaryContainer = Color.White,
    tertiary = Pink80,
    background = MusicBackground,
    onBackground = MusicOnSurface,
    surface = MusicSurface,
    onSurface = MusicOnSurface,
    surfaceVariant = MusicSurface,
    onSurfaceVariant = MusicOnSurface.copy(alpha = 0.7f)
)

private val LightColorScheme = lightColorScheme(
    primary = MusicPrimary,
    onPrimary = Color.White,
    primaryContainer = MusicPrimaryLight,
    onPrimaryContainer = Color.Black,
    secondary = MusicSecondary,
    onSecondary = Color.White,
    secondaryContainer = MusicSecondaryLight,
    onSecondaryContainer = Color.Black,
    tertiary = Pink40,
    background = Color(0xFFFFFBFE),
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFBFE),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F)
)

@Composable
fun AudioPlayerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}