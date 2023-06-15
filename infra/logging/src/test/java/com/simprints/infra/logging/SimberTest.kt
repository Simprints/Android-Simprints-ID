package com.simprints.infra.logging

import com.google.firebase.FirebaseNetworkException
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
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
