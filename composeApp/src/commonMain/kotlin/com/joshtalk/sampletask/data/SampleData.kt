package com.joshtalk.sampletask.data

import com.joshtalk.sampletask.domain.Product
import com.joshtalk.sampletask.domain.ProductsResponse

/**
 * Bundled fallback data providing sample products when API is unavailable.
 * Ensures app remains functional offline or during network failures.
 * Products contain both text descriptions and image URLs suitable for all task types.
 * 
 * Text passages are crafted for field agents to practice clear enunciation and pacing.
 * Images demonstrate real-world product photography for description tasks.
 */
object SampleData {
    val sampleProducts: List<Product> = listOf(
        Product(
            id = 1,
            title = "Practice Perfume",
            description = "Mega long lasting fragrance with soft lavender notes. Read this passage aloud clearly and steadily.",
            images = listOf(
                "https://cdn.dummyjson.com/product-images/14/1.jpg",
                "https://cdn.dummyjson.com/product-images/14/2.jpg"
            )
        ),
        Product(
            id = 2,
            title = "Wireless Headset",
            description = "Comfortable over-ear headset designed for remote work calls. Mention the cushioning, noise cancellation and battery backup in your description.",
            images = listOf(
                "https://cdn.dummyjson.com/product-images/11/1.jpg",
                "https://cdn.dummyjson.com/product-images/11/2.jpg"
            )
        ),
        Product(
            id = 3,
            title = "Bookshelf",
            description = "A wooden bookshelf with four spacious racks, perfect for organizing study materials and décor pieces.",
            images = listOf(
                "https://cdn.dummyjson.com/product-images/74/1.jpg",
                "https://cdn.dummyjson.com/product-images/74/2.jpg"
            )
        )
    )

    /**
     * Wraps sample products in ProductsResponse format matching API structure.
     * Used by ApiService when network fetch fails or returns empty results.
     */
    fun fallbackResponse(): ProductsResponse = ProductsResponse(sampleProducts)
}
