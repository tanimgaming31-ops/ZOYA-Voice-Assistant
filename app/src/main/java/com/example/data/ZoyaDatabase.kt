package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "notes")
data class NoteEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val title: String,
  val content: String,
  val category: String = "General",
  val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "tasks")
data class TaskEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val title: String,
  val description: String = "",
  val dueDate: String = "",
  val priority: String = "Normal",
  val isCompleted: Boolean = false,
  val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "reminders")
data class ReminderEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val title: String,
  val dateTime: String,
  val isEnabled: Boolean = true
)

@Entity(tableName = "memory")
data class MemoryEntity(
  @PrimaryKey val key: String,
  val value: String,
  val category: String = "Preference"
)

@Dao
interface NoteDao {
  @Query("SELECT * FROM notes ORDER BY timestamp DESC")
  fun getAllNotes(): Flow<List<NoteEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertNote(note: NoteEntity)

  @Query("DELETE FROM notes WHERE id = :id")
  suspend fun deleteNote(id: Long)
}

@Dao
interface TaskDao {
  @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, timestamp DESC")
  fun getAllTasks(): Flow<List<TaskEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTask(task: TaskEntity)

  @Update
  suspend fun updateTask(task: TaskEntity)

  @Query("DELETE FROM tasks WHERE id = :id")
  suspend fun deleteTask(id: Long)
}

@Dao
interface ReminderDao {
  @Query("SELECT * FROM reminders ORDER BY dateTime ASC")
  fun getAllReminders(): Flow<List<ReminderEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertReminder(reminder: ReminderEntity)

  @Query("DELETE FROM reminders WHERE id = :id")
  suspend fun deleteReminder(id: Long)
}

@Dao
interface MemoryDao {
  @Query("SELECT * FROM memory")
  fun getAllMemory(): Flow<List<MemoryEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun setMemory(memory: MemoryEntity)

  @Query("DELETE FROM memory WHERE `key` = :key")
  suspend fun deleteMemory(key: String)
}

@Database(entities = [NoteEntity::class, TaskEntity::class, ReminderEntity::class, MemoryEntity::class], version = 1, exportSchema = false)
abstract class ZoyaDatabase : RoomDatabase() {
  abstract fun noteDao(): NoteDao
  abstract fun taskDao(): TaskDao
  abstract fun reminderDao(): ReminderDao
  abstract fun memoryDao(): MemoryDao

  companion object {
    @Volatile private var INSTANCE: ZoyaDatabase? = null

    fun getDatabase(context: Context): ZoyaDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          ZoyaDatabase::class.java,
          "zoya_database"
        ).build()
        INSTANCE = instance
        instance
      }
    }
  }
}
