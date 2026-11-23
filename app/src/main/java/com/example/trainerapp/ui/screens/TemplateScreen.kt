package com.example.trainerapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trainerapp.ui.viewmodel.TemplateViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateScreen(
    onMacroClick: (Long, String) -> Unit,
    viewModel: TemplateViewModel = viewModel()
) {
    val macrocycles by viewModel.macrocycles.collectAsState(initial = emptyList())
    val name by viewModel.newMacroName.collectAsState()
    val weeks by viewModel.newMacroWeeks.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Шаблоны программ") }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = name, onValueChange = { viewModel.onNameChange(it) },
                    label = { Text("Название") }, modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = weeks, onValueChange = { viewModel.onWeeksChange(it) },
                    label = { Text("Недель") }, modifier = Modifier.width(90.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { viewModel.addMacrocycle() }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Создать шаблон")
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (macrocycles.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Нет шаблонов. Создайте первый!", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn {
                    items(macrocycles) { macro ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                .clickable { onMacroClick(macro.id, macro.name) },
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(macro.name, style = MaterialTheme.typography.titleMedium)
                                Text("Длительность: ${macro.durationWeeks} нед.", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}
