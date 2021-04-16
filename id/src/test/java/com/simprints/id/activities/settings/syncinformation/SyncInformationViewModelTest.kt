package com.simprints.id.activities.settings.syncinformation

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.google.common.truth.Truth.assertThat
import com.simprints.id.activities.settings.syncinformation.modulecount.ModuleCount
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_MODULE_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.commontesttools.DefaultTestConstants.projectDownSyncScope
import com.simprints.id.data.db.event.EventRepository
import com.simprints.id.data.db.event.domain.EventCount
import com.simprints.id.data.db.event.domain.models.EventType.*
import com.simprints.id.data.db.events_sync.down.EventDownSyncScopeRepository
import com.simprints.id.data.db.subject.SubjectRepository
import com.simprints.id.data.images.repository.ImageRepository
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.prefs.settings.canSyncToSimprints
import com.simprints.id.services.sync.events.down.EventDownSyncHelper
import com.simprints.id.services.sync.events.master.EventSyncManager
import com.simprints.id.services.sync.events.master.models.EventDownSyncSetting.OFF
import com.simprints.id.services.sync.events.master.models.EventDownSyncSetting.ON
import com.simprints.id.services.sync.events.master.models.EventSyncState
import com.simprints.id.services.sync.events.master.models.EventSyncState.SyncWorkerInfo
import com.simprints.id.services.sync.events.master.models.EventSyncWorkerState
import com.simprints.id.services.sync.events.master.models.EventSyncWorkerState.Succeeded
import com.simprints.id.services.sync.events.master.models.EventSyncWorkerType.DOWNLOADER
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.testtools.common.livedata.getOrAwaitValue
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class SyncInformationViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @MockK
    lateinit var downySyncHelper: EventDownSyncHelper

    @MockK
    lateinit var eventRepository: EventRepository

    @MockK
    lateinit var subjectRepository: SubjectRepository

    @MockK
    lateinit var preferencesManager: PreferencesManager

    @MockK
    lateinit var eventDownSyncScopeRepository: EventDownSyncScopeRepository

    @MockK
    lateinit var imageRepository: ImageRepository

    @MockK
    lateinit var eventSyncManager: EventSyncManager

    private val projectId = DEFAULT_PROJECT_ID
    private lateinit var viewModel: SyncInformationViewModel

    @ExperimentalCoroutinesApi
    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        UnitTestConfig(this).coroutinesMainThread()
        viewModel = SyncInformationViewModel(
            downySyncHelper,
            eventRepository,
            subjectRepository,
            preferencesManager,
            projectId,
            eventDownSyncScopeRepository,
            imageRepository
        )
    }

    @Test
    fun syncReset_shouldResetViewState() {
        viewModel.resetFetchingSyncInformation()
        val vo = ViewObservers(viewModel)

        assertThat(vo.imagesToUploadObserver).isEqualTo(null)
        assertThat(vo.recordsToUpSyncObserver).isEqualTo(null)
        assertThat(vo.recordsInLocalObserver).isEqualTo(null)
        assertThat(vo.recordsToDeleteObserver).isEqualTo(null)
        assertThat(vo.recordsToDownSyncObserver).isEqualTo(null)
        assertThat(vo.moduleCountsObserver).isEqualTo(null)
    }

    @Test
    fun syncComplete_fetchFromRemoteAndLocalSucceeds_shouldHaveCorrectViewState() {
        val localCount = 322
        val imagesToUpload = 12
        val countInRemoteForCreate = 123
        val countInRemoteForDelete = 22
        val countInRemoteForMove = 0

        val moduleName = DEFAULT_MODULE_ID
        val moduleCount = listOf(ModuleCount(moduleName, localCount))

        every { preferencesManager.eventDownSyncSetting } returns ON
        every { preferencesManager.canSyncToSimprints() } returns true
        every { eventSyncManager.getLastSyncState() } returns MutableLiveData(buildSubjectsSyncState(Succeeded))
        coEvery { eventDownSyncScopeRepository.getDownSyncScope() } returns projectDownSyncScope
        every { preferencesManager.selectedModules } returns setOf(moduleName)
        coEvery { eventRepository.localCount(any()) } returns localCount
        coEvery { eventRepository.localCount(any(), any()) } returns localCount
        coEvery { subjectRepository.count(any()) } returns localCount
        every { imageRepository.getNumberOfImagesToUpload() } returns imagesToUpload
        coEvery { downySyncHelper.countForDownSync(any()) } returns listOf(
            EventCount(ENROLMENT_RECORD_CREATION, countInRemoteForCreate),
            EventCount(ENROLMENT_RECORD_DELETION, countInRemoteForDelete),
            EventCount(ENROLMENT_RECORD_MOVE, countInRemoteForMove)
        )

        viewModel.fetchSyncInformation()

        val vo = ViewObservers(viewModel)

        assertThat(vo.imagesToUploadObserver).isEqualTo(imagesToUpload)
        assertThat(vo.recordsToUpSyncObserver).isEqualTo(localCount)
        assertThat(vo.recordsInLocalObserver).isEqualTo(localCount)
        assertThat(vo.recordsToDeleteObserver).isEqualTo(countInRemoteForDelete)
        assertThat(vo.recordsToDownSyncObserver).isEqualTo(countInRemoteForCreate)
        assertThat(vo.moduleCountsObserver).isEqualTo(moduleCount)
    }

    @Test
    fun syncComplete_DownSyncIsOff_shouldHaveCorrectViewState() {
        val localCount = 322
        val imagesToUpload = 12

        val moduleName = DEFAULT_MODULE_ID
        val moduleCount = listOf(ModuleCount(moduleName, localCount))

        every { preferencesManager.eventDownSyncSetting } returns OFF
        every { preferencesManager.canSyncToSimprints() } returns true
        every { eventSyncManager.getLastSyncState() } returns MutableLiveData(buildSubjectsSyncState(Succeeded))
        every { preferencesManager.selectedModules } returns setOf(moduleName)
        coEvery { eventRepository.localCount(any()) } returns localCount
        coEvery { eventRepository.localCount(any(), any()) } returns localCount
        coEvery { subjectRepository.count(any()) } returns localCount
        every { imageRepository.getNumberOfImagesToUpload() } returns imagesToUpload

        viewModel.fetchSyncInformation()

        val vo = ViewObservers(viewModel)

        assertThat(vo.imagesToUploadObserver).isEqualTo(imagesToUpload)
        assertThat(vo.recordsToUpSyncObserver).isEqualTo(localCount)
        assertThat(vo.recordsInLocalObserver).isEqualTo(localCount)
        assertThat(vo.recordsToDeleteObserver).isEqualTo(0)
        assertThat(vo.recordsToDownSyncObserver).isEqualTo(0)
        assertThat(vo.moduleCountsObserver).isEqualTo(moduleCount)
    }

    @Test
    fun syncComplete_FetchFromRemoteFails_shouldHaveCorrectViewState() {
        val localCount = 322
        val imagesToUpload = 12

        val moduleName = "module1"
        val moduleCount = listOf(ModuleCount(moduleName, localCount))

        every { preferencesManager.eventDownSyncSetting } returns ON
        every { preferencesManager.canSyncToSimprints() } returns true
        every { eventSyncManager.getLastSyncState() } returns MutableLiveData(buildSubjectsSyncState(Succeeded))
        every { preferencesManager.selectedModules } returns setOf(moduleName)
        coEvery { eventRepository.localCount(any()) } returns localCount
        coEvery { eventRepository.localCount(any(), any()) } returns localCount
        coEvery { subjectRepository.count(any()) } returns localCount
        coEvery { eventDownSyncScopeRepository.getDownSyncScope() } throws IOException()
        every { imageRepository.getNumberOfImagesToUpload() } returns imagesToUpload

        viewModel.fetchSyncInformation()

        val vo = ViewObservers(viewModel)

        assertThat(vo.imagesToUploadObserver).isEqualTo(imagesToUpload)
        assertThat(vo.recordsToUpSyncObserver).isEqualTo(localCount)
        assertThat(vo.recordsInLocalObserver).isEqualTo(localCount)
        assertThat(vo.recordsToDeleteObserver).isEqualTo(0)
        assertThat(vo.recordsToDownSyncObserver).isEqualTo(0)
        assertThat(vo.moduleCountsObserver).isEqualTo(moduleCount)
    }

    private fun buildSubjectsSyncState(syncWorkerState: EventSyncWorkerState) =
        EventSyncState(
            syncId = "sync_id", progress = 1, total = 20,
            upSyncWorkersInfo = listOf(),
            downSyncWorkersInfo = listOf(
                SyncWorkerInfo(
                    DOWNLOADER, syncWorkerState
                )
            )
        )

    data class ViewObservers(
        val viewModel: SyncInformationViewModel,
        val imagesToUploadObserver: Int? = viewModel.imagesToUpload.getOrAwaitValue(),
        val recordsToUpSyncObserver: Int? = viewModel.recordsToUpSync.getOrAwaitValue(),
        val recordsInLocalObserver: Int? = viewModel.recordsInLocal.getOrAwaitValue(),
        val recordsToDeleteObserver: Int? = viewModel.recordsToDelete.getOrAwaitValue(),
        val recordsToDownSyncObserver: Int? = viewModel.recordsToDownSync.getOrAwaitValue(),
        val moduleCountsObserver: List<ModuleCount>? = viewModel.moduleCounts.getOrAwaitValue()
    )

}
