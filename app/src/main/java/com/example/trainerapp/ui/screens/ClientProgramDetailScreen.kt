package com.example.trainerapp.ui.screens

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
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
import com.example.trainerapp.ui.theme.StatusDoneContainer
import com.example.trainerapp.ui.theme.StatusOverdue
import com.example.trainerapp.ui.theme.StatusOverdueContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ClientProgramDetailViewModel(application: Application, val programId: Long) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).appDao()

    private val _program = MutableStateFlow<ClientProgram?>(null)
    val program: StateFlow<ClientProgram?> = _program.asStateFlow()

    private val _workouts = MutableStateFlow<List<ClientWorkout>>(emptyList())
    val workouts: StateFlow<List<ClientWorkout>> = _workouts.asStateFlow()

    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog: StateFlow<Boolean> = _showAddDialog.asStateFlow()
    private val _newName = MutableStateFlow("")
    val newName: StateFlow<String> = _newName.asStateFlow()
    private val _newDay = MutableStateFlow("")
    val newDay: StateFlow<String> = _newDay.asStateFlow()
    private val _newWeek = MutableStateFlow("")
    val newWeek: StateFlow<String> = _newWeek.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _program.value = dao.getClientProgramSync(programId)
            _workouts.value = dao.getClientWorkoutsForProgramSync(programId)
                .sortedWith(compareBy({ it.weekNumber }, { it.dayNumber }))
        }
    }

    fun currentWeek(): Int {
        val p = _program.value ?: return 1
        val weekMs = 7L * 24 * 60 * 60 * 1000
        return ((System.currentTimeMillis() - p.startDate) / weekMs).toInt() + 1
    }

    fun toggleStatus(workout: ClientWorkout) {
        viewModelScope.launch {
            val newStatus = if (workout.status == "DONE") "PLANNED" else "DONE"
            dao.setClientWorkoutStatus(workout.id, newStatus)
            load()
        }
    }

    fun openAddDialog() {
        _newName.value = ""
        _newDay.value = ""
        _newWeek.value = ""
        _showAddDialog.value = true
    }
    fun closeAddDialog() { _showAddDialog.value = false }
    fun onNameChange(v: String) { _newName.value = v }
    fun onDayChange(v: String) { _newDay.value = v }
    fun onWeekChange(v: String) { _newWeek.value = v }

    fun addWorkout() {
        val name = _newName.value.trim()
        val day = _newDay.value.trim().toIntOrNull() ?: return
        val week = _newWeek.value.trim().toIntOrNull() ?: 1
        if (name.isEmpty() || day <= 0) return
        viewModelScope.launch {
            dao.insertClientWorkout(
                ClientWorkout(
                    clientProgramId = programId,
                    weekNumber = week,
                    dayNumber = day,
                    name = name,
                    status = "PLANNED"
                )
            )
            _showAddDialog.value = false
            load()
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
    onBackClick: () -> Unit,
    onWorkoutClick: (Long, String) -> Unit
) {
    val application = LocalContext.current.applicationContext as Application
    val viewModel: ClientProgramDetailViewModel =
        viewModel(factory = ClientProgramDetailViewModelFactory(application, programId))

    val program by viewModel.program.collectAsState()
    val workouts by viewModel.workouts.collectAsState()
    val showAddDialog by viewModel.showAddDialog.collectAsState()
    val newName by viewModel.newName.collectAsState()
    val newDay by viewModel.newDay.collectAsState()
    val newWeek by viewModel.newWeek.collectAsState()

    val currentWeek = remember(workouts) { viewModel.currentWeek() }
    val doneCount = workouts.count { it.status == "DONE" }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.closeAddDialog() },
            title = { Text("Новая тренировка") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { viewModel.onNameChange(it) },
                        label = { Text("Название") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newWeek,
                            onValueChange = { viewModel.onWeekChange(it) },
                            label = { Text("Неделя") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = newDay,
                            onValueChange = { viewModel.onDayChange(it) },
                            label = { Text("День") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }
                }
            },
            confirmButton = { TextButton(onClick = { viewModel.addWorkout() }) { Text("Добавить") } },
            dismissButton = { TextButton(onClick = { viewModel.closeAddDialog() }) { Text("Отмена") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(program?.name ?: "Программа", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, contentDescription = "Назад") }
                },
                actions = {
                    IconButton(onClick = { viewModel.openAddDialog() }) {
                        Icon(Icons.Default.Add, contentDescription = "Добавить день")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (program == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                return@Column
            }
            if (workouts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("В программе пока нет тренировок", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Добавьте первый день через «+» сверху",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
                return@Column
            }

            val progress = if (workouts.isNotEmpty()) doneCount.toFloat() / workouts.size else 0f
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Выполнено: $doneCount из ${workouts.size}",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "Неделя $currentWeek из ${program!!.durationWeeks}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(progress = progress, modifier = Modifier.fillMaxWidth())
            }

            val grouped = workouts.groupBy { it.weekNumber }
            LazyColumn(contentPadding = PaddingValues(16.dp)) {
                grouped.forEach { (week, list) ->
                    item(key = "week_$week") {
                        val weekOverdue = week < currentWeek && list.any { it.status != "DONE" }
                        Text(
                            text = if (weekOverdue) "Неделя $week · просрочена" else "Неделя $week",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (weekOverdue) StatusOverdue else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )
                    }
                    items(list, key = { it.id }) { workout ->
                        val overdue = workout.weekNumber < currentWeek && workout.status != "DONE"
                        val container = when {
                            workout.status == "DONE" -> StatusDoneContainer
                            overdue -> StatusOverdueContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                        val accent = when {
                            workout.status == "DONE" -> StatusDone
                            overdue -> StatusOverdue
                            else -> MaterialTheme.colorScheme.primary
                        }
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = container)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = { viewModel.toggleStatus(workout) }) {
                                    Icon(
                                        if (workout.status == "DONE") Icons.Default.CheckCircle else Icons.Default.Check,
                                        contentDescription = if (workout.status == "DONE") "Снять отметку" else "Отметить выполненной",
                                        tint = accent
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "День ${workout.dayNumber}: ${workout.name}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = when {
                                            workout.status == "DONE" -> "Выполнено"
                                            overdue -> "Просрочено"
                                            else -> "Запланировано"
                                        },
                                        style = MaterialTheme.typography.labelMedium,
                                        color = accent
                                    )
                                }
                                TextButton(onClick = { onWorkoutClick(workout.id, workout.name) }) {
                                    Text("Открыть")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
