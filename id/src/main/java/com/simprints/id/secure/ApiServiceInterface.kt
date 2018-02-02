package com.simprints.id.secure

import io.reactivex.Single
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.Query

interface ApiServiceInterface {

    companion object {
        fun create(): ApiServiceInterface {

            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BASIC
            val client = OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build()

            val retrofit = Retrofit.Builder()
                .addCallAdapterFactory( RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("https://project-manager-dot-simprints-dev.appspot.com")
                .client(client)
                .build()

            return retrofit.create(ApiServiceInterface::class.java)
        }
    }

    @GET("/nonces")
    fun nonce(@HeaderMap headers: Map<String, String>, @Query("key") key: String):
        Single<String>

    @GET("/public-key")
    fun publicKey(@Query("key") key: String):
        Single<String>
}

val ApiService by lazy {
    ApiServiceInterface.create()
}
