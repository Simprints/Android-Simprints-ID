package com.simprints.testtools.common.retrofit

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.mock.BehaviorDelegate
import retrofit2.mock.MockRetrofit
import retrofit2.mock.NetworkBehavior
import java.util.concurrent.TimeUnit

inline fun <reified T> createMockBehaviorService(retrofit: Retrofit, failurePercent: Int, service: Class<T>): BehaviorDelegate<T> {
    val networkBehavior = NetworkBehavior.create()
    givenNetworkFailurePercentIs(networkBehavior, failurePercent)

    val mockRetrofit = MockRetrofit.Builder(retrofit)
        .networkBehavior(networkBehavior)
        .build()
    return mockRetrofit.create(service)
}

fun buildResponse(statusCode: Int, body: String = "", contentType: String = "\"text/plain\""): Response {
    return getBuilderResponse(statusCode, body, contentType).build()
}

fun getBuilderResponse(statusCode: Int, body: String = "", contentType: String = "\"text/plain\""): Response.Builder {
    return Response.Builder()
        .code(statusCode)
        .message(body)
        .protocol(Protocol.HTTP_1_0)
        .body(ResponseBody.create(contentType.toMediaTypeOrNull(), body.toByteArray()))
        .addHeader("content-type", contentType)
        .request(Request.Builder().url("http://localhost").build())
}

fun givenNetworkFailurePercentIs(behavior: NetworkBehavior, failurePercent: Int) {
    behavior.setDelay(0, TimeUnit.MILLISECONDS)
    behavior.setVariancePercent(0)
    behavior.setFailurePercent(failurePercent)
}
