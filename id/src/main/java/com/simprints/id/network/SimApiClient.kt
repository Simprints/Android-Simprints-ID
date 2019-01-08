package com.simprints.id.network

import com.google.gson.Gson
import com.simprints.id.tools.json.JsonHelper
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

open class SimApiClient<T>(val service: Class<T>,
                           private val endpoint: String,
                           private val authToken: String? = null,
                           private val jsonAdapter: Gson = JsonHelper.gson) {

    val api: T by lazy {
        retrofit.create(service)
    }

    open val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(jsonAdapter))
            .baseUrl(endpoint)
            .client(okHttpClientConfig.build()).build()
    }

    val okHttpClientConfig: OkHttpClient.Builder by lazy {
        DefaultOkHttpClientBuilder().get(authToken)
    }
}
