package com.simprints.id.network

import com.simprints.id.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

class OkHttpClientBuilder {

    fun build(authToken: String? = null): OkHttpClient =
        OkHttpClient.Builder()
            .followRedirects(false)
            .followSslRedirects(false)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(buildAuthenticationInterceptor(authToken))
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(buildLoggingInterceptor())
                }
            }
            .addInterceptor(buildTemporaryRedirectionFollowingInterceptor())
            .build()

    private fun buildAuthenticationInterceptor(authToken: String?): Interceptor =
        Interceptor { chain ->
            val newRequest = chain.request().newBuilder()
                .addAuthTokenIfRequestRequires(authToken)
                .build()
            return@Interceptor chain.proceed(newRequest)
        }

    private fun Request.Builder.addAuthTokenIfRequestRequires(authToken: String?): Request.Builder =
        apply {
            if (!authToken.isNullOrBlank()) {
                addHeader("Authorization", "Bearer $authToken")
            }
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
