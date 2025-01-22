package com.simprints.infra.logging.writers

import co.touchlab.kermit.Logger
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.simprints.infra.logging.Simber
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.spyk
import io.mockk.unmockkObject
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class CrashlyticsLogWriterTest {
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
        val crashMock = mockk<FirebaseCrashlytics>(relaxed = true)
        val spyCrashReportingTree = spyk(CrashlyticsLogWriter(crashMock))

        Logger.setLogWriters(spyCrashReportingTree)
        Simber.d("Test Message")

        verify(exactly = 0) { crashMock.log(any()) }
    }

    @Test
    fun `should log on INFO priority`() {
        val crashMock = mockk<FirebaseCrashlytics>(relaxed = true)
        val spyCrashReportingTree = spyk(CrashlyticsLogWriter(crashMock))

        Logger.setLogWriters(spyCrashReportingTree)
        Simber.i("Test Message", null)

        verify { crashMock.log("[${Simber.DEFAULT_TAG}] Test Message") }
    }

    @Test
    fun `should log custom tag as part of message`() {
        val crashMock = mockk<FirebaseCrashlytics>(relaxed = true)
        val spyCrashReportingTree = spyk(CrashlyticsLogWriter(crashMock))

        Logger.setLogWriters(spyCrashReportingTree)
        Simber.i("Test Message", tag = "TAG")

        verify { crashMock.log("[TAG] Test Message") }
    }

    @Test
    fun `should log and record error on WARN priority`() {
        val crashMock = mockk<FirebaseCrashlytics>(relaxed = true)
        val spyCrashReportingTree = spyk(CrashlyticsLogWriter(crashMock))

        Logger.setLogWriters(spyCrashReportingTree)
        Simber.w("Test Message", null)

        verify {
            crashMock.recordException(
                withArg {
                    it is Exception && it.message.contentEquals("Test Message")
                },
            )
        }
    }

    @Test
    fun `with custom exception should log and record error on WARN priority`() {
        val crashMock = mockk<FirebaseCrashlytics>(relaxed = true)
        val spyCrashReportingTree = spyk(CrashlyticsLogWriter(crashMock))

        val custException = Exception("Custom Exception")

        Logger.setLogWriters(spyCrashReportingTree)
        Simber.w("Test Message", custException)

        verify {
            crashMock.log(withArg { it.contains("Test Message") })
        }
        verify {
            crashMock.recordException(custException)
        }
    }

    @Test
    fun `with custom exception should log and record error on ERROR priority`() {
        val crashMock = mockk<FirebaseCrashlytics>(relaxed = true)
        val spyCrashReportingTree = spyk(CrashlyticsLogWriter(crashMock))

        val custException = Exception("Custom Exception")

        Logger.setLogWriters(spyCrashReportingTree)
        Simber.e("Test Message", custException)

        verify {
            crashMock.log(withArg { it.contains("Test Message") })
        }
        verify {
            crashMock.recordException(custException)
        }
    }

    @Test
    fun `should log crashlytics user property with tags`() {
        val crashMock = mockk<FirebaseCrashlytics>(relaxed = true)
        val spyCrashReportingTree = spyk(CrashlyticsLogWriter(crashMock))

        Logger.setLogWriters(spyCrashReportingTree)
        Simber.setUserProperty("Custom_Tag", "Test Message")

        verify {
            crashMock.setCustomKey("Custom_Tag", "Test Message")
        }
    }
}
