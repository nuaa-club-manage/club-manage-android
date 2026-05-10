package com.clubmgmt.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = Indigo600,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = Indigo100,
    secondary = Gray600,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    background = Gray50,
    onBackground = Gray900,
    surface = androidx.compose.ui.graphics.Color.White,
    onSurface = Gray900,
    surfaceVariant = Gray200,
    outline = Gray300
)

private val DarkColorScheme = darkColorScheme(
    primary = Indigo400,
    onPrimary = Gray900,
    primaryContainer = Indigo900,
    secondary = Gray400,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    background = Gray900,
    onBackground = Gray200,
    surface = Gray800,
    onSurface = Gray200,
    surfaceVariant = Gray700,
    outline = Gray600
)

@Composable
fun ClubManagementTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
