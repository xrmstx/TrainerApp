package com.example.trainerapp.data.local

import android.content.Context
import com.example.trainerapp.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AppSettings {
    private const val PREFS = "trainer_app_settings"
    private const val KEY_THEME = "theme_mode"

    private val _theme = MutableStateFlow(ThemeMode.SPORT)
    val theme: StateFlow<ThemeMode> = _theme.asStateFlow()

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    // Вызвать один раз в MainActivity.onCreate, до setContent
    fun init(context: Context) {
        _theme.value = ThemeMode.fromName(prefs(context).getString(KEY_THEME, null))
    }

    fun setTheme(context: Context, mode: ThemeMode) {
        prefs(context).edit().putString(KEY_THEME, mode.name).apply()
        _theme.value = mode
    }
}
