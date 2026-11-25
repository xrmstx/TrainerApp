package com.example.trainerapp.ui.screens

import android.app.Application
import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.SelfImprovement
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
import androidx.room.withTransaction
import com.example.trainerapp.data.local.AppDatabase
import com.example.trainerapp.data.model.ExerciseBlock
import com.example.trainerapp.data.model.Macrocycle
import com.example.trainerapp.data.model.TemplateExercise
import com.example.trainerapp.data.model.Workout
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.FitnessCenter
import com.example.trainerapp.ui.components.EmptyState
import androidx.compose.animation.*
import androidx.compose.animation.core.tween

// внутри Column, где раньше был if/else с LazyColumn:
AnimatedContent(
    targetState = currentWeek,
    transitionSpec = {
        // Перелистывание: неделя вправо — содержимое уезжает вправо, приходит слева, и наоборот
        if (targetState > initialState) {
            (slideInHorizontally(tween(300)) { it / 4 } + fadeIn(tween(300))) togetherWith
                (slideOutHorizontally(tween(300)) { -it / 4 } + fadeOut(tween(300)))
        } else {
            (slideInHorizontally(tween(300)) { -it / 4 } + fadeIn(tween(300))) togetherWith
                (slideOutHorizontally(tween(300)) { it / 4 } + fadeOut(tween(300)))
        }
    },
    label = "weekContent"
) { week ->
    val weekWorkouts = allWorkouts.filter { it.weekNumber == week }.sortedBy { it.dayNumber }
    if (weekWorkouts.isEmpty()) {
        EmptyState(
            icon = Icons.Default.FitnessCenter,
            title = "Неделя $week пуста",
            subtitle = if (week > 1) "Скопируйте предыдущую неделю с прогрессией или добавьте дни вручную"
                       else "Добавьте первый день через «+» внизу",
            actionLabel = if (week > 1) null else "Добавить день",
            onAction = if (week > 1) null else ({ viewModel.openDialog() })
        )
    } else {
        LazyColumn {
            items(weekWorkouts, key = { it.id }) { workout ->
                // карточка тренировки — как было
            }
        }
    }
}


// было:
if (weekWorkouts.isEmpty()) {
    Box(...) { Text("Неделя $currentWeek пуста....") }
} else { LazyColumn { ... } }

// стало:
if (weekWorkouts.isEmpty()) {
    EmptyState(
        icon = Icons.Default.FitnessCenter,
        title = "Неделя $currentWeek пуста",
        subtitle = if (currentWeek > 1) "Скопируйте предыдущую неделю с прогрессией или добавьте дни вручную"
                   else "Добавьте первый день через «+» внизу",
        actionLabel = if (currentWeek > 1) null else "Добавить день",
        onAction = if (currentWeek > 1) null else ({ viewModel.openDialog() })
    )
} else {
    LazyColumn { /* как было */ }
}


