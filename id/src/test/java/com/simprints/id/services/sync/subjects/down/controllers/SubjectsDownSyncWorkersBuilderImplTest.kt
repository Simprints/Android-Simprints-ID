package com.simprints.id.services.sync.subjects.down.controllers

import androidx.work.WorkRequest
import com.google.common.truth.Truth.assertThat
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_MODULE_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_MODULE_ID_2
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_USER_ID
import com.simprints.id.data.db.subjects_sync.down.SubjectsDownSyncScopeRepository
import com.simprints.id.data.db.subjects_sync.down.domain.EventsDownSyncOperationFactoryImpl
import com.simprints.id.domain.modality.Modes
import com.simprints.id.services.sync.subjects.common.*
import com.simprints.id.services.sync.subjects.down.workers.SubjectsDownSyncCountWorker
import com.simprints.id.services.sync.subjects.down.workers.SubjectsDownSyncDownloaderWorker
import com.simprints.id.services.sync.subjects.master.models.SubjectsSyncWorkerType.Companion.tagForType
import com.simprints.id.services.sync.subjects.master.models.SubjectsSyncWorkerType.DOWNLOADER
import com.simprints.id.services.sync.subjects.master.models.SubjectsSyncWorkerType.DOWN_COUNTER
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class SubjectsDownSyncWorkersBuilderImplTest {

    val builder = EventsDownSyncOperationFactoryImpl()
    private val modes = listOf(Modes.FINGERPRINT, Modes.FACE)
    private val opsForProjectDownSync = listOf(builder.buildProjectSyncOperation(DEFAULT_PROJECT_ID, modes, null))
    private val opsForUserDownSync = listOf(builder.buildUserSyncOperation(DEFAULT_PROJECT_ID, DEFAULT_USER_ID, modes, null))
    private val opsForModuleDownSync = listOf(
        builder.buildModuleSyncOperation(DEFAULT_PROJECT_ID, DEFAULT_MODULE_ID, modes, null),
        builder.buildModuleSyncOperation(DEFAULT_PROJECT_ID, DEFAULT_MODULE_ID_2, modes, null))

    private lateinit var subjectsDownSyncWorkersFactory: SubjectsDownSyncWorkersBuilder
    private lateinit var subjectsDownSyncScopeRepository: SubjectsDownSyncScopeRepository

    @Before
    fun setUp() {
        subjectsDownSyncScopeRepository = mockk(relaxed = true)
        subjectsDownSyncWorkersFactory = SubjectsDownSyncWorkersBuilderImpl(subjectsDownSyncScopeRepository, mockk())
    }

    @Test
    fun builder_forProjectDownSync_shouldReturnTheRightWorkers() = runBlocking {
        coEvery { subjectsDownSyncScopeRepository.getDownSyncOperations(any()) } returns opsForProjectDownSync

        val chain = subjectsDownSyncWorkersFactory.buildDownSyncWorkerChain("")
        chain.assertNumberOfDownSyncDownloaderWorker(1)
        chain.assertSubjectsDownSyncCountWorkerTagsForPeriodic(1)
        assertThat(chain.size).isEqualTo(2)
    }

    @Test
    fun builder_forUserDownSync_shouldReturnTheRightWorkers() = runBlocking {
        coEvery { subjectsDownSyncScopeRepository.getDownSyncOperations(any()) } returns opsForUserDownSync

        val chain = subjectsDownSyncWorkersFactory.buildDownSyncWorkerChain("")
        chain.assertNumberOfDownSyncDownloaderWorker(1)
        chain.assertSubjectsDownSyncCountWorkerTagsForPeriodic(1)
        assertThat(chain.size).isEqualTo(2)
    }

    @Test
    fun builder_forModuleDownSync_shouldReturnTheRightWorkers() = runBlocking {
        coEvery { subjectsDownSyncScopeRepository.getDownSyncOperations(any()) } returns opsForModuleDownSync

        val chain = subjectsDownSyncWorkersFactory.buildDownSyncWorkerChain("")
        chain.assertNumberOfDownSyncDownloaderWorker(2)
        chain.assertSubjectsDownSyncCountWorkerTagsForPeriodic(1)
        assertThat(chain.size).isEqualTo(3)
    }

    @Test
    fun builder_periodicDownSyncWorkers_shouldHaveTheRightTags() = runBlocking {
        coEvery { subjectsDownSyncScopeRepository.getDownSyncOperations(any()) } returns opsForProjectDownSync
        val uniqueSyncId = "uniqueSyncId"
        val chain = subjectsDownSyncWorkersFactory.buildDownSyncWorkerChain(uniqueSyncId)
        chain.assertNumberOfDownSyncDownloaderWorker(1)
        chain.assertSubjectsDownSyncCountWorkerTagsForPeriodic(1)
        chain.first { it.tags.contains(SubjectsDownSyncDownloaderWorker::class.qualifiedName) }.assertSubjectsDownSyncDownloaderWorkerTagsForPeriodic()
        chain.first { it.tags.contains(SubjectsDownSyncCountWorker::class.qualifiedName) }.assertSubjectsDownSyncCountWorkerTagsForPeriodic()
    }

    @Test
    fun builder_oneTimeDownSyncWorkers_shouldHaveTheRightTags() = runBlocking {
        coEvery { subjectsDownSyncScopeRepository.getDownSyncOperations(any()) } returns opsForProjectDownSync

        val chain = subjectsDownSyncWorkersFactory.buildDownSyncWorkerChain(null)
        chain.assertNumberOfDownSyncDownloaderWorker(1)
        chain.assertSubjectsDownSyncCountWorkerTagsForPeriodic(1)
        chain.first { it.tags.contains(SubjectsDownSyncDownloaderWorker::class.qualifiedName) }.assertSubjectsDownSyncDownloaderWorkerTagsForOneTime()
        chain.first { it.tags.contains(SubjectsDownSyncCountWorker::class.qualifiedName) }.assertSubjectsDownSyncCountWorkerTagsForOneTime()
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
    assertThat(count { it.tags.contains(SubjectsDownSyncDownloaderWorker::class.qualifiedName) }).isEqualTo(count)

private fun List<WorkRequest>.assertSubjectsDownSyncCountWorkerTagsForPeriodic(count: Int) =
    assertThat(count { it.tags.contains(SubjectsDownSyncCountWorker::class.qualifiedName) }).isEqualTo(count)
