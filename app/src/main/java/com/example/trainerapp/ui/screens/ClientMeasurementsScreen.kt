package com.example.trainerapp.ui.screens

import android.app.Application
import android.content.Intent
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
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
import com.example.trainerapp.data.model.ClientMeasurement
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MeasurementsViewModel(application: Application, private val clientId: Long) :
    AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).appDao()

    val measurements = dao.getMeasurements(clientId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun add(m: ClientMeasurement) = viewModelScope.launch { dao.insertMeasurement(m) }
    fun delete(m: ClientMeasurement) = viewModelScope.launch { dao.deleteMeasurement(m) }

    companion object {
        fun factory(application: Application, clientId: Long) =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return MeasurementsViewModel(application, clientId) as T
                }
            }
    }
}

private val dateFmt = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

private fun fmt(v: Double?): String =
    v?.let { if (it == it.toLong().toDouble()) it.toLong().toString() else it.toString() } ?: "—"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientMeasurementsScreen(
    clientId: Long,
    clientName: String,
    onBackClick: () -> Unit
) {
    val application = LocalContext.current.applicationContext as Application
    val viewModel: MeasurementsViewModel =
        viewModel(factory = MeasurementsViewModel.factory(application, clientId))
    val context = LocalContext.current
    val measurements by viewModel.measurements.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
    containerColor = Color.Transparent,
    
        topBar = {
            TopAppBar(
                title = { Text("Замеры · $clientName") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    if (measurements.isNotEmpty()) {
                        IconButton(onClick = {
                            val csv = buildCsv(measurements, clientName)
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, csv)
                            }
                            context.startActivity(Intent.createChooser(intent, "Отправить замеры"))
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "Экспорт в CSV")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Добавить замер")
            }
        }
    ) { padding ->
        if (measurements.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Замеров пока нет.\nНажмите «+», чтобы записать первый.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(measurements, key = { it.id }) { m ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    dateFmt.format(Date(m.date)),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    "Вес: ${fmt(m.weightKg)} · Грудь: ${fmt(m.chest)} · " +
                                        "Талия: ${fmt(m.waist)} · Бёдра: ${fmt(m.hips)}\n" +
                                        "Рука: ${fmt(m.arm)} · Бедро: ${fmt(m.thigh)} · Рост: ${fmt(m.heightCm)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                                m.note?.takeIf { it.isNotBlank() }?.let {
                                    Text(it, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            IconButton(onClick = { viewModel.delete(m) }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Удалить замер",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        MeasurementDialog(
            clientId = clientId,
            onDismiss = { showDialog = false },
            onSave = {
                viewModel.add(it)
                showDialog = false
            }
        )
    }
}

@Composable
private fun MeasurementDialog(
    clientId: Long,
    onDismiss: () -> Unit,
    onSave: (ClientMeasurement) -> Unit
) {
    var weight by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var chest by remember { mutableStateOf("") }
    var waist by remember { mutableStateOf("") }
    var hips by remember { mutableStateOf("") }
    var arm by remember { mutableStateOf("") }
    var thigh by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    fun parse(s: String): Double? = s.replace(',', '.').trim().toDoubleOrNull()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новый замер") },
        text = {
            Column {
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("Вес, кг") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = height,
                    onValueChange = { height = it },
                    label = { Text("Рост, см") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = chest,
                    onValueChange = { chest = it },
                    label = { Text("Грудь, см") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = waist,
                    onValueChange = { waist = it },
                    label = { Text("Талия, см") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = hips,
                    onValueChange = { hips = it },
                    label = { Text("Бёдра, см") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = arm,
                    onValueChange = { arm = it },
                    label = { Text("Рука, см") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = thigh,
                    onValueChange = { thigh = it },
                    label = { Text("Бедро, см") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Заметка (необязательно)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(
                    ClientMeasurement(
                        clientId = clientId,
                        weightKg = parse(weight),
                        heightCm = parse(height),
                        chest = parse(chest),
                        waist = parse(waist),
                        hips = parse(hips),
                        arm = parse(arm),
                        thigh = parse(thigh),
                        note = note.ifBlank { null }
                    )
                )
            }) { Text("Сохранить") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}

private fun buildCsv(measurements: List<ClientMeasurement>, clientName: String): String {
    val sb = StringBuilder()
    sb.appendLine("Клиент: $clientName")
    sb.appendLine("Дата;Вес,кг;Рост,см;Грудь;Талия;Бёдра;Рука;Бедро;Заметка")
    measurements.forEach { m ->
        sb.appendLine(
            listOf(
                dateFmt.format(Date(m.date)),
                fmt(m.weightKg), fmt(m.heightCm), fmt(m.chest),
                fmt(m.waist), fmt(m.hips), fmt(m.arm), fmt(m.thigh),
                m.note ?: ""
            ).joinToString(";")
        )
    }
    return sb.toString()
}
