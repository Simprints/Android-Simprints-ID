package com.simprints.infra.eventsync.sync.down

import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.eventsync.status.models.DownSyncCounts
import com.simprints.infra.eventsync.sync.down.EventDownSyncPeriodicCountUseCase.Companion.DOWN_SYNC_COUNT_DEBOUNCE_MILLIS
import com.simprints.infra.eventsync.sync.down.EventDownSyncPeriodicCountUseCase.Companion.DOWN_SYNC_COUNT_INTERVAL_MILLIS
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class EventDownSyncPeriodicCountUseCaseTest {

    @MockK
    private lateinit var repository: EventDownSyncCountsRepository

    @MockK
    private lateinit var timeHelper: TimeHelper

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `emits down sync counts to subscribers`() = runTest {
        every { timeHelper.now() } answers { Timestamp(testScheduler.currentTime) }
        val expected = listOf(
            DownSyncCounts(1, isLowerBound = false),
            DownSyncCounts(2, isLowerBound = true),
        )
        coEvery { repository.countEventsToDownload() } returnsMany expected

        val useCase = EventDownSyncPeriodicCountUseCase(repository, timeHelper = timeHelper, appScope = backgroundScope)
        runCurrent()
        advanceTimeBy(DOWN_SYNC_COUNT_DEBOUNCE_MILLIS + 1)
        runCurrent()

        val emitted = mutableListOf<DownSyncCounts>()
        val collectJob = launch { useCase().take(expected.size).toList(emitted) }
        runCurrent()

        collectJob.join()

        assertThat(emitted).containsExactlyElementsIn(expected).inOrder()
    }

    @Test
    fun `counts once on initialisation`() = runTest {
        every { timeHelper.now() } answers { Timestamp(testScheduler.currentTime) }
        coEvery { repository.countEventsToDownload() } returns DownSyncCounts(1, isLowerBound = false)

        EventDownSyncPeriodicCountUseCase(repository, timeHelper = timeHelper, appScope = backgroundScope)
        runCurrent()

        coVerify(exactly = 1) { repository.countEventsToDownload() }
    }

    @Test
    fun `does not count periodically without subscribers`() = runTest {
        every { timeHelper.now() } answers { Timestamp(testScheduler.currentTime) }
        coEvery { repository.countEventsToDownload() } returns DownSyncCounts(1, isLowerBound = false)

        EventDownSyncPeriodicCountUseCase(repository, timeHelper = timeHelper, appScope = backgroundScope)
        runCurrent()

        advanceTimeBy(DOWN_SYNC_COUNT_INTERVAL_MILLIS * 2)
        runCurrent()
        coVerify(exactly = 1) { repository.countEventsToDownload() }
    }

    @Test
    fun `counts immediately and every interval while subscribed`() = runTest {
        every { timeHelper.now() } answers { Timestamp(testScheduler.currentTime) }
        coEvery { repository.countEventsToDownload() } returnsMany listOf(
            DownSyncCounts(1, isLowerBound = false), // initial
            DownSyncCounts(2, isLowerBound = false), // immediate periodic
            DownSyncCounts(3, isLowerBound = false), // after interval
        )
        val useCase = EventDownSyncPeriodicCountUseCase(repository, timeHelper = timeHelper, appScope = backgroundScope)
        runCurrent()
        advanceTimeBy(DOWN_SYNC_COUNT_DEBOUNCE_MILLIS + 1)
        runCurrent()

        val collectJob = launch { useCase().collect { } }
        runCurrent()

        coVerify(exactly = 2) { repository.countEventsToDownload() }

        advanceTimeBy(DOWN_SYNC_COUNT_INTERVAL_MILLIS)
        runCurrent()

        coVerify(exactly = 3) { repository.countEventsToDownload() }

        collectJob.cancel()
    }

    @Test
    fun `stops counting when unsubscribed and resumes on resubscribe`() = runTest {
        every { timeHelper.now() } answers { Timestamp(testScheduler.currentTime) }
        coEvery { repository.countEventsToDownload() } returnsMany listOf(
            DownSyncCounts(1, isLowerBound = false), // initial
            DownSyncCounts(2, isLowerBound = false), // immediate periodic on first sub
            DownSyncCounts(3, isLowerBound = false), // immediate periodic on second sub
        )
        val useCase = EventDownSyncPeriodicCountUseCase(repository, timeHelper = timeHelper, appScope = backgroundScope)
        runCurrent()
        advanceTimeBy(DOWN_SYNC_COUNT_DEBOUNCE_MILLIS + 1)
        runCurrent()

        val firstJob = launch { useCase().collect { } }
        runCurrent()
        coVerify(exactly = 2) { repository.countEventsToDownload() }

        firstJob.cancel()
        runCurrent()

        advanceTimeBy(DOWN_SYNC_COUNT_INTERVAL_MILLIS)
        runCurrent()

        coVerify(exactly = 2) { repository.countEventsToDownload() }

        val secondJob = launch { useCase().collect { } }
        runCurrent()

        coVerify(exactly = 3) { repository.countEventsToDownload() }

        secondJob.cancel()
    }

    @Test
    fun `resumes paused periodic counting when resubscribed`() = runTest {
        every { timeHelper.now() } answers { Timestamp(testScheduler.currentTime) }
        coEvery { repository.countEventsToDownload() } returnsMany listOf(
            DownSyncCounts(1, isLowerBound = false), // initial
            DownSyncCounts(2, isLowerBound = false), // immediate periodic on first sub
            DownSyncCounts(3, isLowerBound = false), // immediate periodic on second sub
            DownSyncCounts(4, isLowerBound = false), // after interval on second sub
        )
        val useCase = EventDownSyncPeriodicCountUseCase(repository, timeHelper = timeHelper, appScope = backgroundScope)
        runCurrent()
        advanceTimeBy(DOWN_SYNC_COUNT_DEBOUNCE_MILLIS + 1)
        runCurrent()

        val firstJob = launch { useCase().collect { } }
        runCurrent()
        coVerify(exactly = 2) { repository.countEventsToDownload() }

        firstJob.cancel()
        runCurrent()

        advanceTimeBy(DOWN_SYNC_COUNT_INTERVAL_MILLIS * 2)
        runCurrent()
        coVerify(exactly = 2) { repository.countEventsToDownload() }

        val secondJob = launch { useCase().collect { } }
        runCurrent()
        coVerify(exactly = 3) { repository.countEventsToDownload() }

        advanceTimeBy(DOWN_SYNC_COUNT_INTERVAL_MILLIS)
        runCurrent()
        coVerify(exactly = 4) { repository.countEventsToDownload() }
        secondJob.cancel()
    }

    @Test
    fun `does not run consecutive counts when resubscribed within debounce time`() = runTest {
        every { timeHelper.now() } answers { Timestamp(testScheduler.currentTime) }
        coEvery { repository.countEventsToDownload() } returnsMany listOf(
            DownSyncCounts(1, isLowerBound = false), // initial
            DownSyncCounts(2, isLowerBound = false), // immediate periodic on first sub
            DownSyncCounts(3, isLowerBound = false), // would be immediate periodic on second sub
        )
        val useCase = EventDownSyncPeriodicCountUseCase(repository, timeHelper = timeHelper, appScope = backgroundScope)
        runCurrent()

        advanceTimeBy(DOWN_SYNC_COUNT_DEBOUNCE_MILLIS + 1)
        runCurrent()

        val firstJob = launch { useCase().collect { } }
        runCurrent()
        coVerify(exactly = 2) { repository.countEventsToDownload() }

        firstJob.cancel()
        runCurrent()

        val secondJob = launch { useCase().collect { } }
        runCurrent()
        coVerify(exactly = 2) { repository.countEventsToDownload() }

        secondJob.cancel()
    }

    @Test
    fun `runs consecutive counts when resubscribed after debounce time`() = runTest {
        every { timeHelper.now() } answers { Timestamp(testScheduler.currentTime) }
        coEvery { repository.countEventsToDownload() } returnsMany listOf(
            DownSyncCounts(1, isLowerBound = false), // initial
            DownSyncCounts(2, isLowerBound = false), // immediate periodic on first sub
            DownSyncCounts(3, isLowerBound = false), // immediate periodic on second sub
        )
        val useCase = EventDownSyncPeriodicCountUseCase(repository, timeHelper = timeHelper, appScope = backgroundScope)
        runCurrent()
        advanceTimeBy(DOWN_SYNC_COUNT_DEBOUNCE_MILLIS + 1)
        runCurrent()

        val firstJob = launch { useCase().collect { } }
        runCurrent()
        coVerify(exactly = 2) { repository.countEventsToDownload() }
        firstJob.cancel()
        runCurrent()
        advanceTimeBy(DOWN_SYNC_COUNT_DEBOUNCE_MILLIS + 1)
        runCurrent()

        val secondJob = launch { useCase().collect { } }
        runCurrent()
        coVerify(exactly = 3) { repository.countEventsToDownload() }
        secondJob.cancel()
    }

    @Test
    fun `replays latest value to new subscribers`() = runTest {
        every { timeHelper.now() } answers { Timestamp(testScheduler.currentTime) }
        val initial = DownSyncCounts(1, isLowerBound = false)
        val immediate = DownSyncCounts(2, isLowerBound = false)
        val afterInterval = DownSyncCounts(3, isLowerBound = true)
        coEvery { repository.countEventsToDownload() } returnsMany listOf(initial, immediate, afterInterval)

        val useCase = EventDownSyncPeriodicCountUseCase(repository, timeHelper = timeHelper, appScope = backgroundScope)
        runCurrent()

        advanceTimeBy(DOWN_SYNC_COUNT_DEBOUNCE_MILLIS + 1)
        runCurrent()

        val activeSubscriberJob = launch { useCase().collect { } }
        runCurrent()

        advanceTimeBy(DOWN_SYNC_COUNT_INTERVAL_MILLIS)
        runCurrent()

        coVerify(exactly = 3) { repository.countEventsToDownload() }

        val replayedValue = useCase().first()

        assertThat(replayedValue).isEqualTo(afterInterval)
        coVerify(exactly = 3) { repository.countEventsToDownload() }

        activeSubscriberJob.cancel()
    }

}
