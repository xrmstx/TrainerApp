package com.example.trainerapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.trainerapp.data.model.*

@Database(
    entities = [
        Client::class, Macrocycle::class, Workout::class, ExerciseBlock::class,
        TemplateExercise::class, ClientProgram::class, ClientWorkout::class,
        ClientExercise::class, ExerciseLog::class, ClientMeasurement::class
    ],
    version = 14,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Внешние ключи добавляются пересозданием таблиц; данные переносятся. blockOrder у старых данных = 0.
        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE workouts_new (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, mesocycleId INTEGER NOT NULL, dayNumber INTEGER NOT NULL, name TEXT NOT NULL, FOREIGN KEY(mesocycleId) REFERENCES macrocycles(id) ON DELETE CASCADE)")
                db.execSQL("INSERT INTO workouts_new (id, mesocycleId, dayNumber, name) SELECT id, mesocycleId, dayNumber, name FROM workouts")
                db.execSQL("DROP TABLE workouts")
                db.execSQL("ALTER TABLE workouts_new RENAME TO workouts")
                db.execSQL("CREATE INDEX index_workouts_mesocycleId ON workouts(mesocycleId)")

                db.execSQL("CREATE TABLE exercise_blocks_new (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, workoutId INTEGER NOT NULL, blockTitle TEXT NOT NULL, blockType TEXT NOT NULL, `order` INTEGER NOT NULL, FOREIGN KEY(workoutId) REFERENCES workouts(id) ON DELETE CASCADE)")
                db.execSQL("INSERT INTO exercise_blocks_new (id, workoutId, blockTitle, blockType, `order`) SELECT id, workoutId, blockTitle, blockType, id FROM exercise_blocks")
                db.execSQL("DROP TABLE exercise_blocks")
                db.execSQL("ALTER TABLE exercise_blocks_new RENAME TO exercise_blocks")
                db.execSQL("CREATE INDEX index_exercise_blocks_workoutId ON exercise_blocks(workoutId)")

                db.execSQL("CREATE TABLE template_exercises_new (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, blockId INTEGER NOT NULL, exerciseName TEXT NOT NULL, `order` INTEGER NOT NULL, targetSets INTEGER NOT NULL, targetReps TEXT NOT NULL, targetWeight REAL, note TEXT, metricType TEXT NOT NULL, FOREIGN KEY(blockId) REFERENCES exercise_blocks(id) ON DELETE CASCADE)")
                db.execSQL("INSERT INTO template_exercises_new (id, blockId, exerciseName, `order`, targetSets, targetReps, targetWeight, note, metricType) SELECT id, blockId, exerciseName, `order`, targetSets, targetReps, targetWeight, note, metricType FROM template_exercises")
                db.execSQL("DROP TABLE template_exercises")
                db.execSQL("ALTER TABLE template_exercises_new RENAME TO template_exercises")
                db.execSQL("CREATE INDEX index_template_exercises_blockId ON template_exercises(blockId)")

                db.execSQL("CREATE TABLE client_programs_new (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, clientId INTEGER NOT NULL, macrocycleId INTEGER NOT NULL, name TEXT NOT NULL DEFAULT '', weekNumber INTEGER NOT NULL, totalWeeks INTEGER NOT NULL, status TEXT NOT NULL, FOREIGN KEY(clientId) REFERENCES clients(id) ON DELETE CASCADE)")
                db.execSQL("INSERT INTO client_programs_new (id, clientId, macrocycleId, name, weekNumber, totalWeeks, status) SELECT id, clientId, macrocycleId, '', weekNumber, totalWeeks, status FROM client_programs")
                db.execSQL("DROP TABLE client_programs")
                db.execSQL("ALTER TABLE client_programs_new RENAME TO client_programs")
                db.execSQL("CREATE INDEX index_client_programs_clientId ON client_programs(clientId)")

                db.execSQL("CREATE TABLE client_workouts_new (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, clientProgramId INTEGER NOT NULL, originalWorkoutId INTEGER NOT NULL, dayNumber INTEGER NOT NULL, name TEXT NOT NULL, status TEXT NOT NULL, FOREIGN KEY(clientProgramId) REFERENCES client_programs(id) ON DELETE CASCADE)")
                db.execSQL("INSERT INTO client_workouts_new SELECT * FROM client_workouts")
                db.execSQL("DROP TABLE client_workouts")
                db.execSQL("ALTER TABLE client_workouts_new RENAME TO client_workouts")
                db.execSQL("CREATE INDEX index_client_workouts_clientProgramId ON client_workouts(clientProgramId)")

                db.execSQL("CREATE TABLE client_exercises_new (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, clientWorkoutId INTEGER NOT NULL, exerciseName TEXT NOT NULL, `order` INTEGER NOT NULL, blockTitle TEXT NOT NULL, blockType TEXT NOT NULL, blockOrder INTEGER NOT NULL DEFAULT 0, targetSets INTEGER NOT NULL, targetReps TEXT NOT NULL, targetWeight REAL, note TEXT, metricType TEXT NOT NULL, FOREIGN KEY(clientWorkoutId) REFERENCES client_workouts(id) ON DELETE CASCADE)")
                db.execSQL("INSERT INTO client_exercises_new (id, clientWorkoutId, exerciseName, `order`, blockTitle, blockType, blockOrder, targetSets, targetReps, targetWeight, note, metricType) SELECT id, clientWorkoutId, exerciseName, `order`, blockTitle, blockType, 0, targetSets, targetReps, NULL, note, metricType FROM client_exercises")
                db.execSQL("DROP TABLE client_exercises")
                db.execSQL("ALTER TABLE client_exercises_new RENAME TO client_exercises")
                db.execSQL("CREATE INDEX index_client_exercises_clientWorkoutId ON client_exercises(clientWorkoutId)")

                db.execSQL("CREATE TABLE exercise_logs_new (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, clientExerciseId INTEGER NOT NULL, setNumber INTEGER NOT NULL, actualReps INTEGER NOT NULL, actualWeight REAL NOT NULL, resultText TEXT, note TEXT, date INTEGER NOT NULL, FOREIGN KEY(clientExerciseId) REFERENCES client_exercises(id) ON DELETE CASCADE)")
                db.execSQL("INSERT INTO exercise_logs_new SELECT * FROM exercise_logs")
                db.execSQL("DROP TABLE exercise_logs")
                db.execSQL("ALTER TABLE exercise_logs_new RENAME TO exercise_logs")
                db.execSQL("CREATE INDEX index_exercise_logs_clientExerciseId ON exercise_logs(clientExerciseId)")
            }
        }

        // Протоколы меткона: поля добавляются с дефолтами, старые данные не трогаем.
        private val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE exercise_blocks ADD COLUMN protocol TEXT NOT NULL DEFAULT 'NONE'")
                db.execSQL("ALTER TABLE exercise_blocks ADD COLUMN protocolValue TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE client_exercises ADD COLUMN protocol TEXT NOT NULL DEFAULT 'NONE'")
                db.execSQL("ALTER TABLE client_exercises ADD COLUMN protocolValue TEXT NOT NULL DEFAULT ''")
            }
        }

        // Недельная структура: weekNumber у тренировок шаблона и клиента,
        // dayType (TRAINING / RECOVERY) и sessionsPerWeek у макроцикла.
        private val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE workouts ADD COLUMN weekNumber INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE workouts ADD COLUMN dayType TEXT NOT NULL DEFAULT 'TRAINING'")
                db.execSQL("ALTER TABLE client_workouts ADD COLUMN weekNumber INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE client_workouts ADD COLUMN dayType TEXT NOT NULL DEFAULT 'TRAINING'")
                db.execSQL("ALTER TABLE macrocycles ADD COLUMN sessionsPerWeek INTEGER NOT NULL DEFAULT 3")
            }
        }

        // Замеры клиента: таблица создаётся с нуля, все числовые поля опциональны.
        private val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS `measurements` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `clientId` INTEGER NOT NULL,
                        `date` INTEGER NOT NULL,
                        `weightKg` REAL,
                        `heightCm` REAL,
                        `chest` REAL,
                        `waist` REAL,
                        `hips` REAL,
                        `arm` REAL,
                        `thigh` REAL,
                        `note` TEXT,
                        FOREIGN KEY(`clientId`) REFERENCES `clients`(`id`)
                            ON UPDATE NO ACTION ON DELETE CASCADE
                    )"""
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_measurements_clientId` ON `measurements`(`clientId`)"
                )
            }
        }

        private val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `template_exercises` ADD COLUMN `supersetGroup` INTEGER")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fitness_database"
                )
                    .addMigrations(MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13, MIGRATION_13_14)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
