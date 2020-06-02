package com.simprints.id.testtools.testingapi.remote

import com.google.gson.Gson
import com.simprints.id.network.SimApiClientImpl
import com.simprints.id.network.SimRemoteInterface
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.reflect.KClass

class TestingApiClient<T: SimRemoteInterface>(service: KClass<T>, endpoint: String)
    : SimApiClientImpl<T>(service, endpoint, "") {

    override val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(Gson()))
            .baseUrl(endpoint)
            .client(okHttpClientConfig.build()).build()
    }
}
