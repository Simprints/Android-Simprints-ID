package com.simprints.infra.eventsync.sync.up.tasks

import com.fasterxml.jackson.core.JsonParseException
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.utils.randomUUID
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.ProjectConfiguration
import com.simprints.infra.config.domain.models.SynchronizationConfiguration
import com.simprints.infra.config.domain.models.UpSynchronizationConfiguration
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.sampledata.*
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.infra.events.sampledata.SampleDefaults.GUID1
import com.simprints.infra.events.sampledata.SampleDefaults.GUID2
import com.simprints.infra.eventsync.SampleSyncScopes
import com.simprints.infra.eventsync.event.remote.EventRemoteDataSource
import com.simprints.infra.eventsync.exceptions.TryToUploadEventsForNotSignedProject
import com.simprints.infra.eventsync.status.up.EventUpSyncScopeRepository
import com.simprints.infra.eventsync.status.up.domain.EventUpSyncOperation
import com.simprints.infra.eventsync.status.up.domain.EventUpSyncOperation.UpSyncState
import com.simprints.infra.login.LoginManager
import com.simprints.infra.network.exceptions.NetworkConnectionException
import io.kotest.assertions.throwables.shouldThrow
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

internal class EventUpSyncTaskTest {

    private val operation = SampleSyncScopes.projectUpSyncScope.operation

    private lateinit var eventUpSyncTask: EventUpSyncTask

    @MockK
    private lateinit var eventUpSyncScopeRepository: EventUpSyncScopeRepository

    @MockK
    lateinit var loginManager: LoginManager

    @MockK
    lateinit var eventRepo: EventRepository

    @MockK
    lateinit var eventRemoteDataSource: EventRemoteDataSource

    @MockK
    private lateinit var timeHelper: TimeHelper

    @MockK
    private lateinit var synchronizationConfiguration: SynchronizationConfiguration

    @MockK
    private lateinit var projectConfiguration: ProjectConfiguration

    @MockK
    private lateinit var configManager: ConfigManager

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        every { timeHelper.now() } returns NOW
        every { loginManager.getSignedInProjectIdOrEmpty() } returns DEFAULT_PROJECT_ID

        every { projectConfiguration.synchronization } returns synchronizationConfiguration
        coEvery { configManager.getProjectConfiguration() } returns projectConfiguration

