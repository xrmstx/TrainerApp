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
    fun openDialog() { _showDialog.value = true; _workoutName.value = "" }
    fun closeDialog() { _showDialog.value = false; _workoutName.value = "" }

    fun addWorkout() {
        val name = _workoutName.value.trim()
        if (name.isEmpty()) return
        viewModelScope.launch {
            val maxDay = dao.getMaxDayNumber(macroId) ?: 0
            dao.insertWorkout(Workout(mesocycleId = macroId, dayNumber = maxDay + 1, name = name))
            closeDialog()
        }
    }

    fun deleteWorkout(id: Long) {
        viewModelScope.launch { dao.deleteWorkout(id) } // каскад удалит блоки и упражнения
    }

    fun shareProgram(macroName: String, onReady: (String) -> Unit) {
        viewModelScope.launch {
            val text = buildString {
                appendLine("🏋️ Программа: $macroName")
                appendLine()
                dao.getWorkoutsForMacroSync(macroId).forEach { workout ->
                    appendLine("📅 День ${workout.dayNumber}: ${workout.name}")
                    dao.getBlocksForWorkoutSync(workout.id).forEach { block ->
                        appendLine("  ${block.blockTitle}:")
                        dao.getExercisesForBlockSync(block.id).forEach { ex ->
                            val plan = if (ex.targetSets == 0) ex.targetReps else "${ex.targetSets}×${ex.targetReps}"
                            appendLine("    • ${ex.exerciseName} — $plan")
                        }
                    }
                    appendLine()
                }
                appendLine("💪 Успехов в тренировках!")
            }
            onReady(text)
        }
    }
}

class MacroDetailViewModelFactory(private val application: Application, private val macroId: Long) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MacroDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST") return MacroDetailViewModel(application, macroId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MacroDetailScreen(macroId: Long, macroName: String, onBackClick: () -> Unit, onWorkoutClick: (Long, String) -> Unit) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val viewModel: MacroDetailViewModel = viewModel(factory = MacroDetailViewModelFactory(application, macroId))
    val workouts by viewModel.workouts.collectAsState(initial = emptyList())
    val showDialog by viewModel.showDialog.collectAsState()
    val workoutName by viewModel.workoutName.collectAsState()

    var workoutToDelete by remember { mutableStateOf<Workout?>(null) }

    if (workoutToDelete != null) {
        AlertDialog(
            onDismissRequest = { workoutToDelete = null },
            title = { Text("Удалить тренировку?") },
            text = { Text("«${workoutToDelete!!.name}» и все её блоки и упражнения будут удалены безвозвратно.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteWorkout(workoutToDelete!!.id)
                    workoutToDelete = null
                }) { Text("Удалить", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { workoutToDelete = null }) { Text("Отмена") } }
        )
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.closeDialog() },
            title = { Text("Новая тренировка") },
            text = {
                OutlinedTextField(
                    value = workoutName,
                    onValueChange = { viewModel.onNameChange(it) },
                    label = { Text("Название") },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            },
            confirmButton = { TextButton(onClick = { viewModel.addWorkout() }) { Text("Добавить") } },
            dismissButton = { TextButton(onClick = { viewModel.closeDialog() }) { Text("Отмена") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(macroName, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, contentDescription = "Назад") } },
                actions = {
                    IconButton(onClick = { viewModel.shareProgram(macroName) { text ->
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, text)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Поделиться программой"))
                    } }) { Icon(Icons.Default.Share, contentDescription = "Поделиться") }
                }
            )
        },
        floatingActionButton = { FloatingActionButton(onClick = { viewModel.openDialog() }) { Icon(Icons.Default.Add, contentDescription = null) } }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
            if (workouts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Список тренировок пуст.", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                }
            } else {
                LazyColumn {
                    items(workouts, key = { it.id }) { workout ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).combinedClickable(
                                onClick = { onWorkoutClick(workout.id, workout.name) },
                                onLongClick = { workoutToDelete = workout }
                            ),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("День ${workout.dayNumber}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                Text(workout.name, style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}
