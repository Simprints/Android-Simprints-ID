package com.simprints.id.tools.retrofit

import com.simprints.id.secure.ApiServiceInterface
import com.simprints.id.secure.ApiServiceMock
import okhttp3.*
import retrofit2.Retrofit
import retrofit2.mock.MockRetrofit
import retrofit2.mock.NetworkBehavior
import java.util.concurrent.TimeUnit

fun createMockService(retrofit: Retrofit, failurePercent: Int): ApiServiceInterface {
    // Creating a mockServer with 100% of failure rate.
    val networkBehavior = NetworkBehavior.create()
    givenNetworkFailurePercentIs(networkBehavior, failurePercent)

    val mockRetrofit = MockRetrofit.Builder(retrofit)
        .networkBehavior(networkBehavior)
        .build()
    return ApiServiceMock(mockRetrofit.create(ApiServiceInterface::class.java))
}

fun createMockServiceToFailRequests(retrofit: Retrofit): ApiServiceInterface {
    return createMockService(retrofit, 100)
}

fun getBuilderResponse(statusCode: Int, body: String = "", contentType: String = "\"text/plain\""): Response.Builder {
    return Response.Builder()
        .code(statusCode)
        .message(body)
        .protocol(Protocol.HTTP_1_0)
        .body(ResponseBody.create(MediaType.parse(contentType), body.toByteArray()))
        .addHeader("content-type", contentType)
        .request(Request.Builder().url("http://localhost").build())
}

fun givenNetworkFailurePercentIs(behavior: NetworkBehavior, failurePercent: Int) {
    behavior.setDelay(0, TimeUnit.MILLISECONDS)
    behavior.setVariancePercent(0)
    behavior.setFailurePercent(failurePercent)
}

fun buildResponse(statusCode: Int, body: String = "", contentType: String = "\"text/plain\""): Response {
    return getBuilderResponse(statusCode, body, contentType).build()
}
