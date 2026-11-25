package com.example.trainerapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.trainerapp.data.local.AppSettings
import com.example.trainerapp.ui.screens.ClientDetailScreen
import com.example.trainerapp.ui.screens.ClientHistoryScreen
import com.example.trainerapp.ui.screens.ClientMeasurementsScreen
import com.example.trainerapp.ui.screens.ClientProgramDetailScreen
import com.example.trainerapp.ui.screens.ClientScreen
import com.example.trainerapp.ui.screens.ClientWorkoutLogScreen
import com.example.trainerapp.ui.screens.DashboardScreen
import com.example.trainerapp.ui.screens.MacroDetailScreen
import com.example.trainerapp.ui.screens.SettingsScreen
import com.example.trainerapp.ui.screens.TemplateScreen
import com.example.trainerapp.ui.screens.WorkoutDetailScreen
import com.example.trainerapp.ui.theme.TrainerAppTheme
import java.net.URLEncoder

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Временный перехватчик крашей: пишет стек в «Загрузки»,
        // чтобы вытащить причину падения без adb. Уберём после отладки.
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            try {
                val text = "Thread: ${t.name}\n\n" + android.util.Log.getStackTraceString(e)
                val values = android.content.ContentValues().apply {
                    put(android.provider.MediaStore.Downloads.DISPLAY_NAME, "crash_trainerapp.txt")
                    put(android.provider.MediaStore.Downloads.MIME_TYPE, "text/plain")
                }
                contentResolver.insert(
                    android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    values
                )?.let { uri ->
                    contentResolver.openOutputStream(uri)?.use { os ->
                        os.write(text.toByteArray())
                    }
                }
            } catch (_: Exception) {}
            android.os.Process.killProcess(android.os.Process.myPid())
        }

        // Загружаем сохранённую тему до setContent, чтобы не мигнуть дефолтной
        AppSettings.init(this)
        setContent {
            val themeMode by AppSettings.theme.collectAsState()
            TrainerAppTheme(themeMode = themeMode) {
                MainScreen()
            }
        }
    }
}

private data class BottomItem(val route: String, val label: String, val icon: ImageVector)

@Composable
fun MainScreen() {
    val navController: NavHostController = rememberNavController()

    val bottomItems = listOf(
        BottomItem("dashboard", "Сегодня", Icons.Default.Home),
        BottomItem("clients", "Клиенты", Icons.Default.Person),
        BottomItem("templates", "Шаблоны", Icons.Default.List)
    )

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = bottomItems.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("dashboard") {
                DashboardScreen(
                    onWorkoutClick = { workoutId, workoutName ->
                        navController.navigate("workout_log/$workoutId/${URLEncoder.encode(workoutName, "UTF-8")}")
                    },
                    onSettingsClick = { navController.navigate("settings") }
                )
            }
            composable("settings") {
                SettingsScreen(onBackClick = { navController.popBackStack() })
            }
            composable("clients") {
                ClientScreen(
                    onClientClick = { clientId ->
                        navController.navigate("client/$clientId")
                    }
                )
            }
            composable("templates") {
                TemplateScreen(
                    onMacroClick = { macroId, macroName ->
                        navController.navigate("macro_detail/$macroId/${URLEncoder.encode(macroName, "UTF-8")}")
                    }
                )
            }
            composable("macro_detail/{macroId}/{macroName}") { entry ->
                val macroId = entry.arguments?.getString("macroId")?.toLongOrNull()
                    ?: return@composable
                val macroName = entry.arguments?.getString("macroName") ?: ""
                MacroDetailScreen(
                    macroId = macroId,
                    macroName = macroName,
                    onBackClick = { navController.popBackStack() },
                    onWorkoutClick = { workoutId, workoutName ->
                        navController.navigate("workout_detail/$workoutId/${URLEncoder.encode(workoutName, "UTF-8")}")
                    }
                )
            }
            composable("client/{clientId}") { entry ->
                val clientId = entry.arguments?.getString("clientId")?.toLongOrNull()
                    ?: return@composable
                ClientDetailScreen(
                    clientId = clientId,
                    onBackClick = { navController.popBackStack() },
                    onProgramClick = { programId, programName ->
                        navController.navigate("client_program/$programId/${URLEncoder.encode(programName, "UTF-8")}")
                    },
                    onHistoryClick = {
                        navController.navigate("history/$clientId")
                    },
                    onMeasurementsClick = { clientId, clientName ->
                        navController.navigate("client_measurements/$clientId/${URLEncoder.encode(clientName, "UTF-8")}")
                    }
                )
            }
            composable("client_program/{programId}/{programName}") { entry ->
                val programId = entry.arguments?.getString("programId")?.toLongOrNull()
                    ?: return@composable
                val programName = entry.arguments?.getString("programName") ?: ""
                ClientProgramDetailScreen(
                    programId = programId,
                    programName = programName,
                    onBackClick = { navController.popBackStack() },
                    onWorkoutClick = { workoutId, workoutName ->
                        navController.navigate("workout_log/$workoutId/${URLEncoder.encode(workoutName, "UTF-8")}")
                    }
                )
            }
            composable("workout_log/{workoutId}/{workoutName}") { entry ->
                val workoutId = entry.arguments?.getString("workoutId")?.toLongOrNull()
                    ?: return@composable
                val workoutName = entry.arguments?.getString("workoutName") ?: ""
                ClientWorkoutLogScreen(
                    workoutId = workoutId,
                    workoutName = workoutName,
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable("workout_detail/{workoutId}/{workoutName}") { entry ->
                val workoutId = entry.arguments?.getString("workoutId")?.toLongOrNull()
                    ?: return@composable
                val workoutName = entry.arguments?.getString("workoutName") ?: ""
                WorkoutDetailScreen(
                    workoutId = workoutId,
                    workoutName = workoutName,
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable("history/{clientId}") { entry ->
                val clientId = entry.arguments?.getString("clientId")?.toLongOrNull()
                    ?: return@composable
                ClientHistoryScreen(
                    clientId = clientId,
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable("client_measurements/{clientId}/{clientName}") { entry ->
                val clientId = entry.arguments?.getString("clientId")?.toLongOrNull()
                    ?: return@composable
                val clientName = entry.arguments?.getString("clientName") ?: ""
                ClientMeasurementsScreen(
                    clientId = clientId,
                    clientName = clientName,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}
