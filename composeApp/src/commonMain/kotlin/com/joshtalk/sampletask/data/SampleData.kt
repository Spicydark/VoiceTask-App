package com.joshtalk.sampletask.data

import com.joshtalk.sampletask.domain.Product
import com.joshtalk.sampletask.domain.ProductsResponse

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

    fun fallbackResponse(): ProductsResponse = ProductsResponse(sampleProducts)
}
