package com.example.trainerapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clients")
data class Client(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val note: String? = null
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
    val mesocycleId: Long, // Ссылка на Macrocycle.id
    val dayNumber: Int,
    val name: String
)

@Entity(tableName = "exercise_blocks")
data class ExerciseBlock(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val workoutId: Long,
    val blockType: String = "SINGLE"
)

@Entity(tableName = "template_exercises")
data class TemplateExercise(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val blockId: Long,
    val exerciseName: String,
    val order: Int,
    val targetSets: Int,
    val targetReps: String,
    val targetWeight: Double?
)

@Entity(tableName = "client_programs")
data class ClientProgram(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientId: Long,
    val macrocycleId: Long,
    val weekNumber: Int,
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
    val targetSets: Int,
    val targetReps: String
)

@Entity(tableName = "exercise_logs")
data class ExerciseLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientExerciseId: Long,
    val actualSets: Int,
    val actualReps: Int,
    val actualWeight: Double,
    val date: Long = System.currentTimeMillis()
)