        eventUpSyncTask = EventUpSyncTask(
            loginManager,
            eventUpSyncScopeRepository,
            eventRepo,
            eventRemoteDataSource,
            timeHelper,
            configManager
        )
    }

    @Test
    fun `upload fetches events for all provided closed sessions`() = runTest {
        setUpSyncKind(UpSynchronizationConfiguration.UpSynchronizationKind.NONE)

        coEvery { eventRepo.getAllClosedSessionIds(any()) } returns listOf(GUID1, GUID2)
        coEvery { eventRepo.getEventsFromSession(GUID1) } returns listOf(createSessionCaptureEvent(GUID1))
        coEvery { eventRepo.getEventsFromSession(GUID2) } returns listOf(createSessionCaptureEvent(GUID2))

        eventUpSyncTask.upSync(operation).toList()

        coVerify(exactly = 2) { eventRepo.getEventsFromSession(any()) }
    }


    @Test
    fun `upload should not filter any events on upload`() = runTest {
        setUpSyncKind(UpSynchronizationConfiguration.UpSynchronizationKind.ALL)
        coEvery { eventRepo.getAllClosedSessionIds(any()) } returns listOf(GUID1)
        coEvery { eventRepo.getEventsFromSession(any()) } returns listOf(
            createAuthenticationEvent(),
            createEnrolmentEventV2(),
        )

        eventUpSyncTask.upSync(operation).toList()

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
        setUpSyncKind(UpSynchronizationConfiguration.UpSynchronizationKind.ONLY_BIOMETRICS)

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

        eventUpSyncTask.upSync(operation).toList()

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
        setUpSyncKind(UpSynchronizationConfiguration.UpSynchronizationKind.ONLY_ANALYTICS)

        coEvery { eventRepo.getAllClosedSessionIds(any()) } returns listOf(GUID1)
        coEvery { eventRepo.getEventsFromSession(any()) } returns listOf(
            createFingerprintCaptureBiometricsEvent(),
            createFaceCaptureBiometricsEvent(),
            // only following should be uploaded
            createPersonCreationEvent(),
            createEnrolmentEventV2(),
            createAlertScreenEvent(),
        )

        eventUpSyncTask.upSync(operation).toList()

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
            eventUpSyncTask.upSync(EventUpSyncOperation(randomUUID())).toList()
        }
    }

    @Test
    fun `when upload succeeds it should delete events`() = runTest {
        setUpSyncKind(UpSynchronizationConfiguration.UpSynchronizationKind.ALL)

        coEvery { eventRepo.getAllClosedSessionIds(any()) } returns listOf(GUID1, GUID2)
        coEvery { eventRepo.getEventsFromSession(GUID1) } returns listOf(createSessionCaptureEvent(GUID1))
        coEvery { eventRepo.getEventsFromSession(GUID2) } returns listOf(createSessionCaptureEvent(GUID2))

        eventUpSyncTask.upSync(operation).toList()

        coVerify {
            eventRepo.delete(eq(listOf(GUID1)))
            eventRepo.delete(eq(listOf(GUID2)))
        }
    }

    @Test
    fun `upload in progress should emit progress`() = runTest {
        setUpSyncKind(UpSynchronizationConfiguration.UpSynchronizationKind.NONE)

        coEvery { eventRepo.getAllClosedSessionIds(any()) } returns listOf(GUID1, GUID2)
        coEvery { eventRepo.getEventsFromSession(GUID1) } returns listOf(
            createSessionCaptureEvent(GUID1),
        )
        coEvery { eventRepo.getEventsFromSession(GUID2) } returns listOf(
            createEnrolmentEventV2(),
            createAlertScreenEvent(),
        )

        val progress = eventUpSyncTask.upSync(operation).toList()

        assertThat(progress[0].progress).isEqualTo(1)
        assertThat(progress[0].operation.lastState).isEqualTo(UpSyncState.RUNNING)
        assertThat(progress[1].progress).isEqualTo(2)
        assertThat(progress[1].operation.lastState).isEqualTo(UpSyncState.RUNNING)
        assertThat(progress[2].operation.lastState).isEqualTo(UpSyncState.COMPLETE)
    }

    @Test
    fun `when upload fails due to generic error should not delete events`() = runTest {
        setUpSyncKind(UpSynchronizationConfiguration.UpSynchronizationKind.NONE)

        coEvery { eventRepo.getAllClosedSessionIds(any()) } returns listOf(GUID1)
        coEvery { eventRepo.getEventsFromSession(GUID1) } returns listOf(createSessionCaptureEvent(GUID1))

        coEvery { eventRemoteDataSource.post(any(), any()) } throws Throwable("")

        eventUpSyncTask.upSync(operation).toList()

        coVerify(exactly = 0) { eventRepo.delete(any()) }
    }

    @Test
    fun `when upload fails due to network issue should not delete events`() = runTest {
        setUpSyncKind(UpSynchronizationConfiguration.UpSynchronizationKind.NONE)

        coEvery { eventRepo.getAllClosedSessionIds(any()) } returns listOf(GUID1)
        coEvery { eventRepo.getEventsFromSession(GUID1) } returns listOf(createSessionCaptureEvent(GUID1))

        coEvery { eventRemoteDataSource.post(any(), any()) } throws NetworkConnectionException(
            cause = Exception()
        )

        eventUpSyncTask.upSync(operation).toList()

        coVerify(exactly = 0) { eventRepo.delete(any()) }
    }

    @Test
    fun `upload should not dump events when fetch fails`() = runTest {
        setUpSyncKind(UpSynchronizationConfiguration.UpSynchronizationKind.ALL)

        coEvery { eventRepo.getAllClosedSessionIds(any()) } returns listOf(GUID1)
        coEvery { eventRepo.getEventsFromSession(GUID1) } throws IllegalStateException()

        eventUpSyncTask.upSync(operation).toList()

        coVerify(exactly = 0) {
            eventRemoteDataSource.post(any(), any())
            eventRemoteDataSource.dumpInvalidEvents(any(), any())
            eventRepo.deleteSessionEvents(GUID1)
        }
    }

    @Test
    fun `upload should dump invalid events, emit the progress and delete the events`() = runTest {
        setUpSyncKind(UpSynchronizationConfiguration.UpSynchronizationKind.ALL)

        coEvery { eventRepo.getAllClosedSessionIds(any()) } returns listOf(GUID1)
        coEvery { eventRepo.getEventsFromSession(GUID1) } throws JsonParseException(mockk(relaxed = true), "")
        coEvery { eventRepo.getEventsJsonFromSession(GUID1) } returns listOf("{}")

        val progress = eventUpSyncTask.upSync(operation).toList()

        coVerify(exactly = 0) { eventRemoteDataSource.post(any(), any()) }

        coVerify {
            eventRepo.getEventsJsonFromSession(any())
            eventRemoteDataSource.dumpInvalidEvents(any(), any())
            eventRepo.deleteSessionEvents(GUID1)
        }
        assertThat(progress[0].progress).isEqualTo(1)
    }

    @Test
    fun `fail dump of invalid events should not delete the events`() = runTest {
        setUpSyncKind(UpSynchronizationConfiguration.UpSynchronizationKind.ALL)

        coEvery { eventRepo.getAllClosedSessionIds(any()) } returns listOf(GUID1)
        coEvery { eventRepo.getEventsFromSession(GUID1) } throws JsonParseException(mockk(relaxed = true), "")
        coEvery { eventRepo.getEventsJsonFromSession(GUID1) } returns listOf("{}")
        coEvery { eventRemoteDataSource.dumpInvalidEvents(any(), any()) } throws HttpException(
            Response.error<String>(503, "".toResponseBody(null))
        )

        eventUpSyncTask.upSync(operation).toList()

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
    fun `upSync should emit a failure if upload fails`() = runTest {
        coEvery { eventRepo.getAllClosedSessionIds(any()) } throws IllegalStateException()

        val progress = eventUpSyncTask.upSync(operation).toList()
        assertThat(progress.first().operation.lastState).isEqualTo(UpSyncState.FAILED)
        coVerify(exactly = 1) { eventUpSyncScopeRepository.insertOrUpdate(any()) }
    }

    private fun setUpSyncKind(kind: UpSynchronizationConfiguration.UpSynchronizationKind) {
        every { synchronizationConfiguration.up.simprints.kind } returns kind
    }

    companion object {
        private const val NOW = 1000L
    }
}
