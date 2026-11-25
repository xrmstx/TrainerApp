package com.example.trainerapp.ui.screens

import android.app.Application
import android.content.Intent
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
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
import com.example.trainerapp.data.local.AppDatabase
import com.example.trainerapp.data.model.ClientProgram
import com.example.trainerapp.data.model.ClientWorkout
import com.example.trainerapp.ui.theme.StatusDone
import com.example.trainerapp.ui.theme.StatusPlanned
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.HourglassEmpty
import com.example.trainerapp.ui.components.EmptyState

if (workouts.isEmpty()) {
    EmptyState(
        icon = Icons.Default.EventNote,
        title = "Программа пуста",
        subtitle = "Тренировки не назначены. Попросите тренера назначить программу заново."
    )
} else if (weekWorkouts.isEmpty()) {
    EmptyState(
        icon = Icons.Default.HourglassEmpty,
        title = "Неделя $activeWeek пуста",
        subtitle = "В шаблоне не заполнена эта неделя — проверьте шаблон программы."
    )
} else { LazyColumn { /* как было */ } }


class ClientProgramDetailViewModel(application: Application, private val programId: Long) :
    AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).appDao()

    val workouts = dao.getClientWorkoutsForProgram(programId)

    private val _program = MutableStateFlow<ClientProgram?>(null)
    val program: StateFlow<ClientProgram?> = _program.asStateFlow()

    // Выбранная неделя: по умолчанию — текущая неделя программы
    private val _currentWeek = MutableStateFlow<Int?>(null)
    val currentWeek: StateFlow<Int?> = _currentWeek.asStateFlow()

    init {
        viewModelScope.launch {
            _program.value = dao.getClientProgramSync(programId)
        }
    }

    fun onWeekChange(week: Int) { _currentWeek.value = week }

    fun toggleStatus(workoutId: Long, currentStatus: String) {
        viewModelScope.launch {
            dao.setClientWorkoutStatus(
                workoutId,
                if (currentStatus == "DONE") "PLANNED" else "DONE"
            )
        }
    }

    /**
     * Готовит текст одной тренировки для отправки клиенту в мессенджере.
     * Блоки восстанавливаются группировкой по blockOrder — так же, как при копировании шаблона.
     */
    fun shareWorkout(workout: ClientWorkout, onReady: (String) -> Unit) {
        viewModelScope.launch {
            val exercises = dao.getClientExercisesSync(workout.id)
            val isRecovery = workout.dayType == "RECOVERY"
            val header = if (isRecovery) "Восстановление" else "День ${workout.dayNumber}"

            val text = if (exercises.isEmpty()) {
                buildString {
                    appendLine("🏋️ Неделя ${workout.weekNumber} · $header: ${workout.name}")
                    appendLine()
                    appendLine("(тренировка без упражнений)")
                }
            } else {
                buildString {
                    appendLine("🏋️ Неделя ${workout.weekNumber} · $header: ${workout.name}")
                    appendLine()
                    exercises
                        .groupBy { it.blockOrder }
                        .toSortedMap()
                        .forEach { (_, blockExercises) ->
                            val first = blockExercises.first()
                            appendLine("━━ ${first.blockTitle}")
                            // Бейдж протокола меткона — только у Circuit-блоков
                            if (first.blockType == "CIRCUIT" && first.protocol != "NONE") {
                                val badge = when (first.protocol) {
                                    "AMRAP" -> "AMRAP ${first.protocolValue} мин"
                                    "EMOM" -> "EMOM ${first.protocolValue} мин"
                                    "FOR_TIME" -> "For Time" + (if (first.protocolValue.isNotBlank()) " ${first.protocolValue}" else "")
                                    "RFT" -> "RFT ${first.protocolValue}"
                                    "TABATA" -> "Tabata ${first.protocolValue}"
                                    "CUSTOM" -> if (first.protocolValue.isNotBlank()) first.protocolValue else "Своя схема"
                                    else -> null
                                }
                                if (badge != null) appendLine("    $badge")
                            }
                            blockExercises.sortedBy { it.order }.forEach { ex ->
                                val plan = if (ex.targetSets == 0) {
                                    if (ex.targetReps.isBlank()) "" else ex.targetReps
                                } else "${ex.targetSets}×${ex.targetReps}"
                                val weightText = ex.targetWeight?.let { w ->
                                    val formatted = if (w == w.toLong().toDouble()) w.toLong().toString() else w.toString()
                                    " · $formatted кг"
                                } ?: ""
                                val planText = if (plan.isBlank()) weightText.trimStart(' ', '·')
                                else "$plan$weightText"
                                appendLine("• ${ex.exerciseName}" + (if (planText.isBlank()) "" else " — $planText"))
                                ex.note?.takeIf { it.isNotBlank() }?.let { appendLine("  $it") }
                            }
                            appendLine()
                        }
                }
            }
            onReady(text)
        }
    }
}

