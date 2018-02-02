package com.simprints.id.secure

import com.simprints.id.BuildConfig
import com.simprints.id.secure.domain.Nonce
import com.simprints.id.secure.domain.PublicKeyString
import io.reactivex.Single
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.Query

interface ApiServiceInterface {

    companion object {
        fun create(): ApiServiceInterface {

            val baseUrl = "https://project-manager-dot-${BuildConfig.GCP_PROJECT}.appspot.com"

            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BASIC
            val client = OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build()

            val retrofit = Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(JsonHelper.gson))
                .baseUrl(baseUrl)
                .client(client)
                .build()

            return retrofit.create(ApiServiceInterface::class.java)
        }
    }

    @GET("/nonces")
    fun nonce(@HeaderMap headers: Map<String, String>, @Query("key") key: String):
        Single<Nonce>

    @GET("/public-key")
    fun publicKey(@Query("key") key: String):
        Single<PublicKeyString>
}

val ApiService by lazy {
    ApiServiceInterface.create()
}
