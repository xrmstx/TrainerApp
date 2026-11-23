package com.example.trainerapp.ui.screens

import android.app.Application
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trainerapp.data.local.AppDatabase
import com.example.trainerapp.data.model.*
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

    fun deleteProgram(programId: Long) {
        viewModelScope.launch { dao.deleteClientProgram(programId) }
    }

    // ИСПРАВЛЕННЫЙ МЕТОД КОПИРОВАНИЯ
    fun assignProgram(macrocycle: Macrocycle) {
        viewModelScope.launch {
            // 1. Создаем программу
            val programId = dao.insertClientProgram(
                ClientProgram(clientId = clientId, macrocycleId = macrocycle.id, weekNumber = 1)
            )
            
            // 2. Получаем тренировки шаблона (синхронно)
            val originalWorkouts = dao.getWorkoutsForMacroSync(macrocycle.id)
            
            // 3. Копируем
            originalWorkouts.forEach { origWorkout ->
                val newWorkoutId = dao.insertClientWorkout(
                    ClientWorkout(
                        clientProgramId = programId,
                        originalWorkoutId = origWorkout.id,
                        dayNumber = origWorkout.dayNumber,
                        name = origWorkout.name
                    )
                )
                
                // Копируем упражнения
                val block = dao.getBlockForWorkout(origWorkout.id)
                if (block != null) {
                    val exercises = dao.getExercisesForBlockSync(block.id)
                    exercises.forEach { ex ->
                        dao.insertClientExercise(
                            ClientExercise(
                                clientWorkoutId = newWorkoutId,
                                exerciseName = ex.exerciseName,
                                order = ex.order,
                                targetSets = ex.targetSets,
                                targetReps = ex.targetReps,
                                note = ex.note
                            )
                        )
                    }
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
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
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).combinedClickable(
                                onClick = { viewModel.assignProgram(macro) }
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(macro.name, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium)
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
            Button(
                onClick = { viewModel.toggleDialog() }, 
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Назначить программу")
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text("Активные программы:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            
            if (programs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Нет назначенных программ", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                }
            } else {
                LazyColumn {
                    items(programs) { prog ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).combinedClickable(
                                onClick = { onProgramClick(prog.id) },
                                onLongClick = { viewModel.deleteProgram(prog.id) } // <-- Удаление долгим нажатием
                            ),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Программа (ID: ${prog.macrocycleId})", style = MaterialTheme.typography.titleMedium)
                                    Text("Неделя: ${prog.weekNumber}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                }
                                Icon(Icons.Default.Delete, contentDescription = "Удалить", tint = Color.Red.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            }
        }
    }
}
