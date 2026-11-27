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

    // --- Замеры клиента ---
    @Query("SELECT * FROM measurements WHERE clientId = :clientId ORDER BY date DESC")
    fun getMeasurements(clientId: Long): Flow<List<ClientMeasurement>>

    @Insert
    suspend fun insertMeasurement(m: ClientMeasurement)

    @Delete
    suspend fun deleteMeasurement(m: ClientMeasurement)

    // --- Шаблоны (макроциклы) ---
    @Query("SELECT * FROM macrocycles")
    fun getAllMacrocycles(): Flow<List<Macrocycle>>

    @Insert
    suspend fun insertMacrocycle(macrocycle: Macrocycle): Long

    @Update
    suspend fun updateMacrocycle(macrocycle: Macrocycle)

    @Query("SELECT * FROM macrocycles WHERE id = :macroId")
    suspend fun getMacroSync(macroId: Long): Macrocycle?

    @Query("DELETE FROM macrocycles WHERE id = :macroId")
    suspend fun deleteMacrocycle(macroId: Long)

    // --- Тренировки шаблона ---
    @Query("SELECT * FROM workouts WHERE mesocycleId = :macroId ORDER BY weekNumber ASC, dayNumber ASC")
    fun getWorkoutsForMacro(macroId: Long): Flow<List<Workout>>

    @Query("SELECT * FROM workouts WHERE mesocycleId = :macroId ORDER BY weekNumber ASC, dayNumber ASC")
    suspend fun getWorkoutsForMacroSync(macroId: Long): List<Workout>

    @Insert
    suspend fun insertWorkout(workout: Workout): Long

    @Query("DELETE FROM workouts WHERE id = :workoutId")
    suspend fun deleteWorkout(workoutId: Long)

    @Query("SELECT MAX(dayNumber) FROM workouts WHERE mesocycleId = :macroId AND weekNumber = :weekNumber")
    suspend fun getMaxDayNumber(macroId: Long, weekNumber: Int): Int?

    // --- Недели шаблона ---
    @Query("SELECT DISTINCT weekNumber FROM workouts WHERE mesocycleId = :macroId ORDER BY weekNumber ASC")
    suspend fun getWeekNumbersForMacroSync(macroId: Long): List<Int>

    @Query("SELECT MAX(weekNumber) FROM workouts WHERE mesocycleId = :macroId")
    suspend fun getMaxWeekNumber(macroId: Long): Int?

    @Query("SELECT * FROM workouts WHERE mesocycleId = :macroId AND weekNumber = :weekNumber ORDER BY dayNumber ASC")
    fun getWorkoutsForMacroWeek(macroId: Long, weekNumber: Int): Flow<List<Workout>>

    @Query("SELECT * FROM workouts WHERE mesocycleId = :macroId AND weekNumber = :weekNumber ORDER BY dayNumber ASC")
    suspend fun getWorkoutsForMacroWeekSync(macroId: Long, weekNumber: Int): List<Workout>

    @Query("SELECT COUNT(*) FROM workouts WHERE mesocycleId = :macroId AND weekNumber = :weekNumber")
    suspend fun countWorkoutsInWeek(macroId: Long, weekNumber: Int): Int

    // Полная зачистка недели: используется при перезаписи недели копией
    @Query("DELETE FROM workouts WHERE mesocycleId = :macroId AND weekNumber = :weekNumber")
    suspend fun deleteWorkoutsOfWeek(macroId: Long, weekNumber: Int)

    // --- Блоки шаблона ---
    @Query("SELECT * FROM exercise_blocks WHERE workoutId = :workoutId ORDER BY `order` ASC, id ASC")
    suspend fun getBlocksForWorkoutSync(workoutId: Long): List<ExerciseBlock>

    @Query("SELECT MAX(`order`) FROM exercise_blocks WHERE workoutId = :workoutId")
    suspend fun getMaxBlockOrder(workoutId: Long): Int?

    @Insert
    suspend fun insertExerciseBlock(block: ExerciseBlock): Long

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

    // --- Программы клиента ---
    @Query("SELECT * FROM client_programs WHERE clientId = :clientId ORDER BY weekNumber ASC")
    fun getProgramsForClient(clientId: Long): Flow<List<ClientProgram>>

    @Query("SELECT * FROM client_programs WHERE clientId = :clientId")
    suspend fun getProgramsForClientSync(clientId: Long): List<ClientProgram>

    @Query("SELECT * FROM client_programs WHERE id = :programId")
    suspend fun getClientProgramSync(programId: Long): ClientProgram?

    @Query("DELETE FROM client_programs WHERE id = :programId")
    suspend fun deleteClientProgram(programId: Long)

    @Insert
    suspend fun insertClientProgram(program: ClientProgram): Long

    // --- Тренировки клиента ---
    @Query("SELECT * FROM client_workouts WHERE clientProgramId = :programId ORDER BY weekNumber ASC, dayNumber ASC")
    fun getClientWorkoutsForProgram(programId: Long): Flow<List<ClientWorkout>>

    @Query("SELECT * FROM client_workouts WHERE clientProgramId = :programId AND weekNumber = :weekNumber ORDER BY dayNumber ASC")
    fun getClientWorkoutsForProgramWeek(programId: Long, weekNumber: Int): Flow<List<ClientWorkout>>

    @Query("SELECT * FROM client_workouts WHERE clientProgramId = :programId ORDER BY weekNumber ASC, dayNumber ASC")
    suspend fun getClientWorkoutsForProgramSync(programId: Long): List<ClientWorkout>

    @Query("SELECT * FROM client_workouts WHERE id = :workoutId")
    suspend fun getClientWorkoutSync(workoutId: Long): ClientWorkout?

    @Insert
    suspend fun insertClientWorkout(workout: ClientWorkout): Long

    @Query("UPDATE client_workouts SET status = :status WHERE id = :workoutId")
    suspend fun setClientWorkoutStatus(workoutId: Long, status: String)

    // --- Автопереход недель ---
    @Query("SELECT COUNT(*) FROM client_workouts WHERE clientProgramId = :programId AND weekNumber = :weekNumber")
    suspend fun countWorkoutsInClientWeek(programId: Long, weekNumber: Int): Int

    @Query("SELECT COUNT(*) FROM client_workouts WHERE clientProgramId = :programId AND weekNumber = :weekNumber AND status = 'DONE'")
    suspend fun countDoneWorkoutsInClientWeek(programId: Long, weekNumber: Int): Int

    // Первая незакрытая тренировка клиента по всем его активным программам
    @Query("""
        SELECT cw.* FROM client_workouts cw
        JOIN client_programs cp ON cw.clientProgramId = cp.id
        WHERE cp.clientId = :clientId
          AND cp.status != 'DONE'
          AND cw.status = 'PLANNED'
        ORDER BY cw.weekNumber ASC, cw.dayNumber ASC, cw.id ASC
        LIMIT 1
    """)
    suspend fun getNextClientWorkoutSync(clientId: Long): ClientWorkout?

    // --- Упражнения клиента ---
    @Query("SELECT * FROM client_exercises WHERE clientWorkoutId = :workoutId ORDER BY blockOrder ASC, `order` ASC, id ASC")
    fun getClientExercises(workoutId: Long): Flow<List<ClientExercise>>

    @Query("SELECT * FROM client_exercises WHERE clientWorkoutId = :workoutId ORDER BY blockOrder ASC, `order` ASC, id ASC")
    suspend fun getClientExercisesSync(workoutId: Long): List<ClientExercise>

    @Insert
    suspend fun insertClientExercise(exercise: ClientExercise): Long

    // --- Логи ---
    @Query("SELECT * FROM exercise_logs WHERE clientExerciseId = :clientExerciseId ORDER BY date DESC, setNumber ASC LIMIT 1")
    suspend fun getLastLogForExercise(clientExerciseId: Long): ExerciseLog?

    @Query("SELECT * FROM exercise_logs WHERE clientExerciseId = :clientExerciseId ORDER BY setNumber ASC, date ASC")
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

    // --- Сводка на сегодня ---
    // Ровно одна незакрытая тренировка на клиента: самая ранняя PLANNED
    // по его активным программам. Следующая появится после закрытия текущей.
    @Query("""
    SELECT cw.id AS workoutId, cw.name AS workoutName,
           cw.weekNumber AS weekNumber, cw.dayNumber AS dayNumber, cw.status AS status,
           c.name AS clientName, cp.id AS programId
    FROM client_workouts cw
    JOIN client_programs cp ON cw.clientProgramId = cp.id
    JOIN clients c ON cp.clientId = c.id
    WHERE cw.status = 'PLANNED' AND cp.status != 'DONE'
      AND cw.id = (
          SELECT cw2.id FROM client_workouts cw2
          WHERE cw2.clientProgramId = cp.id AND cw2.status = 'PLANNED'
          ORDER BY cw2.weekNumber ASC, cw2.dayNumber ASC, cw2.id ASC
          LIMIT 1
      )
    ORDER BY c.name, cw.weekNumber, cw.dayNumber
""")
    suspend fun getTodayWorkoutsSync(): List<TodayWorkout>

    @Query("SELECT COUNT(*) FROM clients")
    suspend fun getClientsCountSync(): Int
}
