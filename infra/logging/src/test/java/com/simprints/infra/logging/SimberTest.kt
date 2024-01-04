package com.simprints.infra.logging

import com.google.firebase.FirebaseNetworkException
import com.simprints.infra.logging.Simber.USER_PROPERTY_TAG
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Test
import timber.log.Timber
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLHandshakeException
import javax.net.ssl.SSLProtocolException

class SimberTest {

    @Before
    fun setUp() {
        mockkObject(Timber.Forest)
    }

    @After
    fun cleanUp() {
        unmockkObject(Timber.Forest)
    }

    @Test
    fun `tag() sets correctly the user property tag`() {
        Simber.tag("test", true)
        verify(exactly = 1) { Timber.tag(USER_PROPERTY_TAG + "test") }
    }

    @Test
    fun `tag() sets correctly the non-user property tag`() {
        Simber.tag("test")
        verify(exactly = 1) { Timber.tag("test") }
    }

    @Test
    fun `in debug mode tag() throws an exception for tags containing invalid characters`() {
        val exception = kotlin.runCatching { Simber.tag("test:tag") }

        assertEquals(IllegalArgumentException::class.java, exception.exceptionOrNull()?.javaClass)
        assertEquals("Tag must consist of letters, digits or _ (underscores).", exception.exceptionOrNull()?.message)
    }

    @Test
    fun `in debug mode tag() throws an exception for tags longer than 40 characters`() {
        val exception = kotlin.runCatching {
            Simber.tag("01234567890123456789012345678901234567890", true)
        }

        assertEquals(IllegalArgumentException::class.java, exception.exceptionOrNull()?.javaClass)
        assertEquals("String must be less than 40 characters.", exception.exceptionOrNull()?.message)
    }

    @Test
    fun `reports crash reporting when logging warnings`() {
        Simber.w(IllegalStateException("Test"))
        verify(exactly = 1) { Timber.Forest.w(any<Exception>()) }
    }

    @Test
    fun `reports crash reporting when logging warnings with message`() {
        Simber.w(IllegalStateException("Test"), "test", null)
        verify(exactly = 1) { Timber.Forest.w(any<Exception>(), any(), any()) }
    }

    @Test
    fun `reports crash reporting when logging error`() {
        Simber.e(IllegalStateException("Test"))
        verify(exactly = 1) { Timber.Forest.e(any<Exception>()) }
    }

    @Test
    fun `reports crash reporting when logging error with message`() {
        Simber.e(IllegalStateException("Test"), "test", null)
        verify(exactly = 1) { Timber.Forest.e(any<Exception>(), any(), any()) }
    }


    @Test
    fun `skips false-positive crash reporting when logging warnings`() {
        val list = getListOfSkippableExceptions()
        list.forEach { Simber.w(it) }

        verify(exactly = 0) { Timber.Forest.w(any<Exception>()) }
        verify(exactly = list.size) { Timber.Forest.i(any<Exception>()) }
    }

    @Test
    fun `skips false-positive crash reporting when logging warnings with message`() {
        val list = getListOfSkippableExceptions()
        list.forEach { Simber.w(it, "test", null) }

        verify(exactly = 0) { Timber.Forest.w(any<Exception>(), any(), any()) }
        verify(exactly = list.size) { Timber.Forest.i(any<Exception>(), any(), any()) }
    }

    @Test
    fun `skips false-positive crash reporting when logging error`() {
        val list = getListOfSkippableExceptions()
        list.forEach { Simber.e(it) }

        verify(exactly = 0) { Timber.Forest.e(any<Exception>()) }
        verify(exactly = list.size) { Timber.Forest.i(any<Exception>()) }
    }

    @Test
    fun `skips false-positive crash reporting when logging error with message`() {
        val list = getListOfSkippableExceptions()
        list.forEach { Simber.e(it, "test", null) }

        verify(exactly = 0) { Timber.Forest.e(any<Exception>(), any(), any()) }
        verify(exactly = list.size) { Timber.Forest.i(any<Exception>(), any(), any()) }
    }


    @Test
    fun `skips false-positive cause crash reporting when logging warnings`() {
        val list = getListOfSkippableExceptions().map { Throwable(cause = it) }

        list.forEach { Simber.w(it) }

        verify(exactly = 0) { Timber.Forest.w(any<Exception>()) }
        verify(exactly = list.size) { Timber.Forest.i(any<Exception>()) }
    }

    @Test
    fun `skips false-positive cause crash reporting when logging warnings with message`() {
        val list = getListOfSkippableExceptions().map { Throwable(cause = it) }
        list.forEach { Simber.w(it, "test", null) }

        verify(exactly = 0) { Timber.Forest.w(any<Exception>(), any(), any()) }
        verify(exactly = list.size) { Timber.Forest.i(any<Exception>(), any(), any()) }
    }

    @Test
    fun `skips false-positive cause crash reporting when logging error`() {
        val list = getListOfSkippableExceptions().map { Throwable(cause = it) }
        list.forEach { Simber.e(it) }

        verify(exactly = 0) { Timber.Forest.e(any<Exception>()) }
        verify(exactly = list.size) { Timber.Forest.i(any<Exception>()) }
    }

    @Test
    fun `skips false-positive cause crash reporting when logging error with message`() {
        val list = getListOfSkippableExceptions().map { Throwable(cause = it) }
        list.forEach { Simber.e(it, "test", null) }

        verify(exactly = 0) { Timber.Forest.e(any<Exception>(), any(), any()) }
        verify(exactly = list.size) { Timber.Forest.i(any<Exception>(), any(), any()) }
    }
    
    private fun getListOfSkippableExceptions() = listOf(
        SocketTimeoutException(),
        UnknownHostException(),
        SSLProtocolException("Stub"),
        SSLHandshakeException("Stub"),
        FirebaseNetworkException("Stub"),
    )

}
