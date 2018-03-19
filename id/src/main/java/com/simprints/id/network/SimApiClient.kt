package com.simprints.id.network

import com.simprints.id.tools.JsonHelper
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

open class SimApiClient<T>(val service: Class<T>, private val endpoint: String) {

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
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.HEADERS
        OkHttpClient.Builder()
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(interceptor)
    }
}
