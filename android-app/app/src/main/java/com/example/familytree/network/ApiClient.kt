package com.example.familytree.network

import com.example.familytree.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private val baseUrl: String = BuildConfig.API_BASE_URL
    private val apiKey: String = BuildConfig.API_KEY

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val requestBuilder = chain.request().newBuilder()
                if (apiKey.isNotBlank()) {
                    requestBuilder.addHeader("X-API-Key", apiKey)
                }
                chain.proceed(requestBuilder.build())
            }
            .build()
    }

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
