package com.simprints.infra.sync

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.eventsync.DeleteSyncInfoUseCase
import com.simprints.infra.eventsync.EventSyncWorkerTagRepository
import com.simprints.infra.eventsync.status.models.DownSyncState
import com.simprints.infra.eventsync.status.models.UpSyncState
import com.simprints.infra.eventsync.sync.down.DownSyncStateProcessor
import com.simprints.infra.eventsync.sync.up.UpSyncStateProcessor
import com.simprints.infra.sync.firmware.ShouldScheduleFirmwareUpdateUseCase
import com.simprints.infra.sync.usecase.CleanupDeprecatedWorkersUseCase
import com.simprints.infra.sync.usecase.internal.ObserveImageSyncStatusUseCase
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SyncOrchestratorObserveSyncStateTest {
    @MockK
    private lateinit var workManager: androidx.work.WorkManager

    @MockK
    private lateinit var authStore: AuthStore

    @MockK
    private lateinit var configRepository: ConfigRepository

    @MockK
    private lateinit var deleteSyncInfo: DeleteSyncInfoUseCase

    @MockK
    private lateinit var eventSyncWorkerTagRepository: EventSyncWorkerTagRepository

    @MockK
    private lateinit var upSyncStateProcessor: UpSyncStateProcessor

    @MockK
    private lateinit var downSyncStateProcessor: DownSyncStateProcessor

    @MockK
    private lateinit var observeImageSyncStatus: ObserveImageSyncStatusUseCase

    @MockK
    private lateinit var shouldScheduleFirmwareUpdate: ShouldScheduleFirmwareUpdateUseCase

    @MockK
    private lateinit var cleanupDeprecatedWorkers: CleanupDeprecatedWorkersUseCase

    @MockK
    private lateinit var imageSyncTimestampProvider: ImageSyncTimestampProvider

    private val upSyncStatusFlow = MutableSharedFlow<UpSyncState>()
    private val downSyncStatusFlow = MutableSharedFlow<DownSyncState>()
    private val imageSyncStatusFlow = MutableSharedFlow<ImageSyncStatus>()

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        every { workManager.getWorkInfosFlow(any()) } returns flowOf(emptyList())
        every { upSyncStateProcessor.getLastUpSyncState() } returns upSyncStatusFlow
        every { downSyncStateProcessor.getLastDownSyncState() } returns downSyncStatusFlow
        every { observeImageSyncStatus.invoke() } returns imageSyncStatusFlow
    }

    @Test
    fun `returns default SyncStatus before upstream flows emit`() = runTest {
        val expected = SyncStatus(
            upSyncState = UpSyncState(
                syncId = "",
                workersInfo = emptyList(),
                progress = null,
                total = null,
                lastSyncTime = null,
            ),
            downSyncState = DownSyncState(
                syncId = "",
                workersInfo = emptyList(),
                progress = null,
                total = null,
                lastSyncTime = null,
            ),
            imageSyncStatus = ImageSyncStatus(
                isSyncing = false,
                progress = null,
                lastUpdateTimeMillis = -1L,
            ),
        )
        val orchestrator = createOrchestrator(
            appScope = backgroundScope,
            dispatcher = StandardTestDispatcher(testScheduler),
        )

        val resultFlow = orchestrator.observeSyncState()

        assertThat(resultFlow.value).isEqualTo(expected)
    }

    @Test
    fun `combines latest up, down and image states into SyncStatus`() = runTest {
        val upSync = UpSyncState(
            syncId = "up-1",
            workersInfo = emptyList(),
            progress = 1,
            total = 10,
            lastSyncTime = null,
        )
        val downSync = DownSyncState(
            syncId = "down-1",
            workersInfo = emptyList(),
            progress = 3,
            total = 10,
            lastSyncTime = null,
        )
        val image = ImageSyncStatus(
            isSyncing = true,
            progress = 2 to 5,
            lastUpdateTimeMillis = 123L,
        )
        val orchestrator = createOrchestrator(
            appScope = backgroundScope,
            dispatcher = StandardTestDispatcher(testScheduler),
        )

        val resultFlow = orchestrator.observeSyncState()

        runCurrent() // ensure upstream flows are collected before emitting
        upSyncStatusFlow.emit(upSync)
        downSyncStatusFlow.emit(downSync)
        imageSyncStatusFlow.emit(image)
        runCurrent()

        assertThat(resultFlow.value).isEqualTo(SyncStatus(upSync, downSync, image))
    }

    @Test
    fun `returns the same shared StateFlow across invocations`() = runTest {
        val orchestrator = createOrchestrator(
            appScope = backgroundScope,
            dispatcher = StandardTestDispatcher(testScheduler),
        )

        val flow1 = orchestrator.observeSyncState()
        val flow2 = orchestrator.observeSyncState()

        assertThat(flow1).isSameInstanceAs(flow2)
        verify(exactly = 1) { upSyncStateProcessor.getLastUpSyncState() }
        verify(exactly = 1) { downSyncStateProcessor.getLastDownSyncState() }
        verify(exactly = 1) { observeImageSyncStatus.invoke() }
    }

    private fun createOrchestrator(
        appScope: CoroutineScope,
        dispatcher: CoroutineDispatcher,
    ) = SyncOrchestratorImpl(
        workManager = workManager,
        authStore = authStore,
        configRepository = configRepository,
        deleteSyncInfo = deleteSyncInfo,
        eventSyncWorkerTagRepository = eventSyncWorkerTagRepository,
        upSyncStateProcessor = upSyncStateProcessor,
        downSyncStateProcessor = downSyncStateProcessor,
        observeImageSyncStatus = observeImageSyncStatus,
        shouldScheduleFirmwareUpdate = shouldScheduleFirmwareUpdate,
        cleanupDeprecatedWorkers = cleanupDeprecatedWorkers,
        imageSyncTimestampProvider = imageSyncTimestampProvider,
        appScope = appScope,
        ioDispatcher = dispatcher,
    )
}
