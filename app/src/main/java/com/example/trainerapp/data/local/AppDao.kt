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
}
