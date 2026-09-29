package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF0369A1),
    onPrimaryContainer = Color.White,
    secondary = DarkAccentIndigo,
    onSecondary = Color.White,
    secondaryContainer = DarkSecondarySurface,
    onSecondaryContainer = DarkPrimaryText,
    tertiary = AppleGreen,
    onTertiary = Color.White,
    background = DarkBackground,
    onBackground = DarkPrimaryText,
    surface = DarkCardSurface,
    onSurface = DarkPrimaryText,
    surfaceVariant = DarkSecondarySurface,
    onSurfaceVariant = DarkMutedText,
    outline = DarkCardBorder,
    outlineVariant = DarkSubtleSeparator,
    error = AppleRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = LightPrimaryBlue,
    secondary = LightAccentIndigo,
    onSecondary = Color.White,
    secondaryContainer = LightSecondarySurface,
    onSecondaryContainer = LightPrimaryText,
    tertiary = AppleGreen,
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = LightPrimaryText,
    surface = LightCardSurface,
    onSurface = LightPrimaryText,
    surfaceVariant = LightSecondarySurface,
    onSurfaceVariant = LightMutedText,
    outline = LightCardBorder,
    outlineVariant = LightSubtleSeparator,
    error = AppleRed,
    onError = Color.White
)

@Composable
fun CrimeLensTheme(
    isDarkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (isDarkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    CrimeLensTheme(isDarkTheme = darkTheme, content = content)
}



