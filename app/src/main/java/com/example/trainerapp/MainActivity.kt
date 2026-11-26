package com.example.trainerapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.trainerapp.R
import com.example.trainerapp.data.local.AppSettings
import com.example.trainerapp.ui.components.BackgroundLayer
import com.example.trainerapp.ui.components.DockItem
import com.example.trainerapp.ui.components.FloatingBottomBar
import com.example.trainerapp.ui.screens.ClientDetailScreen
import com.example.trainerapp.ui.screens.ClientHistoryScreen
import com.example.trainerapp.ui.screens.ClientMeasurementsScreen
import com.example.trainerapp.ui.screens.ClientProgramDetailScreen
import com.example.trainerapp.ui.screens.ClientScreen
import com.example.trainerapp.ui.screens.ClientWorkoutLogScreen
import com.example.trainerapp.ui.screens.DashboardScreen
import com.example.trainerapp.ui.screens.MacroDetailScreen
import com.example.trainerapp.ui.screens.SettingsScreen
import com.example.trainerapp.ui.screens.SplashScreen
import com.example.trainerapp.ui.screens.TemplateScreen
import com.example.trainerapp.ui.screens.WorkoutDetailScreen
import com.example.trainerapp.ui.theme.ThemeMode
import com.example.trainerapp.ui.theme.TrainerAppTheme
import com.example.trainerapp.ui.theme.colorSchemeFor
import java.net.URLEncoder

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Временный перехватчик крашей: пишет стек в «Загрузки».
        // Уберём после отладки.
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
            val scheme = colorSchemeFor(themeMode)
            val view = LocalView.current

            // Статус-бар и навигационная панель — в цвет фона текущей темы
            LaunchedEffect(themeMode) {
                window.statusBarColor = scheme.background.toArgb()
                window.navigationBarColor = scheme.background.toArgb()
            }
            // Светлые иконки — только на светлых темах (isLight объявлен в enum)
            SideEffect {
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = themeMode.isLight
                    isAppearanceLightNavigationBars = themeMode.isLight
                }
            }

            TrainerAppTheme(themeMode = themeMode) {
                MainScreen()
            }
        }
    }
}

private fun NavBackStackEntry.longArg(name: String): Long? =
    arguments?.getString(name)?.toLongOrNull()

private fun encodeArg(text: String): String =
    URLEncoder.encode(text, "UTF-8")

@Composable
private fun InvalidScreen(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(message, color = MaterialTheme.colorScheme.error)
    }
}

