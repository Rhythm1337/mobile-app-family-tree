package com.example.familytree.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private const val DEFAULT_BASE_URL = "http://10.0.2.2:8000/"

    private fun normalizeBaseUrl(url: String): String {
        return if (url.endsWith("/")) url else "$url/"
    }

    private fun resolveBaseUrl(): String {
        val fromEnv = System.getenv("API_BASE_URL")?.trim()
        if (!fromEnv.isNullOrEmpty()) {
            return normalizeBaseUrl(fromEnv)
        }

        val fromProperty = System.getProperty("API_BASE_URL")?.trim()
        if (!fromProperty.isNullOrEmpty()) {
            return normalizeBaseUrl(fromProperty)
        }

        return DEFAULT_BASE_URL
    }

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(resolveBaseUrl())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
