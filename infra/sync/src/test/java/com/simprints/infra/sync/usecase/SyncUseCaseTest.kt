package com.simprints.infra.sync.usecase

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.simprints.infra.eventsync.status.models.EventSyncState
import com.simprints.infra.eventsync.sync.EventSyncStateProcessor
import com.simprints.infra.sync.ExecutableSyncCommand
import com.simprints.infra.sync.ImageSyncStatus
import com.simprints.infra.sync.SyncCommands
import com.simprints.infra.sync.SyncStatus
import com.simprints.infra.sync.usecase.internal.ExecuteSyncCommandUseCase
import com.simprints.infra.sync.usecase.internal.ObserveImageSyncStatusUseCase
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SyncUseCaseTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @MockK
    private lateinit var eventSyncStateProcessor: EventSyncStateProcessor

    @MockK
    private lateinit var imageSync: ObserveImageSyncStatusUseCase

    @MockK
    private lateinit var executeSyncCommand: ExecuteSyncCommandUseCase

    private val eventSyncStatusFlow = MutableSharedFlow<EventSyncState>()
    private val imageSyncStatusFlow = MutableSharedFlow<ImageSyncStatus>()

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        every { eventSyncStateProcessor.getLastSyncState() } returns eventSyncStatusFlow
        every { imageSync.invoke() } returns imageSyncStatusFlow
        every { executeSyncCommand.invoke(any()) } returns Job().apply { complete() }
    }

    @Test
    fun `returns default SyncStatus before upstream flows emit`() = runTest {
        val expected = SyncStatus(
            eventSyncState = EventSyncState(
                syncId = "",
                progress = null,
                total = null,
                upSyncWorkersInfo = emptyList(),
                downSyncWorkersInfo = emptyList(),
                reporterStates = emptyList(),
                lastSyncTime = null,
            ),
            imageSyncStatus = ImageSyncStatus(
                isSyncing = false,
                progress = null,
                lastUpdateTimeMillis = -1L,
            ),
        )
        val useCase = SyncUseCase(eventSyncStateProcessor, imageSync, executeSyncCommand, appScope = backgroundScope)

        val resultFlow = useCase(SyncCommands.ObserveOnly).syncStatusFlow

        assertThat(resultFlow.value).isEqualTo(expected)
    }

    @Test
    fun `combines latest event and image states into SyncStatus`() = runTest {
        val event = EventSyncState(
            syncId = "sync-1",
            progress = 1,
            total = 10,
            upSyncWorkersInfo = emptyList(),
            downSyncWorkersInfo = emptyList(),
            reporterStates = emptyList(),
            lastSyncTime = null,
        )
        val image = ImageSyncStatus(
            isSyncing = true,
            progress = 2 to 5,
            lastUpdateTimeMillis = 123L,
        )
        val expected = SyncStatus(event, image)
        val useCase = SyncUseCase(eventSyncStateProcessor, imageSync, executeSyncCommand, appScope = backgroundScope)

        val resultFlow = useCase(SyncCommands.ObserveOnly).syncStatusFlow

        runCurrent() // ensure upstream flows are collected before emitting
        eventSyncStatusFlow.emit(event)
        imageSyncStatusFlow.emit(image)
        runCurrent()

        assertThat(resultFlow.value).isEqualTo(expected)
    }

    @Test
    fun `updates SyncStatus when event emits even if image never emits`() = runTest {
        val event = EventSyncState(
            syncId = "sync-1",
            progress = 1,
            total = 10,
            upSyncWorkersInfo = emptyList(),
            downSyncWorkersInfo = emptyList(),
            reporterStates = emptyList(),
            lastSyncTime = null,
        )
        val useCase = SyncUseCase(eventSyncStateProcessor, imageSync, executeSyncCommand, appScope = backgroundScope)

        val resultFlow = useCase(SyncCommands.ObserveOnly).syncStatusFlow

        runCurrent()
        val expected = with(resultFlow.value) {
            copy(eventSyncState = event)
        }
        eventSyncStatusFlow.emit(event)
        runCurrent()

        assertThat(resultFlow.value).isEqualTo(expected)
    }

    @Test
    fun `updates SyncStatus when image emits even if event never emits`() = runTest {
        val image = ImageSyncStatus(
            isSyncing = true,
            progress = 2 to 5,
            lastUpdateTimeMillis = 123L,
        )
        val useCase = SyncUseCase(eventSyncStateProcessor, imageSync, executeSyncCommand, appScope = backgroundScope)

        val resultFlow = useCase(SyncCommands.ObserveOnly).syncStatusFlow

        runCurrent()
        val expected = with(resultFlow.value) {
            copy(imageSyncStatus = image)
        }
        imageSyncStatusFlow.emit(image)
        runCurrent()

        assertThat(resultFlow.value).isEqualTo(expected)
    }

    @Test
    fun `updates SyncStatus when event sync state changes`() = runTest {
        val event1 = EventSyncState(
            syncId = "sync-1",
            progress = 1,
            total = 10,
            upSyncWorkersInfo = emptyList(),
            downSyncWorkersInfo = emptyList(),
            reporterStates = emptyList(),
            lastSyncTime = null,
        )
        val event2 = event1.copy(
            progress = 5,
        )
        val image = ImageSyncStatus(
            isSyncing = true,
            progress = 2 to 5,
            lastUpdateTimeMillis = 123L,
        )
        val expected1 = SyncStatus(event1, image)
        val expected2 = SyncStatus(event2, image)
        val useCase = SyncUseCase(eventSyncStateProcessor, imageSync, executeSyncCommand, appScope = backgroundScope)

        val resultFlow = useCase(SyncCommands.ObserveOnly).syncStatusFlow

        runCurrent()
        eventSyncStatusFlow.emit(event1)
        imageSyncStatusFlow.emit(image)
        runCurrent()

        assertThat(resultFlow.value).isEqualTo(expected1)

        eventSyncStatusFlow.emit(event2)
        runCurrent()

        assertThat(resultFlow.value).isEqualTo(expected2)
    }

    @Test
    fun `updates SyncStatus when image sync status changes`() = runTest {
        val event = EventSyncState(
            syncId = "sync-1",
            progress = 1,
            total = 10,
            upSyncWorkersInfo = emptyList(),
            downSyncWorkersInfo = emptyList(),
            reporterStates = emptyList(),
            lastSyncTime = null,
        )
        val image1 = ImageSyncStatus(
            isSyncing = true,
            progress = 2 to 5,
            lastUpdateTimeMillis = 123L,
        )
        val image2 = image1.copy(
            isSyncing = false,
        )
        val expected1 = SyncStatus(event, image1)
        val expected2 = SyncStatus(event, image2)
        val useCase = SyncUseCase(eventSyncStateProcessor, imageSync, executeSyncCommand, appScope = backgroundScope)

        val resultFlow = useCase(SyncCommands.ObserveOnly).syncStatusFlow

        runCurrent()
        eventSyncStatusFlow.emit(event)
        imageSyncStatusFlow.emit(image1)
        runCurrent()

        assertThat(resultFlow.value).isEqualTo(expected1)

        imageSyncStatusFlow.emit(image2)
        runCurrent()

        assertThat(resultFlow.value).isEqualTo(expected2)
    }

    @Test
    fun `returns the same shared StateFlow across invocations`() = runTest {
        val event = EventSyncState(
            syncId = "sync-1",
            progress = 1,
            total = 10,
            upSyncWorkersInfo = emptyList(),
            downSyncWorkersInfo = emptyList(),
            reporterStates = emptyList(),
            lastSyncTime = null,
        )
        val image1 = ImageSyncStatus(
            isSyncing = true,
            progress = 2 to 5,
            lastUpdateTimeMillis = 123L,
        )
        val image2 = image1.copy(
            isSyncing = false,
        )
        val useCase = SyncUseCase(eventSyncStateProcessor, imageSync, executeSyncCommand, appScope = backgroundScope)

        val resultFlow1 = useCase(SyncCommands.ObserveOnly).syncStatusFlow

        runCurrent()
        eventSyncStatusFlow.emit(event)
        imageSyncStatusFlow.emit(image1)
        runCurrent()

        imageSyncStatusFlow.emit(image2)
        runCurrent()

        val resultFlow2 = useCase(SyncCommands.ObserveOnly).syncStatusFlow

        assertThat(resultFlow1).isSameInstanceAs(resultFlow2)
        verify(exactly = 1) { eventSyncStateProcessor.getLastSyncState() }
        verify(exactly = 1) { imageSync() }
    }

    @Test
    fun `does not execute sync command for observe-only`() = runTest {
        val useCase = SyncUseCase(eventSyncStateProcessor, imageSync, executeSyncCommand, appScope = backgroundScope)

        val response = useCase(SyncCommands.ObserveOnly)

        assertThat(response.syncCommandJob.isCompleted).isTrue()
        verify(exactly = 0) { executeSyncCommand.invoke(any()) }
    }

    @Test
    fun `executes executable sync command and returns its job`() = runTest {
        val expectedJob = Job().apply { complete() }
        every { executeSyncCommand.invoke(any()) } returns expectedJob
        val useCase = SyncUseCase(eventSyncStateProcessor, imageSync, executeSyncCommand, appScope = backgroundScope)
        val command = SyncCommands.Schedule.Everything.stopAndStart() as ExecutableSyncCommand

        val response = useCase(command)

        assertThat(response.syncCommandJob).isSameInstanceAs(expectedJob)
        verify(exactly = 1) { executeSyncCommand.invoke(command) }
    }
}
