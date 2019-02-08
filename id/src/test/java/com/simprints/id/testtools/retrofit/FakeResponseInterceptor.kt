package com.simprints.id.testtools.retrofit

import com.simprints.id.commontesttools.getBuilderResponse
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

// OKHttpClient interceptor to return a specific response
class FakeResponseInterceptor(private val statusCode: Int, private val body: String = "", private val contentType: String = "\"application/json\"") : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response? {
        return getBuilderResponse(statusCode, body, contentType).request(chain.request()).build()
    }
}
