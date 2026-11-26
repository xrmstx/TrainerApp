package com.example.trainerapp.ui.screens

import android.app.Application
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trainerapp.data.local.AppDatabase
import com.example.trainerapp.data.model.Client
import com.example.trainerapp.ui.components.EmptyState
import com.example.trainerapp.ui.theme.AvatarGradients
import com.example.trainerapp.ui.theme.StatusDone
import com.example.trainerapp.ui.theme.StatusPlanned
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ClientCardInfo(
    val client: Client,
    val programName: String?,
    val weekNumber: Int?,
    val totalWeeks: Int?,
    val programStatus: String?
)

class ClientsViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).appDao()

    private val _cards = MutableStateFlow<List<ClientCardInfo>>(emptyList())
    val cards: StateFlow<List<ClientCardInfo>> = _cards.asStateFlow()

    init {
        viewModelScope.launch {
            dao.getAllClients().collect { clients ->
                _cards.value = clients.map { c ->
                    val programs = dao.getProgramsForClientSync(c.id)
                    val active = programs.firstOrNull { it.status != "DONE" }
                        ?: programs.maxByOrNull { it.id }
                    ClientCardInfo(c, active?.name, active?.weekNumber, active?.totalWeeks, active?.status)
                }
            }
        }
    }

    fun addClient(name: String, goal: String, notes: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            dao.insertClient(
                Client(
                    name = name.trim(),
                    goal = goal.trim().ifEmpty { null },
                    notes = notes.trim().ifEmpty { null }
                )
            )
        }
    }

    fun deleteClient(clientId: Long) {
        viewModelScope.launch { dao.deleteClient(clientId) }
    }
}

class ClientsViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ClientsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST") return ClientsViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ClientScreen(onClientClick: (Long) -> Unit) {
    val application = LocalContext.current.applicationContext as Application
    val viewModel: ClientsViewModel = viewModel(factory = ClientsViewModelFactory(application))

    val cards by viewModel.cards.collectAsState()
    var search by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var clientToDelete by remember { mutableStateOf<Client?>(null) }

    val filtered = cards.filter {
        it.client.name.contains(search.trim(), ignoreCase = true)
    }

    Scaffold(
    containerColor = Color.Transparent,
    
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Добавить клиента")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                placeholder = { Text("Поиск по имени") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (filtered.isEmpty()) {
                // Разделяем два случая: клиентов нет вообще или поиск ничего не нашёл
                if (cards.isEmpty()) {
                    EmptyState(
                        icon = Icons.Default.Group,
                        title = "Клиентов нет",
                        subtitle = "Добавьте первого клиента — и назначьте ему программу.",
                        actionLabel = "Добавить клиента",
                        onAction = { showAddDialog = true }
                    )
                } else {
                    EmptyState(
                        icon = Icons.Default.Search,
                        title = "Ничего не найдено",
                        subtitle = "Клиента «$search» нет в списке. Попробуйте другое имя."
                    )
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filtered, key = { it.client.id }) { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = { onClientClick(item.client.id) },
                                    onLongClick = { clientToDelete = item.client }
                                ),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(
                                            Brush.linearGradient(
                                                AvatarGradients[
                                                    (item.client.id % AvatarGradients.size).toInt()
                                                ]
                                            ),
                                            shape = RoundedCornerShape(12.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        item.client.name.trim().take(1).uppercase(),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        item.client.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    val line = if (item.programName != null) {
                                        "${item.programName} · Неделя ${item.weekNumber} из ${item.totalWeeks}"
                                    } else "Программа не назначена"
                                    Text(
                                        line,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                                Surface(
                                    modifier = Modifier.size(10.dp),
                                    shape = androidx.compose.foundation.shape.CircleShape,
                                    color = if (item.programStatus == "DONE") StatusDone else StatusPlanned.copy(alpha = 0.35f)
                                ) {}
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var goal by remember { mutableStateOf("") }
        var notes by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Новый клиент") },
            text = {
                Column {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Имя") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = goal,
                        onValueChange = { goal = it },
                        label = { Text("Цель") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Заметки") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.addClient(name, goal, notes)
                    showAddDialog = false
                }) { Text("Добавить") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Отмена") }
            }
        )
    }

    clientToDelete?.let { client ->
        AlertDialog(
            onDismissRequest = { clientToDelete = null },
            title = { Text("Удалить клиента?") },
            text = { Text("«${client.name}» и все его программы и записи будут удалены безвозвратно.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteClient(client.id)
                    clientToDelete = null
                }) { Text("Удалить") }
            },
            dismissButton = {
                TextButton(onClick = { clientToDelete = null }) { Text("Отмена") }
            }
        )
    }
}
