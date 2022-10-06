package com.simprints.id.services.sync.events.down

import androidx.work.WorkRequest
import androidx.work.workDataOf
import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.common.GROUP
import com.simprints.core.domain.modality.Modes
import com.simprints.core.tools.json.JsonHelper
import com.simprints.eventsystem.events_sync.down.EventDownSyncScopeRepository
import com.simprints.eventsystem.events_sync.down.domain.EventDownSyncScope
import com.simprints.eventsystem.sampledata.SampleDefaults.modulesDownSyncScope
import com.simprints.eventsystem.sampledata.SampleDefaults.projectDownSyncScope
import com.simprints.eventsystem.sampledata.SampleDefaults.userDownSyncScope
import com.simprints.id.services.sync.events.common.*
import com.simprints.id.services.sync.events.down.workers.EventDownSyncCountWorker
import com.simprints.id.services.sync.events.down.workers.EventDownSyncCountWorker.Companion.INPUT_COUNT_WORKER_DOWN
import com.simprints.id.services.sync.events.down.workers.EventDownSyncDownloaderWorker
import com.simprints.id.services.sync.events.down.workers.EventDownSyncDownloaderWorker.Companion.INPUT_DOWN_SYNC_OPS
import com.simprints.id.services.sync.events.master.models.EventSyncWorkerType.Companion.tagForType
import com.simprints.id.services.sync.events.master.models.EventSyncWorkerType.DOWNLOADER
import com.simprints.id.services.sync.events.master.models.EventSyncWorkerType.DOWN_COUNTER
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.DeviceConfiguration
import com.simprints.infra.config.domain.models.DownSynchronizationConfiguration
import com.simprints.infra.config.domain.models.GeneralConfiguration
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
    private lateinit var eventDownSyncWorkersBuilder: EventDownSyncWorkersBuilderImpl

    @Before
    fun setUp() {
        coEvery { configManager.getDeviceConfiguration() } returns DeviceConfiguration(
            "",
            SELECTED_MODULE,
            listOf()
        )
        coEvery { configManager.getProjectConfiguration() } returns mockk {
            every { general } returns generalConfiguration
            every { synchronization } returns mockk {
                every { down } returns downSyncConfiguration
            }
        }

        eventDownSyncWorkersBuilder = EventDownSyncWorkersBuilderImpl(
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
            } returns projectDownSyncScope

            val chain = eventDownSyncWorkersBuilder.buildDownSyncWorkerChain("")

            chain.assertNumberOfDownSyncDownloaderWorkers(1)
            chain.assertNumberOfDownSyncCountWorkers(1)
            chain.assertDownSyncDownloaderWorkerInput(projectDownSyncScope)
            chain.assertDownSyncCountWorkerInput(projectDownSyncScope)
            assertThat(chain.size).isEqualTo(2)
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
            } returns userDownSyncScope

            val chain = eventDownSyncWorkersBuilder.buildDownSyncWorkerChain("")
            chain.assertNumberOfDownSyncDownloaderWorkers(1)
            chain.assertNumberOfDownSyncCountWorkers(1)
            chain.assertDownSyncDownloaderWorkerInput(userDownSyncScope)
            chain.assertDownSyncCountWorkerInput(userDownSyncScope)
            assertThat(chain.size).isEqualTo(2)
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
            } returns modulesDownSyncScope

            val chain = eventDownSyncWorkersBuilder.buildDownSyncWorkerChain("")
            chain.assertNumberOfDownSyncDownloaderWorkers(2)
            chain.assertNumberOfDownSyncCountWorkers(1)
            chain.assertDownSyncDownloaderWorkerInput(modulesDownSyncScope)
            chain.assertDownSyncCountWorkerInput(modulesDownSyncScope)
            assertThat(chain.size).isEqualTo(3)
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
            } returns projectDownSyncScope
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
            } returns projectDownSyncScope

            val chain = eventDownSyncWorkersBuilder.buildDownSyncWorkerChain(null)
            chain.assertNumberOfDownSyncDownloaderWorkers(1)
            chain.assertNumberOfDownSyncCountWorkers(1)
            chain.first { it.tags.contains(EventDownSyncDownloaderWorker::class.qualifiedName) }
                .assertSubjectsDownSyncDownloaderWorkerTagsForOneTime()
            chain.first { it.tags.contains(EventDownSyncCountWorker::class.qualifiedName) }
                .assertSubjectsDownSyncCountWorkerTagsForOneTime()
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
            INPUT_COUNT_WORKER_DOWN to jsonHelper.toJson(
                downSyncScope
            )
        )
    ).isTrue()
}

