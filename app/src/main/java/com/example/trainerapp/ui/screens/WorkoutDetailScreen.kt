package com.example.trainerapp.ui.screens

import android.app.Application
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trainerapp.data.local.AppDatabase
import com.example.trainerapp.data.model.ExerciseBlock
import com.example.trainerapp.data.model.TemplateExercise
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WorkoutDetailViewModel(application: Application, val workoutId: Long) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).appDao()
    private val _selectedSection = MutableStateFlow("MAIN")
    val selectedSection: StateFlow<String> = _selectedSection.asStateFlow()
    private val _exercises = MutableStateFlow<List<TemplateExercise>>(emptyList())
    val exercises: StateFlow<List<TemplateExercise>> = _exercises.asStateFlow()

    init { loadExercises() }

    fun onSectionChange(section: String) { _selectedSection.value = section; loadExercises() }
    
    private fun loadExercises() {
        viewModelScope.launch {
            val blocks = dao.getBlocksForWorkoutSync(workoutId)
            val block = blocks.find { it.section == _selectedSection.value }
            if (block != null) dao.getExercisesForBlock(block.id).collect { list -> _exercises.value = list } else _exercises.value = emptyList()
        }
    }

    private val _showDialog = MutableStateFlow(false)
    val showDialog: StateFlow<Boolean> = _showDialog.asStateFlow()
    private val _editExercise = MutableStateFlow<TemplateExercise?>(null)
    val editExercise: StateFlow<TemplateExercise?> = _editExercise.asStateFlow()

    private val _exName = MutableStateFlow("")
    private val _exSets = MutableStateFlow("")
    private val _exReps = MutableStateFlow("")
    private val _exNote = MutableStateFlow("")
    private val _exMetric = MutableStateFlow("REPS")
    val exName: StateFlow<String> = _exName.asStateFlow()
    val exSets: StateFlow<String> = _exSets.asStateFlow()
    val exReps: StateFlow<String> = _exReps.asStateFlow()
    val exNote: StateFlow<String> = _exNote.asStateFlow()
    val exMetric: StateFlow<String> = _exMetric.asStateFlow()

    fun onNameChange(name: String) { _exName.value = name }
    fun onSetsChange(sets: String) { _exSets.value = sets }
    fun onRepsChange(reps: String) { _exReps.value = reps }
    fun onNoteChange(note: String) { _exNote.value = note }
    fun onMetricChange(metric: String) { _exMetric.value = metric }
    
    fun openDialog(ex: TemplateExercise? = null) {
        _editExercise.value = ex
        _showDialog.value = true
        if (ex != null) {
            _exName.value = ex.exerciseName; _exSets.value = ex.targetSets.toString(); _exReps.value = ex.targetReps; _exNote.value = ex.note ?: ""; _exMetric.value = ex.metricType
        } else {
            _exName.value = ""; _exSets.value = ""; _exReps.value = ""; _exNote.value = ""; _exMetric.value = "REPS"
        }
    }

    fun addOrEditExercise() {
        val name = _exName.value.trim()
        val sets = _exSets.value.trim().toIntOrNull() ?: 0
        val reps = _exReps.value.trim()
        val note = _exNote.value.trim()
        val metric = _exMetric.value
        val section = _selectedSection.value
        val existing = _editExercise.value

        if (name.isNotEmpty() && reps.isNotEmpty()) {
            viewModelScope.launch {
                if (existing != null) {
                    dao.insertTemplateExercise(existing.copy(exerciseName = name, targetSets = sets, targetReps = reps, note = note.ifEmpty { null }, metricType = metric))
                } else {
                    var blockId: Long? = null
                    val existingBlocks = dao.getBlocksForWorkoutSync(workoutId)
                    val existingBlock = existingBlocks.find { it.section == section }
                    blockId = if (existingBlock != null) existingBlock.id else dao.insertExerciseBlock(ExerciseBlock(workoutId = workoutId, section = section, blockType = "CIRCUIT"))
                    if (blockId != null) dao.insertTemplateExercise(TemplateExercise(blockId = blockId, exerciseName = name, order = 100, targetSets = sets, targetReps = reps, targetWeight = null, note = note.ifEmpty { null }, metricType = metric))
                }
                _showDialog.value = false; loadExercises()
            }
        }
    }

    fun deleteExercise(ex: TemplateExercise) { viewModelScope.launch { dao.deleteTemplateExercise(ex.id); loadExercises() } }
}

class WorkoutDetailViewModelFactory(private val application: Application, private val workoutId: Long) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WorkoutDetailViewModel::class.java)) { @Suppress("UNCHECKED_CAST") return WorkoutDetailViewModel(application, workoutId) as T }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun WorkoutDetailScreen(workoutId: Long, workoutName: String, onBackClick: () -> Unit) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val viewModel: WorkoutDetailViewModel = viewModel(factory = WorkoutDetailViewModelFactory(application, workoutId))
    val exercises by viewModel.exercises.collectAsState()
    val selectedSection by viewModel.selectedSection.collectAsState()
    val showDialog by viewModel.showDialog.collectAsState()
    val exName by viewModel.exName.collectAsState()
    val exSets by viewModel.exSets.collectAsState()
    val exReps by viewModel.exReps.collectAsState()
    val exNote by viewModel.exNote.collectAsState()
    val exMetric by viewModel.exMetric.collectAsState()

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.openDialog(null) },
            title = { Text(if (viewModel.editExercise.value == null) "Добавить" else "Редактировать") },
            text = {
                Column {
                    OutlinedTextField(value = exName, onValueChange = { viewModel.onNameChange(it) }, label = { Text("Название") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = exSets, onValueChange = { viewModel.onSetsChange(it) }, label = { Text("Подходы") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
                        OutlinedTextField(value = exReps, onValueChange = { viewModel.onRepsChange(it) }, label = { Text("Повторы") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = exNote, onValueChange = { viewModel.onNoteChange(it) }, label = { Text("Заметка") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                   
