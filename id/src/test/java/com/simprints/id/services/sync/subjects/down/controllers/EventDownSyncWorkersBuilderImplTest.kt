package com.simprints.id.services.sync.subjects.down.controllers

import androidx.work.WorkRequest
import com.google.common.truth.Truth.assertThat
import com.simprints.id.commontesttools.DefaultTestConstants.modulesDownSyncScope
import com.simprints.id.commontesttools.DefaultTestConstants.projectDownSyncScope
import com.simprints.id.commontesttools.DefaultTestConstants.userDownSyncScope
import com.simprints.id.data.db.events_sync.down.EventDownSyncScopeRepository
import com.simprints.id.domain.modality.Modes
import com.simprints.id.services.sync.events.common.*
import com.simprints.id.services.sync.events.down.EventDownSyncWorkersBuilder
import com.simprints.id.services.sync.events.down.EventDownSyncWorkersBuilderImpl
import com.simprints.id.services.sync.events.down.workers.EventDownSyncCountWorker
import com.simprints.id.services.sync.events.down.workers.EventDownSyncDownloaderWorker
import com.simprints.id.services.sync.events.master.models.EventSyncWorkerType.Companion.tagForType
import com.simprints.id.services.sync.events.master.models.EventSyncWorkerType.DOWNLOADER
import com.simprints.id.services.sync.events.master.models.EventSyncWorkerType.DOWN_COUNTER
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class EventDownSyncWorkersBuilderImplTest {

    private val modes = listOf(Modes.FINGERPRINT, Modes.FACE)
    private lateinit var eventDownSyncWorkersFactory: EventDownSyncWorkersBuilder
    private lateinit var eventDownSyncScopeRepository: EventDownSyncScopeRepository

    @Before
    fun setUp() {
        eventDownSyncScopeRepository = mockk(relaxed = true)
        eventDownSyncWorkersFactory = EventDownSyncWorkersBuilderImpl(eventDownSyncScopeRepository, mockk())
    }

    @Test
    fun builder_forProjectDownSync_shouldReturnTheRightWorkers() = runBlocking {
        coEvery { eventDownSyncScopeRepository.getDownSyncScope() } returns projectDownSyncScope

        val chain = eventDownSyncWorkersFactory.buildDownSyncWorkerChain("")
        chain.assertNumberOfDownSyncDownloaderWorker(1)
        chain.assertSubjectsDownSyncCountWorkerTagsForPeriodic(1)
        assertThat(chain.size).isEqualTo(2)
    }

    @Test
    fun builder_forUserDownSync_shouldReturnTheRightWorkers() = runBlocking {
        coEvery { eventDownSyncScopeRepository.getDownSyncScope() } returns userDownSyncScope

        val chain = eventDownSyncWorkersFactory.buildDownSyncWorkerChain("")
        chain.assertNumberOfDownSyncDownloaderWorker(1)
        chain.assertSubjectsDownSyncCountWorkerTagsForPeriodic(1)
        assertThat(chain.size).isEqualTo(2)
    }

    @Test
    fun builder_forModuleDownSync_shouldReturnTheRightWorkers() = runBlocking {
        coEvery { eventDownSyncScopeRepository.getDownSyncScope() } returns modulesDownSyncScope

        val chain = eventDownSyncWorkersFactory.buildDownSyncWorkerChain("")
        chain.assertNumberOfDownSyncDownloaderWorker(2)
        chain.assertSubjectsDownSyncCountWorkerTagsForPeriodic(1)
        assertThat(chain.size).isEqualTo(3)
    }

    @Test
    fun builder_periodicDownSyncWorkers_shouldHaveTheRightTags() = runBlocking {
        coEvery { eventDownSyncScopeRepository.getDownSyncScope() } returns projectDownSyncScope
        val uniqueSyncId = "uniqueSyncId"
        val chain = eventDownSyncWorkersFactory.buildDownSyncWorkerChain(uniqueSyncId)
        chain.assertNumberOfDownSyncDownloaderWorker(1)
        chain.assertSubjectsDownSyncCountWorkerTagsForPeriodic(1)
        chain.first { it.tags.contains(EventDownSyncDownloaderWorker::class.qualifiedName) }.assertSubjectsDownSyncDownloaderWorkerTagsForPeriodic()
        chain.first { it.tags.contains(EventDownSyncCountWorker::class.qualifiedName) }.assertSubjectsDownSyncCountWorkerTagsForPeriodic()
    }

    @Test
    fun builder_oneTimeDownSyncWorkers_shouldHaveTheRightTags() = runBlocking {
        coEvery { eventDownSyncScopeRepository.getDownSyncScope() } returns projectDownSyncScope

        val chain = eventDownSyncWorkersFactory.buildDownSyncWorkerChain(null)
        chain.assertNumberOfDownSyncDownloaderWorker(1)
        chain.assertSubjectsDownSyncCountWorkerTagsForPeriodic(1)
        chain.first { it.tags.contains(EventDownSyncDownloaderWorker::class.qualifiedName) }.assertSubjectsDownSyncDownloaderWorkerTagsForOneTime()
        chain.first { it.tags.contains(EventDownSyncCountWorker::class.qualifiedName) }.assertSubjectsDownSyncCountWorkerTagsForOneTime()
    }
}

