package com.simprints.id.services.scheduledSync.people.down.controllers

import androidx.work.WorkRequest
import com.google.common.truth.Truth.assertThat
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_MODULE_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_MODULE_ID_2
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_USER_ID
import com.simprints.id.data.db.people_sync.down.PeopleDownSyncScopeRepository
import com.simprints.id.data.db.people_sync.down.domain.PeopleDownSyncOperationFactoryImpl
import com.simprints.id.domain.modality.Modes
import com.simprints.id.services.scheduledSync.people.down.controllers.PeopleDownSyncWorkersFactory.Companion.TAG_DOWN_MASTER_SYNC_ID
import com.simprints.id.services.scheduledSync.people.down.controllers.PeopleDownSyncWorkersFactory.Companion.TAG_PEOPLE_DOWN_SYNC_ALL_WORKERS
import com.simprints.id.services.scheduledSync.people.down.workers.PeopleDownSyncCountWorker
import com.simprints.id.services.scheduledSync.people.down.workers.PeopleDownSyncDownloaderWorker
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncMasterWorker.Companion.TAG_MASTER_SYNC_ID
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncMasterWorker.Companion.TAG_PEOPLE_SYNC_ALL_WORKERS
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncMasterWorker.Companion.TAG_SCHEDULED_AT
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncWorkerType.Companion.tagForType
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncWorkerType.DOWNLOADER
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncWorkerType.DOWN_COUNTER
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class PeopleDownSyncWorkersFactoryImplTest {

    val builder = PeopleDownSyncOperationFactoryImpl()
    private val modes = listOf(Modes.FINGERPRINT, Modes.FACE)
    private val opsForProjectDownSync = listOf(builder.buildProjectSyncOperation(DEFAULT_PROJECT_ID, modes, null))
    private val opsForUserDownSync = listOf(builder.buildUserSyncOperation(DEFAULT_PROJECT_ID, DEFAULT_USER_ID, modes, null))
    private val opsForModuleDownSync = listOf(
        builder.buildModuleSyncOperation(DEFAULT_PROJECT_ID, DEFAULT_MODULE_ID, modes, null),
        builder.buildModuleSyncOperation(DEFAULT_PROJECT_ID, DEFAULT_MODULE_ID_2, modes, null))

    private lateinit var peopleDownSyncWorkersFactory: PeopleDownSyncWorkersFactory
    private lateinit var peopleDownSyncScopeRepository: PeopleDownSyncScopeRepository

    @Before
    fun setUp() {
        peopleDownSyncScopeRepository = mockk(relaxed = true)
        peopleDownSyncWorkersFactory = PeopleDownSyncWorkersFactoryImpl(peopleDownSyncScopeRepository)
    }

    @Test
    fun builder_forProjectDownSync_shouldReturnTheRightWorkers() = runBlocking {
        coEvery { peopleDownSyncScopeRepository.getDownSyncOperations(any()) } returns opsForProjectDownSync

        val chain = peopleDownSyncWorkersFactory.buildDownSyncWorkerChain("")
        chain.assertNumberOfDownSyncDownloaderWorker(1)
        chain.assertPeopleDownSyncCountWorkerTagsForPeriodic(1)
        assertThat(chain.size).isEqualTo(2)
    }

    @Test
    fun builder_forUserDownSync_shouldReturnTheRightWorkers() = runBlocking {
        coEvery { peopleDownSyncScopeRepository.getDownSyncOperations(any()) } returns opsForUserDownSync

        val chain = peopleDownSyncWorkersFactory.buildDownSyncWorkerChain("")
        chain.assertNumberOfDownSyncDownloaderWorker(1)
        chain.assertPeopleDownSyncCountWorkerTagsForPeriodic(1)
        assertThat(chain.size).isEqualTo(2)
    }

    @Test
    fun builder_forModuleDownSync_shouldReturnTheRightWorkers() = runBlocking {
        coEvery { peopleDownSyncScopeRepository.getDownSyncOperations(any()) } returns opsForModuleDownSync

        val chain = peopleDownSyncWorkersFactory.buildDownSyncWorkerChain("")
        chain.assertNumberOfDownSyncDownloaderWorker(2)
        chain.assertPeopleDownSyncCountWorkerTagsForPeriodic(1)
        assertThat(chain.size).isEqualTo(3)
    }

    @Test
    fun builder_periodicDownSyncWorkers_shouldHaveTheRightTags() = runBlocking {
        coEvery { peopleDownSyncScopeRepository.getDownSyncOperations(any()) } returns opsForProjectDownSync
        val uniqueSyncId = "uniqueSyncId"
        val chain = peopleDownSyncWorkersFactory.buildDownSyncWorkerChain(uniqueSyncId)
        chain.assertNumberOfDownSyncDownloaderWorker(1)
        chain.assertPeopleDownSyncCountWorkerTagsForPeriodic(1)
        chain.first { it.tags.contains(PeopleDownSyncDownloaderWorker::class.qualifiedName) }.assertPeopleDownSyncDownloaderWorkerTagsForPeriodic()
        chain.first { it.tags.contains(PeopleDownSyncCountWorker::class.qualifiedName) }.assertPeopleDownSyncCountWorkerTagsForPeriodic()
    }

    @Test
    fun builder_oneTimeDownSyncWorkers_shouldHaveTheRightTags() = runBlocking {
        coEvery { peopleDownSyncScopeRepository.getDownSyncOperations(any()) } returns opsForProjectDownSync

        val chain = peopleDownSyncWorkersFactory.buildDownSyncWorkerChain(null)
        chain.assertNumberOfDownSyncDownloaderWorker(1)
        chain.assertPeopleDownSyncCountWorkerTagsForPeriodic(1)
        chain.first { it.tags.contains(PeopleDownSyncDownloaderWorker::class.qualifiedName) }.assertPeopleDownSyncDownloaderWorkerTagsForOneTime()
        chain.first { it.tags.contains(PeopleDownSyncCountWorker::class.qualifiedName) }.assertPeopleDownSyncCountWorkerTagsForOneTime()
    }
}

