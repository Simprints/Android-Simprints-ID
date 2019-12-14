package com.simprints.id.data.analytics.crashreport

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.crashlytics.android.core.CrashlyticsCore
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.any
import com.simprints.id.data.analytics.crashreport.CrashlyticsKeyConstants.Companion.FINGERS_SELECTED
import com.simprints.id.data.analytics.crashreport.CrashlyticsKeyConstants.Companion.MALFUNCTION_MESSAGE
import com.simprints.id.data.analytics.crashreport.CrashlyticsKeyConstants.Companion.MODULE_IDS
import com.simprints.id.data.analytics.crashreport.CrashlyticsKeyConstants.Companion.PEOPLE_DOWN_SYNC_TRIGGERS
import com.simprints.id.data.analytics.crashreport.CrashlyticsKeyConstants.Companion.PROJECT_ID
import com.simprints.id.data.analytics.crashreport.CrashlyticsKeyConstants.Companion.SESSION_ID
import com.simprints.id.data.analytics.crashreport.CrashlyticsKeyConstants.Companion.USER_ID
import com.simprints.id.data.db.person.domain.FingerIdentifier
import com.simprints.id.exceptions.safe.MalfunctionException
import com.simprints.id.exceptions.safe.secure.AuthRequestInvalidCredentialsException
import com.simprints.id.services.scheduledSync.sync.peopleDownSync.models.PeopleDownSyncTrigger
import com.simprints.id.testtools.TestApplication
import com.simprints.testtools.common.syntax.*
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class CrashReportManagerImplTest: AutoCloseKoinTest() {

    @Test
    fun logMessageForCrashReportTest_shouldLogInRightFormat() {
        val testMessageForCrash = "Test Message"
        val crashReportManagerSpy = spy(CrashReportManagerImpl())

        whenever(crashReportManagerSpy) { crashlyticsInstance } thenReturn mock()
        crashReportManagerSpy.logMessageForCrashReport(CrashReportTag.LOGIN, CrashReportTrigger.UI, Log.WARN, testMessageForCrash)

        verifyOnce(crashReportManagerSpy) { getLogMessage(CrashReportTrigger.UI, testMessageForCrash) }
        assertThat(crashReportManagerSpy.getLogMessage(CrashReportTrigger.UI, testMessageForCrash)).isEqualTo("[UI] $testMessageForCrash")
    }

    @Test
    fun logException_shouldLogExceptionInCrashlytics() {
        val crashReportManagerSpy = spy(CrashReportManagerImpl())
        val crashlyticsInstanceMock: CrashlyticsCore = mock()

        whenever(crashReportManagerSpy) { crashlyticsInstance } thenReturn  crashlyticsInstanceMock
        crashReportManagerSpy.logException(Throwable())

        verifyOnce(crashlyticsInstanceMock) { logException(anyNotNull()) }
    }

    @Test
    fun logSafeException_shouldLogExceptionAsCrashReportMessage() {
        val crashReportManagerSpy = spy(CrashReportManagerImpl())
        val crashlyticsInstanceMock: CrashlyticsCore = mock()
        val throwable = Throwable()

        whenever(crashReportManagerSpy) { crashlyticsInstance } thenReturn crashlyticsInstanceMock
        crashReportManagerSpy.logSafeException(throwable)

        verifyOnce(crashlyticsInstanceMock) { log(Log.ERROR, CrashReportTag.SAFE_EXCEPTION.name, "$throwable") }
    }

    @Test
    fun logExceptionOrSafeException_providedWithException_shouldLogException() {
        val crashReportManagerSpy = spy(CrashReportManagerImpl())
        val crashlyticsInstanceMock: CrashlyticsCore = mock()
        val throwable = Throwable()

        whenever(crashReportManagerSpy) { crashlyticsInstance } thenReturn crashlyticsInstanceMock
        crashReportManagerSpy.logExceptionOrSafeException(throwable)

        verifyOnce(crashReportManagerSpy) { logException(throwable) }
        verifyOnce(crashlyticsInstanceMock) { logException(throwable) }
    }

    @Test
    fun logExceptionOrSafeException_providedSafeException_shouldLogExceptionAsCrashReportMessage() {
        val crashReportManagerSpy = spy(CrashReportManagerImpl())
        val crashlyticsInstanceMock: CrashlyticsCore = mock()
        val safeThrowable = AuthRequestInvalidCredentialsException()

        whenever(crashReportManagerSpy) { crashlyticsInstance } thenReturn crashlyticsInstanceMock
        crashReportManagerSpy.logExceptionOrSafeException(safeThrowable)

        verifyOnce(crashReportManagerSpy) { logSafeException(safeThrowable) }
        verifyOnce(crashlyticsInstanceMock) { log(Log.ERROR, CrashReportTag.SAFE_EXCEPTION.name, "$safeThrowable") }
    }

    @Test
    fun setProjectIdCrashlyticsKey_shouldSetCorrectKeyValueInCrashlytics() {
        val crashReportManagerSpy = spy(CrashReportManagerImpl())
        val crashlyticsInstanceMock: CrashlyticsCore = mock()
        val testProject = "test_project"

        whenever(crashReportManagerSpy) { crashlyticsInstance } thenReturn crashlyticsInstanceMock
        crashReportManagerSpy.setProjectIdCrashlyticsKey(testProject)

        verifyOnce(crashlyticsInstanceMock) { setString(PROJECT_ID, testProject) }
    }

    @Test
    fun setUserIdCrashlyticsKey_shouldSetCorrectKeyValueInCrashlytics() {
        val crashReportManagerSpy = spy(CrashReportManagerImpl())
        val crashlyticsInstanceMock: CrashlyticsCore = mock()
        val testUser = "test_user"

        whenever(crashReportManagerSpy) { crashlyticsInstance } thenReturn crashlyticsInstanceMock
        crashReportManagerSpy.setUserIdCrashlyticsKey(testUser)

        verifyOnce(crashlyticsInstanceMock) { setString(USER_ID, testUser) }
    }

    @Test
    fun setModuleIdsCrashlyticsKey_shouldSetCorrectKeyValueInCrashlytics() {
        val crashReportManagerSpy = spy(CrashReportManagerImpl())
        val crashlyticsInstanceMock: CrashlyticsCore = mock()
        val moduleIds = setOf("module_1", "module_2", "module_3")

        whenever(crashReportManagerSpy) { crashlyticsInstance } thenReturn crashlyticsInstanceMock
        crashReportManagerSpy.setModuleIdsCrashlyticsKey(moduleIds)

        verifyOnce(crashlyticsInstanceMock) { setString(MODULE_IDS, moduleIds.toString()) }
    }

    @Test
    fun setSessionIdCrashlyticsKey_shouldSetCorrectKeyValueInCrashlytics() {
        val crashReportManagerSpy = spy(CrashReportManagerImpl())
        val crashlyticsInstanceMock: CrashlyticsCore = mock()
        val testSessionId = "test_session_id"

        whenever(crashReportManagerSpy) { crashlyticsInstance } thenReturn crashlyticsInstanceMock
        crashReportManagerSpy.setSessionIdCrashlyticsKey(testSessionId)

        verifyOnce(crashlyticsInstanceMock) { setString(SESSION_ID, testSessionId) }
    }

    @Test
    fun setFingersSelectedCrashlyticsKey_shouldSetCorrectKeyValueInCrashlytics() {
        val crashReportManagerSpy = spy(CrashReportManagerImpl())
        val crashlyticsInstanceMock: CrashlyticsCore = mock()
        val testFingersSelected = mapOf(
            FingerIdentifier.LEFT_3RD_FINGER to true,
            FingerIdentifier.LEFT_4TH_FINGER to true,
            FingerIdentifier.LEFT_THUMB to true,
            FingerIdentifier.RIGHT_4TH_FINGER to false
        )

        whenever(crashReportManagerSpy) { crashlyticsInstance } thenReturn crashlyticsInstanceMock
        crashReportManagerSpy.setFingersSelectedCrashlyticsKey(testFingersSelected)

        verifyOnce(crashlyticsInstanceMock) { setString(FINGERS_SELECTED, testFingersSelected.toString()) }
    }

    @Test
    fun setDownSyncTriggersCrashlyticsKey_shouldSetCorrectKeyValueInCrashlytics() {
        val crashReportManagerSpy = spy(CrashReportManagerImpl())
        val crashlyticsInstanceMock: CrashlyticsCore = mock()
        val testDownSyncTriggers = mapOf(
            PeopleDownSyncTrigger.MANUAL to true,
            PeopleDownSyncTrigger.ON_LAUNCH_CALLOUT to false,
            PeopleDownSyncTrigger.PERIODIC_BACKGROUND to true
        )

        whenever(crashReportManagerSpy) { crashlyticsInstance } thenReturn crashlyticsInstanceMock
        crashReportManagerSpy.setDownSyncTriggersCrashlyticsKey(testDownSyncTriggers)

        verifyOnce(crashlyticsInstanceMock) { setString(PEOPLE_DOWN_SYNC_TRIGGERS, testDownSyncTriggers.toString()) }
    }

    @Test
    fun logMalfunction_shouldSetUserMessageAsKeyAndLogMalfunctionException() {
        val crashReportManagerSpy = spy(CrashReportManagerImpl())
        val crashlyticsInstanceMock: CrashlyticsCore = mock()
        val userMessage = "user message for malfunction"

        whenever(crashReportManagerSpy) { crashlyticsInstance } thenReturn crashlyticsInstanceMock
        crashReportManagerSpy.logMalfunction(userMessage)

        verifyOnce(crashlyticsInstanceMock) { setString(MALFUNCTION_MESSAGE, userMessage) }
        verifyOnce(crashlyticsInstanceMock) { logException(any<MalfunctionException>()) }
    }
}
