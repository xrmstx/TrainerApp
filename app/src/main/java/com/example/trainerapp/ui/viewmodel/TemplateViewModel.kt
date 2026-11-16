package com.example.trainerapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.trainerapp.data.local.AppDatabase
import com.example.trainerapp.data.model.Macrocycle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TemplateViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val dao = db.appDao()

    val macrocycles = dao.getAllMacrocycles()

    private val _newMacroName = MutableStateFlow("")
    val newMacroName: StateFlow<String> = _newMacroName.asStateFlow()

    private val _newMacroWeeks = MutableStateFlow("")
    val newMacroWeeks: StateFlow<String> = _newMacroWeeks.asStateFlow()

    fun onNameChange(name: String) { _newMacroName.value = name }
    fun onWeeksChange(weeks: String) { _newMacroWeeks.value = weeks }

    fun addMacrocycle() {
        val name = _newMacroName.value.trim()
        val weeks = _newMacroWeeks.value.trim().toIntOrNull() ?: 0
        if (name.isNotEmpty() && weeks > 0) {
            viewModelScope.launch {
                dao.insertMacrocycle(Macrocycle(name = name, durationWeeks = weeks))
                _newMacroName.value = ""
                _newMacroWeeks.value = ""
            }
        }
    }
}
