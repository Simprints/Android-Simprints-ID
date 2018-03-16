package com.simprints.id.data.db.sync

import com.simprints.id.data.db.sync.SimApiInterface.Companion.baseUrl
import com.simprints.id.secure.JsonHelper
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

open class SimApi {

    val api: SimApiInterface by lazy {
        retrofit.create(SimApiInterface::class.java)
    }

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(JsonHelper.gson))
            .baseUrl(baseUrl)
            .client(okHttpClientConfig.build()).build()
    }

    private val okHttpClientConfig: OkHttpClient.Builder by lazy {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.HEADERS
        OkHttpClient.Builder()
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(interceptor)
    }
}
