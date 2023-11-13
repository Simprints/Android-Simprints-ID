package com.simprints.core.tools.exceptions

import com.simprints.infra.logging.Simber
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ErrorHandlerHelperTest {

    @Test
    fun `Ignoring an exception should not throw`() = runTest {
        ignoreException {
           throwException()
        }
    }

    @Test
    fun `Ignoring an exception should print to debug console`() = runTest {
        mockkStatic(Simber::class)
        spyk(Simber) {
            val exception = IllegalStateException()

            @Suppress("IMPLICIT_NOTHING_TYPE_ARGUMENT_IN_RETURN_POSITION")
            ignoreException {
                throw exception
            }

            verify(exactly = 1) { this@spyk.d(exception) }
        }

    }

    @Test
    fun `Ignoring an exception should return null`() = runTest {

        val rtn = ignoreException {
            throwException()
        }

        assert(rtn == null)
    }

    private fun throwException() {
        throw IllegalStateException()
    }

}
