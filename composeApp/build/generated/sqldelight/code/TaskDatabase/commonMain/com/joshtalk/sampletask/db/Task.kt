package com.joshtalk.sampletask.db

import kotlin.Long
import kotlin.String

public data class Task(
  public val taskId: String,
  public val taskType: String,
  public val text: String?,
  public val imageUrl: String?,
  public val imagePath: String?,
  public val audioPath: String,
  public val durationSec: Long,
  public val timestamp: String,
)
