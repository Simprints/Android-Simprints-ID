package com.simprints.infra.network

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

class DefaultOkHttpClientBuilder {

    companion object {
        const val DEVICE_ID_HEADER = "X-Device-ID"
        const val AUTHORIZATION_HEADER = "Authorization"
        const val USER_AGENT_HEADER = "User-Agent"
    }

    fun get(
        ctx: Context,
        authToken: String? = null,
        deviceId: String,
        versionName: String,
    ): OkHttpClient.Builder =
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
                    addInterceptor(buildSimberLoggingInterceptor())
                }
            }
            .addNetworkInterceptor(ChuckerInterceptor.Builder(ctx).build())
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

    private fun buildSimberLoggingInterceptor(): Interceptor {
        val logger = SimberLogger
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
