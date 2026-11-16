package com.example.trainerapp.ui.screens

import android.app.Application
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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

// ViewModel для экрана шаблона
class MacroDetailViewModel(application: Application, macroId: Long) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).appDao()

    val workouts = dao.getWorkoutsForMacro(macroId)

    private val _showDialog = MutableStateFlow(false)
    val showDialog: StateFlow<Boolean> = _showDialog.asStateFlow()

    private val _workoutName = MutableStateFlow("")
    val workoutName: StateFlow<String> = _workoutName.asStateFlow()

    fun onNameChange(name: String) {
        _workoutName.value = name
    }

    fun toggleDialog() {
        _showDialog.value = !_showDialog.value
        _workoutName.value = ""
    }

    fun addWorkout(currentCount: Int) {
        val name = _workoutName.value.trim()
        if (name.isNotEmpty()) {
            viewModelScope.launch {
                dao.insertWorkout(Workout(mesocycleId = macroId, dayNumber = currentCount + 1, name = name))
                toggleDialog()
            }
        }
    }
}

// Фабрика для создания ViewModel с параметром macroId
class MacroDetailViewModelFactory(
    private val application: Application,
    private val macroId: Long
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MacroDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MacroDetailViewModel(application, macroId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MacroDetailScreen(
    macroId: Long,
    macroName: String,
    onBackClick: () -> Unit,
    onWorkoutClick: (Long, String) -> Unit // <-- ДОБАВИЛИ ПАРАМЕТР ДЛЯ ПЕРЕХОДА
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application

    val viewModel: MacroDetailViewModel = viewModel(
        factory = MacroDetailViewModelFactory(application, macroId)
    )

    val workouts by viewModel.workouts.collectAsState(initial = emptyList())
    val showDialog by viewModel.showDialog.collectAsState()
    val workoutName by viewModel.workoutName.collectAsState()

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.toggleDialog() },
            title = { Text("Новая тренировка") },
            text = {
                OutlinedTextField(
                    value = workoutName,
                    onValueChange = { viewModel.onNameChange(it) },
                    label = { Text("Название (напр. Верх тела)") }
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.addWorkout(workouts.size) }) {
                    Text("Добавить")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.toggleDialog() }) {
                    Text("Отмена")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(macroName) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.toggleDialog() }) {
                Icon(Icons.Default.Add, contentDescription = "Добавить тренировку")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (workouts.isEmpty()) {
                Text(
                    "Список тренировок пуст. Нажмите + чтобы добавить.",
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                LazyColumn {
                    items(workouts) { workout ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { onWorkoutClick(workout.id, workout.name) } // <-- ДОБАВИЛИ КЛИК
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("День ${workout.dayNumber}", style = MaterialTheme.typography.labelMedium)
                                Text(workout.name, style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}
