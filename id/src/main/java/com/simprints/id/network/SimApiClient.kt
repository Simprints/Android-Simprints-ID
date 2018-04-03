package com.simprints.id.network

import com.simprints.id.data.db.models.Project
import com.simprints.id.tools.json.JsonHelper
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

open class SimApiClient<T>(val service: Class<T>,
                           private val endpoint: String,
                           private val authToken: String? = null) {

    val api: T by lazy {
        retrofit.create(service)
    }

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(JsonHelper.gson))
            .baseUrl(endpoint)
            .client(okHttpClientConfig.build()).build()
    }

    val okHttpClientConfig: OkHttpClient.Builder by lazy {
        val logger = HttpLoggingInterceptor()
        logger.level = HttpLoggingInterceptor.Level.BODY
        OkHttpClient.Builder()
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(logger)
            .addInterceptor(authenticator)
    }

    private val authenticator = Interceptor { chain ->
        val newRequest = chain.request().newBuilder()
        if (!authToken.isNullOrBlank()) {
            newRequest.addHeader("Authorization", "Bearer " + authToken)
        }

        if (chain.request().url().toString().contains("/projects")) {
            val body = JsonHelper.toJson(Project().apply {
                projectId = "some_id"
                description = "some_description"
            })

            return@Interceptor Response.Builder()
                    .code(200)
                    .message(body)
                    .protocol(Protocol.HTTP_1_0)
                    .body(ResponseBody.create(MediaType.parse("application/json"), body.toByteArray()))
                    .addHeader("content-type", "application/json")
                    .request(Request.Builder().url("http://localhost").build()).build()
        } else {
            return@Interceptor chain.proceed(newRequest.build())
        }
    }
}
