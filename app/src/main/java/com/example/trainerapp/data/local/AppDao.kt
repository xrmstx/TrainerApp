package com.example.trainerapp.data.local

import androidx.room.*
import com.example.trainerapp.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Query("SELECT * FROM clients ORDER BY name ASC")
    fun getAllClients(): Flow<List<Client>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClient(client: Client): Long

    @Query("SELECT * FROM macrocycles")
    fun getAllMacrocycles(): Flow<List<Macrocycle>>
    @Insert
    suspend fun insertMacrocycle(macrocycle: Macrocycle): Long

    @Query("SELECT * FROM workouts WHERE mesocycleId = :macroId ORDER BY dayNumber ASC")
    fun getWorkoutsForMacro(macroId: Long): Flow<List<Workout>>
    @Insert
    suspend fun insertWorkout(workout: Workout): Long

    // Получаем блок для тренировки (нужно для фикса бага)
    @Query("SELECT * FROM exercise_blocks WHERE workoutId = :workoutId LIMIT 1")
    suspend fun getBlockForWorkout(workoutId: Long): ExerciseBlock?

    @Query("SELECT * FROM template_exercises WHERE blockId = :blockId ORDER BY `order` ASC")
    fun getExercisesForBlock(blockId: Long): Flow<List<TemplateExercise>>
    
    @Insert
    suspend fun insertExerciseBlock(block: ExerciseBlock): Long
    @Insert
    suspend fun insertTemplateExercise(exercise: TemplateExercise): Long

    // --- Клиенты и Программы ---
    @Query("SELECT * FROM client_programs WHERE clientId = :clientId ORDER BY weekNumber ASC")
    fun getProgramsForClient(clientId: Long): Flow<List<ClientProgram>>
    @Insert
    suspend fun insertClientProgram(program: ClientProgram): Long

    @Query("SELECT * FROM client_workouts WHERE clientProgramId = :programId ORDER BY dayNumber ASC")
    fun getClientWorkoutsForProgram(programId: Long): Flow<List<ClientWorkout>>
    @Insert
    suspend fun insertClientWorkout(workout: ClientWorkout): Long

    @Query("SELECT * FROM client_exercises WHERE clientWorkoutId = :workoutId ORDER BY `order` ASC")
    fun getClientExercises(workoutId: Long): Flow<List<ClientExercise>>
    @Insert
    suspend fun insertClientExercise(exercise: ClientExercise): Long

    @Query("SELECT * FROM exercise_logs WHERE clientExerciseId = :clientExerciseId ORDER BY date DESC")
    fun getLogsForExercise(clientExerciseId: Long): Flow<List<ExerciseLog>>
    @Insert
    suspend fun insertExerciseLog(log: ExerciseLog): Long
}
