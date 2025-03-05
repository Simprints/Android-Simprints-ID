package com.simprints.infra.eventsync.sync.up.workers

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.workDataOf
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.json.JsonHelper
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.authstore.exceptions.RemoteDbNotSignedInException
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.scope.EventScope
import com.simprints.infra.eventsync.status.up.domain.EventUpSyncOperation
import com.simprints.infra.eventsync.status.up.domain.EventUpSyncScope
import com.simprints.infra.eventsync.sync.common.EventSyncCache
import com.simprints.infra.eventsync.sync.common.OUTPUT_ESTIMATED_MAINTENANCE_TIME
import com.simprints.infra.eventsync.sync.common.OUTPUT_FAILED_BECAUSE_BACKEND_MAINTENANCE
import com.simprints.infra.eventsync.sync.common.OUTPUT_FAILED_BECAUSE_CLOUD_INTEGRATION
import com.simprints.infra.eventsync.sync.common.OUTPUT_FAILED_BECAUSE_RELOGIN_REQUIRED
import com.simprints.infra.eventsync.sync.up.EventUpSyncProgress
import com.simprints.infra.eventsync.sync.up.tasks.EventUpSyncTask
import com.simprints.infra.eventsync.sync.up.workers.EventUpSyncUploaderWorker.Companion.INPUT_EVENT_UP_SYNC_SCOPE_ID
import com.simprints.infra.eventsync.sync.up.workers.EventUpSyncUploaderWorker.Companion.INPUT_UP_SYNC
import com.simprints.infra.network.exceptions.BackendMaintenanceException
import com.simprints.infra.network.exceptions.SyncCloudIntegrationException
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class EventUpSyncUploaderWorkerTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    lateinit var authStore: AuthStore

    @MockK
    lateinit var eventSyncCache: EventSyncCache

    @MockK
    lateinit var upSyncTask: EventUpSyncTask

    @MockK
    lateinit var eventRepository: EventRepository

    @MockK
    lateinit var eventScope: EventScope

    private val projectScope = JsonHelper.toJson(EventUpSyncScope.ProjectScope(PROJECT_ID))

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        coEvery { eventSyncCache.readProgress(any()) } returns 0
        every { authStore.signedInProjectId } returns PROJECT_ID
        coEvery { eventRepository.observeEventCountInClosedScopes() } returns flowOf(12)
    }

    @Test
    fun worker_shouldExecuteTheTask() = runTest {
        coEvery { eventRepository.getEventScope(any()) } returns eventScope
        coEvery { upSyncTask.upSync(any(), any()) } returns flowOf(
            EventUpSyncProgress(
                EventUpSyncOperation(
                    projectId = "",
                    lastState = EventUpSyncOperation.UpSyncState.COMPLETE,
                    lastSyncTime = null,
                ),
                12,
            ),
        )

        val eventUpSyncUploaderWorker = init(projectScope)
        val result = eventUpSyncUploaderWorker.doWork()

        assertThat(result).isEqualTo(
            ListenableWorker.Result.success(
                workDataOf(
                    EventUpSyncUploaderWorker.OUTPUT_UP_SYNC to 12,
                    EventUpSyncUploaderWorker.OUTPUT_UP_MAX_SYNC to 12,
                ),
            ),
        )
    }

    @Test
    fun worker_shouldFailCorrectlyIfNoEventScope() = runTest {
        coEvery { eventRepository.getEventScope(any()) } returns null

        val result = init(projectScope).doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.failure())
    }

    @Test
    fun worker_shouldSetFailCorrectlyIfBackendError() = runTest {
        coEvery { eventRepository.getEventScope(any()) } returns eventScope
        coEvery {
            upSyncTask.upSync(any(), eventScope)
        } throws BackendMaintenanceException(estimatedOutage = null)

        val result = init(projectScope).doWork()

        assertThat(result).isEqualTo(
            ListenableWorker.Result.failure(
                workDataOf(
                    OUTPUT_FAILED_BECAUSE_BACKEND_MAINTENANCE to true,
                    OUTPUT_ESTIMATED_MAINTENANCE_TIME to null,
                ),
            ),
        )
    }

    @Test
    fun worker_shouldSetFailCorrectlyIfTimedBackendError() = runTest {
        coEvery { eventRepository.getEventScope(any()) } returns eventScope
        coEvery {
            upSyncTask.upSync(any(), eventScope)
        } throws BackendMaintenanceException(estimatedOutage = 600)

        val result = init(projectScope).doWork()

        assertThat(result).isEqualTo(
            ListenableWorker.Result.failure(
                workDataOf(
                    OUTPUT_FAILED_BECAUSE_BACKEND_MAINTENANCE to true,
                    OUTPUT_ESTIMATED_MAINTENANCE_TIME to 600L,
                ),
            ),
        )
    }

    @Test
    fun worker_shouldSetFailCorrectlyIfCloudIntegrationError() = runTest {
        coEvery { eventRepository.getEventScope(any()) } returns eventScope
        coEvery {
            upSyncTask.upSync(any(), eventScope)
        } throws SyncCloudIntegrationException("Cloud integration", Throwable())

        val result = init(projectScope).doWork()

        assertThat(result).isEqualTo(
            ListenableWorker.Result.failure(
                workDataOf(
                    OUTPUT_FAILED_BECAUSE_CLOUD_INTEGRATION to true,
                ),
            ),
        )
    }

    @Test
    fun worker_shouldSetFailCorrectlyIfRemoteDbNotSignedInException() = runTest {
        val eventUpSyncUploaderWorker = init(projectScope)

        coEvery {
            upSyncTask.upSync(any(), any())
        } throws RemoteDbNotSignedInException()

        val result = eventUpSyncUploaderWorker.doWork()

        assertThat(result).isEqualTo(
            ListenableWorker.Result.failure(
                workDataOf(
                    OUTPUT_FAILED_BECAUSE_RELOGIN_REQUIRED to true,
                ),
            ),
        )
    }

    @Test
    fun worker_shouldRetryIfNotBackendMaintenanceOrSyncIssue() = runTest {
        coEvery { eventRepository.getEventScope(any()) } returns eventScope
        coEvery {
            upSyncTask.upSync(any(), eventScope)
        } throws Throwable()

        val result = init(projectScope).doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.retry())
    }

    @Test
    fun `should create a new scope if the current one throws JsonParseException`() = runTest {
        coEvery { eventRepository.getEventScope(any()) } returns eventScope

        val jsonInput =
            """
            {
                "type": "EventUpSyncScope${'$'}ProjectScope",
                "projectId': "pcmqBbcaB4xWvfRHRELG",
                "operation": {
                    "projectId": "pcmqBbcaB4xWvfRHRELG",
                    "lastState${0.toChar()}": "FAILED",
                    "lastSyncTime": 1620103325620
                }
            }
            """.trimIndent()

        init(jsonInput).doWork()

        val expectedScope = EventUpSyncScope.ProjectScope(PROJECT_ID)

        coVerify(exactly = 1) { upSyncTask.upSync(expectedScope.operation, eventScope) }
    }

    @Test
    fun `should create a new scope if the current one throws JsonMappingException`() = runTest {
        coEvery { eventRepository.getEventScope(any()) } returns eventScope

        val jsonInput =
            """
            {
                "type": "EventUpSyncScope${'$'}ProjectScope"
            }
            """.trimIndent()
        init(jsonInput).doWork()

        val expectedScope = EventUpSyncScope.ProjectScope(PROJECT_ID)

        coVerify(exactly = 1) { upSyncTask.upSync(expectedScope.operation, eventScope) }
    }

    @Test
    fun `should retry when input is null`() = runTest {
        val eventUpSyncUploaderWorker = init(null)

        val result = eventUpSyncUploaderWorker.doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.retry())
    }

    // We are using the TestListenableWorkerBuilder and not the constructor directly to have a test worker
    // that will finish when calling setProgress
    private fun init(
        scope: String?,
        eventScopeId: String? = "scopeId",
    ): EventUpSyncUploaderWorker = TestListenableWorkerBuilder<EventUpSyncUploaderWorker>(
        mockk(),
        workDataOf(
            INPUT_UP_SYNC to scope,
            INPUT_EVENT_UP_SYNC_SCOPE_ID to eventScopeId,
        ),
    ).setWorkerFactory(
        TestWorkerFactory(
            upSyncTask,
            mockk(relaxed = true),
            authStore,
            eventRepository,
            testCoroutineRule.testCoroutineDispatcher,
        ),
    ).build()

    private class TestWorkerFactory(
        private val upSyncTask: EventUpSyncTask,
        private val eventSyncCache: EventSyncCache,
        private val authStore: AuthStore,
        private val eventRepository: EventRepository,
        private val dispatcher: CoroutineDispatcher,
    ) : WorkerFactory() {
        override fun createWorker(
            appContext: Context,
            workerClassName: String,
            workerParameters: WorkerParameters,
        ): ListenableWorker = EventUpSyncUploaderWorker(
            appContext,
            workerParameters,
            upSyncTask,
            eventSyncCache,
            authStore,
            JsonHelper,
            eventRepository,
            dispatcher,
        )
    }

    companion object {
        private const val PROJECT_ID = "projectId"
    }
}
