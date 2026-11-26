package com.example.trainerapp.`data`.local

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.example.trainerapp.`data`.model.Client
import com.example.trainerapp.`data`.model.ClientExercise
import com.example.trainerapp.`data`.model.ClientMeasurement
import com.example.trainerapp.`data`.model.ClientProgram
import com.example.trainerapp.`data`.model.ClientWorkout
import com.example.trainerapp.`data`.model.ExerciseBlock
import com.example.trainerapp.`data`.model.ExerciseLog
import com.example.trainerapp.`data`.model.Macrocycle
import com.example.trainerapp.`data`.model.TemplateExercise
import com.example.trainerapp.`data`.model.TodayWorkout
import com.example.trainerapp.`data`.model.Workout
import javax.`annotation`.processing.Generated
import kotlin.Double
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class AppDao_Impl(
  __db: RoomDatabase,
) : AppDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfClient: EntityInsertAdapter<Client>

  private val __insertAdapterOfClientMeasurement: EntityInsertAdapter<ClientMeasurement>

  private val __insertAdapterOfMacrocycle: EntityInsertAdapter<Macrocycle>

  private val __insertAdapterOfWorkout: EntityInsertAdapter<Workout>

  private val __insertAdapterOfExerciseBlock: EntityInsertAdapter<ExerciseBlock>

  private val __insertAdapterOfTemplateExercise: EntityInsertAdapter<TemplateExercise>

  private val __insertAdapterOfClientProgram: EntityInsertAdapter<ClientProgram>

  private val __insertAdapterOfClientWorkout: EntityInsertAdapter<ClientWorkout>

  private val __insertAdapterOfClientExercise: EntityInsertAdapter<ClientExercise>

  private val __insertAdapterOfExerciseLog: EntityInsertAdapter<ExerciseLog>

  private val __deleteAdapterOfClientMeasurement: EntityDeleteOrUpdateAdapter<ClientMeasurement>

  private val __updateAdapterOfMacrocycle: EntityDeleteOrUpdateAdapter<Macrocycle>

  private val __updateAdapterOfTemplateExercise: EntityDeleteOrUpdateAdapter<TemplateExercise>
  init {
    this.__db = __db
    this.__insertAdapterOfClient = object : EntityInsertAdapter<Client>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `clients` (`id`,`name`,`goal`,`notes`) VALUES (nullif(?, 0),?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: Client) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.name)
        val _tmpGoal: String? = entity.goal
        if (_tmpGoal == null) {
          statement.bindNull(3)
        } else {
          statement.bindText(3, _tmpGoal)
        }
        val _tmpNotes: String? = entity.notes
        if (_tmpNotes == null) {
          statement.bindNull(4)
        } else {
          statement.bindText(4, _tmpNotes)
        }
      }
    }
    this.__insertAdapterOfClientMeasurement = object : EntityInsertAdapter<ClientMeasurement>() {
      protected override fun createQuery(): String =
          "INSERT OR ABORT INTO `measurements` (`id`,`clientId`,`date`,`weightKg`,`heightCm`,`chest`,`waist`,`hips`,`arm`,`thigh`,`note`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: ClientMeasurement) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.clientId)
        statement.bindLong(3, entity.date)
        val _tmpWeightKg: Double? = entity.weightKg
        if (_tmpWeightKg == null) {
          statement.bindNull(4)
        } else {
          statement.bindDouble(4, _tmpWeightKg)
        }
        val _tmpHeightCm: Double? = entity.heightCm
        if (_tmpHeightCm == null) {
          statement.bindNull(5)
        } else {
          statement.bindDouble(5, _tmpHeightCm)
        }
        val _tmpChest: Double? = entity.chest
        if (_tmpChest == null) {
          statement.bindNull(6)
        } else {
          statement.bindDouble(6, _tmpChest)
        }
        val _tmpWaist: Double? = entity.waist
        if (_tmpWaist == null) {
          statement.bindNull(7)
        } else {
          statement.bindDouble(7, _tmpWaist)
        }
        val _tmpHips: Double? = entity.hips
        if (_tmpHips == null) {
          statement.bindNull(8)
        } else {
          statement.bindDouble(8, _tmpHips)
        }
        val _tmpArm: Double? = entity.arm
        if (_tmpArm == null) {
          statement.bindNull(9)
        } else {
          statement.bindDouble(9, _tmpArm)
        }
        val _tmpThigh: Double? = entity.thigh
        if (_tmpThigh == null) {
          statement.bindNull(10)
        } else {
          statement.bindDouble(10, _tmpThigh)
        }
        val _tmpNote: String? = entity.note
        if (_tmpNote == null) {
          statement.bindNull(11)
        } else {
          statement.bindText(11, _tmpNote)
        }
      }
    }
    this.__insertAdapterOfMacrocycle = object : EntityInsertAdapter<Macrocycle>() {
      protected override fun createQuery(): String =
          "INSERT OR ABORT INTO `macrocycles` (`id`,`name`,`durationWeeks`,`sessionsPerWeek`) VALUES (nullif(?, 0),?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: Macrocycle) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.name)
        statement.bindLong(3, entity.durationWeeks.toLong())
        statement.bindLong(4, entity.sessionsPerWeek.toLong())
      }
    }
    this.__insertAdapterOfWorkout = object : EntityInsertAdapter<Workout>() {
      protected override fun createQuery(): String =
          "INSERT OR ABORT INTO `workouts` (`id`,`mesocycleId`,`weekNumber`,`dayNumber`,`dayType`,`name`) VALUES (nullif(?, 0),?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: Workout) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.mesocycleId)
        statement.bindLong(3, entity.weekNumber.toLong())
        statement.bindLong(4, entity.dayNumber.toLong())
        statement.bindText(5, entity.dayType)
        statement.bindText(6, entity.name)
      }
    }
    this.__insertAdapterOfExerciseBlock = object : EntityInsertAdapter<ExerciseBlock>() {
      protected override fun createQuery(): String =
          "INSERT OR ABORT INTO `exercise_blocks` (`id`,`workoutId`,`blockTitle`,`blockType`,`order`,`protocol`,`protocolValue`) VALUES (nullif(?, 0),?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: ExerciseBlock) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.workoutId)
        statement.bindText(3, entity.blockTitle)
        statement.bindText(4, entity.blockType)
        statement.bindLong(5, entity.order.toLong())
        statement.bindText(6, entity.protocol)
        statement.bindText(7, entity.protocolValue)
      }
    }
    this.__insertAdapterOfTemplateExercise = object : EntityInsertAdapter<TemplateExercise>() {
      protected override fun createQuery(): String =
          "INSERT OR ABORT INTO `template_exercises` (`id`,`blockId`,`exerciseName`,`order`,`targetSets`,`targetReps`,`targetWeight`,`note`,`metricType`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: TemplateExercise) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.blockId)
        statement.bindText(3, entity.exerciseName)
        statement.bindLong(4, entity.order.toLong())
        statement.bindLong(5, entity.targetSets.toLong())
        statement.bindText(6, entity.targetReps)
        val _tmpTargetWeight: Double? = entity.targetWeight
        if (_tmpTargetWeight == null) {
          statement.bindNull(7)
        } else {
          statement.bindDouble(7, _tmpTargetWeight)
        }
        val _tmpNote: String? = entity.note
        if (_tmpNote == null) {
          statement.bindNull(8)
        } else {
          statement.bindText(8, _tmpNote)
        }
        statement.bindText(9, entity.metricType)
      }
    }
    this.__insertAdapterOfClientProgram = object : EntityInsertAdapter<ClientProgram>() {
      protected override fun createQuery(): String =
          "INSERT OR ABORT INTO `client_programs` (`id`,`clientId`,`macrocycleId`,`name`,`weekNumber`,`totalWeeks`,`status`) VALUES (nullif(?, 0),?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: ClientProgram) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.clientId)
        statement.bindLong(3, entity.macrocycleId)
        statement.bindText(4, entity.name)
        statement.bindLong(5, entity.weekNumber.toLong())
        statement.bindLong(6, entity.totalWeeks.toLong())
        statement.bindText(7, entity.status)
      }
    }
    this.__insertAdapterOfClientWorkout = object : EntityInsertAdapter<ClientWorkout>() {
      protected override fun createQuery(): String =
          "INSERT OR ABORT INTO `client_workouts` (`id`,`clientProgramId`,`originalWorkoutId`,`weekNumber`,`dayNumber`,`dayType`,`name`,`status`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: ClientWorkout) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.clientProgramId)
        statement.bindLong(3, entity.originalWorkoutId)
        statement.bindLong(4, entity.weekNumber.toLong())
        statement.bindLong(5, entity.dayNumber.toLong())
        statement.bindText(6, entity.dayType)
        statement.bindText(7, entity.name)
        statement.bindText(8, entity.status)
      }
    }
    this.__insertAdapterOfClientExercise = object : EntityInsertAdapter<ClientExercise>() {
      protected override fun createQuery(): String =
          "INSERT OR ABORT INTO `client_exercises` (`id`,`clientWorkoutId`,`exerciseName`,`order`,`blockTitle`,`blockType`,`blockOrder`,`targetSets`,`targetReps`,`targetWeight`,`note`,`metricType`,`protocol`,`protocolValue`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: ClientExercise) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.clientWorkoutId)
        statement.bindText(3, entity.exerciseName)
        statement.bindLong(4, entity.order.toLong())
        statement.bindText(5, entity.blockTitle)
        statement.bindText(6, entity.blockType)
        statement.bindLong(7, entity.blockOrder.toLong())
        statement.bindLong(8, entity.targetSets.toLong())
        statement.bindText(9, entity.targetReps)
        val _tmpTargetWeight: Double? = entity.targetWeight
        if (_tmpTargetWeight == null) {
          statement.bindNull(10)
        } else {
          statement.bindDouble(10, _tmpTargetWeight)
        }
        val _tmpNote: String? = entity.note
        if (_tmpNote == null) {
          statement.bindNull(11)
        } else {
          statement.bindText(11, _tmpNote)
        }
        statement.bindText(12, entity.metricType)
        statement.bindText(13, entity.protocol)
        statement.bindText(14, entity.protocolValue)
      }
    }
    this.__insertAdapterOfExerciseLog = object : EntityInsertAdapter<ExerciseLog>() {
      protected override fun createQuery(): String =
          "INSERT OR ABORT INTO `exercise_logs` (`id`,`clientExerciseId`,`setNumber`,`actualReps`,`actualWeight`,`resultText`,`note`,`date`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: ExerciseLog) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.clientExerciseId)
        statement.bindLong(3, entity.setNumber.toLong())
        statement.bindLong(4, entity.actualReps.toLong())
        statement.bindDouble(5, entity.actualWeight)
        val _tmpResultText: String? = entity.resultText
        if (_tmpResultText == null) {
          statement.bindNull(6)
        } else {
          statement.bindText(6, _tmpResultText)
        }
        val _tmpNote: String? = entity.note
        if (_tmpNote == null) {
          statement.bindNull(7)
        } else {
          statement.bindText(7, _tmpNote)
        }
        statement.bindLong(8, entity.date)
      }
    }
    this.__deleteAdapterOfClientMeasurement = object :
        EntityDeleteOrUpdateAdapter<ClientMeasurement>() {
      protected override fun createQuery(): String = "DELETE FROM `measurements` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: ClientMeasurement) {
        statement.bindLong(1, entity.id)
      }
    }
    this.__updateAdapterOfMacrocycle = object : EntityDeleteOrUpdateAdapter<Macrocycle>() {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `macrocycles` SET `id` = ?,`name` = ?,`durationWeeks` = ?,`sessionsPerWeek` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: Macrocycle) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.name)
        statement.bindLong(3, entity.durationWeeks.toLong())
        statement.bindLong(4, entity.sessionsPerWeek.toLong())
        statement.bindLong(5, entity.id)
      }
    }
    this.__updateAdapterOfTemplateExercise = object :
        EntityDeleteOrUpdateAdapter<TemplateExercise>() {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `template_exercises` SET `id` = ?,`blockId` = ?,`exerciseName` = ?,`order` = ?,`targetSets` = ?,`targetReps` = ?,`targetWeight` = ?,`note` = ?,`metricType` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: TemplateExercise) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.blockId)
        statement.bindText(3, entity.exerciseName)
        statement.bindLong(4, entity.order.toLong())
        statement.bindLong(5, entity.targetSets.toLong())
        statement.bindText(6, entity.targetReps)
        val _tmpTargetWeight: Double? = entity.targetWeight
        if (_tmpTargetWeight == null) {
          statement.bindNull(7)
        } else {
          statement.bindDouble(7, _tmpTargetWeight)
        }
        val _tmpNote: String? = entity.note
        if (_tmpNote == null) {
          statement.bindNull(8)
        } else {
          statement.bindText(8, _tmpNote)
        }
        statement.bindText(9, entity.metricType)
        statement.bindLong(10, entity.id)
      }
    }
  }

  public override suspend fun insertClient(client: Client): Long = performSuspending(__db, false,
      true) { _connection ->
    val _result: Long = __insertAdapterOfClient.insertAndReturnId(_connection, client)
    _result
  }

  public override suspend fun insertMeasurement(m: ClientMeasurement): Unit =
      performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfClientMeasurement.insert(_connection, m)
  }

  public override suspend fun insertMacrocycle(macrocycle: Macrocycle): Long =
      performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfMacrocycle.insertAndReturnId(_connection, macrocycle)
    _result
  }

  public override suspend fun insertWorkout(workout: Workout): Long = performSuspending(__db, false,
      true) { _connection ->
    val _result: Long = __insertAdapterOfWorkout.insertAndReturnId(_connection, workout)
    _result
  }

  public override suspend fun insertExerciseBlock(block: ExerciseBlock): Long =
      performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfExerciseBlock.insertAndReturnId(_connection, block)
    _result
  }

  public override suspend fun insertTemplateExercise(exercise: TemplateExercise): Long =
      performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfTemplateExercise.insertAndReturnId(_connection, exercise)
    _result
  }

  public override suspend fun insertClientProgram(program: ClientProgram): Long =
      performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfClientProgram.insertAndReturnId(_connection, program)
    _result
  }

  public override suspend fun insertClientWorkout(workout: ClientWorkout): Long =
      performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfClientWorkout.insertAndReturnId(_connection, workout)
    _result
  }

  public override suspend fun insertClientExercise(exercise: ClientExercise): Long =
      performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfClientExercise.insertAndReturnId(_connection, exercise)
    _result
  }

  public override suspend fun insertExerciseLog(log: ExerciseLog): Long = performSuspending(__db,
      false, true) { _connection ->
    val _result: Long = __insertAdapterOfExerciseLog.insertAndReturnId(_connection, log)
    _result
  }

  public override suspend fun deleteMeasurement(m: ClientMeasurement): Unit =
      performSuspending(__db, false, true) { _connection ->
    __deleteAdapterOfClientMeasurement.handle(_connection, m)
  }

  public override suspend fun updateMacrocycle(macrocycle: Macrocycle): Unit =
      performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfMacrocycle.handle(_connection, macrocycle)
  }

  public override suspend fun updateTemplateExercise(exercise: TemplateExercise): Unit =
      performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfTemplateExercise.handle(_connection, exercise)
  }

  public override fun getAllClients(): Flow<List<Client>> {
    val _sql: String = "SELECT * FROM clients ORDER BY name ASC"
    return createFlow(__db, false, arrayOf("clients")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfGoal: Int = getColumnIndexOrThrow(_stmt, "goal")
        val _columnIndexOfNotes: Int = getColumnIndexOrThrow(_stmt, "notes")
        val _result: MutableList<Client> = mutableListOf()
        while (_stmt.step()) {
          val _item: Client
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpGoal: String?
          if (_stmt.isNull(_columnIndexOfGoal)) {
            _tmpGoal = null
          } else {
            _tmpGoal = _stmt.getText(_columnIndexOfGoal)
          }
          val _tmpNotes: String?
          if (_stmt.isNull(_columnIndexOfNotes)) {
            _tmpNotes = null
          } else {
            _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          }
          _item = Client(_tmpId,_tmpName,_tmpGoal,_tmpNotes)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getClientSync(clientId: Long): Client? {
    val _sql: String = "SELECT * FROM clients WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, clientId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfGoal: Int = getColumnIndexOrThrow(_stmt, "goal")
        val _columnIndexOfNotes: Int = getColumnIndexOrThrow(_stmt, "notes")
        val _result: Client?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpGoal: String?
          if (_stmt.isNull(_columnIndexOfGoal)) {
            _tmpGoal = null
          } else {
            _tmpGoal = _stmt.getText(_columnIndexOfGoal)
          }
          val _tmpNotes: String?
          if (_stmt.isNull(_columnIndexOfNotes)) {
            _tmpNotes = null
          } else {
            _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          }
          _result = Client(_tmpId,_tmpName,_tmpGoal,_tmpNotes)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getMeasurements(clientId: Long): Flow<List<ClientMeasurement>> {
    val _sql: String = "SELECT * FROM measurements WHERE clientId = ? ORDER BY date DESC"
    return createFlow(__db, false, arrayOf("measurements")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, clientId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfClientId: Int = getColumnIndexOrThrow(_stmt, "clientId")
        val _columnIndexOfDate: Int = getColumnIndexOrThrow(_stmt, "date")
        val _columnIndexOfWeightKg: Int = getColumnIndexOrThrow(_stmt, "weightKg")
        val _columnIndexOfHeightCm: Int = getColumnIndexOrThrow(_stmt, "heightCm")
        val _columnIndexOfChest: Int = getColumnIndexOrThrow(_stmt, "chest")
        val _columnIndexOfWaist: Int = getColumnIndexOrThrow(_stmt, "waist")
        val _columnIndexOfHips: Int = getColumnIndexOrThrow(_stmt, "hips")
        val _columnIndexOfArm: Int = getColumnIndexOrThrow(_stmt, "arm")
        val _columnIndexOfThigh: Int = getColumnIndexOrThrow(_stmt, "thigh")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _result: MutableList<ClientMeasurement> = mutableListOf()
        while (_stmt.step()) {
          val _item: ClientMeasurement
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpClientId: Long
          _tmpClientId = _stmt.getLong(_columnIndexOfClientId)
          val _tmpDate: Long
          _tmpDate = _stmt.getLong(_columnIndexOfDate)
          val _tmpWeightKg: Double?
          if (_stmt.isNull(_columnIndexOfWeightKg)) {
            _tmpWeightKg = null
          } else {
            _tmpWeightKg = _stmt.getDouble(_columnIndexOfWeightKg)
          }
          val _tmpHeightCm: Double?
          if (_stmt.isNull(_columnIndexOfHeightCm)) {
            _tmpHeightCm = null
          } else {
            _tmpHeightCm = _stmt.getDouble(_columnIndexOfHeightCm)
          }
          val _tmpChest: Double?
          if (_stmt.isNull(_columnIndexOfChest)) {
            _tmpChest = null
          } else {
            _tmpChest = _stmt.getDouble(_columnIndexOfChest)
          }
          val _tmpWaist: Double?
          if (_stmt.isNull(_columnIndexOfWaist)) {
            _tmpWaist = null
          } else {
            _tmpWaist = _stmt.getDouble(_columnIndexOfWaist)
          }
          val _tmpHips: Double?
          if (_stmt.isNull(_columnIndexOfHips)) {
            _tmpHips = null
          } else {
            _tmpHips = _stmt.getDouble(_columnIndexOfHips)
          }
          val _tmpArm: Double?
          if (_stmt.isNull(_columnIndexOfArm)) {
            _tmpArm = null
          } else {
            _tmpArm = _stmt.getDouble(_columnIndexOfArm)
          }
          val _tmpThigh: Double?
          if (_stmt.isNull(_columnIndexOfThigh)) {
            _tmpThigh = null
          } else {
            _tmpThigh = _stmt.getDouble(_columnIndexOfThigh)
          }
          val _tmpNote: String?
          if (_stmt.isNull(_columnIndexOfNote)) {
            _tmpNote = null
          } else {
            _tmpNote = _stmt.getText(_columnIndexOfNote)
          }
          _item =
              ClientMeasurement(_tmpId,_tmpClientId,_tmpDate,_tmpWeightKg,_tmpHeightCm,_tmpChest,_tmpWaist,_tmpHips,_tmpArm,_tmpThigh,_tmpNote)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getAllMacrocycles(): Flow<List<Macrocycle>> {
    val _sql: String = "SELECT * FROM macrocycles"
    return createFlow(__db, false, arrayOf("macrocycles")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfDurationWeeks: Int = getColumnIndexOrThrow(_stmt, "durationWeeks")
        val _columnIndexOfSessionsPerWeek: Int = getColumnIndexOrThrow(_stmt, "sessionsPerWeek")
        val _result: MutableList<Macrocycle> = mutableListOf()
        while (_stmt.step()) {
          val _item: Macrocycle
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpDurationWeeks: Int
          _tmpDurationWeeks = _stmt.getLong(_columnIndexOfDurationWeeks).toInt()
          val _tmpSessionsPerWeek: Int
          _tmpSessionsPerWeek = _stmt.getLong(_columnIndexOfSessionsPerWeek).toInt()
          _item = Macrocycle(_tmpId,_tmpName,_tmpDurationWeeks,_tmpSessionsPerWeek)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getMacroSync(macroId: Long): Macrocycle? {
    val _sql: String = "SELECT * FROM macrocycles WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, macroId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfDurationWeeks: Int = getColumnIndexOrThrow(_stmt, "durationWeeks")
        val _columnIndexOfSessionsPerWeek: Int = getColumnIndexOrThrow(_stmt, "sessionsPerWeek")
        val _result: Macrocycle?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpDurationWeeks: Int
          _tmpDurationWeeks = _stmt.getLong(_columnIndexOfDurationWeeks).toInt()
          val _tmpSessionsPerWeek: Int
          _tmpSessionsPerWeek = _stmt.getLong(_columnIndexOfSessionsPerWeek).toInt()
          _result = Macrocycle(_tmpId,_tmpName,_tmpDurationWeeks,_tmpSessionsPerWeek)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getWorkoutsForMacro(macroId: Long): Flow<List<Workout>> {
    val _sql: String =
        "SELECT * FROM workouts WHERE mesocycleId = ? ORDER BY weekNumber ASC, dayNumber ASC"
    return createFlow(__db, false, arrayOf("workouts")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, macroId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfMesocycleId: Int = getColumnIndexOrThrow(_stmt, "mesocycleId")
        val _columnIndexOfWeekNumber: Int = getColumnIndexOrThrow(_stmt, "weekNumber")
        val _columnIndexOfDayNumber: Int = getColumnIndexOrThrow(_stmt, "dayNumber")
        val _columnIndexOfDayType: Int = getColumnIndexOrThrow(_stmt, "dayType")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _result: MutableList<Workout> = mutableListOf()
        while (_stmt.step()) {
          val _item: Workout
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpMesocycleId: Long
          _tmpMesocycleId = _stmt.getLong(_columnIndexOfMesocycleId)
          val _tmpWeekNumber: Int
          _tmpWeekNumber = _stmt.getLong(_columnIndexOfWeekNumber).toInt()
          val _tmpDayNumber: Int
          _tmpDayNumber = _stmt.getLong(_columnIndexOfDayNumber).toInt()
          val _tmpDayType: String
          _tmpDayType = _stmt.getText(_columnIndexOfDayType)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          _item = Workout(_tmpId,_tmpMesocycleId,_tmpWeekNumber,_tmpDayNumber,_tmpDayType,_tmpName)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getWorkoutsForMacroSync(macroId: Long): List<Workout> {
    val _sql: String =
        "SELECT * FROM workouts WHERE mesocycleId = ? ORDER BY weekNumber ASC, dayNumber ASC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, macroId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfMesocycleId: Int = getColumnIndexOrThrow(_stmt, "mesocycleId")
        val _columnIndexOfWeekNumber: Int = getColumnIndexOrThrow(_stmt, "weekNumber")
        val _columnIndexOfDayNumber: Int = getColumnIndexOrThrow(_stmt, "dayNumber")
        val _columnIndexOfDayType: Int = getColumnIndexOrThrow(_stmt, "dayType")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _result: MutableList<Workout> = mutableListOf()
        while (_stmt.step()) {
          val _item: Workout
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpMesocycleId: Long
          _tmpMesocycleId = _stmt.getLong(_columnIndexOfMesocycleId)
          val _tmpWeekNumber: Int
          _tmpWeekNumber = _stmt.getLong(_columnIndexOfWeekNumber).toInt()
          val _tmpDayNumber: Int
          _tmpDayNumber = _stmt.getLong(_columnIndexOfDayNumber).toInt()
          val _tmpDayType: String
          _tmpDayType = _stmt.getText(_columnIndexOfDayType)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          _item = Workout(_tmpId,_tmpMesocycleId,_tmpWeekNumber,_tmpDayNumber,_tmpDayType,_tmpName)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getMaxDayNumber(macroId: Long, weekNumber: Int): Int? {
    val _sql: String =
        "SELECT MAX(dayNumber) FROM workouts WHERE mesocycleId = ? AND weekNumber = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, macroId)
        _argIndex = 2
        _stmt.bindLong(_argIndex, weekNumber.toLong())
        val _result: Int?
        if (_stmt.step()) {
          val _tmp: Int?
          if (_stmt.isNull(0)) {
            _tmp = null
          } else {
            _tmp = _stmt.getLong(0).toInt()
          }
          _result = _tmp
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getWeekNumbersForMacroSync(macroId: Long): List<Int> {
    val _sql: String =
        "SELECT DISTINCT weekNumber FROM workouts WHERE mesocycleId = ? ORDER BY weekNumber ASC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, macroId)
        val _result: MutableList<Int> = mutableListOf()
        while (_stmt.step()) {
          val _item: Int
          _item = _stmt.getLong(0).toInt()
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getMaxWeekNumber(macroId: Long): Int? {
    val _sql: String = "SELECT MAX(weekNumber) FROM workouts WHERE mesocycleId = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, macroId)
        val _result: Int?
        if (_stmt.step()) {
          val _tmp: Int?
          if (_stmt.isNull(0)) {
            _tmp = null
          } else {
            _tmp = _stmt.getLong(0).toInt()
          }
          _result = _tmp
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getWorkoutsForMacroWeek(macroId: Long, weekNumber: Int): Flow<List<Workout>> {
    val _sql: String =
        "SELECT * FROM workouts WHERE mesocycleId = ? AND weekNumber = ? ORDER BY dayNumber ASC"
    return createFlow(__db, false, arrayOf("workouts")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, macroId)
        _argIndex = 2
        _stmt.bindLong(_argIndex, weekNumber.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfMesocycleId: Int = getColumnIndexOrThrow(_stmt, "mesocycleId")
        val _columnIndexOfWeekNumber: Int = getColumnIndexOrThrow(_stmt, "weekNumber")
        val _columnIndexOfDayNumber: Int = getColumnIndexOrThrow(_stmt, "dayNumber")
        val _columnIndexOfDayType: Int = getColumnIndexOrThrow(_stmt, "dayType")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _result: MutableList<Workout> = mutableListOf()
        while (_stmt.step()) {
          val _item: Workout
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpMesocycleId: Long
          _tmpMesocycleId = _stmt.getLong(_columnIndexOfMesocycleId)
          val _tmpWeekNumber: Int
          _tmpWeekNumber = _stmt.getLong(_columnIndexOfWeekNumber).toInt()
          val _tmpDayNumber: Int
          _tmpDayNumber = _stmt.getLong(_columnIndexOfDayNumber).toInt()
          val _tmpDayType: String
          _tmpDayType = _stmt.getText(_columnIndexOfDayType)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          _item = Workout(_tmpId,_tmpMesocycleId,_tmpWeekNumber,_tmpDayNumber,_tmpDayType,_tmpName)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getWorkoutsForMacroWeekSync(macroId: Long, weekNumber: Int):
      List<Workout> {
    val _sql: String =
        "SELECT * FROM workouts WHERE mesocycleId = ? AND weekNumber = ? ORDER BY dayNumber ASC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, macroId)
        _argIndex = 2
        _stmt.bindLong(_argIndex, weekNumber.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfMesocycleId: Int = getColumnIndexOrThrow(_stmt, "mesocycleId")
        val _columnIndexOfWeekNumber: Int = getColumnIndexOrThrow(_stmt, "weekNumber")
        val _columnIndexOfDayNumber: Int = getColumnIndexOrThrow(_stmt, "dayNumber")
        val _columnIndexOfDayType: Int = getColumnIndexOrThrow(_stmt, "dayType")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _result: MutableList<Workout> = mutableListOf()
        while (_stmt.step()) {
          val _item: Workout
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpMesocycleId: Long
          _tmpMesocycleId = _stmt.getLong(_columnIndexOfMesocycleId)
          val _tmpWeekNumber: Int
          _tmpWeekNumber = _stmt.getLong(_columnIndexOfWeekNumber).toInt()
          val _tmpDayNumber: Int
          _tmpDayNumber = _stmt.getLong(_columnIndexOfDayNumber).toInt()
          val _tmpDayType: String
          _tmpDayType = _stmt.getText(_columnIndexOfDayType)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          _item = Workout(_tmpId,_tmpMesocycleId,_tmpWeekNumber,_tmpDayNumber,_tmpDayType,_tmpName)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun countWorkoutsInWeek(macroId: Long, weekNumber: Int): Int {
    val _sql: String = "SELECT COUNT(*) FROM workouts WHERE mesocycleId = ? AND weekNumber = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, macroId)
        _argIndex = 2
        _stmt.bindLong(_argIndex, weekNumber.toLong())
        val _result: Int
        if (_stmt.step()) {
          val _tmp: Int
          _tmp = _stmt.getLong(0).toInt()
          _result = _tmp
        } else {
          _result = 0
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getBlocksForWorkoutSync(workoutId: Long): List<ExerciseBlock> {
    val _sql: String =
        "SELECT * FROM exercise_blocks WHERE workoutId = ? ORDER BY `order` ASC, id ASC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, workoutId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfWorkoutId: Int = getColumnIndexOrThrow(_stmt, "workoutId")
        val _columnIndexOfBlockTitle: Int = getColumnIndexOrThrow(_stmt, "blockTitle")
        val _columnIndexOfBlockType: Int = getColumnIndexOrThrow(_stmt, "blockType")
        val _columnIndexOfOrder: Int = getColumnIndexOrThrow(_stmt, "order")
        val _columnIndexOfProtocol: Int = getColumnIndexOrThrow(_stmt, "protocol")
        val _columnIndexOfProtocolValue: Int = getColumnIndexOrThrow(_stmt, "protocolValue")
        val _result: MutableList<ExerciseBlock> = mutableListOf()
        while (_stmt.step()) {
          val _item: ExerciseBlock
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpWorkoutId: Long
          _tmpWorkoutId = _stmt.getLong(_columnIndexOfWorkoutId)
          val _tmpBlockTitle: String
          _tmpBlockTitle = _stmt.getText(_columnIndexOfBlockTitle)
          val _tmpBlockType: String
          _tmpBlockType = _stmt.getText(_columnIndexOfBlockType)
          val _tmpOrder: Int
          _tmpOrder = _stmt.getLong(_columnIndexOfOrder).toInt()
          val _tmpProtocol: String
          _tmpProtocol = _stmt.getText(_columnIndexOfProtocol)
          val _tmpProtocolValue: String
          _tmpProtocolValue = _stmt.getText(_columnIndexOfProtocolValue)
          _item =
              ExerciseBlock(_tmpId,_tmpWorkoutId,_tmpBlockTitle,_tmpBlockType,_tmpOrder,_tmpProtocol,_tmpProtocolValue)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getMaxBlockOrder(workoutId: Long): Int? {
    val _sql: String = "SELECT MAX(`order`) FROM exercise_blocks WHERE workoutId = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, workoutId)
        val _result: Int?
        if (_stmt.step()) {
          val _tmp: Int?
          if (_stmt.isNull(0)) {
            _tmp = null
          } else {
            _tmp = _stmt.getLong(0).toInt()
          }
          _result = _tmp
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getExercisesForBlock(blockId: Long): Flow<List<TemplateExercise>> {
    val _sql: String =
        "SELECT * FROM template_exercises WHERE blockId = ? ORDER BY `order` ASC, id ASC"
    return createFlow(__db, false, arrayOf("template_exercises")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, blockId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfBlockId: Int = getColumnIndexOrThrow(_stmt, "blockId")
        val _columnIndexOfExerciseName: Int = getColumnIndexOrThrow(_stmt, "exerciseName")
        val _columnIndexOfOrder: Int = getColumnIndexOrThrow(_stmt, "order")
        val _columnIndexOfTargetSets: Int = getColumnIndexOrThrow(_stmt, "targetSets")
        val _columnIndexOfTargetReps: Int = getColumnIndexOrThrow(_stmt, "targetReps")
        val _columnIndexOfTargetWeight: Int = getColumnIndexOrThrow(_stmt, "targetWeight")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _columnIndexOfMetricType: Int = getColumnIndexOrThrow(_stmt, "metricType")
        val _result: MutableList<TemplateExercise> = mutableListOf()
        while (_stmt.step()) {
          val _item: TemplateExercise
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpBlockId: Long
          _tmpBlockId = _stmt.getLong(_columnIndexOfBlockId)
          val _tmpExerciseName: String
          _tmpExerciseName = _stmt.getText(_columnIndexOfExerciseName)
          val _tmpOrder: Int
          _tmpOrder = _stmt.getLong(_columnIndexOfOrder).toInt()
          val _tmpTargetSets: Int
          _tmpTargetSets = _stmt.getLong(_columnIndexOfTargetSets).toInt()
          val _tmpTargetReps: String
          _tmpTargetReps = _stmt.getText(_columnIndexOfTargetReps)
          val _tmpTargetWeight: Double?
          if (_stmt.isNull(_columnIndexOfTargetWeight)) {
            _tmpTargetWeight = null
          } else {
            _tmpTargetWeight = _stmt.getDouble(_columnIndexOfTargetWeight)
          }
          val _tmpNote: String?
          if (_stmt.isNull(_columnIndexOfNote)) {
            _tmpNote = null
          } else {
            _tmpNote = _stmt.getText(_columnIndexOfNote)
          }
          val _tmpMetricType: String
          _tmpMetricType = _stmt.getText(_columnIndexOfMetricType)
          _item =
              TemplateExercise(_tmpId,_tmpBlockId,_tmpExerciseName,_tmpOrder,_tmpTargetSets,_tmpTargetReps,_tmpTargetWeight,_tmpNote,_tmpMetricType)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getExercisesForBlockSync(blockId: Long): List<TemplateExercise> {
    val _sql: String =
        "SELECT * FROM template_exercises WHERE blockId = ? ORDER BY `order` ASC, id ASC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, blockId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfBlockId: Int = getColumnIndexOrThrow(_stmt, "blockId")
        val _columnIndexOfExerciseName: Int = getColumnIndexOrThrow(_stmt, "exerciseName")
        val _columnIndexOfOrder: Int = getColumnIndexOrThrow(_stmt, "order")
        val _columnIndexOfTargetSets: Int = getColumnIndexOrThrow(_stmt, "targetSets")
        val _columnIndexOfTargetReps: Int = getColumnIndexOrThrow(_stmt, "targetReps")
        val _columnIndexOfTargetWeight: Int = getColumnIndexOrThrow(_stmt, "targetWeight")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _columnIndexOfMetricType: Int = getColumnIndexOrThrow(_stmt, "metricType")
        val _result: MutableList<TemplateExercise> = mutableListOf()
        while (_stmt.step()) {
          val _item: TemplateExercise
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpBlockId: Long
          _tmpBlockId = _stmt.getLong(_columnIndexOfBlockId)
          val _tmpExerciseName: String
          _tmpExerciseName = _stmt.getText(_columnIndexOfExerciseName)
          val _tmpOrder: Int
          _tmpOrder = _stmt.getLong(_columnIndexOfOrder).toInt()
          val _tmpTargetSets: Int
          _tmpTargetSets = _stmt.getLong(_columnIndexOfTargetSets).toInt()
          val _tmpTargetReps: String
          _tmpTargetReps = _stmt.getText(_columnIndexOfTargetReps)
          val _tmpTargetWeight: Double?
          if (_stmt.isNull(_columnIndexOfTargetWeight)) {
            _tmpTargetWeight = null
          } else {
            _tmpTargetWeight = _stmt.getDouble(_columnIndexOfTargetWeight)
          }
          val _tmpNote: String?
          if (_stmt.isNull(_columnIndexOfNote)) {
            _tmpNote = null
          } else {
            _tmpNote = _stmt.getText(_columnIndexOfNote)
          }
          val _tmpMetricType: String
          _tmpMetricType = _stmt.getText(_columnIndexOfMetricType)
          _item =
              TemplateExercise(_tmpId,_tmpBlockId,_tmpExerciseName,_tmpOrder,_tmpTargetSets,_tmpTargetReps,_tmpTargetWeight,_tmpNote,_tmpMetricType)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getMaxExerciseOrder(blockId: Long): Int? {
    val _sql: String = "SELECT MAX(`order`) FROM template_exercises WHERE blockId = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, blockId)
        val _result: Int?
        if (_stmt.step()) {
          val _tmp: Int?
          if (_stmt.isNull(0)) {
            _tmp = null
          } else {
            _tmp = _stmt.getLong(0).toInt()
          }
          _result = _tmp
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getProgramsForClient(clientId: Long): Flow<List<ClientProgram>> {
    val _sql: String = "SELECT * FROM client_programs WHERE clientId = ? ORDER BY weekNumber ASC"
    return createFlow(__db, false, arrayOf("client_programs")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, clientId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfClientId: Int = getColumnIndexOrThrow(_stmt, "clientId")
        val _columnIndexOfMacrocycleId: Int = getColumnIndexOrThrow(_stmt, "macrocycleId")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfWeekNumber: Int = getColumnIndexOrThrow(_stmt, "weekNumber")
        val _columnIndexOfTotalWeeks: Int = getColumnIndexOrThrow(_stmt, "totalWeeks")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _result: MutableList<ClientProgram> = mutableListOf()
        while (_stmt.step()) {
          val _item: ClientProgram
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpClientId: Long
          _tmpClientId = _stmt.getLong(_columnIndexOfClientId)
          val _tmpMacrocycleId: Long
          _tmpMacrocycleId = _stmt.getLong(_columnIndexOfMacrocycleId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpWeekNumber: Int
          _tmpWeekNumber = _stmt.getLong(_columnIndexOfWeekNumber).toInt()
          val _tmpTotalWeeks: Int
          _tmpTotalWeeks = _stmt.getLong(_columnIndexOfTotalWeeks).toInt()
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          _item =
              ClientProgram(_tmpId,_tmpClientId,_tmpMacrocycleId,_tmpName,_tmpWeekNumber,_tmpTotalWeeks,_tmpStatus)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getProgramsForClientSync(clientId: Long): List<ClientProgram> {
    val _sql: String = "SELECT * FROM client_programs WHERE clientId = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, clientId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfClientId: Int = getColumnIndexOrThrow(_stmt, "clientId")
        val _columnIndexOfMacrocycleId: Int = getColumnIndexOrThrow(_stmt, "macrocycleId")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfWeekNumber: Int = getColumnIndexOrThrow(_stmt, "weekNumber")
        val _columnIndexOfTotalWeeks: Int = getColumnIndexOrThrow(_stmt, "totalWeeks")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _result: MutableList<ClientProgram> = mutableListOf()
        while (_stmt.step()) {
          val _item: ClientProgram
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpClientId: Long
          _tmpClientId = _stmt.getLong(_columnIndexOfClientId)
          val _tmpMacrocycleId: Long
          _tmpMacrocycleId = _stmt.getLong(_columnIndexOfMacrocycleId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpWeekNumber: Int
          _tmpWeekNumber = _stmt.getLong(_columnIndexOfWeekNumber).toInt()
          val _tmpTotalWeeks: Int
          _tmpTotalWeeks = _stmt.getLong(_columnIndexOfTotalWeeks).toInt()
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          _item =
              ClientProgram(_tmpId,_tmpClientId,_tmpMacrocycleId,_tmpName,_tmpWeekNumber,_tmpTotalWeeks,_tmpStatus)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getClientProgramSync(programId: Long): ClientProgram? {
    val _sql: String = "SELECT * FROM client_programs WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, programId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfClientId: Int = getColumnIndexOrThrow(_stmt, "clientId")
        val _columnIndexOfMacrocycleId: Int = getColumnIndexOrThrow(_stmt, "macrocycleId")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfWeekNumber: Int = getColumnIndexOrThrow(_stmt, "weekNumber")
        val _columnIndexOfTotalWeeks: Int = getColumnIndexOrThrow(_stmt, "totalWeeks")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _result: ClientProgram?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpClientId: Long
          _tmpClientId = _stmt.getLong(_columnIndexOfClientId)
          val _tmpMacrocycleId: Long
          _tmpMacrocycleId = _stmt.getLong(_columnIndexOfMacrocycleId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpWeekNumber: Int
          _tmpWeekNumber = _stmt.getLong(_columnIndexOfWeekNumber).toInt()
          val _tmpTotalWeeks: Int
          _tmpTotalWeeks = _stmt.getLong(_columnIndexOfTotalWeeks).toInt()
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          _result =
              ClientProgram(_tmpId,_tmpClientId,_tmpMacrocycleId,_tmpName,_tmpWeekNumber,_tmpTotalWeeks,_tmpStatus)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getClientWorkoutsForProgram(programId: Long): Flow<List<ClientWorkout>> {
    val _sql: String =
        "SELECT * FROM client_workouts WHERE clientProgramId = ? ORDER BY weekNumber ASC, dayNumber ASC"
    return createFlow(__db, false, arrayOf("client_workouts")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, programId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfClientProgramId: Int = getColumnIndexOrThrow(_stmt, "clientProgramId")
        val _columnIndexOfOriginalWorkoutId: Int = getColumnIndexOrThrow(_stmt, "originalWorkoutId")
        val _columnIndexOfWeekNumber: Int = getColumnIndexOrThrow(_stmt, "weekNumber")
        val _columnIndexOfDayNumber: Int = getColumnIndexOrThrow(_stmt, "dayNumber")
        val _columnIndexOfDayType: Int = getColumnIndexOrThrow(_stmt, "dayType")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _result: MutableList<ClientWorkout> = mutableListOf()
        while (_stmt.step()) {
          val _item: ClientWorkout
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpClientProgramId: Long
          _tmpClientProgramId = _stmt.getLong(_columnIndexOfClientProgramId)
          val _tmpOriginalWorkoutId: Long
          _tmpOriginalWorkoutId = _stmt.getLong(_columnIndexOfOriginalWorkoutId)
          val _tmpWeekNumber: Int
          _tmpWeekNumber = _stmt.getLong(_columnIndexOfWeekNumber).toInt()
          val _tmpDayNumber: Int
          _tmpDayNumber = _stmt.getLong(_columnIndexOfDayNumber).toInt()
          val _tmpDayType: String
          _tmpDayType = _stmt.getText(_columnIndexOfDayType)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          _item =
              ClientWorkout(_tmpId,_tmpClientProgramId,_tmpOriginalWorkoutId,_tmpWeekNumber,_tmpDayNumber,_tmpDayType,_tmpName,_tmpStatus)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getClientWorkoutsForProgramWeek(programId: Long, weekNumber: Int):
      Flow<List<ClientWorkout>> {
    val _sql: String =
        "SELECT * FROM client_workouts WHERE clientProgramId = ? AND weekNumber = ? ORDER BY dayNumber ASC"
    return createFlow(__db, false, arrayOf("client_workouts")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, programId)
        _argIndex = 2
        _stmt.bindLong(_argIndex, weekNumber.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfClientProgramId: Int = getColumnIndexOrThrow(_stmt, "clientProgramId")
        val _columnIndexOfOriginalWorkoutId: Int = getColumnIndexOrThrow(_stmt, "originalWorkoutId")
        val _columnIndexOfWeekNumber: Int = getColumnIndexOrThrow(_stmt, "weekNumber")
        val _columnIndexOfDayNumber: Int = getColumnIndexOrThrow(_stmt, "dayNumber")
        val _columnIndexOfDayType: Int = getColumnIndexOrThrow(_stmt, "dayType")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _result: MutableList<ClientWorkout> = mutableListOf()
        while (_stmt.step()) {
          val _item: ClientWorkout
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpClientProgramId: Long
          _tmpClientProgramId = _stmt.getLong(_columnIndexOfClientProgramId)
          val _tmpOriginalWorkoutId: Long
          _tmpOriginalWorkoutId = _stmt.getLong(_columnIndexOfOriginalWorkoutId)
          val _tmpWeekNumber: Int
          _tmpWeekNumber = _stmt.getLong(_columnIndexOfWeekNumber).toInt()
          val _tmpDayNumber: Int
          _tmpDayNumber = _stmt.getLong(_columnIndexOfDayNumber).toInt()
          val _tmpDayType: String
          _tmpDayType = _stmt.getText(_columnIndexOfDayType)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          _item =
              ClientWorkout(_tmpId,_tmpClientProgramId,_tmpOriginalWorkoutId,_tmpWeekNumber,_tmpDayNumber,_tmpDayType,_tmpName,_tmpStatus)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getClientWorkoutsForProgramSync(programId: Long):
      List<ClientWorkout> {
    val _sql: String =
        "SELECT * FROM client_workouts WHERE clientProgramId = ? ORDER BY weekNumber ASC, dayNumber ASC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, programId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfClientProgramId: Int = getColumnIndexOrThrow(_stmt, "clientProgramId")
        val _columnIndexOfOriginalWorkoutId: Int = getColumnIndexOrThrow(_stmt, "originalWorkoutId")
        val _columnIndexOfWeekNumber: Int = getColumnIndexOrThrow(_stmt, "weekNumber")
        val _columnIndexOfDayNumber: Int = getColumnIndexOrThrow(_stmt, "dayNumber")
        val _columnIndexOfDayType: Int = getColumnIndexOrThrow(_stmt, "dayType")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _result: MutableList<ClientWorkout> = mutableListOf()
        while (_stmt.step()) {
          val _item: ClientWorkout
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpClientProgramId: Long
          _tmpClientProgramId = _stmt.getLong(_columnIndexOfClientProgramId)
          val _tmpOriginalWorkoutId: Long
          _tmpOriginalWorkoutId = _stmt.getLong(_columnIndexOfOriginalWorkoutId)
          val _tmpWeekNumber: Int
          _tmpWeekNumber = _stmt.getLong(_columnIndexOfWeekNumber).toInt()
          val _tmpDayNumber: Int
          _tmpDayNumber = _stmt.getLong(_columnIndexOfDayNumber).toInt()
          val _tmpDayType: String
          _tmpDayType = _stmt.getText(_columnIndexOfDayType)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          _item =
              ClientWorkout(_tmpId,_tmpClientProgramId,_tmpOriginalWorkoutId,_tmpWeekNumber,_tmpDayNumber,_tmpDayType,_tmpName,_tmpStatus)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getClientWorkoutSync(workoutId: Long): ClientWorkout? {
    val _sql: String = "SELECT * FROM client_workouts WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, workoutId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfClientProgramId: Int = getColumnIndexOrThrow(_stmt, "clientProgramId")
        val _columnIndexOfOriginalWorkoutId: Int = getColumnIndexOrThrow(_stmt, "originalWorkoutId")
        val _columnIndexOfWeekNumber: Int = getColumnIndexOrThrow(_stmt, "weekNumber")
        val _columnIndexOfDayNumber: Int = getColumnIndexOrThrow(_stmt, "dayNumber")
        val _columnIndexOfDayType: Int = getColumnIndexOrThrow(_stmt, "dayType")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _result: ClientWorkout?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpClientProgramId: Long
          _tmpClientProgramId = _stmt.getLong(_columnIndexOfClientProgramId)
          val _tmpOriginalWorkoutId: Long
          _tmpOriginalWorkoutId = _stmt.getLong(_columnIndexOfOriginalWorkoutId)
          val _tmpWeekNumber: Int
          _tmpWeekNumber = _stmt.getLong(_columnIndexOfWeekNumber).toInt()
          val _tmpDayNumber: Int
          _tmpDayNumber = _stmt.getLong(_columnIndexOfDayNumber).toInt()
          val _tmpDayType: String
          _tmpDayType = _stmt.getText(_columnIndexOfDayType)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          _result =
              ClientWorkout(_tmpId,_tmpClientProgramId,_tmpOriginalWorkoutId,_tmpWeekNumber,_tmpDayNumber,_tmpDayType,_tmpName,_tmpStatus)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getNextClientWorkoutSync(clientId: Long): ClientWorkout? {
    val _sql: String = """
        |
        |        SELECT cw.* FROM client_workouts cw
        |        JOIN client_programs cp ON cw.clientProgramId = cp.id
        |        WHERE cp.clientId = ?
        |          AND cp.status != 'DONE'
        |          AND cw.status = 'PLANNED'
        |        ORDER BY cw.weekNumber ASC, cw.dayNumber ASC, cw.id ASC
        |        LIMIT 1
        |    
        """.trimMargin()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, clientId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfClientProgramId: Int = getColumnIndexOrThrow(_stmt, "clientProgramId")
        val _columnIndexOfOriginalWorkoutId: Int = getColumnIndexOrThrow(_stmt, "originalWorkoutId")
        val _columnIndexOfWeekNumber: Int = getColumnIndexOrThrow(_stmt, "weekNumber")
        val _columnIndexOfDayNumber: Int = getColumnIndexOrThrow(_stmt, "dayNumber")
        val _columnIndexOfDayType: Int = getColumnIndexOrThrow(_stmt, "dayType")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _result: ClientWorkout?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpClientProgramId: Long
          _tmpClientProgramId = _stmt.getLong(_columnIndexOfClientProgramId)
          val _tmpOriginalWorkoutId: Long
          _tmpOriginalWorkoutId = _stmt.getLong(_columnIndexOfOriginalWorkoutId)
          val _tmpWeekNumber: Int
          _tmpWeekNumber = _stmt.getLong(_columnIndexOfWeekNumber).toInt()
          val _tmpDayNumber: Int
          _tmpDayNumber = _stmt.getLong(_columnIndexOfDayNumber).toInt()
          val _tmpDayType: String
          _tmpDayType = _stmt.getText(_columnIndexOfDayType)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          _result =
              ClientWorkout(_tmpId,_tmpClientProgramId,_tmpOriginalWorkoutId,_tmpWeekNumber,_tmpDayNumber,_tmpDayType,_tmpName,_tmpStatus)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getClientExercises(workoutId: Long): Flow<List<ClientExercise>> {
    val _sql: String =
        "SELECT * FROM client_exercises WHERE clientWorkoutId = ? ORDER BY blockOrder ASC, `order` ASC, id ASC"
    return createFlow(__db, false, arrayOf("client_exercises")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, workoutId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfClientWorkoutId: Int = getColumnIndexOrThrow(_stmt, "clientWorkoutId")
        val _columnIndexOfExerciseName: Int = getColumnIndexOrThrow(_stmt, "exerciseName")
        val _columnIndexOfOrder: Int = getColumnIndexOrThrow(_stmt, "order")
        val _columnIndexOfBlockTitle: Int = getColumnIndexOrThrow(_stmt, "blockTitle")
        val _columnIndexOfBlockType: Int = getColumnIndexOrThrow(_stmt, "blockType")
        val _columnIndexOfBlockOrder: Int = getColumnIndexOrThrow(_stmt, "blockOrder")
        val _columnIndexOfTargetSets: Int = getColumnIndexOrThrow(_stmt, "targetSets")
        val _columnIndexOfTargetReps: Int = getColumnIndexOrThrow(_stmt, "targetReps")
        val _columnIndexOfTargetWeight: Int = getColumnIndexOrThrow(_stmt, "targetWeight")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _columnIndexOfMetricType: Int = getColumnIndexOrThrow(_stmt, "metricType")
        val _columnIndexOfProtocol: Int = getColumnIndexOrThrow(_stmt, "protocol")
        val _columnIndexOfProtocolValue: Int = getColumnIndexOrThrow(_stmt, "protocolValue")
        val _result: MutableList<ClientExercise> = mutableListOf()
        while (_stmt.step()) {
          val _item: ClientExercise
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpClientWorkoutId: Long
          _tmpClientWorkoutId = _stmt.getLong(_columnIndexOfClientWorkoutId)
          val _tmpExerciseName: String
          _tmpExerciseName = _stmt.getText(_columnIndexOfExerciseName)
          val _tmpOrder: Int
          _tmpOrder = _stmt.getLong(_columnIndexOfOrder).toInt()
          val _tmpBlockTitle: String
          _tmpBlockTitle = _stmt.getText(_columnIndexOfBlockTitle)
          val _tmpBlockType: String
          _tmpBlockType = _stmt.getText(_columnIndexOfBlockType)
          val _tmpBlockOrder: Int
          _tmpBlockOrder = _stmt.getLong(_columnIndexOfBlockOrder).toInt()
          val _tmpTargetSets: Int
          _tmpTargetSets = _stmt.getLong(_columnIndexOfTargetSets).toInt()
          val _tmpTargetReps: String
          _tmpTargetReps = _stmt.getText(_columnIndexOfTargetReps)
          val _tmpTargetWeight: Double?
          if (_stmt.isNull(_columnIndexOfTargetWeight)) {
            _tmpTargetWeight = null
          } else {
            _tmpTargetWeight = _stmt.getDouble(_columnIndexOfTargetWeight)
          }
          val _tmpNote: String?
          if (_stmt.isNull(_columnIndexOfNote)) {
            _tmpNote = null
          } else {
            _tmpNote = _stmt.getText(_columnIndexOfNote)
          }
          val _tmpMetricType: String
          _tmpMetricType = _stmt.getText(_columnIndexOfMetricType)
          val _tmpProtocol: String
          _tmpProtocol = _stmt.getText(_columnIndexOfProtocol)
          val _tmpProtocolValue: String
          _tmpProtocolValue = _stmt.getText(_columnIndexOfProtocolValue)
          _item =
              ClientExercise(_tmpId,_tmpClientWorkoutId,_tmpExerciseName,_tmpOrder,_tmpBlockTitle,_tmpBlockType,_tmpBlockOrder,_tmpTargetSets,_tmpTargetReps,_tmpTargetWeight,_tmpNote,_tmpMetricType,_tmpProtocol,_tmpProtocolValue)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getClientExercisesSync(workoutId: Long): List<ClientExercise> {
    val _sql: String =
        "SELECT * FROM client_exercises WHERE clientWorkoutId = ? ORDER BY blockOrder ASC, `order` ASC, id ASC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, workoutId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfClientWorkoutId: Int = getColumnIndexOrThrow(_stmt, "clientWorkoutId")
        val _columnIndexOfExerciseName: Int = getColumnIndexOrThrow(_stmt, "exerciseName")
        val _columnIndexOfOrder: Int = getColumnIndexOrThrow(_stmt, "order")
        val _columnIndexOfBlockTitle: Int = getColumnIndexOrThrow(_stmt, "blockTitle")
        val _columnIndexOfBlockType: Int = getColumnIndexOrThrow(_stmt, "blockType")
        val _columnIndexOfBlockOrder: Int = getColumnIndexOrThrow(_stmt, "blockOrder")
        val _columnIndexOfTargetSets: Int = getColumnIndexOrThrow(_stmt, "targetSets")
        val _columnIndexOfTargetReps: Int = getColumnIndexOrThrow(_stmt, "targetReps")
        val _columnIndexOfTargetWeight: Int = getColumnIndexOrThrow(_stmt, "targetWeight")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _columnIndexOfMetricType: Int = getColumnIndexOrThrow(_stmt, "metricType")
        val _columnIndexOfProtocol: Int = getColumnIndexOrThrow(_stmt, "protocol")
        val _columnIndexOfProtocolValue: Int = getColumnIndexOrThrow(_stmt, "protocolValue")
        val _result: MutableList<ClientExercise> = mutableListOf()
        while (_stmt.step()) {
          val _item: ClientExercise
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpClientWorkoutId: Long
          _tmpClientWorkoutId = _stmt.getLong(_columnIndexOfClientWorkoutId)
          val _tmpExerciseName: String
          _tmpExerciseName = _stmt.getText(_columnIndexOfExerciseName)
          val _tmpOrder: Int
          _tmpOrder = _stmt.getLong(_columnIndexOfOrder).toInt()
          val _tmpBlockTitle: String
          _tmpBlockTitle = _stmt.getText(_columnIndexOfBlockTitle)
          val _tmpBlockType: String
          _tmpBlockType = _stmt.getText(_columnIndexOfBlockType)
          val _tmpBlockOrder: Int
          _tmpBlockOrder = _stmt.getLong(_columnIndexOfBlockOrder).toInt()
          val _tmpTargetSets: Int
          _tmpTargetSets = _stmt.getLong(_columnIndexOfTargetSets).toInt()
          val _tmpTargetReps: String
          _tmpTargetReps = _stmt.getText(_columnIndexOfTargetReps)
          val _tmpTargetWeight: Double?
          if (_stmt.isNull(_columnIndexOfTargetWeight)) {
            _tmpTargetWeight = null
          } else {
            _tmpTargetWeight = _stmt.getDouble(_columnIndexOfTargetWeight)
          }
          val _tmpNote: String?
          if (_stmt.isNull(_columnIndexOfNote)) {
            _tmpNote = null
          } else {
            _tmpNote = _stmt.getText(_columnIndexOfNote)
          }
          val _tmpMetricType: String
          _tmpMetricType = _stmt.getText(_columnIndexOfMetricType)
          val _tmpProtocol: String
          _tmpProtocol = _stmt.getText(_columnIndexOfProtocol)
          val _tmpProtocolValue: String
          _tmpProtocolValue = _stmt.getText(_columnIndexOfProtocolValue)
          _item =
              ClientExercise(_tmpId,_tmpClientWorkoutId,_tmpExerciseName,_tmpOrder,_tmpBlockTitle,_tmpBlockType,_tmpBlockOrder,_tmpTargetSets,_tmpTargetReps,_tmpTargetWeight,_tmpNote,_tmpMetricType,_tmpProtocol,_tmpProtocolValue)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getLastLogForExercise(clientExerciseId: Long): ExerciseLog? {
    val _sql: String =
        "SELECT * FROM exercise_logs WHERE clientExerciseId = ? ORDER BY date DESC, setNumber ASC LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, clientExerciseId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfClientExerciseId: Int = getColumnIndexOrThrow(_stmt, "clientExerciseId")
        val _columnIndexOfSetNumber: Int = getColumnIndexOrThrow(_stmt, "setNumber")
        val _columnIndexOfActualReps: Int = getColumnIndexOrThrow(_stmt, "actualReps")
        val _columnIndexOfActualWeight: Int = getColumnIndexOrThrow(_stmt, "actualWeight")
        val _columnIndexOfResultText: Int = getColumnIndexOrThrow(_stmt, "resultText")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _columnIndexOfDate: Int = getColumnIndexOrThrow(_stmt, "date")
        val _result: ExerciseLog?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpClientExerciseId: Long
          _tmpClientExerciseId = _stmt.getLong(_columnIndexOfClientExerciseId)
          val _tmpSetNumber: Int
          _tmpSetNumber = _stmt.getLong(_columnIndexOfSetNumber).toInt()
          val _tmpActualReps: Int
          _tmpActualReps = _stmt.getLong(_columnIndexOfActualReps).toInt()
          val _tmpActualWeight: Double
          _tmpActualWeight = _stmt.getDouble(_columnIndexOfActualWeight)
          val _tmpResultText: String?
          if (_stmt.isNull(_columnIndexOfResultText)) {
            _tmpResultText = null
          } else {
            _tmpResultText = _stmt.getText(_columnIndexOfResultText)
          }
          val _tmpNote: String?
          if (_stmt.isNull(_columnIndexOfNote)) {
            _tmpNote = null
          } else {
            _tmpNote = _stmt.getText(_columnIndexOfNote)
          }
          val _tmpDate: Long
          _tmpDate = _stmt.getLong(_columnIndexOfDate)
          _result =
              ExerciseLog(_tmpId,_tmpClientExerciseId,_tmpSetNumber,_tmpActualReps,_tmpActualWeight,_tmpResultText,_tmpNote,_tmpDate)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getLogsForExerciseSync(clientExerciseId: Long): List<ExerciseLog> {
    val _sql: String =
        "SELECT * FROM exercise_logs WHERE clientExerciseId = ? ORDER BY setNumber ASC, date ASC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, clientExerciseId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfClientExerciseId: Int = getColumnIndexOrThrow(_stmt, "clientExerciseId")
        val _columnIndexOfSetNumber: Int = getColumnIndexOrThrow(_stmt, "setNumber")
        val _columnIndexOfActualReps: Int = getColumnIndexOrThrow(_stmt, "actualReps")
        val _columnIndexOfActualWeight: Int = getColumnIndexOrThrow(_stmt, "actualWeight")
        val _columnIndexOfResultText: Int = getColumnIndexOrThrow(_stmt, "resultText")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _columnIndexOfDate: Int = getColumnIndexOrThrow(_stmt, "date")
        val _result: MutableList<ExerciseLog> = mutableListOf()
        while (_stmt.step()) {
          val _item: ExerciseLog
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpClientExerciseId: Long
          _tmpClientExerciseId = _stmt.getLong(_columnIndexOfClientExerciseId)
          val _tmpSetNumber: Int
          _tmpSetNumber = _stmt.getLong(_columnIndexOfSetNumber).toInt()
          val _tmpActualReps: Int
          _tmpActualReps = _stmt.getLong(_columnIndexOfActualReps).toInt()
          val _tmpActualWeight: Double
          _tmpActualWeight = _stmt.getDouble(_columnIndexOfActualWeight)
          val _tmpResultText: String?
          if (_stmt.isNull(_columnIndexOfResultText)) {
            _tmpResultText = null
          } else {
            _tmpResultText = _stmt.getText(_columnIndexOfResultText)
          }
          val _tmpNote: String?
          if (_stmt.isNull(_columnIndexOfNote)) {
            _tmpNote = null
          } else {
            _tmpNote = _stmt.getText(_columnIndexOfNote)
          }
          val _tmpDate: Long
          _tmpDate = _stmt.getLong(_columnIndexOfDate)
          _item =
              ExerciseLog(_tmpId,_tmpClientExerciseId,_tmpSetNumber,_tmpActualReps,_tmpActualWeight,_tmpResultText,_tmpNote,_tmpDate)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getHistoryForExercise(clientId: Long, exerciseName: String):
      List<ExerciseLog> {
    val _sql: String = """
        |
        |        SELECT exercise_logs.* FROM exercise_logs
        |        JOIN client_exercises ON client_exercises.id = exercise_logs.clientExerciseId
        |        JOIN client_workouts ON client_workouts.id = client_exercises.clientWorkoutId
        |        JOIN client_programs ON client_programs.id = client_workouts.clientProgramId
        |        WHERE client_programs.clientId = ? AND client_exercises.exerciseName = ?
        |        ORDER BY exercise_logs.date DESC
        |        LIMIT 30
        |    
        """.trimMargin()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, clientId)
        _argIndex = 2
        _stmt.bindText(_argIndex, exerciseName)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfClientExerciseId: Int = getColumnIndexOrThrow(_stmt, "clientExerciseId")
        val _columnIndexOfSetNumber: Int = getColumnIndexOrThrow(_stmt, "setNumber")
        val _columnIndexOfActualReps: Int = getColumnIndexOrThrow(_stmt, "actualReps")
        val _columnIndexOfActualWeight: Int = getColumnIndexOrThrow(_stmt, "actualWeight")
        val _columnIndexOfResultText: Int = getColumnIndexOrThrow(_stmt, "resultText")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _columnIndexOfDate: Int = getColumnIndexOrThrow(_stmt, "date")
        val _result: MutableList<ExerciseLog> = mutableListOf()
        while (_stmt.step()) {
          val _item: ExerciseLog
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpClientExerciseId: Long
          _tmpClientExerciseId = _stmt.getLong(_columnIndexOfClientExerciseId)
          val _tmpSetNumber: Int
          _tmpSetNumber = _stmt.getLong(_columnIndexOfSetNumber).toInt()
          val _tmpActualReps: Int
          _tmpActualReps = _stmt.getLong(_columnIndexOfActualReps).toInt()
          val _tmpActualWeight: Double
          _tmpActualWeight = _stmt.getDouble(_columnIndexOfActualWeight)
          val _tmpResultText: String?
          if (_stmt.isNull(_columnIndexOfResultText)) {
            _tmpResultText = null
          } else {
            _tmpResultText = _stmt.getText(_columnIndexOfResultText)
          }
          val _tmpNote: String?
          if (_stmt.isNull(_columnIndexOfNote)) {
            _tmpNote = null
          } else {
            _tmpNote = _stmt.getText(_columnIndexOfNote)
          }
          val _tmpDate: Long
          _tmpDate = _stmt.getLong(_columnIndexOfDate)
          _item =
              ExerciseLog(_tmpId,_tmpClientExerciseId,_tmpSetNumber,_tmpActualReps,_tmpActualWeight,_tmpResultText,_tmpNote,_tmpDate)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getClientIdForWorkoutSync(workoutId: Long): Long? {
    val _sql: String = """
        |
        |        SELECT client_programs.clientId FROM client_programs
        |        JOIN client_workouts ON client_workouts.clientProgramId = client_programs.id
        |        WHERE client_workouts.id = ?
        |        LIMIT 1
        |    
        """.trimMargin()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, workoutId)
        val _result: Long?
        if (_stmt.step()) {
          if (_stmt.isNull(0)) {
            _result = null
          } else {
            _result = _stmt.getLong(0)
          }
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getTodayWorkoutsSync(): List<TodayWorkout> {
    val _sql: String = """
        |
        |    SELECT cw.id AS workoutId, cw.name AS workoutName,
        |           cw.weekNumber AS weekNumber, cw.dayNumber AS dayNumber, cw.status AS status,
        |           c.name AS clientName, cp.id AS programId
        |    FROM client_workouts cw
        |    JOIN client_programs cp ON cw.clientProgramId = cp.id
        |    JOIN clients c ON cp.clientId = c.id
        |    WHERE cw.status = 'PLANNED' AND cp.status != 'DONE'
        |      AND cw.id = (
        |          SELECT cw2.id FROM client_workouts cw2
        |          WHERE cw2.clientProgramId = cp.id AND cw2.status = 'PLANNED'
        |          ORDER BY cw2.weekNumber ASC, cw2.dayNumber ASC, cw2.id ASC
        |          LIMIT 1
        |      )
        |    ORDER BY c.name, cw.weekNumber, cw.dayNumber
        |""".trimMargin()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfWorkoutId: Int = 0
        val _columnIndexOfWorkoutName: Int = 1
        val _columnIndexOfWeekNumber: Int = 2
        val _columnIndexOfDayNumber: Int = 3
        val _columnIndexOfStatus: Int = 4
        val _columnIndexOfClientName: Int = 5
        val _columnIndexOfProgramId: Int = 6
        val _result: MutableList<TodayWorkout> = mutableListOf()
        while (_stmt.step()) {
          val _item: TodayWorkout
          val _tmpWorkoutId: Long
          _tmpWorkoutId = _stmt.getLong(_columnIndexOfWorkoutId)
          val _tmpWorkoutName: String
          _tmpWorkoutName = _stmt.getText(_columnIndexOfWorkoutName)
          val _tmpWeekNumber: Int
          _tmpWeekNumber = _stmt.getLong(_columnIndexOfWeekNumber).toInt()
          val _tmpDayNumber: Int
          _tmpDayNumber = _stmt.getLong(_columnIndexOfDayNumber).toInt()
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          val _tmpClientName: String
          _tmpClientName = _stmt.getText(_columnIndexOfClientName)
          val _tmpProgramId: Long
          _tmpProgramId = _stmt.getLong(_columnIndexOfProgramId)
          _item =
              TodayWorkout(_tmpWorkoutId,_tmpWorkoutName,_tmpWeekNumber,_tmpDayNumber,_tmpStatus,_tmpClientName,_tmpProgramId)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getClientsCountSync(): Int {
    val _sql: String = "SELECT COUNT(*) FROM clients"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _result: Int
        if (_stmt.step()) {
          val _tmp: Int
          _tmp = _stmt.getLong(0).toInt()
          _result = _tmp
        } else {
          _result = 0
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteClient(clientId: Long) {
    val _sql: String = "DELETE FROM clients WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, clientId)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteMacrocycle(macroId: Long) {
    val _sql: String = "DELETE FROM macrocycles WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, macroId)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteWorkout(workoutId: Long) {
    val _sql: String = "DELETE FROM workouts WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, workoutId)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteWorkoutsOfWeek(macroId: Long, weekNumber: Int) {
    val _sql: String = "DELETE FROM workouts WHERE mesocycleId = ? AND weekNumber = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, macroId)
        _argIndex = 2
        _stmt.bindLong(_argIndex, weekNumber.toLong())
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteExerciseBlock(blockId: Long) {
    val _sql: String = "DELETE FROM exercise_blocks WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, blockId)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteTemplateExercise(exerciseId: Long) {
    val _sql: String = "DELETE FROM template_exercises WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, exerciseId)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteClientProgram(programId: Long) {
    val _sql: String = "DELETE FROM client_programs WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, programId)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun setClientWorkoutStatus(workoutId: Long, status: String) {
    val _sql: String = "UPDATE client_workouts SET status = ? WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, status)
        _argIndex = 2
        _stmt.bindLong(_argIndex, workoutId)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteLog(logId: Long) {
    val _sql: String = "DELETE FROM exercise_logs WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, logId)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
