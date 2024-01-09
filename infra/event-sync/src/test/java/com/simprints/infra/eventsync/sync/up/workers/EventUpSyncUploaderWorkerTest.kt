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
import com.simprints.infra.eventsync.status.up.domain.EventUpSyncOperation
import com.simprints.infra.eventsync.status.up.domain.EventUpSyncScope
import com.simprints.infra.eventsync.sync.common.EventSyncCache
import com.simprints.infra.eventsync.sync.common.OUTPUT_ESTIMATED_MAINTENANCE_TIME
import com.simprints.infra.eventsync.sync.common.OUTPUT_FAILED_BECAUSE_BACKEND_MAINTENANCE
import com.simprints.infra.eventsync.sync.common.OUTPUT_FAILED_BECAUSE_CLOUD_INTEGRATION
import com.simprints.infra.eventsync.sync.up.tasks.EventUpSyncTask
import com.simprints.infra.eventsync.sync.up.EventUpSyncProgress
import com.simprints.infra.eventsync.sync.up.workers.EventUpSyncUploaderWorker.Companion.INPUT_UP_SYNC
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.logging.Simber
import com.simprints.infra.network.exceptions.BackendMaintenanceException
import com.simprints.infra.network.exceptions.SyncCloudIntegrationException
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EventUpSyncUploaderWorkerTest {

    companion object {
        private const val PROJECT_ID = "projectId"
    }

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private val projectScope = JsonHelper.toJson(EventUpSyncScope.ProjectScope(PROJECT_ID))
    private val authStore = mockk<AuthStore> {
        every { signedInProjectId } returns PROJECT_ID
    }
    private val upSyncTask = mockk<EventUpSyncTask>()

    @Before
    fun setUp() {
        mockkObject(Simber)
        every { Simber.i(ofType<String>()) } answers {}
    }

    @After
    fun cleanUp() {
        unmockkObject(Simber)
    }

    @Test
    fun worker_shouldExecuteTheTask() = runTest {
        val eventUpSyncUploaderWorker = init(projectScope)

        coEvery {
            upSyncTask.upSync(any())
        } returns flowOf(
            EventUpSyncProgress(
                EventUpSyncOperation(
                    projectId = "",
                    lastState = EventUpSyncOperation.UpSyncState.COMPLETE,
                    lastSyncTime = null
                ), 12
            )
        )

        val result = eventUpSyncUploaderWorker.doWork()

        assertThat(result).isEqualTo(
            ListenableWorker.Result.success(
                workDataOf(
                    EventUpSyncUploaderWorker.OUTPUT_UP_SYNC to 12
                )
            )
        )
    }

    @Test
    fun worker_shouldSetFailCorrectlyIfBackendError() = runTest {
        val eventUpSyncUploaderWorker = init(projectScope)

        coEvery {
            upSyncTask.upSync(any())
        } throws BackendMaintenanceException(estimatedOutage = null)

        val result = eventUpSyncUploaderWorker.doWork()

        assertThat(result).isEqualTo(
            ListenableWorker.Result.failure(
                workDataOf(
                    OUTPUT_FAILED_BECAUSE_BACKEND_MAINTENANCE to true,
                    OUTPUT_ESTIMATED_MAINTENANCE_TIME to null
                )
            )
        )
    }

    @Test
    fun worker_shouldSetFailCorrectlyIfTimedBackendError() = runTest {
        val eventUpSyncUploaderWorker = init(projectScope)

        coEvery {
            upSyncTask.upSync(any())
        } throws BackendMaintenanceException(estimatedOutage = 600)

        val result = eventUpSyncUploaderWorker.doWork()

        assertThat(result).isEqualTo(
            ListenableWorker.Result.failure(
                workDataOf(
                    OUTPUT_FAILED_BECAUSE_BACKEND_MAINTENANCE to true,
                    OUTPUT_ESTIMATED_MAINTENANCE_TIME to 600L
                )
            )
        )
    }


    @Test
    fun worker_shouldSetFailCorrectlyIfCloudIntegrationError() = runTest {
        val eventUpSyncUploaderWorker = init(projectScope)

        coEvery {
            upSyncTask.upSync(any())
        } throws SyncCloudIntegrationException("Cloud integration", Throwable())

        val result = eventUpSyncUploaderWorker.doWork()

        assertThat(result).isEqualTo(
            ListenableWorker.Result.failure(
                workDataOf(
                    OUTPUT_FAILED_BECAUSE_CLOUD_INTEGRATION to true
                )
            )
        )
    }


    @Test
    fun worker_shouldRetryIfNotBackendMaintenanceOrSyncIssue() = runTest {
        val eventUpSyncUploaderWorker = init(projectScope)

        coEvery {
            upSyncTask.upSync(any())
        } throws Throwable()

        val result = eventUpSyncUploaderWorker.doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.retry())
    }


    @Test
    fun eventUpSyncScope_canDeserializeOldFormat() {
        val jsonInput = """
        {
            "type": "EventUpSyncScope${'$'}ProjectScope",
            "projectId": "pcmqBbcaB4xWvfRHRELG",
            "operation": {
                "queryEvent": {
                    "projectId": "pcmqBbcaB4xWvfRHRELG"
                },
                "lastState": "FAILED",
                "lastSyncTime": 1620103325620
            }
        }    
        """.trimIndent()

        val expectedScope = EventUpSyncScope.ProjectScope("pcmqBbcaB4xWvfRHRELG")
        expectedScope.operation.lastState = EventUpSyncOperation.UpSyncState.FAILED
        expectedScope.operation.lastSyncTime = 1620103325620

        val scope = EventUpSyncUploaderWorker.parseUpSyncInput(jsonInput)
        assertThat(scope).isEqualTo(expectedScope)
    }

    @Test
    fun eventUpSyncScope_canDeserializeNewFormat() {
        val jsonInput = """
        {
            "type": "EventUpSyncScope${'$'}ProjectScope",
            "projectId": "pcmqBbcaB4xWvfRHRELG",
            "operation": {
                "projectId": "pcmqBbcaB4xWvfRHRELG",
                "lastState": "FAILED",
                "lastSyncTime": 1620103325620
            }
        }
        """.trimIndent()

        val expectedScope = EventUpSyncScope.ProjectScope("pcmqBbcaB4xWvfRHRELG")
        expectedScope.operation.lastState = EventUpSyncOperation.UpSyncState.FAILED
        expectedScope.operation.lastSyncTime = 1620103325620

        val scope = EventUpSyncUploaderWorker.parseUpSyncInput(jsonInput)
        assertThat(scope).isEqualTo(expectedScope)
    }

    @Test
    fun `should create a new scope if the current one throws JsonParseException`() = runTest {
        val jsonInput = """
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
        val eventUpSyncUploaderWorker = init(jsonInput)

        eventUpSyncUploaderWorker.doWork()

        val expectedScope = EventUpSyncScope.ProjectScope(PROJECT_ID)

        coVerify(exactly = 1) { upSyncTask.upSync(expectedScope.operation) }
    }

    @Test
    fun `should create a new scope if the current one throws JsonMappingException`() = runTest {
        val jsonInput = """
        {
            "type": "EventUpSyncScope${'$'}ProjectScope"
        }
        """.trimIndent()
        val eventUpSyncUploaderWorker = init(jsonInput)

        eventUpSyncUploaderWorker.doWork()

        val expectedScope = EventUpSyncScope.ProjectScope(PROJECT_ID)

        coVerify(exactly = 1) { upSyncTask.upSync(expectedScope.operation) }
    }

    @Test
    fun `should retry when input is null`() = runTest {
        val eventUpSyncUploaderWorker = init(null)

        val result = eventUpSyncUploaderWorker.doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.retry())
    }

    // We are using the TestListenableWorkerBuilder and not the constructor directly to have a test worker
    // that will finish when calling setProgress
    private fun init(scope: String?): EventUpSyncUploaderWorker =
        TestListenableWorkerBuilder<EventUpSyncUploaderWorker>(
            mockk(),
            workDataOf(INPUT_UP_SYNC to scope)
        ).setWorkerFactory(
            TestWorkerFactory(
                upSyncTask,
                mockk(relaxed = true),
                authStore,
                testCoroutineRule.testCoroutineDispatcher
            )
        ).build()
}

private class TestWorkerFactory(
    private val upSyncTask: EventUpSyncTask,
    private val eventSyncCache: EventSyncCache,
    private val authStore: AuthStore,
    private val dispatcher: CoroutineDispatcher,
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker = EventUpSyncUploaderWorker(
        appContext,
        workerParameters,
        upSyncTask,
        eventSyncCache,
        authStore,
        JsonHelper,
        dispatcher
    )
}
