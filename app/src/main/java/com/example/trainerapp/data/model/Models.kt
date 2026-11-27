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
    val durationWeeks: Int,
    val sessionsPerWeek: Int = 3 // тренировок в неделю (3–6); восстановительные дни идут поверх
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
    val weekNumber: Int = 1,          // неделя макроцикла, к которой относится день
    val dayNumber: Int,               // день внутри недели
    val dayType: String = "TRAINING", // TRAINING — тренировка, RECOVERY — восстановительный день
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
    val blockTitle: String, // "Разминка", "Скилл", "Силовая", "Меткон", "Заминка", "Кардио"
    val blockType: String = "LINEAR", // LINEAR, CIRCUIT, TEXT
    val order: Int = 0,
    val protocol: String = "NONE",   // NONE, AMRAP, EMOM, FOR_TIME, RFT, TABATA, CUSTOM
    val protocolValue: String = ""   // "10" (минут) для AMRAP, "20/10×8" для TABATA, "21-15-9" для CUSTOM
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
    val metricType: String = "REPS", // REPS, WEIGHT, TIME, DISTANCE, CALORIES
    val supersetGroup: Int? = null,  // null = одиночное; одинаковый номер = связка A1/A2/A3
    // Стабильный ключ истории: переименование не рвёт связь с логами.
    // null = старые записи, история ищется по имени (обратная совместимость).
    val exerciseKey: String? = null
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
    val weekNumber: Int = 1,          // неделя программы клиента
    val dayNumber: Int,
    val dayType: String = "TRAINING", // TRAINING / RECOVERY
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
    val metricType: String = "REPS",
    val protocol: String = "NONE",   // копируется из блока шаблона при назначении программы
    val protocolValue: String = "",
    val supersetGroup: Int? = null,  // связка: копируется из шаблона; null = одиночное
    val exerciseKey: String? = null  // копируется из шаблона при назначении; null = история по имени
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

// Замеры клиента: любые поля опциональны — записываем только то, что замеряли
@Entity(
    tableName = "measurements",
    foreignKeys = [ForeignKey(
        entity = Client::class,
        parentColumns = ["id"],
        childColumns = ["clientId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("clientId")]
)
data class ClientMeasurement(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientId: Long,
    val date: Long = System.currentTimeMillis(),
    val weightKg: Double? = null,   // вес
    val heightCm: Double? = null,   // рост
    val chest: Double? = null,      // грудь
    val waist: Double? = null,      // талия
    val hips: Double? = null,       // бёдра
    val arm: Double? = null,        // рука (бицепс)
    val thigh: Double? = null,      // бедро
    val note: String? = null
)

data class TodayWorkout(
    val workoutId: Long,
    val workoutName: String,
    val weekNumber: Int,
    val dayNumber: Int,
    val status: String,
    val clientName: String,
    val programId: Long
)
