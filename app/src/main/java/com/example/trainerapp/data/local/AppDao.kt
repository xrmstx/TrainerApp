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
    @Query("DELETE FROM clients WHERE id = :clientId")
    suspend fun deleteClient(clientId: Long)

    @Query("SELECT * FROM macrocycles")
    fun getAllMacrocycles(): Flow<List<Macrocycle>>
    @Insert
    suspend fun insertMacrocycle(macrocycle: Macrocycle): Long
    @Query("DELETE FROM macrocycles WHERE id = :macroId")
    suspend fun deleteMacrocycle(macroId: Long)

    @Query("SELECT * FROM workouts WHERE mesocycleId = :macroId ORDER BY dayNumber ASC")
    fun getWorkoutsForMacro(macroId: Long): Flow<List<Workout>>
    @Insert
    suspend fun insertWorkout(workout: Workout): Long
    @Query("DELETE FROM workouts WHERE id = :workoutId")
    suspend fun deleteWorkout(workoutId: Long)

    @Query("SELECT * FROM workouts WHERE mesocycleId = :macroId ORDER BY dayNumber ASC")
    suspend fun getWorkoutsForMacroSync(macroId: Long): List<Workout>

    @Query("SELECT * FROM exercise_blocks WHERE workoutId = :workoutId")
    suspend fun getBlocksForWorkoutSync(workoutId: Long): List<ExerciseBlock>

    @Query("SELECT * FROM template_exercises WHERE blockId = :blockId ORDER BY `order` ASC")
    fun getExercisesForBlock(blockId: Long): Flow<List<TemplateExercise>>

    @Query("SELECT * FROM template_exercises WHERE blockId = :blockId ORDER BY `order` ASC")
    suspend fun getExercisesForBlockSync(blockId: Long): List<TemplateExercise>

    @Insert
    suspend fun insertExerciseBlock(block: ExerciseBlock): Long
    @Insert
    suspend fun insertTemplateExercise(exercise: TemplateExercise): Long
    @Query("DELETE FROM template_exercises WHERE id = :exerciseId")
    suspend fun deleteTemplateExercise(exerciseId: Long)

    @Query("SELECT * FROM template_exercises GROUP BY exerciseName ORDER BY id DESC")
    suspend fun getUniqueExercises(): List<TemplateExercise>

    @Query("SELECT * FROM client_programs WHERE clientId = :clientId ORDER BY weekNumber ASC")
    fun getProgramsForClient(clientId: Long): Flow<List<ClientProgram>>
    @Insert
    suspend fun insertClientProgram(program: ClientProgram): Long
    @Query("DELETE FROM client_programs WHERE id = :programId")
    suspend fun deleteClientProgram(programId: Long)

    @Query("SELECT * FROM client_workouts WHERE clientProgramId = :programId ORDER BY dayNumber ASC")
    fun getClientWorkoutsForProgram(programId: Long): Flow<List<ClientWorkout>>
    @Insert
    suspend fun insertClientWorkout(workout: ClientWorkout): Long

    @Query("SELECT * FROM client_exercises WHERE clientWorkoutId = :workoutId ORDER BY blockTitle ASC, `order` ASC")
    fun getClientExercises(workoutId: Long): Flow<List<ClientExercise>>
    @Insert
    suspend fun insertClientExercise(exercise: ClientExercise): Long

    @Query("SELECT * FROM exercise_logs WHERE clientExerciseId = :clientExerciseId ORDER BY date DESC LIMIT 1")
    suspend fun getLastLogForExercise(clientExerciseId: Long): ExerciseLog?

    @Query("SELECT * FROM exercise_logs WHERE clientExerciseId = :clientExerciseId ORDER BY setNumber ASC")
    fun getLogsForExercise(clientExerciseId: Long): Flow<List<ExerciseLog>>
    @Insert
    suspend fun insertExerciseLog(log: ExerciseLog): Long
    @Query("DELETE FROM exercise_logs WHERE id = :logId")
    suspend fun deleteLog(logId: Long)
}
