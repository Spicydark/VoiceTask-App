package com.joshtalk.sampletask.data

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import com.joshtalk.sampletask.domain.ProductsResponse

/**
 * HTTP client service for fetching task content from external APIs.
 * Provides automatic fallback to bundled sample data when network requests fail.
 * Uses Ktor client with JSON content negotiation for API communication.
 */
class ApiService {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    /**
     * Fetches product catalog from DummyJSON API for use as task content.
     * Automatically falls back to bundled SampleData if request fails or returns empty.
     * This ensures the app functions offline or when API is unavailable.
     * @return Result containing ProductsResponse with products list, never fails completely
     */
    suspend fun getProducts(): Result<ProductsResponse> = try {
        val response = client.get("https://dummyjson.com/products")
        val body: ProductsResponse = response.body()
        if (body.products.isNotEmpty()) {
            Result.success(body)
        } else {
            Result.success(SampleData.fallbackResponse())
        }
    } catch (e: Exception) {
        if (SampleData.sampleProducts.isNotEmpty()) {
            Result.success(SampleData.fallbackResponse())
        } else {
            Result.failure(e)
        }
    }
    
    /**
     * Closes the HTTP client and releases associated resources.
     * Should be called when the service is no longer needed.
     */
    fun close() {
        client.close()
    }
}
