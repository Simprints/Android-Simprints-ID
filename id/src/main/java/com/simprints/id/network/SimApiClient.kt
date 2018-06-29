package com.simprints.id.network

import com.simprints.id.BuildConfig
import com.simprints.id.tools.json.JsonHelper
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

open class SimApiClient<T>(val service: Class<T>,
                           private val endpoint: String,
                           private val authToken: String? = null) {

    val api: T by lazy {
        retrofit.create(service)
    }

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(JsonHelper.gson))
            .baseUrl(endpoint)
            .client(okHttpClientConfig.build()).build()
    }

    val okHttpClientConfig: OkHttpClient.Builder by lazy {
        val logger = HttpLoggingInterceptor()
        logger.level = HttpLoggingInterceptor.Level.HEADERS
        OkHttpClient.Builder()
            .followRedirects(false)
            .followSslRedirects(false)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(authenticator).also {
                if (BuildConfig.DEBUG) {
                    it.addInterceptor(logger)
                }
            }
            .addInterceptor(followTemporaryRedirectResponses)
    }

    private val authenticator = Interceptor { chain ->
        val newRequest = chain.request().newBuilder()
        addAuthTokenIfRequestRequires(newRequest)
        return@Interceptor chain.proceed(newRequest.build())
    }

    // if the server returns 307, we follow the url in the "Location" header field.
    private val followTemporaryRedirectResponses = Interceptor { chain ->
        val newRequest = chain.request().newBuilder()
        val response = chain.proceed(newRequest.build())

        return@Interceptor if (response.code() == 307) {
            response.header("Location")?.let {
                newRequest.url(it)
                chain.proceed(newRequest.build())
            } ?: response
        } else {
            response
        }
    }

    private fun addAuthTokenIfRequestRequires(newRequest: Request.Builder) {
        if (!authToken.isNullOrBlank()) {
            newRequest.addHeader("Authorization", "Bearer $authToken")
        }
    }
}
