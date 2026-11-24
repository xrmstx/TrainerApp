package com.example.trainerapp.ui.screens

import android.app.Application
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.trainerapp.data.model.ExerciseBlock
import com.example.trainerapp.data.model.TemplateExercise
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WorkoutDetailViewModel(application: Application, val workoutId: Long) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).appDao()
    private val _blocks = MutableStateFlow<List<ExerciseBlock>>(emptyList())
    val blocks: StateFlow<List<ExerciseBlock>> = _blocks.asStateFlow()
    
    // Храним упражнения для каждого блока (ID блока -> Список упражнений)
    private val _blockExercises = MutableStateFlow<Map<Long, List<TemplateExercise>>>(emptyMap())
    val blockExercises: StateFlow<Map<Long, List<TemplateExercise>>> = _blockExercises.asStateFlow()

    // Состояние для добавления блока
    private val _showBlockDialog = MutableStateFlow(false)
    val showBlockDialog: StateFlow<Boolean> = _showBlockDialog.asStateFlow()
    private val _newBlockTitle = MutableStateFlow("")
    val newBlockTitle: StateFlow<String> = _newBlockTitle.asStateFlow()

    // Состояние для добавления/редактирования упражнения
    private val _showExerciseDialog = MutableStateFlow(false)
    val showExerciseDialog: StateFlow<Boolean> = _showExerciseDialog.asStateFlow()
    private val _currentBlockId = MutableStateFlow<Long?>(null)
    private val _editExercise = MutableStateFlow<TemplateExercise?>(null)
    
    private val _exName = MutableStateFlow("")
    private val _exSets = MutableStateFlow("")
    private val _exReps = MutableStateFlow("")
    private val _exNote = MutableStateFlow("")
    private val _exMetric = MutableStateFlow("REPS")
    private val _exType = MutableStateFlow("LINEAR") // LINEAR или CIRCUIT
    val exName: StateFlow<String> = _exName.asStateFlow()
    val exSets: StateFlow<String> = _exSets.asStateFlow()
    val exReps: StateFlow<String> = _exReps.asStateFlow()
    val exNote: StateFlow<String> = _exNote.asStateFlow()
    val exMetric: StateFlow<String> = _exMetric.asStateFlow()
    val exType: StateFlow<String> = _exType.asStateFlow()

    // Библиотека
    private val _library = MutableStateFlow<List<TemplateExercise>>(emptyList())
    val library: StateFlow<List<TemplateExercise>> = _library.asStateFlow()
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    private val _showLibrary = MutableStateFlow(false)
    val showLibrary: StateFlow<Boolean> = _showLibrary.asStateFlow()

    init { loadBlocks() }

    fun loadBlocks() {
        viewModelScope.launch {
            val list = dao.getBlocksForWorkoutSync(workoutId)
            _blocks.value = list
            val map = mutableMapOf<Long, List<TemplateExercise>>()
            list.forEach { block ->
                dao.getExercisesForBlock(block.id).collect { exList -> map[block.id] = exList; _blockExercises.value = map.toMap() }
            }
        }
    }

    fun openBlockDialog() { _showBlockDialog.value = true; _newBlockTitle.value = "" }
    fun closeBlockDialog() { _showBlockDialog.value = false }
    fun onBlockTitleChange(title: String) { _newBlockTitle.value = title }

    fun addBlock() {
        val title = _newBlockTitle.value.trim()
        if (title.isNotEmpty()) {
            viewModelScope.launch {
                dao.insertExerciseBlock(ExerciseBlock(workoutId = workoutId, blockTitle = title))
                loadBlocks()
                closeBlockDialog()
            }
        }
    }

    fun openExerciseDialog(blockId: Long, ex: TemplateExercise? = null) {
        _currentBlockId.value = blockId
        _editExercise.value = ex
        _showExerciseDialog.value = true
        if (ex != null) {
            _exName.value = ex.exerciseName; _exSets.value = ex.targetSets.toString(); _exReps.value = ex.targetReps; _exNote.value = ex.note ?: ""; _exMetric.value = ex.metricType
            _exType.value = if (ex.targetSets == 0) "CIRCUIT" else "LINEAR"
        } else {
            _exName.value = ""; _exSets.value = ""; _exReps.value = ""; _exNote.value = ""; _exMetric.value = "REPS"; _exType.value = "LINEAR"
        }
    }

    fun closeExerciseDialog() { _showExerciseDialog.value = false }

    fun onNameChange(name: String) { _exName.value = name }
    fun onSetsChange(sets: String) { _exSets.value = sets }
    fun onRepsChange(reps: String) { _exReps.value = reps }
    fun onNoteChange(note: String) { _exNote.value = note }
    fun onMetricChange(metric: String) { _exMetric.value = metric }
    fun onTypeChange(type: String) { _exType.value = type }

    fun loadLibrary() {
        viewModelScope.launch {
            _library.value = dao.getUniqueExercises()
            _showLibrary.value = true
        }
    }
    fun closeLibrary() { _showLibrary.value = false }
    fun onSearchChange(query: String) { _searchQuery.value = query }

    fun applyFromLibrary(ex: TemplateExercise) {
        _exName.value = ex.exerciseName; _exSets.value = ex.targetSets.toString(); _exReps.value = ex.targetReps; _exNote.value = ex.note ?: ""; _exMetric.value = ex.metricType
        _exType.value = if (ex.targetSets == 0) "CIRCUIT" else "LINEAR"
        _showLibrary.value = false
    }

    fun addOrEditExercise() {
        val name = _exName.value.trim()
        val sets = _exSets.value.trim().toIntOrNull() ?: 0
        val reps = _exReps.value.trim()
        val note = _exNote.value.trim()
        val metric = _exMetric.value
        val blockId = _currentBlockId.value
        val existing = _editExercise.value

        if (name.isNotEmpty() && reps.isNotEmpty() && blockId != null) {
            viewModelScope.launch {
                if (existing != null) {
                    dao.insertTemplateExercise(existing.copy(exerciseName = name, targetSets = sets, targetReps = reps, note = note.ifEmpty { null }, metricType = metric))
                } else {
                    dao.insertTemplateExercise(TemplateExercise(blockId = blockId, exerciseName = name, order = 100, targetSets = sets, targetReps = reps, targetWeight = null, note = note.ifEmpty { null }, metricType = metric))
                }
                loadBlocks()
                closeExerciseDialog()
            }
        }
    }

    fun deleteExercise(ex: TemplateExercise) { viewModelScope.launch { dao.deleteTemplateExercise(ex.id); loadBlocks() } }
    fun deleteBlock(blockId: Long) { viewModelScope.launch { /* Нужен метод deleteBlock в DAO, но для простоты пока оставим */ } }
}

