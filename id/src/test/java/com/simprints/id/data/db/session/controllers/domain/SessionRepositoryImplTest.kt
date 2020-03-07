package com.simprints.id.data.db.session.controllers.domain

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.EncodingUtils
import com.simprints.id.commontesttools.sessionEvents.createFakeOpenSession
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.db.person.domain.FingerIdentifier.RIGHT_THUMB
import com.simprints.id.data.db.person.domain.FingerprintSample
import com.simprints.id.data.db.session.SessionRepository
import com.simprints.id.data.db.session.SessionRepositoryImpl
import com.simprints.id.data.db.session.domain.models.SessionQuery
import com.simprints.id.data.db.session.domain.models.events.*
import com.simprints.id.data.db.session.domain.models.events.FingerprintCaptureEvent.Fingerprint
import com.simprints.id.data.db.session.domain.models.events.FingerprintCaptureEvent.Result.GOOD_SCAN
import com.simprints.id.data.db.session.domain.models.events.FingerprintCaptureEvent.Result.SKIPPED
import com.simprints.id.data.db.session.domain.models.events.callback.IdentificationCallbackEvent
import com.simprints.id.data.db.session.domain.models.session.SessionEvents
import com.simprints.id.data.db.session.local.SessionLocalDataSource
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.services.scheduledSync.sessionSync.SessionEventsSyncManager
import com.simprints.id.testtools.TestApplication
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.TimeHelperImpl
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import io.kotlintest.shouldThrow
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class SessionRepositoryImplTest {

    private val timeHelper: TimeHelper = TimeHelperImpl()
    @MockK private lateinit var sessionEventsSyncManagerMock: SessionEventsSyncManager
    @MockK private lateinit var sessionLocalDataSourceMock: SessionLocalDataSource
    @MockK private lateinit var preferencesManagerMock: PreferencesManager
    @MockK private lateinit var crashReportManagerMock: CrashReportManager
    private lateinit var sessionsRepository: SessionRepository

    @Before
    fun setUp() {
        ShadowLog.stream = System.out
        MockKAnnotations.init(this, relaxed = true)

        sessionsRepository = SessionRepositoryImpl(
            DEVICE_ID,
            APP_VERSION_NAME,
            sessionEventsSyncManagerMock, sessionLocalDataSourceMock, preferencesManagerMock, timeHelper, crashReportManagerMock)
        mockPreferenceManagerInfo()
    }

    private fun mockPreferenceManagerInfo() {
        every { preferencesManagerMock.language } returns LANGUAGE
    }

    @Test
    fun createSession_shouldCreateASession() {
        runBlocking {
            sessionsRepository.createSession(LIB_VERSION_NAME)

            coVerify(exactly = 1) { sessionLocalDataSourceMock.create(APP_VERSION_NAME, LIB_VERSION_NAME, LANGUAGE, DEVICE_ID) }
            coVerify(exactly = 1) { preferencesManagerMock.language }
        }
    }

    @Test
    fun createSession_shouldReportExceptionAndThrow() {
        runBlockingTest {
            every { preferencesManagerMock.language } throws Throwable("Error")

            shouldThrow<Throwable> {
                sessionsRepository.createSession("")
            }

            coVerify(exactly = 1) { crashReportManagerMock.logExceptionOrSafeException(any()) }
        }
    }

    @Test
    fun addGuidSelectionEvent_shouldAddBeAdded() {
        runBlockingTest {
            val session = createFakeOpenSession(TimeHelperImpl()).apply {
                this.events.add(IdentificationCallbackEvent(0, id, emptyList()))
            }
            coEvery { sessionLocalDataSourceMock.updateCurrentSession(any()) } answers {
                val block = (args.first() as (SessionEvents) -> Unit)
                block(session)
            }

            sessionsRepository.addGuidSelectionEvent("selected_guid", session.id)

            assertThat(session.events.last()).isInstanceOf(GuidSelectionEvent::class.java)
        }
    }

    @Test
    fun addGuidSelectionEventInANotIdentificationSession_shouldNotBeAdded() {
        runBlockingTest {
            val session = createFakeOpenSession(TimeHelperImpl()).apply {
                this.events.addAll(listOf(
                    IdentificationCallbackEvent(0, id, emptyList()),
                    GuidSelectionEvent(0, id))
                )
            }
            coEvery { sessionLocalDataSourceMock.updateCurrentSession(any()) } answers {
                val block = (args.first() as (SessionEvents) -> Unit)
                block(session)
            }

            sessionsRepository.addGuidSelectionEvent("selected_guid", session.id)

            assertThat(session.events.count()).isEqualTo(2)
        }
    }

    @Test
    fun addGuidSelectionEventInASessionWithGuidSelectionEvent_shouldNotBeAdded() {
        runBlockingTest {
            val session = createFakeOpenSession(TimeHelperImpl())
            coEvery { sessionLocalDataSourceMock.updateCurrentSession(any()) } answers {
                val block = (args.first() as (SessionEvents) -> Unit)
                block(session)
            }

            sessionsRepository.addGuidSelectionEvent("selected_guid", session.id)

            assertThat(session.events.count()).isEqualTo(0)
        }
    }

    @Test
    fun addGuidSelectionEventInASessionWithGuidSelectionEvent_shouldReportExceptionAndThrow() {
        runBlockingTest {
            coEvery { sessionLocalDataSourceMock.updateCurrentSession(any()) } throws Throwable("Error")
            shouldThrow<Throwable> {
                sessionsRepository.addGuidSelectionEvent("selected_guid", "session_id")
            }
            coVerify(exactly = 1) { crashReportManagerMock.logExceptionOrSafeException(any()) }
        }
    }

    @Test
    fun addPersonCreationEvent() {
        runBlockingTest {
            val session = createFakeOpenSession(TimeHelperImpl())
            val goodScanEvent = FingerprintCaptureEvent(0, 0, RIGHT_THUMB, 10, GOOD_SCAN, Fingerprint(RIGHT_THUMB, 50, "good\n"))
            val badScanEvent = FingerprintCaptureEvent(0, 0, RIGHT_THUMB, 10, SKIPPED, Fingerprint(RIGHT_THUMB, 50, "bad\n"))
            session.events.addAll(arrayListOf<Event>(goodScanEvent, badScanEvent))
            coEvery { sessionLocalDataSourceMock.updateCurrentSession(any()) } answers {
                val block = (args.first() as (SessionEvents) -> Unit)
                block(session)
            }

            sessionsRepository.addPersonCreationEvent(listOf(
                FingerprintSample(RIGHT_THUMB, EncodingUtils.base64ToBytes("good"), 50)
            ))

            with(session.events.last() as PersonCreationEvent) {
                assertThat(fingerprintCaptureIds.first()).isEqualTo(goodScanEvent.id)
                assertThat(fingerprintCaptureIds.count()).isEqualTo(1)
            }
        }
    }

    @Test
    fun addPersonCreationEvent_shouldReportExceptionAndThrow() {
        runBlockingTest {
            coEvery { sessionLocalDataSourceMock.updateCurrentSession(any()) } throws Throwable("Error")
            shouldThrow<Throwable> {
                sessionsRepository.addPersonCreationEvent(listOf())
            }
            coVerify(exactly = 1) { crashReportManagerMock.logExceptionOrSafeException(any()) }
        }
    }

    @Test
    fun updateHardwareVersionInScannerConnectivityEvent() {
        runBlockingTest {
            val session = createFakeOpenSession(TimeHelperImpl())
            session.events.add(ScannerConnectionEvent(0, ScannerConnectionEvent.ScannerInfo("scannerId", "macAddress", "")))
            coEvery { sessionLocalDataSourceMock.updateCurrentSession(any()) } answers {
                val block = (args.first() as (SessionEvents) -> Unit)
                block(session)
            }

            sessionsRepository.updateHardwareVersionInScannerConnectivityEvent("hardwareVersion")

            with(session.events.last() as ScannerConnectionEvent) {
                assertThat(this.scannerInfo.hardwareVersion).isEqualTo("hardwareVersion")
            }
        }
    }

    @Test
    fun updateHardwareVersionInScannerConnectivityEvent_shouldReportExceptionAndThrow() {
        runBlockingTest {
            coEvery { sessionLocalDataSourceMock.updateCurrentSession(any()) } throws Throwable("Error")
            shouldThrow<Throwable> {
                sessionsRepository.updateHardwareVersionInScannerConnectivityEvent("hardwareVersion")
            }
            coVerify(exactly = 1) { crashReportManagerMock.logExceptionOrSafeException(any()) }
        }
    }

    @Test
    fun getCurrentSession_shouldReturnCurrentSession() {
        runBlockingTest {
            val session = createFakeOpenSession(TimeHelperImpl())
            coEvery { sessionLocalDataSourceMock.load(any()) } returns flowOf(session)

            val currentSession = sessionsRepository.getCurrentSession()

            assertThat(currentSession).isEqualTo(session)
            coVerify(exactly = 1) { sessionLocalDataSourceMock.load(SessionQuery(openSession = true)) }
        }
    }

    @Test
    fun getCurrentSession_shouldReportExceptionAndThrow() {
        runBlockingTest {
            coEvery { sessionLocalDataSourceMock.updateCurrentSession(any()) } throws Throwable("Error")
            shouldThrow<Throwable> {
                sessionsRepository.getCurrentSession()
            }
            coVerify(exactly = 1) { crashReportManagerMock.logExceptionOrSafeException(any()) }
        }
    }

    @Test
    fun updateCurrentSession_shouldUpdateCurrentSession() {
        runBlockingTest {
            val block: (SessionEvents) -> Unit = {}
            sessionsRepository.updateCurrentSession(block)
            coVerify(exactly = 1) { sessionLocalDataSourceMock.updateCurrentSession(block) }
        }
    }

    @Test
    fun updateCurrentSession_shouldReportExceptionAndThrow() {
        runBlockingTest {
            coEvery { sessionLocalDataSourceMock.updateCurrentSession(any()) } throws Throwable("Error")
            shouldThrow<Throwable> {
                sessionsRepository.updateCurrentSession { }
            }
            coVerify(exactly = 1) { crashReportManagerMock.logExceptionOrSafeException(any()) }
        }
    }

    @Test
    fun addEventToCurrentSessionInBackground_shouldAddEventIntoCurrentSession() {
        runBlockingTest {
            val event = mockk<Event>()
            sessionsRepository.addEventToCurrentSessionInBackground(event)
            coVerify(exactly = 1) { sessionLocalDataSourceMock.addEventToCurrentSession(event) }
        }
    }

    @Test
    fun addEventToCurrentSessionInBackground_shouldReportException() {
        runBlockingTest {
            coEvery { sessionLocalDataSourceMock.addEventToCurrentSession(any()) } throws Throwable("Error")
            sessionsRepository.addEventToCurrentSessionInBackground(mockk())
            coVerify(exactly = 1) { crashReportManagerMock.logExceptionOrSafeException(any()) }
        }
    }


    @Test
    fun signOut_shouldDeleteSessionsAndStopWorkers() {
        runBlockingTest {
            sessionsRepository.signOut()

            coVerify(exactly = 1) { sessionLocalDataSourceMock.delete(SessionQuery(openSession = false)) }
            coVerify(exactly = 1) { sessionEventsSyncManagerMock.cancelSyncWorkers() }
        }
    }

    companion object {
        private const val DEVICE_ID = "deviceId"
        private const val APP_VERSION_NAME = "v1"
        private const val LIB_VERSION_NAME = "v1"
        private const val LANGUAGE = "en"
    }
}
