package com.example.trainerapp.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Три темы на выбор: тёмная (по умолчанию), светлая и неон
enum class ThemeMode(val label: String, val description: String) {
    DARK("Тёмная", "Графит и оранжевый акцент"),
    LIGHT("Светлая", "Бумажный фон, деликатные тени"),
    NEON("Неон", "Тёмный космос с фиолетовым свечением");

    companion object {
        fun fromName(name: String?): ThemeMode =
            entries.firstOrNull { it.name == name } ?: DARK
    }
}

private val DarkColorScheme = darkColorScheme(
    primary = AccentOrange,
    onPrimary = Color.White,
    primaryContainer = OrangeDark,
    onPrimaryContainer = OnOrangeContainerDark,

    secondary = AccentPink,
    onSecondary = Color.White,
    secondaryContainer = PinkDark,
    onSecondaryContainer = OnPinkContainerDark,

    tertiary = RecoveryTealDark,
    onTertiary = Color(0xFF003731),
    tertiaryContainer = TealContainerDark,
    onTertiaryContainer = OnTealContainerDark,

    error = ErrorDark,
    onError = Color(0xFF690005),

    background = DarkBackground,
    onBackground = DarkText,
    surface = DarkSurface,
    onSurface = DarkText,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkOutline
)

private val LightColorScheme = lightColorScheme(
    primary = AccentOrange,
    onPrimary = Color.White,
    primaryContainer = OrangeContainerLight,
    onPrimaryContainer = OnOrangeContainerLight,

    secondary = AccentPink,
    onSecondary = Color.White,
    secondaryContainer = PinkContainerLight,
    onSecondaryContainer = OnPinkContainerLight,

    tertiary = RecoveryTeal,
    onTertiary = Color.White,
    tertiaryContainer = TealContainerLight,
    onTertiaryContainer = OnTealContainerLight,

    error = ErrorLight,
    onError = Color.White,

    background = LightBackground,
    onBackground = LightText,
    surface = LightSurface,
    onSurface = LightText,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = LightOutline
)

// Неон: тёмная база с фиолетовым свечением — по варианту 03 из макета
private val NeonColorScheme = darkColorScheme(
    primary = Color(0xFFDF70FF),
    onPrimary = Color(0xFF2A0A33),
    primaryContainer = Color(0xFF3A1A4D),
    onPrimaryContainer = Color(0xFFF3D9FF),

    secondary = Color(0xFF9C8BFF),
    onSecondary = Color(0xFF1A1020),
    secondaryContainer = Color(0xFF2B2450),
    onSecondaryContainer = Color(0xFFE4DEFF),

    tertiary = Color(0xFF64F0DC), // неоновая бирюза — восстановительные дни
    onTertiary = Color(0xFF003731),
    tertiaryContainer = Color(0xFF0E4A44),
    onTertiaryContainer = Color(0xFFB8FFF4),

    error = Color(0xFFFF7A93),
    onError = Color(0xFF2A0A12),

    background = Color(0xFF090817),
    onBackground = Color(0xFFF7F4FF),
    surface = Color(0xFF141326),
    onSurface = Color(0xFFF7F4FF),
    surfaceVariant = Color(0xFF201D39),
    onSurfaceVariant = Color(0xFFAAA4C4),
    outline = Color(0xFF4A3F66)
)

fun colorSchemeFor(mode: ThemeMode) = when (mode) {
    ThemeMode.DARK -> DarkColorScheme
    ThemeMode.LIGHT -> LightColorScheme
    ThemeMode.NEON -> NeonColorScheme
}

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

private val AppFontFamily = FontFamily.Default

private val AppTypography = Typography(
    headlineMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 32.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp
    ),
    titleMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        lineHeight = 23.sp
    ),
    titleSmall = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 20.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 17.sp
    ),
    labelLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 19.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.3.sp
    ),
    labelSmall = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 15.sp,
        letterSpacing = 0.4.sp
    )
)

@Composable
fun TrainerAppTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = colorSchemeFor(themeMode),
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
