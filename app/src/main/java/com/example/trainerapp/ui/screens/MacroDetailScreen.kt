package com.example.trainerapp.ui.screens

import android.app.Application
import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trainerapp.data.local.AppDatabase
import com.example.trainerapp.data.model.Workout
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MacroDetailViewModel(application: Application, val macroId: Long) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).appDao()
    val workouts = dao.getWorkoutsForMacro(macroId)
    private val _showDialog = MutableStateFlow(false)
    val showDialog: StateFlow<Boolean> = _showDialog.asStateFlow()
    private val _workoutName = MutableStateFlow("")
    val workoutName: StateFlow<String> = _workoutName.asStateFlow()

    fun onNameChange(name: String) { _workoutName.value = name }
    fun toggleDialog() { _showDialog.value = !_showDialog.value; _workoutName.value = "" }

    fun addWorkout(currentCount: Int) {
        val name = _workoutName.value.trim()
        if (name.isNotEmpty()) {
            viewModelScope.launch {
                dao.insertWorkout(Workout(mesocycleId = macroId, dayNumber = currentCount + 1, name = name))
                toggleDialog()
            }
        }
    }

    fun deleteWorkout(id: Long) {
        viewModelScope.launch { dao.deleteWorkout(id) }
    }
}

class MacroDetailViewModelFactory(private val application: Application, private val macroId: Long) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MacroDetailViewModel::class.java)) { @Suppress("UNCHECKED_CAST") return MacroDetailViewModel(application, macroId) as T }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MacroDetailScreen(macroId: Long, macroName: String, onBackClick: () -> Unit, onWorkoutClick: (Long, String) -> Unit) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val viewModel: MacroDetailViewModel = viewModel(factory = MacroDetailViewModelFactory(
