package com.simprints.testtools.common.retrofit

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

// OKHttpClient interceptor to return a specific response
class FakeResponseInterceptor(private val statusCode: Int,
                              private val body: String = "",
                              private val contentType: String = "\"application/json\"",
                              private val validateUrl: (url: String) -> Unit = {}) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        validateUrl(chain.request().url.toString())
        return getBuilderResponse(statusCode, body, contentType).request(chain.request()).build()
    }
}
