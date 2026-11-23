package com.example.trainerapp.ui.screens

import android.app.Application
import android.content.Intent
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trainerapp.data.local.AppDatabase
import com.example.trainerapp.data.model.ExerciseBlock
import com.example.trainerapp.data.model.TemplateExercise
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class WorkoutDetailViewModel(application: Application, val workoutId: Long) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).appDao()
    private val _exercises = MutableStateFlow<List<TemplateExercise>>(emptyList())
    val exercises: StateFlow<List<TemplateExercise>> = _exercises.asStateFlow()
    private val _blockId = MutableStateFlow<Long?>(null)
    val blockId: StateFlow<Long?> = _blockId.asStateFlow()

    // ИСПРАВЛЕНИЕ БАГА: Сначала ищем существующий блок, если нет - создаем
    init {
        viewModelScope.launch {
            val existingBlock = dao.getBlockForWorkout(workoutId)
            val id = if (existingBlock != null) {
                existingBlock.id
            } else {
                dao.insertExerciseBlock(ExerciseBlock(workoutId = workoutId))
            }
            _blockId.value = id
            dao.getExercisesForBlock(id).collectLatest { list -> _exercises.value = list }
        }
    }

    private val _showDialog = MutableStateFlow(false)
    val showDialog: StateFlow<Boolean> = _showDialog.asStateFlow()
    private val _exName = MutableStateFlow("")
    private val _exSets = MutableStateFlow("")
    private val _exReps = MutableStateFlow("")
    val exName: StateFlow<String> = _exName.asStateFlow()
    val exSets: StateFlow<String> = _exSets.asStateFlow()
    val exReps: StateFlow<String> = _exReps.asStateFlow()

    fun onNameChange(name: String) { _exName.value = name }
    fun onSetsChange(sets: String) { _exSets.value = sets }
    fun onRepsChange(reps: String) { _exReps.value = reps }
    fun toggleDialog() { _showDialog.value = !_showDialog.value; _exName.value = ""; _exSets.value = ""; _exReps.value = "" }

    fun addExercise() {
        val name = _exName.value.trim()
        val sets = _exSets.value.trim().toIntOrNull() ?: 0
        val reps = _exReps.value.trim()
        val block = _blockId.value
        if (name.isNotEmpty() && sets > 0 && reps.isNotEmpty() && block != null) {
            viewModelScope.launch {
                dao.insertTemplateExercise(TemplateExercise(blockId = block, exerciseName = name, order = _exercises.value.size + 1, targetSets = sets, targetReps = reps, targetWeight = null))
                toggleDialog()
            }
        }
    }
}

class WorkoutDetailViewModelFactory(private val application: Application, private val workoutId: Long) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WorkoutDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WorkoutDetailViewModel(application, workoutId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutDetailScreen(workoutId: Long, workoutName: String, onBackClick: () -> Unit) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val viewModel: WorkoutDetailViewModel = viewModel(factory = WorkoutDetailViewModelFactory(application, workoutId))

    val exercises by viewModel.exercises.collectAsState()
    val showDialog by viewModel.showDialog.collectAsState()
    val exName by viewModel.exName.collectAsState()
    val exSets by viewModel.exSets.collectAsState()
    val exReps by viewModel.exReps.collectAsState()

    // Функция для формирования текста и шаринга
    fun shareWorkout() {
        val text = buildString {
            appendLine("🏋️ Тренировка: $workoutName")
            appendLine("")
            exercises.forEachIndexed { index, ex ->
                appendLine("${index + 1}. ${ex.exerciseName} — ${ex.targetSets}x${ex.targetReps}")
            }
            appendLine("")
            appendLine("💪 Хорошей тренировки!")
        }
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Поделиться планом")
        context.startActivity(shareIntent)
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.toggleDialog() },
            title = { Text("Новое упражнение") },
            text = {
                Column {
                    OutlinedTextField(value = exName, onValueChange = { viewModel.onNameChange(it) }, label = { Text("Название") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Row {
                        OutlinedTextField(value = exSets, onValueChange = { viewModel.onSetsChange(it) }, label = { Text("Подходы") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(value = exReps, onValueChange = { viewModel.onRepsChange(it) }, label = { Text("Повторы") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
                    }
                }
            },
            confirmButton = { TextButton(onClick = { viewModel.addExercise() }) { Text("Добавить") } },
            dismissButton = { TextButton(onClick = { viewModel.toggleDialog() }) { Text("Отмена") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(workoutName) },
                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, contentDescription = "Назад") } },
                actions = {
                    IconButton(onClick = { shareWorkout() }) {
                        Icon(Icons.Default.Share, contentDescription = "Поделиться")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.toggleDialog() }, containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary) { 
                Icon(Icons.Default.Add, contentDescription = "Добавить") 
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
            if (exercises.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Нет упражнений. Нажмите + чтобы добавить.", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                }
            } else {
                LazyColumn {
                    items(exercises) { ex ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(ex.exerciseName, style = MaterialTheme.typography.titleMedium)
                                Text("План: ${ex.targetSets} x ${ex.targetReps}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}
