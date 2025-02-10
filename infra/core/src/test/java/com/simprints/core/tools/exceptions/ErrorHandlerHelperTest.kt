package com.simprints.core.tools.exceptions

import com.simprints.infra.logging.Simber
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class ErrorHandlerHelperTest {
    @Before
    fun setUp() {
        // Not using mockObject since the verified method is annotated with JvmStatic and it confuses the mockk library
        mockkStatic(Simber::class)
    }

    @After
    fun cleanUp() {
        unmockkStatic(Simber::class)
    }

    @Test
    fun `Ignoring an exception should not throw`() = runTest {
        ignoreException {
            throwException()
        }
    }

    @Test
    fun `Ignoring an exception should print to debug console`() = runTest {
        val exception = IllegalStateException()

        @Suppress("IMPLICIT_NOTHING_TYPE_ARGUMENT_IN_RETURN_POSITION")
        ignoreException {
            throw exception
        }

        verify(exactly = 1) { Simber.i(any<String>(), eq(exception), any<String>()) }
    }

    @Test
    fun `Ignoring an exception should return null`() = runTest {
        val rtn = ignoreException {
            throwException()
        }

        assert(rtn == null)
    }

    private fun throwException(): Unit = throw IllegalStateException()
}