@Composable
fun MainScreen() {
    val navController: NavHostController = rememberNavController()

    val bottomItems = listOf(
        DockItem("dashboard", "Сегодня", Icons.Default.Home),
        DockItem("clients", "Клиенты", Icons.Default.Person),
        DockItem("templates", "Шаблоны", Icons.Default.List)
    )

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = bottomItems.any { it.route == currentRoute }

    Scaffold(
        // Прозрачный фон: пусть сквозь него виден BackgroundLayer с картинкой
        containerColor = Color.Transparent,
        bottomBar = {
            // Панель не убираем из дерева условием, а прячем анимацией —
            // обход бага slot table при пересборке подвкладки Scaffold
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(tween(200)) { it } + fadeIn(tween(200)),
                exit = slideOutVertically(tween(200)) { it } + fadeOut(tween(200))
            ) {
                FloatingBottomBar(
                    items = bottomItems,
                    currentRoute = currentRoute,
                    onItemClick = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        // Общий фон на всех экранах: картинка + оверлей 90%
        BackgroundLayer(imageResId = R.drawable.app_background) {
            NavHost(
                navController = navController,
                startDestination = "splash",
                modifier = Modifier.padding(paddingValues),
                // Плавные переходы: вперёд — появление снизу с фейдом,
                // назад — лёгкий уход вниз с фейдом
                enterTransition = {
                    fadeIn(tween(250)) + slideInVertically(tween(250)) { it / 12 }
                },
                exitTransition = {
                    fadeOut(tween(200))
                },
                popEnterTransition = {
                    fadeIn(tween(250)) + slideInVertically(tween(250)) { -it / 12 }
                },
                popExitTransition = {
                    fadeOut(tween(200)) + slideOutVertically(tween(250)) { it / 12 }
                }
            ) {
                composable("splash") {
                    SplashScreen(onReady = {
                        navController.navigate("dashboard") {
                            popUpTo("splash") { inclusive = true }
                        }
                    })
                }
                composable("dashboard") {
                    DashboardScreen(
                        onWorkoutClick = { workoutId, workoutName ->
                            navController.navigate("workout_log/$workoutId/${encodeArg(workoutName)}")
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
                            navController.navigate("macro_detail/$macroId/${encodeArg(macroName)}")
                        }
                    )
                }
                composable("macro_detail/{macroId}/{macroName}") { entry ->
                    val macroId = entry.longArg("macroId")
                    val macroName = entry.arguments?.getString("macroName") ?: ""
                    if (macroId == null) {
                        InvalidScreen("Ошибка навигации")
                    } else {
                        MacroDetailScreen(
                            macroId = macroId,
                            macroName = macroName,
                            onBackClick = { navController.popBackStack() },
                            onWorkoutClick = { workoutId, workoutName ->
                                navController.navigate("workout_detail/$workoutId/${encodeArg(workoutName)}")
                            }
                        )
                    }
                }
                composable("client/{clientId}") { entry ->
                    val clientId = entry.longArg("clientId")
                    if (clientId == null) {
                        InvalidScreen("Ошибка навигации")
                    } else {
                        ClientDetailScreen(
                            clientId = clientId,
                            onBackClick = { navController.popBackStack() },
                            onProgramClick = { programId, programName ->
                                navController.navigate("client_program/$programId/${encodeArg(programName)}")
                            },
                            onHistoryClick = {
                                navController.navigate("history/$clientId")
                            },
                            onMeasurementsClick = { cid, clientName ->
                                navController.navigate("client_measurements/$cid/${encodeArg(clientName)}")
                            }
                        )
                    }
                }
                composable("client_program/{programId}/{programName}") { entry ->
                    val programId = entry.longArg("programId")
                    val programName = entry.arguments?.getString("programName") ?: ""
                    if (programId == null) {
                        InvalidScreen("Ошибка навигации")
                    } else {
                        ClientProgramDetailScreen(
                            programId = programId,
                            programName = programName,
                            onBackClick = { navController.popBackStack() },
                            onWorkoutClick = { workoutId, workoutName ->
                                navController.navigate("workout_log/$workoutId/${encodeArg(workoutName)}")
                            }
                        )
                    }
                }
                composable("workout_log/{workoutId}/{workoutName}") { entry ->
                    val workoutId = entry.longArg("workoutId")
                    val workoutName = entry.arguments?.getString("workoutName") ?: ""
                    if (workoutId == null) {
                        InvalidScreen("Ошибка навигации")
                    } else {
                        ClientWorkoutLogScreen(
                            workoutId = workoutId,
                            workoutName = workoutName,
                            onBackClick = { navController.popBackStack() }
                        )
                    }
                }
                composable("workout_detail/{workoutId}/{workoutName}") { entry ->
                    val workoutId = entry.longArg("workoutId")
                    val workoutName = entry.arguments?.getString("workoutName") ?: ""
                    if (workoutId == null) {
                        InvalidScreen("Ошибка навигации")
                    } else {
                        WorkoutDetailScreen(
                            workoutId = workoutId,
                            workoutName = workoutName,
                            onBackClick = { navController.popBackStack() }
                        )
                    }
                }
                composable("history/{clientId}") { entry ->
                    val clientId = entry.longArg("clientId")
                    if (clientId == null) {
                        InvalidScreen("Ошибка навигации")
                    } else {
                        ClientHistoryScreen(
                            clientId = clientId,
                            onBackClick = { navController.popBackStack() }
                        )
                    }
                }
                composable("client_measurements/{clientId}/{clientName}") { entry ->
                    val clientId = entry.longArg("clientId")
                    val clientName = entry.arguments?.getString("clientName") ?: ""
                    if (clientId == null) {
                        InvalidScreen("Ошибка навигации")
                    } else {
                        ClientMeasurementsScreen(
                            clientId = clientId,
                            clientName = clientName,
                            onBackClick = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
