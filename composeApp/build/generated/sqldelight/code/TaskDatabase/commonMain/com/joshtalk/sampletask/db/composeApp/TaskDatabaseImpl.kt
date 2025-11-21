package com.joshtalk.sampletask.db.composeApp

import app.cash.sqldelight.TransacterImpl
import app.cash.sqldelight.db.AfterVersion
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import com.joshtalk.sampletask.db.TaskDatabase
import com.joshtalk.sampletask.db.TaskDatabaseQueries
import kotlin.Long
import kotlin.Unit
import kotlin.reflect.KClass

internal val KClass<TaskDatabase>.schema: SqlSchema<QueryResult.Value<Unit>>
  get() = TaskDatabaseImpl.Schema

internal fun KClass<TaskDatabase>.newInstance(driver: SqlDriver): TaskDatabase =
    TaskDatabaseImpl(driver)

private class TaskDatabaseImpl(
  driver: SqlDriver,
) : TransacterImpl(driver), TaskDatabase {
  override val taskDatabaseQueries: TaskDatabaseQueries = TaskDatabaseQueries(driver)

  public object Schema : SqlSchema<QueryResult.Value<Unit>> {
    override val version: Long
      get() = 1

    override fun create(driver: SqlDriver): QueryResult.Value<Unit> {
      driver.execute(null, """
          |CREATE TABLE Task (
          |    taskId TEXT PRIMARY KEY NOT NULL,
          |    taskType TEXT NOT NULL,
          |    text TEXT,
          |    imageUrl TEXT,
          |    imagePath TEXT,
          |    audioPath TEXT NOT NULL,
          |    durationSec INTEGER NOT NULL,
          |    timestamp TEXT NOT NULL
          |)
          """.trimMargin(), 0)
      return QueryResult.Unit
    }

    override fun migrate(
      driver: SqlDriver,
      oldVersion: Long,
      newVersion: Long,
      vararg callbacks: AfterVersion,
    ): QueryResult.Value<Unit> = QueryResult.Unit
  }
}