private fun WorkRequest.assertSubjectsDownSyncDownloaderWorkerTagsForPeriodic() {
    assertThat(tags.size).isEqualTo(7)
    assertUniqueMasterIdTag()

    assertCommonDownSyncWorkersTags()
    assertCommonDownSyncDownloadersWorkersTag()
}

private fun WorkRequest.assertSubjectsDownSyncCountWorkerTagsForPeriodic() {
    assertThat(tags.size).isEqualTo(7)
    assertUniqueMasterIdTag()

    assertCommonDownSyncWorkersTags()
    assertCommonDownSyncCounterWorkersTag()
}

private fun WorkRequest.assertSubjectsDownSyncDownloaderWorkerTagsForOneTime() {
    assertThat(tags.size).isEqualTo(6)
    assertCommonDownSyncWorkersTags()
    assertCommonDownSyncDownloadersWorkersTag()
}

private fun WorkRequest.assertSubjectsDownSyncCountWorkerTagsForOneTime() {
    assertThat(tags.size).isEqualTo(6)
    assertCommonDownSyncWorkersTags()
    assertCommonDownSyncCounterWorkersTag()
}

private fun WorkRequest.assertCommonDownSyncWorkersTags() {
    assertUniqueDownSyncMasterTag()
    assertScheduleAtTag()
    assertCommonDownSyncTag()
    assertCommonSyncTag()
}

private fun WorkRequest.assertCommonDownSyncDownloadersWorkersTag() =
    assertThat(tags).contains(tagForType(DOWNLOADER))

private fun WorkRequest.assertCommonDownSyncCounterWorkersTag() =
    assertThat(tags).contains(tagForType(DOWN_COUNTER))

private fun WorkRequest.assertUniqueMasterIdTag() =
    assertThat(tags.firstOrNull { it.contains(TAG_MASTER_SYNC_ID) }).isNotNull()

private fun WorkRequest.assertCommonSyncTag() =
    assertThat(tags).contains(TAG_SUBJECTS_SYNC_ALL_WORKERS)

private fun WorkRequest.assertCommonDownSyncTag() =
    assertThat(tags).contains(TAG_SUBJECTS_DOWN_SYNC_ALL_WORKERS)

private fun WorkRequest.assertScheduleAtTag() =
    assertThat(tags.firstOrNull { it.contains(TAG_SCHEDULED_AT) }).isNotNull()

private fun WorkRequest.assertUniqueDownSyncMasterTag() =
    assertThat(tags.firstOrNull { it.contains(TAG_DOWN_MASTER_SYNC_ID) }).isNotNull()

private fun List<WorkRequest>.assertNumberOfDownSyncDownloaderWorker(count: Int) =
    assertThat(count { it.tags.contains(EventDownSyncDownloaderWorker::class.qualifiedName) }).isEqualTo(count)

private fun List<WorkRequest>.assertSubjectsDownSyncCountWorkerTagsForPeriodic(count: Int) =
    assertThat(count { it.tags.contains(EventDownSyncCountWorker::class.qualifiedName) }).isEqualTo(count)
