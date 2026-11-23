package com.example.trainerapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trainerapp.ui.theme.AccentOrange
import com.example.trainerapp.ui.theme.AccentPink
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
        topBar = { TopAppBar(title = { Text("Мои клиенты", fontWeight = FontWeight.Bold) }) }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = newClientName,
                    onValueChange = { viewModel.onNameChange(it) },
                    label = { Text("Имя нового клиента") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Button(
                    onClick = { viewModel.addClient() },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.height(56.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Добавить")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (clients.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Список пуст. Добавьте первого клиента!", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                }
            } else {
                LazyColumn {
                    items(clients) { client ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                                .clickable { onClientClick(client.id, client.name) },
                            shape = RoundedCornerShape(18.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                // Аватар с градиентом
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(CircleShape)
                                        .background(Brush.linearGradient(listOf(AccentOrange, AccentPink))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = client.name.first().toString().uppercase(),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(text = client.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text(text = "Активный клиент", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
