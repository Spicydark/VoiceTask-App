package com.joshtalk.sampletask.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale

/**
 * Cross-platform async image loading composable with platform-specific implementations.
 * Android uses Coil library for efficient image loading with caching and transformations.
 * iOS currently uses placeholder stub until native image loader is integrated.
 * 
 * Abstracts image loading complexity and provides consistent API across platforms
 * for displaying both remote URLs (API images) and local file paths (captured photos).
 * 
 * @param model Image source - can be URL string, file path, or null
 * @param contentDescription Accessibility description for screen readers
 * @param modifier Optional modifier for sizing and positioning
 * @param contentScale How to scale/crop the image within its bounds
 */
@Composable
expect fun PlatformAsyncImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
)
