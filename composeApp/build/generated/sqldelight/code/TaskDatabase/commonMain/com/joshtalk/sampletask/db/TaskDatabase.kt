package com.joshtalk.sampletask.db

import app.cash.sqldelight.Transacter
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import com.joshtalk.sampletask.db.composeApp.newInstance
import com.joshtalk.sampletask.db.composeApp.schema
import kotlin.Unit

public interface TaskDatabase : Transacter {
  public val taskDatabaseQueries: TaskDatabaseQueries

  public companion object {
    public val Schema: SqlSchema<QueryResult.Value<Unit>>
      get() = TaskDatabase::class.schema

    public operator fun invoke(driver: SqlDriver): TaskDatabase =
        TaskDatabase::class.newInstance(driver)
  }
}
