package com.simprints.id.data.license.remote

import com.simprints.id.network.TimberLogger
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

object NetworkComponentsFactory {
    fun getLicenseServer(): SimprintsLicenseServer = getRetrofit().create(SimprintsLicenseServer::class.java)

    private fun getRetrofit(): Retrofit = Retrofit.Builder()
        .baseUrl(SimprintsLicenseServer.BASE_URL)
        .client(getOkHttpClientConfig().build())
        .addConverterFactory(ScalarsConverterFactory.create())
        .build()

    private fun getOkHttpClientConfig(): OkHttpClient.Builder {
        val loggingInterceptor = HttpLoggingInterceptor(TimberLogger()).apply {
            level = HttpLoggingInterceptor.Level.HEADERS
        }

        return OkHttpClient.Builder()
            .addInterceptor(BasicAuthInterceptor("VYHffwmvMxiaoxzm", "3fM01e10sn5Vq6FV2EVd"))
            .addInterceptor(loggingInterceptor)
    }
}
