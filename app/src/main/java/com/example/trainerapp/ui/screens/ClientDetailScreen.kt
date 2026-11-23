package com.example.trainerapp.ui.screens

import android.app.Application
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.trainerapp.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ClientDetailViewModel(
    application: Application,
    val clientId: Long
) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).appDao()

    val programs = dao.getProgramsForClient(clientId)
    val templates = dao.getAllMacrocycles()

    private val _client = MutableStateFlow<Client?>(null)
    val client: StateFlow<Client?> = _client.asStateFlow()

    private val _showDialog = MutableStateFlow(false)
    val showDialog: StateFlow<Boolean> = _showDialog.asStateFlow()

    private val _showEditDialog = MutableStateFlow(false)
    val showEditDialog: StateFlow<Boolean> = _showEditDialog.asStateFlow()

    init {
        viewModelScope.launch {
            dao.getAllClients().collect { list ->
                _client.value = list.find { it.id == clientId }
            }
        }
    }

    fun toggleDialog() {
        _showDialog.value = !_showDialog.value
    }

    fun toggleEditDialog() {
        _showEditDialog.value = !_showEditDialog.value
    }

    fun updateClient(goal: String, notes: String) {
        viewModelScope.launch {
            val current = _client.value ?: return@launch
            dao.insertClient(
                current.copy(
                    goal = goal.ifEmpty { null },
                    notes = notes.ifEmpty { null }
                )
            )
            toggleEditDialog()
        }
    }

    fun deleteClient() {
        viewModelScope.launch {
            dao.deleteClient(clientId)
        }
    }

    fun deleteProgram(programId: Long) {
        viewModelScope.launch {
            dao.deleteClientProgram(programId)
        }
    }

    fun assignProgram(macrocycle: Macrocycle) {
        viewModelScope.launch {
            val programId = dao.insertClientProgram(
                ClientProgram(
                    clientId = clientId,
                    macrocycleId = macrocycle.id,
                    weekNumber = 1,
                    totalWeeks = macrocycle.durationWeeks
                )
            )

            val originalWorkouts = dao.getWorkoutsForMacroSync(macrocycle.id)

            originalWorkouts.forEach { originalWorkout ->
                val newWorkoutId = dao.insertClientWorkout(
                    ClientWorkout(
                        clientProgramId = programId,
                        originalWorkoutId = originalWorkout.id,
                        dayNumber = originalWorkout.dayNumber,
                        name = originalWorkout.name
                    )
                )

                val blocks = dao.getBlocksForWorkoutSync(originalWorkout.id)

                blocks.forEach { block ->
                    val exercises = dao.getExercisesForBlockSync(block.id)

                    exercises.forEach { exercise ->
                        dao.insertClientExercise(
                            ClientExercise(
                                clientWorkoutId = newWorkoutId,
                                exerciseName = exercise.exerciseName,
                                order = exercise.order,
                                section = block.section,
                                blockType = block.blockType,
                                targetSets = exercise.targetSets,
                                targetReps = exercise.targetReps,
                                note = exercise.note,
                                metricType = exercise.metricType
                            )
                        )
                    }
                }
            }

            toggleDialog()
        }
    }
}

class ClientDetailViewModelFactory(
    private val application: Application,
    private val clientId: Long
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ClientDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ClientDetailViewModel(application, clientId) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ClientDetailScreen(
    clientId: Long,
    onBackClick: () -> Unit,
    onProgramClick: (Long) -> Unit,
    onHistoryClick: () -> Unit
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application

    val viewModel: ClientDetailViewModel = viewModel(
        factory = ClientDetailViewModelFactory(application, clientId)
    )

    val client by viewModel.client.collectAsState()
    val programs by viewModel.programs.collectAsState(initial = emptyList())
    val templates by viewModel.templates.collectAsState(initial = emptyList())
    val showDialog by viewModel.showDialog.collectAsState()
    val showEditDialog by viewModel.showEditDialog.collectAsState()

    if (showEditDialog) {
        var goal by remember { mutableStateOf(client?.goal ?: "") }
        var notes by remember { mutableStateOf(client?.notes ?: "") }

        AlertDialog(
            onDismissRequest = { viewModel.toggleEditDialog() },
            title = { Text("Редактировать профиль") },
            text = {
                Column {
                    OutlinedTextField(
                        value = goal,
                        onValueChange = { goal = it },
                        label = { Text("Цель") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Заметки") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.updateClient(goal, notes) }) {
                    Text("Сохранить")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.toggleEditDialog() }) {
                    Text("Отмена")
                }
            }
        )
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.toggleDialog() },
            title = { Text("Выберите шаблон") },
            text = {
                if (templates.isEmpty()) {
                    Text("У вас нет созданных шаблонов.")
                } else {
                    LazyColumn {
                        items(templates) { macro ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .combinedClickable(
                                        onClick = { viewModel.assignProgram(macro) }
                                    ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = macro.name,
                                    modifier = Modifier.padding(16.dp),
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.toggleDialog() }) {
                    Text("Отмена")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = client?.name ?: "Профиль",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleEditDialog() }) {
                        Icon(Icons.Default.Edit, contentDescription = "Редактировать")
                    }

                    IconButton(onClick = { viewModel.deleteClient() }) {
                        Icon(Icons.Default.Delete, contentDescription = "Удалить клиента")
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
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                elevation = CardDefaults.cardElevation(3.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Цель: ${client?.goal ?: "Не задана"}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Заметки: ${client?.notes ?: "Нет заметок"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.toggleDialog() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Назначить программу")
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onHistoryClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.List, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("История тренировок")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Активные программы:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (programs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Нет назначенных программ",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            } else {
                LazyColumn {
                    items(programs) { program ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .combinedClickable(
                                    onClick = { onProgramClick(program.id) },
                                    onLongClick = {
                                        viewModel.deleteProgram(program.id)
                                    }
                                ),
                            shape = RoundedCornerShape(18.dp),
                            elevation = CardDefaults.cardElevation(3.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Программа",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Text(
                                        text = "Неделя ${program.weekNumber} из ${program.totalWeeks}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                LinearProgressIndicator(
                                    progress = if (program.totalWeeks > 0) {
                                        (program.weekNumber.toFloat() / program.totalWeeks)
                                            .coerceIn(0f, 1f)
                                    } else {
                                        0f
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
