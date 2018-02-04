package com.simprints.id.secure

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

open class ApiService {

    val okHttpClientConfig:OkHttpClient.Builder by lazy {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BASIC
        OkHttpClient.Builder().addInterceptor(interceptor)
    }

    val retrofit: Retrofit by lazy {
        getRetrofitConfig().build()
    }

    val api: ApiServiceInterface by lazy {
        retrofit.create(ApiServiceInterface::class.java)
    }


    private fun getRetrofitConfig(): Retrofit.Builder {

        return Retrofit.Builder()
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(JsonHelper.gson))
            .baseUrl("https://project-manager-dot-simprints-dev.appspot.com")
            .client(okHttpClientConfig.build())
    }
}

