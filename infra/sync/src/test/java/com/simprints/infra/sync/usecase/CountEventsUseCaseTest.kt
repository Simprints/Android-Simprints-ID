package com.simprints.infra.sync.usecase

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.EventType
import com.simprints.infra.eventsync.status.models.DownSyncCounts
import com.simprints.infra.eventsync.sync.down.EventDownSyncCountsRepository
import com.simprints.infra.eventsync.sync.down.EventDownSyncPeriodicCountUseCase
import com.simprints.infra.sync.SyncableCounts
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
class CountEventsUseCaseTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @MockK
    private lateinit var eventRepository: EventRepository

    @MockK
    private lateinit var downSyncCountsRepository: EventDownSyncCountsRepository

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
    }

    @Test
    fun `combines latest download and upload counts into EventCounts`() = runTest {
        val downSyncCountsFlow = MutableSharedFlow<DownSyncCounts>()
        val uploadFlowAll = MutableSharedFlow<Int>()
        val uploadFlowEnrolmentV2 = MutableSharedFlow<Int>()
        val uploadFlowEnrolmentV4 = MutableSharedFlow<Int>()
        val eventDownSyncCount = io.mockk.mockk<EventDownSyncPeriodicCountUseCase>()
        every { eventDownSyncCount.invoke() } returns downSyncCountsFlow
        coEvery { eventRepository.observeEventCount(null) } returns uploadFlowAll
        coEvery { eventRepository.observeEventCount(EventType.ENROLMENT_V2) } returns uploadFlowEnrolmentV2
        coEvery { eventRepository.observeEventCount(EventType.ENROLMENT_V4) } returns uploadFlowEnrolmentV4
        val useCase = CountSyncableUseCase(eventDownSyncCount, eventRepository, appScope = backgroundScope)
        val emitted = mutableListOf<SyncableCounts>()

        val collectJob = launch { useCase().take(1).toList(emitted) }

        runCurrent() // ensure upstream flows are collected before emitting
        val expected = SyncableCounts(
            eventsToDownload = 10,
            isEventsToDownloadLowerBound = true,
            eventsToUpload = 1,
            eventsToUploadEnrolmentV2 = 2,
            eventsToUploadEnrolmentV4 = 3,
        )
        downSyncCountsFlow.emit(DownSyncCounts(count = 10, isLowerBound = true))
        uploadFlowAll.emit(1)
        uploadFlowEnrolmentV2.emit(2)
        uploadFlowEnrolmentV4.emit(3)
        runCurrent()
        collectJob.join()

        assertThat(emitted).containsExactly(expected)
    }

    @Test
    fun `returns the same shared Flow across invocations`() = runTest {
        val downSyncCountsFlow = MutableSharedFlow<DownSyncCounts>(replay = 1)
        val uploadFlowAll = MutableSharedFlow<Int>(replay = 1)
        val uploadFlowEnrolmentV2 = MutableSharedFlow<Int>(replay = 1)
        val uploadFlowEnrolmentV4 = MutableSharedFlow<Int>(replay = 1)
        downSyncCountsFlow.tryEmit(DownSyncCounts(count = 0, isLowerBound = false))
        uploadFlowAll.tryEmit(0)
        uploadFlowEnrolmentV2.tryEmit(0)
        uploadFlowEnrolmentV4.tryEmit(0)
        val eventDownSyncCount = io.mockk.mockk<EventDownSyncPeriodicCountUseCase>()
        every { eventDownSyncCount.invoke() } returns downSyncCountsFlow
        coEvery { eventRepository.observeEventCount(null) } returns uploadFlowAll
        coEvery { eventRepository.observeEventCount(EventType.ENROLMENT_V2) } returns uploadFlowEnrolmentV2
        coEvery { eventRepository.observeEventCount(EventType.ENROLMENT_V4) } returns uploadFlowEnrolmentV4
        val useCase = CountSyncableUseCase(eventDownSyncCount, eventRepository, appScope = backgroundScope)

        val flow1 = useCase()

        val collectJob = launch { flow1.collect { } }
        runCurrent()

        val flow2 = useCase()

        assertThat(flow1).isSameInstanceAs(flow2)
        verify(exactly = 1) { eventDownSyncCount() }
        coVerify(exactly = 1) { eventRepository.observeEventCount(null) }
        coVerify(exactly = 1) { eventRepository.observeEventCount(EventType.ENROLMENT_V2) }
        coVerify(exactly = 1) { eventRepository.observeEventCount(EventType.ENROLMENT_V4) }
        collectJob.cancel()
    }

    @Test
    fun `subscribes to down-sync counts only while collected`() = runTest {
        val downSyncCountsFlow = MutableSharedFlow<DownSyncCounts>(replay = 1).apply {
            tryEmit(DownSyncCounts(count = 0, isLowerBound = false))
        }
        val uploadFlowAll = MutableSharedFlow<Int>(replay = 1).apply { tryEmit(0) }
        val uploadFlowEnrolmentV2 = MutableSharedFlow<Int>(replay = 1).apply { tryEmit(0) }
        val uploadFlowEnrolmentV4 = MutableSharedFlow<Int>(replay = 1).apply { tryEmit(0) }
        val eventDownSyncCount = io.mockk.mockk<EventDownSyncPeriodicCountUseCase>()
        every { eventDownSyncCount.invoke() } returns downSyncCountsFlow
        coEvery { eventRepository.observeEventCount(null) } returns uploadFlowAll
        coEvery { eventRepository.observeEventCount(EventType.ENROLMENT_V2) } returns uploadFlowEnrolmentV2
        coEvery { eventRepository.observeEventCount(EventType.ENROLMENT_V4) } returns uploadFlowEnrolmentV4
        val useCase = CountSyncableUseCase(eventDownSyncCount, eventRepository, appScope = backgroundScope)

        val sharedFlow = useCase()
        runCurrent()
        assertThat(downSyncCountsFlow.subscriptionCount.value).isEqualTo(0)

        val collectJob = launch { sharedFlow.collect { } }
        runCurrent()
        assertThat(downSyncCountsFlow.subscriptionCount.value).isEqualTo(1)

        collectJob.cancel()
        runCurrent()
        assertThat(downSyncCountsFlow.subscriptionCount.value).isEqualTo(0)
    }

    @Test
    fun `stops down-sync periodic counting when CountEventsUseCase unsubscribed`() = runTest {
        // integration test case with DownSyncCountsRepository, to check for its accidental overuse
        coEvery { downSyncCountsRepository.countEventsToDownload() } returnsMany listOf(
            DownSyncCounts(count = 1, isLowerBound = false), // initial
            DownSyncCounts(count = 2, isLowerBound = false), // immediate periodic on subscribe
            DownSyncCounts(count = 3, isLowerBound = false), // would be after interval if still subscribed
        )
        val timeHelper = io.mockk.mockk<TimeHelper>()
        every { timeHelper.now() } answers { Timestamp(testScheduler.currentTime) }
        val eventDownSyncCount = EventDownSyncPeriodicCountUseCase(
            downSyncCountsRepository,
            timeHelper = timeHelper,
            appScope = backgroundScope,
        )
        runCurrent()
        advanceTimeBy(10_000L + 1) // debounce time, from EventDownSyncPeriodicCountUseCase
        runCurrent()
        val uploadFlowAll = MutableSharedFlow<Int>(replay = 1).apply { tryEmit(0) }
        val uploadFlowEnrolmentV2 = MutableSharedFlow<Int>(replay = 1).apply { tryEmit(0) }
        val uploadFlowEnrolmentV4 = MutableSharedFlow<Int>(replay = 1).apply { tryEmit(0) }
        coEvery { eventRepository.observeEventCount(null) } returns uploadFlowAll
        coEvery { eventRepository.observeEventCount(EventType.ENROLMENT_V2) } returns uploadFlowEnrolmentV2
        coEvery { eventRepository.observeEventCount(EventType.ENROLMENT_V4) } returns uploadFlowEnrolmentV4

        val useCase = CountSyncableUseCase(eventDownSyncCount, eventRepository, appScope = backgroundScope)

        val collectJob = launch { useCase().collect { } }
        runCurrent()
        coVerify(exactly = 2) { downSyncCountsRepository.countEventsToDownload() }

        collectJob.cancel()
        runCurrent()
        val downSyncIntervalMillis = 300_000L // from EventDownSyncPeriodicCountUseCase
        advanceTimeBy(downSyncIntervalMillis * 2)
        runCurrent()
        coVerify(exactly = 2) { downSyncCountsRepository.countEventsToDownload() }
    }
}
