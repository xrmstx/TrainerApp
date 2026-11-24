package com.example.trainerapp.ui.screens

import android.app.Application
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trainerapp.data.local.AppDatabase
import com.example.trainerapp.data.model.Client
import com.example.trainerapp.data.model.ClientProgram
import com.example.trainerapp.ui.theme.AvatarGradients
import com.example.trainerapp.ui.theme.StatusDone
import com.example.trainerapp.ui.theme.StatusOverdue
import com.example.trainerapp.ui.theme.StatusPlanned
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ClientListItem(
    val client: Client,
    val programName: String?,
    val weekNumber: Int?,
    val totalWeeks: Int?,
    val status: String // DONE / OVERDUE / PLANNED / NONE
)

class ClientsViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).appDao()

    private val _clients = MutableStateFlow<List<ClientListItem>>(emptyList())
    val clients: StateFlow<List<ClientListItem>> = _clients.asStateFlow()

    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog: StateFlow<Boolean> = _showAddDialog.asStateFlow()
    private val _newName = MutableStateFlow("")
    val newName: StateFlow<String> = _newName.asStateFlow()
    private val _newPhone = MutableStateFlow("")
    val newPhone: StateFlow<String> = _newPhone.asStateFlow()

    private val _clientToDelete = MutableStateFlow<Client?>(null)
    val clientToDelete: StateFlow<Client?> = _clientToDelete.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            val items = mutableListOf<ClientListItem>()
            dao.getAllClientsSync().forEach { client ->
                val programs = dao.getProgramsForClientSync(client.id)
                val active = programs.firstOrNull { it.status == "ACTIVE" } ?: programs.lastOrNull()
                if (active == null) {
                    items.add(ClientListItem(client, null, null, null, "NONE"))
                } else {
                    val workouts = dao.getClientWorkoutsForProgramSync(active.id)
                    val week = currentWeekOf(active)
                    val overdue = workouts.any { it.weekNumber < week && it.status != "DONE" }
                    val weekWorkouts = workouts.filter { it.weekNumber == week }
                    val status = when {
                        overdue || week > active.durationWeeks -> "OVERDUE"
                        weekWorkouts.isNotEmpty() && weekWorkouts.all { it.status == "DONE" } -> "DONE"
                        else -> "PLANNED"
                    }
                    items.add(ClientListItem(client, active.name, week, active.durationWeeks, status))
                }
            }
            _clients.value = items
        }
    }

    private fun currentWeekOf(program: ClientProgram): Int {
        val weekMs = 7L * 24 * 60 * 60 * 1000
        return ((System.currentTimeMillis() - program.startDate) / weekMs).toInt() + 1
    }

    fun openAddDialog() {
        _newName.value = ""
        _newPhone.value = ""
        _showAddDialog.value = true
    }
    fun closeAddDialog() { _showAddDialog.value = false }
    fun onNameChange(v: String) { _newName.value = v }
    fun onPhoneChange(v: String) { _newPhone.value = v }

    fun addClient() {
        val name = _newName.value.trim()
        if (name.isEmpty()) return
        viewModelScope.launch {
            dao.insertClient(Client(name = name, phone = _newPhone.value.trim().ifEmpty { null }))
            _showAddDialog.value = false
            load()
        }
    }

    fun askDelete(client: Client) { _clientToDelete.value = client }
    fun cancelDelete() { _clientToDelete.value = null }

    fun deleteClient() {
        val target = _clientToDelete.value ?: return
        viewModelScope.launch {
            dao.deleteClient(target)
            _clientToDelete.value = null
            load()
        }
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

fun statusColorFor(status: String): Color = when (status) {
    "DONE" -> StatusDone
    "OVERDUE" -> StatusOverdue
    "PLANNED" -> StatusPlanned
    else -> StatusPlanned.copy(alpha = 0.35f)
}

fun statusLabelFor(status: String): String = when (status) {
    "DONE" -> "Неделя выполнена"
    "OVERDUE" -> "Просрочено"
    "PLANNED" -> "В работе"
    else -> "Нет программы"
}

fun initialsOf(name: String): String {
    val parts = name.trim().split(" ").filter { it.isNotBlank() }
    return when {
        parts.isEmpty() -> "?"
        parts.size == 1 -> parts[0].take(1).uppercase()
        else -> (parts[0].take(1) + parts[1].take(1)).uppercase()
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ClientScreen(onClientClick: (Long) -> Unit) {
    val application = LocalContext.current.applicationContext as Application
    val viewModel: ClientsViewModel = viewModel(factory = ClientsViewModelFactory(application))

    val clients by viewModel.clients.collectAsState()
    val showAddDialog by viewModel.showAddDialog.collectAsState()
    val newName by viewModel.newName.collectAsState()
    val newPhone by viewModel.newPhone.collectAsState()
    val clientToDelete by viewModel.clientToDelete.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    val filtered = remember(clients, searchQuery) {
        val q = searchQuery.trim()
        if (q.isEmpty()) clients
        else clients.filter { it.client.name.contains(q, ignoreCase = true) }
    }

    clientToDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { viewModel.cancelDelete() },
            title = { Text("Удалить клиента?") },
            text = { Text("«${target.name}» и все его программы с записями будут удалены безвозвратно.") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteClient() }) {
                    Text("Удалить", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { viewModel.cancelDelete() }) { Text("Отмена") } }
        )
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.closeAddDialog() },
            title = { Text("Новый клиент") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { viewModel.onNameChange(it) },
                        label = { Text("Имя") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newPhone,
                        onValueChange = { viewModel.onPhoneChange(it) },
                        label = { Text("Телефон (необязательно)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }
            },
            confirmButton = { TextButton(onClick = { viewModel.addClient() }) { Text("Добавить") } },
            dismissButton = { TextButton(onClick = { viewModel.closeAddDialog() }) { Text("Отмена") } }
        )
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Клиенты", fontWeight = FontWeight.Bold) }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.openAddDialog() }) {
                Icon(Icons.Default.Add, contentDescription = "Добавить клиента")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (clients.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Здесь появятся ваши клиенты", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Добавьте первого — и начнём тренировки",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.openAddDialog() }) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Text("Добавить клиента")
                        }
                    }
                }
            } else {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Поиск по имени") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )
                LazyColumn(contentPadding = PaddingValues(16.dp)) {
                    items(filtered, key = { it.client.id }) { item ->
                        ClientCard(
                            item = item,
                            onClick = { onClientClick(item.client.id) },
                            onLongClick = { viewModel.askDelete(item.client) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ClientCard(item: ClientListItem, onClick: () -> Unit, onLongClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            val gradient = AvatarGradients[(item.client.id % AvatarGradients.size).toInt()]
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Brush.linearGradient(gradient), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    initialsOf(item.client.name),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.client.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (item.programName != null) {
                        "${item.programName} · неделя ${item.weekNumber} из ${item.totalWeeks}"
                    } else {
                        "Нет активной программы"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(statusColorFor(item.status), CircleShape)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    statusLabelFor(item.status),
                    style = MaterialTheme.typography.labelSmall,
                    color = statusColorFor(item.status)
                )
            }
        }
    }
}
