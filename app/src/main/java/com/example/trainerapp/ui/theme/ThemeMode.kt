package com.example.trainerapp.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

enum class ThemeMode(val label: String, val description: String) {
    DARK("Тёмная", "Графит и спокойный акцент — как сейчас"),
    LIGHT("Светлая", "Минималистичная, бумажный фон, синий акцент"),
    NEON("Неон", "Тёмный космос с фиолетовым свечением");

    companion object {
        fun fromName(name: String?): ThemeMode =
            entries.firstOrNull { it.name == name } ?: DARK
    }
}

// Светлая схема — по варианту 02 из макета
val LightColorScheme = lightColorScheme(
    primary = Color(0xFF556FE5),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDDE3FF),
    onPrimaryContainer = Color(0xFF0B1A5C),
    secondary = Color(0xFF5A6156),
    onSecondary = Color.White,
    tertiary = Color(0xFF0F8A80), // бирюза восстановительных дней
    background = Color(0xFFF1F2ED),
    onBackground = Color(0xFF20241F),
    surface = Color.White,
    onSurface = Color(0xFF20241F),
    surfaceVariant = Color(0xFFEDEFE9),
    onSurfaceVariant = Color(0xFF555D52),
    error = Color(0xFFB3261E),
    onError = Color.White
)

// Неон — по варианту 03 из макета
val NeonColorScheme = darkColorScheme(
    primary = Color(0xFFDF70FF),
    onPrimary = Color(0xFF2A0A33),
    primaryContainer = Color(0xFF3A1A4D),
    onPrimaryContainer = Color(0xFFF3D9FF),
    secondary = Color(0xFF9C8BFF),
    tertiary = Color(0xFF64F0DC), // восстановительные дни — неоновая бирюза
    background = Color(0xFF090817),
    onBackground = Color(0xFFF7F4FF),
    surface = Color(0xFF141326),
    onSurface = Color(0xFFF7F4FF),
    surfaceVariant = Color(0xFF201D39),
    onSurfaceVariant = Color(0xFFAAA4C4),
    error = Color(0xFFFF7A93),
    onError = Color(0xFF2A0A12)
)
