package com.simprints.id.services.sync.subjects.down.controllers

import androidx.work.WorkRequest
import androidx.work.workDataOf
import com.google.common.truth.Truth.assertThat
import com.simprints.core.login.LoginInfoManager
import com.simprints.core.tools.json.JsonHelper
import com.simprints.eventsystem.events_sync.down.EventDownSyncScopeRepository
import com.simprints.eventsystem.events_sync.down.domain.EventDownSyncScope
import com.simprints.eventsystem.sampledata.SampleDefaults.modulesDownSyncScope
import com.simprints.eventsystem.sampledata.SampleDefaults.projectDownSyncScope
import com.simprints.eventsystem.sampledata.SampleDefaults.userDownSyncScope
import com.simprints.id.data.prefs.IdPreferencesManager
import com.simprints.id.services.sync.events.common.*
import com.simprints.id.services.sync.events.down.EventDownSyncWorkersBuilder
import com.simprints.id.services.sync.events.down.EventDownSyncWorkersBuilderImpl
import com.simprints.id.services.sync.events.down.workers.EventDownSyncCountWorker
import com.simprints.id.services.sync.events.down.workers.EventDownSyncCountWorker.Companion.INPUT_COUNT_WORKER_DOWN
import com.simprints.id.services.sync.events.down.workers.EventDownSyncDownloaderWorker
import com.simprints.id.services.sync.events.down.workers.EventDownSyncDownloaderWorker.Companion.INPUT_DOWN_SYNC_OPS
import com.simprints.id.services.sync.events.master.models.EventSyncWorkerType.Companion.tagForType
import com.simprints.id.services.sync.events.master.models.EventSyncWorkerType.DOWNLOADER
import com.simprints.id.services.sync.events.master.models.EventSyncWorkerType.DOWN_COUNTER
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class EventDownSyncWorkersBuilderImplTest {

    private lateinit var eventDownSyncWorkersFactory: EventDownSyncWorkersBuilder

    @MockK
    lateinit var eventDownSyncScopeRepository: EventDownSyncScopeRepository

    @MockK
    lateinit var mockLoginInfoManager: LoginInfoManager

    @MockK
    lateinit var mockPreferencesManager: IdPreferencesManager

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        eventDownSyncWorkersFactory = EventDownSyncWorkersBuilderImpl(
            eventDownSyncScopeRepository,
            JsonHelper,
            mockPreferencesManager
        )
    }

    @Test
    fun builder_forProjectDownSync_shouldReturnTheRightWorkers() = runBlocking {
        coEvery {
            eventDownSyncScopeRepository.getDownSyncScope(
                any(),
                any(),
                any()
            )
        } returns projectDownSyncScope

        val chain = eventDownSyncWorkersFactory.buildDownSyncWorkerChain("")

        chain.assertNumberOfDownSyncDownloaderWorker(1)
        chain.assertSubjectsDownSyncCountWorkerTagsForPeriodic(1)
        chain.assertDownSyncDownloaderWorkerInput(projectDownSyncScope)
        chain.assertDownSyncCountWorkerInput(projectDownSyncScope)
        assertThat(chain.size).isEqualTo(2)
    }

    @Test
    fun builder_forUserDownSync_shouldReturnTheRightWorkers() = runBlocking {
        coEvery {
            eventDownSyncScopeRepository.getDownSyncScope(
                any(),
                any(),
                any()
            )
        } returns userDownSyncScope

        val chain = eventDownSyncWorkersFactory.buildDownSyncWorkerChain("")
        chain.assertNumberOfDownSyncDownloaderWorker(1)
        chain.assertSubjectsDownSyncCountWorkerTagsForPeriodic(1)
        chain.assertDownSyncDownloaderWorkerInput(userDownSyncScope)
        chain.assertDownSyncCountWorkerInput(userDownSyncScope)
        assertThat(chain.size).isEqualTo(2)
    }

    @Test
    fun builder_forModuleDownSync_shouldReturnTheRightWorkers() = runBlocking {
        coEvery {
            eventDownSyncScopeRepository.getDownSyncScope(
                any(),
                any(),
                any()
            )
        } returns modulesDownSyncScope

        val chain = eventDownSyncWorkersFactory.buildDownSyncWorkerChain("")
        chain.assertNumberOfDownSyncDownloaderWorker(2)
        chain.assertSubjectsDownSyncCountWorkerTagsForPeriodic(1)
        chain.assertDownSyncDownloaderWorkerInput(modulesDownSyncScope)
        chain.assertDownSyncCountWorkerInput(modulesDownSyncScope)
        assertThat(chain.size).isEqualTo(3)
    }

    @Test
    fun builder_forModuleDownSync_shouldOnlySyncSelectedModules() = runBlockingTest {
        coEvery {
            eventDownSyncScopeRepository.getDownSyncScope(
                any(),
                any(),
                any()
            )
        } returns modulesDownSyncScope

        eventDownSyncWorkersFactory.buildDownSyncWorkerChain("")
        verify(exactly = 1) { mockPreferencesManager.selectedModules }
        verify(exactly = 0) { mockPreferencesManager.moduleIdOptions }
    }

    @Test
    fun builder_periodicDownSyncWorkers_shouldHaveTheRightTags() = runBlocking {
        coEvery {
            eventDownSyncScopeRepository.getDownSyncScope(
                any(),
                any(),
                any()
            )
        } returns projectDownSyncScope
        val uniqueSyncId = "uniqueSyncId"
        val chain = eventDownSyncWorkersFactory.buildDownSyncWorkerChain(uniqueSyncId)
        chain.assertNumberOfDownSyncDownloaderWorker(1)
        chain.assertSubjectsDownSyncCountWorkerTagsForPeriodic(1)
        chain.first { it.tags.contains(EventDownSyncDownloaderWorker::class.qualifiedName) }
            .assertSubjectsDownSyncDownloaderWorkerTagsForPeriodic()
        chain.first { it.tags.contains(EventDownSyncCountWorker::class.qualifiedName) }
            .assertSubjectsDownSyncCountWorkerTagsForPeriodic()
    }

    @Test
    fun builder_oneTimeDownSyncWorkers_shouldHaveTheRightTags() = runBlocking {
        coEvery {
            eventDownSyncScopeRepository.getDownSyncScope(
                any(),
                any(),
                any()
            )
        } returns projectDownSyncScope

        val chain = eventDownSyncWorkersFactory.buildDownSyncWorkerChain(null)
        chain.assertNumberOfDownSyncDownloaderWorker(1)
        chain.assertSubjectsDownSyncCountWorkerTagsForPeriodic(1)
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

private fun List<WorkRequest>.assertNumberOfDownSyncDownloaderWorker(count: Int) =
    assertThat(count { it.tags.contains(EventDownSyncDownloaderWorker::class.qualifiedName) }).isEqualTo(
        count
    )

private fun List<WorkRequest>.assertSubjectsDownSyncCountWorkerTagsForPeriodic(count: Int) =
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

