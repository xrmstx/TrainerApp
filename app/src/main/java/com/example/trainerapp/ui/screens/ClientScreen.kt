package com.example.trainerapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trainerapp.ui.viewmodel.ClientViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientScreen(
    onClientClick: (Long, String) -> Unit,
    viewModel: ClientViewModel = viewModel()
) {
    val clients by viewModel.clients.collectAsState(initial = emptyList())
    val newClientName by viewModel.newClientName.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Мои клиенты", style = MaterialTheme.typography.titleLarge) }) }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = newClientName,
                    onValueChange = { viewModel.onNameChange(it) },
                    label = { Text("Имя нового клиента") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Button(
                    onClick = { viewModel.addClient() },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(56.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Добавить")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (clients.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Список пуст. Добавьте первого клиента!", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                }
            } else {
                LazyColumn {
                    items(clients) { client ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                                .clickable { onClientClick(client.id, client.name) },
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(text = client.name, style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}
