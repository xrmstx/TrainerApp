package com.example.trainerapp.`data`.local

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class AppDatabase_Impl : AppDatabase() {
  private val _appDao: Lazy<AppDao> = lazy {
    AppDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(13,
        "590c38145b8a264318613227dd4f17ed", "782766364781190c57c6d955f4b191ba") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `clients` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `goal` TEXT, `notes` TEXT)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `macrocycles` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `durationWeeks` INTEGER NOT NULL, `sessionsPerWeek` INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `workouts` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `mesocycleId` INTEGER NOT NULL, `weekNumber` INTEGER NOT NULL, `dayNumber` INTEGER NOT NULL, `dayType` TEXT NOT NULL, `name` TEXT NOT NULL, FOREIGN KEY(`mesocycleId`) REFERENCES `macrocycles`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_workouts_mesocycleId` ON `workouts` (`mesocycleId`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `exercise_blocks` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `workoutId` INTEGER NOT NULL, `blockTitle` TEXT NOT NULL, `blockType` TEXT NOT NULL, `order` INTEGER NOT NULL, `protocol` TEXT NOT NULL, `protocolValue` TEXT NOT NULL, FOREIGN KEY(`workoutId`) REFERENCES `workouts`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_exercise_blocks_workoutId` ON `exercise_blocks` (`workoutId`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `template_exercises` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `blockId` INTEGER NOT NULL, `exerciseName` TEXT NOT NULL, `order` INTEGER NOT NULL, `targetSets` INTEGER NOT NULL, `targetReps` TEXT NOT NULL, `targetWeight` REAL, `note` TEXT, `metricType` TEXT NOT NULL, FOREIGN KEY(`blockId`) REFERENCES `exercise_blocks`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_template_exercises_blockId` ON `template_exercises` (`blockId`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `client_programs` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `clientId` INTEGER NOT NULL, `macrocycleId` INTEGER NOT NULL, `name` TEXT NOT NULL, `weekNumber` INTEGER NOT NULL, `totalWeeks` INTEGER NOT NULL, `status` TEXT NOT NULL, FOREIGN KEY(`clientId`) REFERENCES `clients`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_client_programs_clientId` ON `client_programs` (`clientId`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `client_workouts` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `clientProgramId` INTEGER NOT NULL, `originalWorkoutId` INTEGER NOT NULL, `weekNumber` INTEGER NOT NULL, `dayNumber` INTEGER NOT NULL, `dayType` TEXT NOT NULL, `name` TEXT NOT NULL, `status` TEXT NOT NULL, FOREIGN KEY(`clientProgramId`) REFERENCES `client_programs`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_client_workouts_clientProgramId` ON `client_workouts` (`clientProgramId`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `client_exercises` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `clientWorkoutId` INTEGER NOT NULL, `exerciseName` TEXT NOT NULL, `order` INTEGER NOT NULL, `blockTitle` TEXT NOT NULL, `blockType` TEXT NOT NULL, `blockOrder` INTEGER NOT NULL, `targetSets` INTEGER NOT NULL, `targetReps` TEXT NOT NULL, `targetWeight` REAL, `note` TEXT, `metricType` TEXT NOT NULL, `protocol` TEXT NOT NULL, `protocolValue` TEXT NOT NULL, FOREIGN KEY(`clientWorkoutId`) REFERENCES `client_workouts`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_client_exercises_clientWorkoutId` ON `client_exercises` (`clientWorkoutId`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `exercise_logs` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `clientExerciseId` INTEGER NOT NULL, `setNumber` INTEGER NOT NULL, `actualReps` INTEGER NOT NULL, `actualWeight` REAL NOT NULL, `resultText` TEXT, `note` TEXT, `date` INTEGER NOT NULL, FOREIGN KEY(`clientExerciseId`) REFERENCES `client_exercises`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_exercise_logs_clientExerciseId` ON `exercise_logs` (`clientExerciseId`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `measurements` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `clientId` INTEGER NOT NULL, `date` INTEGER NOT NULL, `weightKg` REAL, `heightCm` REAL, `chest` REAL, `waist` REAL, `hips` REAL, `arm` REAL, `thigh` REAL, `note` TEXT, FOREIGN KEY(`clientId`) REFERENCES `clients`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_measurements_clientId` ON `measurements` (`clientId`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '590c38145b8a264318613227dd4f17ed')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `clients`")
        connection.execSQL("DROP TABLE IF EXISTS `macrocycles`")
        connection.execSQL("DROP TABLE IF EXISTS `workouts`")
        connection.execSQL("DROP TABLE IF EXISTS `exercise_blocks`")
        connection.execSQL("DROP TABLE IF EXISTS `template_exercises`")
        connection.execSQL("DROP TABLE IF EXISTS `client_programs`")
        connection.execSQL("DROP TABLE IF EXISTS `client_workouts`")
        connection.execSQL("DROP TABLE IF EXISTS `client_exercises`")
        connection.execSQL("DROP TABLE IF EXISTS `exercise_logs`")
        connection.execSQL("DROP TABLE IF EXISTS `measurements`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        connection.execSQL("PRAGMA foreign_keys = ON")
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection):
          RoomOpenDelegate.ValidationResult {
        val _columnsClients: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsClients.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsClients.put("name", TableInfo.Column("name", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsClients.put("goal", TableInfo.Column("goal", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsClients.put("notes", TableInfo.Column("notes", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysClients: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesClients: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoClients: TableInfo = TableInfo("clients", _columnsClients, _foreignKeysClients,
            _indicesClients)
        val _existingClients: TableInfo = read(connection, "clients")
        if (!_infoClients.equals(_existingClients)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |clients(com.example.trainerapp.data.model.Client).
              | Expected:
              |""".trimMargin() + _infoClients + """
              |
              | Found:
              |""".trimMargin() + _existingClients)
        }
        val _columnsMacrocycles: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsMacrocycles.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsMacrocycles.put("name", TableInfo.Column("name", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsMacrocycles.put("durationWeeks", TableInfo.Column("durationWeeks", "INTEGER", true,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMacrocycles.put("sessionsPerWeek", TableInfo.Column("sessionsPerWeek", "INTEGER",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysMacrocycles: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesMacrocycles: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoMacrocycles: TableInfo = TableInfo("macrocycles", _columnsMacrocycles,
            _foreignKeysMacrocycles, _indicesMacrocycles)
        val _existingMacrocycles: TableInfo = read(connection, "macrocycles")
        if (!_infoMacrocycles.equals(_existingMacrocycles)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |macrocycles(com.example.trainerapp.data.model.Macrocycle).
              | Expected:
              |""".trimMargin() + _infoMacrocycles + """
              |
              | Found:
              |""".trimMargin() + _existingMacrocycles)
        }
        val _columnsWorkouts: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsWorkouts.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkouts.put("mesocycleId", TableInfo.Column("mesocycleId", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkouts.put("weekNumber", TableInfo.Column("weekNumber", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkouts.put("dayNumber", TableInfo.Column("dayNumber", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkouts.put("dayType", TableInfo.Column("dayType", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkouts.put("name", TableInfo.Column("name", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysWorkouts: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        _foreignKeysWorkouts.add(TableInfo.ForeignKey("macrocycles", "CASCADE", "NO ACTION",
            listOf("mesocycleId"), listOf("id")))
        val _indicesWorkouts: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesWorkouts.add(TableInfo.Index("index_workouts_mesocycleId", false,
            listOf("mesocycleId"), listOf("ASC")))
        val _infoWorkouts: TableInfo = TableInfo("workouts", _columnsWorkouts, _foreignKeysWorkouts,
            _indicesWorkouts)
        val _existingWorkouts: TableInfo = read(connection, "workouts")
        if (!_infoWorkouts.equals(_existingWorkouts)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |workouts(com.example.trainerapp.data.model.Workout).
              | Expected:
              |""".trimMargin() + _infoWorkouts + """
              |
              | Found:
              |""".trimMargin() + _existingWorkouts)
        }
        val _columnsExerciseBlocks: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsExerciseBlocks.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsExerciseBlocks.put("workoutId", TableInfo.Column("workoutId", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsExerciseBlocks.put("blockTitle", TableInfo.Column("blockTitle", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsExerciseBlocks.put("blockType", TableInfo.Column("blockType", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsExerciseBlocks.put("order", TableInfo.Column("order", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsExerciseBlocks.put("protocol", TableInfo.Column("protocol", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsExerciseBlocks.put("protocolValue", TableInfo.Column("protocolValue", "TEXT", true,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysExerciseBlocks: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        _foreignKeysExerciseBlocks.add(TableInfo.ForeignKey("workouts", "CASCADE", "NO ACTION",
            listOf("workoutId"), listOf("id")))
        val _indicesExerciseBlocks: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesExerciseBlocks.add(TableInfo.Index("index_exercise_blocks_workoutId", false,
            listOf("workoutId"), listOf("ASC")))
        val _infoExerciseBlocks: TableInfo = TableInfo("exercise_blocks", _columnsExerciseBlocks,
            _foreignKeysExerciseBlocks, _indicesExerciseBlocks)
        val _existingExerciseBlocks: TableInfo = read(connection, "exercise_blocks")
        if (!_infoExerciseBlocks.equals(_existingExerciseBlocks)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |exercise_blocks(com.example.trainerapp.data.model.ExerciseBlock).
              | Expected:
              |""".trimMargin() + _infoExerciseBlocks + """
              |
              | Found:
              |""".trimMargin() + _existingExerciseBlocks)
        }
        val _columnsTemplateExercises: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsTemplateExercises.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsTemplateExercises.put("blockId", TableInfo.Column("blockId", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTemplateExercises.put("exerciseName", TableInfo.Column("exerciseName", "TEXT", true,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTemplateExercises.put("order", TableInfo.Column("order", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsTemplateExercises.put("targetSets", TableInfo.Column("targetSets", "INTEGER", true,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTemplateExercises.put("targetReps", TableInfo.Column("targetReps", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTemplateExercises.put("targetWeight", TableInfo.Column("targetWeight", "REAL",
            false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTemplateExercises.put("note", TableInfo.Column("note", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsTemplateExercises.put("metricType", TableInfo.Column("metricType", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysTemplateExercises: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        _foreignKeysTemplateExercises.add(TableInfo.ForeignKey("exercise_blocks", "CASCADE",
            "NO ACTION", listOf("blockId"), listOf("id")))
        val _indicesTemplateExercises: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesTemplateExercises.add(TableInfo.Index("index_template_exercises_blockId", false,
            listOf("blockId"), listOf("ASC")))
        val _infoTemplateExercises: TableInfo = TableInfo("template_exercises",
            _columnsTemplateExercises, _foreignKeysTemplateExercises, _indicesTemplateExercises)
        val _existingTemplateExercises: TableInfo = read(connection, "template_exercises")
        if (!_infoTemplateExercises.equals(_existingTemplateExercises)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |template_exercises(com.example.trainerapp.data.model.TemplateExercise).
              | Expected:
              |""".trimMargin() + _infoTemplateExercises + """
              |
              | Found:
              |""".trimMargin() + _existingTemplateExercises)
        }
        val _columnsClientPrograms: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsClientPrograms.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsClientPrograms.put("clientId", TableInfo.Column("clientId", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsClientPrograms.put("macrocycleId", TableInfo.Column("macrocycleId", "INTEGER", true,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsClientPrograms.put("name", TableInfo.Column("name", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsClientPrograms.put("weekNumber", TableInfo.Column("weekNumber", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsClientPrograms.put("totalWeeks", TableInfo.Column("totalWeeks", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsClientPrograms.put("status", TableInfo.Column("status", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysClientPrograms: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        _foreignKeysClientPrograms.add(TableInfo.ForeignKey("clients", "CASCADE", "NO ACTION",
            listOf("clientId"), listOf("id")))
        val _indicesClientPrograms: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesClientPrograms.add(TableInfo.Index("index_client_programs_clientId", false,
            listOf("clientId"), listOf("ASC")))
        val _infoClientPrograms: TableInfo = TableInfo("client_programs", _columnsClientPrograms,
            _foreignKeysClientPrograms, _indicesClientPrograms)
        val _existingClientPrograms: TableInfo = read(connection, "client_programs")
        if (!_infoClientPrograms.equals(_existingClientPrograms)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |client_programs(com.example.trainerapp.data.model.ClientProgram).
              | Expected:
              |""".trimMargin() + _infoClientPrograms + """
              |
              | Found:
              |""".trimMargin() + _existingClientPrograms)
        }
        val _columnsClientWorkouts: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsClientWorkouts.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsClientWorkouts.put("clientProgramId", TableInfo.Column("clientProgramId", "INTEGER",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsClientWorkouts.put("originalWorkoutId", TableInfo.Column("originalWorkoutId",
            "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsClientWorkouts.put("weekNumber", TableInfo.Column("weekNumber", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsClientWorkouts.put("dayNumber", TableInfo.Column("dayNumber", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsClientWorkouts.put("dayType", TableInfo.Column("dayType", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsClientWorkouts.put("name", TableInfo.Column("name", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsClientWorkouts.put("status", TableInfo.Column("status", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysClientWorkouts: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        _foreignKeysClientWorkouts.add(TableInfo.ForeignKey("client_programs", "CASCADE",
            "NO ACTION", listOf("clientProgramId"), listOf("id")))
        val _indicesClientWorkouts: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesClientWorkouts.add(TableInfo.Index("index_client_workouts_clientProgramId", false,
            listOf("clientProgramId"), listOf("ASC")))
        val _infoClientWorkouts: TableInfo = TableInfo("client_workouts", _columnsClientWorkouts,
            _foreignKeysClientWorkouts, _indicesClientWorkouts)
        val _existingClientWorkouts: TableInfo = read(connection, "client_workouts")
        if (!_infoClientWorkouts.equals(_existingClientWorkouts)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |client_workouts(com.example.trainerapp.data.model.ClientWorkout).
              | Expected:
              |""".trimMargin() + _infoClientWorkouts + """
              |
              | Found:
              |""".trimMargin() + _existingClientWorkouts)
        }
        val _columnsClientExercises: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsClientExercises.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsClientExercises.put("clientWorkoutId", TableInfo.Column("clientWorkoutId",
            "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsClientExercises.put("exerciseName", TableInfo.Column("exerciseName", "TEXT", true,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsClientExercises.put("order", TableInfo.Column("order", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsClientExercises.put("blockTitle", TableInfo.Column("blockTitle", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsClientExercises.put("blockType", TableInfo.Column("blockType", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsClientExercises.put("blockOrder", TableInfo.Column("blockOrder", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsClientExercises.put("targetSets", TableInfo.Column("targetSets", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsClientExercises.put("targetReps", TableInfo.Column("targetReps", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsClientExercises.put("targetWeight", TableInfo.Column("targetWeight", "REAL", false,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsClientExercises.put("note", TableInfo.Column("note", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsClientExercises.put("metricType", TableInfo.Column("metricType", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsClientExercises.put("protocol", TableInfo.Column("protocol", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsClientExercises.put("protocolValue", TableInfo.Column("protocolValue", "TEXT", true,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysClientExercises: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        _foreignKeysClientExercises.add(TableInfo.ForeignKey("client_workouts", "CASCADE",
            "NO ACTION", listOf("clientWorkoutId"), listOf("id")))
        val _indicesClientExercises: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesClientExercises.add(TableInfo.Index("index_client_exercises_clientWorkoutId", false,
            listOf("clientWorkoutId"), listOf("ASC")))
        val _infoClientExercises: TableInfo = TableInfo("client_exercises", _columnsClientExercises,
            _foreignKeysClientExercises, _indicesClientExercises)
        val _existingClientExercises: TableInfo = read(connection, "client_exercises")
        if (!_infoClientExercises.equals(_existingClientExercises)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |client_exercises(com.example.trainerapp.data.model.ClientExercise).
              | Expected:
              |""".trimMargin() + _infoClientExercises + """
              |
              | Found:
              |""".trimMargin() + _existingClientExercises)
        }
        val _columnsExerciseLogs: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsExerciseLogs.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsExerciseLogs.put("clientExerciseId", TableInfo.Column("clientExerciseId", "INTEGER",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsExerciseLogs.put("setNumber", TableInfo.Column("setNumber", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsExerciseLogs.put("actualReps", TableInfo.Column("actualReps", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsExerciseLogs.put("actualWeight", TableInfo.Column("actualWeight", "REAL", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsExerciseLogs.put("resultText", TableInfo.Column("resultText", "TEXT", false, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsExerciseLogs.put("note", TableInfo.Column("note", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsExerciseLogs.put("date", TableInfo.Column("date", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysExerciseLogs: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        _foreignKeysExerciseLogs.add(TableInfo.ForeignKey("client_exercises", "CASCADE",
            "NO ACTION", listOf("clientExerciseId"), listOf("id")))
        val _indicesExerciseLogs: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesExerciseLogs.add(TableInfo.Index("index_exercise_logs_clientExerciseId", false,
            listOf("clientExerciseId"), listOf("ASC")))
        val _infoExerciseLogs: TableInfo = TableInfo("exercise_logs", _columnsExerciseLogs,
            _foreignKeysExerciseLogs, _indicesExerciseLogs)
        val _existingExerciseLogs: TableInfo = read(connection, "exercise_logs")
        if (!_infoExerciseLogs.equals(_existingExerciseLogs)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |exercise_logs(com.example.trainerapp.data.model.ExerciseLog).
              | Expected:
              |""".trimMargin() + _infoExerciseLogs + """
              |
              | Found:
              |""".trimMargin() + _existingExerciseLogs)
        }
        val _columnsMeasurements: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsMeasurements.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsMeasurements.put("clientId", TableInfo.Column("clientId", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsMeasurements.put("date", TableInfo.Column("date", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsMeasurements.put("weightKg", TableInfo.Column("weightKg", "REAL", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsMeasurements.put("heightCm", TableInfo.Column("heightCm", "REAL", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsMeasurements.put("chest", TableInfo.Column("chest", "REAL", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsMeasurements.put("waist", TableInfo.Column("waist", "REAL", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsMeasurements.put("hips", TableInfo.Column("hips", "REAL", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsMeasurements.put("arm", TableInfo.Column("arm", "REAL", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsMeasurements.put("thigh", TableInfo.Column("thigh", "REAL", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsMeasurements.put("note", TableInfo.Column("note", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysMeasurements: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        _foreignKeysMeasurements.add(TableInfo.ForeignKey("clients", "CASCADE", "NO ACTION",
            listOf("clientId"), listOf("id")))
        val _indicesMeasurements: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesMeasurements.add(TableInfo.Index("index_measurements_clientId", false,
            listOf("clientId"), listOf("ASC")))
        val _infoMeasurements: TableInfo = TableInfo("measurements", _columnsMeasurements,
            _foreignKeysMeasurements, _indicesMeasurements)
        val _existingMeasurements: TableInfo = read(connection, "measurements")
        if (!_infoMeasurements.equals(_existingMeasurements)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |measurements(com.example.trainerapp.data.model.ClientMeasurement).
              | Expected:
              |""".trimMargin() + _infoMeasurements + """
              |
              | Found:
              |""".trimMargin() + _existingMeasurements)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "clients", "macrocycles",
        "workouts", "exercise_blocks", "template_exercises", "client_programs", "client_workouts",
        "client_exercises", "exercise_logs", "measurements")
  }

  public override fun clearAllTables() {
    super.performClear(true, "clients", "macrocycles", "workouts", "exercise_blocks",
        "template_exercises", "client_programs", "client_workouts", "client_exercises",
        "exercise_logs", "measurements")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(AppDao::class, AppDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override
      fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>):
      List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun appDao(): AppDao = _appDao.value
}
