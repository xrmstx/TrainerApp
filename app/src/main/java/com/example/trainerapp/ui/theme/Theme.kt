package com.example.trainerapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Typography
// ...
@Composable
fun TrainerAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(colorScheme = colorScheme, typography = AppTypography, shapes = AppShapes, content = content)
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

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun TrainerAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(colorScheme = colorScheme, shapes = AppShapes, content = content)
}
