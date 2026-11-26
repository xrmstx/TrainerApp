package com.example.trainerapp.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

enum class ThemeMode(
    val label: String,
    val description: String,
    val isLight: Boolean
) {
    LIGHT("Калькулятор", "Светлая тема, индиго-акцент", isLight = true),
    SPORT("Спорт", "Тёмный графит, лайм-акцент", isLight = false),
    MONO("Моно", "Чёрно-белая база, тиффани-акцент", isLight = false);

    companion object {
        // Читает сохранённое имя; «DARK» — значение из старой версии приложения,
        // мигрируем его в «Спорт», чтобы у пользователей не сбилась тема
        fun fromName(name: String?): ThemeMode = when (name) {
            LIGHT.name -> LIGHT
            MONO.name -> MONO
            SPORT.name, "DARK" -> SPORT
            else -> SPORT
        }
    }
}

fun colorSchemeFor(mode: ThemeMode): ColorScheme = when (mode) {
    ThemeMode.LIGHT -> LightScheme
    ThemeMode.SPORT -> SportScheme
    ThemeMode.MONO -> MonoScheme
}

@Composable
fun TrainerAppTheme(
    themeMode: ThemeMode,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = colorSchemeFor(themeMode),
        typography = AppTypography,
        content = content
    )
}
