package com.simprints.core.network

import com.google.gson.Gson
import com.simprints.core.tools.json.JsonHelper
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

open class SimApiClient<T>(private val service: Class<T>,
                           private val url: String,
                           private val deviceId: String,
                           private val authToken: String? = null,
                           private val jsonAdapter: Gson = JsonHelper.gson) {

    val api: T by lazy {
        retrofit.create(service)
    }

    open val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(jsonAdapter))
            .baseUrl(url)
            .client(okHttpClientConfig.build()).build()
    }

    val okHttpClientConfig: OkHttpClient.Builder by lazy {
        DefaultOkHttpClientBuilder().get(authToken, deviceId)
    }
}
