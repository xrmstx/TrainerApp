package com.example.trainerapp.ui.screens

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Link
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

// Событие: в связке groupId введён вес weight. stamp нужен,
// чтобы повторный ввод того же веса заново триггерил автоподстановку.
data class WeightBroadcast(
    val groupId: Long,
    val weight: Double,
    val stamp: Long = System.nanoTime()
)

class ClientWorkoutLogViewModel(application: Application, val workoutId: Long) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).appDao()
    val exercises = dao.getClientExercises(workoutId)

    private val _clientId = MutableStateFlow<Long?>(null)

    private val _historyDialog = MutableStateFlow<Pair<String, List<ExerciseLog>>?>(null)
    val historyDialog: StateFlow<Pair<String, List<ExerciseLog>>?> = _historyDialog.asStateFlow()

    private val _lastWeights = MutableStateFlow<Map<Long, Double?>>(emptyMap())
    val lastWeights: StateFlow<Map<Long, Double?>> = _lastWeights.asStateFlow()

    // Автозаполнение веса внутри связки: последняя введённая величина
    private val _weightBroadcast = MutableStateFlow<WeightBroadcast?>(null)
    val weightBroadcast: StateFlow<WeightBroadcast?> = _weightBroadcast.asStateFlow()

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

    fun showHistory(exerciseName: String, exerciseId: Long, exerciseKey: String?) {
        viewModelScope.launch {
            val clientId = _clientId.value
            val logs = if (clientId != null) {
                dao.getHistoryForExercise(clientId, exerciseKey, exerciseName)
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

    fun broadcastWeight(groupId: Long?, weight: Double) {
        if (groupId != null && weight > 0) {
            _weightBroadcast.value = WeightBroadcast(groupId, weight)
        }
    }

    fun saveLog(
        exerciseId: Long,
        setNum: String,
        reps: String,
        weight: String,
        note: String,
        isCircuit: Boolean,
        circuitResult: String? = null,
        supersetGroupId: Long? = null
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
                    if (w > 0) broadcastWeight(supersetGroupId, w)
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

/** Разбивает упражнения блока на подряд идущие группы: связки и одиночные */
private fun groupBySuperset(list: List<ClientExercise>): List<Pair<Int?, List<ClientExercise>>> {
    val result = mutableListOf<Pair<Int?, List<ClientExercise>>>()
    list.sortedBy { it.order }.forEach { ex ->
        val last = result.lastOrNull()
        if (ex.supersetGroup != null && last?.first == ex.supersetGroup) {
            result[result.lastIndex] = last.first to (last.second + ex)
        } else {
            result.add(ex.supersetGroup to listOf(ex))
        }
    }
    return result
}

@Composable
private fun ExerciseLogCard(
    ex: ClientExercise,
    inSuperset: Boolean,
    viewModel: ClientWorkoutLogViewModel,
    lastWeights: Map<Long, Double?>,
    weightBroadcast: WeightBroadcast?,
    onSaved: (String) -> Unit
) {
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

    // Автоподстановка веса из соседей по связке — только в пустое поле
    LaunchedEffect(weightBroadcast) {
        val b = weightBroadcast ?: return@LaunchedEffect
        if (b.groupId == ex.supersetGroup && b.weight > 0 && weight.trim().isEmpty()) {
            weight = formatWeight(b.weight)
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
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (inSuperset) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(ex.exerciseName, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                TextButton(onClick = { viewModel.showHistory(ex.exerciseName, ex.id, ex.exerciseKey) }) { Text("История") }
            }
            // У участника связки подходы уже указаны в заголовке связки — тут только повторы и вес
            val planText = if (isCircuit) {
                protocolLabel(ex) ?: "Меткон"
            } else if (inSuperset) {
                ex.targetReps + (ex.targetWeight?.let { " · ${formatWeight(it)} кг" } ?: "")
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
                        onValueChange = {
                            weight = it
                            viewModel.broadcastWeight(ex.supersetGroup, it.trim().replace(',', '.').toDoubleOrNull() ?: 0.0)
                        },
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
                        onSaved("Записано: $result")
                        rounds = ""; extraReps = ""; timeText = ""; freeResult = ""
                    } else {
                        val r = reps.toIntOrNull() ?: 0
                        if (r <= 0) {
                            showError = true
                            return@Button
                        }
                        val s = setNum.toIntOrNull() ?: 1
                        viewModel.saveLog(ex.id, setNum, reps, weight, note, false, supersetGroupId = ex.supersetGroup)
                        val over = if (targetSets > 0 && s > targetSets) " (сверх плана)" else ""
                        onSaved("Записано: подход $s, ${weight.trim().ifEmpty { "0" }} кг$over")
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientWorkoutLogScreen(workoutId: Long, workoutName: String, onBackClick: () -> Unit) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val viewModel: ClientWorkoutLogViewModel = viewModel(factory = ClientWorkoutLogViewModelFactory(application, workoutId))

    val exercises by viewModel.exercises.collectAsState(initial = emptyList())
    val historyDialog by viewModel.historyDialog.collectAsState()
    val lastWeights by viewModel.lastWeights.collectAsState()
    val weightBroadcast by viewModel.weightBroadcast.collectAsState()
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
        containerColor = Color.Transparent,
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
                        groupBySuperset(list).forEach { (supersetId, groupExs) ->
                            if (groupExs.size == 1) {
                                val ex = groupExs.first()
                                item(key = ex.id) {
                                    ExerciseLogCard(
                                        ex = ex,
                                        inSuperset = false,
                                        viewModel = viewModel,
                                        lastWeights = lastWeights,
                                        weightBroadcast = weightBroadcast,
                                        onSaved = { showSavedSnackbar(it) }
                                    )
                                }
                            } else {
                                item(key = "superset_${groupExs.first().id}") {
                                    val sets = groupExs.map { it.targetSets }.distinct()
                                    val roundsLabel = if (sets.size == 1 && sets.first() > 0) " ×${sets.first()}" else ""
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp)
                                            .border(1.dp, MaterialTheme.colorScheme.secondary, RoundedCornerShape(12.dp))
                                            .padding(8.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.Link,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                                tint = MaterialTheme.colorScheme.secondary
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                "Связка$roundsLabel — выполняется непрерывно, подходы общие",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.secondary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        groupExs.forEach { ex ->
                                            ExerciseLogCard(
                                                ex = ex,
                                                inSuperset = true,
                                                viewModel = viewModel,
                                                lastWeights = lastWeights,
                                                weightBroadcast = weightBroadcast,
                                                onSaved = { showSavedSnackbar(it) }
                                            )
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
}
