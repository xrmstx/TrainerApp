package com.example.trainerapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
        topBar = { TopAppBar(title = { Text("Мои клиенты") }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newClientName,
                    onValueChange = { viewModel.onNameChange(it) },
                    label = { Text("Имя нового клиента") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { viewModel.addClient() }) {
                    Text("Добавить")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (clients.isEmpty()) {
                Text("Список пуст. Добавьте первого клиента!", modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(clients) { client ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { onClientClick(client.id, client.name) }
                        ) {
                            Text(
                                text = client.name,
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
    }
}
