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


// Основной акцент — энергичный оранжевый (замените на ваши прежние hex, если были)
val AccentOrange = Color(0xFFFF7043)
val OrangeDark = Color(0xFF7A2E0F)
val OrangeContainerLight = Color(0xFFFFDBCE)
val OnOrangeContainerLight = Color(0xFF3B0A00)
val OnOrangeContainerDark = Color(0xFFFFDBCE)

// Второстепенный — розовый
val AccentPink = Color(0xFFF06292)
val PinkDark = Color(0xFF8E2A50)
val PinkContainerLight = Color(0xFFFFD9E2)
val OnPinkContainerLight = Color(0xFF3E001D)
val OnPinkContainerDark = Color(0xFFFFD9E2)

// Третичный — бирюзовый: восстановительные дни, общий прогресс
val RecoveryTeal = Color(0xFF00897B)
val RecoveryTealDark = Color(0xFF4DB6AC)
val TealContainerLight = Color(0xFFB2DFDB)
val OnTealContainerLight = Color(0xFF00201C)
val TealContainerDark = Color(0xFF00504A)
val OnTealContainerDark = Color(0xFF9CF2E7)

// Тёмная тема
val DarkBackground = Color(0xFF141414)
val DarkSurface = Color(0xFF1E1E1E)
val DarkSurfaceVariant = Color(0xFF2C2C2E)
val DarkText = Color(0xFFEAEAEA)
val DarkTextSecondary = Color(0xFFA6A6A6)
val DarkOutline = Color(0xFF494949)

// Светлая тема
val LightBackground = Color(0xFFFCF8F5)
val LightSurfaceVariant = Color(0xFFF3E9E4)
val LightTextSecondary = Color(0xFF5D4A42)
val LightOutline = Color(0xFF85736C)

// Статусы тренировок (используются на клиентских экранах)
val StatusDone = Color(0xFF4CAF50)
val StatusPlanned = Color(0xFF90A4AE)

// Ошибки
val ErrorLight = Color(0xFFBA1A1A)
val ErrorDark = Color(0xFFFFB4AB)
// Градиенты аватаров клиентов: цвет подбирается по id клиента
val AvatarGradients: List<List<Color>> = listOf(
    listOf(Color(0xFFFF7043), Color(0xFFFF8A65)),
    listOf(Color(0xFF4DB6AC), Color(0xFF80CBC4)),
    listOf(Color(0xFFF06292), Color(0xFFF48FB1)),
    listOf(Color(0xFF7E57C2), Color(0xFFB39DDB)),
    listOf(Color(0xFF66BB6A), Color(0xFFA5D6A7)),
    listOf(Color(0xFF42A5F5), Color(0xFF90CAF9)),
    listOf(Color(0xFFFFCA28), Color(0xFFFFE082)),
    listOf(Color(0xFF8D6E63), Color(0xFFBCAAA4))
)
