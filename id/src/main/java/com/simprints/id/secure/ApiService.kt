package com.simprints.id.secure

import com.simprints.id.secure.ApiServiceInterface.Companion.baseUrl
import com.simprints.id.tools.JsonHelper
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

open class ApiService {

    val api: ApiServiceInterface by lazy {
        retrofit.create(ApiServiceInterface::class.java)
    }

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(JsonHelper.gson))
            .baseUrl(baseUrl)
            .client(okHttpClientConfig.build()).build()
    }

    val okHttpClientConfig: OkHttpClient.Builder by lazy {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BASIC
        OkHttpClient.Builder().addInterceptor(interceptor)
    }
}
