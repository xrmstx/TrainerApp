package com.example.trainerapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.trainerapp.ui.screens.ClientDetailScreen
import com.example.trainerapp.ui.screens.ClientScreen
import com.example.trainerapp.ui.screens.TemplateScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.Person, contentDescription = null) },
                                label = { Text("Клиенты") },
                                selected = currentRoute == "clients" || currentRoute?.startsWith("client_detail") == true,
                                onClick = {
                                    navController.navigate("clients") {
                                        popUpTo("clients") { inclusive = true }
                                    }
                                }
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.List, contentDescription = null) },
                                label = { Text("Шаблоны") },
                                selected = currentRoute == "templates" || currentRoute?.startsWith("macro_detail") == true,
                                onClick = {
                                    navController.navigate("templates") {
                                        popUpTo("clients") { inclusive = false }
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "clients",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("clients") {
                            ClientScreen(
                                onClientClick = { id, name ->
                                    navController.navigate("client_detail/$id/$name")
                                }
                            )
                        }
                        composable("client_detail/{clientId}/{clientName}") { backStackEntry ->
                            val id = backStackEntry.arguments?.getString("clientId")?.toLongOrNull() ?: 0L
                            val name = backStackEntry.arguments?.getString("clientName") ?: ""
                            ClientDetailScreen(
                                clientId = id,
                                clientName = name,
                                onBackClick = { navController.popBackStack() }
                            )
                        }
                        composable("templates") {
                            TemplateScreen(
                                onMacroClick = { id, name ->
                                    navController.navigate("macro_detail/$id/$name")
                                }
                            )
                        }
                        // Пока просто заглушка для экрана макроцикла (сделаем на следующем шаге)
                        composable("macro_detail/{macroId}/{macroName}") { backStackEntry ->
                            val name = backStackEntry.arguments?.getString("macroName") ?: ""
                            // Временный экран-заглушка
                            Surface(modifier = Modifier.padding(16.dp)) {
                                Text("Экран шаблона: $name (в разработке)", style = MaterialTheme.typography.titleLarge)
                            }
                        }
                    }
                }
            }
        }
    }
}
