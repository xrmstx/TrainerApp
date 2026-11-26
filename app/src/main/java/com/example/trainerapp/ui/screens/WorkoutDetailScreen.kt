package com.example.trainerapp.ui.screens

import android.app.Application
import androidx.compose.ui.graphics.Color
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
import com.example.trainerapp.ui.components.EmptyState
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
    private val _blockType = MutableStateFlow("LINEAR")
    val blockType: StateFlow<String> = _blockType.asStateFlow()
    private val _protocol = MutableStateFlow("AMRAP")
    val protocol: StateFlow<String> = _protocol.asStateFlow()
    private val _protocolValue = MutableStateFlow("")
    val protocolValue: StateFlow<String> = _protocolValue.asStateFlow()

    private val _blockError = MutableStateFlow<String?>(null)
    val blockError: StateFlow<String?> = _blockError.asStateFlow()

    private val _showExerciseDialog = MutableStateFlow(false)
    val showExerciseDialog: StateFlow<Boolean> = _showExerciseDialog.asStateFlow()
    private val _currentBlockId = MutableStateFlow<Long?>(null)
    private val _currentBlockIsCircuit = MutableStateFlow(false)
    val currentBlockIsCircuit: StateFlow<Boolean> = _currentBlockIsCircuit.asStateFlow()
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

    fun openBlockDialog() { _showBlockDialog.value = true; _newBlockTitle.value = ""; _blockType.value = "LINEAR"; _protocol.value = "AMRAP"; _protocolValue.value = "" }
    fun closeBlockDialog() { _showBlockDialog.value = false }
    fun onBlockTitleChange(title: String) { _newBlockTitle.value = title }
    fun onBlockTypeChange(type: String) {
        _blockType.value = type
        if (type != "CIRCUIT") { _protocol.value = "AMRAP"; _protocolValue.value = "" }
    }
    fun onProtocolChange(protocol: String) { _protocol.value = protocol; _protocolValue.value = "" }
    fun onProtocolValueChange(value: String) { _protocolValue.value = value }

    fun clearBlockError() { _blockError.value = null }

    fun addBlock() {
        val title = _newBlockTitle.value.trim()
        if (title.isEmpty()) return
        viewModelScope.launch {
            try {
                val order = (dao.getMaxBlockOrder(workoutId) ?: 0) + 1
                val type = _blockType.value
                // Протокол имеет смысл только у Circuit-блока
                val protocol = if (type == "CIRCUIT") _protocol.value else "NONE"
                val protocolValue = if (type == "CIRCUIT") _protocolValue.value.trim() else ""
                dao.insertExerciseBlock(
                    ExerciseBlock(
                        workoutId = workoutId,
                        blockTitle = title,
                        blockType = type,
                        order = order,
                        protocol = protocol,
                        protocolValue = protocolValue
                    )
                )
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

    fun openExerciseDialog(block: ExerciseBlock, ex: TemplateExercise? = null) {
        _currentBlockId.value = block.id
        _currentBlockIsCircuit.value = block.blockType == "CIRCUIT"
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
            // Тип упражнения наследуется от блока: в Circuit-блоке — Circuit, иначе Linear
            _exName.value = ""; _exSets.value = ""; _exReps.value = ""; _exNote.value = ""
            _exMetric.value = "REPS"
            _exType.value = if (block.blockType == "CIRCUIT") "CIRCUIT" else "LINEAR"
        }
    }

    fun closeExerciseDialog() {
        _showExerciseDialog.value = false
        _editExercise.value = null
        _currentBlockId.value = null
        _currentBlockIsCircuit.value = false
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

        if (name.isEmpty()) return
        // В Circuit у упражнения может не быть повторений — метрика (метры, ккал, время) важнее
        if (!isCircuit && (reps.isEmpty() || sets <= 0)) return

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

private val protocolLabels = listOf(
    "AMRAP" to "AMRAP", "EMOM" to "EMOM", "FOR_TIME" to "For Time",
    "RFT" to "RFT", "TABATA" to "Tabata", "CUSTOM" to "Свой"
)

private val protocolHints = mapOf(
    "AMRAP" to "Время, мин (например, 10)",
    "EMOM" to "Длительность, мин (например, 12)",
    "FOR_TIME" to "Схема (например, 21-15-9)",
    "RFT" to "Раундов на время (например, 3)",
    "TABATA" to "Схема (например, 20/10×8)",
    "CUSTOM" to "Схема свободно"
)

private fun protocolBadge(block: ExerciseBlock): String? {
    if (block.blockType != "CIRCUIT" || block.protocol == "NONE") return null
    return when (block.protocol) {
        "AMRAP" -> "AMRAP ${block.protocolValue} мин"
        "EMOM" -> "EMOM ${block.protocolValue} мин"
        "FOR_TIME" -> "For Time" + (if (block.protocolValue.isNotBlank()) " · ${block.protocolValue}" else "")
        "RFT" -> "RFT ${block.protocolValue}"
        "TABATA" -> "Tabata ${block.protocolValue}"
        "CUSTOM" -> if (block.protocolValue.isNotBlank()) block.protocolValue else "Своя схема"
        else -> null
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
    val blockType by viewModel.blockType.collectAsState()
    val protocol by viewModel.protocol.collectAsState()
    val protocolValue by viewModel.protocolValue.collectAsState()
    val showExerciseDialog by viewModel.showExerciseDialog.collectAsState()
    val isEditing by viewModel.editExercise.collectAsState()
    val blockIsCircuit by viewModel.currentBlockIsCircuit.collectAsState()
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
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    OutlinedTextField(
                        value = newBlockTitle,
                        onValueChange = { viewModel.onBlockTitleChange(it) },
                        label = { Text("Название (Разминка, Силовая, Меткон)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = blockType == "LINEAR",
                            onClick = { viewModel.onBlockTypeChange("LINEAR") },
                            label = { Text("Подходы") }
                        )
                        FilterChip(
                            selected = blockType == "CIRCUIT",
                            onClick = { viewModel.onBlockTypeChange("CIRCUIT") },
                            label = { Text("Меткон") }
                        )
                    }
                    if (blockType == "CIRCUIT") {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Протокол", style = MaterialTheme.typography.labelMedium)
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            protocolLabels.forEach { (value, label) ->
                                FilterChip(
                                    selected = protocol == value,
                                    onClick = { viewModel.onProtocolChange(value) },
                                    label = { Text(label) }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = protocolValue,
                            onValueChange = { viewModel.onProtocolValueChange(it) },
                            label = { Text(protocolHints[protocol] ?: "Значение") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }
                }
            },
            confirmButton = { TextButton(onClick = { viewModel.addBlock() }) { Text("Добавить") } },
            dismissButton = { TextButton(onClick = { viewModel.closeBlockDialog() }) { Text("Отмена") } }
        )
    }

    if (showExerciseDialog) {
        // Подсказки из библиотеки: пока имя не совпало с найденным целиком
        val suggestions = remember(exName) {
            val query = exName.trim()
            if (query.isEmpty()) emptyList()
            else ExerciseLibraryRegistry.search(query)
                .filter { !it.exerciseName.equals(query, ignoreCase = true) }
                .take(5)
        }

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
                    if (suggestions.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        suggestions.forEach { entry ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp).combinedClickable(
                                    onClick = { viewModel.applyFromLibrary(entry) }
                                ),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                                    Text(entry.exerciseName, style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        "${entry.libraryTitle} · ${entry.categoryTitle}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                    }
                    if (!blockIsCircuit) {
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
                        // В Circuit-блоке схема уже задана протоколом блока — тут только количество на раунд
                        OutlinedTextField(
                            value = exReps,
                            onValueChange = { viewModel.onRepsChange(it) },
                            label = { Text(if (exMetric == "TIME") "Время" else if (exMetric == "DISTANCE") "Метры" else if (exMetric == "CALORIES") "Ккал" else "Повторов на раунд") },
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
    containerColor = Color.Transparent,
    
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
                EmptyState(
                    icon = Icons.Default.ViewModule,
                    title = "Тренировка пуста",
                    subtitle = "Добавьте первый блок: разминка, силовая или меткон — через «+» сверху."
                )
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
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(block.blockTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        protocolBadge(block)?.let { badge ->
                                            Text(badge, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                        }
                                    }
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
                                                onClick = { viewModel.openExerciseDialog(block, ex) },
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
                                                val plan = if (ex.targetSets == 0) {
                                                    // Circuit: показываем метрику, если повторения не заданы (метры, ккал, время)
                                                    if (ex.targetReps.isBlank()) metricLabels.firstOrNull { it.first == ex.metricType }?.second ?: ""
                                                    else ex.targetReps
                                                } else "${ex.targetSets}×${ex.targetReps}"
                                                if (plan.isNotBlank()) {
                                                    Text(plan, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedButton(
                                    onClick = { viewModel.openExerciseDialog(block) },
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
