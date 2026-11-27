package com.example.trainerapp.ui.screens

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.compose.ui.graphics.Color
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trainerapp.data.local.AppDatabase
import com.example.trainerapp.data.model.ClientProgram
import com.example.trainerapp.data.model.ClientWorkout
import com.example.trainerapp.ui.components.EmptyState
import com.example.trainerapp.ui.theme.StatusDone
import com.example.trainerapp.ui.theme.StatusPlanned
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ClientProgramDetailViewModel(application: Application, private val programId: Long) :
    AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).appDao()

    val workouts = dao.getClientWorkoutsForProgram(programId)

    private val _program = MutableStateFlow<ClientProgram?>(null)
    val program: StateFlow<ClientProgram?> = _program.asStateFlow()

    // Выбранная неделя: по умолчанию — текущая неделя программы
    private val _currentWeek = MutableStateFlow<Int?>(null)
    val currentWeek: StateFlow<Int?> = _currentWeek.asStateFlow()

    // Предложение перехода: номер недели, на которую можно перейти; null — баннер скрыт
    private val _weekSuggestion = MutableStateFlow<Int?>(null)
    val weekSuggestion: StateFlow<Int?> = _weekSuggestion.asStateFlow()

    // Неделя, для которой предложение уже отклонили («Позже») — больше не дёргаем
    private var dismissedSuggestionForWeek: Int? = null

    init {
        viewModelScope.launch {
            _program.value = dao.getClientProgramSync(programId)
            // Без текущей недели проверка не сработает — выставляем из программы или первой недели
            if (_currentWeek.value == null) {
                val p = _program.value
                _currentWeek.value = p?.weekNumber?.takeIf { it > 0 }
                    ?: dao.getClientWorkoutsForProgramSync(programId).minOfOrNull { it.weekNumber }
                    ?: 1
            }
            checkSuggestion()
        }
    }

    fun onWeekChange(week: Int) {
        _currentWeek.value = week
        checkSuggestion()
    }

    // Прямая установка статуса: нужна для Undo — вернуть ровно прежнее значение,
    // а не полагаться на текущее состояние flow в момент тапа по «Отменить»
    fun setWorkoutStatus(workoutId: Long, status: String) {
        viewModelScope.launch {
            dao.setClientWorkoutStatus(workoutId, status)
            // После смены статуса неделя могла закрыться целиком — проверяем предложение
            checkSuggestion()
        }
    }

    /**
     * Проверка: все ли тренировки активной недели выполнены и есть ли куда переходить.
     * Считаем по базе, а не по UI-состоянию — так не бывает рассинхрона.
     */
    fun checkSuggestion() {
        viewModelScope.launch {
            val week = _currentWeek.value
            val p = _program.value
            if (week == null || p == null) return@launch
            val next = week + 1
            if (p.totalWeeks <= 0 || next > p.totalWeeks) {
                _weekSuggestion.value = null
                return@launch
            }
            // Для недели, предложение по которой уже отклонили, не спрашиваем повторно
            if (dismissedSuggestionForWeek == week) return@launch
            val count = dao.countWorkoutsInClientWeek(p.id, week)
            if (count == 0) {
                _weekSuggestion.value = null
                return@launch
            }
            val done = dao.countDoneWorkoutsInClientWeek(p.id, week)
            _weekSuggestion.value = if (count == done) next else null
        }
    }

    fun dismissSuggestion() {
        dismissedSuggestionForWeek = _currentWeek.value
        _weekSuggestion.value = null
    }

    fun goToWeek(week: Int) {
        _currentWeek.value = week
        dismissedSuggestionForWeek = null
        checkSuggestion()
    }

    /**
     * Сборка текста одной тренировки. Общая для шаринга дня, недели и всего цикла.
     * Блоки восстанавливаются группировкой по blockOrder — так же, как при копировании шаблона.
     */
    private suspend fun buildWorkoutText(workout: ClientWorkout): String {
        val exercises = dao.getClientExercisesSync(workout.id)
        val isRecovery = workout.dayType == "RECOVERY"
        val header = if (isRecovery) "Восстановление" else "День ${workout.dayNumber}"

        return if (exercises.isEmpty()) {
            buildString {
                appendLine("🏋️ Неделя ${workout.weekNumber} · $header: ${workout.name}")
                appendLine()
                appendLine("(тренировка без упражнений)")
            }
        } else {
            buildString {
                appendLine("🏋️ Неделя ${workout.weekNumber} · $header: ${workout.name}")
                appendLine()
                exercises
                    .groupBy { it.blockOrder }
                    .toSortedMap()
                    .forEach { (_, blockExercises) ->
                        val first = blockExercises.first()
                        appendLine("━━ ${first.blockTitle}")
                        // Бейдж протокола меткона — только у Circuit-блоков
                        if (first.blockType == "CIRCUIT" && first.protocol != "NONE") {
                            val badge = when (first.protocol) {
                                "AMRAP" -> "AMRAP ${first.protocolValue} мин"
                                "EMOM" -> "EMOM ${first.protocolValue} мин"
                                "FOR_TIME" -> "For Time" + (if (first.protocolValue.isNotBlank()) " ${first.protocolValue}" else "")
                                "RFT" -> "RFT ${first.protocolValue}"
                                "TABATA" -> "Tabata ${first.protocolValue}"
                                "CUSTOM" -> if (first.protocolValue.isNotBlank()) first.protocolValue else "Своя схема"
                                else -> null
                            }
                            if (badge != null) appendLine("    $badge")
                        }
                        val sorted = blockExercises.sortedBy { it.order }
                        var i = 0
                        while (i < sorted.size) {
                            val ex = sorted[i]
                            if (ex.supersetGroup != null) {
                                val group = sorted.drop(i).takeWhile { it.supersetGroup == ex.supersetGroup }
                                val distinctSets = group.map { it.targetSets }.distinct()
                                val rounds = if (distinctSets.size == 1 && distinctSets.first() > 0) distinctSets.first().toString() else ""
                                appendLine("🔗 Связка" + (if (rounds.isNotBlank()) " ×$rounds" else "") + ":")
                                group.forEachIndexed { gi, gex ->
                                    val weightText = gex.targetWeight?.let { w ->
                                        val formatted = if (w == w.toLong().toDouble()) w.toLong().toString() else w.toString()
                                        " · $formatted кг"
                                    } ?: ""
                                    val planText = if (rounds.isNotBlank()) gex.targetReps + weightText
                                    else {
                                        val plan = if (gex.targetSets == 0) {
                                            if (gex.targetReps.isBlank()) "" else gex.targetReps
                                        } else "${gex.targetSets}×${gex.targetReps}"
                                        if (plan.isBlank()) weightText.trimStart(' ', '·') else "$plan$weightText"
                                    }
                                    val prefix = if (gi == 0) "• " else "  + "
                                    appendLine("$prefix${gex.exerciseName}" + (if (planText.isBlank()) "" else " — $planText"))
                                    gex.note?.takeIf { it.isNotBlank() }?.let { appendLine("    $it") }
                                }
                                i += group.size
                            } else {
                                val plan = if (ex.targetSets == 0) {
                                    if (ex.targetReps.isBlank()) "" else ex.targetReps
                                } else "${ex.targetSets}×${ex.targetReps}"
                                val weightText = ex.targetWeight?.let { w ->
                                    val formatted = if (w == w.toLong().toDouble()) w.toLong().toString() else w.toString()
                                    " · $formatted кг"
                                } ?: ""
                                val planText = if (plan.isBlank()) weightText.trimStart(' ', '·')
                                else "$plan$weightText"
                                appendLine("• ${ex.exerciseName}" + (if (planText.isBlank()) "" else " — $planText"))
                                ex.note?.takeIf { it.isNotBlank() }?.let { appendLine("  $it") }
                                i++
                            }
                        }
                        appendLine()
                    }
            }
        }
    }

    fun shareWorkout(workout: ClientWorkout, onReady: (String) -> Unit) {
        viewModelScope.launch {
            onReady(buildWorkoutText(workout))
        }
    }

    /** Шаринг всей недели: заголовок недели + все дни по порядку */
    fun shareWeek(weekNumber: Int, onReady: (String) -> Unit) {
        viewModelScope.launch {
            val all = dao.getClientWorkoutsForProgram(programId).first()
            val week = all.filter { it.weekNumber == weekNumber }.sortedBy { it.dayNumber }
            val text = buildString {
                appendLine("🏋️ Неделя $weekNumber")
                week.forEach { w ->
                    appendLine()
                    append(buildWorkoutText(w))
                }
            }.trimEnd()
            onReady(text)
        }
    }

    /** Шаринг всего цикла: все недели с заголовками */
    fun shareProgram(onReady: (String) -> Unit) {
        viewModelScope.launch {
            val all = dao.getClientWorkoutsForProgram(programId).first()
            val maxWeek = all.maxOfOrNull { it.weekNumber } ?: 0
            val programName = program.value?.name ?: ""
            val text = buildString {
                appendLine("🏋️ Программа${if (programName.isNotBlank()) " · $programName" else ""}")
                (1..maxWeek).forEach { week ->
                    appendLine()
                    appendLine("━━ Неделя $week ━")
                    all.filter { it.weekNumber == week }.sortedBy { it.dayNumber }
                        .forEach { w ->
                            appendLine()
                            append(buildWorkoutText(w))
                        }
                }
            }.trimEnd()
            onReady(text)
        }
    }
}

