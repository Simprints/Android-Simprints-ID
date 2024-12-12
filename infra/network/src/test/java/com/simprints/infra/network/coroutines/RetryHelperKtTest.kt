package com.simprints.infra.network.coroutines

import com.google.common.truth.Truth.assertThat
import com.simprints.testtools.common.syntax.assertThrows
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test

@ExperimentalCoroutinesApi
class RetryHelperKtTest {
    @Test
    fun `should not retry if there is no exception thrown`() = runTest(StandardTestDispatcher()) {
        var nbCall = 0
        retryIO(
            times = 5,
            runBlock = {
                nbCall++
            },
            retryIf = { true },
        )
        assertThat(nbCall).isEqualTo(1)
    }

    @Test
    fun `should not retry if the condition is false and throw the exception`() = runTest(StandardTestDispatcher()) {
        var nbCall = 0
        val exception = RuntimeException("exception")
        val thrownException = assertThrows<RuntimeException> {
            retryIO(
                times = 5,
                runBlock = {
                    nbCall++
                    throw exception
                },
                retryIf = { false },
            )
        }
        assertThat(nbCall).isEqualTo(1)
        assertThat(thrownException).isEqualTo(exception)
    }

    @Test
    fun `should retry if the condition is true and throw the exception at the end`() = runTest(StandardTestDispatcher()) {
        var nbCall = 0
        val exception = RuntimeException("exception")
        val thrownException = assertThrows<RuntimeException> {
            retryIO(
                times = 5,
                runBlock = {
                    nbCall++
                    throw exception
                },
                retryIf = { true },
            )
        }
        assertThat(nbCall).isEqualTo(5)
        assertThat(thrownException).isEqualTo(exception)
    }

    @Test
    fun `should retry until the condition is false and throw the last exception at the end`() = runTest(StandardTestDispatcher()) {
        var nbCall = 0
        val exception = RuntimeException("exception")
        val otherException = RuntimeException("other exception")
        val thrownException = assertThrows<RuntimeException> {
            retryIO(
                times = 5,
                runBlock = {
                    nbCall++
                    if (nbCall <= 1) {
                        throw exception
                    } else {
                        throw otherException
                    }
                },
                retryIf = { it == exception },
            )
        }
        assertThat(nbCall).isEqualTo(2)
        assertThat(thrownException).isEqualTo(otherException)
    }

    @Test
    fun `should retry until there is no exception thrown`() = runTest(StandardTestDispatcher()) {
        var nbCall = 0
        val exception = RuntimeException("exception")
        retryIO(
            times = 5,
            runBlock = {
                nbCall++
                if (nbCall <= 1) {
                    throw exception
                }
                return@retryIO
            },
            retryIf = { it == exception },
        )
        assertThat(nbCall).isEqualTo(2)
    }
}
