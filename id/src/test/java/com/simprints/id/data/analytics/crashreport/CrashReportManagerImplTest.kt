package com.simprints.id.data.analytics.crashreport

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.crashlytics.android.core.CrashlyticsCore
import com.google.common.truth.Truth.assertThat
import com.simprints.id.data.db.session.crashreport.CrashlyticsKeyConstants.Companion.FINGERS_SELECTED
import com.simprints.id.data.db.session.crashreport.CrashlyticsKeyConstants.Companion.MALFUNCTION_MESSAGE
import com.simprints.id.data.db.session.crashreport.CrashlyticsKeyConstants.Companion.MODULE_IDS
import com.simprints.id.data.db.session.crashreport.CrashlyticsKeyConstants.Companion.PEOPLE_DOWN_SYNC_TRIGGERS
import com.simprints.id.data.db.session.crashreport.CrashlyticsKeyConstants.Companion.PROJECT_ID
import com.simprints.id.data.db.session.crashreport.CrashlyticsKeyConstants.Companion.SESSION_ID
import com.simprints.id.data.db.session.crashreport.CrashlyticsKeyConstants.Companion.USER_ID
import com.simprints.id.data.db.person.domain.FingerIdentifier
import com.simprints.id.data.db.session.crashreport.CrashReportManagerImpl
import com.simprints.id.data.db.session.crashreport.CrashReportTag
import com.simprints.id.data.db.session.crashreport.CrashReportTrigger
import com.simprints.id.exceptions.safe.MalfunctionException
import com.simprints.id.exceptions.safe.secure.AuthRequestInvalidCredentialsException
import com.simprints.id.services.scheduledSync.people.master.models.PeopleDownSyncTrigger
import com.simprints.id.testtools.TestApplication
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class CrashReportManagerImplTest : AutoCloseKoinTest() {

    @Test
    fun logMessageForCrashReportTest_shouldLogInRightFormat() {
        val testMessageForCrash = "Test Message"
        val crashReportManagerSpy = spyk(CrashReportManagerImpl())

        every { crashReportManagerSpy.crashlyticsInstance } returns mockk(relaxed = true)
        crashReportManagerSpy.logMessageForCrashReport(CrashReportTag.LOGIN, CrashReportTrigger.UI, Log.WARN, testMessageForCrash)

        verify(atLeast = 1) { crashReportManagerSpy.getLogMessage(CrashReportTrigger.UI, testMessageForCrash) }
        assertThat(crashReportManagerSpy.getLogMessage(CrashReportTrigger.UI, testMessageForCrash)).isEqualTo("[UI] $testMessageForCrash")
    }

    @Test
    fun logException_shouldLogExceptionInCrashlytics() {
        val crashReportManagerSpy = spyk(CrashReportManagerImpl())
        val crashlyticsInstanceMock: CrashlyticsCore = mockk()

        every { crashReportManagerSpy.crashlyticsInstance } returns crashlyticsInstanceMock
        crashReportManagerSpy.logException(Throwable())

        verify(atLeast = 1) { crashlyticsInstanceMock.logException(any()) }
    }

    @Test
    fun logSafeException_shouldLogExceptionAsCrashReportMessage() {
        val crashReportManagerSpy = spyk(CrashReportManagerImpl())
        val crashlyticsInstanceMock: CrashlyticsCore = mockk()
        val throwable = Throwable()

        every { crashReportManagerSpy.crashlyticsInstance } returns crashlyticsInstanceMock
        crashReportManagerSpy.logSafeException(throwable)

        verify(atLeast = 1) { crashlyticsInstanceMock.log(Log.ERROR, CrashReportTag.SAFE_EXCEPTION.name, "$throwable") }
    }

    @Test
    fun logExceptionOrSafeException_providedWithException_shouldLogException() {
        val crashReportManagerSpy = spyk(CrashReportManagerImpl())
        val crashlyticsInstanceMock: CrashlyticsCore = mockk()
        val throwable = Throwable()

        every { crashReportManagerSpy.crashlyticsInstance } returns crashlyticsInstanceMock
        crashReportManagerSpy.logExceptionOrSafeException(throwable)

        verify(atLeast = 1) { crashReportManagerSpy.logException(throwable) }
        verify(atLeast = 1) { crashlyticsInstanceMock.logException(throwable) }
    }

    @Test
    fun logExceptionOrSafeException_providedSafeException_shouldLogExceptionAsCrashReportMessage() {
        val crashReportManagerSpy = spyk(CrashReportManagerImpl())
        val crashlyticsInstanceMock: CrashlyticsCore = mockk()
        val safeThrowable = AuthRequestInvalidCredentialsException()

        every { crashReportManagerSpy.crashlyticsInstance } returns crashlyticsInstanceMock
        crashReportManagerSpy.logExceptionOrSafeException(safeThrowable)

        verify(atLeast = 1) { crashReportManagerSpy.logSafeException(safeThrowable) }
        verify(atLeast = 1) { crashlyticsInstanceMock.log(Log.ERROR, CrashReportTag.SAFE_EXCEPTION.name, "$safeThrowable") }
    }

    @Test
    fun setProjectIdCrashlyticsKey_shouldSetCorrectKeyValueInCrashlytics() {
        val crashReportManagerSpy = spyk(CrashReportManagerImpl())
        val crashlyticsInstanceMock: CrashlyticsCore = mockk()
        val testProject = "test_project"

        every { crashReportManagerSpy.crashlyticsInstance } returns crashlyticsInstanceMock
        crashReportManagerSpy.setProjectIdCrashlyticsKey(testProject)

        verify(atLeast = 1) { crashlyticsInstanceMock.setString(PROJECT_ID, testProject) }
    }

    @Test
    fun setUserIdCrashlyticsKey_shouldSetCorrectKeyValueInCrashlytics() {
        val crashReportManagerSpy = spyk(CrashReportManagerImpl())
        val crashlyticsInstanceMock: CrashlyticsCore = mockk()
        val testUser = "test_user"

        every { crashReportManagerSpy.crashlyticsInstance } returns crashlyticsInstanceMock
        crashReportManagerSpy.setUserIdCrashlyticsKey(testUser)

        verify(atLeast = 1) { crashlyticsInstanceMock.setString(USER_ID, testUser) }
    }

    @Test
    fun setModuleIdsCrashlyticsKey_shouldSetCorrectKeyValueInCrashlytics() {
        val crashReportManagerSpy = spyk(CrashReportManagerImpl())
        val crashlyticsInstanceMock: CrashlyticsCore = mockk()
        val moduleIds = setOf("module_1", "module_2", "module_3")

        every { crashReportManagerSpy.crashlyticsInstance } returns crashlyticsInstanceMock
        crashReportManagerSpy.setModuleIdsCrashlyticsKey(moduleIds)

        verify(atLeast = 1) { crashlyticsInstanceMock.setString(MODULE_IDS, moduleIds.toString()) }
    }

    @Test
    fun setSessionIdCrashlyticsKey_shouldSetCorrectKeyValueInCrashlytics() {
        val crashReportManagerSpy = spyk(CrashReportManagerImpl())
        val crashlyticsInstanceMock: CrashlyticsCore = mockk()
        val testSessionId = "test_session_id"

        every { crashReportManagerSpy.crashlyticsInstance } returns crashlyticsInstanceMock
        crashReportManagerSpy.setSessionIdCrashlyticsKey(testSessionId)

        verify(atLeast = 1) { crashlyticsInstanceMock.setString(SESSION_ID, testSessionId) }
    }

    @Test
    fun setFingersSelectedCrashlyticsKey_shouldSetCorrectKeyValueInCrashlytics() {
        val crashReportManagerSpy = spyk(CrashReportManagerImpl())
        val crashlyticsInstanceMock: CrashlyticsCore = mockk()
        val testFingersSelected = mapOf(
            FingerIdentifier.LEFT_3RD_FINGER to true,
            FingerIdentifier.LEFT_4TH_FINGER to true,
            FingerIdentifier.LEFT_THUMB to true,
            FingerIdentifier.RIGHT_4TH_FINGER to false
        )

        every { crashReportManagerSpy.crashlyticsInstance } returns crashlyticsInstanceMock
        crashReportManagerSpy.setFingersSelectedCrashlyticsKey(testFingersSelected)

        verify(atLeast = 1) { crashlyticsInstanceMock.setString(FINGERS_SELECTED, testFingersSelected.toString()) }
    }

    @Test
    fun setDownSyncTriggersCrashlyticsKey_shouldSetCorrectKeyValueInCrashlytics() {
        val crashReportManagerSpy = spyk(CrashReportManagerImpl())
        val crashlyticsInstanceMock: CrashlyticsCore = mockk()
        val testDownSyncTriggers = mapOf(
            PeopleDownSyncTrigger.MANUAL to true,
            PeopleDownSyncTrigger.ON_LAUNCH_CALLOUT to false,
            PeopleDownSyncTrigger.PERIODIC_BACKGROUND to true
        )

        every { crashReportManagerSpy.crashlyticsInstance } returns crashlyticsInstanceMock
        crashReportManagerSpy.setDownSyncTriggersCrashlyticsKey(testDownSyncTriggers)

        verify(atLeast = 1) { crashlyticsInstanceMock.setString(PEOPLE_DOWN_SYNC_TRIGGERS, testDownSyncTriggers.toString()) }
    }

    @Test
    fun logMalfunction_shouldSetUserMessageAsKeyAndLogMalfunctionException() {
        val crashReportManagerSpy = spyk(CrashReportManagerImpl())
        val crashlyticsInstanceMock: CrashlyticsCore = mockk()
        val userMessage = "user message for malfunction"

        every { crashReportManagerSpy.crashlyticsInstance } returns crashlyticsInstanceMock
        crashReportManagerSpy.logMalfunction(userMessage)

        verify(atLeast = 1) { crashlyticsInstanceMock.setString(MALFUNCTION_MESSAGE, userMessage) }
        verify(atLeast = 1) { crashlyticsInstanceMock.logException(any<MalfunctionException>()) }
    }
}
