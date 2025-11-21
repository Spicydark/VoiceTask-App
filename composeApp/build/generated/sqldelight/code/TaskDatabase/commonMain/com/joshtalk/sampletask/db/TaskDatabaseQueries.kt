package com.joshtalk.sampletask.db

import app.cash.sqldelight.Query
import app.cash.sqldelight.TransacterImpl
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import kotlin.Any
import kotlin.Long
import kotlin.String

public class TaskDatabaseQueries(
  driver: SqlDriver,
) : TransacterImpl(driver) {
  public fun <T : Any> selectAll(mapper: (
    taskId: String,
    taskType: String,
    text: String?,
    imageUrl: String?,
    imagePath: String?,
    audioPath: String,
    durationSec: Long,
    timestamp: String,
  ) -> T): Query<T> = Query(-1_563_549_153, arrayOf("Task"), driver, "TaskDatabase.sq", "selectAll",
      "SELECT Task.taskId, Task.taskType, Task.text, Task.imageUrl, Task.imagePath, Task.audioPath, Task.durationSec, Task.timestamp FROM Task ORDER BY timestamp DESC") {
      cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2),
      cursor.getString(3),
      cursor.getString(4),
      cursor.getString(5)!!,
      cursor.getLong(6)!!,
      cursor.getString(7)!!
    )
  }

  public fun selectAll(): Query<Task> = selectAll { taskId, taskType, text, imageUrl, imagePath,
      audioPath, durationSec, timestamp ->
    Task(
      taskId,
      taskType,
      text,
      imageUrl,
      imagePath,
      audioPath,
      durationSec,
      timestamp
    )
  }

  public fun <T : Any> selectById(taskId: String, mapper: (
    taskId: String,
    taskType: String,
    text: String?,
    imageUrl: String?,
    imagePath: String?,
    audioPath: String,
    durationSec: Long,
    timestamp: String,
  ) -> T): Query<T> = SelectByIdQuery(taskId) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2),
      cursor.getString(3),
      cursor.getString(4),
      cursor.getString(5)!!,
      cursor.getLong(6)!!,
      cursor.getString(7)!!
    )
  }

  public fun selectById(taskId: String): Query<Task> = selectById(taskId) { taskId_, taskType, text,
      imageUrl, imagePath, audioPath, durationSec, timestamp ->
    Task(
      taskId_,
      taskType,
      text,
      imageUrl,
      imagePath,
      audioPath,
      durationSec,
      timestamp
    )
  }

  public fun <T : Any> getTotalDuration(mapper: (SUM: Long?) -> T): Query<T> = Query(-88_986_392,
      arrayOf("Task"), driver, "TaskDatabase.sq", "getTotalDuration",
      "SELECT SUM(durationSec) FROM Task") { cursor ->
    mapper(
      cursor.getLong(0)
    )
  }

  public fun getTotalDuration(): Query<GetTotalDuration> = getTotalDuration { SUM ->
    GetTotalDuration(
      SUM
    )
  }

  public fun getTaskCount(): Query<Long> = Query(-2_145_029_510, arrayOf("Task"), driver,
      "TaskDatabase.sq", "getTaskCount", "SELECT COUNT(*) FROM Task") { cursor ->
    cursor.getLong(0)!!
  }

  public fun insert(
    taskId: String,
    taskType: String,
    text: String?,
    imageUrl: String?,
    imagePath: String?,
    audioPath: String,
    durationSec: Long,
    timestamp: String,
  ) {
    driver.execute(-512_532_001, """
        |INSERT INTO Task (taskId, taskType, text, imageUrl, imagePath, audioPath, durationSec, timestamp)
        |VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """.trimMargin(), 8) {
          bindString(0, taskId)
          bindString(1, taskType)
          bindString(2, text)
          bindString(3, imageUrl)
          bindString(4, imagePath)
          bindString(5, audioPath)
          bindLong(6, durationSec)
          bindString(7, timestamp)
        }
    notifyQueries(-512_532_001) { emit ->
      emit("Task")
    }
  }

  public fun deleteAll() {
    driver.execute(-206_282_992, """DELETE FROM Task""", 0)
    notifyQueries(-206_282_992) { emit ->
      emit("Task")
    }
  }

  private inner class SelectByIdQuery<out T : Any>(
    public val taskId: String,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("Task", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("Task", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(-1_225_342_188,
        """SELECT Task.taskId, Task.taskType, Task.text, Task.imageUrl, Task.imagePath, Task.audioPath, Task.durationSec, Task.timestamp FROM Task WHERE taskId = ?""",
        mapper, 1) {
      bindString(0, taskId)
    }

    override fun toString(): String = "TaskDatabase.sq:selectById"
  }
}
