package com.simprints.infra.backendapi

import com.google.common.truth.Truth.*
import org.junit.Test

class ApiResultTest {
    @Test
    fun `getOrThrow returns data when Success`() {
        val value = ApiResult.Success("ok").getOrThrow()

        assertThat(value).isEqualTo("ok")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `getOrThrow throws cause when Failure`() {
        ApiResult.Failure<String>(IllegalArgumentException("bad")).getOrThrow()
    }

    @Test
    fun `getOrMapFailure returns data when Success`() {
        val value = ApiResult.Success("ok").getOrMapFailure { "fallback" }

        assertThat(value).isEqualTo("ok")
    }

    @Test
    fun `getOrMapFailure maps failure when Failure`() {
        val result: ApiResult<String> = ApiResult.Failure(IllegalStateException("bad"))
        val value = result.getOrMapFailure { failure -> "mapped:${failure.cause.message}" }

        assertThat(value).isEqualTo("mapped:bad")
    }
}