class MacroDetailViewModel(application: Application, val macroId: Long) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).appDao()
    val workouts = dao.getWorkoutsForMacro(macroId)

    private val _currentWeek = MutableStateFlow(1)
    val currentWeek: StateFlow<Int> = _currentWeek.asStateFlow()

    private val _showDialog = MutableStateFlow(false)
    val showDialog: StateFlow<Boolean> = _showDialog.asStateFlow()
    private val _workoutName = MutableStateFlow("")
    val workoutName: StateFlow<String> = _workoutName.asStateFlow()
    private val _dayType = MutableStateFlow("TRAINING")
    val dayType: StateFlow<String> = _dayType.asStateFlow()

    private val _showCopyDialog = MutableStateFlow(false)
    val showCopyDialog: StateFlow<Boolean> = _showCopyDialog.asStateFlow()
    private val _progression = MutableStateFlow("")
    val progression: StateFlow<String> = _progression.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun clearError() { _error.value = null }

    fun onWeekChange(week: Int) { _currentWeek.value = week }
    fun onNameChange(name: String) { _workoutName.value = name }
    fun onDayTypeChange(type: String) { _dayType.value = type }

    fun openDialog() {
        _showDialog.value = true
        _workoutName.value = ""
        _dayType.value = "TRAINING"
    }
    fun closeDialog() { _showDialog.value = false; _workoutName.value = "" }

    fun openCopyDialog() { _showCopyDialog.value = true; _progression.value = "" }
    fun closeCopyDialog() { _showCopyDialog.value = false }
    fun onProgressionChange(value: String) { _progression.value = value }

    fun addWorkout() {
        viewModelScope.launch {
            try {
                val macro = dao.getMacroSync(macroId) ?: run {
                    _error.value = "Макроцикл id=$macroId не найден в базе."
                    return@launch
                }
                val week = _currentWeek.value
                val type = _dayType.value
                // Имя можно не придумывать: по умолчанию «Тренировка N»
                val maxDay = dao.getMaxDayNumber(macroId, week) ?: 0
                val name = _workoutName.value.trim()
                    .ifEmpty { if (type == "RECOVERY") "Восстановление" else "Тренировка ${maxDay + 1}" }

                val workoutId = dao.insertWorkout(
                    Workout(
                        mesocycleId = macroId,
                        weekNumber = week,
                        dayNumber = maxDay + 1,
                        dayType = type,
                        name = name
                    )
                )

                // Восстановительный день наполняется сразу типовыми блоками — их потом можно править
                if (type == "RECOVERY") {
                    val cardioId = dao.insertExerciseBlock(
                        ExerciseBlock(workoutId = workoutId, blockTitle = "Зона 2 кардио", blockType = "LINEAR", order = 1)
                    )
                    dao.insertTemplateExercise(
                        TemplateExercise(
                            blockId = cardioId, exerciseName = "Ходьба или велосипед в зоне 2",
                            order = 1, targetSets = 1, targetReps = "40 мин", metricType = "TIME",
                            note = "Пульс 120–140, дыхание носом, разговорный темп"
                        )
                    )
                    val stretchId = dao.insertExerciseBlock(
                        ExerciseBlock(workoutId = workoutId, blockTitle = "Растяжка и заминка", blockType = "LINEAR", order = 2)
                    )
                    dao.insertTemplateExercise(
                        TemplateExercise(
                            blockId = stretchId, exerciseName = "Растяжка основных групп мышц",
                            order = 1, targetSets = 1, targetReps = "10 мин", metricType = "TIME"
                        )
                    )
                    dao.insertTemplateExercise(
                        TemplateExercise(
                            blockId = stretchId, exerciseName = "Спокойное дыхание и расслабление",
                            order = 2, targetSets = 1, targetReps = "5 мин", metricType = "TIME"
                        )
                    )
                }
                closeDialog()
            } catch (e: Exception) {
                _error.value = e.message ?: e.toString()
            }
        }
    }

    fun deleteWorkout(id: Long) {
        viewModelScope.launch {
            try {
                dao.deleteWorkout(id) // каскад удалит блоки и упражнения
            } catch (e: Exception) {
                _error.value = e.message ?: e.toString()
            }
        }
    }

    /**
     * Копирует все тренировки недели (week - 1) в текущую неделю.
     * Прогрессия применяется только к силовым (LINEAR) упражнениям с заданным весом:
     * "+2.5" — плюс 2.5 кг, "5%" — плюс 5 процентов, пусто — точная копия.
     */
    fun copyPreviousWeek() {
        viewModelScope.launch {
            try {
                val week = _currentWeek.value
                if (week <= 1) return@launch
                val db = AppDatabase.getDatabase(getApplication())

                val source = dao.getWorkoutsForMacroWeekSync(macroId, week - 1)
                if (source.isEmpty()) {
                    _error.value = "Неделя ${week - 1} пуста — копировать нечего."
                    return@launch
                }

                val progression = _progression.value.trim()
                val percent = progression.endsWith("%")
                val step = progression.trimEnd('%').replace(',', '.').toDoubleOrNull() ?: 0.0

                db.withTransaction {
                    // Перезаписываем неделю копией: старое содержимое (если было) уходит
                    dao.deleteWorkoutsOfWeek(macroId, week)
                    source.forEach { originalWorkout ->
                        val newWorkoutId = dao.insertWorkout(
                            originalWorkout.copy(id = 0, weekNumber = week)
                        )
                        dao.getBlocksForWorkoutSync(originalWorkout.id).forEach { block ->
                            val newBlockId = dao.insertExerciseBlock(
                                block.copy(id = 0, workoutId = newWorkoutId)
                            )
                            dao.getExercisesForBlockSync(block.id).forEach { ex ->
                                val isStrength = block.blockType == "LINEAR"
                                val newWeight = if (isStrength && ex.targetWeight != null && step > 0.0) {
                                    if (percent) ex.targetWeight * (1 + step / 100) else ex.targetWeight + step
                                } else ex.targetWeight
                                dao.insertTemplateExercise(
                                    ex.copy(id = 0, blockId = newBlockId, targetWeight = newWeight)
                                )
                            }
                        }
                    }
                }
                closeCopyDialog()
            } catch (e: Exception) {
                _error.value = e.message ?: e.toString()
            }
        }
    }

    fun shareProgram(macroName: String, onReady: (String) -> Unit) {
        viewModelScope.launch {
            val workouts = dao.getWorkoutsForMacroSync(macroId)
            val weeks = workouts.map { it.weekNumber }.distinct().sorted()
            val text = buildString {
                appendLine("🏋️ Программа: $macroName")
                appendLine()
                weeks.forEach { week ->
                    appendLine("━━ Неделя $week ━")
                    workouts.filter { it.weekNumber == week }.sortedBy { it.dayNumber }.forEach { workout ->
                        val dayLabel = if (workout.dayType == "RECOVERY") "Восстановление" else "День ${workout.dayNumber}"
                        appendLine("📅 $dayLabel: ${workout.name}")
                        dao.getBlocksForWorkoutSync(workout.id).forEach { block ->
                            appendLine("  ${block.blockTitle}:")
                            dao.getExercisesForBlockSync(block.id).forEach { ex ->
                                val plan = if (ex.targetSets == 0) ex.targetReps else "${ex.targetSets}×${ex.targetReps}"
                                val weightText = ex.targetWeight?.let { " · ${formatWeight(it)} кг" } ?: ""
                                appendLine("    • ${ex.exerciseName} — $plan$weightText")
                            }
                        }
                        appendLine()
                    }
                }
                appendLine("💪 Успехов в тренировках!")
            }
            onReady(text)
        }
    }
}

