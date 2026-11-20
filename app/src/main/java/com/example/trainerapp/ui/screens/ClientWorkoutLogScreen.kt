package com.example.trainerapp.ui.screens

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trainerapp.data.local.AppDatabase
import com.example.trainerapp.data.model.ExerciseLog
import com.example.trainerapp.data.model.TemplateExercise
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ClientWorkoutLogViewModel(application: Application, workoutId: Long, clientProgramId: Long) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).appDao()

    val exercises = MutableStateFlow<List<TemplateExercise>>(emptyList())

    init {
        viewModelScope.launch {
            // Для простоты мы берем первый блок упражнений этой тренировки
            // В реальном приложении тут будет более сложная логика
            dao.getExercisesForWorkout(workoutId).collect { list ->
                exercises.value = list
            }
        }
    }

    fun saveLog(exercise: TemplateExercise, actualSets: String, actualReps: String, actualWeight: String) {
        val sets = actualSets.toIntOrNull() ?: 0
        val reps = actualReps.toIntOrNull() ?: 0
        val weight = actualWeight.toDoubleOrNull() ?: 0.0

        if (sets > 0 && reps > 0) {
            viewModelScope.launch {
                dao.insertExerciseLog(
                    ExerciseLog(
                        clientProgramId = clientProgramId,
                        exerciseName = exercise.exerciseName,
                        actualSets = sets,
                        actualReps = reps,
                        actualWeight = weight
                    )
                )
            }
        }
    }
}

class ClientWorkoutLogViewModelFactory(
    private val application: Application,
    private val workoutId: Long,
    private val clientProgramId: Long
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ClientWorkoutLogViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ClientWorkoutLogViewModel(application, workoutId, clientProgramId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientWorkoutLogScreen(
    workoutId: Long,
    clientProgramId: Long,
    workoutName: String,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val viewModel: ClientWorkoutLogViewModel = viewModel(
        factory = ClientWorkoutLogViewModelFactory(application, workoutId, clientProgramId)
    )

    val exercises by viewModel.exercises.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(workoutName) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)
        ) {
            if (exercises.isEmpty()) {
                Text("В этой тренировке нет упражнений.", modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                LazyColumn {
                    items(exercises) { ex ->
                        var sets by remember { mutableStateOf(ex.targetSets.toString()) }
                        var reps by remember { mutableStateOf(ex.targetReps) }
                        var weight by remember { mutableStateOf("") }

                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(ex.exerciseName, style = MaterialTheme.typography.titleMedium)
                                Text("План: ${ex.targetSets} x ${ex.targetReps}", style = MaterialTheme.typography.bodySmall)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row {
                                    OutlinedTextField(
                                        value = sets,
                                        onValueChange = { sets = it },
                                        label = { Text("Подходы") },
                                        modifier = Modifier.weight(1f),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    OutlinedTextField(
                                        value = reps,
                                        onValueChange = { reps = it },
                                        label = { Text("Повторы") },
                                        modifier = Modifier.weight(1f),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    OutlinedTextField(
                                        value = weight,
                                        onValueChange = { weight = it },
                                        label = { Text("Вес") },
                                        modifier = Modifier.weight(1f),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { 
                                        viewModel.saveLog(ex, sets, reps, weight) 
                                        // Очищаем поле веса после сохранения
                                        weight = ""
                                    },
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Text("Сохранить")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
