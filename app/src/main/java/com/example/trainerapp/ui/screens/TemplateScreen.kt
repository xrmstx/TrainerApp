package com.example.trainerapp.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trainerapp.ui.viewmodel.TemplateViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TemplateScreen(onMacroClick: (Long, String) -> Unit, viewModel: TemplateViewModel = viewModel()) {
    val macrocycles by viewModel.macrocycles.collectAsState(initial = emptyList())
    val name by viewModel.newMacroName.collectAsState()
    val weeks by viewModel.newMacroWeeks.collectAsState()

    Scaffold(topBar = { TopAppBar(title = { Text("Шаблоны программ", fontWeight = FontWeight.Bold) }) }) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(value = name, onValueChange = { viewModel.onNameChange(it) }, label = { Text("Название") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(value = weeks, onValueChange = { viewModel.onWeeksChange(it) }, label = { Text("Недель") }, modifier = Modifier.width(90.dp), shape = RoundedCornerShape(12.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = { viewModel.addMacrocycle() }, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(12.dp)) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Создать шаблон")
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (macrocycles.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Нет шаблонов. Создайте первый!", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)) }
            } else {
                LazyColumn {
                    items(macrocycles) { macro ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).combinedClickable(
                                onClick = { onMacroClick(macro.id, macro.name) },
                                onLongClick = { viewModel.deleteMacrocycle(macro.id) }
                            ),
                            shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(macro.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("Длительность: ${macro.durationWeeks} нед.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                            }
                        }
                    }
                }
            }
        }
    }
}
