package com.simprints.core.network

import com.test.core.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

class DefaultOkHttpClientBuilder(val deviceId: String) {

    companion object {
        const val DEVICE_ID_HEADER = "X-Device-ID"
        const val AUTHORIZATION_HEADER = "Authorization"
    }

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
                if (BuildConfig.DEBUG || BuildConfig.BUILD_TYPE == "releaseWithLogfile") {
                    addInterceptor(buildLoggingInterceptor())
                }
            }
            .addInterceptor(buildDeviceIdInterceptor(deviceId))

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
}
