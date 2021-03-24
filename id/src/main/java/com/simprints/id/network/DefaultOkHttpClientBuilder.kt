package com.simprints.id.network

import com.test.core.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

open class DefaultOkHttpClientBuilder {

    companion object {
        const val DEVICE_ID_HEADER = "X-Device-ID"
        const val AUTHORIZATION_HEADER = "Authorization"
        const val USER_AGENT_HEADER = "User-Agent"
    }

    open fun get(authToken: String? = null,
                 deviceId: String,
                 versionName: String): OkHttpClient.Builder =
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
                if (BuildConfig.DEBUG_MODE) {
                    addInterceptor(buildLoggingInterceptor())
                }
            }
            .addInterceptor(buildDeviceIdInterceptor(deviceId))
            .addInterceptor(buildVersionInterceptor(versionName))

    private fun buildAuthenticationInterceptor(authToken: String): Interceptor =
        Interceptor { chain ->
            val newRequest = chain.request().newBuilder()
                .addHeader(AUTHORIZATION_HEADER, "Bearer $authToken")
                .build()
            return@Interceptor chain.proceed(newRequest)
        }

    private fun buildDeviceIdInterceptor(deviceId: String): Interceptor =
        Interceptor { chain ->
            val newRequest = chain.request().newBuilder()
                .addHeader(DEVICE_ID_HEADER, deviceId)
                .build()
            return@Interceptor chain.proceed(newRequest)
        }

    private fun buildLoggingInterceptor(): Interceptor {
        val logger = TimberLogger()
        return HttpLoggingInterceptor(logger).apply {
            level = HttpLoggingInterceptor.Level.HEADERS
        }
    }

    private fun buildVersionInterceptor(versionName: String): Interceptor =
        Interceptor { chain ->
            val newRequest = chain.request().newBuilder()
                .addHeader(USER_AGENT_HEADER, "SimprintsID/$versionName")
                .build()
            return@Interceptor chain.proceed(newRequest)
        }
}
