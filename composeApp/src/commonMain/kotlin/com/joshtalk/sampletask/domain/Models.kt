@file:OptIn(kotlinx.serialization.InternalSerializationApi::class)
package com.joshtalk.sampletask.domain

import kotlinx.serialization.Serializable

/**
 * Sealed hierarchy representing different types of tasks a field agent can complete.
 * Each task type captures required fields and links to audio recordings.
 * All task data is persisted to SQLDelight database and used for history aggregation.
 */
@Serializable
sealed class TaskData {
    abstract val taskId: String
    abstract val taskType: String
    abstract val durationSec: Int
    abstract val timestamp: String
    
    /**
     * Task where agent reads a text passage aloud in their native language.
     * Text content is fetched from API or fallback sample data.
     */
    @Serializable
    data class TextReading(
        override val taskId: String,
        override val taskType: String = "text_reading",
        val text: String,
        val audioPath: String,
        override val durationSec: Int,
        override val timestamp: String
    ) : TaskData()
    
    /**
     * Task where agent describes an image shown on screen.
     * Image is fetched from external API providing product catalog photos.
     */
    @Serializable
    data class ImageDescription(
        override val taskId: String,
        override val taskType: String = "image_description",
        val imageUrl: String,
        val audioPath: String,
        override val durationSec: Int,
        override val timestamp: String
    ) : TaskData()
    
    /**
     * Task where agent captures a photo using device camera and provides description.
     * Supports both text and optional audio description of the captured photo.
     */
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

/**
 * API response wrapper for DummyJSON products endpoint.
 * Used to fetch text passages and images for task content.
 */
@Serializable
data class ProductsResponse(
    val products: List<Product>
)

/**
 * Individual product from API containing text description and image URLs.
 * Serves as source content for text reading and image description tasks.
 */
@Serializable
data class Product(
    val id: Int,
    val title: String,
    val description: String,
    val images: List<String> = emptyList()
)
