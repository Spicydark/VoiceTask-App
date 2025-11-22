package com.joshtalk.sampletask.data

import com.joshtalk.sampletask.db.TaskDatabase
import com.joshtalk.sampletask.domain.TaskData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import com.joshtalk.sampletask.db.Task

/**
 * Repository layer providing reactive access to task data stored in SQLDelight database.
 * Handles conversion between database Task entities and domain TaskData models.
 * All database operations use Flow for reactive updates across the UI.
 */
class TaskRepository(private val database: TaskDatabase) {
    
    /**
     * Retrieves all stored tasks as a reactive Flow that emits on any database change.
     * Results are automatically mapped from database entities to domain models.
     * @return Flow emitting list of all tasks, automatically updates on inserts/deletes
     */
    fun getAllTasks(): Flow<List<TaskData>> {
        return database.taskDatabaseQueries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { tasks -> tasks.map { it.toTaskData() } }
    }
    
    /**
     * Calculates total duration of all completed tasks in seconds.
     * Used for summary statistics in task history screen.
     * @return Sum of all task durations, or null if no tasks exist
     */
    fun getTotalDuration(): Long? {
        return database.taskDatabaseQueries.getTotalDuration().executeAsOneOrNull()?.SUM
    }
    
    /**
     * Returns count of all completed tasks across all types.
     * @return Total number of tasks in database
     */
    fun getTaskCount(): Long {
        return database.taskDatabaseQueries.getTaskCount().executeAsOne()
    }
    
    /**
     * Persists a new task to the database.
     * Handles polymorphic TaskData types and maps them to appropriate database columns.
     * @param task Domain model of the task to store (TextReading, ImageDescription, or PhotoCapture)
     */
    fun insertTask(task: TaskData) {
        when (task) {
            is TaskData.TextReading -> {
                database.taskDatabaseQueries.insert(
                    taskId = task.taskId,
                    taskType = task.taskType,
                    text = task.text,
                    imageUrl = null,
                    imagePath = null,
                    audioPath = task.audioPath,
                    durationSec = task.durationSec.toLong(),
                    timestamp = task.timestamp
                )
            }
            is TaskData.ImageDescription -> {
                database.taskDatabaseQueries.insert(
                    taskId = task.taskId,
                    taskType = task.taskType,
                    text = null,
                    imageUrl = task.imageUrl,
                    imagePath = null,
                    audioPath = task.audioPath,
                    durationSec = task.durationSec.toLong(),
                    timestamp = task.timestamp
                )
            }
            is TaskData.PhotoCapture -> {
                database.taskDatabaseQueries.insert(
                    taskId = task.taskId,
                    taskType = task.taskType,
                    text = task.description,
                    imageUrl = null,
                    imagePath = task.imagePath,
                    audioPath = task.audioPath,
                    durationSec = task.durationSec.toLong(),
                    timestamp = task.timestamp
                )
            }
        }
    }
    
    /**
     * Converts database Task entity to domain TaskData sealed class instance.
     * Maps taskType string to appropriate subclass and handles nullable fields.
     * @throws IllegalArgumentException if taskType is not recognized
     */
    private fun Task.toTaskData(): TaskData {
        return when (taskType) {
            "text_reading" -> TaskData.TextReading(
                taskId = taskId,
                text = text ?: "",
                audioPath = audioPath,
                durationSec = durationSec.toInt(),
                timestamp = timestamp
            )
            "image_description" -> TaskData.ImageDescription(
                taskId = taskId,
                imageUrl = imageUrl ?: "",
                audioPath = audioPath,
                durationSec = durationSec.toInt(),
                timestamp = timestamp
            )
            "photo_capture" -> TaskData.PhotoCapture(
                taskId = taskId,
                description = text ?: "",
                imagePath = imagePath ?: "",
                audioPath = audioPath,
                durationSec = durationSec.toInt(),
                timestamp = timestamp
            )
            else -> throw IllegalArgumentException("Unknown task type: $taskType")
        }
    }
}
