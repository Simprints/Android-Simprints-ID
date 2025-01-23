package com.simprints.infra.logging

import co.touchlab.kermit.Logger
import co.touchlab.kermit.LoggerConfig
import co.touchlab.kermit.Severity
import com.google.firebase.FirebaseNetworkException
import com.simprints.infra.logging.Simber.USER_PROPERTY_TAG
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLHandshakeException
import javax.net.ssl.SSLProtocolException

class SimberTest {
    @Before
    fun setUp() {
        mockkObject(Logger)

        // All of the Logger methods are implemented as inline, so we have to mock more
        // than usually to accommodate for the inlined code as well.
        every { Logger.tag } returns "tag"
        every { Logger.config } returns mockk<LoggerConfig> {
            every { minSeverity } returns Severity.Verbose
        }
        justRun { Logger.processLog(any(), any(), any(), any<String>()) }
    }

    @Test
    fun `setUserProperty() sets correctly the user property tag`() {
        Simber.setUserProperty("test", "value")
        verify(exactly = 1) { Logger.processLog(Severity.Info, USER_PROPERTY_TAG + "test", null, "value") }
    }

    @Test
    fun `tag sets correctly`() {
        every { Logger.withTag(any()) } returns Logger
        Simber.d("message", tag = "test")

        verify(exactly = 1) {
            Logger.processLog(Severity.Debug, "test", any<Exception>(), "message")
        }
    }

    @Test
    fun `in debug mode tag throws an exception for tags containing invalid characters`() {
        val exception = kotlin.runCatching { Simber.i("message", tag = "test:tag") }

        assertEquals(IllegalArgumentException::class.java, exception.exceptionOrNull()?.javaClass)
        assertEquals("Tag must consist of letters, digits or _ (underscores).", exception.exceptionOrNull()?.message)
    }

    @Test
    fun `in debug mode tag throws an exception for tags longer than 40 characters`() {
        val exception = kotlin.runCatching {
            Simber.setUserProperty("01234567890123456789012345678901234567890", "value")
        }

        assertEquals(IllegalArgumentException::class.java, exception.exceptionOrNull()?.javaClass)
        assertEquals("String must be less than 40 characters.", exception.exceptionOrNull()?.message)
    }

    @Test
    fun `reports crash reporting when logging warnings with message`() {
        Simber.w("test", IllegalStateException("Test"))
        verify(exactly = 1) { Logger.processLog(Severity.Warn, any<String>(), any<Exception>(), "test") }
    }

    @Test
    fun `reports crash reporting when logging error with message`() {
        Simber.e("test", IllegalStateException("Test"))
        verify(exactly = 1) { Logger.processLog(Severity.Error, any<String>(), any<Exception>(), "test") }
    }

    @Test
    fun `skips false-positive crash reporting when logging warnings with message`() {
        val list = getListOfSkippableExceptions()
        list.forEach { Simber.w("test", it) }

        verify(exactly = 0) { Logger.processLog(Severity.Warn, any<String>(), any<Exception>(), "test") }
        verify(exactly = list.size) { Logger.processLog(Severity.Info, any(), any<Exception>(), "test") }
    }

    @Test
    fun `skips false-positive crash reporting when logging error with message`() {
        val list = getListOfSkippableExceptions()
        list.forEach { Simber.e("test", it) }

        verify(exactly = 0) { Logger.processLog(Severity.Error, any<String>(), any<Exception>(), "test") }
        verify(exactly = list.size) { Logger.processLog(Severity.Info, any(), any<Exception>(), "test") }
    }

    @Test
    fun `skips false-positive cause crash reporting when logging warnings with message`() {
        val list = getListOfSkippableExceptions().map { Throwable(cause = it) }
        list.forEach { Simber.w("test", it) }

        verify(exactly = 0) { Logger.processLog(Severity.Warn, any<String>(), any<Exception>(), "test") }
        verify(exactly = list.size) { Logger.processLog(Severity.Info, any(), any<Exception>(), "test") }
    }

    @Test
    fun `skips false-positive cause crash reporting when logging error with message`() {
        val list = getListOfSkippableExceptions().map { Throwable(cause = it) }
        list.forEach { Simber.e("test", it) }

        verify(exactly = 0) { Logger.processLog(Severity.Error, any<String>(), any<Exception>(), "test") }
        verify(exactly = list.size) { Logger.processLog(Severity.Info, any(), any<Exception>(), "test") }
    }

    private fun getListOfSkippableExceptions() = listOf(
        SocketTimeoutException(),
        UnknownHostException(),
        SSLProtocolException("Stub"),
        SSLHandshakeException("Stub"),
        FirebaseNetworkException("Stub"),
    )
}
