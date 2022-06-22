package com.simprints.logging.trees

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.simprints.logging.Simber
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.Test
import timber.log.Timber

class CrashReportingTreeTest {

    @Test
    fun `should return on VERBOSE priority`() {
        val crashMock = mockk<FirebaseCrashlytics>(relaxed = true)
        val spyCrashReportingTree = spyk(CrashReportingTree(crashMock))

        Timber.plant(spyCrashReportingTree)
        Simber.v("Test Message")

        verify(exactly = 0) { crashMock.log(any()) }
    }

    @Test
    fun `should return on DEBUG priority`() {
        val crashMock = mockk<FirebaseCrashlytics>(relaxed = true)
        val spyCrashReportingTree = spyk(CrashReportingTree(crashMock))

        Timber.plant(spyCrashReportingTree)
        Simber.d("Test Message")

        verify(exactly = 0) { crashMock.log(any()) }
    }

    @Test
    fun `should log on INFO priority`() {
        val crashMock = mockk<FirebaseCrashlytics>(relaxed = true)
        val spyCrashReportingTree = spyk(CrashReportingTree(crashMock))

        Timber.plant(spyCrashReportingTree)
        Simber.i("Test Message")

        verify { crashMock.log("Test Message") }
    }

    @Test
    fun `should log and record error on WARN priority`() {
        val crashMock = mockk<FirebaseCrashlytics>(relaxed = true)
        val spyCrashReportingTree = spyk(CrashReportingTree(crashMock))

        Timber.plant(spyCrashReportingTree)
        Simber.w("Test Message")

        verify {
            crashMock.recordException(withArg {
                it is Exception && it.message.contentEquals("Test Message")
            })
        }
    }

    @Test
    fun `with custom exception should log and record error on WARN priority`() {
        val crashMock = mockk<FirebaseCrashlytics>(relaxed = true)
        val spyCrashReportingTree = spyk(CrashReportingTree(crashMock))

        val custException = Exception("Custom Exception")

        Timber.plant(spyCrashReportingTree)
        Simber.w(custException, "Test Message")

        verify {
            crashMock.log(withArg { it.contains("Test Message") })
        }
        verify {
            crashMock.recordException(custException)
        }
    }

    @Test
    fun `should log and record error on ERROR priority`() {
        val crashMock = mockk<FirebaseCrashlytics>(relaxed = true)
        val spyCrashReportingTree = spyk(CrashReportingTree(crashMock))

        Timber.plant(spyCrashReportingTree)
        Simber.e("Test Message")

        verify {
            crashMock.recordException(withArg {
                it is Exception && it.message.contentEquals("Test Message")
            })
        }
    }

    @Test
    fun `with custom exception should log and record error on ERROR priority`() {
        val crashMock = mockk<FirebaseCrashlytics>(relaxed = true)
        val spyCrashReportingTree = spyk(CrashReportingTree(crashMock))

        val custException = Exception("Custom Exception")

        Timber.plant(spyCrashReportingTree)
        Simber.e(custException, "Test Message")

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
        val spyCrashReportingTree = spyk(CrashReportingTree(crashMock))

        Timber.plant(spyCrashReportingTree)
        Simber.tag("Custom Tag", true).i("Test Message")

        verify {
            crashMock.setCustomKey("Custom Tag", "Test Message")
        }
    }


}
