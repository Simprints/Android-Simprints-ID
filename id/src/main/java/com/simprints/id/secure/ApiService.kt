package com.simprints.id.secure

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

open class ApiService {

    //val baseUrl = "https://project-manager-dot-${BuildConfig.GCP_PROJECT}.appspot.com"
    var baseUrl = "http://192.168.86.57:8080"

    val api: ApiServiceInterface by lazy {
        retrofit.create(ApiServiceInterface::class.java)
    }

    val okHttpClientConfig: OkHttpClient.Builder by lazy {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BASIC
        OkHttpClient.Builder().addInterceptor(interceptor)
    }

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(JsonHelper.gson))
            .baseUrl(baseUrl)
            .client(okHttpClientConfig.build()).build()
    }
}
