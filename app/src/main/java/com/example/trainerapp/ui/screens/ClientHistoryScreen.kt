package com.example.trainerapp.ui.screens

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HistoryLogRow(
    val setNumber: Int,
    val weightText: String,
    val reps: Int,
    val resultText: String?,
    val note: String?,
    val date: Long
)

data class HistoryExerciseGroup(
    val exerciseName: String,
    val blockTitle: String,
    val plan: String,
    val logs: List<HistoryLogRow>
)

data class HistorySession(
    val programId: Long, // нужен для стабильного key в LazyColumn
    val programName: String,
    val dayNumber: Int,
    val workoutName: String,
    val date: Long, // дата последнего подхода в этой сессии
    val exercises: List<HistoryExerciseGroup>,
    val totalSets: Int
)

class ClientHistoryViewModel(application: Application, val clientId: Long) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).appDao()

    private val _sessions = MutableStateFlow<List<HistorySession>>(emptyList())
    val sessions: StateFlow<List<HistorySession>> = _sessions.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _totalSets = MutableStateFlow(0)
    val totalSets: StateFlow<Int> = _totalSets.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _loading.value = true
            val sessions = mutableListOf<HistorySession>()
            var totalSets = 0

            dao.getProgramsForClientSync(clientId).forEach { program ->
                dao.getClientWorkoutsForProgramSync(program.id).forEach { workout ->
                    val groups = mutableListOf<HistoryExerciseGroup>()
                    dao.getClientExercisesSync(workout.id).forEach { ex ->
                        val logs = dao.getLogsForExerciseSync(ex.id)
                        if (logs.isEmpty()) return@forEach
                        val plan = if (ex.targetSets == 0) "Меткон: ${ex.targetReps}"
                        else "${ex.targetSets} × ${ex.targetReps}" +
                            (ex.targetWeight?.let { " · ${formatWeight(it)} кг" } ?: "")
                        val rows = logs.map { log ->
                            HistoryLogRow(
                                setNumber = log.setNumber,
                                weightText = formatWeight(log.actualWeight),
                                reps = log.actualReps,
                                resultText = log.resultText,
                                note = log.note,
                                date = log.date
                            )
                        }
                        groups.add(HistoryExerciseGroup(ex.exerciseName, ex.blockTitle, plan, rows))
                        totalSets += logs.size
                    }
                    if (groups.isNotEmpty()) {
                        val sessionDate = groups.maxOf { g -> g.logs.maxOf { it.date } }
                        sessions.add(
                            HistorySession(
                                programId = program.id,
                                programName = program.name.ifEmpty { "Программа" },
                                dayNumber = workout.dayNumber,
                                workoutName = workout.name,
                                date = sessionDate,
                                exercises = groups,
                                totalSets = groups.sumOf { it.logs.size }
                            )
                        )
                    }
                }
            }

            _sessions.value = sessions.sortedByDescending { it.date }
            _totalSets.value = totalSets
            _loading.value = false
        }
    }
}

class ClientHistoryViewModelFactory(
    private val application: Application,
    private val clientId: Long
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ClientHistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST") return ClientHistoryViewModel(application, clientId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientHistoryScreen(clientId: Long, onBackClick: () -> Unit) {
    val application = LocalContext.current.applicationContext as Application
    val viewModel: ClientHistoryViewModel = viewModel(factory = ClientHistoryViewModelFactory(application, clientId))

    val sessions by viewModel.sessions.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val totalSets by viewModel.totalSets.collectAsState()

    val dateFormat = remember { java.text.SimpleDateFormat("d MMMM yyyy", java.util.Locale("ru")) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("История тренировок", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, contentDescription = "Назад") }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                return@Column
            }

            if (sessions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "Записей пока нет.\nКак только клиент отметит подходы в тренировках, они появятся здесь.",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp)) {
                    item(key = "summary") {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Row(modifier = Modifier.padding(16.dp)) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "${sessions.size}",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        "тренировок с записями",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                Column {
                                    Text(
                                        "$totalSets",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        "подходов записано",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }

                    // Стабильный key: programId + день + дата; дата даёт уникальность,
                    // если клиент проходит одну и ту же программу повторно
                    items(sessions, key = { "${it.programId}-${it.dayNumber}-${it.date}" }) { session ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            shape = RoundedCornerShape(18.dp),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    dateFormat.format(java.util.Date(session.date)),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "День ${session.dayNumber}: ${session.workoutName}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    session.programName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                session.exercises.forEach { group ->
                                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                        Row {
                                            Text(
                                                group.exerciseName,
                                                style = MaterialTheme.typography.bodyLarge,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Text(
                                                group.plan,
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        val fact = group.logs.joinToString(" · ") { row ->
                                            if (row.resultText != null) row.resultText
                                            else "${row.weightText}×${row.reps}"
                                        }
                                        Text(
                                            fact,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                        )
                                        val notes = group.logs.mapNotNull { it.note }.distinct()
                                        if (notes.isNotEmpty()) {
                                            Text(
                                                notes.joinToString(" / "),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
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
