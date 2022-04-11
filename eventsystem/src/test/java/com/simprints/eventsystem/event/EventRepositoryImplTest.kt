@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.simprints.eventsystem.event

import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.modality.Modes
import com.simprints.core.login.LoginInfoManager
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.utils.randomUUID
import com.simprints.eventsystem.event.EventRepositoryImpl.Companion.PROJECT_ID_FOR_NOT_SIGNED_IN
import com.simprints.eventsystem.event.EventRepositoryImpl.Companion.SESSION_BATCH_SIZE
import com.simprints.eventsystem.event.domain.models.ArtificialTerminationEvent.ArtificialTerminationPayload.Reason.NEW_SESSION
import com.simprints.eventsystem.event.domain.models.EventLabels
import com.simprints.eventsystem.event.domain.models.EventType
import com.simprints.eventsystem.event.domain.models.EventType.SESSION_CAPTURE
import com.simprints.eventsystem.event.domain.models.session.SessionCaptureEvent
import com.simprints.eventsystem.event.domain.validators.EventValidator
import com.simprints.eventsystem.event.domain.validators.SessionEventValidatorsFactory
import com.simprints.eventsystem.event.local.EventLocalDataSource
import com.simprints.eventsystem.event.local.SessionDataCache
import com.simprints.eventsystem.event.remote.EventRemoteDataSource
import com.simprints.eventsystem.exceptions.TryToUploadEventsForNotSignedProject
import com.simprints.eventsystem.exceptions.validator.DuplicateGuidSelectEventValidatorException
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.eventsystem.sampledata.SampleDefaults.GUID1
import com.simprints.eventsystem.sampledata.SampleDefaults.GUID2
import com.simprints.eventsystem.sampledata.SampleDefaults.GUID3
import com.simprints.eventsystem.sampledata.createAlertScreenEvent
import io.kotest.assertions.throwables.shouldThrow
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class EventRepositoryImplTest {

    private lateinit var eventRepo: EventRepository

    @MockK
    lateinit var loginInfoManager: LoginInfoManager

    @MockK
    lateinit var eventLocalDataSource: EventLocalDataSource

    @MockK
    lateinit var eventRemoteDataSource: EventRemoteDataSource

    @MockK
    lateinit var timeHelper: TimeHelper

    @MockK
    lateinit var sessionEventValidatorsFactory: SessionEventValidatorsFactory

    @MockK
    lateinit var eventValidator: EventValidator

    @MockK
    lateinit var sessionDataCache: SessionDataCache

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        every { timeHelper.now() } returns NOW
        every { loginInfoManager.getSignedInProjectIdOrEmpty() } returns DEFAULT_PROJECT_ID
        every { sessionDataCache.eventCache } returns mutableMapOf()
        every { sessionEventValidatorsFactory.build() } returns arrayOf(eventValidator)

        eventRepo = EventRepositoryImpl(
            DEVICE_ID,
            APP_VERSION_NAME,
            loginInfoManager,
            eventLocalDataSource,
            eventRemoteDataSource,
            timeHelper,
            sessionEventValidatorsFactory,
            LIB_VERSION_NAME,
            sessionDataCache,
            LANGUAGE,
            listOf(Modes.FACE, Modes.FINGERPRINT)
        )

        runBlocking {
            coEvery { eventLocalDataSource.loadAll() } returns emptyFlow()
            mockDbToLoadPersonRecordEvents(0)
        }
    }

    @Test
    fun createSession_shouldHaveTheRightSessionCount() {
        runBlocking {
            mockDbToHaveOneOpenSession()
            coEvery { eventLocalDataSource.count(SESSION_CAPTURE) } returns N_SESSIONS_DB

            eventRepo.createSession()

            coVerify {
                eventLocalDataSource.insertOrUpdate(match<SessionCaptureEvent> {
                    it.payload.databaseInfo.sessionCount == N_SESSIONS_DB
                })
            }
        }
    }

    @Test
    fun createSession_ForEmptyProjectId() {
        runBlocking {
            every { loginInfoManager.getSignedInProjectIdOrEmpty() } returns ""
            coEvery { eventLocalDataSource.count(SESSION_CAPTURE) } returns N_SESSIONS_DB

            val session = eventRepo.createSession()

            assertThat(session.payload.projectId).isEqualTo(PROJECT_ID_FOR_NOT_SIGNED_IN)

        }
    }

    @Test(expected = DuplicateGuidSelectEventValidatorException::class)
    fun createSession_ReportDuplicateGuidSelectEventValidatorExceptionException() {
        runBlocking {
            coEvery { eventLocalDataSource.count(SESSION_CAPTURE) } returns N_SESSIONS_DB
            coEvery {
                eventLocalDataSource.insertOrUpdate(any())
            } throws DuplicateGuidSelectEventValidatorException("oops...")
            val session = eventRepo.createSession()
        }
    }

    @Test
    fun createSession_shouldCloseOpenSessionEvents() {
        runBlocking {
            val openSession = mockDbToHaveOneOpenSession()

            eventRepo.createSession()

            coVerify {
                eventLocalDataSource.insertOrUpdate(match {
                    assertThatArtificialTerminationEventWasAdded(it, openSession.id)
                })
            }

            coVerify {
                eventLocalDataSource.insertOrUpdate(match {
                    assertThatSessionCaptureEventWasClosed(it)
                })
            }
        }
    }

    @Test
    fun createSession_shouldAddANewSessionEvent() {
        runBlocking {
            mockDbToBeEmpty()

            eventRepo.createSession()

            coVerify {
                eventLocalDataSource.insertOrUpdate(match {
                    assertANewSessionCaptureWasAdded(it)
                })
            }
        }
    }

    @Test
    fun addEventIntoASession_shouldStoreItIntoTheDbWithRightLabels() {
        runBlocking {
            val session = mockDbToHaveOneOpenSession()
            val newEvent = createAlertScreenEvent()

            eventRepo.addOrUpdateEvent(newEvent)

            coVerify {
                eventLocalDataSource.insertOrUpdate(
                    newEvent.copy(
                        labels = EventLabels(
                            sessionId = GUID1,
                            deviceId = DEVICE_ID,
                            projectId = DEFAULT_PROJECT_ID
                        )
                    )
                )
            }
        }
    }

    @Test
    fun addEvent_shouldStoreItIntoTheDbWithRightLabels() {
        runBlocking {
            val session = mockDbToHaveOneOpenSession()
            val newEvent = createAlertScreenEvent()
            newEvent.labels = EventLabels()

            eventRepo.addOrUpdateEvent(newEvent)

            coVerify {
                eventLocalDataSource.insertOrUpdate(
                    newEvent.copy(
                        labels = EventLabels(
                            deviceId = DEVICE_ID,
                            projectId = DEFAULT_PROJECT_ID,
                            sessionId = GUID1
                        )
                    )
                )
            }
        }
    }

    @Test
    fun addEventToCurrentSession_shouldAddEventRelatedToCurrentSessionIntoDb() {
        runBlocking {
            mockDbToHaveOneOpenSession(GUID1)
            val newEvent = createAlertScreenEvent()

            eventRepo.addOrUpdateEvent(newEvent)

            coVerify {
                eventLocalDataSource.insertOrUpdate(
                    newEvent.copy(
                        labels = EventLabels(
                            sessionId = GUID1,
                            deviceId = DEVICE_ID,
                            projectId = DEFAULT_PROJECT_ID
                        )
                    )
                )
            }
        }
    }

    @Test
    fun upload_shouldLoadAllEventsPartOfSessionsToUpload() {
        runBlocking {
            mockDbToLoadTwoClosedSessionsWithEvents(2 * SESSION_BATCH_SIZE, GUID1, GUID2)

            eventRepo.uploadEvents(DEFAULT_PROJECT_ID,
                canSyncAllData = false,
                canSyncBiometricData = false,
                canSyncAnalyticsData = false
            ).toList()

            coVerify { eventLocalDataSource.loadAllFromSession(sessionId = GUID1) }
            coVerify { eventLocalDataSource.loadAllFromSession(sessionId = GUID2) }
        }
    }

    @Test
    fun upload_shouldNotUploadOpenSession() {
        runBlocking {
            mockDbToLoadTwoClosedSessionsWithEvents(2 * SESSION_BATCH_SIZE)
            mockDbToLoadOpenSession(GUID3)

            eventRepo.uploadEvents(DEFAULT_PROJECT_ID,
                canSyncAllData = false,
                canSyncBiometricData = false,
                canSyncAnalyticsData = false
            ).toList()

            verifySessionHasNotGotUploaded(GUID3)
        }
    }

    @Test
    fun upload_shouldNotUploadSessionsForNotSignedProject() {
        runBlocking {
            shouldThrow<TryToUploadEventsForNotSignedProject> {
                eventRepo.uploadEvents(randomUUID(),
                    canSyncAllData = false,
                    canSyncBiometricData = false,
                    canSyncAnalyticsData = false
                ).toList()
            }
        }
    }

    @Test
    fun upload_succeeds_shouldDeleteEvents() {
        runBlocking {
            val events =
                mockDbToLoadTwoClosedSessionsWithEvents(2 * SESSION_BATCH_SIZE) +
                    mockDbToLoadPersonRecordEvents(SESSION_BATCH_SIZE / 2)

            eventRepo.uploadEvents(DEFAULT_PROJECT_ID,
                canSyncAllData = true,
                canSyncBiometricData = false,
                canSyncAnalyticsData = false
            ).toList()

            coVerify {
                eventLocalDataSource.delete(events.filter { it.labels.sessionId == GUID1 }
                    .map { it.id })
                eventLocalDataSource.delete(events.filter { it.labels.sessionId == GUID2 }
                    .map { it.id })
            }
        }
    }

    @Test
    fun upload_inProgress_shouldEmitProgress() {
        runBlocking {
            mockDbToLoadTwoClosedSessionsWithEvents(2 * SESSION_BATCH_SIZE)
            mockDbToLoadPersonRecordEvents(SESSION_BATCH_SIZE / 2)

            val progress = eventRepo.uploadEvents(DEFAULT_PROJECT_ID,
                canSyncAllData = false,
                canSyncBiometricData = false,
                canSyncAnalyticsData = false
            ).toList()

            assertThat(progress[0]).isEqualTo(SESSION_BATCH_SIZE)
            assertThat(progress[1]).isEqualTo(SESSION_BATCH_SIZE)
            assertThat(progress[2]).isEqualTo(SESSION_BATCH_SIZE / 2)
        }
    }

    @Test
    fun upload_succeeds_shouldDeleteUploadedEvents() {
        runBlocking {
            val events =
                mockDbToLoadTwoClosedSessionsWithEvents(2 * SESSION_BATCH_SIZE) +
                    mockDbToLoadPersonRecordEvents(SESSION_BATCH_SIZE / 2)

            eventRepo.uploadEvents(DEFAULT_PROJECT_ID,
                canSyncAllData = true,
                canSyncBiometricData = false,
                canSyncAnalyticsData = false
            ).toList()

            coVerify {
                eventLocalDataSource.delete(events.filter { it.labels.sessionId == GUID1 }
                    .map { it.id })
                eventLocalDataSource.delete(events.filter { it.labels.sessionId == GUID2 }
                    .map { it.id })
            }
        }
    }

    @Test
    fun upload_fails_shouldNotDeleteEventsAfterNetworkIssues() {
        runBlocking {
            mockDbToLoadTwoClosedSessionsWithEvents(2 * SESSION_BATCH_SIZE)
            coEvery { eventRemoteDataSource.post(any(), any()) } throws Throwable("Network issue")

            eventRepo.uploadEvents(DEFAULT_PROJECT_ID,
                canSyncAllData = false,
                canSyncBiometricData = false,
                canSyncAnalyticsData = false
            ).toList()

            coVerify(exactly = 0) { eventLocalDataSource.delete(any()) }
        }
    }

    @Test
    fun upload_fails_shouldNotDeleteSessionEventsAfterIntegrationIssues() {
        runBlocking {
            coEvery { eventRemoteDataSource.post(any(), any()) } throws HttpException(
                Response.error<String>(
                    404,
                    "".toResponseBody(null)
                )
            )

            val events = mockDbToLoadTwoClosedSessionsWithEvents(2 * SESSION_BATCH_SIZE)
            val subjectEvents = mockDbToLoadPersonRecordEvents(SESSION_BATCH_SIZE / 2)

            eventRepo.uploadEvents(DEFAULT_PROJECT_ID,
                canSyncAllData = false,
                canSyncBiometricData = false,
                canSyncAnalyticsData = false
            ).toList()

            coVerify(exactly = 0) {
                eventLocalDataSource.delete(events.filter { it.labels.sessionId == GUID1 }
                    .map { it.id })
                eventLocalDataSource.delete(events.filter { it.labels.sessionId == GUID2 }
                    .map { it.id })
            }

            coVerify(exactly = 0) { eventLocalDataSource.delete(subjectEvents.map { it.id }) }
        }
    }

    @Test
    fun createSession_shouldAddArtificialTerminationEventToThePreviousOne() {
        runBlocking {
            mockDbToHaveOneOpenSession(GUID1)

            eventRepo.createSession()

            coVerify { eventLocalDataSource.loadAllSessions(false) }
            verifyArtificialEventWasAdded(GUID1, NEW_SESSION)
        }
    }

    @Test
    fun getCurrentOpenSession() {
        runBlocking {
            val session = mockDbToHaveOneOpenSession(GUID1)

            val loadedSession = eventRepo.getCurrentCaptureSessionEvent()
            assertThat(loadedSession).isEqualTo(session)
        }
    }

    @Test
    fun insertEventIntoCurrentOpenSession() {
        runBlocking {
            mockSignedId()
            val session = mockDbToHaveOneOpenSession(GUID1)
            val eventInSession = createAlertScreenEvent().removeLabels()

            eventRepo.addOrUpdateEvent(eventInSession)

            coVerify { eventLocalDataSource.loadAllSessions(false) }
            coVerify {
                eventLocalDataSource.insertOrUpdate(
                    eventInSession.copy(
                        labels = EventLabels(
                            deviceId = DEVICE_ID,
                            sessionId = session.id,
                            projectId = DEFAULT_PROJECT_ID
                        )
                    )
                )
            }
        }
    }

    @Test
    fun insertEventIntoCurrentOpenSession_shouldInvokeValidators() {
        runBlocking {
            mockSignedId()
            val session = mockDbToHaveOneOpenSession(GUID1)
            val eventInSession = createAlertScreenEvent().removeLabels()
            coEvery { eventLocalDataSource.loadAllFromSession(sessionId = session.id) } returns listOf(
                session,
                eventInSession
            )
            val newEvent = createAlertScreenEvent().removeLabels()

            eventRepo.addOrUpdateEvent(newEvent)

            verify { eventValidator.validate(listOf(eventInSession), newEvent) }
        }
    }

    @Test
    fun `should close current session correctly`() = runBlockingTest {
        val session = mockDbToHaveOneOpenSession(GUID1)
        eventRepo.closeCurrentSession(null)

        assertThatSessionCaptureEventWasClosed(session)
        coVerify(exactly = 0) {
            eventLocalDataSource.insertOrUpdate(match { it.type == EventType.ARTIFICIAL_TERMINATION })
        }
    }

    private fun mockSignedId() =
        every { loginInfoManager.getSignedInProjectIdOrEmpty() } returns DEFAULT_PROJECT_ID

    companion object {
        const val DEVICE_ID = "DEVICE_ID"
        const val APP_VERSION_NAME = "APP_VERSION_NAME"
        const val LIB_VERSION_NAME = "LIB_VERSION_NAME"
        const val LANGUAGE = "en"

        const val N_SESSIONS_DB = 3
        const val NOW = 1000L
    }
}
