package com.example.trainerapp.ui.screens

import android.app.Application
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trainerapp.data.local.AppDatabase
import com.example.trainerapp.data.model.ClientExercise
import com.example.trainerapp.data.model.ExerciseLog
import com.example.trainerapp.ui.components.EmptyState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ClientWorkoutLogViewModel(application: Application, val workoutId: Long) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).appDao()
    val exercises = dao.getClientExercises(workoutId)

    private val _clientId = MutableStateFlow<Long?>(null)

    private val _historyDialog = MutableStateFlow<Pair<String, List<ExerciseLog>>?>(null)
    val historyDialog: StateFlow<Pair<String, List<ExerciseLog>>?> = _historyDialog.asStateFlow()

    private val _lastWeights = MutableStateFlow<Map<Long, Double?>>(emptyMap())
    val lastWeights: StateFlow<Map<Long, Double?>> = _lastWeights.asStateFlow()

    private val _isDone = MutableStateFlow(false)
    val isDone: StateFlow<Boolean> = _isDone.asStateFlow()

    init {
        viewModelScope.launch {
            _clientId.value = dao.getClientIdForWorkoutSync(workoutId)
            _isDone.value = dao.getClientWorkoutSync(workoutId)?.status != "PLANNED"
            val map = mutableMapOf<Long, Double?>()
            dao.getClientExercisesSync(workoutId).forEach { ex ->
                if (ex.blockType != "CIRCUIT") map[ex.id] = dao.getLastLogForExercise(ex.id)?.actualWeight
            }
            _lastWeights.value = map
        }
    }

    fun showHistory(exerciseName: String, exerciseId: Long) {
        viewModelScope.launch {
            val clientId = _clientId.value
            val logs = if (clientId != null) {
                dao.getHistoryForExercise(clientId, exerciseName)
            } else {
                dao.getLogsForExerciseSync(exerciseId)
            }
            _historyDialog.value = Pair(exerciseName, logs)
        }
    }

    fun closeHistory() { _historyDialog.value = null }

    fun toggleDone() {
        viewModelScope.launch {
            val newStatus = if (_isDone.value) "PLANNED" else "DONE"
            dao.setClientWorkoutStatus(workoutId, newStatus)
            _isDone.value = newStatus != "PLANNED"
        }
    }

    fun saveLog(
        exerciseId: Long,
        setNum: String,
        reps: String,
        weight: String,
        note: String,
        isCircuit: Boolean,
        circuitResult: String? = null
    ) {
        val s = setNum.toIntOrNull() ?: 1
        val r = reps.toIntOrNull() ?: 0
        val w = weight.trim().replace(',', '.').toDoubleOrNull() ?: 0.0
        if (r > 0 || isCircuit) {
            viewModelScope.launch {
                dao.insertExerciseLog(
                    ExerciseLog(
                        clientExerciseId = exerciseId,
                        setNumber = s,
                        actualReps = r,
                        actualWeight = w,
                        resultText = if (isCircuit) (circuitResult ?: reps.trim()) else null,
                        note = note.trim().ifEmpty { null }
                    )
                )
                if (!isCircuit) {
                    _lastWeights.value = _lastWeights.value + (exerciseId to w)
                }
            }
        }
    }
}

