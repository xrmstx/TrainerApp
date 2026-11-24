package com.example.trainerapp.ui.screens

import android.app.Application
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.trainerapp.data.library.ExerciseLibraryRegistry
import com.example.trainerapp.data.library.LibraryEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WorkoutDetailViewModel(application: Application, val workoutId: Long) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).appDao()

    private val _blocks = MutableStateFlow<List<ExerciseBlock>>(emptyList())
    val blocks: StateFlow<List<ExerciseBlock>> = _blocks.asStateFlow()
    private val _blockExercises = MutableStateFlow<Map<Long, List<TemplateExercise>>>(emptyMap())
    val blockExercises: StateFlow<Map<Long, List<TemplateExercise>>> = _blockExercises.asStateFlow()

    private val _showBlockDialog = MutableStateFlow(false)
    val showBlockDialog: StateFlow<Boolean> = _showBlockDialog.asStateFlow()
    private val _newBlockTitle = MutableStateFlow("")
    val newBlockTitle: StateFlow<String> = _newBlockTitle.asStateFlow()

    private val _blockError = MutableStateFlow<String?>(null)
    val blockError: StateFlow<String?> = _blockError.asStateFlow()

    private val _showExerciseDialog = MutableStateFlow(false)
    val showExerciseDialog: StateFlow<Boolean> = _showExerciseDialog.asStateFlow()
    private val _currentBlockId = MutableStateFlow<Long?>(null)
    private val _editExercise = MutableStateFlow<TemplateExercise?>(null)
    val editExercise: StateFlow<TemplateExercise?> = _editExercise.asStateFlow()

    private val _exName = MutableStateFlow("")
    private val _exSets = MutableStateFlow("")
    private val _exReps = MutableStateFlow("")
    private val _exNote = MutableStateFlow("")
    private val _exMetric = MutableStateFlow("REPS")
    private val _exType = MutableStateFlow("LINEAR")
    val exName: StateFlow<String> = _exName.asStateFlow()
    val exSets: StateFlow<String> = _exSets.asStateFlow()
    val exReps: StateFlow<String> = _exReps.asStateFlow()
    val exNote: StateFlow<String> = _exNote.asStateFlow()
    val exMetric: StateFlow<String> = _exMetric.asStateFlow()
    val exType: StateFlow<String> = _exType.asStateFlow()

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
            list.forEach { block -> map[block.id] = dao.getExercisesForBlockSync(block.id) }
            _blockExercises.value = map
        }
    }

    fun openBlockDialog() { _showBlockDialog.value = true; _newBlockTitle.value = "" }
    fun closeBlockDialog() { _showBlockDialog.value = false }
    fun onBlockTitleChange(title: String) { _newBlockTitle.value = title }

    fun clearBlockError() { _blockError.value = null }

    fun addBlock() {
        val title = _newBlockTitle.value.trim()
        if (title.isEmpty()) return
        viewModelScope.launch {
            try {
                val order = (dao.getMaxBlockOrder(workoutId) ?: 0) + 1
                dao.insertExerciseBlock(ExerciseBlock(workoutId = workoutId, blockTitle = title, order = order))
                loadBlocks()
                closeBlockDialog()
            } catch (e: Exception) {
                _blockError.value = e.message ?: e.toString()
            }
        }
    }

    fun deleteBlock(blockId: Long) {
        viewModelScope.launch {
            try {
                dao.deleteExerciseBlock(blockId) // каскад удалит и упражнения блока
                loadBlocks()
            } catch (e: Exception) {
                _blockError.value = e.message ?: e.toString()
            }
        }
    }

    fun openExerciseDialog(blockId: Long, ex: TemplateExercise? = null) {
        _currentBlockId.value = blockId
        _editExercise.value = ex
        _showExerciseDialog.value = true
        if (ex != null) {
            _exName.value = ex.exerciseName
            _exSets.value = if (ex.targetSets == 0) "" else ex.targetSets.toString()
            _exReps.value = ex.targetReps
            _exNote.value = ex.note ?: ""
            _exMetric.value = ex.metricType
            _exType.value = if (ex.targetSets == 0) "CIRCUIT" else "LINEAR"
        } else {
            _exName.value = ""; _exSets.value = ""; _exReps.value = ""; _exNote.value = ""
            _exMetric.value = "REPS"; _exType.value = "LINEAR"
        }
    }

    fun closeExerciseDialog() {
        _showExerciseDialog.value = false
        _editExercise.value = null
        _currentBlockId.value = null
    }

    fun onNameChange(name: String) { _exName.value = name }
    fun onSetsChange(sets: String) { _exSets.value = sets }
    fun onRepsChange(reps: String) { _exReps.value = reps }
    fun onNoteChange(note: String) { _exNote.value = note }
    fun onMetricChange(metric: String) { _exMetric.value = metric }
    fun onTypeChange(type: String) { _exType.value = type }

    fun loadLibrary() { _showLibrary.value = true }
    fun closeLibrary() { _showLibrary.value = false }
    fun onSearchChange(query: String) { _searchQuery.value = query }

    fun applyFromLibrary(entry: LibraryEntry) {
        _exName.value = entry.exerciseName
        _showLibrary.value = false
    }

    fun addOrEditExercise() {
        val name = _exName.value.trim()
        val reps = _exReps.value.trim()
        val note = _exNote.value.trim()
        val metric = _exMetric.value
        val blockId = _currentBlockId.value ?: return
        val existing = _editExercise.value
        val isCircuit = _exType.value == "CIRCUIT"
        val sets = if (isCircuit) 0 else (_exSets.value.trim().toIntOrNull() ?: 0)

        if (name.isEmpty() || reps.isEmpty()) return
        if (!isCircuit && sets <= 0) return

        viewModelScope.launch {
            try {
                if (existing != null) {
                    dao.updateTemplateExercise(
                        existing.copy(
                            exerciseName = name,
                            targetSets = sets,
                            targetReps = reps,
                            note = note.ifEmpty { null },
                            metricType = metric
                        )
                    )
                } else {
                    val order = (dao.getMaxExerciseOrder(blockId) ?: 0) + 1
                    dao.insertTemplateExercise(
                        TemplateExercise(
                            blockId = blockId,
                            exerciseName = name,
                            order = order,
                            targetSets = sets,
                            targetReps = reps,
                            targetWeight = null,
                            note = note.ifEmpty { null },
                            metricType = metric
                        )
                    )
                }
                loadBlocks()
                closeExerciseDialog()
            } catch (e: Exception) {
                _blockError.value = e.message ?: e.toString()
            }
        }
    }

    fun deleteExercise(ex: TemplateExercise) {
        viewModelScope.launch {
            try {
                dao.deleteTemplateExercise(ex.id)
                loadBlocks()
            } catch (e: Exception) {
                _blockError.value = e.message ?: e.toString()
            }
        }
    }
}

