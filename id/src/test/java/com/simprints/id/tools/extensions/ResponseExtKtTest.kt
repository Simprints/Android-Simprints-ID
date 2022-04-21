package com.simprints.id.tools.extensions

import com.google.common.truth.Truth.assertThat
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test
import retrofit2.Response

class ResponseExtKtTest {

    @Test
    fun gettingNoBackendMaintenanceErrorReturnsFalse() {
        assertThat(Response.success("").isBackendMaitenanceException()).isFalse()
    }

    @Test
    fun gettingBackendMaintenanceErrorReturnsTrue() {
        val errorResponse =
            "{\"error\":\"002\"}"
        val errorResponseBody = errorResponse.toResponseBody("application/json".toMediaTypeOrNull())
        val mockResponse = Response.error<Any>(503, errorResponseBody)

        assertThat(mockResponse.isBackendMaitenanceException()).isTrue()
    }

    @Test
    fun gettingHttpErrorReturnsFalse() {
        val errorResponse =
            "{\"some\":\"data\"}"
        val errorResponseBody = errorResponse.toResponseBody("application/json".toMediaTypeOrNull())
        val mockResponse = Response.error<Any>(503, errorResponseBody)

        assertThat(mockResponse.isBackendMaitenanceException()).isFalse()
    }
}
