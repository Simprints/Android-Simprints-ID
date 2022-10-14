package com.simprints.id.services.sync.events.up.workers

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
import com.simprints.eventsystem.events_sync.up.domain.EventUpSyncOperation
import com.simprints.eventsystem.events_sync.up.domain.EventUpSyncScope
import com.simprints.id.services.sync.events.master.internal.EventSyncCache
import com.simprints.id.services.sync.events.master.internal.OUTPUT_ESTIMATED_MAINTENANCE_TIME
import com.simprints.id.services.sync.events.master.internal.OUTPUT_FAILED_BECAUSE_BACKEND_MAINTENANCE
import com.simprints.id.services.sync.events.master.internal.OUTPUT_FAILED_BECAUSE_CLOUD_INTEGRATION
import com.simprints.id.services.sync.events.up.EventUpSyncHelper
import com.simprints.id.services.sync.events.up.EventUpSyncProgress
import com.simprints.id.services.sync.events.up.workers.EventUpSyncUploaderWorker.Companion.INPUT_UP_SYNC
import com.simprints.infra.network.exceptions.BackendMaintenanceException
import com.simprints.infra.network.exceptions.SyncCloudIntegrationException
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EventUpSyncUploaderWorkerTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private val scope = EventUpSyncScope.ProjectScope("pcmqBbcaB4xWvfRHRELG")
    private val upSyncHelper = mockk<EventUpSyncHelper>()
    // We are using the TestListenableWorkerBuilder and not the constructor directly to have a test worker
    // that will finish when calling setProgress
    private val eventUpSyncUploaderWorker = TestListenableWorkerBuilder<EventUpSyncUploaderWorker>(
        mockk(),
        workDataOf(INPUT_UP_SYNC to JsonHelper.toJson(scope))
    ).setWorkerFactory(
        TestWorkerFactory(
            upSyncHelper,
            mockk(relaxed = true),
            testCoroutineRule.testCoroutineDispatcher
        )
    ).build()

    @Test
    fun worker_shouldExecuteTheTask() = runTest {
        coEvery {
            upSyncHelper.upSync(any(), any())
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
        coEvery {
            upSyncHelper.upSync(any(), any())
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
        coEvery {
            upSyncHelper.upSync(any(), any())
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
        coEvery {
            upSyncHelper.upSync(any(), any())
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
        coEvery {
            upSyncHelper.upSync(any(), any())
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
}

class TestWorkerFactory(
    private val upSyncHelper: EventUpSyncHelper,
    private val eventSyncCache: EventSyncCache,
    private val dispatcher: CoroutineDispatcher,
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker {
        return EventUpSyncUploaderWorker(
            appContext,
            workerParameters,
            upSyncHelper,
            eventSyncCache,
            dispatcher
        )
    }
}
