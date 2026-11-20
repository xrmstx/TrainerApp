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

    @Query("SELECT * FROM template_exercises WHERE blockId = :blockId ORDER BY `order` ASC")
    fun getExercisesForBlock(blockId: Long): Flow<List<TemplateExercise>>

    @Insert
    suspend fun insertExerciseBlock(block: ExerciseBlock): Long

    @Insert
    suspend fun insertTemplateExercise(exercise: TemplateExercise): Long

    @Query("SELECT * FROM client_programs WHERE clientId = :clientId ORDER BY weekNumber ASC")
    fun getProgramsForClient(clientId: Long): Flow<List<ClientProgram>>

    @Insert
    suspend fun insertClientProgram(program: ClientProgram): Long

    @Query("SELECT * FROM client_programs WHERE id = :programId")
    fun getProgramById(programId: Long): Flow<ClientProgram?>

    @Query("""
        SELECT te.* FROM template_exercises te
        INNER JOIN exercise_blocks eb ON te.blockId = eb.id
        WHERE eb.workoutId = :workoutId
        ORDER BY te.`order` ASC
    """)
    fun getExercisesForWorkout(workoutId: Long): Flow<List<TemplateExercise>>

    @Query("SELECT * FROM exercise_logs WHERE clientProgramId = :clientProgramId")
    suspend fun getLogsForProgram(clientProgramId: Long): List<ExerciseLog>

    @Insert
    suspend fun insertExerciseLog(log: ExerciseLog): Long
}
