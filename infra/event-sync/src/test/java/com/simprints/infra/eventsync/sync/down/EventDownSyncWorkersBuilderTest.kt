package com.simprints.infra.eventsync.sync.down

import androidx.work.WorkRequest
import androidx.work.workDataOf
import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.common.Partitioning
import com.simprints.core.domain.modality.Modes
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.core.domain.tokenization.values
import com.simprints.core.tools.json.JsonHelper
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.DeviceConfiguration
import com.simprints.infra.config.store.models.DownSynchronizationConfiguration
import com.simprints.infra.config.store.models.GeneralConfiguration
import com.simprints.infra.eventsync.SampleSyncScopes
import com.simprints.infra.eventsync.status.down.EventDownSyncScopeRepository
import com.simprints.infra.eventsync.status.down.domain.EventDownSyncScope
import com.simprints.infra.eventsync.status.models.EventSyncWorkerType
import com.simprints.infra.eventsync.sync.common.*
import com.simprints.infra.eventsync.sync.down.workers.EventDownSyncCountWorker
import com.simprints.infra.eventsync.sync.down.workers.EventDownSyncCountWorker.Companion.INPUT_COUNT_WORKER_DOWN
import com.simprints.infra.eventsync.sync.down.workers.EventDownSyncDownloaderWorker
import com.simprints.infra.eventsync.sync.down.workers.EventDownSyncDownloaderWorker.Companion.INPUT_DOWN_SYNC_OPS
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class EventDownSyncWorkersBuilderTest {

    companion object {
        private val SELECTED_MODULE = listOf("MODULE_1".asTokenizableEncrypted())
    }

    @MockK
    private lateinit var generalConfiguration: GeneralConfiguration

    @MockK
    private lateinit var downSyncConfiguration: DownSynchronizationConfiguration

    @MockK
    private lateinit var configRepository: ConfigRepository

    @MockK
    private lateinit var eventDownSyncScopeRepository: EventDownSyncScopeRepository

    private lateinit var eventDownSyncWorkersBuilder: EventDownSyncWorkersBuilder

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        coEvery { configRepository.getDeviceConfiguration() } returns DeviceConfiguration(
            "",
            SELECTED_MODULE,
            ""
        )
        coEvery { configRepository.getConfiguration() } returns mockk {
            every { general } returns generalConfiguration
            every { synchronization } returns mockk {
                every { down } returns downSyncConfiguration
            }
        }

        eventDownSyncWorkersBuilder = EventDownSyncWorkersBuilder(
            eventDownSyncScopeRepository,
            JsonHelper,
            configRepository
        )
    }

    @Test
    fun builder_forProjectDownSync_shouldReturnTheRightWorkers() = runTest {
        every { generalConfiguration.modalities } returns listOf(GeneralConfiguration.Modality.FINGERPRINT)
        every { downSyncConfiguration.partitionType } returns DownSynchronizationConfiguration.PartitionType.PROJECT
        coEvery {
            eventDownSyncScopeRepository.getDownSyncScope(
                modes = listOf(Modes.FINGERPRINT),
                selectedModuleIDs = SELECTED_MODULE.values(),
                syncPartitioning = Partitioning.GLOBAL,
            )
        } returns SampleSyncScopes.projectDownSyncScope

        val chain = eventDownSyncWorkersBuilder.buildDownSyncWorkerChain("")

        chain.assertNumberOfDownSyncDownloaderWorkers(1)
        chain.assertNumberOfDownSyncCountWorkers(1)
        chain.assertDownSyncDownloaderWorkerInput(SampleSyncScopes.projectDownSyncScope)
        chain.assertDownSyncCountWorkerInput(SampleSyncScopes.projectDownSyncScope)
        assertThat(chain.size).isEqualTo(2)
    }

    @Test
    fun builder_forUserDownSync_shouldReturnTheRightWorkers() = runTest {
        every { generalConfiguration.modalities } returns listOf(GeneralConfiguration.Modality.FINGERPRINT)
        every { downSyncConfiguration.partitionType } returns DownSynchronizationConfiguration.PartitionType.USER
        coEvery {
            eventDownSyncScopeRepository.getDownSyncScope(
                modes = listOf(Modes.FINGERPRINT),
                selectedModuleIDs = SELECTED_MODULE.values(),
                syncPartitioning = Partitioning.USER
            )
        } returns SampleSyncScopes.userDownSyncScope

        val chain = eventDownSyncWorkersBuilder.buildDownSyncWorkerChain("")
        chain.assertNumberOfDownSyncDownloaderWorkers(1)
        chain.assertNumberOfDownSyncCountWorkers(1)
        chain.assertDownSyncDownloaderWorkerInput(SampleSyncScopes.userDownSyncScope)
        chain.assertDownSyncCountWorkerInput(SampleSyncScopes.userDownSyncScope)
        assertThat(chain.size).isEqualTo(2)
    }

    @Test
    fun builder_forModuleDownSync_shouldReturnTheRightWorkers() = runTest {
        every { generalConfiguration.modalities } returns listOf(GeneralConfiguration.Modality.FINGERPRINT)
        every { downSyncConfiguration.partitionType } returns DownSynchronizationConfiguration.PartitionType.MODULE
        coEvery {
            eventDownSyncScopeRepository.getDownSyncScope(
                modes = listOf(Modes.FINGERPRINT),
                selectedModuleIDs = SELECTED_MODULE.values(),
                syncPartitioning = Partitioning.MODULE
            )
        } returns SampleSyncScopes.modulesDownSyncScope

        val chain = eventDownSyncWorkersBuilder.buildDownSyncWorkerChain("")
        chain.assertNumberOfDownSyncDownloaderWorkers(2)
        chain.assertNumberOfDownSyncCountWorkers(1)
        chain.assertDownSyncDownloaderWorkerInput(SampleSyncScopes.modulesDownSyncScope)
        chain.assertDownSyncCountWorkerInput(SampleSyncScopes.modulesDownSyncScope)
        assertThat(chain.size).isEqualTo(3)
    }

    @Test
    fun builder_periodicDownSyncWorkers_shouldHaveTheRightTags() = runTest {
        every { generalConfiguration.modalities } returns listOf(GeneralConfiguration.Modality.FACE)
        every { downSyncConfiguration.partitionType } returns DownSynchronizationConfiguration.PartitionType.PROJECT
        coEvery {
            eventDownSyncScopeRepository.getDownSyncScope(
                modes = listOf(Modes.FACE),
                selectedModuleIDs = SELECTED_MODULE.values(),
                syncPartitioning = Partitioning.GLOBAL
            )
        } returns SampleSyncScopes.projectDownSyncScope
        val uniqueSyncId = "uniqueSyncId"
        val chain = eventDownSyncWorkersBuilder.buildDownSyncWorkerChain(uniqueSyncId)
        chain.assertNumberOfDownSyncDownloaderWorkers(1)
        chain.assertNumberOfDownSyncCountWorkers(1)
        chain.first { it.tags.contains(EventDownSyncDownloaderWorker::class.qualifiedName) }
            .assertSubjectsDownSyncDownloaderWorkerTagsForPeriodic()
        chain.first { it.tags.contains(EventDownSyncCountWorker::class.qualifiedName) }
            .assertSubjectsDownSyncCountWorkerTagsForPeriodic()
    }

    @Test
    fun builder_oneTimeDownSyncWorkers_shouldHaveTheRightTags() = runTest {
        every { generalConfiguration.modalities } returns listOf(GeneralConfiguration.Modality.FINGERPRINT)
        every { downSyncConfiguration.partitionType } returns DownSynchronizationConfiguration.PartitionType.PROJECT
        coEvery {
            eventDownSyncScopeRepository.getDownSyncScope(
                modes = listOf(Modes.FINGERPRINT),
                selectedModuleIDs = SELECTED_MODULE.values(),
                syncPartitioning = Partitioning.GLOBAL
            )
        } returns SampleSyncScopes.projectDownSyncScope

        val chain = eventDownSyncWorkersBuilder.buildDownSyncWorkerChain(null)
        chain.assertNumberOfDownSyncDownloaderWorkers(1)
        chain.assertNumberOfDownSyncCountWorkers(1)
        chain.first { it.tags.contains(EventDownSyncDownloaderWorker::class.qualifiedName) }
            .assertSubjectsDownSyncDownloaderWorkerTagsForOneTime()
        chain.first { it.tags.contains(EventDownSyncCountWorker::class.qualifiedName) }
            .assertSubjectsDownSyncCountWorkerTagsForOneTime()
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
        assertThat(tags).contains(EventSyncWorkerType.tagForType(EventSyncWorkerType.DOWNLOADER))

    private fun WorkRequest.assertCommonDownSyncCounterWorkersTag() =
        assertThat(tags).contains(EventSyncWorkerType.tagForType(EventSyncWorkerType.DOWN_COUNTER))

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

    private fun List<WorkRequest>.assertNumberOfDownSyncDownloaderWorkers(count: Int) =
        assertThat(count { it.tags.contains(EventDownSyncDownloaderWorker::class.qualifiedName) }).isEqualTo(
            count
        )

    private fun List<WorkRequest>.assertNumberOfDownSyncCountWorkers(count: Int) =
        assertThat(count { it.tags.contains(EventDownSyncCountWorker::class.qualifiedName) }).isEqualTo(
            count
        )

    private fun List<WorkRequest>.assertDownSyncDownloaderWorkerInput(downSyncScope: EventDownSyncScope) {
        val downloaders =
            filter { it.tags.contains(EventDownSyncDownloaderWorker::class.qualifiedName) }
        val jsonHelper = JsonHelper
        val ops = downSyncScope.operations
        ops.forEach { op ->
            assertThat(downloaders.any {
                it.workSpec.input == workDataOf(INPUT_DOWN_SYNC_OPS to jsonHelper.toJson(op))
            }).isTrue()
        }
        assertThat(downloaders).hasSize(ops.size)
    }

    private fun List<WorkRequest>.assertDownSyncCountWorkerInput(downSyncScope: EventDownSyncScope) {
        val counter = first { it.tags.contains(EventDownSyncCountWorker::class.qualifiedName) }
        val jsonHelper = JsonHelper
        assertThat(
            counter.workSpec.input == workDataOf(
                INPUT_COUNT_WORKER_DOWN to jsonHelper.toJson(downSyncScope)
            )
        ).isTrue()
    }
}
