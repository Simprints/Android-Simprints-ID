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

    private val otherErrorResponse =
        "{\"some\":\"thing\"}"
    private val backendMaintenanceErroresponse = "{\"error\":\"002\"}"

    @Test
    fun gettingNoBackendMaintenanceErrorReturnsFalse() {
        assertThat(Throwable().isBackendMaintenanceException()).isFalse()
    }

    @Test
    fun gettingBackendMaintenanceErrorReturnsTrue() {
        val throwable = createThrowable(503, backendMaintenanceErroresponse)

        assertThat(throwable.isBackendMaintenanceException()).isTrue()
    }

    @Test
    fun gettingExceptionWitResponseReturnsFalse() {
        val throwable = createThrowable(503, null)

        assertThat(throwable.isBackendMaintenanceException()).isFalse()
    }

    @Test
    fun gettingExceptionWithNullResponseReturnsFalse() {
        val exception: HttpException = mockk()

        every {
            exception.response()?.errorBody()
        } returns null

        every {
            exception.response()?.code()
        } returns 503

        assertThat(exception.isBackendMaintenanceException()).isFalse()
    }

    @Test
    fun gettingExceptionWithNullErrorBodyStringReturnsFalse() {
        val exception: HttpException = mockk()

        every {
            exception.response()?.errorBody()?.string()
        } returns null

        every {
            exception.response()?.code()
        } returns 503

        assertThat(exception.isBackendMaintenanceException()).isFalse()
    }

    @Test
    fun gettingNoBackendErrorReturnsFalse() {
        val throwable = createThrowable(500, otherErrorResponse)

        assertThat(throwable.isBackendMaintenanceException()).isFalse()
    }

    @Test
    fun gettingNoBackendErrorReturnsFalseWith503() {
        val throwable = createThrowable(503, otherErrorResponse)

        assertThat(throwable.isBackendMaintenanceException()).isFalse()
    }

    @Test
    fun gettingNoHeadersReturnsNull() {
        assertThat(Throwable().getEstimatedOutage()).isNull()
    }

    @Test
    fun gettingBackendMaintenanceErrorWithNoHeaderReturns0() {
        val throwable = createThrowable(503, backendMaintenanceErroresponse)
        assertThat(throwable.isBackendMaintenanceException()).isTrue()
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

    @Test
    fun gettingBackendErrorWithNullHeaderReturns0() {
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
            .add("Retry-After", "hello")
            .build()

        assertThat(exception.getEstimatedOutage()).isNull()
    }

    @Test
    fun gettingBackendErrorWithInvalidHeaderReturnsNull() {
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
        } returns null

        assertThat(exception.getEstimatedOutage()).isEqualTo(0L)
    }

    private fun createThrowable(code: Int, errorResponse: String?): Throwable {
        val errorResponseBody = errorResponse?.toResponseBody("application/json".toMediaTypeOrNull())
        val mockResponse = errorResponseBody?.let { Response.error<Any>(code, it) }
        return if (mockResponse != null) HttpException(mockResponse) else Throwable()
    }
}
