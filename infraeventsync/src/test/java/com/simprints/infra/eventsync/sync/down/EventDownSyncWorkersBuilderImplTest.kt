package com.simprints.infra.eventsync.sync.down

import androidx.work.WorkRequest
import androidx.work.workDataOf
import com.google.common.truth.Truth
import com.simprints.core.domain.common.GROUP
import com.simprints.core.domain.modality.Modes
import com.simprints.core.tools.json.JsonHelper
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.DeviceConfiguration
import com.simprints.infra.config.domain.models.DownSynchronizationConfiguration
import com.simprints.infra.config.domain.models.GeneralConfiguration
import com.simprints.infra.eventsync.SampleSyncScopes
import com.simprints.infra.eventsync.status.down.EventDownSyncScopeRepository
import com.simprints.infra.eventsync.status.down.domain.EventDownSyncScope
import com.simprints.infra.eventsync.status.models.EventSyncWorkerType
import com.simprints.infra.eventsync.sync.common.*
import com.simprints.infra.eventsync.sync.down.workers.EventDownSyncCountWorker
import com.simprints.infra.eventsync.sync.down.workers.EventDownSyncCountWorker.Companion.INPUT_COUNT_WORKER_DOWN
import com.simprints.infra.eventsync.sync.down.workers.EventDownSyncDownloaderWorker
import com.simprints.infra.eventsync.sync.down.workers.EventDownSyncDownloaderWorker.Companion.INPUT_DOWN_SYNC_OPS
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class EventDownSyncWorkersBuilderImplTest {

    companion object {
        private val SELECTED_MODULE = listOf("MODULE_1")
    }

    private var generalConfiguration = mockk<GeneralConfiguration>()
    private var downSyncConfiguration = mockk<DownSynchronizationConfiguration>()
    private val configManager = mockk<ConfigManager>()
    private val eventDownSyncScopeRepository = mockk<EventDownSyncScopeRepository>()
    private lateinit var eventDownSyncWorkersBuilder: EventDownSyncWorkersBuilder

    @Before
    fun setUp() {
        coEvery { configManager.getDeviceConfiguration() } returns DeviceConfiguration(
                "",
                SELECTED_MODULE,
                ""
        )
        coEvery { configManager.getProjectConfiguration() } returns mockk {
            every { general } returns generalConfiguration
            every { synchronization } returns mockk {
                every { down } returns downSyncConfiguration
            }
        }

        eventDownSyncWorkersBuilder = EventDownSyncWorkersBuilder(
                eventDownSyncScopeRepository,
                JsonHelper,
                configManager
        )
    }

    @Test
    fun builder_forProjectDownSync_shouldReturnTheRightWorkers() =
            runTest(UnconfinedTestDispatcher()) {
                every { generalConfiguration.modalities } returns listOf(GeneralConfiguration.Modality.FINGERPRINT)
                every { downSyncConfiguration.partitionType } returns DownSynchronizationConfiguration.PartitionType.PROJECT
                coEvery {
                    eventDownSyncScopeRepository.getDownSyncScope(
                            listOf(Modes.FINGERPRINT),
                            SELECTED_MODULE,
                            GROUP.GLOBAL,
                    )
                } returns SampleSyncScopes.projectDownSyncScope

                val chain = eventDownSyncWorkersBuilder.buildDownSyncWorkerChain("")

                chain.assertNumberOfDownSyncDownloaderWorkers(1)
                chain.assertNumberOfDownSyncCountWorkers(1)
                chain.assertDownSyncDownloaderWorkerInput(SampleSyncScopes.projectDownSyncScope)
                chain.assertDownSyncCountWorkerInput(SampleSyncScopes.projectDownSyncScope)
                Truth.assertThat(chain.size).isEqualTo(2)
            }

    @Test
    fun builder_forUserDownSync_shouldReturnTheRightWorkers() =
            runTest(UnconfinedTestDispatcher()) {
                every { generalConfiguration.modalities } returns listOf(GeneralConfiguration.Modality.FINGERPRINT)
                every { downSyncConfiguration.partitionType } returns DownSynchronizationConfiguration.PartitionType.USER
                coEvery {
                    eventDownSyncScopeRepository.getDownSyncScope(
                            listOf(Modes.FINGERPRINT),
                            SELECTED_MODULE,
                            GROUP.USER
                    )
                } returns SampleSyncScopes.userDownSyncScope

                val chain = eventDownSyncWorkersBuilder.buildDownSyncWorkerChain("")
                chain.assertNumberOfDownSyncDownloaderWorkers(1)
                chain.assertNumberOfDownSyncCountWorkers(1)
                chain.assertDownSyncDownloaderWorkerInput(SampleSyncScopes.userDownSyncScope)
                chain.assertDownSyncCountWorkerInput(SampleSyncScopes.userDownSyncScope)
                Truth.assertThat(chain.size).isEqualTo(2)
            }

    @Test
    fun builder_forModuleDownSync_shouldReturnTheRightWorkers() =
            runTest(UnconfinedTestDispatcher()) {
                every { generalConfiguration.modalities } returns listOf(GeneralConfiguration.Modality.FINGERPRINT)
                every { downSyncConfiguration.partitionType } returns DownSynchronizationConfiguration.PartitionType.MODULE
                coEvery {
                    eventDownSyncScopeRepository.getDownSyncScope(
                            listOf(Modes.FINGERPRINT),
                            SELECTED_MODULE,
                            GROUP.MODULE
                    )
                } returns SampleSyncScopes.modulesDownSyncScope

                val chain = eventDownSyncWorkersBuilder.buildDownSyncWorkerChain("")
                chain.assertNumberOfDownSyncDownloaderWorkers(2)
                chain.assertNumberOfDownSyncCountWorkers(1)
                chain.assertDownSyncDownloaderWorkerInput(SampleSyncScopes.modulesDownSyncScope)
                chain.assertDownSyncCountWorkerInput(SampleSyncScopes.modulesDownSyncScope)
                Truth.assertThat(chain.size).isEqualTo(3)
            }

    @Test
    fun builder_periodicDownSyncWorkers_shouldHaveTheRightTags() =
            runTest(UnconfinedTestDispatcher()) {
                every { generalConfiguration.modalities } returns listOf(GeneralConfiguration.Modality.FACE)
                every { downSyncConfiguration.partitionType } returns DownSynchronizationConfiguration.PartitionType.PROJECT
                coEvery {
                    eventDownSyncScopeRepository.getDownSyncScope(
                            listOf(Modes.FACE),
                            SELECTED_MODULE,
                            GROUP.GLOBAL
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
    fun builder_oneTimeDownSyncWorkers_shouldHaveTheRightTags() =
            runTest(UnconfinedTestDispatcher()) {
                every { generalConfiguration.modalities } returns listOf(GeneralConfiguration.Modality.FINGERPRINT)
                every { downSyncConfiguration.partitionType } returns DownSynchronizationConfiguration.PartitionType.PROJECT
                coEvery {
                    eventDownSyncScopeRepository.getDownSyncScope(
                            listOf(Modes.FINGERPRINT),
                            SELECTED_MODULE,
                            GROUP.GLOBAL
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
        Truth.assertThat(tags.size).isEqualTo(7)
        assertUniqueMasterIdTag()

        assertCommonDownSyncWorkersTags()
        assertCommonDownSyncDownloadersWorkersTag()
    }

    private fun WorkRequest.assertSubjectsDownSyncCountWorkerTagsForPeriodic() {
        Truth.assertThat(tags.size).isEqualTo(7)
        assertUniqueMasterIdTag()

        assertCommonDownSyncWorkersTags()
        assertCommonDownSyncCounterWorkersTag()
    }

    private fun WorkRequest.assertSubjectsDownSyncDownloaderWorkerTagsForOneTime() {
        Truth.assertThat(tags.size).isEqualTo(6)
        assertCommonDownSyncWorkersTags()
        assertCommonDownSyncDownloadersWorkersTag()
    }

    private fun WorkRequest.assertSubjectsDownSyncCountWorkerTagsForOneTime() {
        Truth.assertThat(tags.size).isEqualTo(6)
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
        Truth.assertThat(tags).contains(EventSyncWorkerType.tagForType(EventSyncWorkerType.DOWNLOADER))

    private fun WorkRequest.assertCommonDownSyncCounterWorkersTag() =
        Truth.assertThat(tags).contains(EventSyncWorkerType.tagForType(EventSyncWorkerType.DOWN_COUNTER))

    private fun WorkRequest.assertUniqueMasterIdTag() =
        Truth.assertThat(tags.firstOrNull { it.contains(TAG_MASTER_SYNC_ID) }).isNotNull()

    private fun WorkRequest.assertCommonSyncTag() =
        Truth.assertThat(tags).contains(TAG_SUBJECTS_SYNC_ALL_WORKERS)

    private fun WorkRequest.assertCommonDownSyncTag() =
        Truth.assertThat(tags).contains(TAG_SUBJECTS_DOWN_SYNC_ALL_WORKERS)

    private fun WorkRequest.assertScheduleAtTag() =
        Truth.assertThat(tags.firstOrNull { it.contains(TAG_SCHEDULED_AT) }).isNotNull()

    private fun WorkRequest.assertUniqueDownSyncMasterTag() =
        Truth.assertThat(tags.firstOrNull { it.contains(TAG_DOWN_MASTER_SYNC_ID) }).isNotNull()

    private fun List<WorkRequest>.assertNumberOfDownSyncDownloaderWorkers(count: Int) =
        Truth.assertThat(count { it.tags.contains(EventDownSyncDownloaderWorker::class.qualifiedName) }).isEqualTo(
            count
        )

    private fun List<WorkRequest>.assertNumberOfDownSyncCountWorkers(count: Int) =
        Truth.assertThat(count { it.tags.contains(EventDownSyncCountWorker::class.qualifiedName) }).isEqualTo(
            count
        )

    private fun List<WorkRequest>.assertDownSyncDownloaderWorkerInput(downSyncScope: EventDownSyncScope) {
        val downloaders =
            filter { it.tags.contains(EventDownSyncDownloaderWorker::class.qualifiedName) }
        val jsonHelper = JsonHelper
        val ops = downSyncScope.operations
        ops.forEach { op ->
            Truth.assertThat(downloaders.any {
                it.workSpec.input == workDataOf(INPUT_DOWN_SYNC_OPS to jsonHelper.toJson(op))
            }).isTrue()
        }
        Truth.assertThat(downloaders).hasSize(ops.size)
    }

    private fun List<WorkRequest>.assertDownSyncCountWorkerInput(downSyncScope: EventDownSyncScope) {
        val counter = first { it.tags.contains(EventDownSyncCountWorker::class.qualifiedName) }
        val jsonHelper = JsonHelper
        Truth.assertThat(
            counter.workSpec.input == workDataOf(
                INPUT_COUNT_WORKER_DOWN to jsonHelper.toJson(
                    downSyncScope
                )
            )
        ).isTrue()
    }
}
