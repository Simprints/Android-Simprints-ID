package com.simprints.infra.sync.devicestate

import app.cash.turbine.test
import com.google.common.truth.Truth.*
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.enrolment.records.repository.EnrolmentRecordRepository
import com.simprints.infra.enrolment.records.repository.domain.models.EnrolmentRecordQuery
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.EventType
import com.simprints.infra.events.event.domain.models.scope.EventScopeType
import com.simprints.infra.eventsync.sync.common.EventSyncCache
import com.simprints.infra.images.ImageRepository
import com.simprints.infra.sync.ImageSyncTimestampProvider
import com.simprints.infra.sync.devicestate.internal.ObserveEnrolmentRecordsCountUseCase
import com.simprints.infra.sync.devicestate.internal.ObserveSamplesToUploadCountUseCase
import io.mockk.*
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DeviceStateDataTrackerImplTest {
    @MockK
    private lateinit var configRepository: ConfigRepository

    @MockK
    private lateinit var enrolmentRecordRepository: EnrolmentRecordRepository

    @MockK
    private lateinit var eventRepository: EventRepository

    @MockK
    private lateinit var imageRepository: ImageRepository

    @MockK
    private lateinit var eventSyncCache: EventSyncCache

    @MockK
    private lateinit var imageSyncTimestampProvider: ImageSyncTimestampProvider

    @MockK
    private lateinit var observeEnrolmentRecordsCount: ObserveEnrolmentRecordsCountUseCase

    @MockK
    private lateinit var observeSamplesToUploadCount: ObserveSamplesToUploadCountUseCase

    @MockK
    private lateinit var projectConfiguration: ProjectConfiguration

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        every { projectConfiguration.projectId } returns PROJECT_ID
        coEvery { configRepository.getProjectConfiguration() } returns projectConfiguration
        every { configRepository.observeProjectConfiguration() } returns flowOf(projectConfiguration)

        coEvery { enrolmentRecordRepository.count(any(), any()) } returns RECORDS
        every { observeEnrolmentRecordsCount() } returns flowOf(RECORDS)

        every { eventRepository.observeClosedEventScopeCounts() } returns flowOf(SCOPE_COUNTS)
        every { eventRepository.observeEventCount(null) } returns flowOf(EVENTS)
        every { eventRepository.observeEventCount(EventType.ENROLMENT_V2) } returns flowOf(ENROLMENTS_V2)
        every { eventRepository.observeEventCount(EventType.ENROLMENT_V4) } returns flowOf(ENROLMENTS_V4)

        coEvery { imageRepository.getNumberOfImagesToUpload(any()) } returns SAMPLES
        every { observeSamplesToUploadCount() } returns flowOf(SAMPLES)

        coEvery { eventSyncCache.readLastSuccessfulSyncTime() } returns Timestamp(EVENT_SYNC_AT)
        every { imageSyncTimestampProvider.getLastImageSyncTimestamp() } returns SAMPLE_SYNC_AT
    }

    // --- aggregation ---

    @Test
    fun `snapshot reads every source into the right field`() = runTest {
        val state = tracker(backgroundScope).getCurrentDeviceDataState()

        assertThat(state).isEqualTo(
            DeviceDataState(
                projectId = PROJECT_ID,
                recordCount = RECORDS,
                pendingScopes = SCOPE_COUNTS,
                pendingEvents = EVENTS,
                pendingEnrolments = ENROLMENTS_V2 + ENROLMENTS_V4,
                pendingSamples = SAMPLES,
                lastEventSyncAt = EVENT_SYNC_AT,
                lastSampleSyncAt = SAMPLE_SYNC_AT,
            ),
        )
    }

    @Test
    fun `observed state reads every source into the right field`() = runTest {
        tracker(backgroundScope).observeDeviceDataState().test {
            assertThat(awaitItem()).isEqualTo(
                DeviceDataState(
                    projectId = PROJECT_ID,
                    recordCount = RECORDS,
                    pendingScopes = SCOPE_COUNTS,
                    pendingEvents = EVENTS,
                    pendingEnrolments = ENROLMENTS_V2 + ENROLMENTS_V4,
                    pendingSamples = SAMPLES,
                    lastEventSyncAt = EVENT_SYNC_AT,
                    lastSampleSyncAt = SAMPLE_SYNC_AT,
                ),
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `snapshot scopes the record count to the signed-in project`() = runTest {
        tracker(backgroundScope).getCurrentDeviceDataState()

        // Matches the observed state, which scopes via ObserveEnrolmentRecordsCountUseCase.
        coVerify { enrolmentRecordRepository.count(EnrolmentRecordQuery(projectId = PROJECT_ID), any()) }
    }

    @Test
    fun `getRecordCount counts every project on the device`() = runTest {
        tracker(backgroundScope).getRecordCount()

        // The session scope payload must keep counting records left behind by an earlier project.
        coVerify { enrolmentRecordRepository.count(EnrolmentRecordQuery(), any()) }
    }

    // --- the never-throw contract, one case per source ---

    @Test
    fun `snapshot reports a failing record count as null and keeps the other fields`() = runTest {
        coEvery { enrolmentRecordRepository.count(any(), any()) } throws RuntimeException()

        val state = tracker(backgroundScope).getCurrentDeviceDataState()

        assertThat(state.recordCount).isNull()
        assertThat(state.pendingEvents).isEqualTo(EVENTS)
        assertThat(state.pendingSamples).isEqualTo(SAMPLES)
    }

    @Test
    fun `snapshot reports a failing scope count as null and keeps the other fields`() = runTest {
        every { eventRepository.observeClosedEventScopeCounts() } returns flow { throw RuntimeException() }

        val state = tracker(backgroundScope).getCurrentDeviceDataState()

        assertThat(state.pendingScopes).isNull()
        assertThat(state.recordCount).isEqualTo(RECORDS)
        assertThat(state.pendingSamples).isEqualTo(SAMPLES)
    }

    @Test
    fun `snapshot reports failing event counts as null and keeps the other fields`() = runTest {
        every { eventRepository.observeEventCount(null) } returns flow { throw RuntimeException() }
        every { eventRepository.observeEventCount(EventType.ENROLMENT_V2) } returns flow { throw RuntimeException() }

        val state = tracker(backgroundScope).getCurrentDeviceDataState()

        assertThat(state.pendingEvents).isNull()
        assertThat(state.pendingEnrolments).isNull()
        assertThat(state.recordCount).isEqualTo(RECORDS)
    }

    @Test
    fun `snapshot reports a failing sample count as null and keeps the other fields`() = runTest {
        coEvery { imageRepository.getNumberOfImagesToUpload(any()) } throws RuntimeException()

        val state = tracker(backgroundScope).getCurrentDeviceDataState()

        assertThat(state.pendingSamples).isNull()
        assertThat(state.recordCount).isEqualTo(RECORDS)
        assertThat(state.pendingEvents).isEqualTo(EVENTS)
    }

    @Test
    fun `snapshot reports failing sync timestamps as null and keeps the other fields`() = runTest {
        coEvery { eventSyncCache.readLastSuccessfulSyncTime() } throws RuntimeException()
        every { imageSyncTimestampProvider.getLastImageSyncTimestamp() } throws RuntimeException()

        val state = tracker(backgroundScope).getCurrentDeviceDataState()

        assertThat(state.lastEventSyncAt).isNull()
        assertThat(state.lastSampleSyncAt).isNull()
        assertThat(state.recordCount).isEqualTo(RECORDS)
    }

    @Test
    fun `snapshot reports a failing project id as null and does not throw`() = runTest {
        coEvery { configRepository.getProjectConfiguration() } throws RuntimeException()

        val state = tracker(backgroundScope).getCurrentDeviceDataState()

        assertThat(state.projectId).isNull()
        assertThat(state.pendingEvents).isEqualTo(EVENTS)
    }

    @Test
    fun `observed state reports a failing source as null rather than failing the stream`() = runTest {
        every { observeSamplesToUploadCount() } returns flow { throw RuntimeException() }

        tracker(backgroundScope).observeDeviceDataState().test {
            val state = awaitItem()
            assertThat(state.pendingSamples).isNull()
            assertThat(state.recordCount).isEqualTo(RECORDS)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `refresh re-subscribes a source that a failure left null`() = runTest {
        var firstCollection = true
        every { observeSamplesToUploadCount() } returns flow {
            if (firstCollection) {
                firstCollection = false
                throw RuntimeException()
            }
            emit(SAMPLES)
        }
        val tracker = tracker(backgroundScope)

        tracker.observeDeviceDataState().test {
            assertThat(awaitItem().pendingSamples).isNull()

            tracker.refresh()
            // Every source is re-subscribed, so settle before reading rather than racing the
            // order in which the five slots re-emit.
            runCurrent()

            // The source is rebuilt rather than left dark for the lifetime of the shared upstream.
            assertThat(expectMostRecentItem().pendingSamples).isEqualTo(SAMPLES)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `refresh leaves healthy sources alone`() = runTest {
        val tracker = tracker(backgroundScope)

        tracker.observeDeviceDataState().test {
            assertThat(awaitItem().recordCount).isEqualTo(RECORDS)

            tracker.refresh()
            runCurrent()

            assertThat(expectMostRecentItem().recordCount).isEqualTo(RECORDS)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // --- freshness ---

    @Test
    fun `snapshot is re-evaluated rather than replayed from the shared flow`() = runTest {
        val records = MutableStateFlow(RECORDS)
        every { observeEnrolmentRecordsCount() } returns records
        val tracker = tracker(backgroundScope)

        // Warm the shared flow up with the old value, as a live UI subscriber would.
        tracker.observeDeviceDataState().test {
            assertThat(awaitItem().recordCount).isEqualTo(RECORDS)
            cancelAndIgnoreRemainingEvents()
        }
        runCurrent()
        coEvery { enrolmentRecordRepository.count(any(), any()) } returns RECORDS + 7

        assertThat(tracker.getCurrentDeviceDataState().recordCount).isEqualTo(RECORDS + 7)
    }

    @Test
    fun `successive snapshots pick up a source change`() = runTest {
        val tracker = tracker(backgroundScope)
        assertThat(tracker.getCurrentDeviceDataState().pendingSamples).isEqualTo(SAMPLES)

        coEvery { imageRepository.getNumberOfImagesToUpload(any()) } returns SAMPLES + 1

        assertThat(tracker.getCurrentDeviceDataState().pendingSamples).isEqualTo(SAMPLES + 1)
    }

    @Test
    fun `observed state re-emits when a source changes`() = runTest {
        val records = MutableStateFlow(RECORDS)
        every { observeEnrolmentRecordsCount() } returns records

        tracker(backgroundScope).observeDeviceDataState().test {
            assertThat(awaitItem().recordCount).isEqualTo(RECORDS)

            records.value = RECORDS + 3

            assertThat(awaitItem().recordCount).isEqualTo(RECORDS + 3)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observed state does not re-walk the sample directory per emission`() = runTest {
        val records = MutableStateFlow(RECORDS)
        every { observeEnrolmentRecordsCount() } returns records

        tracker(backgroundScope).observeDeviceDataState().test {
            awaitItem()
            records.value = RECORDS + 1
            awaitItem()
            records.value = RECORDS + 2
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 0) { imageRepository.getNumberOfImagesToUpload(any()) }
    }

    @Test
    fun `observeDeviceDataState returns the same shared flow across invocations`() = runTest {
        val tracker = tracker(backgroundScope)

        val first = tracker.observeDeviceDataState()
        val collectJob = launch { first.collect { } }
        runCurrent()
        val second = tracker.observeDeviceDataState()

        assertThat(first).isSameInstanceAs(second)
        verify(exactly = 1) { observeEnrolmentRecordsCount() }
        verify(exactly = 1) { observeSamplesToUploadCount() }
        verify(exactly = 1) { eventRepository.observeClosedEventScopeCounts() }
        collectJob.cancel()
    }

    @Test
    fun `sources are subscribed only while the state is collected`() = runTest {
        val records = MutableSharedFlow<Int>(replay = 1).apply { tryEmit(RECORDS) }
        every { observeEnrolmentRecordsCount() } returns records

        val state = tracker(backgroundScope).observeDeviceDataState()
        runCurrent()
        assertThat(records.subscriptionCount.value).isEqualTo(0)

        val collectJob = launch { state.collect { } }
        runCurrent()
        assertThat(records.subscriptionCount.value).isEqualTo(1)

        collectJob.cancel()
        runCurrent()
        assertThat(records.subscriptionCount.value).isEqualTo(0)
    }

    @Test
    fun `holding the tracker touches no repository until something collects`() = runTest {
        val tracker = tracker(backgroundScope)

        tracker.observeDeviceDataState()
        runCurrent()

        verify(exactly = 0) { observeEnrolmentRecordsCount() }
        verify(exactly = 0) { observeSamplesToUploadCount() }
        verify(exactly = 0) { eventRepository.observeClosedEventScopeCounts() }
        verify(exactly = 0) { eventRepository.observeEventCount(any()) }
    }

    // --- convenience members ---

    @Test
    fun `getRecordCount does not walk the sample directory`() = runTest {
        val count = tracker(backgroundScope).getRecordCount()

        assertThat(count).isEqualTo(RECORDS)
        coVerify(exactly = 0) { imageRepository.getNumberOfImagesToUpload(any()) }
    }

    @Test
    fun `hasPendingEvents does not walk the sample directory`() = runTest {
        val hasPending = tracker(backgroundScope).hasPendingEvents()

        assertThat(hasPending).isTrue()
        coVerify(exactly = 0) { imageRepository.getNumberOfImagesToUpload(any()) }
    }

    @Test
    fun `hasPendingEvents ignores pending samples`() = runTest {
        every { eventRepository.observeEventCount(null) } returns flowOf(0)
        coEvery { imageRepository.getNumberOfImagesToUpload(any()) } returns 42

        assertThat(tracker(backgroundScope).hasPendingEvents()).isFalse()
    }

    @Test
    fun `getRecordCount propagates a source failure instead of degrading`() = runTest {
        coEvery { enrolmentRecordRepository.count(any(), any()) } throws RuntimeException()

        var thrown = false
        try {
            tracker(backgroundScope).getRecordCount()
        } catch (e: RuntimeException) {
            thrown = true
        }

        assertThat(thrown).isTrue()
    }

    @Test
    fun `hasPendingEvents propagates a source failure instead of degrading`() = runTest {
        every { eventRepository.observeEventCount(null) } returns flow { throw RuntimeException() }

        var thrown = false
        try {
            tracker(backgroundScope).hasPendingEvents()
        } catch (e: RuntimeException) {
            thrown = true
        }

        assertThat(thrown).isTrue()
    }

    // --- edge states ---

    @Test
    fun `failing to fetch config snapshot has no project id and null project-scoped counts`() = runTest {
        coEvery { configRepository.getProjectConfiguration() } throws Exception("stub")

        val state = tracker(backgroundScope).getCurrentDeviceDataState()

        assertThat(state.projectId).isNull()
        assertThat(state.recordCount).isNull()
        assertThat(state.pendingSamples).isNull()
    }

    @Test
    fun `signed out snapshot has no project id and zero project-scoped counts`() = runTest {
        every { projectConfiguration.projectId } returns ""

        val state = tracker(backgroundScope).getCurrentDeviceDataState()

        assertThat(state.projectId).isNull()
        assertThat(state.recordCount).isEqualTo(0)
        assertThat(state.pendingSamples).isEqualTo(0)
        assertThat(state.pendingEvents).isEqualTo(EVENTS)
        coVerify(exactly = 0) { enrolmentRecordRepository.count(any(), any()) }
        coVerify(exactly = 0) { imageRepository.getNumberOfImagesToUpload(any()) }
    }

    @Test
    fun `never-synced timestamps stay null rather than becoming zero`() = runTest {
        coEvery { eventSyncCache.readLastSuccessfulSyncTime() } returns null
        every { imageSyncTimestampProvider.getLastImageSyncTimestamp() } returns null

        val state = tracker(backgroundScope).getCurrentDeviceDataState()

        assertThat(state.lastEventSyncAt).isNull()
        assertThat(state.lastSampleSyncAt).isNull()
    }

    @Test
    fun `timestamps are re-read on every snapshot`() = runTest {
        val tracker = tracker(backgroundScope)
        tracker.getCurrentDeviceDataState()
        tracker.getCurrentDeviceDataState()

        coVerify(exactly = 2) { eventSyncCache.readLastSuccessfulSyncTime() }
        verify(exactly = 2) { imageSyncTimestampProvider.getLastImageSyncTimestamp() }
    }

    private fun tracker(scope: CoroutineScope) = DeviceStateDataTrackerImpl(
        configRepository = configRepository,
        enrolmentRecordRepository = enrolmentRecordRepository,
        eventRepository = eventRepository,
        imageRepository = imageRepository,
        eventSyncCache = eventSyncCache,
        imageSyncTimestampProvider = imageSyncTimestampProvider,
        observeEnrolmentRecordsCount = observeEnrolmentRecordsCount,
        observeSamplesToUploadCount = observeSamplesToUploadCount,
        appScope = scope,
        dispatcherIO = UnconfinedTestDispatcher(),
    )

    companion object {
        private const val PROJECT_ID = "projectId"
        private const val RECORDS = 5
        private const val EVENTS = 11
        private const val ENROLMENTS_V2 = 2
        private const val ENROLMENTS_V4 = 3
        private const val SAMPLES = 4
        private const val EVENT_SYNC_AT = 1000L
        private const val SAMPLE_SYNC_AT = 2000L
        private val SCOPE_COUNTS = EventScopeType.entries.associateWith { 1 }
    }
}
