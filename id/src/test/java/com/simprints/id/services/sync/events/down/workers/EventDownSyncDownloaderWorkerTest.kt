package com.simprints.id.services.sync.events.down.workers

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Data
import androidx.work.WorkInfo
import androidx.work.WorkInfo.State.RUNNING
import androidx.work.WorkInfo.State.SUCCEEDED
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.workDataOf
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.json.JsonHelper
import com.simprints.eventsystem.event.remote.exceptions.TooManyRequestsException
import com.simprints.eventsystem.sampledata.SampleDefaults.projectDownSyncScope
import com.simprints.id.services.sync.events.common.TAG_DOWN_SYNC_NEW_MODULES
import com.simprints.id.services.sync.events.down.workers.EventDownSyncDownloaderWorker.Companion.INPUT_DOWN_SYNC_OPS
import com.simprints.id.services.sync.events.down.workers.EventDownSyncDownloaderWorker.Companion.OUTPUT_DOWN_SYNC
import com.simprints.id.services.sync.events.down.workers.EventDownSyncDownloaderWorker.Companion.PROGRESS_DOWN_SYNC
import com.simprints.id.services.sync.events.master.internal.*
import com.simprints.id.testtools.TestApplication
import com.simprints.infra.network.exceptions.BackendMaintenanceException
import com.simprints.infra.network.exceptions.SyncCloudIntegrationException
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.coroutines.TestDispatcherProvider
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.util.*

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class EventDownSyncDownloaderWorkerTest {

    private val app = ApplicationProvider.getApplicationContext() as TestApplication
    private lateinit var eventDownSyncDownloaderWorker: EventDownSyncDownloaderWorker

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()
    private val testDispatcherProvider = TestDispatcherProvider(testCoroutineRule)

    @Before
    fun setUp() {
        app.component = mockk(relaxed = true)
        val correctInputData = JsonHelper.toJson(projectDownSyncScope.operations.first())
        eventDownSyncDownloaderWorker =
            createWorker(workDataOf(INPUT_DOWN_SYNC_OPS to correctInputData))
    }

    @Test
    fun worker_shouldParseInputDataCorrectly() = runBlocking<Unit> {
        with(eventDownSyncDownloaderWorker) {
            doWork()
        }
    }

    @Test
    fun worker_shouldExecuteTheTask() {
        runBlocking {
            with(eventDownSyncDownloaderWorker) {
                coEvery {
                    eventDownSyncDownloaderTask.execute(
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any()
                    )
                } returns 0

                doWork()

                verify { resultSetter.success(workDataOf(OUTPUT_DOWN_SYNC to 0)) }
            }
        }
    }

    @Test
    fun worker_shouldNotRefreshOperationForNewModulesSync() {
        runBlocking {
            with(eventDownSyncDownloaderWorker) {
                tags.add(TAG_DOWN_SYNC_NEW_MODULES)
                doWork()

                coVerify(exactly = 0) { eventDownSyncScopeRepository.refreshState(any()) }
            }
        }
    }

    @Test
    fun worker_shouldClearNewlyAddedModulesAfterSuccessfulSync() {
        runBlocking {
            with(eventDownSyncDownloaderWorker) {
                tags.add(TAG_DOWN_SYNC_NEW_MODULES)
                doWork()

                coVerify(exactly = 1) { preferencesManager.newlyAddedModules = setOf() }
            }
        }
    }

    @Test
    fun worker_failForCloudIntegration_shouldFail() = runBlocking {
        with(eventDownSyncDownloaderWorker) {
            coEvery {
                eventDownSyncDownloaderTask.execute(
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any()
                )
            } throws SyncCloudIntegrationException("Cloud integration", Throwable())

            doWork()

            verify { resultSetter.failure(workDataOf(OUTPUT_FAILED_BECAUSE_CLOUD_INTEGRATION to true)) }
        }
    }

    @Test
    fun worker_failForBackendMaintenanceError_shouldFail() = runBlocking {
        with(eventDownSyncDownloaderWorker) {
            coEvery {
                eventDownSyncDownloaderTask.execute(
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any()
                )
            } throws BackendMaintenanceException(estimatedOutage = null)

            doWork()

            verify {
                resultSetter.failure(
                    workDataOf(
                        OUTPUT_FAILED_BECAUSE_BACKEND_MAINTENANCE to true,
                        OUTPUT_ESTIMATED_MAINTENANCE_TIME to null
                    )
                )
            }
        }
    }

    @Test
    fun worker_failForTimedBackendMaintenanceError_shouldFail() = runBlocking {
        with(eventDownSyncDownloaderWorker) {
            coEvery {
                eventDownSyncDownloaderTask.execute(
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any()
                )
            } throws BackendMaintenanceException(estimatedOutage = 600)

            doWork()

            verify {
                resultSetter.failure(
                    workDataOf(
                        OUTPUT_FAILED_BECAUSE_BACKEND_MAINTENANCE to true,
                        OUTPUT_ESTIMATED_MAINTENANCE_TIME to 600L
                    )
                )
            }
        }
    }

    @Test
    fun worker_failForTooManyRequestsError_shouldFail() = runBlocking {
        with(eventDownSyncDownloaderWorker) {
            coEvery {
                eventDownSyncDownloaderTask.execute(
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any()
                )
            } throws TooManyRequestsException()

            doWork()

            verify {
                resultSetter.failure(
                    workDataOf(OUTPUT_FAILED_BECAUSE_TOO_MANY_REQUESTS to true)
                )
            }
        }
    }

    @Test
    fun worker_failForNetworkIssue_shouldRetry() = runBlocking {
        with(eventDownSyncDownloaderWorker) {
            coEvery {
                eventDownSyncDownloaderTask.execute(
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any()
                )
            } throws Throwable("Network Exception")

            doWork()

            verify { resultSetter.retry() }
        }
    }

    @Test
    fun worker_progressCountInProgressData_shouldExtractTheProgressCountCorrectly() = runBlocking {
        val progress = 2
        val syncCacheMock = mockk<EventSyncCache>()
        every { syncCacheMock.readProgress(any()) } returns 1

        val workInfo = WorkInfo(
            UUID.randomUUID(),
            RUNNING,
            workDataOf(),
            listOf(),
            workDataOf(PROGRESS_DOWN_SYNC to progress),
            2
        )
        assertThat(workInfo.extractDownSyncProgress(syncCacheMock)).isEqualTo(progress)
    }

    @Test
    fun worker_SyncDown_shouldExtractTheFinalProgressCountCorrectly() = runBlocking {
        val progress = 2
        val syncCacheMock = mockk<EventSyncCache>()
        every { syncCacheMock.readProgress(any()) } returns 1

        val workInfo = WorkInfo(
            UUID.randomUUID(),
            SUCCEEDED,
            workDataOf(OUTPUT_DOWN_SYNC to progress),
            listOf(),
            workDataOf(),
            2
        )
        assertThat(workInfo.extractDownSyncProgress(syncCacheMock)).isEqualTo(progress)
    }

    @Test
    fun workerResumed_progressCountInCache_shouldExtractTheProgressCountCorrectly() = runBlocking {
        val progress = 2
        val syncCacheMock = mockk<EventSyncCache>()
        every { syncCacheMock.readProgress(any()) } returns progress

        val workInfo = WorkInfo(
            UUID.randomUUID(),
            RUNNING,
            workDataOf(),
            listOf(),
            workDataOf(),
            2
        )
        assertThat(workInfo.extractDownSyncProgress(syncCacheMock)).isEqualTo(progress)
    }

    private fun createWorker(inputData: Data? = null) =
        (inputData?.let {
            TestListenableWorkerBuilder<EventDownSyncDownloaderWorker>(app, inputData = it).build()
        } ?: TestListenableWorkerBuilder<EventDownSyncDownloaderWorker>(app).build()).apply {
            resultSetter = mockk(relaxed = true)
            eventDownSyncScopeRepository = mockk(relaxed = true)
            coEvery { eventDownSyncScopeRepository.refreshState(any()) } answers { this.args.first() as com.simprints.eventsystem.events_sync.down.domain.EventDownSyncOperation }
            syncCache = mockk(relaxed = true)
            jsonHelper = JsonHelper
            eventDownSyncDownloaderTask = mockk(relaxed = true)
            downSyncHelper = mockk(relaxed = true)
            dispatcher = testDispatcherProvider
            preferencesManager = mockk()
        }
}

