package com.simprints.infra.sync.usecase

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.EventType
import com.simprints.infra.eventsync.status.models.DownSyncCounts
import com.simprints.infra.eventsync.sync.down.CountEventsToDownloadUseCase
import com.simprints.infra.eventsync.sync.down.EventDownSyncPeriodicCountUseCase
import com.simprints.infra.sync.SyncableCounts
import com.simprints.infra.sync.usecase.internal.ObserveEnrolmentRecordsCountUseCase
import com.simprints.infra.sync.usecase.internal.ObserveSamplesToUploadCountUseCase
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ObserveSyncableCountsUseCaseTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @MockK
    private lateinit var eventRepository: EventRepository

    @MockK
    private lateinit var countEventsToDownload: CountEventsToDownloadUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
    }

    @Test
    fun `combines latest download and upload counts into EventCounts`() = runTest {
        val totalRecordsCountFlow = MutableSharedFlow<Int>()
        val recordEventsToDownloadCountFlow = MutableSharedFlow<DownSyncCounts>()
        val eventsToUploadCountFlow = MutableSharedFlow<Int>()
        val enrolmentsToUploadCountFlowV2 = MutableSharedFlow<Int>()
        val enrolmentsToUploadCountFlowV4 = MutableSharedFlow<Int>()
        val samplesToUploadCountFlow = MutableSharedFlow<Int>()
        val eventDownSyncCount = io.mockk.mockk<EventDownSyncPeriodicCountUseCase>()
        val countEnrolmentRecords = io.mockk.mockk<ObserveEnrolmentRecordsCountUseCase>()
        val countSamplesToUpload = io.mockk.mockk<ObserveSamplesToUploadCountUseCase>()
        every { eventDownSyncCount.invoke() } returns recordEventsToDownloadCountFlow
        every { countEnrolmentRecords.invoke() } returns totalRecordsCountFlow
        every { countSamplesToUpload.invoke() } returns samplesToUploadCountFlow
        coEvery { eventRepository.observeEventCount(null) } returns eventsToUploadCountFlow
        coEvery { eventRepository.observeEventCount(EventType.ENROLMENT_V2) } returns enrolmentsToUploadCountFlowV2
        coEvery { eventRepository.observeEventCount(EventType.ENROLMENT_V4) } returns enrolmentsToUploadCountFlowV4
        val useCase =
            ObserveSyncableCountsUseCase(
                countEnrolmentRecords,
                countSamplesToUpload,
                eventDownSyncCount,
                eventRepository,
                appScope = backgroundScope,
            )
        val emitted = mutableListOf<SyncableCounts>()

        val collectJob = launch { useCase().take(1).toList(emitted) }

        runCurrent() // ensure upstream flows are collected before emitting
        val expected = SyncableCounts(
            totalRecords = 5,
            recordEventsToDownload = 10,
            isRecordEventsToDownloadLowerBound = true,
            eventsToUpload = 1,
            enrolmentsToUpload = 5, // 2 of V2 + 3 of V4
            samplesToUpload = 4,
        )
        totalRecordsCountFlow.emit(5)
        recordEventsToDownloadCountFlow.emit(DownSyncCounts(count = 10, isLowerBound = true))
        eventsToUploadCountFlow.emit(1)
        enrolmentsToUploadCountFlowV2.emit(2)
        enrolmentsToUploadCountFlowV4.emit(3)
        samplesToUploadCountFlow.emit(4)
        runCurrent()
        collectJob.join()

        assertThat(emitted).containsExactly(expected)
    }

    @Test
    fun `returns the same shared Flow across invocations`() = runTest {
        val totalRecordsCountFlow = MutableSharedFlow<Int>(replay = 1)
        val recordEventsToDownloadCountFlow = MutableSharedFlow<DownSyncCounts>(replay = 1)
        val eventsToUploadCountFlow = MutableSharedFlow<Int>(replay = 1)
        val enrolmentsToUploadCountFlowV2 = MutableSharedFlow<Int>(replay = 1)
        val enrolmentsToUploadCountFlowV4 = MutableSharedFlow<Int>(replay = 1)
        val samplesToUploadCountFlow = MutableSharedFlow<Int>(replay = 1)
        totalRecordsCountFlow.tryEmit(0)
        recordEventsToDownloadCountFlow.tryEmit(DownSyncCounts(count = 0, isLowerBound = false))
        eventsToUploadCountFlow.tryEmit(0)
        enrolmentsToUploadCountFlowV2.tryEmit(0)
        enrolmentsToUploadCountFlowV4.tryEmit(0)
        samplesToUploadCountFlow.tryEmit(0)
        val eventDownSyncCount = io.mockk.mockk<EventDownSyncPeriodicCountUseCase>()
        val countEnrolmentRecords = io.mockk.mockk<ObserveEnrolmentRecordsCountUseCase>()
        val countSamplesToUpload = io.mockk.mockk<ObserveSamplesToUploadCountUseCase>()
        every { eventDownSyncCount.invoke() } returns recordEventsToDownloadCountFlow
        every { countEnrolmentRecords.invoke() } returns totalRecordsCountFlow
        every { countSamplesToUpload.invoke() } returns samplesToUploadCountFlow
        coEvery { eventRepository.observeEventCount(null) } returns eventsToUploadCountFlow
        coEvery { eventRepository.observeEventCount(EventType.ENROLMENT_V2) } returns enrolmentsToUploadCountFlowV2
        coEvery { eventRepository.observeEventCount(EventType.ENROLMENT_V4) } returns enrolmentsToUploadCountFlowV4
        val useCase =
            ObserveSyncableCountsUseCase(
                countEnrolmentRecords,
                countSamplesToUpload,
                eventDownSyncCount,
                eventRepository,
                appScope = backgroundScope,
            )

        val flow1 = useCase()

        val collectJob = launch { flow1.collect { } }
        runCurrent()

        val flow2 = useCase()

        assertThat(flow1).isSameInstanceAs(flow2)
        verify(exactly = 1) { eventDownSyncCount() }
        verify(exactly = 1) { countEnrolmentRecords() }
        verify(exactly = 1) { countSamplesToUpload() }
        coVerify(exactly = 1) { eventRepository.observeEventCount(null) }
        coVerify(exactly = 1) { eventRepository.observeEventCount(EventType.ENROLMENT_V2) }
        coVerify(exactly = 1) { eventRepository.observeEventCount(EventType.ENROLMENT_V4) }
        collectJob.cancel()
    }

    @Test
    fun `subscribes to down-sync counts only while collected`() = runTest {
        val totalRecordsCountFlow = MutableSharedFlow<Int>(replay = 1).apply { tryEmit(0) }
        val recordEventsToDownloadCountFlow = MutableSharedFlow<DownSyncCounts>(replay = 1).apply {
            tryEmit(DownSyncCounts(count = 0, isLowerBound = false))
        }
        val eventsToUploadCountFlow = MutableSharedFlow<Int>(replay = 1).apply { tryEmit(0) }
        val enrolmentsToUploadCountFlowV2 = MutableSharedFlow<Int>(replay = 1).apply { tryEmit(0) }
        val enrolmentsToUploadCountFlowV4 = MutableSharedFlow<Int>(replay = 1).apply { tryEmit(0) }
        val samplesToUploadCountFlow = MutableSharedFlow<Int>(replay = 1).apply { tryEmit(0) }
        val eventDownSyncCount = io.mockk.mockk<EventDownSyncPeriodicCountUseCase>()
        val countEnrolmentRecords = io.mockk.mockk<ObserveEnrolmentRecordsCountUseCase>()
        val countSamplesToUpload = io.mockk.mockk<ObserveSamplesToUploadCountUseCase>()
        every { eventDownSyncCount.invoke() } returns recordEventsToDownloadCountFlow
        every { countEnrolmentRecords.invoke() } returns totalRecordsCountFlow
        every { countSamplesToUpload.invoke() } returns samplesToUploadCountFlow
        coEvery { eventRepository.observeEventCount(null) } returns eventsToUploadCountFlow
        coEvery { eventRepository.observeEventCount(EventType.ENROLMENT_V2) } returns enrolmentsToUploadCountFlowV2
        coEvery { eventRepository.observeEventCount(EventType.ENROLMENT_V4) } returns enrolmentsToUploadCountFlowV4
        val useCase =
            ObserveSyncableCountsUseCase(
                countEnrolmentRecords,
                countSamplesToUpload,
                eventDownSyncCount,
                eventRepository,
                appScope = backgroundScope,
            )

        val sharedFlow = useCase()
        runCurrent()
        assertThat(recordEventsToDownloadCountFlow.subscriptionCount.value).isEqualTo(0)

        val collectJob = launch { sharedFlow.collect { } }
        runCurrent()
        assertThat(recordEventsToDownloadCountFlow.subscriptionCount.value).isEqualTo(1)

        collectJob.cancel()
        runCurrent()
        assertThat(recordEventsToDownloadCountFlow.subscriptionCount.value).isEqualTo(0)
    }

    @Test
    fun `stops down-sync periodic counting when downstream unsubscribed`() = runTest {
        // integration test case with DownSyncCountsRepository, to check for its accidental overuse
        coEvery { countEventsToDownload() } returnsMany listOf(
            DownSyncCounts(count = 1, isLowerBound = false), // initial
            DownSyncCounts(count = 2, isLowerBound = false), // immediate periodic on subscribe
            DownSyncCounts(count = 3, isLowerBound = false), // would be after interval if still subscribed
        )
        val eventDownSyncCount = EventDownSyncPeriodicCountUseCase(
            countEventsToDownload,
            appScope = backgroundScope,
        )
        runCurrent()
        advanceTimeBy(10_000L + 1) // debounce time, from EventDownSyncPeriodicCountUseCase
        runCurrent()
        val totalRecordsCountFlow = MutableSharedFlow<Int>(replay = 1).apply { tryEmit(0) }
        val eventsToUploadCountFlow = MutableSharedFlow<Int>(replay = 1).apply { tryEmit(0) }
        val enrolmentsToUploadCountFlowV2 = MutableSharedFlow<Int>(replay = 1).apply { tryEmit(0) }
        val enrolmentsToUploadCountFlowV4 = MutableSharedFlow<Int>(replay = 1).apply { tryEmit(0) }
        val samplesToUploadCountFlow = MutableSharedFlow<Int>(replay = 1).apply { tryEmit(0) }
        val countEnrolmentRecords = io.mockk.mockk<ObserveEnrolmentRecordsCountUseCase>()
        val countSamplesToUpload = io.mockk.mockk<ObserveSamplesToUploadCountUseCase>()
        every { countEnrolmentRecords.invoke() } returns totalRecordsCountFlow
        every { countSamplesToUpload.invoke() } returns samplesToUploadCountFlow
        coEvery { eventRepository.observeEventCount(null) } returns eventsToUploadCountFlow
        coEvery { eventRepository.observeEventCount(EventType.ENROLMENT_V2) } returns enrolmentsToUploadCountFlowV2
        coEvery { eventRepository.observeEventCount(EventType.ENROLMENT_V4) } returns enrolmentsToUploadCountFlowV4

        val useCase =
            ObserveSyncableCountsUseCase(
                countEnrolmentRecords,
                countSamplesToUpload,
                eventDownSyncCount,
                eventRepository,
                appScope = backgroundScope,
            )

        val collectJob = launch { useCase().collect { } }
        runCurrent()
        coVerify(exactly = 2) { countEventsToDownload() }

        collectJob.cancel()
        runCurrent()
        val downSyncIntervalMillis = 300_000L // from EventDownSyncPeriodicCountUseCase
        advanceTimeBy(downSyncIntervalMillis * 2)
        runCurrent()
        coVerify(exactly = 2) { countEventsToDownload() }
    }
}
