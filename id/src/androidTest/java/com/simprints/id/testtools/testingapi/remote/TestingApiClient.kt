package com.simprints.id.testtools.testingapi.remote

import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.network.SimApiClientImpl
import com.simprints.id.network.SimRemoteInterface
import com.simprints.id.network.TimberLogger
import io.mockk.mockk
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.jackson.JacksonConverterFactory
import kotlin.reflect.KClass

class TestingApiClient<T : SimRemoteInterface>(service: KClass<T>, endpoint: String,
                                               private val jsonHelper: JsonHelper)
    : SimApiClientImpl<T>(service, endpoint, "", "", "", mockk(), jsonHelper) {

    override val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(JacksonConverterFactory.create(jsonHelper.jackson))
            .baseUrl(endpoint)
            .client(okHttpClientConfig.addInterceptor(
                HttpLoggingInterceptor(TimberLogger()).apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            ).build()).build()
    }
}
