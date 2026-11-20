package com.example.trainerapp.ui.screens

import android.app.Application
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trainerapp.data.local.AppDatabase
import com.example.trainerapp.data.model.Workout
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ClientProgramDetailViewModel(application: Application, clientProgramId: Long) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).appDao()

    // Получаем программу клиента, чтобы узнать ID шаблона
    private val programFlow = dao.getProgramById(clientProgramId) // Нам нужен этот метод в DAO

    // Пока делаем просто: получаем тренировки по templateWorkoutId (ID макроцикла)
    // В реальном приложении тут будет копирование тренировок под клиента, но для MVP сойдет
    val workouts = MutableStateFlow<List<Workout>>(emptyList())

    init {
        viewModelScope.launch {
            // Ждем, пока загрузится программа, и берем её templateWorkoutId
            programFlow.collect { prog ->
                if (prog != null && prog.templateWorkoutId != null) {
                    // Загружаем тренировки этого шаблона
                    dao.getWorkoutsForMacro(prog.templateWorkoutId).collect { list ->
                        workouts.value = list
                    }
                }
            }
        }
    }
}

class ClientProgramDetailViewModelFactory(
    private val application: Application,
    private val clientProgramId: Long
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ClientProgramDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ClientProgramDetailViewModel(application, clientProgramId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientProgramDetailScreen(
    clientProgramId: Long,
    onBackClick: () -> Unit,
    onWorkoutClick: (Long, String) -> Unit
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val viewModel: ClientProgramDetailViewModel = viewModel(
        factory = ClientProgramDetailViewModelFactory(application, clientProgramId)
    )

    val workouts by viewModel.workouts.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Программа клиента") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)
        ) {
            if (workouts.isEmpty()) {
                Text("Нет тренировок в этой программе.", modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                LazyColumn {
                    items(workouts) { workout ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { onWorkoutClick(workout.id, workout.name) }
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("День ${workout.dayNumber}", style = MaterialTheme.typography.labelMedium)
                                Text(workout.name, style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}