class WorkoutDetailViewModelFactory(private val application: Application, private val workoutId: Long) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WorkoutDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST") return WorkoutDetailViewModel(application, workoutId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

private val metricLabels = listOf(
    "REPS" to "Повт.", "WEIGHT" to "Вес", "TIME" to "Время", "DISTANCE" to "Дист.", "CALORIES" to "Ккал"
)
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
    val isEditing by viewModel.editExercise.collectAsState()
    val exName by viewModel.exName.collectAsState()
    val exSets by viewModel.exSets.collectAsState()
    val exReps by viewModel.exReps.collectAsState()
    val exNote by viewModel.exNote.collectAsState()
    val exMetric by viewModel.exMetric.collectAsState()
    val exType by viewModel.exType.collectAsState()
    val showLibrary by viewModel.showLibrary.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val blockError by viewModel.blockError.collectAsState()

    var blockToDelete by remember { mutableStateOf<ExerciseBlock?>(null) }

    blockError?.let { message ->
        AlertDialog(
            onDismissRequest = { viewModel.clearBlockError() },
            title = { Text("Ошибка") },
            text = { Text(message) },
            confirmButton = { TextButton(onClick = { viewModel.clearBlockError() }) { Text("Понятно") } }
        )
    }

    if (blockToDelete != null) {
        AlertDialog(
            onDismissRequest = { blockToDelete = null },
            title = { Text("Удалить блок?") },
            text = { Text("Блок «${blockToDelete!!.blockTitle}» и все упражнения в нём будут удалены безвозвратно.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteBlock(blockToDelete!!.id)
                    blockToDelete = null
                }) { Text("Удалить", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { blockToDelete = null }) { Text("Отмена") } }
        )
    }

    if (showBlockDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.closeBlockDialog() },
            title = { Text("Новый блок") },
            text = {
                OutlinedTextField(
                    value = newBlockTitle,
                    onValueChange = { viewModel.onBlockTitleChange(it) },
                    label = { Text("Название (Разминка, Силовая, Меткон)") },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            },
            confirmButton = { TextButton(onClick = { viewModel.addBlock() }) { Text("Добавить") } },
            dismissButton = { TextButton(onClick = { viewModel.closeBlockDialog() }) { Text("Отмена") } }
        )
    }

    if (showExerciseDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.closeExerciseDialog() },
            title = { Text(if (isEditing != null) "Редактировать упражнение" else "Новое упражнение") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    OutlinedTextField(
                        value = exName,
                        onValueChange = { viewModel.onNameChange(it) },
                        label = { Text("Упражнение") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = exType == "LINEAR",
                            onClick = { viewModel.onTypeChange("LINEAR") },
                            label = { Text("Подходы") }
                        )
                        FilterChip(
                            selected = exType == "CIRCUIT",
                            onClick = { viewModel.onTypeChange("CIRCUIT") },
                            label = { Text("Circuit") }
                        )
                    }
                    if (exType == "LINEAR") {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = exSets,
                                onValueChange = { viewModel.onSetsChange(it) },
                                label = { Text("Подходов") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = exReps,
                                onValueChange = { viewModel.onRepsChange(it) },
                                label = { Text(if (exMetric == "TIME") "Время" else if (exMetric == "DISTANCE") "Метры" else if (exMetric == "CALORIES") "Ккал" else "Повторы") },
                                modifier = Modifier.weight(1.4f),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = exReps,
                            onValueChange = { viewModel.onRepsChange(it) },
                            label = { Text("Формат (например, AMRAP 10 мин)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Метрика", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        metricLabels.forEach { (value, label) ->
                            FilterChip(
                                selected = exMetric == value,
                                onClick = { viewModel.onMetricChange(value) },
                                label = { Text(label) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = exNote,
                        onValueChange = { viewModel.onNoteChange(it) },
                        label = { Text("Комментарий") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = { viewModel.loadLibrary() }) {
                        Icon(Icons.Default.Search, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Выбрать из библиотеки")
                    }
                }
            },
            confirmButton = { TextButton(onClick = { viewModel.addOrEditExercise() }) { Text("Сохранить") } },
            dismissButton = { TextButton(onClick = { viewModel.closeExerciseDialog() }) { Text("Отмена") } }
        )
    }

    if (showLibrary) {
        AlertDialog(
            onDismissRequest = { viewModel.closeLibrary() },
            title = { Text("Библиотека упражнений") },
            text = {
                Column {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.onSearchChange(it) },
                        label = { Text("Поиск...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val results = ExerciseLibraryRegistry.search(searchQuery)
                    if (results.isEmpty()) Text("Ничего не найдено.")
                    else LazyColumn(modifier = Modifier.heightIn(max = 360.dp)) {
                        items(results) { entry ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                    .combinedClickable(onClick = { viewModel.applyFromLibrary(entry) }),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(entry.exerciseName, style = MaterialTheme.typography.bodyLarge)
                                    Text(
                                        "${entry.libraryTitle} · ${entry.categoryTitle}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { viewModel.closeLibrary() }) { Text("Закрыть") } }
        )
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(workoutName, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, contentDescription = "Назад") }
                },
                actions = {
                    IconButton(onClick = { viewModel.openBlockDialog() }) { Icon(Icons.Default.Add, contentDescription = "Добавить блок") }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
            if (blocks.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Добавьте первый блок через «+» сверху.", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                }
            } else {
                LazyColumn {
                    items(blocks, key = { it.id }) { block ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(block.blockTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                    IconButton(onClick = { blockToDelete = block }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Удалить блок", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                                val exercises = blockExercises[block.id].orEmpty()
                                if (exercises.isEmpty()) {
                                    Text("Пусто", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                } else {
                                    exercises.forEach { ex ->
                                        Card(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).combinedClickable(
                                                onClick = { viewModel.openExerciseDialog(block.id, ex) },
                                                onLongClick = { viewModel.deleteExercise(ex) }
                                            ),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                        ) {
                                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(ex.exerciseName, style = MaterialTheme.typography.bodyLarge)
                                                    if (!ex.note.isNullOrBlank()) {
                                                        Text(ex.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                                    }
                                                }
                                                val plan = if (ex.targetSets == 0) ex.targetReps else "${ex.targetSets}×${ex.targetReps}"
                                                Text(plan, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedButton(
                                    onClick = { viewModel.openExerciseDialog(block.id) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Упражнение")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
