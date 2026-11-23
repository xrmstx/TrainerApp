package com.example.trainerapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clients")
data class Client(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val goal: String? = null,
    val notes: String? = null
)

@Entity(tableName = "macrocycles")
data class Macrocycle(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val durationWeeks: Int
)

@Entity(tableName = "workouts")
data class Workout(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mesocycleId: Long,
    val dayNumber: Int,
    val name: String
)

@Entity(tableName = "exercise_blocks")
data class ExerciseBlock(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val workoutId: Long,
    val section: String = "MAIN", // WARMUP, MAIN, COOLDOWN
    val blockType: String = "SINGLE" // SINGLE, CIRCUIT
)

@Entity(tableName = "template_exercises")
data class TemplateExercise(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val blockId: Long,
    val exerciseName: String,
    val order: Int,
    val targetSets: Int, // 0 для меткона
    val targetReps: String, // Для меткона тут может быть дистанция/время
    val targetWeight: Double?,
    val note: String? = null,
    val metricType: String = "REPS" // REPS, WEIGHT, TIME, DISTANCE, CALORIES
)

@Entity(tableName = "client_programs")
data class ClientProgram(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientId: Long,
    val macrocycleId: Long,
    val weekNumber: Int,
    val totalWeeks: Int, // Для прогресс-бара
    val status: String = "PLANNED"
)

@Entity(tableName = "client_workouts")
data class ClientWorkout(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientProgramId: Long,
    val originalWorkoutId: Long,
    val dayNumber: Int,
    val name: String,
    val status: String = "PLANNED"
)

@Entity(tableName = "client_exercises")
data class ClientExercise(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientWorkoutId: Long,
    val exerciseName: String,
    val order: Int,
    val section: String = "MAIN",
    val blockType: String = "SINGLE",
    val targetSets: Int,
    val targetReps: String,
    val note: String? = null,
    val metricType: String = "REPS"
)

@Entity(tableName = "exercise_logs")
data class ExerciseLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientExerciseId: Long,
    val setNumber: Int, // Для меткона номер раунда
    val actualReps: Int,
    val actualWeight: Double,
    val resultText: String? = null, // Для меткона (например "Сделано 5 раундов за 12 мин")
    val note: String? = null,
    val date: Long = System.currentTimeMillis()
)
