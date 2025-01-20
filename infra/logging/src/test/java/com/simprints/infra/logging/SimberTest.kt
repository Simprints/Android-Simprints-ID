package com.simprints.infra.logging

import co.touchlab.kermit.Logger
import co.touchlab.kermit.LoggerConfig
import co.touchlab.kermit.Severity
import com.google.firebase.FirebaseNetworkException
import com.simprints.infra.logging.Simber.Companion.USER_PROPERTY_TAG
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLHandshakeException
import javax.net.ssl.SSLProtocolException

class SimberTest {
    @MockK
    private lateinit var logger: Logger

    lateinit var simber: Simber

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = false)

        // All of the logger methods are implemented as inline, so we have to mock more
        // than usually to accommodate for the inlined code as well.
        every { logger.tag } returns "tag"
        every { logger.config } returns mockk<LoggerConfig> {
            every { minSeverity } returns Severity.Verbose
        }
        justRun { logger.processLog(any(), any(), any(), any<String>()) }

        simber = Simber("tag", logger)
    }

    @Test
    fun `setUserProperty() sets correctly the user property tag`() {
        simber.setUserProperty("test", "value")
        verify(exactly = 1) { logger.processLog(Severity.Info, USER_PROPERTY_TAG + "test", null, "value") }
    }

    @Test
    fun `tag() sets correctly the non-user property tag`() {
        every { logger.withTag(any()) } returns logger
        simber.tag("test").d("message")

        verify(exactly = 1) {
            logger.withTag("test")
            logger.processLog(Severity.Debug, any(), any<Exception>(), "message")
        }
    }

    @Test
    fun `in debug mode tag() throws an exception for tags containing invalid characters`() {
        val exception = kotlin.runCatching { simber.tag("test:tag") }

        assertEquals(IllegalArgumentException::class.java, exception.exceptionOrNull()?.javaClass)
        assertEquals("Tag must consist of letters, digits or _ (underscores).", exception.exceptionOrNull()?.message)
    }

    @Test
    fun `in debug mode tag() throws an exception for tags longer than 40 characters`() {
        val exception = kotlin.runCatching {
            simber.setUserProperty("01234567890123456789012345678901234567890", "value")
        }

        assertEquals(IllegalArgumentException::class.java, exception.exceptionOrNull()?.javaClass)
        assertEquals("String must be less than 40 characters.", exception.exceptionOrNull()?.message)
    }

    @Test
    fun `reports crash reporting when logging warnings with message`() {
        simber.w("test", IllegalStateException("Test"))
        verify(exactly = 1) { logger.processLog(Severity.Warn, any<String>(), any<Exception>(), "test") }
    }

    @Test
    fun `reports crash reporting when logging error with message`() {
        simber.e("test", IllegalStateException("Test"))
        verify(exactly = 1) { logger.processLog(Severity.Error, any<String>(), any<Exception>(), "test") }
    }

    @Test
    fun `skips false-positive crash reporting when logging warnings with message`() {
        val list = getListOfSkippableExceptions()
        list.forEach { simber.w("test", it) }

        verify(exactly = 0) { logger.processLog(Severity.Warn, any<String>(), any<Exception>(), "test") }
        verify(exactly = list.size) { logger.processLog(Severity.Info, any(), any<Exception>(), "test") }
    }

    @Test
    fun `skips false-positive crash reporting when logging error with message`() {
        val list = getListOfSkippableExceptions()
        list.forEach { simber.e("test", it) }

        verify(exactly = 0) { logger.processLog(Severity.Error, any<String>(), any<Exception>(), "test") }
        verify(exactly = list.size) { logger.processLog(Severity.Info, any(), any<Exception>(), "test") }
    }

    @Test
    fun `skips false-positive cause crash reporting when logging warnings with message`() {
        val list = getListOfSkippableExceptions().map { Throwable(cause = it) }
        list.forEach { simber.w("test", it) }

        verify(exactly = 0) { logger.processLog(Severity.Warn, any<String>(), any<Exception>(), "test") }
        verify(exactly = list.size) { logger.processLog(Severity.Info, any(), any<Exception>(), "test") }
    }

    @Test
    fun `skips false-positive cause crash reporting when logging error with message`() {
        val list = getListOfSkippableExceptions().map { Throwable(cause = it) }
        list.forEach { simber.e("test", it) }

        verify(exactly = 0) { logger.processLog(Severity.Error, any<String>(), any<Exception>(), "test") }
        verify(exactly = list.size) { logger.processLog(Severity.Info, any(), any<Exception>(), "test") }
    }

    private fun getListOfSkippableExceptions() = listOf(
        SocketTimeoutException(),
        UnknownHostException(),
        SSLProtocolException("Stub"),
        SSLHandshakeException("Stub"),
        FirebaseNetworkException("Stub"),
    )
}