fun formatWeight(w: Double): String =
    if (w == w.toLong().toDouble()) w.toLong().toString() else w.toString()

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
    val allWorkouts by viewModel.workouts.collectAsState(initial = emptyList())
    val currentWeek by viewModel.currentWeek.collectAsState()
    val showDialog by viewModel.showDialog.collectAsState()
    val workoutName by viewModel.workoutName.collectAsState()
    val dayType by viewModel.dayType.collectAsState()
    val showCopyDialog by viewModel.showCopyDialog.collectAsState()
    val progression by viewModel.progression.collectAsState()
    val error by viewModel.error.collectAsState()

    var workoutToDelete by remember { mutableStateOf<Workout?>(null) }

    // Недели определяем по факту: что создано — то и показываем
    val weekNumbers = allWorkouts.map { it.weekNumber }.distinct().sorted()
    val maxWeek = weekNumbers.maxOrNull() ?: 1
    val weekWorkouts = allWorkouts.filter { it.weekNumber == currentWeek }.sortedBy { it.dayNumber }
    val weekIsEmpty = weekWorkouts.isEmpty()

    error?.let { message ->
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("Ошибка") },
            text = { Text(message) },
            confirmButton = { TextButton(onClick = { viewModel.clearError() }) { Text("Понятно") } }
        )
    }

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
            title = { Text("Новый день · неделя $currentWeek") },
            text = {
                Column {
                    OutlinedTextField(
                        value = workoutName,
                        onValueChange = { viewModel.onNameChange(it) },
                        label = { Text("Название (необязательно)") },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = dayType == "TRAINING",
                            onClick = { viewModel.onDayTypeChange("TRAINING") },
                            label = { Text("Тренировка") }
                        )
                        FilterChip(
                            selected = dayType == "RECOVERY",
                            onClick = { viewModel.onDayTypeChange("RECOVERY") },
                            label = { Text("Восстановление") }
                        )
                    }
                    if (dayType == "RECOVERY") {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Будут созданы блоки «Зона 2 кардио» и «Растяжка» — потом можно изменить.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            },
            confirmButton = { TextButton(onClick = { viewModel.addWorkout() }) { Text("Добавить") } },
            dismissButton = { TextButton(onClick = { viewModel.closeDialog() }) { Text("Отмена") } }
        )
    }

    if (showCopyDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.closeCopyDialog() },
            title = { Text("Скопировать неделю ${currentWeek - 1} → $currentWeek") },
            text = {
                Column {
                    Text(
                        "Все тренировки, блоки и упражнения недели ${currentWeek - 1} будут скопированы. " +
                            "Текущее содержимое недели $currentWeek, если оно есть, будет заменено.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = progression,
                        onValueChange = { viewModel.onProgressionChange(it) },
                        label = { Text("Прогрессия весов (необязательно)") },
                        supportingText = { Text("Например: +2.5 (кг) или 5 (%). Пусто — точная копия") },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Прогрессия меняет только силовые упражнения с заданным весом. Меткон, кардио и растяжка копируются как есть.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            },
            confirmButton = { TextButton(onClick = { viewModel.copyPreviousWeek() }) { Text("Скопировать") } },
            dismissButton = { TextButton(onClick = { viewModel.closeCopyDialog() }) { Text("Отмена") } }
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
            // Переключатель недель
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                (1..maxOf(maxWeek, 1)).forEach { week ->
                    FilterChip(
                        selected = week == currentWeek,
                        onClick = { viewModel.onWeekChange(week) },
                        label = { Text(if (week in weekNumbers) "Нед. $week" else "Нед. $week +") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Быстрое заполнение: копия предыдущей недели с прогрессией
            if (currentWeek > 1 && weekIsEmpty) {
                Button(
                    onClick = { viewModel.openCopyDialog() },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Скопировать неделю ${currentWeek - 1}", fontSize = MaterialTheme.typography.bodyLarge.fontSize)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (weekWorkouts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "Неделя $currentWeek пуста.\nДобавьте дни через «+» или скопируйте предыдущую неделю.",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                LazyColumn {
                    items(weekWorkouts, key = { it.id }) { workout ->
                        val isRecovery = workout.dayType == "RECOVERY"
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).combinedClickable(
                                onClick = { onWorkoutClick(workout.id, workout.name) },
                                onLongClick = { workoutToDelete = workout }
                            ),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isRecovery) {
                                        Icon(
                                            Icons.Default.SelfImprovement,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.tertiary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }
                                    Text(
                                        if (isRecovery) "Восстановительный день" else "День ${workout.dayNumber}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (isRecovery) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(workout.name, style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}
