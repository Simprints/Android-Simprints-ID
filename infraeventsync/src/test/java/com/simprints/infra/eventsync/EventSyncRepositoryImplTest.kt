package com.simprints.infra.eventsync

import com.fasterxml.jackson.core.JsonParseException
import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.modality.Modes
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.utils.randomUUID
import com.simprints.infra.events.EventRepository
import com.simprints.infra.eventsync.event.remote.ApiModes
import com.simprints.infra.eventsync.event.remote.EventRemoteDataSource
import com.simprints.infra.events.sampledata.*
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.infra.events.sampledata.SampleDefaults.GUID1
import com.simprints.infra.events.sampledata.SampleDefaults.GUID2
import com.simprints.infra.eventsync.exceptions.TryToUploadEventsForNotSignedProject
import com.simprints.infra.eventsync.status.down.domain.RemoteEventQuery
import com.simprints.infra.login.LoginManager
import com.simprints.infra.network.exceptions.NetworkConnectionException
import io.kotest.assertions.throwables.shouldThrow
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

internal class EventSyncRepositoryImplTest {

    private lateinit var eventSyncRepo: EventSyncRepository

    @MockK
    lateinit var loginManager: LoginManager

    @MockK
    lateinit var eventRepo: EventRepository

    @MockK
    private lateinit var eventRemoteDataSource: EventRemoteDataSource

    @MockK
    lateinit var timeHelper: TimeHelper

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        every { timeHelper.now() } returns NOW
        every { loginManager.getSignedInProjectIdOrEmpty() } returns DEFAULT_PROJECT_ID

