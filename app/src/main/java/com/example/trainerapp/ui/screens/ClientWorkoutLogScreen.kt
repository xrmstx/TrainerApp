package com.example.trainerapp.ui.screens

import android.app.Application
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
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import com.example.trainerapp.data.model.ExerciseLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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
            dao.getClientExercises(workoutId).first().forEach { ex ->
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

    fun saveLog(exerciseId: Long, setNum: String, reps: String, weight: String, note: String, isCircuit: Boolean) {
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
                        resultText = if (isCircuit) reps.trim() else null,
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

fun formatWeight(w: Double): String =
    if (w == w.toLong().toDouble()) w.toLong().toString() else w.toString()

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

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun showSavedSnackbar(setNum: Int, weightText: String) {
        scope.launch {
            snackbarHostState.showSnackbar("Записано: подход $setNum, $weightText кг")
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
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("В этой тренировке нет упражнений.", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                }
            } else {
                val grouped = exercises.groupBy { it.blockTitle }
                LazyColumn {
                    grouped.forEach { (blockTitle, list) ->
                        item(key = "header_$blockTitle") {
                            Text(blockTitle, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 8.dp))
                        }
                        items(list, key = { it.id }) { ex ->
                            var setNum by remember(ex.id) { mutableStateOf("1") }
                            var reps by remember(ex.id) { mutableStateOf(ex.targetReps) }
                            var weight by remember(ex.id) { mutableStateOf("") }
                            var note by remember(ex.id) { mutableStateOf("") }
                            var showError by remember(ex.id) { mutableStateOf(false) }
                            val isCircuit = ex.blockType == "CIRCUIT"

                            LaunchedEffect(lastWeights) {
                                if (!isCircuit && weight.isEmpty()) {
                                    lastWeights[ex.id]?.let { weight = formatWeight(it) }
                                }
                            }

                            val doneSets = (setNum.toIntOrNull() ?: 1) - 1
                            val targetSets = ex.targetSets.coerceAtLeast(0)
                            val progress = if (targetSets > 0) {
                                (doneSets.toFloat() / targetSets).coerceIn(0f, 1f)
                            } else 0f

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
                                    Text(
                                        "План: ${if (isCircuit) "Меткон" else "${ex.targetSets} × ${ex.targetReps}"}" +
                                            (ex.targetWeight?.let { " · ${formatWeight(it)} кг" } ?: ""),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))

                                    if (isCircuit) {
                                        OutlinedTextField(
                                            value = reps,
                                            onValueChange = { reps = it },
                                            label = { Text("Результат") },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp)
                                        )
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
                                                Icon(Icons.Default.Remove, contentDescription = "Минус 2.5 кг", modifier = Modifier.size(20.dp))
                                                Text("2.5", modifier = Modifier.padding(start = 2.dp))
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
                                                Icon(Icons.Default.Add, contentDescription = "Плюс 2.5 кг", modifier = Modifier.size(20.dp))
                                                Text("2.5", modifier = Modifier.padding(start = 2.dp))
                                            }
                                        }
                                    }

                                    if (!isCircuit && targetSets > 0 && doneSets > 0) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            LinearProgressIndicator(
                                                progress = progress,
                                                modifier = Modifier.weight(1f),
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                "$doneSets из $targetSets",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.primary
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
                                            val r = reps.toIntOrNull() ?: 0
                                            if (!isCircuit && r <= 0) {
                                                showError = true
                                                return@Button
                                            }
                                            val s = setNum.toIntOrNull() ?: 1
                                            viewModel.saveLog(ex.id, setNum, reps, weight, note, isCircuit)
                                            if (!isCircuit) {
                                                showSavedSnackbar(s, weight.trim().ifEmpty { "0" })
                                                setNum = (s + 1).toString()
                                                weight = ""
                                            } else {
                                                showSavedSnackbar(s, reps.trim())
                                                reps = ""
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
