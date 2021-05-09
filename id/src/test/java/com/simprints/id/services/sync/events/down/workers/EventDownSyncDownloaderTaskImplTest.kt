package com.simprints.id.services.sync.events.down.workers

import com.simprints.id.sampledata.DefaultTestConstants.GUID1
import com.simprints.id.sampledata.DefaultTestConstants.projectDownSyncScope
import com.simprints.id.services.sync.events.common.WorkerProgressCountReporter
import com.simprints.id.services.sync.events.down.EventDownSyncHelper
import com.simprints.id.services.sync.events.down.EventDownSyncProgress
import com.simprints.id.services.sync.events.master.internal.EventSyncCache
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class EventDownSyncDownloaderTaskImplTest {

    private lateinit var eventDownSyncDownloaderTask: EventDownSyncDownloaderTask
    private lateinit var syncHelper: EventDownSyncHelper
    private lateinit var downloadProgressChannel: Channel<EventDownSyncProgress>

    private val op = projectDownSyncScope.operations.first()

    @Before
    fun setUp() {
        eventDownSyncDownloaderTask = EventDownSyncDownloaderTaskImpl()
        syncHelper = mockk()

        runBlocking {
            mockProgressEmission(emptyList())
        }
    }

    @Test
    fun task_shouldResumeTheProgress() {
        runBlocking {
            val syncCache = mockk<EventSyncCache>()
            eventDownSyncDownloaderTask.execute(GUID1, op, syncHelper, syncCache, mockk(), this)
            verify { syncCache.readProgress(GUID1) }
        }
    }

    @Test
    fun task_shouldUseTheDownSyncHelperChannel() {
        runBlocking {
            eventDownSyncDownloaderTask.execute(GUID1, op, syncHelper, mockk(), mockk(), this)
            coVerify { syncHelper.downSync(this@runBlocking, op) }
        }
    }

    @Test
    fun task_forEveryDownloadedEventShouldUpdateTheProgress() {
        runBlocking {
            mockProgressEmission(listOf(EventDownSyncProgress(op, 1)))

            val reporter = mockk<WorkerProgressCountReporter>()
            eventDownSyncDownloaderTask.execute(GUID1, op, syncHelper, mockk(), reporter, this)

            coVerify { reporter.reportCount(1) }
        }
    }

    @Test
    fun task_forEveryDownloadedEventShouldStoreTheProgress() {
        runBlocking {
            mockProgressEmission(listOf(EventDownSyncProgress(op, 1)))

            val cache = mockk<EventSyncCache>()
            eventDownSyncDownloaderTask.execute(GUID1, op, syncHelper, cache, mockk(), this)

            coVerify { cache.saveProgress(GUID1, 1) }
        }
    }

    private suspend fun mockProgressEmission(progressEvents: List<EventDownSyncProgress>) {
        downloadProgressChannel = Channel(capacity = Channel.UNLIMITED)
        coEvery { syncHelper.downSync(any(), any()) } returns downloadProgressChannel

        progressEvents.forEach {
            downloadProgressChannel.send(it)
        }
        downloadProgressChannel.close()
    }
}
