package com.simprints.infra.logging.writers

import co.touchlab.kermit.Logger
import com.google.firebase.analytics.FirebaseAnalytics
import com.simprints.infra.logging.LoggingConstants.AnalyticsUserProperties.USER_ID
import com.simprints.infra.logging.Simber
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.spyk
import io.mockk.unmockkObject
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class AnalyticsPropertyLogWriterTest {
    @Before
    fun setUp() {
        mockkObject(Logger)
    }

    @After
    fun tearDown() {
        unmockkObject(Logger)
    }

    @Test
    fun `should return on DEBUG priority`() {
        val faMock = mockk<FirebaseAnalytics>(relaxed = true)
        val spyAnalyticsTree = spyk(AnalyticsPropertyLogWriter(faMock))

        Logger.setLogWriters(spyAnalyticsTree)
        Simber.d("Test Message")

        verify(exactly = 0) { faMock.logEvent(any(), any()) }
    }

    @Test
    fun `should log user property on INFO priority with tag`() {
        val faMock = mockk<FirebaseAnalytics>(relaxed = true)
        val spyAnalyticsTree = spyk(AnalyticsPropertyLogWriter(faMock))

        Logger.setLogWriters(spyAnalyticsTree)
        Simber.setUserProperty("Test_Tag", "Test Message")

        verify {
            faMock.setUserProperty("Test_Tag", "Test Message")
        }
    }

    @Test
    fun `should log user ID on INFO priority with tag`() {
        val faMock = mockk<FirebaseAnalytics>(relaxed = true)
        val spyAnalyticsTree = spyk(AnalyticsPropertyLogWriter(faMock))

        Logger.setLogWriters(spyAnalyticsTree)
        Simber.setUserProperty(USER_ID, "Test Message ID")

        verify {
            faMock.setUserId("Test Message ID")
        }
    }
}
