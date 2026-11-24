package com.example.trainerapp.ui.screens

import android.app.Application
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
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
import kotlinx.coroutines.launch

class ClientWorkoutLogViewModel(application: Application, val workoutId: Long) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).appDao()
    val exercises = dao.getClientExercises(workoutId)

    private val _clientId = MutableStateFlow<Long?>(null)

    // Данные для истории: имя упражнения + список логов
    private val _historyDialog = MutableStateFlow<Pair<String, List<ExerciseLog>>?>(null)
    val historyDialog: StateFlow<Pair<String, List<ExerciseLog>>?> = _historyDialog.asStateFlow()

    private val _lastWeights = MutableStateFlow<Map<Long, Double?>>(emptyMap())
    val lastWeights: StateFlow<Map<Long, Double?>> = _lastWeights.asStateFlow()

    init {
        viewModelScope.launch {
            _clientId.value = dao.getClientIdForWorkoutSync(workoutId)
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

    fun deleteLog(logId: Long, exerciseName: String) {
        viewModelScope.launch {
            dao.deleteLog(logId)
            val clientId = _clientId.value
            val fresh = if (clientId != null) {
                dao.getHistoryForExercise(clientId, exerciseName)
            } else emptyList()
            _historyDialog.value = Pair(exerciseName, fresh)
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

private fun formatWeight(w: Double): String =
    if (w == w.toLong().toDouble()) w.toLong().toString() else w.toString()

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ClientWorkoutLogScreen(workoutId: Long, workoutName: String, onBackClick: () -> Unit) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val viewModel: ClientWorkoutLogViewModel = viewModel(factory = ClientWorkoutLogViewModelFactory(application, workoutId))

    val exercises by viewModel.exercises.collectAsState(initial = emptyList())
    val historyDialog by viewModel.historyDialog.collectAsState()
    val lastWeights by viewModel.lastWeights.collectAsState()

    historyDialog?.let { (name, logs) ->
        AlertDialog(
            onDismissRequest = { viewModel.closeHistory() },
            title = { Text("История: $name") },
            text = {
                if (logs.isEmpty()) Text("Нет записей.")
                else LazyColumn(modifier = Modifier.heightIn(max = 360.dp)) {
                    items(logs, key = { it.id }) { log ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).combinedClickable(
                                onClick = {},
                                onLongClick = { viewModel.deleteLog(log.id, name) }
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                val dateText = java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault())
                                    .format(java.util.Date(log.date))
                                Text(
                                    "Подход ${log.setNumber}: ${formatWeight(log.actualWeight)} кг × ${log.actualReps} · $dateText",
                                    style = MaterialTheme.typography.bodyMedium
                                )
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
        topBar = {
            TopAppBar(
                title = { Text(workoutName) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, contentDescription = "Назад") }
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
                val orderedBlocks = grouped.toList()
                LazyColumn {
                    orderedBlocks.forEach { (blockTitle, list) ->
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
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            OutlinedTextField(
                                                value = weight,
                                                onValueChange = { weight = it },
                                                label = { Text("Вес") },
                                                modifier = Modifier.weight(1f),
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                                shape = RoundedCornerShape(12.dp)
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
                                                isError = showError,
                                                supportingText = { if (showError) Text("Укажите повторы") }
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
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = {
                                            val r = reps.toIntOrNull() ?: 0
                                            if (!isCircuit && r <= 0) {
                                                showError = true
                                                return@Button
                                            }
                                            viewModel.saveLog(ex.id, setNum, reps, weight, note, isCircuit)
                                            setNum = (setNum.toIntOrNull()?.plus(1) ?: 2).toString()
                                            weight = ""
                                            note = ""
                                            if (isCircuit) reps = ""
                                        },
                                        modifier = Modifier.align(Alignment.End),
                                        shape = RoundedCornerShape(12.dp)
                                    ) { Text("Записать") }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
