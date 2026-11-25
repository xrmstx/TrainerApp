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
import androidx.room.withTransaction
import com.example.trainerapp.data.local.AppDatabase
import com.example.trainerapp.data.model.*
import com.example.trainerapp.ui.components.EmptyState
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

    private val _isAssigning = MutableStateFlow(false)
    val isAssigning: StateFlow<Boolean> = _isAssigning.asStateFlow()

    init {
        viewModelScope.launch {
            _client.value = dao.getClientSync(clientId)
        }
    }

    fun openDialog() { _showDialog.value = true }
    fun closeDialog() { _showDialog.value = false }
    fun toggleEditDialog() { _showEditDialog.value = !_showEditDialog.value }

    fun updateClient(goal: String, notes: String) {
        viewModelScope.launch {
            val current = _client.value ?: return@launch
            val updated = current.copy(
                goal = goal.trim().ifEmpty { null },
                notes = notes.trim().ifEmpty { null }
            )
            dao.insertClient(updated)
            _client.value = updated
            toggleEditDialog()
        }
    }

    // Каскад по внешним ключам чистит программы, тренировки, упражнения и логи
    fun deleteClient() {
        viewModelScope.launch { dao.deleteClient(clientId) }
    }

    // Каскад чистит тренировки, упражнения и логи программы
    fun deleteProgram(programId: Long) {
        viewModelScope.launch { dao.deleteClientProgram(programId) }
    }

    fun assignProgram(macrocycle: Macrocycle) {
        if (_isAssigning.value) return
        _isAssigning.value = true
        _showDialog.value = false
        viewModelScope.launch {
            val db = AppDatabase.getDatabase(getApplication())
            db.withTransaction {
                val programId = dao.insertClientProgram(
                    ClientProgram(
                        clientId = clientId,
                        macrocycleId = macrocycle.id,
                        name = macrocycle.name,
                        weekNumber = 1,
                        totalWeeks = macrocycle.durationWeeks
                    )
                )

                // Копируем все недели шаблона: каждая тренировка получает свой weekNumber и dayType
                dao.getWorkoutsForMacroSync(macrocycle.id).forEach { originalWorkout ->
                    val newWorkoutId = dao.insertClientWorkout(
                        ClientWorkout(
                            clientProgramId = programId,
                            originalWorkoutId = originalWorkout.id,
                            weekNumber = originalWorkout.weekNumber,
                            dayNumber = originalWorkout.dayNumber,
                            dayType = originalWorkout.dayType,
                            name = originalWorkout.name
                        )
                    )

                    dao.getBlocksForWorkoutSync(originalWorkout.id).forEach { block ->
                        dao.getExercisesForBlockSync(block.id).forEach { exercise ->
                            dao.insertClientExercise(
                                ClientExercise(
                                    clientWorkoutId = newWorkoutId,
                                    exerciseName = exercise.exerciseName,
                                    order = exercise.order,
                                    blockTitle = block.blockTitle,
                                    blockType = block.blockType,
                                    blockOrder = block.order,
                                    targetSets = exercise.targetSets,
                                    targetReps = exercise.targetReps,
                                    targetWeight = exercise.targetWeight,
                                    note = exercise.note,
                                    metricType = exercise.metricType,
                                    protocol = block.protocol,
                                    protocolValue = block.protocolValue
                                )
                            )
                        }
                    }
                }
            }
            _isAssigning.value = false
        }
    }
}

class ClientDetailViewModelFactory(
    private val application: Application,
    private val clientId: Long
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ClientDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST") return ClientDetailViewModel(application, clientId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ClientDetailScreen(
    clientId: Long,
    onBackClick: () -> Unit,
    onProgramClick: (Long, String) -> Unit,
    onHistoryClick: () -> Unit,
    onMeasurementsClick: (Long, String) -> Unit
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
    val isAssigning by viewModel.isAssigning.collectAsState()

    var programToDelete by remember { mutableStateOf<ClientProgram?>(null) }
    var confirmDeleteClient by remember { mutableStateOf(false) }

    if (confirmDeleteClient) {
        AlertDialog(
            onDismissRequest = { confirmDeleteClient = false },
            title = { Text("Удалить клиента?") },
            text = {
                Text(
                    "Клиент «${client?.name ?: ""}» и все его программы, тренировки и история будут удалены безвозвратно."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteClient()
                    confirmDeleteClient = false
                    onBackClick()
                }) { Text("Удалить", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDeleteClient = false }) { Text("Отмена") }
            }
        )
    }

    if (programToDelete != null) {
        AlertDialog(
            onDismissRequest = { programToDelete = null },
            title = { Text("Удалить программу?") },
            text = {
                Text(
                    "«${programToDelete!!.name.ifEmpty { "Программа" }}» со всеми тренировками и историей будет удалена безвозвратно."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteProgram(programToDelete!!.id)
                    programToDelete = null
                }) { Text("Удалить", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { programToDelete = null }) { Text("Отмена") }
            }
        )
    }

    // Диалог редактирования: открываем только когда клиент загружен,
    // иначе пустые поля затёрли бы данные при сохранении
    if (showEditDialog) {
        val currentClient = client
        if (currentClient != null) {
            var goal by remember(currentClient.id) { mutableStateOf(currentClient.goal ?: "") }
            var notes by remember(currentClient.id) { mutableStateOf(currentClient.notes ?: "") }

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
                    TextButton(onClick = { viewModel.updateClient(goal, notes) }) { Text("Сохранить") }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.toggleEditDialog() }) { Text("Отмена") }
                }
            )
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.closeDialog() },
            title = { Text("Выберите шаблон") },
            text = {
                if (templates.isEmpty()) {
                    Text("У вас нет созданных шаблонов.")
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 360.dp)) {
                        items(templates, key = { it.id }) { macro ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .combinedClickable(onClick = { viewModel.assignProgram(macro) }),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "${macro.name} · ${macro.durationWeeks} нед. · ${macro.sessionsPerWeek} трен./нед.",
                                    modifier = Modifier.padding(16.dp),
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { viewModel.closeDialog() }) { Text("Отмена") } }
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
                    // Кнопки видимы только после загрузки клиента
                    if (client != null) {
                        IconButton(onClick = { viewModel.toggleEditDialog() }) {
                            Icon(Icons.Default.Edit, contentDescription = "Редактировать")
                        }
                        IconButton(onClick = { confirmDeleteClient = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Удалить клиента")
                        }
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
                onClick = { viewModel.openDialog() },
                enabled = !isAssigning,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isAssigning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Add, contentDescription = null)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isAssigning) "Назначаю..." else "Назначить программу")
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = { onMeasurementsClick(clientId, client?.name ?: "") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.FitnessCenter, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Замеры")
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
                EmptyState(
                    icon = Icons.Default.FolderOpen,
                    title = "Пока нет программ",
                    subtitle = "Назначьте клиенту шаблон — он появится здесь с недельным планом.",
                    actionLabel = "Назначить программу",
                    onAction = { viewModel.openDialog() }
                )
            } else {
                LazyColumn {
                    items(programs, key = { it.id }) { program ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .combinedClickable(
                                    onClick = { onProgramClick(program.id, program.name) },
                                    onLongClick = { programToDelete = program }
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
                                        text = program.name.ifEmpty { "Программа" },
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
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
                                        (program.weekNumber.toFloat() / program.totalWeeks).coerceIn(0f, 1f)
                                    } else 0f,
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