class ClientProgramDetailViewModelFactory(
    private val application: Application,
    private val programId: Long
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ClientProgramDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST") return ClientProgramDetailViewModel(application, programId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientProgramDetailScreen(
    programId: Long,
    programName: String,
    onBackClick: () -> Unit,
    onWorkoutClick: (Long, String) -> Unit
) {
    val application = LocalContext.current.applicationContext as Application
    val viewModel: ClientProgramDetailViewModel =
        viewModel(factory = ClientProgramDetailViewModelFactory(application, programId))
    val context = LocalContext.current

    val program by viewModel.program.collectAsState()
    val workouts by viewModel.workouts.collectAsState(initial = emptyList())
    val selectedWeek by viewModel.currentWeek.collectAsState()

    val totalWeeks = program?.totalWeeks ?: 0
    // Пока программа не загрузилась, неделя не выбрана; после загрузки — текущая неделя программы
    val activeWeek = selectedWeek
        ?: program?.weekNumber
        ?: workouts.minOfOrNull { it.weekNumber }
        ?: 1

    val weekWorkouts = workouts.filter { it.weekNumber == activeWeek }.sortedBy { it.dayNumber }
    val weekDone = weekWorkouts.count { it.status == "DONE" }
    val weekProgress = if (weekWorkouts.isNotEmpty()) weekDone.toFloat() / weekWorkouts.size else 0f

    val allDone = workouts.count { it.status == "DONE" }
    val allProgress = if (workouts.isNotEmpty()) allDone.toFloat() / workouts.size else 0f

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(programName.ifEmpty { "Программа" }) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Переключатель недель программы
            if (totalWeeks > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    (1..totalWeeks).forEach { week ->
                        val weekDoneCount = workouts.count { it.weekNumber == week && it.status == "DONE" }
                        val weekTotal = workouts.count { it.weekNumber == week }
                        FilterChip(
                            selected = week == activeWeek,
                            onClick = { viewModel.onWeekChange(week) },
                            label = {
                                Text(
                                    when {
                                        weekTotal > 0 && weekDoneCount == weekTotal -> "Нед. $week ✓"
                                        else -> "Нед. $week"
                                    }
                                )
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Прогресс недели
            Row(verticalAlignment = Alignment.CenterVertically) {
                LinearProgressIndicator(
                    progress = weekProgress,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (weekWorkouts.isEmpty()) "Неделя пуста"
                    else "Неделя: $weekDone из ${weekWorkouts.size}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            // Общий прогресс программы
            Row(verticalAlignment = Alignment.CenterVertically) {
                LinearProgressIndicator(
                    progress = allProgress,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.tertiary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Всего: $allDone из ${workouts.size}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            if (workouts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "В программе нет тренировок.",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            } else if (weekWorkouts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "Неделя $activeWeek пуста.\nПроверьте шаблон: в нём не заполнена эта неделя.",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(weekWorkouts, key = { it.id }) { w ->
                        val isDone = w.status == "DONE"
                        val isRecovery = w.dayType == "RECOVERY"
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            elevation = CardDefaults.cardElevation(2.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = when {
                                    isDone -> StatusDone.copy(alpha = 0.12f)
                                    isRecovery -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.08f)
                                    else -> MaterialTheme.colorScheme.surface
                                }
                            ),
                            onClick = { onWorkoutClick(w.id, w.name) }
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        w.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (isRecovery) {
                                            Icon(
                                                Icons.Default.SelfImprovement,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.tertiary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                        }
                                        Text(
                                            if (isRecovery) "Восстановительный день" else "День ${w.dayNumber}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (isRecovery) MaterialTheme.colorScheme.tertiary
                                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                                // Отправить тренировку клиенту в мессенджер
                                IconButton(onClick = {
                                    viewModel.shareWorkout(w) { text ->
                                        val sendIntent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(Intent.EXTRA_TEXT, text)
                                            type = "text/plain"
                                        }
                                        context.startActivity(
                                            Intent.createChooser(sendIntent, "Отправить тренировку")
                                        )
                                    }
                                }) {
                                    Icon(
                                        Icons.Default.Share,
                                        contentDescription = "Отправить тренировку клиенту"
                                    )
                                }
                                IconButton(onClick = { viewModel.toggleStatus(w.id, w.status) }) {
                                    Icon(
                                        if (isDone) Icons.Default.CheckCircle else Icons.Default.Check,
                                        contentDescription = if (isDone) "Вернуть в план" else "Отметить выполненной",
                                        tint = if (isDone) StatusDone else StatusPlanned
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
