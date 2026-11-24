package com.example.trainerapp.ui.screens

import android.app.Application
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trainerapp.data.local.AppDatabase
import com.example.trainerapp.data.model.ExerciseLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ClientWorkoutLogViewModel(application: Application, workoutId: Long) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).appDao()
    val exercises = dao.getClientExercises(workoutId)

    private val _historyDialog = MutableStateFlow<Pair<String, List<ExerciseLog>>?>(null)
    val historyDialog: StateFlow<Pair<String, List<ExerciseLog>>?> = _historyDialog.asStateFlow()

    fun showHistory(exerciseName: String, exerciseId: Long) {
        viewModelScope.launch {
            val logs = mutableListOf<ExerciseLog>()
            dao.getLogsForExercise(exerciseId).collect { logs.clear(); logs.addAll(it); _historyDialog.value = Pair(exerciseName, logs.toList()) }
        }
    }
    fun closeHistory() { _historyDialog.value = null }
    fun deleteLog(logId: Long) { viewModelScope.launch { dao.deleteLog(logId); closeHistory() } }

    suspend fun getLastWeight(exerciseId: Long): Double? = dao.getLastLogForExercise(exerciseId)?.actualWeight

    fun saveLog(exerciseId: Long, setNum: String, reps: String, weight: String, note: String, isCircuit: Boolean) {
        val s = setNum.toIntOrNull() ?: 1
        val r = reps.toIntOrNull() ?: 0
        val w = weight.toDoubleOrNull() ?: 0.0
        if (r > 0 || isCircuit) {
            viewModelScope.launch {
                dao.insertExerciseLog(ExerciseLog(clientExerciseId = exerciseId, setNumber = s, actualReps = r, actualWeight = w, resultText = if (isCircuit) reps else null, note = note))
            }
        }
    }
}

class ClientWorkoutLogViewModelFactory(private val application: Application, private val workoutId: Long) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ClientWorkoutLogViewModel::class.java)) { @Suppress("UNCHECKED_CAST") return ClientWorkoutLogViewModel(application, workoutId) as T }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ClientWorkoutLogScreen(workoutId: Long, workoutName: String
