package com.example.trainerapp.ui.screens

import androidx.compose.ui.graphics.Color
import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
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
import com.example.trainerapp.data.model.TodayWorkout
import com.example.trainerapp.ui.components.EmptyState
import com.example.trainerapp.ui.theme.StatusDone
import com.example.trainerapp.ui.theme.StatusPlanned
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).appDao()

    private val _todayWorkouts = MutableStateFlow<List<TodayWorkout>>(emptyList())
    val todayWorkouts: StateFlow<List<TodayWorkout>> = _todayWorkouts.asStateFlow()

    private val _clientsCount = MutableStateFlow(0)
    val clientsCount: StateFlow<Int> = _clientsCount.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _todayWorkouts.value = dao.getTodayWorkoutsSync()
                _clientsCount.value = dao.getClientsCountSync()
            } finally {
                _isLoading.value = false
            }
        }
    }
}

class DashboardViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST") return DashboardViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// Русские формы: 1 тренировка, 2 тренировки, 5 тренировок
private fun plural(n: Int, one: String, few: String, many: String) = when {
    n % 10 == 1 && n % 100 != 11 -> one
    n % 10 in 2..4 && (n % 100 < 10 || n % 100 >= 20) -> few
    else -> many
}

// Локальная обёртка над семантическими цветами из темы
private fun statusColor(status: String) = when (status) {
    "DONE" -> StatusDone
    else -> StatusPlanned
}

// «Сегодня, 25 ноября» — заголовок дашборда
private val todayTitleFmt = SimpleDateFormat("d MMMM", Locale("ru"))
private val todayText = "Сегодня, ${todayTitleFmt.format(Date())}"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(onWorkoutClick: (Long, String) -> Unit, onSettingsClick: () -> Unit = {}) {
    val application = LocalContext.current.applicationContext as Application
    val viewModel: DashboardViewModel = viewModel(factory = DashboardViewModelFactory(application))

    val workouts by viewModel.todayWorkouts.collectAsState()
    val clientsCount by viewModel.clientsCount.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

Scaffold(
    containerColor = Color.Transparent,
    topBar = {
            TopAppBar(
                title = { Text(todayText, fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Настройки")
                    }
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Обновить")
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
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "${workouts.size}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "${plural(workouts.size, "тренировка", "тренировки", "тренировок")} в плане",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "$clientsCount",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            plural(clientsCount, "клиент", "клиента", "клиентов"),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                workouts.isEmpty() -> {
                    EmptyState(
                        icon = Icons.Default.EventNote,
                        title = "Тренировок в плане нет",
                        subtitle = "Назначьте программу клиенту — её незакрытые дни появятся здесь."
                    )
                }
                else -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(workouts, key = { it.workoutId }) { item ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { onWorkoutClick(item.workoutId, item.workoutName) },
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        modifier = Modifier.size(12.dp),
                                        shape = CircleShape,
                                        color = statusColor(item.status)
                                    ) {}
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            item.clientName,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            item.workoutName,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            "Неделя ${item.weekNumber} · День ${item.dayNumber}",
                                            style = MaterialTheme.typography.bodySmall,
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
    }
}
