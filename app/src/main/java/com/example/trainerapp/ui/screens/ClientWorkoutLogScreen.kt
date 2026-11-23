package com.example.trainerapp.ui.screens

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import com.example.trainerapp.data.model.ClientExercise
import com.example.trainerapp.data.model.ExerciseLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ClientWorkoutLogViewModel(application: Application, workoutId: Long) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).appDao()
    val exercises = dao.getClientExercises(workoutId)

    private val _historyDialog = MutableStateFlow<Pair<String, List<ExerciseLog>>?>(null)
    val historyDialog: StateFlow<Pair<String, List<ExerciseLog>>?> = _historyDialog.asStateFlow()

    fun showHistory(exerciseName: String, exerciseId: Long) {
        viewModelScope.launch {
            val logs = mutableListOf<ExerciseLog>()
            dao.getLogsForExercise(exerciseId).collect { 
                logs.clear()
                logs.addAll(it)
                _historyDialog.value = Pair(exerciseName, logs.toList())
            }
        }
    }
    fun closeHistory() { _historyDialog.value = null }

    // Авто-подстановка прошлого веса при открытии экрана
    suspend fun getLastWeight(exerciseId: Long): Double? {
        return dao.getLastLogForExercise(exerciseId)?.actualWeight
    }

    fun saveLog(exerciseId: Long, setNum: String, reps: String, weight: String, note: String) {
        val s = setNum.toIntOrNull() ?: 1
        val r = reps.toIntOrNull() ?: 0
        val w = weight.toDoubleOrNull() ?: 0.0
        if (r > 0) {
            viewModelScope.launch {
                dao.insertExerciseLog(ExerciseLog(clientExerciseId = exerciseId, setNumber = s, actualReps = r, actualWeight = w, note = note))
            }
        }
    }
}

class ClientWorkoutLogViewModelFactory(private val application: Application, private val workoutId: Long) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ClientWorkoutLogViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ClientWorkoutLogViewModel(application, workoutId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
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

    if (historyDialog != null) {
        AlertDialog(
            onDismissRequest = { viewModel.closeHistory() },
            title = { Text("История: ${historyDialog!!.first}") },
            text = {
                if (historyDialog!!.second.isEmpty()) Text("Нет записей.")
                else LazyColumn {
                    items(historyDialog!!.second) { log ->
                        Text("Подход ${log.setNumber}: ${log.actualWeight} кг x ${log.actualReps} (${log.note ?: ""})", modifier = Modifier.padding(vertical = 4.dp))
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
                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, contentDescription = "Назад") } }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
            if (exercises.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("В этой тренировке нет упражнений.", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                }
            } else {
                // Группируем по секциям
                val grouped = exercises.groupBy { it.section }
                
                LazyColumn {
                    grouped.forEach { (section, exerciseList) ->
                        item {
                            Text(
                                text = when(section) { "WARMUP" -> "🔥 Разминка"; "COOLDOWN" -> "❄️ Заминка"; else -> "💪 Основная часть" },
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        items(exerciseList) { ex ->
                            var setNum by remember { mutableStateOf("1") }
                            var reps by remember { mutableStateOf(ex.targetReps) }
                            var weight by remember { mutableStateOf("") }
                            var note by remember { mutableStateOf("") }
                            
                            // Авто-подстановка
                            LaunchedEffect(ex.id) {
                                val lastWeight = viewModel.getLastWeight(ex.id)
                                if (lastWeight != null) weight = lastWeight.toString()
                            }

                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(ex.exerciseName, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                                        TextButton(onClick = { viewModel.showHistory(ex.exerciseName, ex.id) }) {
                                            Text("История")
                                        }
                                    }
                                    Text("План: ${ex.targetSets} x ${ex.targetReps}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                    if (ex.note != null) Text("📝 ${ex.note}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(value = setNum, onValueChange = { setNum = it }, label = { Text("Подх.") }, modifier = Modifier.weight(0.5f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(12.dp))
                                        OutlinedTextField(value = weight, onValueChange = { weight = it }, label = { Text("Вес") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(12.dp))
                                        OutlinedTextField(value = reps, onValueChange = { reps = it }, label = { Text("Повт.") }, modifier = Modifier.weight(0.8f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(12.dp))
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Комментарий") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { 
                                            viewModel.saveLog(ex.id, setNum, reps, weight, note)
                                            setNum = (setNum.toIntOrNull()?.plus(1) ?: 2).toString()
                                            weight = ""
                                            note = ""
                                        },
                                        modifier = Modifier.align(Alignment.End),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Записать подход")
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
