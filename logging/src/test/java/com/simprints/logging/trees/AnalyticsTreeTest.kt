package com.simprints.logging.trees

import com.google.firebase.analytics.FirebaseAnalytics
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.Test

class AnalyticsTreeTest {

    @Test
    fun `should return on VERBOSE priority`() {
        val faMock = mockk<FirebaseAnalytics>(relaxed = true)
        val spyAnalyticsTree = spyk(AnalyticsTree(faMock))

        spyAnalyticsTree.v("Test Message")

        verify(exactly = 0) { faMock.logEvent(any(), any()) }
    }

    @Test
    fun `should return on DEBUG priority`() {
        val faMock = mockk<FirebaseAnalytics>(relaxed = true)
        val spyAnalyticsTree = spyk(AnalyticsTree(faMock))

        spyAnalyticsTree.d("Test Message")

        verify(exactly = 0) { faMock.logEvent(any(), any()) }
    }

    @Test
    fun `should log event on INFO priority`() {
        val faMock = mockk<FirebaseAnalytics>(relaxed = true)
        val spyAnalyticsTree = spyk(AnalyticsTree(faMock))

        spyAnalyticsTree.i("Test Message")
        
        verify {
            faMock.logEvent("DEFAULT", withArg {
                it.getString("DEFAULT").contentEquals("Test Message")
            })
        }
    }

}
