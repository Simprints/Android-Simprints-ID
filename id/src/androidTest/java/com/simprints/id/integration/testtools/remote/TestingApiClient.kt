package com.simprints.id.integration.testtools.remote

import com.google.gson.Gson
import com.simprints.id.network.SimApiClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class TestingApiClient<T>(service: Class<T>, endpoint: String)
    : SimApiClient<T>(service, endpoint, null) {

    override val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(Gson()))
            .baseUrl(endpoint)
            .client(okHttpClientConfig.build()).build()
    }
}
