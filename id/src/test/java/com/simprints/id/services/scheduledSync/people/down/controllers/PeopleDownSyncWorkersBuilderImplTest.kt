package com.simprints.id.services.scheduledSync.people.down.controllers

import androidx.work.WorkRequest
import com.google.common.truth.Truth.assertThat
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_MODULE_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_MODULE_ID_2
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_USER_ID
import com.simprints.id.data.db.people_sync.down.PeopleDownSyncScopeRepository
import com.simprints.id.data.db.people_sync.down.domain.PeopleDownSyncOperation.Companion.buildModuleOperation
import com.simprints.id.data.db.people_sync.down.domain.PeopleDownSyncOperation.Companion.buildProjectOperation
import com.simprints.id.data.db.people_sync.down.domain.PeopleDownSyncOperation.Companion.buildUserOperation
import com.simprints.id.domain.modality.Modes
import com.simprints.id.services.scheduledSync.people.down.controllers.PeopleDownSyncWorkersBuilder.Companion.TAG_DOWN_MASTER_SYNC_ID
import com.simprints.id.services.scheduledSync.people.down.controllers.PeopleDownSyncWorkersBuilder.Companion.TAG_PEOPLE_DOWN_SYNC_ALL_WORKERS
import com.simprints.id.services.scheduledSync.people.down.controllers.PeopleDownSyncWorkersBuilderImplTest.Companion.peopleDownSyncCountWorker
import com.simprints.id.services.scheduledSync.people.down.controllers.PeopleDownSyncWorkersBuilderImplTest.Companion.peopleDownSyncDownloaderWorker
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

class PeopleDownSyncWorkersBuilderImplTest {

    companion object {
        const val peopleDownSyncDownloaderWorker = "com.simprints.id.services.scheduledSync.people.down.workers.PeopleDownSyncDownloaderWorker"
        const val peopleDownSyncCountWorker = "com.simprints.id.services.scheduledSync.people.down.workers.PeopleDownSyncCountWorker"
    }

    private val modes = listOf(Modes.FINGERPRINT, Modes.FACE)
    private val opsForProjectDownSync = listOf(buildProjectOperation(DEFAULT_PROJECT_ID, modes, null))
    private val opsForUserDownSync = listOf(buildUserOperation(DEFAULT_PROJECT_ID, DEFAULT_USER_ID, modes, null))
    private val opsForModuleDownSync = listOf(
        buildModuleOperation(DEFAULT_PROJECT_ID, DEFAULT_MODULE_ID, modes, null),
        buildModuleOperation(DEFAULT_PROJECT_ID, DEFAULT_MODULE_ID_2, modes, null))

    private lateinit var peopleDownSyncWorkersBuilder: PeopleDownSyncWorkersBuilder
    private lateinit var peopleDownSyncScopeRepository: PeopleDownSyncScopeRepository

    @Before
    fun setUp() {
        peopleDownSyncScopeRepository = mockk(relaxed = true)
        peopleDownSyncWorkersBuilder = PeopleDownSyncWorkersBuilderImpl(peopleDownSyncScopeRepository)
    }

    @Test
    fun builder_forProjectDownSync_shouldReturnTheRightWorkers() = runBlocking {
        coEvery { peopleDownSyncScopeRepository.getDownSyncOperations(any()) } returns opsForProjectDownSync

        val chain = peopleDownSyncWorkersBuilder.buildDownSyncWorkerChain("")
        chain.assertNumberOfDownSyncDownloaderWorker(1)
        chain.assertPeopleDownSyncCountWorkerTagsForPeriodic(1)
        assertThat(chain.size).isEqualTo(2)
    }

    @Test
    fun builder_forUserDownSync_shouldReturnTheRightWorkers() = runBlocking {
        coEvery { peopleDownSyncScopeRepository.getDownSyncOperations(any()) } returns opsForUserDownSync

        val chain = peopleDownSyncWorkersBuilder.buildDownSyncWorkerChain("")
        chain.assertNumberOfDownSyncDownloaderWorker(1)
        chain.assertPeopleDownSyncCountWorkerTagsForPeriodic(1)
        assertThat(chain.size).isEqualTo(2)
    }

    @Test
    fun builder_forModuleDownSync_shouldReturnTheRightWorkers() = runBlocking {
        coEvery { peopleDownSyncScopeRepository.getDownSyncOperations(any()) } returns opsForModuleDownSync

        val chain = peopleDownSyncWorkersBuilder.buildDownSyncWorkerChain("")
        chain.assertNumberOfDownSyncDownloaderWorker(2)
        chain.assertPeopleDownSyncCountWorkerTagsForPeriodic(1)
        assertThat(chain.size).isEqualTo(3)
    }

    @Test
    fun builder_periodicDownSyncWorkers_shouldHaveTheRightTags() = runBlocking {
        coEvery { peopleDownSyncScopeRepository.getDownSyncOperations(any()) } returns opsForProjectDownSync
        val uniqueSyncId = "uniqueSyncId"
        val chain = peopleDownSyncWorkersBuilder.buildDownSyncWorkerChain(uniqueSyncId)
        chain.assertNumberOfDownSyncDownloaderWorker(1)
        chain.assertPeopleDownSyncCountWorkerTagsForPeriodic(1)
        chain.first { it.tags.contains(peopleDownSyncDownloaderWorker) }.assertPeopleDownSyncDownloaderWorkerTagsForPeriodic()
        chain.first { it.tags.contains(peopleDownSyncCountWorker) }.assertPeopleDownSyncCountWorkerTagsForPeriodic()
    }

    @Test
    fun builder_oneTimeDownSyncWorkers_shouldHaveTheRightTags() = runBlocking {
        coEvery { peopleDownSyncScopeRepository.getDownSyncOperations(any()) } returns opsForProjectDownSync

        val chain = peopleDownSyncWorkersBuilder.buildDownSyncWorkerChain(null)
        chain.assertNumberOfDownSyncDownloaderWorker(1)
        chain.assertPeopleDownSyncCountWorkerTagsForPeriodic(1)
        chain.first { it.tags.contains(peopleDownSyncDownloaderWorker) }.assertPeopleDownSyncDownloaderWorkerTagsForOneTime()
        chain.first { it.tags.contains(peopleDownSyncCountWorker) }.assertPeopleDownSyncCountWorkerTagsForOneTime()
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
    assertThat(count { it.tags.contains(peopleDownSyncDownloaderWorker) }).isEqualTo(count)

private fun List<WorkRequest>.assertPeopleDownSyncCountWorkerTagsForPeriodic(count: Int) =
    assertThat(count { it.tags.contains(peopleDownSyncCountWorker) }).isEqualTo(count)