class WorkoutDetailViewModelFactory(private val application: Application, private val workoutId: Long) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WorkoutDetailViewModel::class.java)) { @Suppress("UNCHECKED_CAST") return WorkoutDetailViewModel(application, workoutId) as T }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun WorkoutDetailScreen(workoutId: Long, workoutName: String, onBackClick: () -> Unit) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val viewModel: WorkoutDetailViewModel = viewModel(factory = WorkoutDetailViewModelFactory(application, workoutId))
    
    val blocks by viewModel.blocks.collectAsState()
    val blockExercises by viewModel.blockExercises.collectAsState()
    val showBlockDialog by viewModel.showBlockDialog.collectAsState()
    val newBlockTitle by viewModel.newBlockTitle.collectAsState()
    
    val showExerciseDialog by viewModel.showExerciseDialog.collectAsState()
    val exName by viewModel.exName.collectAsState()
    val exSets by viewModel.exSets.collectAsState()
    val exReps by viewModel.exReps.collectAsState()
    val exNote by viewModel.exNote.collectAsState()
    val exMetric by viewModel.exMetric.collectAsState()
    val exType by viewModel.exType.collectAsState()

    val showLibrary by viewModel.showLibrary.collectAsState()
    val library by viewModel.library.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    // Диалог добавления блока
    if (showBlockDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.closeBlockDialog() },
            title = { Text("Новый блок") },
            text = { OutlinedTextField(value = newBlockTitle, onValueChange = { viewModel.onBlockTitleChange(it) }, label = { Text("Название (Разминка, Силовая, Меткон)") }, shape = RoundedCornerShape(12.dp)) },
            confirmButton = { TextButton(onClick = { viewModel.addBlock() }) { Text("Добавить") } },
            dismissButton = { TextButton(onClick = { viewModel.closeBlockDialog() }) { Text("Отмена") } }
        )
    }

    // Диалог библиотеки
    if (showLibrary) {
        AlertDialog(
            onDismissRequest = { viewModel.closeLibrary() },
            title = { Text("Библиотека упражнений") },
            text = {
                Column {
                    OutlinedTextField(value = searchQuery, onValueChange = { viewModel.onSearchChange(it) }, label = { Text("Поиск...") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    val filtered = library.filter { it.exerciseName.contains(searchQuery, ignoreCase = true) }
                    if (filtered.isEmpty()) Text("Ничего не найдено.")
                    else LazyColumn {
                        items(filtered) { libEx ->
                            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).combinedClickable(onClick = { viewModel.applyFromLibrary(libEx) }), shape = RoundedCornerShape(12.dp)) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(libEx.exerciseName, fontWeight = FontWeight.Bold)
                                    Text("${libEx.targetSets} x ${libEx.targetReps}", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { viewModel.closeLibrary() }) { Text("Закрыть") } }
        )
    }

    // Диалог добавления/редактирования упражнения
    if (showExerciseDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.closeExerciseDialog() },
            title = { Text(if (viewModel.exerciseIsEdit()) "Редактировать" else "Добавить упражнение") },
            text = {
                Column {
                    OutlinedTextField(value = exName, onValueChange = { viewModel.onNameChange(it) }, label = { Text("Название") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(onClick = { viewModel.loadLibrary() }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.List, contentDescription = null); Spacer(modifier = Modifier.width(8.dp)); Text("Из библиотеки") }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = exSets, onValueChange = { viewModel.onSetsChange(it) }, label = { Text("Подходы") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
                        OutlinedTextField(value = exReps, onValueChange = { viewModel.onRepsChange(it) }, label = { Text("Повторы/Время") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = exNote, onValueChange = { viewModel.onNoteChange(it) }, label = { Text("Заметка") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = exMetric == "REPS", onClick = { viewModel.onMetricChange("REPS") }, label = { Text("Повторы") })
                        FilterChip(selected = exMetric == "TIME", onClick = { viewModel.onMetricChange("TIME") }, label = { Text("Время") })
                        FilterChip(selected = exMetric == "DISTANCE", onClick = { viewModel.onMetricChange("DISTANCE") }, label = { Text("Дистанция") })
                    }
                }
            },
            confirmButton = { TextButton(onClick = { viewModel.addOrEditExercise() }) { Text("Сохранить") } },
            dismissButton = { TextButton(onClick = { viewModel.closeExerciseDialog() }) { Text("Отмена") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(workoutName) },
                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, contentDescription = "Назад") } }
            )
        },
        floatingActionButton = { FloatingActionButton(onClick = { viewModel.openBlockDialog() }) { Icon(Icons.Default.Add, contentDescription = "Добавить блок") } }
    ) { paddingValues ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
            if (blocks.isEmpty()) {
                item { Text("Нет блоков. Добавьте первый!", modifier = Modifier.fillMaxWidth().padding(32.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center) }
            } else {
                items(blocks) { block ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(2.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(block.blockTitle, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            val exercises = blockExercises[block.id] ?: emptyList()
                            if (exercises.isEmpty()) {
                                Text("Нет упражнений", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            } else {
                                exercises.forEach { ex ->
                                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).combinedClickable(
                                        onClick = { viewModel.openExerciseDialog(block.id, ex) },
                                        onLongClick = { viewModel.deleteExercise(ex) }
                                    ), verticalAlignment = Alignment.CenterVertically) {
                                        Text(ex.exerciseName, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                                        Text(if (ex.targetSets == 0) "Меткон" else "${ex.targetSets} x ${ex.targetReps}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(onClick = { viewModel.openExerciseDialog(block.id) }, modifier = Modifier.fillMaxWidth()) {
                                Icon(Icons.Default.Add, contentDescription = null); Spacer(modifier = Modifier.width(8.dp)); Text("Добавить упражнение")
                            }
                        }
                    }
                }
            }
        }
    }
}

// Заглушка для проверки режима редактирования
fun WorkoutDetailViewModel.exerciseIsEdit(): Boolean = false
