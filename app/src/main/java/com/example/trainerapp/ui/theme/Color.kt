package com.example.trainerapp.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// ---------- Калькулятор: светлая, индиго ----------
private val Indigo = Color(0xFF3F51B5)
private val LightBg = Color(0xFFF7F8FC)
private val LightSurface = Color(0xFFFFFFFF)
private val LightText = Color(0xFF1A1C2B)
private val LightSubtext = Color(0xFF6B6F80)

// ---------- Спорт: тёмный графит, лайм ----------
private val Graphite = Color(0xFF14161A)
private val GraphiteSurface = Color(0xFF1E222A)
private val GraphiteText = Color(0xFFEDEFF3)
private val GraphiteSubtext = Color(0xFF9AA0AC)
private val Lime = Color(0xFFB4E33D)

// ---------- Моно: ч/б, тиффани ----------
private val MonoBg = Color(0xFF101012)
private val MonoSurface = Color(0xFF1C1C1F)
private val MonoText = Color(0xFFF2F2F2)
private val MonoSubtext = Color(0xFF8E8E93)
private val Tiffany = Color(0xFF0ABAB5)

val LightScheme: ColorScheme = lightColorScheme(
    primary = Indigo,
    onPrimary = Color.White,
    background = LightBg,
    onBackground = LightText,
    surface = LightSurface,
    onSurface = LightText,
    onSurfaceVariant = LightSubtext,
    surfaceVariant = Color(0xFFE8EAF2),
    outline = Color(0xFFC9CCD8)
)

val SportScheme: ColorScheme = darkColorScheme(
    primary = Lime,
    onPrimary = Graphite,
    background = Graphite,
    onBackground = GraphiteText,
    surface = GraphiteSurface,
    onSurface = GraphiteText,
    onSurfaceVariant = GraphiteSubtext,
    surfaceVariant = Color(0xFF2A2F38),
    outline = Color(0xFF3A404C)
)

val MonoScheme: ColorScheme = darkColorScheme(
    primary = Tiffany,
    onPrimary = Color(0xFF062F2D),
    background = MonoBg,
    onBackground = MonoText,
    surface = MonoSurface,
    onSurface = MonoText,
    onSurfaceVariant = MonoSubtext,
    surfaceVariant = Color(0xFF2A2A2E),
    outline = Color(0xFF3C3C40)
)