class ClientWorkoutLogViewModelFactory(
    private val application: Application,
    private val workoutId: Long
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ClientWorkoutLogViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST") return ClientWorkoutLogViewModel(application, workoutId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// Человекочитаемая подпись протокола для клиентского упражнения
private fun protocolLabel(ex: ClientExercise): String? {
    if (ex.blockType != "CIRCUIT" || ex.protocol == "NONE") return null
    return when (ex.protocol) {
        "AMRAP" -> "AMRAP ${ex.protocolValue} мин"
        "EMOM" -> "EMOM ${ex.protocolValue} мин"
        "FOR_TIME" -> "For Time" + (if (ex.protocolValue.isNotBlank()) " · ${ex.protocolValue}" else "")
        "RFT" -> "RFT ${ex.protocolValue}"
        "TABATA" -> "Tabata ${ex.protocolValue}"
        "CUSTOM" -> if (ex.protocolValue.isNotBlank()) ex.protocolValue else "Своя схема"
        else -> null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientWorkoutLogScreen(workoutId: Long, workoutName: String, onBackClick: () -> Unit) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val viewModel: ClientWorkoutLogViewModel = viewModel(factory = ClientWorkoutLogViewModelFactory(application, workoutId))

    val exercises by viewModel.exercises.collectAsState(initial = emptyList())
    val historyDialog by viewModel.historyDialog.collectAsState()
    val lastWeights by viewModel.lastWeights.collectAsState()
    val isDone by viewModel.isDone.collectAsState()

    val keyboard = LocalSoftwareKeyboardController.current

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun showSavedSnackbar(message: String) {
        scope.launch {
            snackbarHostState.showSnackbar(message)
        }
    }

    historyDialog?.let { (name, logs) ->
        AlertDialog(
            onDismissRequest = { viewModel.closeHistory() },
            title = { Text("История: $name") },
            text = {
                if (logs.isEmpty()) Text("Нет записей.")
                else LazyColumn(modifier = Modifier.heightIn(max = 360.dp)) {
                    items(logs, key = { it.id }) { log ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                val dateText = java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault())
                                    .format(java.util.Date(log.date))
                                val mainLine = if (log.resultText != null) {
                                    "Меткон: ${log.resultText} · $dateText"
                                } else {
                                    "Подход ${log.setNumber}: ${formatWeight(log.actualWeight)} кг × ${log.actualReps} · $dateText"
                                }
                                Text(mainLine, style = MaterialTheme.typography.bodyMedium)
                                if (!log.note.isNullOrBlank()) {
                                    Text(log.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { viewModel.closeHistory() }) { Text("Закрыть") } }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(workoutName) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, contentDescription = "Назад") }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleDone() }) {
                        Icon(
                            if (isDone) Icons.Default.CheckCircle else Icons.Default.Check,
                            contentDescription = if (isDone) "Вернуть в план" else "Отметить выполненной",
                            tint = if (isDone) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
            if (exercises.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.EventNote,
                    title = "Пустая тренировка",
                    subtitle = "В этой тренировке нет упражнений — попросите тренера дополнить план."
                )
            } else {
                val grouped = exercises.groupBy { it.blockTitle }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        // Тап по пустому месту списка прячет клавиатуру
                        .pointerInput(Unit) {
                            detectTapGestures(onTap = { keyboard?.hide() })
                        }
                ) {
                    grouped.forEach { (blockTitle, list) ->
                        item(key = "header_$blockTitle") {
                            Text(blockTitle, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 8.dp))
                        }
                        items(list, key = { it.id }) { ex ->
                            val isCircuit = ex.blockType == "CIRCUIT"
                            val protocol = ex.protocol

                            var setNum by remember(ex.id) { mutableStateOf("1") }
                            var reps by remember(ex.id) { mutableStateOf(ex.targetReps) }
                            var weight by remember(ex.id) { mutableStateOf("") }
                            var note by remember(ex.id) { mutableStateOf("") }
                            var showError by remember(ex.id) { mutableStateOf(false) }

                            // Поля результата для меткона — зависят от протокола
                            var rounds by remember(ex.id) { mutableStateOf("") }
                            var extraReps by remember(ex.id) { mutableStateOf("") }
                            var timeText by remember(ex.id) { mutableStateOf("") }
                            var freeResult by remember(ex.id) { mutableStateOf("") }
                            var circuitError by remember(ex.id) { mutableStateOf(false) }

                            // Первичная подстановка последнего веса из базы (до первой записи в этой сессии)
                            LaunchedEffect(lastWeights) {
                                if (!isCircuit && weight.isEmpty()) {
                                    lastWeights[ex.id]?.let { weight = formatWeight(it) }
                                }
                            }

                            val targetSets = ex.targetSets.coerceAtLeast(0)
                            val doneSets = (setNum.toIntOrNull() ?: 1) - 1
                            val overBy = (doneSets - targetSets).coerceAtLeast(0)
                            val progress = when {
                                targetSets <= 0 -> 0f
                                doneSets >= targetSets -> 1f
                                else -> doneSets.toFloat() / targetSets
                            }

                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(ex.exerciseName, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                                        TextButton(onClick = { viewModel.showHistory(ex.exerciseName, ex.id) }) { Text("История") }
                                    }
                                    val planText = if (isCircuit) {
                                        protocolLabel(ex) ?: "Меткон"
                                    } else {
                                        "${ex.targetSets} × ${ex.targetReps}" + (ex.targetWeight?.let { " · ${formatWeight(it)} кг" } ?: "")
                                    }
                                    Text(
                                        "План: $planText",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))

                                    if (isCircuit) {
                                        when (protocol) {
                                            "AMRAP" -> {
                                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    OutlinedTextField(
                                                        value = rounds,
                                                        onValueChange = {
                                                            rounds = it
                                                            circuitError = false
                                                        },
                                                        label = { Text("Раундов") },
                                                        modifier = Modifier.weight(1f),
                                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                        shape = RoundedCornerShape(12.dp),
                                                        textStyle = LocalTextStyle.current.copy(fontSize = 20.sp),
                                                        isError = circuitError,
                                                        supportingText = { if (circuitError) Text("Укажите раунды") }
                                                    )
                                                    OutlinedTextField(
                                                        value = extraReps,
                                                        onValueChange = { extraReps = it },
                                                        label = { Text("Доп. повт.") },
                                                        modifier = Modifier.weight(1f),
                                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                        shape = RoundedCornerShape(12.dp),
                                                        textStyle = LocalTextStyle.current.copy(fontSize = 20.sp)
                                                    )
                                                }
                                            }
                                            "EMOM" -> {
                                                OutlinedTextField(
                                                    value = rounds,
                                                    onValueChange = {
                                                        rounds = it
                                                        circuitError = false
                                                    },
                                                    label = { Text("Выполнено раундов из ${ex.protocolValue.ifBlank { "?" }}") },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                    shape = RoundedCornerShape(12.dp),
                                                    singleLine = true,
                                                    textStyle = LocalTextStyle.current.copy(fontSize = 20.sp),
                                                    isError = circuitError,
                                                    supportingText = { if (circuitError) Text("Укажите раунды") }
                                                )
                                            }
                                            "FOR_TIME", "RFT" -> {
                                                OutlinedTextField(
                                                    value = timeText,
                                                    onValueChange = {
                                                        timeText = it
                                                        circuitError = false
                                                    },
                                                    label = { Text("Время (мин:сек)") },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(12.dp),
                                                    singleLine = true,
                                                    textStyle = LocalTextStyle.current.copy(fontSize = 20.sp),
                                                    isError = circuitError,
                                                    supportingText = { if (circuitError) Text("Укажите время, например 12:34") }
                                                )
                                            }
                                            else -> {
                                                OutlinedTextField(
                                                    value = freeResult,
                                                    onValueChange = {
                                                        freeResult = it
                                                        circuitError = false
                                                    },
                                                    label = { Text("Результат") },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(12.dp),
                                                    isError = circuitError,
                                                    supportingText = { if (circuitError) Text("Заполните результат") }
                                                )
                                            }
                                        }
                                    } else {
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            OutlinedTextField(
                                                value = setNum,
                                                onValueChange = { setNum = it },
                                                label = { Text("Подх.") },
                                                modifier = Modifier.weight(0.5f),
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                shape = RoundedCornerShape(12.dp),
                                                textStyle = LocalTextStyle.current.copy(fontSize = 20.sp)
                                            )
                                            OutlinedTextField(
                                                value = weight,
                                                onValueChange = { weight = it },
                                                label = { Text("Вес") },
                                                modifier = Modifier.weight(1.2f),
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                                shape = RoundedCornerShape(12.dp),
                                                textStyle = LocalTextStyle.current.copy(fontSize = 20.sp)
                                            )
                                            OutlinedTextField(
                                                value = reps,
                                                onValueChange = {
                                                    reps = it
                                                    showError = false
                                                },
                                                label = { Text("Повт.") },
                                                modifier = Modifier.weight(0.8f),
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                shape = RoundedCornerShape(12.dp),
                                                textStyle = LocalTextStyle.current.copy(fontSize = 20.sp),
                                                isError = showError,
                                                supportingText = { if (showError) Text("Укажите повторы") }
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Шаговые кнопки веса: крупное касание вместо клавиатуры
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            OutlinedButton(
                                                onClick = {
                                                    val w = weight.trim().replace(',', '.').toDoubleOrNull() ?: ex.targetWeight ?: 0.0
                                                    weight = formatWeight((w - 2.5).coerceAtLeast(0.0))
                                                },
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier.height(44.dp)
                                            ) {
                                                Text("− 2.5", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = if (weight.isBlank()) "—" else weight.trim(),
                                                style = MaterialTheme.typography.titleLarge,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.weight(1f)
                                            )
                                            OutlinedButton(
                                                onClick = {
                                                    val w = weight.trim().replace(',', '.').toDoubleOrNull() ?: ex.targetWeight ?: 0.0
                                                    weight = formatWeight((w + 2.5).coerceAtMost(500.0))
                                                },
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier.height(44.dp)
                                            ) {
                                                Text("+ 2.5", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                                            }
                                        }
                                    }

                                    if (!isCircuit && targetSets > 0 && doneSets > 0) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            LinearProgressIndicator(
                                                progress = progress,
                                                modifier = Modifier.weight(1f),
                                                color = if (overBy > 0) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                if (overBy > 0) "$doneSets из $targetSets · +$overBy сверх" else "$doneSets из $targetSets",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = if (overBy > 0) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = note,
                                        onValueChange = { note = it },
                                        label = { Text("Комментарий") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = {
                                            if (isCircuit) {
                                                // Собираем результат по протоколу
                                                val result: String? = when (protocol) {
                                                    "AMRAP" -> {
                                                        val r = rounds.toIntOrNull()
                                                        if (r != null && r > 0) "${r}+" + (extraReps.toIntOrNull() ?: 0).toString() else null
                                                    }
                                                    "EMOM" -> rounds.toIntOrNull()?.let { "$it/" + ex.protocolValue.ifBlank { "?" } }
                                                    "FOR_TIME", "RFT" -> timeText.trim().ifBlank { null }
                                                    else -> freeResult.trim().ifBlank { null }
                                                }
                                                if (result == null) {
                                                    circuitError = true
                                                    return@Button
                                                }
                                                viewModel.saveLog(ex.id, "1", "0", "0", note, true, result)
                                                showSavedSnackbar("Записано: $result")
                                                rounds = ""; extraReps = ""; timeText = ""; freeResult = ""
                                            } else {
                                                val r = reps.toIntOrNull() ?: 0
                                                if (r <= 0) {
                                                    showError = true
                                                    return@Button
                                                }
                                                val s = setNum.toIntOrNull() ?: 1
                                                viewModel.saveLog(ex.id, setNum, reps, weight, note, false)
                                                val over = if (targetSets > 0 && s > targetSets) " (сверх плана)" else ""
                                                showSavedSnackbar("Записано: подход $s, ${weight.trim().ifEmpty { "0" }} кг$over")
                                                setNum = (s + 1).toString()
                                                // Вес НЕ сбрасываем: он остаётся в поле
                                                // и автоматически становится весом следующего подхода.
                                                // LaunchedEffect выше нужен только для первой подстановки из базы.
                                            }
                                            note = ""
                                        },
                                        modifier = Modifier.fillMaxWidth().height(52.dp),
                                        shape = RoundedCornerShape(14.dp)
                                    ) {
                                        Text("Записать", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
