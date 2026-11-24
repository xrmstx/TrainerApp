package com.example.trainerapp.ui.screens

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.trainerapp.ui.theme.StatusDone
import com.example.trainerapp.ui.theme.StatusPlanned
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ClientProgramDetailViewModel(application: Application, private val programId: Long) :
    AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).appDao()

    val workouts = dao.getClientWorkoutsForProgram(programId)

    private val _program = MutableStateFlow<ClientProgram?>(null)
    val program: StateFlow<ClientProgram?> = _program.asStateFlow()

    init {
        viewModelScope.launch { _program.value = dao.getClientProgramSync(programId) }
    }

    fun toggleStatus(workoutId: Long, currentStatus: String) {
        viewModelScope.launch {
            dao.setClientWorkoutStatus(
                workoutId,
                if (currentStatus == "DONE") "PLANNED" else "DONE"
            )
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

    val program by viewModel.program.collectAsState()
    val workouts by viewModel.workouts.collectAsState(initial = emptyList())

    val doneCount = workouts.count { it.status == "DONE" }
    val progress = if (workouts.isNotEmpty()) doneCount.toFloat() / workouts.size else 0f

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
            program?.let { p ->
                Text(
                    "Неделя ${p.weekNumber} из ${p.totalWeeks}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Выполнено: $doneCount из ${workouts.size}",
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
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(workouts, key = { it.id }) { w ->
                        val isDone = w.status == "DONE"
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            elevation = CardDefaults.cardElevation(2.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDone) StatusDone.copy(alpha = 0.12f)
                                else MaterialTheme.colorScheme.surface
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
                                    Text(
                                        "День ${w.dayNumber}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
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
