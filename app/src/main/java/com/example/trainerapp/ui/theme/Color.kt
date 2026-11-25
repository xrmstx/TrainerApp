package com.example.trainerapp.ui.theme

import androidx.compose.ui.graphics.Color

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
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFF3E9E4)
val LightText = Color(0xFF201A17)
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
