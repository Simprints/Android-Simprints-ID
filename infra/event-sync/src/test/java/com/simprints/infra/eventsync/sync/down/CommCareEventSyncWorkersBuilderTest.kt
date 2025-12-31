package com.simprints.infra.eventsync.sync.down

import androidx.work.WorkRequest
import androidx.work.workDataOf
import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.common.Modality
import com.simprints.core.domain.common.Partitioning
import com.simprints.core.tools.json.JsonHelper
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.GeneralConfiguration
import com.simprints.infra.eventsync.SampleSyncScopes
import com.simprints.infra.eventsync.status.down.EventDownSyncScopeRepository
import com.simprints.infra.eventsync.status.down.domain.EventDownSyncScope
import com.simprints.infra.eventsync.status.models.EventSyncWorkerType
import com.simprints.infra.eventsync.sync.common.TAG_DOWN_MASTER_SYNC_ID
import com.simprints.infra.eventsync.sync.common.TAG_MASTER_SYNC_ID
import com.simprints.infra.eventsync.sync.common.TAG_SCHEDULED_AT
import com.simprints.infra.eventsync.sync.common.TAG_SUBJECTS_DOWN_SYNC_ALL_WORKERS
import com.simprints.infra.eventsync.sync.common.TAG_SUBJECTS_SYNC_ALL_WORKERS
import com.simprints.infra.eventsync.sync.down.workers.BaseEventDownSyncDownloaderWorker.Companion.INPUT_DOWN_SYNC_OPS
import com.simprints.infra.eventsync.sync.down.workers.BaseEventDownSyncDownloaderWorker.Companion.INPUT_EVENT_DOWN_SYNC_SCOPE_ID
import com.simprints.infra.eventsync.sync.down.workers.CommCareEventSyncDownloaderWorker
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class CommCareEventSyncWorkersBuilderTest {
    @MockK
    private lateinit var generalConfiguration: GeneralConfiguration

    @MockK
    private lateinit var configRepository: ConfigRepository

    @MockK
    private lateinit var eventDownSyncScopeRepository: EventDownSyncScopeRepository

    private lateinit var commCareEventSyncWorkersBuilder: CommCareEventSyncWorkersBuilder

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        coEvery { configRepository.getProjectConfiguration() } returns mockk {
            every { general } returns generalConfiguration
        }

        commCareEventSyncWorkersBuilder = CommCareEventSyncWorkersBuilder(
            eventDownSyncScopeRepository,
            JsonHelper,
            configRepository,
        )
    }

    @Test
    fun builder_forCommCareDownSyncWithFingerprintModality_shouldReturnTheRightWorkers() = runTest {
        every { generalConfiguration.modalities } returns listOf(Modality.FINGERPRINT)
        coEvery {
            eventDownSyncScopeRepository.getDownSyncScope(
                modes = listOf(Modality.FINGERPRINT),
                selectedModuleIDs = emptyList(),
                syncPartitioning = Partitioning.GLOBAL,
            )
        } returns SampleSyncScopes.projectDownSyncScope

        val chain = commCareEventSyncWorkersBuilder.buildDownSyncWorkerChain("", "scopeId")

        chain.assertNumberOfDownSyncDownloaderWorkers(1)
        chain.assertDownSyncDownloaderWorkerInput(SampleSyncScopes.projectDownSyncScope, "scopeId")
        assertThat(chain.size).isEqualTo(1)
    }

    @Test
    fun builder_forCommCareDownSyncWithFaceModality_shouldReturnTheRightWorkers() = runTest {
        every { generalConfiguration.modalities } returns listOf(Modality.FACE)
        coEvery {
            eventDownSyncScopeRepository.getDownSyncScope(
                modes = listOf(Modality.FACE),
                selectedModuleIDs = emptyList(),
                syncPartitioning = Partitioning.GLOBAL,
            )
        } returns SampleSyncScopes.projectDownSyncScope

        val chain = commCareEventSyncWorkersBuilder.buildDownSyncWorkerChain("", "scopeId")

        chain.assertNumberOfDownSyncDownloaderWorkers(1)
        chain.assertDownSyncDownloaderWorkerInput(SampleSyncScopes.projectDownSyncScope, "scopeId")
        assertThat(chain.size).isEqualTo(1)
    }

    @Test
    fun builder_periodicDownSyncWorkers_shouldHaveTheRightTags() = runTest {
        every { generalConfiguration.modalities } returns listOf(Modality.FACE)
        coEvery {
            eventDownSyncScopeRepository.getDownSyncScope(
                modes = listOf(Modality.FACE),
                selectedModuleIDs = emptyList(),
                syncPartitioning = Partitioning.GLOBAL,
            )
        } returns SampleSyncScopes.projectDownSyncScope
        val uniqueSyncId = "uniqueSyncId"
        val chain = commCareEventSyncWorkersBuilder.buildDownSyncWorkerChain(uniqueSyncId, "scopeId")
        chain.assertNumberOfDownSyncDownloaderWorkers(1)
        chain
            .first { it.tags.contains(CommCareEventSyncDownloaderWorker::class.qualifiedName) }
            .assertCommCareDownSyncDownloaderWorkerTagsForPeriodic()
    }

    @Test
    fun builder_oneTimeDownSyncWorkers_shouldHaveTheRightTags() = runTest {
        every { generalConfiguration.modalities } returns listOf(Modality.FINGERPRINT)
        coEvery {
            eventDownSyncScopeRepository.getDownSyncScope(
                modes = listOf(Modality.FINGERPRINT),
                selectedModuleIDs = emptyList(),
                syncPartitioning = Partitioning.GLOBAL,
            )
        } returns SampleSyncScopes.projectDownSyncScope

        val chain = commCareEventSyncWorkersBuilder.buildDownSyncWorkerChain("", "scopeId")
        chain.assertNumberOfDownSyncDownloaderWorkers(1)
        chain
            .first { it.tags.contains(CommCareEventSyncDownloaderWorker::class.qualifiedName) }
            .assertCommCareDownSyncDownloaderWorkerTagsForOneTime()
    }

    private fun WorkRequest.assertCommCareDownSyncDownloaderWorkerTagsForPeriodic() {
        assertThat(tags.size).isEqualTo(7)
        assertUniqueMasterIdTag()

        assertCommonDownSyncWorkersTags()
        assertCommonDownSyncDownloadersWorkersTag()
    }

    private fun WorkRequest.assertCommCareDownSyncDownloaderWorkerTagsForOneTime() {
        assertThat(tags.size).isEqualTo(7)
        assertCommonDownSyncWorkersTags()
        assertCommonDownSyncDownloadersWorkersTag()
    }

    private fun WorkRequest.assertCommonDownSyncWorkersTags() {
        assertUniqueDownSyncMasterTag()
        assertScheduleAtTag()
        assertCommonDownSyncTag()
        assertCommonSyncTag()
    }

    private fun WorkRequest.assertCommonDownSyncDownloadersWorkersTag() =
        assertThat(tags).contains(EventSyncWorkerType.tagForType(EventSyncWorkerType.DOWNLOADER))

    private fun WorkRequest.assertUniqueMasterIdTag() = assertThat(tags.firstOrNull { it.contains(TAG_MASTER_SYNC_ID) }).isNotNull()

    private fun WorkRequest.assertCommonSyncTag() = assertThat(tags).contains(TAG_SUBJECTS_SYNC_ALL_WORKERS)

    private fun WorkRequest.assertCommonDownSyncTag() = assertThat(tags).contains(TAG_SUBJECTS_DOWN_SYNC_ALL_WORKERS)

    private fun WorkRequest.assertScheduleAtTag() = assertThat(tags.firstOrNull { it.contains(TAG_SCHEDULED_AT) }).isNotNull()

    private fun WorkRequest.assertUniqueDownSyncMasterTag() =
        assertThat(tags.firstOrNull { it.contains(TAG_DOWN_MASTER_SYNC_ID) }).isNotNull()

    private fun List<WorkRequest>.assertNumberOfDownSyncDownloaderWorkers(count: Int) =
        assertThat(count { it.tags.contains(CommCareEventSyncDownloaderWorker::class.qualifiedName) }).isEqualTo(
            count,
        )

    private fun List<WorkRequest>.assertDownSyncDownloaderWorkerInput(
        downSyncScope: EventDownSyncScope,
        scopeId: String,
    ) {
        val downloaders =
            filter { it.tags.contains(CommCareEventSyncDownloaderWorker::class.qualifiedName) }
        val jsonHelper = JsonHelper
        val ops = downSyncScope.operations
        ops.forEach { op ->
            assertThat(
                downloaders.any {
                    it.workSpec.input == workDataOf(
                        INPUT_DOWN_SYNC_OPS to jsonHelper.json.encodeToString(op),
                        INPUT_EVENT_DOWN_SYNC_SCOPE_ID to scopeId,
                    )
                },
            ).isTrue()
        }
        assertThat(downloaders).hasSize(ops.size)
    }
}
