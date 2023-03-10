package com.simprints.infra.eventsync.sync.down

import com.simprints.infra.events.sampledata.SampleDefaults.GUID1
import com.simprints.infra.eventsync.SampleSyncScopes.projectDownSyncScope
import com.simprints.infra.eventsync.sync.common.EventSyncCache
import com.simprints.infra.eventsync.sync.common.WorkerProgressCountReporter
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

internal class EventDownSyncDownloaderTaskTest {

    @MockK
    private lateinit var syncCache: EventSyncCache

    @MockK
    private lateinit var reporter: WorkerProgressCountReporter

    @MockK
    private lateinit var syncHelper: EventDownSyncHelper

    private lateinit var eventDownSyncDownloaderTask: EventDownSyncDownloaderTask

    private lateinit var downloadProgressChannel: Channel<EventDownSyncProgress>

    private val op = projectDownSyncScope.operations.first()

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        eventDownSyncDownloaderTask = EventDownSyncDownloaderTask()

        runBlocking {
            mockProgressEmission(emptyList())
        }
    }

    @Test
    fun task_shouldResumeTheProgress() = runTest {
        eventDownSyncDownloaderTask.execute(GUID1, op, syncHelper, syncCache, reporter, this)

        coVerify { syncCache.readProgress(GUID1) }
    }

    @Test
    fun task_shouldUseTheDownSyncHelperChannel() = runTest {
        eventDownSyncDownloaderTask.execute(GUID1, op, syncHelper, syncCache, reporter, this)

        coVerify { syncHelper.downSync(this@runTest, op) }
    }

    @Test
    fun task_forEveryDownloadedEventShouldUpdateTheProgress() = runTest {
        mockProgressEmission(listOf(EventDownSyncProgress(op, 1)))

        eventDownSyncDownloaderTask.execute(GUID1, op, syncHelper, syncCache, reporter, this)

        coVerify { reporter.reportCount(1) }
    }

    @Test
    fun task_forEveryDownloadedEventShouldStoreTheProgress() = runTest {
        mockProgressEmission(listOf(EventDownSyncProgress(op, 1)))

        eventDownSyncDownloaderTask.execute(GUID1, op, syncHelper, syncCache, reporter, this)

        coVerify { syncCache.saveProgress(GUID1, 1) }
    }

    private suspend fun mockProgressEmission(progressEvents: List<EventDownSyncProgress>) {
        coEvery { syncHelper.downSync(any(), any()) } returns progressEvents.asFlow()
    }
}