private fun WorkRequest.assertPeopleDownSyncDownloaderWorkerTagsForPeriodic() {
    assertThat(tags.size).isEqualTo(7)
    assertUniqueMasterIdTag()

    assertCommonDownSyncWorkersTags()
    assertCommonDownSyncDownloadersWorkersTag()
}

private fun WorkRequest.assertPeopleDownSyncCountWorkerTagsForPeriodic() {
    assertThat(tags.size).isEqualTo(7)
    assertUniqueMasterIdTag()

    assertCommonDownSyncWorkersTags()
    assertCommonDownSyncCounterWorkersTag()
}

private fun WorkRequest.assertPeopleDownSyncDownloaderWorkerTagsForOneTime() {
    assertThat(tags.size).isEqualTo(6)
    assertCommonDownSyncWorkersTags()
    assertCommonDownSyncDownloadersWorkersTag()
}

private fun WorkRequest.assertPeopleDownSyncCountWorkerTagsForOneTime() {
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
    assertThat(tags).contains(TAG_PEOPLE_SYNC_ALL_WORKERS)

private fun WorkRequest.assertCommonDownSyncTag() =
    assertThat(tags).contains(TAG_PEOPLE_DOWN_SYNC_ALL_WORKERS)

private fun WorkRequest.assertScheduleAtTag() =
    assertThat(tags.firstOrNull { it.contains(TAG_SCHEDULED_AT) }).isNotNull()

private fun WorkRequest.assertUniqueDownSyncMasterTag() =
    assertThat(tags.firstOrNull { it.contains(TAG_DOWN_MASTER_SYNC_ID) }).isNotNull()

private fun List<WorkRequest>.assertNumberOfDownSyncDownloaderWorker(count: Int) =
    assertThat(count { it.tags.contains(PeopleDownSyncDownloaderWorker::class.qualifiedName) }).isEqualTo(count)

private fun List<WorkRequest>.assertPeopleDownSyncCountWorkerTagsForPeriodic(count: Int) =
    assertThat(count { it.tags.contains(PeopleDownSyncCountWorker::class.qualifiedName) }).isEqualTo(count)
