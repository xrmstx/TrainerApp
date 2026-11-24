package com.example.trainerapp.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
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

// Поле исторически называется mesocycleId, но хранит id макроцикла — не переименовываем, чтобы не ломать существующую базу
@Entity(
    tableName = "workouts",
    foreignKeys = [ForeignKey(
        entity = Macrocycle::class,
        parentColumns = ["id"],
        childColumns = ["mesocycleId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("mesocycleId")]
)
data class Workout(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mesocycleId: Long,
    val dayNumber: Int,
    val name: String
)

@Entity(
    tableName = "exercise_blocks",
    foreignKeys = [ForeignKey(
        entity = Workout::class,
        parentColumns = ["id"],
        childColumns = ["workoutId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("workoutId")]
)
data class ExerciseBlock(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val workoutId: Long,
    val blockTitle: String, // "Разминка", "Скилл", "Силовая", "Меткон"
    val blockType: String = "LINEAR", // LINEAR, CIRCUIT, TEXT
    val order: Int = 0
)

@Entity(
    tableName = "template_exercises",
    foreignKeys = [ForeignKey(
        entity = ExerciseBlock::class,
        parentColumns = ["id"],
        childColumns = ["blockId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("blockId")]
)
data class TemplateExercise(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val blockId: Long,
    val exerciseName: String,
    val order: Int,
    val targetSets: Int, // 0 = CIRCUIT
    val targetReps: String,
    val targetWeight: Double?,
    val note: String? = null,
    val metricType: String = "REPS" // REPS, WEIGHT, TIME, DISTANCE, CALORIES
)

// Имя программы копируем при назначении: если шаблон удалят, программа останется с внятным названием
@Entity(
    tableName = "client_programs",
    foreignKeys = [ForeignKey(
        entity = Client::class,
        parentColumns = ["id"],
        childColumns = ["clientId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("clientId")]
)
data class ClientProgram(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientId: Long,
    val macrocycleId: Long, // без FK: удаление шаблона не должно трогать уже назначенные программы
    val name: String = "",
    val weekNumber: Int,
    val totalWeeks: Int,
    val status: String = "PLANNED"
)

@Entity(
    tableName = "client_workouts",
    foreignKeys = [ForeignKey(
        entity = ClientProgram::class,
        parentColumns = ["id"],
        childColumns = ["clientProgramId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("clientProgramId")]
)
data class ClientWorkout(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientProgramId: Long,
    val originalWorkoutId: Long,
    val dayNumber: Int,
    val name: String,
    val status: String = "PLANNED"
)

@Entity(
    tableName = "client_exercises",
    foreignKeys = [ForeignKey(
        entity = ClientWorkout::class,
        parentColumns = ["id"],
        childColumns = ["clientWorkoutId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("clientWorkoutId")]
)
data class ClientExercise(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientWorkoutId: Long,
    val exerciseName: String,
    val order: Int,
    val blockTitle: String,
    val blockType: String,
    val blockOrder: Int = 0,
    val targetSets: Int,
    val targetReps: String,
    val targetWeight: Double? = null,
    val note: String? = null,
    val metricType: String = "REPS"
)

@Entity(
    tableName = "exercise_logs",
    foreignKeys = [ForeignKey(
        entity = ClientExercise::class,
        parentColumns = ["id"],
        childColumns = ["clientExerciseId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("clientExerciseId")]
)
data class ExerciseLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientExerciseId: Long,
    val setNumber: Int,
    val actualReps: Int,
    val actualWeight: Double,
    val resultText: String? = null, // Для меткона
    val note: String? = null,
    val date: Long = System.currentTimeMillis()
)
