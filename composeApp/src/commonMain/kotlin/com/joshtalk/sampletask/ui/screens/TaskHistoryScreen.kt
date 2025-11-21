package com.joshtalk.sampletask.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joshtalk.sampletask.data.TaskRepository
import com.joshtalk.sampletask.domain.TaskData
import com.joshtalk.sampletask.ui.components.PlatformAsyncImage
import com.joshtalk.sampletask.ui.theme.PrimaryBlue
import com.joshtalk.sampletask.ui.theme.TextGray

/**
 * Screen displaying aggregated task history with summary statistics and detail list.
 * Shows total task count, cumulative duration, and scrollable list of all completed tasks.
 * Uses reactive Flow from repository to automatically update on new task submissions.
 * 
 * Each task card displays type-specific preview (image/text/photo), timestamp, and duration.
 * This serves as the QA reference screen for agents to review their completed work.
 * 
 * @param taskRepository Database repository providing reactive task data
 * @param onNavigateBack Callback to return to task selection
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskHistoryScreen(
    taskRepository: TaskRepository,
    onNavigateBack: () -> Unit
) {
    var tasks by remember { mutableStateOf<List<TaskData>>(emptyList()) }
    var totalTasks by remember { mutableLongStateOf(0L) }
    var totalDuration by remember { mutableLongStateOf(0L) }

    LaunchedEffect(Unit) {
        taskRepository.getAllTasks().collect { taskList ->
            tasks = taskList
            totalTasks = taskList.size.toLong()
            totalDuration = taskList.sumOf { it.durationSec }.toLong()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recording Tasks") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryBlue,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Total Tasks",
                            fontSize = 14.sp,
                            color = TextGray
                        )
                        Text(
                            text = totalTasks.toString(),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Total Recording Duration",
                            fontSize = 14.sp,
                            color = TextGray
                        )
                        Text(
                            text = "${totalDuration}s",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                    }
                }
            }

            if (tasks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "No tasks yet",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Complete some tasks to see them here",
                            fontSize = 14.sp,
                            color = TextGray
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(tasks) { task ->
                        TaskListItem(task = task)
                    }
                }
            }
        }
    }
}


@Composable
fun TaskListItem(task: TaskData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (task) {
                is TaskData.ImageDescription -> {
                    Card(
                        modifier = Modifier.size(60.dp)
                    ) {
                        PlatformAsyncImage(
                            model = task.imageUrl,
                            contentDescription = "Task image",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                is TaskData.PhotoCapture -> {
                    Card(
                        modifier = Modifier.size(60.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        if (task.imagePath.isNotBlank()) {
                            PlatformAsyncImage(
                                model = task.imagePath,
                                contentDescription = "Captured photo",
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("📷", fontSize = 30.sp)
                            }
                        }
                    }
                }
                is TaskData.TextReading -> {
                    Card(
                        modifier = Modifier.size(60.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("📝", fontSize = 30.sp)
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = when (task) {
                        is TaskData.TextReading -> "Text Reading"
                        is TaskData.ImageDescription -> "Image Description"
                        is TaskData.PhotoCapture -> "Photo Capture"
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "ID: ${task.taskId.take(8)}...",
                    fontSize = 12.sp,
                    color = TextGray
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "${task.durationSec}s",
                        fontSize = 12.sp,
                        color = TextGray
                    )
                    Text(
                        text = "•",
                        fontSize = 12.sp,
                        color = TextGray
                    )
                    Text(
                        text = task.timestamp.substringBefore("T"),
                        fontSize = 12.sp,
                        color = TextGray
                    )
                }

                when (task) {
                    is TaskData.TextReading -> {
                        val snippet = task.text.take(50)
                        Text(
                            text = if (task.text.length > 50) "$snippet..." else snippet,
                            fontSize = 12.sp,
                            color = TextGray,
                            maxLines = 1
                        )
                    }
                    is TaskData.PhotoCapture -> {
                        val snippet = task.description.take(50)
                        if (snippet.isNotEmpty()) {
                            Text(
                                text = if (task.description.length > 50) "$snippet..." else snippet,
                                fontSize = 12.sp,
                                color = TextGray,
                                maxLines = 1
                            )
                        }
                    }
                    else -> Unit
                }
            }
        }
    }
}
