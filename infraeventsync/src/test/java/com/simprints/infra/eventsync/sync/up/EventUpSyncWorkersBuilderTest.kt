package com.simprints.infra.eventsync.sync.up

import androidx.work.WorkRequest
import androidx.work.workDataOf
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.json.JsonHelper
import com.simprints.infra.config.domain.models.DownSynchronizationConfiguration
import com.simprints.infra.config.domain.models.GeneralConfiguration
import com.simprints.infra.eventsync.SampleSyncScopes
import com.simprints.infra.eventsync.status.models.EventSyncWorkerType
import com.simprints.infra.eventsync.status.up.EventUpSyncScopeRepository
import com.simprints.infra.eventsync.status.up.domain.EventUpSyncScope
import com.simprints.infra.eventsync.sync.common.*
import com.simprints.infra.eventsync.sync.down.workers.EventDownSyncCountWorker
import com.simprints.infra.eventsync.sync.down.workers.EventDownSyncDownloaderWorker
import com.simprints.infra.eventsync.sync.up.workers.EventUpSyncCountWorker
import com.simprints.infra.eventsync.sync.up.workers.EventUpSyncCountWorker.Companion.INPUT_COUNT_WORKER_UP
import com.simprints.infra.eventsync.sync.up.workers.EventUpSyncUploaderWorker
import com.simprints.infra.eventsync.sync.up.workers.EventUpSyncUploaderWorker.Companion.INPUT_UP_SYNC
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class EventUpSyncWorkersBuilderTest {

    @MockK
    private lateinit var generalConfiguration: GeneralConfiguration

    @MockK
    private lateinit var downSyncConfiguration: DownSynchronizationConfiguration

    @MockK
    private lateinit var eventUpSyncScopeRepository: EventUpSyncScopeRepository


    private lateinit var eventUpSyncWorkersBuilder: EventUpSyncWorkersBuilder

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        eventUpSyncWorkersBuilder = EventUpSyncWorkersBuilder(eventUpSyncScopeRepository, JsonHelper)
    }

    @Test
    fun builder_forProjectUpSync_shouldReturnTheRightWorkers() = runTest {
        every { generalConfiguration.modalities } returns listOf(GeneralConfiguration.Modality.FINGERPRINT)
        every { downSyncConfiguration.partitionType } returns DownSynchronizationConfiguration.PartitionType.PROJECT
        coEvery {
            eventUpSyncScopeRepository.getUpSyncScope()
        } returns SampleSyncScopes.projectUpSyncScope

        val chain = eventUpSyncWorkersBuilder.buildUpSyncWorkerChain("")

        chain.assertNumberOfUpSyncUploaderWorkers(1)
        chain.assertNumberOfUpSyncCountWorkers(1)
        chain.assertUpSyncUploaderWorkerInput(SampleSyncScopes.projectUpSyncScope)
        chain.assertUpSyncCountWorkerInput(SampleSyncScopes.projectUpSyncScope)
        assertThat(chain.size).isEqualTo(2)
    }

    @Test
    fun builder_periodicUpSyncWorkers_shouldHaveTheRightTags() = runTest {
        every { generalConfiguration.modalities } returns listOf(GeneralConfiguration.Modality.FACE)
        every { downSyncConfiguration.partitionType } returns DownSynchronizationConfiguration.PartitionType.PROJECT
        coEvery {
            eventUpSyncScopeRepository.getUpSyncScope()
        } returns SampleSyncScopes.projectUpSyncScope

        val uniqueSyncId = "uniqueSyncId"
        val chain = eventUpSyncWorkersBuilder.buildUpSyncWorkerChain(uniqueSyncId)

        chain.assertNumberOfUpSyncUploaderWorkers(1)
        chain.assertNumberOfUpSyncCountWorkers(1)
        chain.first { it.tags.contains(EventUpSyncUploaderWorker::class.qualifiedName) }
            .assertSubjectsDownSyncDownloaderWorkerTagsForPeriodic()
        chain.first { it.tags.contains(EventUpSyncCountWorker::class.qualifiedName) }
            .assertSubjectsDownSyncCountWorkerTagsForPeriodic()
    }

    @Test
    fun builder_oneTimeDownSyncWorkers_shouldHaveTheRightTags() = runTest {
        every { generalConfiguration.modalities } returns listOf(GeneralConfiguration.Modality.FINGERPRINT)
        every { downSyncConfiguration.partitionType } returns DownSynchronizationConfiguration.PartitionType.PROJECT
        coEvery {
            eventUpSyncScopeRepository.getUpSyncScope()
        } returns SampleSyncScopes.projectUpSyncScope

        val chain = eventUpSyncWorkersBuilder.buildUpSyncWorkerChain(null)
        chain.assertNumberOfUpSyncUploaderWorkers(1)
        chain.assertNumberOfUpSyncCountWorkers(1)
        chain.first { it.tags.contains(EventUpSyncUploaderWorker::class.qualifiedName) }
            .assertSubjectsUpSyncDownloaderWorkerTagsForOneTime()
        chain.first { it.tags.contains(EventUpSyncCountWorker::class.qualifiedName) }
            .assertSubjectsUpSyncCountWorkerTagsForOneTime()
    }


    private fun WorkRequest.assertSubjectsDownSyncDownloaderWorkerTagsForPeriodic() {
        assertThat(tags.size).isEqualTo(7)
        assertUniqueMasterIdTag()

        assertCommonUpSyncWorkersTags()
        assertCommonUpSyncDownloadersWorkersTag()
    }

    private fun WorkRequest.assertSubjectsDownSyncCountWorkerTagsForPeriodic() {
        assertThat(tags.size).isEqualTo(7)
        assertUniqueMasterIdTag()

        assertCommonUpSyncWorkersTags()
        assertCommonUpSyncCounterWorkersTag()
    }

    private fun WorkRequest.assertSubjectsUpSyncDownloaderWorkerTagsForOneTime() {
        assertThat(tags.size).isEqualTo(6)
        assertCommonUpSyncWorkersTags()
        assertCommonUpSyncDownloadersWorkersTag()
    }

    private fun WorkRequest.assertSubjectsUpSyncCountWorkerTagsForOneTime() {
        assertThat(tags.size).isEqualTo(6)
        assertCommonUpSyncWorkersTags()
        assertCommonUpSyncCounterWorkersTag()
    }

    private fun WorkRequest.assertCommonUpSyncWorkersTags() {
        assertUniqueUpSyncMasterTag()
        assertScheduleAtTag()
        assertCommonUpSyncTag()
        assertCommonSyncTag()
    }

    private fun WorkRequest.assertCommonUpSyncDownloadersWorkersTag() =
        assertThat(tags).contains(EventSyncWorkerType.tagForType(EventSyncWorkerType.UPLOADER))

    private fun WorkRequest.assertCommonUpSyncCounterWorkersTag() =
        assertThat(tags).contains(EventSyncWorkerType.tagForType(EventSyncWorkerType.UP_COUNTER))

    private fun WorkRequest.assertUniqueMasterIdTag() =
        assertThat(tags.firstOrNull { it.contains(TAG_MASTER_SYNC_ID) }).isNotNull()

    private fun WorkRequest.assertCommonSyncTag() =
        assertThat(tags).contains(TAG_SUBJECTS_SYNC_ALL_WORKERS)

    private fun WorkRequest.assertCommonUpSyncTag() =
        assertThat(tags).contains(TAG_SUBJECTS_UP_SYNC_ALL_WORKERS)

    private fun WorkRequest.assertScheduleAtTag() =
        assertThat(tags.firstOrNull { it.contains(TAG_SCHEDULED_AT) }).isNotNull()

    private fun WorkRequest.assertUniqueUpSyncMasterTag() =
        assertThat(tags.firstOrNull { it.contains(TAG_UP_MASTER_SYNC_ID) }).isNotNull()

    private fun List<WorkRequest>.assertNumberOfUpSyncUploaderWorkers(count: Int) =
        assertThat(count { it.tags.contains(EventUpSyncUploaderWorker::class.qualifiedName) }).isEqualTo(count)

    private fun List<WorkRequest>.assertNumberOfUpSyncCountWorkers(count: Int) =
        assertThat(count { it.tags.contains(EventUpSyncCountWorker::class.qualifiedName) }).isEqualTo(count)

    private fun List<WorkRequest>.assertUpSyncUploaderWorkerInput(upSyncScope: EventUpSyncScope) {
        val uploaders = filter { it.tags.contains(EventUpSyncUploaderWorker::class.qualifiedName) }
        val jsonHelper = JsonHelper

        assertThat(uploaders.any {
            it.workSpec.input == workDataOf(INPUT_UP_SYNC to jsonHelper.toJson(upSyncScope))
        }).isTrue()

        assertThat(uploaders).hasSize(1)
    }

    private fun List<WorkRequest>.assertUpSyncCountWorkerInput(upSyncScope: EventUpSyncScope) {
        val counter = first { it.tags.contains(EventUpSyncCountWorker::class.qualifiedName) }
        val jsonHelper = JsonHelper
        assertThat(
            counter.workSpec.input == workDataOf(INPUT_COUNT_WORKER_UP to jsonHelper.toJson(upSyncScope))
        ).isTrue()
    }
}
