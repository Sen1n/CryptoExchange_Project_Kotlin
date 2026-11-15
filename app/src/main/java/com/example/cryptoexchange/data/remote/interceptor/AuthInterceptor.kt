package com.example.cryptoexchange.data.remote.interceptor

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val apiKey: String,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val cleanKey = apiKey.trim().trim('"')
        val requestBuilder = chain.request().newBuilder()
        if (cleanKey.isNotBlank() && (cleanKey != "your_actual_api_key_here")) {
            requestBuilder.header("Authorization", "Apikey $cleanKey")
        }
        return chain.proceed(requestBuilder.build())
    }
}