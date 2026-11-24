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
            dao.insertClient(
                current.copy(
                    goal = goal.trim().ifEmpty { null },
                    notes = notes.trim().ifEmpty { null }
                )
            )
            _client.value = current.copy(goal = goal.trim().ifEmpty { null }, notes = notes.trim().ifEmpty { null })
            toggleEditDialog()
        }
    }

    fun deleteClient() {
        viewModelScope.launch { dao.deleteClient(clientId) } // каскад чистит программы, тренировки, упражнения и логи
    }

    fun deleteProgram(programId: Long) {
        viewModelScope.launch { dao.deleteClientProgram(programId) } // каскад чистит тренировки, упражнения и логи
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

                dao.getWorkoutsForMacroSync(macrocycle.id).forEach { originalWorkout ->
                    val newWorkoutId = dao.insertClientWorkout(
                        ClientWorkout(
                            clientProgramId = programId,
                            originalWorkoutId = originalWorkout.id,
                            dayNumber = originalWorkout.dayNumber,
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
                                    metricType = exercise.metricType
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
    onHistoryClick: () -> Unit
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application

    val viewModel: ClientDetailViewModel = viewModel(factory = ClientDetailViewModelFactory(application, clientId))

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
            text = { Text("Клиент «${client?.name ?: ""}» и все его программы, тренировки и история будут удалены безвозвратно.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteClient()
                    confirmDeleteClient = false
                    onBackClick()
                }) { Text("Удалить", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { confirmDeleteClient = false }) { Text("Отмена") } }
        )
    }

    if (programToDelete != null) {
        AlertDialog(
            onDismissRequest = { programToDelete = null },
            title = { Text("Удалить программу?") },
            text = { Text("«${programToDelete!!.name}» со всеми тренировками и историей будет удалена безвозвратно.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteProgram(programToDelete!!.id)
                    programToDelete = null
                }) { Text("Удалить", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { programToDelete = null }) { Text("Отмена") } }
        )
    }

    if (showEditDialog && client != null) {
        var goal by remember(client.id) { mutableStateOf(client.goal ?: "") }
        var notes by remember(client.id) { mutableStateOf(client.notes ?: "") }

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
                        shape = RoundedCornerShape(
