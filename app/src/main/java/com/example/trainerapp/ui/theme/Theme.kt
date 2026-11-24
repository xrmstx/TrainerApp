package com.example.trainerapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(primary = AccentOrange, secondary = AccentPink, background = DarkBackground, surface = DarkSurface, onPrimary = Color.White, onSecondary = Color.White, onBackground = DarkText, onSurface = DarkText)
private val LightColorScheme = lightColorScheme(primary = AccentOrange, secondary = AccentPink, background = LightBackground, surface = LightSurface, onPrimary = Color.White, onSecondary = Color.White, onBackground = LightText, onSurface = LightText)

@Composable
fun TrainerAppTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(colorScheme = colorScheme, content = content)
}
