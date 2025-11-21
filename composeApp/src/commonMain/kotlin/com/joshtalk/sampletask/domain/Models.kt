@file:OptIn(kotlinx.serialization.InternalSerializationApi::class)
package com.joshtalk.sampletask.domain

import kotlinx.serialization.Serializable

@Serializable
sealed class TaskData {
    abstract val taskId: String
    abstract val taskType: String
    abstract val durationSec: Int
    abstract val timestamp: String
    
    @Serializable
    data class TextReading(
        override val taskId: String,
        override val taskType: String = "text_reading",
        val text: String,
        val audioPath: String,
        override val durationSec: Int,
        override val timestamp: String
    ) : TaskData()
    
    @Serializable
    data class ImageDescription(
        override val taskId: String,
        override val taskType: String = "image_description",
        val imageUrl: String,
        val audioPath: String,
        override val durationSec: Int,
        override val timestamp: String
    ) : TaskData()
    
    @Serializable
    data class PhotoCapture(
        override val taskId: String,
        override val taskType: String = "photo_capture",
        val description: String,
        val imagePath: String,
        val audioPath: String,
        override val durationSec: Int,
        override val timestamp: String
    ) : TaskData()
}

// API Models
@Serializable
data class ProductsResponse(
    val products: List<Product>
)

@Serializable
data class Product(
    val id: Int,
    val title: String,
    val description: String,
    val images: List<String> = emptyList()
)
