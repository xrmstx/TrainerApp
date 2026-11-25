package com.example.trainerapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.trainerapp.data.local.AppDatabase
import com.example.trainerapp.data.model.Macrocycle
import com.example.trainerapp.data.model.Workout
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TemplateViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).appDao()
    val macrocycles = dao.getAllMacrocycles()

    private val _newMacroName = MutableStateFlow("")
    val newMacroName: StateFlow<String> = _newMacroName.asStateFlow()
    private val _newMacroWeeks = MutableStateFlow("")
    val newMacroWeeks: StateFlow<String> = _newMacroWeeks.asStateFlow()
    private val _weeksError = MutableStateFlow(false)
    val weeksError: StateFlow<Boolean> = _weeksError.asStateFlow()
    private val _newMacroSessions = MutableStateFlow(3)
    val newMacroSessions: StateFlow<Int> = _newMacroSessions.asStateFlow()

    fun onNameChange(name: String) { _newMacroName.value = name }

    fun onWeeksChange(weeks: String) {
        _newMacroWeeks.value = weeks
        _weeksError.value = false
    }

    fun onSessionsChange(count: Int) { _newMacroSessions.value = count }

    fun addMacrocycle() {
        val name = _newMacroName.value.trim()
        val weeks = _newMacroWeeks.value.trim().toIntOrNull() ?: 0
        if (name.isEmpty()) return
        if (weeks !in 1..52) { _weeksError.value = true; return }
        val sessions = _newMacroSessions.value
        viewModelScope.launch {
            val macroId = dao.insertMacrocycle(
                Macrocycle(name = name, durationWeeks = weeks, sessionsPerWeek = sessions)
            )
            // Автогенерация слотов первой недели: тренировки созданы — остаётся наполнить их блоками
            (1..sessions).forEach { day ->
                dao.insertWorkout(
                    Workout(
                        mesocycleId = macroId,
                        weekNumber = 1,
                        dayNumber = day,
                        dayType = "TRAINING",
                        name = "Тренировка $day"
                    )
                )
            }
            _newMacroName.value = ""
            _newMacroWeeks.value = ""
        }
    }

    fun deleteMacrocycle(id: Long) {
        viewModelScope.launch { dao.deleteMacrocycle(id) } // каскад чистит тренировки, блоки и упражнения
    }
}
