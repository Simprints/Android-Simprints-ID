package com.simprints.infra.eventsync.sync.up

import androidx.work.WorkRequest
import androidx.work.workDataOf
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.json.JsonHelper
import com.simprints.infra.config.store.models.DownSynchronizationConfiguration
import com.simprints.infra.config.store.models.GeneralConfiguration
import com.simprints.infra.eventsync.SampleSyncScopes
import com.simprints.infra.eventsync.status.up.EventUpSyncScopeRepository
import com.simprints.infra.eventsync.status.up.domain.EventUpSyncScope
import com.simprints.infra.eventsync.sync.common.TAG_MASTER_SYNC_ID
import com.simprints.infra.eventsync.sync.common.TAG_SCHEDULED_AT
import com.simprints.infra.eventsync.sync.common.TAG_SUBJECTS_SYNC_ALL_WORKERS
import com.simprints.infra.eventsync.sync.common.TAG_SUBJECTS_UP_SYNC_ALL_WORKERS
import com.simprints.infra.eventsync.sync.common.TAG_UP_MASTER_SYNC_ID
import com.simprints.infra.eventsync.sync.up.workers.EventUpSyncUploaderWorker
import com.simprints.infra.eventsync.sync.up.workers.EventUpSyncUploaderWorker.Companion.INPUT_EVENT_UP_SYNC_SCOPE_ID
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

        val chain = eventUpSyncWorkersBuilder.buildUpSyncWorkerChain("uniqueSyncId", "scopeId")

        chain.assertNumberOfUpSyncUploaderWorkers(1)
        chain.assertUpSyncUploaderWorkerInput(SampleSyncScopes.projectUpSyncScope)
        assertThat(chain.size).isEqualTo(1)
    }

    @Test
    fun builder_periodicUpSyncWorkers_shouldHaveTheRightTags() = runTest {
        every { generalConfiguration.modalities } returns listOf(GeneralConfiguration.Modality.FACE)
        every { downSyncConfiguration.partitionType } returns DownSynchronizationConfiguration.PartitionType.PROJECT
        coEvery {
            eventUpSyncScopeRepository.getUpSyncScope()
        } returns SampleSyncScopes.projectUpSyncScope

        val chain = eventUpSyncWorkersBuilder.buildUpSyncWorkerChain("uniqueSyncId", "scopeId")

        chain.assertNumberOfUpSyncUploaderWorkers(1)
        chain
            .first { it.tags.contains(EventUpSyncUploaderWorker::class.qualifiedName) }
            .assertSubjectsDownSyncDownloaderWorkerTagsForPeriodic()
    }

    @Test
    fun builder_oneTimeDownSyncWorkers_shouldHaveTheRightTags() = runTest {
        every { generalConfiguration.modalities } returns listOf(GeneralConfiguration.Modality.FINGERPRINT)
        every { downSyncConfiguration.partitionType } returns DownSynchronizationConfiguration.PartitionType.PROJECT
        coEvery {
            eventUpSyncScopeRepository.getUpSyncScope()
        } returns SampleSyncScopes.projectUpSyncScope

        val chain = eventUpSyncWorkersBuilder.buildUpSyncWorkerChain("uniqueSyncId", "scopeId")
        chain.assertNumberOfUpSyncUploaderWorkers(1)
        chain
            .first { it.tags.contains(EventUpSyncUploaderWorker::class.qualifiedName) }
            .assertSubjectsUpSyncDownloaderWorkerTagsForOneTime()
    }

    private fun WorkRequest.assertSubjectsDownSyncDownloaderWorkerTagsForPeriodic() {
        assertThat(tags.size).isEqualTo(7)
        assertUniqueMasterIdTag()

        assertCommonUpSyncWorkersTags()
    }

    private fun WorkRequest.assertSubjectsUpSyncDownloaderWorkerTagsForOneTime() {
        assertThat(tags.size).isEqualTo(7)
        assertCommonUpSyncWorkersTags()
    }

    private fun WorkRequest.assertCommonUpSyncWorkersTags() {
        assertUniqueUpSyncMasterTag()
        assertScheduleAtTag()
        assertCommonUpSyncTag()
        assertCommonSyncTag()
    }

    private fun WorkRequest.assertUniqueMasterIdTag() = assertThat(tags.firstOrNull { it.contains(TAG_MASTER_SYNC_ID) }).isNotNull()

    private fun WorkRequest.assertCommonSyncTag() = assertThat(tags).contains(TAG_SUBJECTS_SYNC_ALL_WORKERS)

    private fun WorkRequest.assertCommonUpSyncTag() = assertThat(tags).contains(TAG_SUBJECTS_UP_SYNC_ALL_WORKERS)

    private fun WorkRequest.assertScheduleAtTag() = assertThat(tags.firstOrNull { it.contains(TAG_SCHEDULED_AT) }).isNotNull()

    private fun WorkRequest.assertUniqueUpSyncMasterTag() = assertThat(tags.firstOrNull { it.contains(TAG_UP_MASTER_SYNC_ID) }).isNotNull()

    private fun List<WorkRequest>.assertNumberOfUpSyncUploaderWorkers(count: Int) =
        assertThat(count { it.tags.contains(EventUpSyncUploaderWorker::class.qualifiedName) }).isEqualTo(count)

    private fun List<WorkRequest>.assertUpSyncUploaderWorkerInput(upSyncScope: EventUpSyncScope) {
        val uploaders = filter { it.tags.contains(EventUpSyncUploaderWorker::class.qualifiedName) }
        val jsonHelper = JsonHelper

        assertThat(
            uploaders.any {
                it.workSpec.input == workDataOf(
                    INPUT_UP_SYNC to jsonHelper.toJson(upSyncScope),
                    INPUT_EVENT_UP_SYNC_SCOPE_ID to "scopeId",
                )
            },
        ).isTrue()

        assertThat(uploaders).hasSize(1)
    }
}
