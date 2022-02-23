package com.simprints.core.tools.extentions

import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import okhttp3.Headers
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class ThrowableExtKtTest {

    private val errorResponse =
        "{\"some\":\"thing\"}"
    private val backendMaintenanceErroresponse = "{\"error\":\"002\"}"

    @Test
    fun gettingNoBackendMaintenanceErrorReturnsFalse() {
        assertThat(Throwable().isBackendMaintenanceException()).isFalse()
    }

    @Test
    fun gettingBackendMaintenanceErrorReturnsTrue() {
        val throwable = createHttpException(503, backendMaintenanceErroresponse)

        assertThat(throwable.isBackendMaintenanceException()).isTrue()
    }

    @Test
    fun gettingNoBackendErrorReturnsFalse() {
        val throwable = createHttpException(500, errorResponse)

        assertThat(throwable.isBackendMaintenanceException()).isFalse()
    }

    @Test
    fun gettingNoHeadersReturnsNull() {
        assertThat(Throwable().getEstimatedOutage()).isNull()
    }

    @Test
    fun gettingBackendMaintenanceErrorWithNoHeaderReturns0() {
        val throwable = createHttpException(503, backendMaintenanceErroresponse)
        assertThat(throwable.getEstimatedOutage()).isEqualTo(0L)
    }

    @Test
    fun gettingBackendErrorWithHeaderReturnsValidRetry() {
        val exception: HttpException = mockk()

        every {
            exception.response()
        } returns mockk()

        every {
            exception.response()?.code()
        } returns 503

        every {
            exception.response()?.errorBody()?.string()
        } returns backendMaintenanceErroresponse

        every {
            exception.response()?.headers()
        } returns Headers.Builder()
            .add("Retry-After", "600")
            .build()

        assertThat(exception.getEstimatedOutage()).isEqualTo(600L)
    }

    private fun createHttpException(code: Int, errorResponse: String): HttpException {
        val errorResponseBody = errorResponse.toResponseBody("application/json".toMediaTypeOrNull())
        val mockResponse = Response.error<Any>(code, errorResponseBody)
        return HttpException(mockResponse)
    }
}