class ClientProgramDetailViewModelFactory(
    private val application: Application,
    private val programId: Long
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ClientProgramDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST") return ClientProgramDetailViewModel(application, programId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

private fun sendText(context: Context, text: String, title: String) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(sendIntent, title))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientProgramDetailScreen(
    programId: Long,
    programName: String,
    onBackClick: () -> Unit,
    onWorkoutClick: (Long, String) -> Unit
) {
    val application = LocalContext.current.applicationContext as Application
    val viewModel: ClientProgramDetailViewModel =
        viewModel(factory = ClientProgramDetailViewModelFactory(application, programId))
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val program by viewModel.program.collectAsState()
    val workouts by viewModel.workouts.collectAsState(initial = emptyList())
    val selectedWeek by viewModel.currentWeek.collectAsState()
    val weekSuggestion by viewModel.weekSuggestion.collectAsState()

    // Статус тренировки могут менять и в другом экране (клиентский лог) —
    // при возврате сюда пересчитываем предложение перехода
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.checkSuggestion()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val totalWeeks = program?.totalWeeks ?: 0
    // Пока программа не загрузилась, неделя не выбрана; после загрузки — текущая неделя программы
    val activeWeek = selectedWeek
        ?: program?.weekNumber
        ?: workouts.minOfOrNull { it.weekNumber }
        ?: 1

    val maxWeeks = maxOf(totalWeeks, workouts.maxOfOrNull { it.weekNumber } ?: 0)

    val weekWorkouts = workouts.filter { it.weekNumber == activeWeek }.sortedBy { it.dayNumber }
    val weekDone = weekWorkouts.count { it.status == "DONE" }
    val weekProgress = if (weekWorkouts.isNotEmpty()) weekDone.toFloat() / weekWorkouts.size else 0f

    val allDone = workouts.count { it.status == "DONE" }
    val allProgress = if (workouts.isNotEmpty()) allDone.toFloat() / workouts.size else 0f

    // Плавный рост прогресс-баров при отметке тренировок и смене недели
    val animatedWeekProgress by animateFloatAsState(
        targetValue = weekProgress,
        animationSpec = tween(500),
        label = "weekProgress"
    )
    val animatedAllProgress by animateFloatAsState(
        targetValue = allProgress,
        animationSpec = tween(500),
        label = "allProgress"
    )

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(programName.ifEmpty { "Программа" }) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    if (workouts.isNotEmpty()) {
                        var shareMenu by remember { mutableStateOf(false) }
                        Box {
                            IconButton(onClick = { shareMenu = true }) {
                                Icon(Icons.Default.Share, contentDescription = "Поделиться планом")
                            }
                            DropdownMenu(
                                expanded = shareMenu,
                                onDismissRequest = { shareMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Весь цикл") },
                                    onClick = {
                                        shareMenu = false
                                        viewModel.shareProgram { text ->
                                            sendText(context, text, "Отправить план")
                                        }
                                    }
                                )
                                (1..maxWeeks).forEach { week ->
                                    DropdownMenuItem(
                                        text = { Text("Неделя $week") },
                                        onClick = {
                                            shareMenu = false
                                            viewModel.shareWeek(week) { text ->
                                                sendText(context, text, "Отправить неделю")
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (totalWeeks > 0) {
                Text(
                    "Неделя $activeWeek из $totalWeeks",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Переключатель недель программы
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    (1..totalWeeks).forEach { week ->
                        val weekDoneCount = workouts.count { it.weekNumber == week && it.status == "DONE" }
                        val weekTotal = workouts.count { it.weekNumber == week }
                        // Пружинка: выбранный чип чуть крупнее, невыбранные — чуть меньше
                        val chipScale by animateFloatAsState(
                            targetValue = if (week == activeWeek) 1f else 0.94f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            ),
                            label = "chipScale$week"
                        )
                        FilterChip(
                            selected = week == activeWeek,
                            onClick = { viewModel.onWeekChange(week) },
                            label = {
                                Text(
                                    when {
                                        weekTotal > 0 && weekDoneCount == weekTotal -> "Нед. $week ✓"
                                        else -> "Нед. $week"
                                    }
                                )
                            },
                            modifier = Modifier.graphicsLayer {
                                scaleX = chipScale
                                scaleY = chipScale
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Баннер: неделя закрыта целиком — предложение перейти дальше
            weekSuggestion?.let { next ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Неделя закрыта целиком",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "Перейти на неделю $next?",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                        TextButton(onClick = { viewModel.dismissSuggestion() }) { Text("Позже") }
                        Button(
                            onClick = { viewModel.goToWeek(next) },
                            shape = RoundedCornerShape(10.dp)
                        ) { Text("Вперёд") }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Прогресс недели
            Row(verticalAlignment = Alignment.CenterVertically) {
                LinearProgressIndicator(
                    progress = animatedWeekProgress,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (weekWorkouts.isEmpty()) "Неделя пуста"
                    else "Неделя: $weekDone из ${weekWorkouts.size}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            // Общий прогресс программы
            Row(verticalAlignment = Alignment.CenterVertically) {
                LinearProgressIndicator(
                    progress = animatedAllProgress,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.tertiary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Всего: $allDone из ${workouts.size}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Пустая программа — вне AnimatedContent: чипов нет, анимировать нечего
            if (workouts.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.EventNote,
                    title = "Программа пуста",
                    subtitle = "Тренировки не назначены. Попросите тренера назначить программу заново."
                )
            } else {
                // Содержимое недели с анимацией перелистывания
                AnimatedContent(
                    targetState = activeWeek,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally(tween(300)) { it / 4 } + fadeIn(tween(300))) togetherWith
                                (slideOutHorizontally(tween(300)) { -it / 4 } + fadeOut(tween(300)))
                        } else {
                            (slideInHorizontally(tween(300)) { -it / 4 } + fadeIn(tween(300))) togetherWith
                                (slideOutHorizontally(tween(300)) { it / 4 } + fadeOut(tween(300)))
                        }
                    },
                    label = "weekContent"
                ) { week ->
                    // Внутри лямбды используем параметр week, а не activeWeek из замыкания
                    val contentWorkouts = workouts.filter { it.weekNumber == week }.sortedBy { it.dayNumber }

                    if (contentWorkouts.isEmpty()) {
                        EmptyState(
                            icon = Icons.Default.HourglassEmpty,
                            title = "Неделя $week пуста",
                            subtitle = "В шаблоне не заполнена эта неделя — проверьте шаблон программы."
                        )
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(contentWorkouts, key = { it.id }) { w ->
                                val isDone = w.status == "DONE"
                                val isRecovery = w.dayType == "RECOVERY"
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    elevation = CardDefaults.cardElevation(2.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = when {
                                            isDone -> StatusDone.copy(alpha = 0.12f)
                                            isRecovery -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.08f)
                                            else -> MaterialTheme.colorScheme.surface
                                        }
                                    ),
                                    onClick = { onWorkoutClick(w.id, w.name) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                w.name,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                if (isRecovery) {
                                                    Icon(
                                                        Icons.Default.SelfImprovement,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.tertiary,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                }
                                                Text(
                                                    if (isRecovery) "Восстановительный день" else "День ${w.dayNumber}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = if (isRecovery) MaterialTheme.colorScheme.tertiary
                                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                                )
                                            }
                                        }
                                        // Отправить тренировку клиенту в мессенджер
                                        IconButton(onClick = {
                                            viewModel.shareWorkout(w) { text ->
                                                sendText(context, text, "Отправить тренировку")
                                            }
                                        }) {
                                            Icon(
                                                Icons.Default.Share,
                                                contentDescription = "Отправить тренировку клиенту"
                                            )
                                        }
                                        IconButton(onClick = {
                                            // Запоминаем прежний статус ДО переключения — снекбар вернёт именно его
                                            val wasDone = w.status == "DONE"
                                            viewModel.setWorkoutStatus(w.id, if (wasDone) "PLANNED" else "DONE")
                                            scope.launch {
                                                val result = snackbarHostState.showSnackbar(
                                                    message = if (wasDone) "Возвращено в план" else "Отмечено выполненной",
                                                    actionLabel = "Отменить",
                                                    duration = SnackbarDuration.Short
                                                )
                                                if (result == SnackbarResult.ActionPerformed) {
                                                    viewModel.setWorkoutStatus(w.id, if (wasDone) "DONE" else "PLANNED")
                                                }
                                            }
                                        }) {
                                            Icon(
                                                if (isDone) Icons.Default.CheckCircle else Icons.Default.Check,
                                                contentDescription = if (isDone) "Вернуть в план" else "Отметить выполненной",
                                                tint = if (isDone) StatusDone else StatusPlanned
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
