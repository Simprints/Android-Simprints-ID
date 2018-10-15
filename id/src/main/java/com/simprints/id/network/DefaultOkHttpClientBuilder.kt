package com.simprints.id.network

import com.simprints.id.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

class DefaultOkHttpClientBuilder {

    fun get(authToken: String? = null): OkHttpClient.Builder =
        OkHttpClient.Builder()
            .followRedirects(false)
            .followSslRedirects(false)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .apply {
                if (authToken != null && authToken.isNotBlank()) {
                    addInterceptor(buildAuthenticationInterceptor(authToken))
                }
            }
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(buildLoggingInterceptor())
                }
            }
            .addInterceptor(buildTemporaryRedirectionFollowingInterceptor())

    private fun buildAuthenticationInterceptor(authToken: String): Interceptor =
        Interceptor { chain ->
            val newRequest = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $authToken")
                .build()
            return@Interceptor chain.proceed(newRequest)
        }

    private fun buildLoggingInterceptor(): Interceptor {
        val logger = TimberLogger()
        return HttpLoggingInterceptor(logger).apply {
            level = HttpLoggingInterceptor.Level.HEADERS
        }
    }

    // if the server returns 307, we follow the url in the "Location" header field.
    private fun buildTemporaryRedirectionFollowingInterceptor(): Interceptor =
        Interceptor { chain ->
            val initialRequest = chain.request()
            val initialResponse = chain.proceed(initialRequest)

            if (initialResponse.code() != 307) {
                return@Interceptor initialResponse
            }

            val redirectLocation = initialResponse.header("Location") ?: return@Interceptor initialResponse

            val newRequest = chain.request().newBuilder()
                .url(redirectLocation)
                .build()

            return@Interceptor chain.proceed(newRequest)
        }




}
