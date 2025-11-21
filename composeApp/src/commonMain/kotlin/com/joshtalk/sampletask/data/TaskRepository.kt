package com.joshtalk.sampletask.data

import com.joshtalk.sampletask.db.TaskDatabase
import com.joshtalk.sampletask.domain.TaskData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import com.joshtalk.sampletask.db.Task

class TaskRepository(private val database: TaskDatabase) {
    
    fun getAllTasks(): Flow<List<TaskData>> {
        return database.taskDatabaseQueries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { tasks -> tasks.map { it.toTaskData() } }
    }
    
    fun getTotalDuration(): Long? {
        return database.taskDatabaseQueries.getTotalDuration().executeAsOneOrNull()?.SUM
    }
    
    fun getTaskCount(): Long {
        return database.taskDatabaseQueries.getTaskCount().executeAsOne()
    }
    
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
