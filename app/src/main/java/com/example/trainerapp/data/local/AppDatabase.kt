package com.example.trainerapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.trainerapp.data.model.*

@Database(
    entities = [
        Client::class, Macrocycle::class, Workout::class, ExerciseBlock::class, 
        TemplateExercise::class, ClientProgram::class, ClientWorkout::class, 
        ClientExercise::class, ExerciseLog::class
    ],
    version = 7, // <-- ВЕРСИЯ 7
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fitness_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
