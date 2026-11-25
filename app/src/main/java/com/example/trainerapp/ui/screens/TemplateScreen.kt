package com.example.trainerapp.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trainerapp.data.model.Macrocycle
import com.example.trainerapp.ui.viewmodel.TemplateViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TemplateScreen(onMacroClick: (Long, String) -> Unit, viewModel: TemplateViewModel = viewModel()) {
    val macrocycles by viewModel.macrocycles.collectAsState(initial = emptyList())
    val name by viewModel.newMacroName.collectAsState()
    val weeks by viewModel.newMacroWeeks.collectAsState()
    val weeksError by viewModel.weeksError.collectAsState()
    val sessions by viewModel.newMacroSessions.collectAsState()

    var macroToDelete by remember { mutableStateOf<Macrocycle?>(null) }

    if (macroToDelete != null) {
        AlertDialog(
            onDismissRequest = { macroToDelete = null },
            title = { Text("Удалить шаблон?") },
            text = { Text("«${macroToDelete!!.name}» и все его тренировки, блоки и упражнения будут удалены безвозвратно.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteMacrocycle(macroToDelete!!.id)
                    macroToDelete = null
                }) { Text("Удалить", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { macroToDelete = null }) { Text("Отмена") } }
        )
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Шаблоны программ", fontWeight = FontWeight.Bold) }) }) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { viewModel.onNameChange(it) },
                    label = { Text("Название") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = weeks,
                    onValueChange = { viewModel.onWeeksChange(it) },
                    label = { Text("Недель") },
                    modifier = Modifier.width(90.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = weeksError,
                    supportingText = { if (weeksError) Text("1–52") }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("Тренировок в неделю", style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(3, 4, 5, 6).forEach { count ->
                    FilterChip(
                        selected = sessions == count,
                        onClick = { viewModel.onSessionsChange(count) },
                        label = { Text("$count") }
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { viewModel.addMacrocycle() },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Создать шаблон")
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (macrocycles.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Нет шаблонов. Создайте первый!", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                }
            } else {
                LazyColumn {
                    items(macrocycles, key = { it.id }) { macro ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).combinedClickable(
                                onClick = { onMacroClick(macro.id, macro.name) },
                                onLongClick = { macroToDelete = macro }
                            ),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(macro.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(
                                    "Длительность: ${macro.durationWeeks} нед. · Тренировок в неделю: ${macro.sessionsPerWeek}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
