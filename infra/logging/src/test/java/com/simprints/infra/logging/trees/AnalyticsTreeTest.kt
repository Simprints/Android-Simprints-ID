package com.simprints.infra.logging.trees

import com.google.firebase.analytics.FirebaseAnalytics
import com.simprints.infra.logging.LoggingConstants.AnalyticsUserProperties.USER_ID
import com.simprints.infra.logging.Simber
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.Test
import timber.log.Timber

class AnalyticsTreeTest {
    @Test
    fun `should return on DEBUG priority`() {
        val faMock = mockk<FirebaseAnalytics>(relaxed = true)
        val spyAnalyticsTree = spyk(AnalyticsTree(faMock))

        Timber.plant(spyAnalyticsTree)
        Simber.d("Test Message")

        verify(exactly = 0) { faMock.logEvent(any(), any()) }
    }

    @Test
    fun `should log user property on INFO priority with tag`() {
        val faMock = mockk<FirebaseAnalytics>(relaxed = true)
        val spyAnalyticsTree = spyk(AnalyticsTree(faMock))

        Timber.plant(spyAnalyticsTree)
        Simber.setUserProperty("Test_Tag", "Test Message")

        verify {
            faMock.setUserProperty("Test_Tag", "Test Message")
        }
    }

    @Test
    fun `should log user ID on INFO priority with tag`() {
        val faMock = mockk<FirebaseAnalytics>(relaxed = true)
        val spyAnalyticsTree = spyk(AnalyticsTree(faMock))

        Timber.plant(spyAnalyticsTree)
        Simber.setUserProperty(USER_ID, "Test Message ID")

        verify {
            faMock.setUserId("Test Message ID")
        }
    }
}
