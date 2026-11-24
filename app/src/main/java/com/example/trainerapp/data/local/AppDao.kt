package com.example.trainerapp.data.local

import androidx.room.*
import com.example.trainerapp.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // --- Клиенты ---
    @Query("SELECT * FROM clients ORDER BY name ASC")
    fun getAllClients(): Flow<List<Client>>

    @Query("SELECT * FROM clients WHERE id = :clientId")
    suspend fun getClientSync(clientId: Long): Client?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClient(client: Client): Long

    // Сироты чистятся каскадом по внешним ключам
    @Query("DELETE FROM clients WHERE id = :clientId")
    suspend fun deleteClient(clientId: Long)

    // --- Шаблоны (макроциклы) ---
    @Query("SELECT * FROM macrocycles")
    fun getAllMacrocycles(): Flow<List<Macrocycle>>

    @Insert
    suspend fun insertMacrocycle(macrocycle: Macrocycle): Long

    // Каскад удалит workouts -> blocks -> exercises
    @Query("DELETE FROM macrocycles WHERE id = :macroId")
    suspend fun deleteMacrocycle(macroId: Long)

    // --- Тренировки шаблона ---
    @Query("SELECT * FROM workouts WHERE mesocycleId = :macroId ORDER BY dayNumber ASC")
    fun getWorkoutsForMacro(macroId: Long): Flow<List<Workout>>

    @Query("SELECT * FROM workouts WHERE mesocycleId = :macroId ORDER BY dayNumber ASC")
    suspend fun getWorkoutsForMacroSync(macroId: Long): List<Workout>

    @Insert
    suspend fun insertWorkout(workout: Workout): Long

    // Каскад удалит блоки и упражнения этой тренировки
    @Query("DELETE FROM workouts WHERE id = :workoutId")
    suspend fun deleteWorkout(workoutId: Long)

    @Query("SELECT MAX(dayNumber) FROM workouts WHERE mesocycleId = :macroId")
    suspend fun getMaxDayNumber(macroId: Long): Int?

    // --- Блоки шаблона ---
    @Query("SELECT * FROM exercise_blocks WHERE workoutId = :workoutId ORDER BY `order` ASC, id ASC")
    suspend fun getBlocksForWorkoutSync(workoutId: Long): List<ExerciseBlock>

    @Query("SELECT MAX(`order`) FROM exercise_blocks WHERE workoutId = :workoutId")
    suspend fun getMaxBlockOrder(workoutId: Long): Int?

    @Insert
    suspend fun insertExerciseBlock(block: ExerciseBlock): Long

    // Каскад удалит упражнения блока
    @Query("DELETE FROM exercise_blocks WHERE id = :blockId")
    suspend fun deleteExerciseBlock(blockId: Long)

    // --- Упражнения шаблона ---
    @Query("SELECT * FROM template_exercises WHERE blockId = :blockId ORDER BY `order` ASC, id ASC")
    fun getExercisesForBlock(blockId: Long): Flow<List<TemplateExercise>>

    @Query("SELECT * FROM template_exercises WHERE blockId = :blockId ORDER BY `order` ASC, id ASC")
    suspend fun getExercisesForBlockSync(blockId: Long): List<TemplateExercise>

    @Insert
    suspend fun insertTemplateExercise(exercise: TemplateExercise): Long

    @Update
    suspend fun updateTemplateExercise(exercise: TemplateExercise)

    @Query("DELETE FROM template_exercises WHERE id = :exerciseId")
    suspend fun deleteTemplateExercise(exerciseId: Long)

    @Query("SELECT MAX(`order`) FROM template_exercises WHERE blockId = :blockId")
    suspend fun getMaxExerciseOrder(blockId: Long): Int?

    @Query("SELECT * FROM template_exercises GROUP BY exerciseName ORDER BY id DESC")
    suspend fun getUniqueExercises(): List<TemplateExercise>

    // --- Программы клиента ---
    @Query("SELECT * FROM client_programs WHERE clientId = :clientId ORDER BY weekNumber ASC")
    fun getProgramsForClient(clientId: Long): Flow<List<ClientProgram>>

    @Query("SELECT * FROM client_programs WHERE id = :programId")
    suspend fun getClientProgramSync(programId: Long): ClientProgram?

    // Каскад удалит тренировки, упражнения и логи программы
    @Query("DELETE FROM client_programs WHERE id = :programId")
    suspend fun deleteClientProgram(programId: Long)

    @Insert
    suspend fun insertClientProgram(program: ClientProgram): Long

    // --- Тренировки клиента ---
    @Query("SELECT * FROM client_workouts WHERE clientProgramId = :programId ORDER BY dayNumber ASC")
    fun getClientWorkoutsForProgram(programId: Long): Flow<List<ClientWorkout>>

    @Insert
    suspend fun insertClientWorkout(workout: ClientWorkout): Long

    // --- Упражнения клиента ---
    @Query("SELECT * FROM client_exercises WHERE clientWorkoutId = :workoutId ORDER BY blockOrder ASC, `order` ASC, id ASC")
    fun getClientExercises(workoutId: Long): Flow<List<ClientExercise>>

    @Insert
    suspend fun insertClientExercise(exercise: ClientExercise): Long

    // --- Логи ---
    @Query("SELECT * FROM exercise_logs WHERE clientExerciseId = :clientExerciseId ORDER BY date DESC, setNumber ASC LIMIT 1")
    suspend fun getLastLogForExercise(clientExerciseId: Long): ExerciseLog?

    @Query("SELECT * FROM exercise_logs WHERE clientExerciseId = :clientExerciseId ORDER BY setNumber ASC")
    suspend fun getLogsForExerciseSync(clientExerciseId: Long): List<ExerciseLog>

    @Insert
    suspend fun insertExerciseLog(log: ExerciseLog): Long

    @Query("DELETE FROM exercise_logs WHERE id = :logId")
    suspend fun deleteLog(logId: Long)

    // Кросс-сессионная история: все логи упражнения клиента по его имени
    @Query("""
        SELECT exercise_logs.* FROM exercise_logs
        JOIN client_exercises ON client_exercises.id = exercise_logs.clientExerciseId
        JOIN client_workouts ON client_workouts.id = client_exercises.clientWorkoutId
        JOIN client_programs ON client_programs.id = client_workouts.clientProgramId
        WHERE client_programs.clientId = :clientId AND client_exercises.exerciseName = :exerciseName
        ORDER BY exercise_logs.date DESC
        LIMIT 30
    """)
    suspend fun getHistoryForExercise(clientId: Long, exerciseName: String): List<ExerciseLog>

    @Query("""
        SELECT client_programs.clientId FROM client_programs
        JOIN client_workouts ON client_workouts.clientProgramId = client_programs.id
        WHERE client_workouts.id = :workoutId
        LIMIT 1
    """)
    suspend fun getClientIdForWorkoutSync(workoutId: Long): Long?
}
