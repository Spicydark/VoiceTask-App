package com.joshtalk.sampletask.data

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import com.joshtalk.sampletask.domain.ProductsResponse

class ApiService {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

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
    
    fun close() {
        client.close()
    }
}
