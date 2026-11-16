package com.example.trainerapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.trainerapp.ui.screens.ClientDetailScreen
import com.example.trainerapp.ui.screens.ClientScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface {
                    val navController = rememberNavController()
                    
                    NavHost(navController = navController, startDestination = "clients") {
                        // Экран списка клиентов
                        composable("clients") {
                            ClientScreen(
                                onClientClick = { id, name ->
                                    // При клике переходим на экран клиента, передавая id и имя
                                    navController.navigate("client_detail/$id/$name")
                                }
                            )
                        }
                        
                        // Экран конкретного клиента
                        composable("client_detail/{clientId}/{clientName}") { backStackEntry ->
                            val id = backStackEntry.arguments?.getString("clientId")?.toLongOrNull() ?: 0L
                            val name = backStackEntry.arguments?.getString("clientName") ?: "Неизвестно"
                            
                            ClientDetailScreen(
                                clientId = id,
                                clientName = name,
                                onBackClick = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}