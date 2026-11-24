package com.example.trainerapp.ui.screens

import android.app.Application
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trainerapp.data.local.AppDatabase
import com.example.trainerapp.data.model.ClientProgram
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ClientProgramDetailViewModel(application: Application, val programId: Long) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).appDao()
    val workouts = dao.getClientWorkoutsForProgram(programId)

    private val _program = MutableStateFlow<ClientProgram?>(null)
    val program: StateFlow<ClientProgram?> = _program.asStateFlow()

    init {
        viewModelScope.launch { _program.value = dao.getClientProgramSync(programId) }
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
fun ClientProgramDetailScreen(programId: Long, onBackClick: () -> Unit, onWorkoutClick: (Long, String) -> Unit) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val viewModel: ClientProgramDetailViewModel = viewModel(factory = ClientProgramDetailViewModelFactory(application, programId))

    val workouts by viewModel.workouts.collectAsState(initial = emptyList())
    val program by viewModel.program.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val p = program
                    Text(
                        text = if (p != null) "${p.name.ifEmpty { "Программа" }} · неделя ${p.weekNumber}/${p.totalWeeks}"
                        else "Программа клиента",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, contentDescription = "Назад") }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
            if (workouts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Нет тренировок в этой программе.", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                }
            } else {
                LazyColumn {
                    items(workouts, key = { it.id }) { workout ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                                .clickable { onWorkoutClick(workout.id, workout.name) },
                            shape = RoundedCornerShape(18.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "День ${workout.dayNumber}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(workout.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                }
                                if (workout.status != "PLANNED") {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = "Выполнено",
                                        tint = MaterialTheme.colorScheme.primary
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
