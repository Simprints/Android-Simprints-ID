package com.simprints.core.tools.exceptions

import com.simprints.infra.logging.Simber
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class ErrorHandlerHelperTest {
    val simber = mockk<Simber>(relaxed = true) {
        every { tag(any()) } returns this
    }

    @Before
    fun setUp() {
        mockkObject(Simber)
        every { Simber.INSTANCE } returns simber
    }

    @After
    fun cleanUp() {
        unmockkObject(Simber)
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

        verify(exactly = 1) { simber.i(any(), exception) }
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
