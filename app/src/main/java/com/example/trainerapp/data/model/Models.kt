package com.example.trainerapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

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

@Entity(tableName = "mesocycles", foreignKeys = [ForeignKey(entity = Macrocycle::class, parentColumns = ["id"], childColumns = ["macrocycleId"], onDelete = ForeignKey.CASCADE)], indices = [Index("macrocycleId")])
data class Mesocycle(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val macrocycleId: Long,
    val name: String,
    val weeks: Int
)

@Entity(tableName = "workouts", foreignKeys = [ForeignKey(entity = Mesocycle::class, parentColumns = ["id"], childColumns = ["mesocycleId"], onDelete = ForeignKey.CASCADE)], indices = [Index("mesocycleId")])
data class Workout(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mesocycleId: Long,
    val dayNumber: Int,
    val name: String
)

@Entity(tableName = "exercise_blocks", foreignKeys = [ForeignKey(entity = Workout::class, parentColumns = ["id"], childColumns = ["workoutId"], onDelete = ForeignKey.CASCADE)], indices = [Index("workoutId")])
data class ExerciseBlock(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val workoutId: Long,
    val blockType: String = "SINGLE"
)

@Entity(tableName = "template_exercises", foreignKeys = [ForeignKey(entity = ExerciseBlock::class, parentColumns = ["id"], childColumns = ["blockId"], onDelete = ForeignKey.CASCADE)], indices = [Index("blockId")])
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
    val templateWorkoutId: Long?,
    val weekNumber: Int,
    val status: String = "PLANNED"
)

@Entity(tableName = "exercise_logs", foreignKeys = [ForeignKey(entity = ClientProgram::class, parentColumns = ["id"], childColumns = ["clientProgramId"], onDelete = ForeignKey.CASCADE)], indices = [Index("clientProgramId")])
data class ExerciseLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientProgramId: Long,
    val exerciseName: String,
    val actualSets: Int,
    val actualReps: Int,
    val actualWeight: Double,
    val rpe: Int? = null,
    val comment: String? = null
)
