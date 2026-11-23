package com.example.trainerapp.ui.screens

import android.app.Application
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trainerapp.data.local.AppDatabase
import com.example.trainerapp.data.model.ClientProgram
import com.example.trainerapp.data.model.Macrocycle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ClientDetailViewModel(application: Application, val clientId: Long) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).appDao()
    val programs = dao.getProgramsForClient(clientId)
    val templates = dao.getAllMacrocycles()

    private val _showDialog = MutableStateFlow(false)
    val showDialog: StateFlow<Boolean> = _showDialog.asStateFlow()

    fun toggleDialog() { _showDialog.value = !_showDialog.value }

    fun assignProgram(macrocycle: Macrocycle) {
        viewModelScope.launch {
            // Глубокое копирование
            val programId = dao.insertClientProgram(ClientProgram(clientId = clientId, macrocycleId = macrocycle.id, weekNumber = 1))
            
            val originalWorkouts = mutableListOf<com.example.trainerapp.data.model.Workout>()
            dao.getWorkoutsForMacro(macrocycle.id).collect { list ->
                originalWorkouts.clear()
                originalWorkouts.addAll(list)
                return@collect
            }

            originalWorkouts.forEach { origWorkout ->
                val newWorkoutId = dao.insertClientWorkout(
                    com.example.trainerapp.data.model.ClientWorkout(
                        clientProgramId = programId,
                        originalWorkoutId = origWorkout.id,
                        dayNumber = origWorkout.dayNumber,
                        name = origWorkout.name
                    )
                )
                
                val exercises = mutableListOf<com.example.trainerapp.data.model.TemplateExercise>()
                dao.getExercisesForBlock(origWorkout.id).collect { list ->
                    exercises.clear()
                    exercises.addAll(list)
                    return@collect
                }

                exercises.forEach { ex ->
                    dao.insertClientExercise(
                        com.example.trainerapp.data.model.ClientExercise(
                            clientWorkoutId = newWorkoutId,
                            exerciseName = ex.exerciseName,
                            order = ex.order,
                            targetSets = ex.targetSets,
                            targetReps = ex.targetReps
                        )
                    )
                }
            }
            toggleDialog()
        }
    }
}

class ClientDetailViewModelFactory(private val application: Application, private val clientId: Long) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ClientDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ClientDetailViewModel(application, clientId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientDetailScreen(clientId: Long, clientName: String, onBackClick: () -> Unit, onProgramClick: (Long) -> Unit) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val viewModel: ClientDetailViewModel = viewModel(factory = ClientDetailViewModelFactory(application, clientId))

    val programs by viewModel.programs.collectAsState(initial = emptyList())
    val templates by viewModel.templates.collectAsState(initial = emptyList())
    val showDialog by viewModel.showDialog.collectAsState()

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.toggleDialog() },
            title = { Text("Выберите шаблон") },
            text = {
                if (templates.isEmpty()) Text("У вас нет созданных шаблонов.")
                else LazyColumn {
                    items(templates) { macro ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { viewModel.assignProgram(macro) },
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Text(macro.name, modifier = Modifier.padding(16.dp))
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { viewModel.toggleDialog() }) { Text("Отмена") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(clientName) },
                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, contentDescription = "Назад") } }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
            Button(onClick = { viewModel.toggleDialog() }, modifier = Modifier.fillMaxWidth()) {
                Text("Назначить программу")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Активные программы:", style = MaterialTheme.typography.titleMedium)
            
            if (programs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Нет назначенных программ", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn {
                    items(programs) { prog ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onProgramClick(prog.id) },
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Программа (ID: ${prog.macrocycleId})", style = MaterialTheme.typography.titleMedium)
                                Text("Неделя: ${prog.weekNumber}", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}
