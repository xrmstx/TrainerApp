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
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class WorkoutDetailViewModel(application: Application, val workoutId: Long) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).appDao()
    
    // Все блоки и их упражнения
    val blocks = dao.getBlocksForWorkout(workoutId)
    
    private val _selectedSection = MutableStateFlow("MAIN") // WARMUP, MAIN, COOLDOWN
    val selectedSection: StateFlow<String> = _selectedSection.asStateFlow()
    
    // Состояние для диалога добавления
    private val _showDialog = MutableStateFlow(false)
    val showDialog: StateFlow<Boolean> = _showDialog.asStateFlow()
    private val _exName = MutableStateFlow("")
    private val _exSets = MutableStateFlow("")
    private val _exReps = MutableStateFlow("")
    private val _exNote = MutableStateFlow("")
    private val _exType = MutableStateFlow("SINGLE") // SINGLE, CIRCUIT
    
    val exName: StateFlow<String> = _exName.asStateFlow()
    val exSets: StateFlow<String> = _exSets.asStateFlow()
    val exReps: StateFlow<String> = _exReps.asStateFlow()
    val exNote: StateFlow<String> = _exNote.asStateFlow()
    val exType: StateFlow<String> = _exType.asStateFlow()

    fun onSectionChange(section: String) { _selectedSection.value = section }
    fun onNameChange(name: String) { _exName.value = name }
    fun onSetsChange(sets: String) { _exSets.value = sets }
    fun onRepsChange(reps: String) { _exReps.value = reps }
    fun onNoteChange(note: String) { _exNote.value = note }
    fun onTypeChange(type: String) { _exType.value = type }
    
    fun toggleDialog() { _showDialog.value = !_showDialog.value; _exName.value = ""; _exSets.value = ""; _exReps.value = ""; _exNote.value = ""; _exType.value = "SINGLE" }

    fun addExercise() {
        val name = _exName.value.trim()
        val sets = _exSets.value.trim().toIntOrNull() ?: 0
        val reps = _exReps.value.trim()
        val note = _exNote.value.trim()
        val section = _selectedSection.value
        val type = _exType.value

        if (name.isNotEmpty() && sets > 0 && reps.isNotEmpty()) {
            viewModelScope.launch {
                // Ищем существующий блок для этой секции или создаем новый
                var blockId: Long? = null
                blocks.collect { blockList ->
                    val existingBlock = blockList.find { it.section == section }
                    if (existingBlock != null) {
                        blockId = existingBlock.id
                    } else {
                        blockId = dao.insertExerciseBlock(ExerciseBlock(workoutId = workoutId, section = section, blockType = type))
                    }
                    return@collect
                }

                if (blockId != null) {
                    dao.insertTemplateExercise(TemplateExercise(blockId = blockId!!, exerciseName = name, order = 100, targetSets = sets, targetReps = reps, targetWeight = null, note = note.ifEmpty { null }))
                }
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

    val blocks by viewModel.blocks.collectAsState(initial = emptyList())
    val selectedSection by viewModel.selectedSection.collectAsState()
    val showDialog by viewModel.showDialog.collectAsState()
    
    val exName by viewModel.exName.collectAsState()
    val exSets by viewModel.exSets.collectAsState()
    val exReps by viewModel.exReps.collectAsState()
    val exNote by viewModel.exNote.collectAsState()
    val exType by viewModel.exType.collectAsState()

    val currentBlock = blocks.find { it.section == selectedSection }
    val currentExercises = if (currentBlock != null) {
        // В реальном приложении нужно загружать список упражнений по ID блока
        // Для простоты в этом MVP мы просто покажем заглушку, но в следующем коммите добавим загрузку
        emptyList<TemplateExercise>() // <-- Временно
    } else emptyList()

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.toggleDialog() },
            title = { Text("Добавить в ${selectedSection}") },
            text = {
                Column {
                    OutlinedTextField(value = exName, onValueChange = { viewModel.onNameChange(it) }, label = { Text("Название") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Row {
                        OutlinedTextField(value = exSets, onValueChange = { viewModel.onSetsChange(it) }, label = { Text("Подходы") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(value = exReps, onValueChange = { viewModel.onRepsChange(it) }, label = { Text("Повторы") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = exNote, onValueChange = { viewModel.onNoteChange(it) }, label = { Text("Заметка") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Выбор типа (Обычный или Круг)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = exType == "SINGLE", onClick = { viewModel.onTypeChange("SINGLE") })
                        Text("Обычное")
                        Spacer(modifier = Modifier.width(16.dp))
                        RadioButton(selected = exType == "CIRCUIT", onClick = { viewModel.onTypeChange("CIRCUIT") })
                        Text("Круг/Меткон")
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
                    // Шаринг (упрощенно)
                    IconButton(onClick = { /* Логика шаринга */ }) { 
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
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // Вкладки для секций
            TabRow(selectedTabIndex = when(selectedSection) { "WARMUP" -> 0; "MAIN" -> 1; else -> 2 }) {
                Tab(selected = selectedSection == "WARMUP", onClick = { viewModel.onSectionChange("WARMUP") }, text = { Text("Разминка") })
                Tab(selected = selectedSection == "MAIN", onClick = { viewModel.onSectionChange("MAIN") }, text = { Text("Основа") })
                Tab(selected = selectedSection == "COOLDOWN", onClick = { viewModel.onSectionChange("COOLDOWN") }, text = { Text("Заминка") })
            }
            
            // Список упражнений для выбранной секции
            if (currentExercises.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Нет упражнений в этой секции. Нажмите + чтобы добавить.", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                }
            } else {
                // В этой версии мы просто выводим список, так как пока нет полноценной загрузки
                Text("Список упражнений будет здесь", modifier = Modifier.padding(16.dp))
            }
        }
    }
}
