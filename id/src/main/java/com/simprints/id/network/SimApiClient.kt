package com.simprints.id.network

import com.simprints.id.tools.json.JsonHelper
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

open class SimApiClient<T>(val service: Class<T>,
                           private val endpoint: String,
                           private val okHttpClientBuilder: OkHttpClientBuilder = OkHttpClientBuilder(),
                           private val authToken: String? = null) {

    val api: T by lazy {
        retrofit.create(service)
    }

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(JsonHelper.gson))
            .baseUrl(endpoint)
            .client(okHttpClientBuilder.build(authToken)).build()
    }

//    val okHttpClientConfig: OkHttpClient.Builder by lazy {
//        val logger = HttpLoggingInterceptor(TimberLogger())
//        logger.level = HttpLoggingInterceptor.Level.HEADERS
//        OkHttpClient.Builder()
//            .followRedirects(false)
//            .followSslRedirects(false)
//            .readTimeout(30, TimeUnit.SECONDS)
//            .writeTimeout(30, TimeUnit.SECONDS)
//            .addInterceptor(authenticator).also {
//                if (BuildConfig.DEBUG) {
//                    it.addInterceptor(logger)
//                }
//            }
//            .addInterceptor(followTemporaryRedirectResponses)
//    }

}
