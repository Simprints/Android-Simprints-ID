package com.simprints.core.tools.extentions

import com.google.common.truth.Truth
import io.mockk.every
import io.mockk.mockk

import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class ThrowableKtTest {

    @Test
    fun `test isCloudRecoverableIssue should be false for non HttpException` () {
        val throwable = Throwable()
        Truth.assertThat(throwable.isCloudRecoverableIssue()).isFalse()
    }
    @Test
    fun `test isCloudRecoverableIssue should be false for HttpException non RecoverableCloudIssues` () {

        val response = mockk<Response<String>>(relaxed = true)
        val throwable = HttpException(response )
        Truth.assertThat(throwable.isCloudRecoverableIssue()).isFalse()
    }
    @Test
    fun `test isCloudRecoverableIssue should be true  for HttpException and RecoverableCloudIssues` () {

        val response = mockk<Response<String>>(relaxed = true)
        every { response.code() } returns 500
        val throwable = HttpException(response )
        Truth.assertThat(throwable.isCloudRecoverableIssue()).isTrue()
    }
}
