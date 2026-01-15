package com.simprints.infra.sync.usecase

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.simprints.infra.eventsync.status.models.EventSyncState
import com.simprints.infra.sync.ImageSyncStatus
import com.simprints.infra.sync.SyncCommand
import com.simprints.infra.sync.SyncStatus
import com.simprints.infra.sync.usecase.internal.EventSyncUseCase
import com.simprints.infra.sync.usecase.internal.ImageSyncUseCase
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.runCurrent
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SyncUseCaseTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @MockK
    private lateinit var eventSync: EventSyncUseCase

    @MockK
    private lateinit var imageSync: ImageSyncUseCase

    private val eventSyncStatusFlow = MutableSharedFlow<EventSyncState>()
    private val imageSyncStatusFlow = MutableSharedFlow<ImageSyncStatus>()

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        every { eventSync.invoke() } returns eventSyncStatusFlow
        every { imageSync.invoke() } returns imageSyncStatusFlow
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
        val useCase = SyncUseCase(eventSync, imageSync, appScope = backgroundScope)

        val resultFlow = useCase(eventSync = SyncCommand.OBSERVE_ONLY, imageSync = SyncCommand.OBSERVE_ONLY)

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
        val useCase = SyncUseCase(eventSync, imageSync, appScope = backgroundScope)

        val resultFlow = useCase(eventSync = SyncCommand.OBSERVE_ONLY, imageSync = SyncCommand.OBSERVE_ONLY)

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
        val useCase = SyncUseCase(eventSync, imageSync, appScope = backgroundScope)

        val resultFlow = useCase(eventSync = SyncCommand.OBSERVE_ONLY, imageSync = SyncCommand.OBSERVE_ONLY)

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
        val useCase = SyncUseCase(eventSync, imageSync, appScope = backgroundScope)

        val resultFlow = useCase(eventSync = SyncCommand.OBSERVE_ONLY, imageSync = SyncCommand.OBSERVE_ONLY)

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
        val useCase = SyncUseCase(eventSync, imageSync, appScope = backgroundScope)

        val resultFlow = useCase(eventSync = SyncCommand.OBSERVE_ONLY, imageSync = SyncCommand.OBSERVE_ONLY)

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
        val useCase = SyncUseCase(eventSync, imageSync, appScope = backgroundScope)

        val resultFlow = useCase(eventSync = SyncCommand.OBSERVE_ONLY, imageSync = SyncCommand.OBSERVE_ONLY)

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
        val useCase = SyncUseCase(eventSync, imageSync, appScope = backgroundScope)

        val resultFlow1 = useCase(eventSync = SyncCommand.OBSERVE_ONLY, imageSync = SyncCommand.OBSERVE_ONLY)

        runCurrent()
        eventSyncStatusFlow.emit(event)
        imageSyncStatusFlow.emit(image1)
        runCurrent()

        imageSyncStatusFlow.emit(image2)
        runCurrent()

        val resultFlow2 = useCase(eventSync = SyncCommand.OBSERVE_ONLY, imageSync = SyncCommand.OBSERVE_ONLY)

        assertThat(resultFlow1).isSameInstanceAs(resultFlow2)
        verify(exactly = 1) { eventSync() }
        verify(exactly = 1) { imageSync() }
    }
}