        eventSyncRepo = EventSyncRepositoryImpl(
            loginManager,
            eventRepo,
            eventRemoteDataSource,
            timeHelper,
        )
    }

    @Test
    fun `call event repository to count upload events`() = runTest {
        coEvery { eventRepo.observeEventCount(any(), any()) } returns flowOf(3)

        assertThat(eventSyncRepo.countEventsToUpload(DEFAULT_PROJECT_ID, null).firstOrNull())
            .isEqualTo(3)
    }

    @Test
    fun `upload fetches events for all provided closed sessions`() = runTest {
        coEvery { eventRepo.getAllClosedSessionIds(any()) } returns listOf(GUID1, GUID2)
        coEvery { eventRepo.getEventsFromSession(GUID1) } returns listOf(createSessionCaptureEvent(GUID1))
        coEvery { eventRepo.getEventsFromSession(GUID2) } returns listOf(createSessionCaptureEvent(GUID2))

        eventSyncRepo.uploadEvents(
            DEFAULT_PROJECT_ID,
            canSyncAllDataToSimprints = false,
            canSyncBiometricDataToSimprints = false,
            canSyncAnalyticsDataToSimprints = false
        ).toList()

        coVerify(exactly = 2) { eventRepo.getEventsFromSession(any()) }
    }


    @Test
    fun `upload should not filter any events on upload`() = runTest {
        coEvery { eventRepo.getAllClosedSessionIds(any()) } returns listOf(GUID1)
        coEvery { eventRepo.getEventsFromSession(any()) } returns listOf(
            createAuthenticationEvent(),
            createEnrolmentEventV2(),
        )

        eventSyncRepo.uploadEvents(
            DEFAULT_PROJECT_ID,
            canSyncAllDataToSimprints = true,
            canSyncBiometricDataToSimprints = false,
            canSyncAnalyticsDataToSimprints = false
        ).toList()

        coVerify {
            eventRemoteDataSource.post(
                any(),
                withArg { assertThat(it).hasSize(2) },
                any()
            )
        }
    }

    @Test
    fun `upload should filter biometric events on upload`() = runTest {
        coEvery { eventRepo.getAllClosedSessionIds(any()) } returns listOf(GUID1)
        coEvery { eventRepo.getEventsFromSession(any()) } returns listOf(
            createAuthenticationEvent(),
            createAlertScreenEvent(),
            // only following should be uploaded
            createEnrolmentEventV2(),
            createPersonCreationEvent(),
            createFingerprintCaptureBiometricsEvent(),
            createFaceCaptureBiometricsEvent(),
        )

        eventSyncRepo.uploadEvents(
            DEFAULT_PROJECT_ID,
            canSyncAllDataToSimprints = false,
            canSyncBiometricDataToSimprints = true,
            canSyncAnalyticsDataToSimprints = false
        ).toList()

        coVerify {
            eventRemoteDataSource.post(
                any(),
                withArg { assertThat(it).hasSize(4) },
                any()
            )
        }
    }

    @Test
    fun `upload should filter analytics events on upload`() = runTest {
        coEvery { eventRepo.getAllClosedSessionIds(any()) } returns listOf(GUID1)
        coEvery { eventRepo.getEventsFromSession(any()) } returns listOf(
            createFingerprintCaptureBiometricsEvent(),
            createFaceCaptureBiometricsEvent(),
            // only following should be uploaded
            createPersonCreationEvent(),
            createEnrolmentEventV2(),
            createAlertScreenEvent(),
        )

        eventSyncRepo.uploadEvents(
            DEFAULT_PROJECT_ID,
            canSyncAllDataToSimprints = false,
            canSyncBiometricDataToSimprints = false,
            canSyncAnalyticsDataToSimprints = true
        ).toList()

        coVerify {
            eventRemoteDataSource.post(
                any(),
                withArg { assertThat(it).hasSize(3) },
                any()
            )
        }
    }

    @Test
    fun `should not upload sessions for not signed project`() = runTest {
        shouldThrow<TryToUploadEventsForNotSignedProject> {
            eventSyncRepo.uploadEvents(
                randomUUID(),
                canSyncAllDataToSimprints = false,
                canSyncBiometricDataToSimprints = false,
                canSyncAnalyticsDataToSimprints = false
            ).toList()
        }
    }

    @Test
    fun `when upload succeeds it should delete events`() = runTest {
        coEvery { eventRepo.getAllClosedSessionIds(any()) } returns listOf(GUID1, GUID2)
        coEvery { eventRepo.getEventsFromSession(GUID1) } returns listOf(createSessionCaptureEvent(GUID1))
        coEvery { eventRepo.getEventsFromSession(GUID2) } returns listOf(createSessionCaptureEvent(GUID2))

        eventSyncRepo.uploadEvents(
            DEFAULT_PROJECT_ID,
            canSyncAllDataToSimprints = true,
            canSyncBiometricDataToSimprints = false,
            canSyncAnalyticsDataToSimprints = false
        ).toList()

        coVerify {
            eventRepo.delete(eq(listOf(GUID1)))
            eventRepo.delete(eq(listOf(GUID2)))
        }
    }

    @Test
    fun `upload in progress should emit progress`() = runTest {
        coEvery { eventRepo.getAllClosedSessionIds(any()) } returns listOf(GUID1, GUID2)
        coEvery { eventRepo.getEventsFromSession(GUID1) } returns listOf(
            createSessionCaptureEvent(GUID1),
        )
        coEvery { eventRepo.getEventsFromSession(GUID2) } returns listOf(
            createEnrolmentEventV2(),
            createAlertScreenEvent(),
        )

        val progress = eventSyncRepo.uploadEvents(
            DEFAULT_PROJECT_ID,
            canSyncAllDataToSimprints = false,
            canSyncBiometricDataToSimprints = false,
            canSyncAnalyticsDataToSimprints = false
        ).toList()

        assertThat(progress[0]).isEqualTo(1)
        assertThat(progress[1]).isEqualTo(2)
    }

    @Test
    fun `when upload fails due to generic error should not delete events`() = runTest {
        coEvery { eventRepo.getAllClosedSessionIds(any()) } returns listOf(GUID1)
        coEvery { eventRepo.getEventsFromSession(GUID1) } returns listOf(createSessionCaptureEvent(GUID1))

        coEvery { eventRemoteDataSource.post(any(), any()) } throws Throwable("")

        eventSyncRepo.uploadEvents(
            DEFAULT_PROJECT_ID,
            canSyncAllDataToSimprints = false,
            canSyncBiometricDataToSimprints = false,
            canSyncAnalyticsDataToSimprints = false
        ).toList()

        coVerify(exactly = 0) { eventRepo.delete(any()) }
    }

    @Test
    fun `when upload fails due to network issue should not delete events`() = runTest {
        coEvery { eventRepo.getAllClosedSessionIds(any()) } returns listOf(GUID1)
        coEvery { eventRepo.getEventsFromSession(GUID1) } returns listOf(createSessionCaptureEvent(GUID1))

        coEvery { eventRemoteDataSource.post(any(), any()) } throws NetworkConnectionException(
            cause = Exception()
        )

        eventSyncRepo.uploadEvents(
            DEFAULT_PROJECT_ID,
            canSyncAllDataToSimprints = false,
            canSyncBiometricDataToSimprints = false,
            canSyncAnalyticsDataToSimprints = false
        ).toList()

        coVerify(exactly = 0) { eventRepo.delete(any()) }
    }

    @Test
    fun `upload should not dump events when fetch fails`() = runTest {
        coEvery { eventRepo.getAllClosedSessionIds(any()) } returns listOf(GUID1)
        coEvery { eventRepo.getEventsFromSession(GUID1) } throws IllegalStateException()

        eventSyncRepo.uploadEvents(
            DEFAULT_PROJECT_ID,
            canSyncAllDataToSimprints = true,
            canSyncBiometricDataToSimprints = false,
            canSyncAnalyticsDataToSimprints = false
        ).toList()

        coVerify(exactly = 0) {
            eventRemoteDataSource.post(any(), any())
            eventRemoteDataSource.dumpInvalidEvents(any(), any())
            eventRepo.deleteSessionEvents(GUID1)
        }
    }

    @Test
    fun `upload should dump invalid events, emit the progress and delete the events`() = runTest {
        coEvery { eventRepo.getAllClosedSessionIds(any()) } returns listOf(GUID1)
        coEvery { eventRepo.getEventsFromSession(GUID1) } throws JsonParseException(mockk(relaxed = true), "")
        coEvery { eventRepo.getEventsJsonFromSession(GUID1) } returns listOf("{}")

        val progress = eventSyncRepo.uploadEvents(
            DEFAULT_PROJECT_ID,
            canSyncAllDataToSimprints = true,
            canSyncBiometricDataToSimprints = false,
            canSyncAnalyticsDataToSimprints = false
        ).toList()

        coVerify(exactly = 0) { eventRemoteDataSource.post(any(), any()) }

        coVerify {
            eventRepo.getEventsJsonFromSession(any())
            eventRemoteDataSource.dumpInvalidEvents(any(), any())
            eventRepo.deleteSessionEvents(GUID1)
        }
        assertThat(progress[0]).isEqualTo(1)
    }

    @Test
    fun `fail dump of invalid events should not delete the events`() = runTest {
        coEvery { eventRepo.getAllClosedSessionIds(any()) } returns listOf(GUID1)
        coEvery { eventRepo.getEventsFromSession(GUID1) } throws JsonParseException(mockk(relaxed = true), "")
        coEvery { eventRepo.getEventsJsonFromSession(GUID1) } returns listOf("{}")
        coEvery { eventRemoteDataSource.dumpInvalidEvents(any(), any()) } throws HttpException(
            Response.error<String>(503, "".toResponseBody(null))
        )

        eventSyncRepo.uploadEvents(
            DEFAULT_PROJECT_ID,
            canSyncAllDataToSimprints = true,
            canSyncBiometricDataToSimprints = false,
            canSyncAnalyticsDataToSimprints = false
        ).toList()

        coVerify(exactly = 0) {
            eventRemoteDataSource.post(any(), any())
            eventRepo.deleteSessionEvents(GUID1)
        }

        coVerify {
            eventRepo.getEventsJsonFromSession(any())
            eventRemoteDataSource.dumpInvalidEvents(any(), any())
        }
    }

    @Test
    fun `download count correctly passes query arguments`() = runTest {
        coEvery { eventRemoteDataSource.count(any()) } returns emptyList()

        eventSyncRepo.countEventsToDownload(
            RemoteEventQuery(
                DEFAULT_PROJECT_ID,
                modes = listOf(Modes.FACE, Modes.FINGERPRINT),
            )
        )

        coVerify {
            eventRemoteDataSource.count(withArg { query ->
                assertThat(query.projectId).isEqualTo(DEFAULT_PROJECT_ID)
                assertThat(query.modes).containsExactly(ApiModes.FACE, ApiModes.FINGERPRINT)
            })
        }
    }

    @Test
    fun `download correctly passes query arguments`() = runTest {
        coEvery {
            eventRemoteDataSource.getEvents(
                any(),
                any()
            )
        } returns produce { this@produce.close() }

        eventSyncRepo.downloadEvents(
            this@runTest,
            RemoteEventQuery(
                DEFAULT_PROJECT_ID,
                modes = listOf(Modes.FACE),
            )
        )

        coVerify {
            eventRemoteDataSource.getEvents(withArg { query ->
                assertThat(query.projectId).isEqualTo(DEFAULT_PROJECT_ID)
                assertThat(query.modes).containsExactly(ApiModes.FACE)
            }, any())
        }
    }

    companion object {
        private const val NOW = 1000L
    }
}
