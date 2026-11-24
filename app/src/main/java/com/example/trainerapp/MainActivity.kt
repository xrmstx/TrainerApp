package com.example.trainerapp

import android.net.Uri
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
import com.example.trainerapp.ui.screens.*
import com.example.trainerapp.ui.theme.TrainerAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TrainerAppTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                Scaffold(
                    bottomBar = {
                        if (currentRoute == "clients" || currentRoute == "templates") {
                            NavigationBar {
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                                    label = { Text("Клиенты") },
                                    selected = currentRoute == "clients",
                                    onClick = {
                                        navController.navigate("clients") {
                                            popUpTo("clients") { inclusive = true }
                                            launchSingleTop = true
                                        }
                                    }
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.List, contentDescription = null) },
                                    label = { Text("Шаблоны") },
                                    selected = currentRoute == "templates",
                                    onClick = {
                                        navController.navigate("templates") {
                                            popUpTo("clients") { inclusive = true }
                                            launchSingleTop = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "clients",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("clients") {
                            ClientScreen(onClientClick = { id, _ -> navController.navigate("client_detail/$id") })
                        }
                        composable("client_detail/{clientId}") { backStackEntry ->
                            val id = backStackEntry.arguments?.getString("clientId")?.toLongOrNull() ?: 0L
                            ClientDetailScreen(
                                clientId = id,
                                onBackClick = { navController.popBackStack() },
                                onProgramClick = { programId, programName ->
                                    navController.navigate("client_program_detail/$programId/${Uri.encode(programName)}")
                                },
                                onHistoryClick = { navController.navigate("client_history/$id") }
                            )
                        }
                        composable("client_program_detail/{programId}/{programName}") { backStackEntry ->
                            val programId = backStackEntry.arguments?.getString("programId")?.toLongOrNull() ?: 0L
                            val programName = backStackEntry.arguments?.getString("programName") ?: ""
                            ClientProgramDetailScreen(
                                programId = programId,
                                onBackClick = { navController.popBackStack() },
                                onWorkoutClick = { workoutId, workoutName ->
                                    navController.navigate("client_workout_log/$workoutId/${Uri.encode(workoutName)}")
                                }
                            )
                        }
                        composable("client_workout_log/{workoutId}/{workoutName}") { backStackEntry ->
                            val workoutId = backStackEntry.arguments?.getString("workoutId")?.toLongOrNull() ?: 0L
                            val workoutName = backStackEntry.arguments?.getString("workoutName") ?: ""
                            ClientWorkoutLogScreen(
                                workoutId = workoutId,
                                workoutName = workoutName,
                                onBackClick = { navController.popBackStack() }
                            )
                        }
                        composable("client_history/{clientId}") { backStackEntry ->
                            val id = backStackEntry.arguments?.getString("clientId")?.toLongOrNull() ?: 0L
                            com.example.trainerapp.ui.screens.ClientHistoryScreen(clientId = id, onBackClick = { navController.popBackStack() })
                        }
                        composable("templates") {
                            TemplateScreen(onMacroClick = { id, name ->
                                navController.navigate("macro_detail/$id/${Uri.encode(name)}")
                            })
                        }
                        composable("macro_detail/{macroId}/{macroName}") { backStackEntry ->
                            val id = backStackEntry.arguments?.getString("macroId")?.toLongOrNull() ?: 0L
                            val name = backStackEntry.arguments?.getString("macroName") ?: ""
                            MacroDetailScreen(
                                macroId = id,
                                macroName = name,
                                onBackClick = { navController.popBackStack() },
                                onWorkoutClick = { workoutId, workoutName ->
                                    navController.navigate("workout_detail/$workoutId/${Uri.encode(workoutName)}")
                                }
                            )
                        }
                        composable("workout_detail/{workoutId}/{workoutName}") { backStackEntry ->
                            val id = backStackEntry.arguments?.getString("workoutId")?.toLongOrNull() ?: 0L
                            val name = backStackEntry.arguments?.getString("workoutName") ?: ""
                            WorkoutDetailScreen(
                                workoutId = id,
                                workoutName = name,
                                onBackClick = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
