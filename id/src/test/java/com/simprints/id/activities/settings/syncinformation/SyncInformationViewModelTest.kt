package com.simprints.id.activities.settings.syncinformation

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.asLiveData
import com.google.common.truth.Truth.assertThat
import com.simprints.id.activities.settings.syncinformation.SyncInformationActivity.ViewState.SyncDataFetched
import com.simprints.id.activities.settings.syncinformation.modulecount.ModuleCount
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_MODULE_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.data.db.event.domain.EventCount
import com.simprints.id.data.db.event.domain.models.EventType.*
import com.simprints.id.data.db.events_sync.down.EventDownSyncScopeRepository
import com.simprints.id.data.db.subject.SubjectRepository
import com.simprints.id.data.images.repository.ImageRepository
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.services.sync.events.down.EventDownSyncHelper
import com.simprints.id.services.sync.events.master.EventSyncManager
import com.simprints.id.services.sync.events.master.models.EventDownSyncSetting.OFF
import com.simprints.id.services.sync.events.master.models.EventDownSyncSetting.ON
import com.simprints.id.services.sync.events.master.models.EventSyncState
import com.simprints.id.services.sync.events.master.models.EventSyncState.SyncWorkerInfo
import com.simprints.id.services.sync.events.master.models.EventSyncWorkerState
import com.simprints.id.services.sync.events.master.models.EventSyncWorkerState.Running
import com.simprints.id.services.sync.events.master.models.EventSyncWorkerState.Succeeded
import com.simprints.id.services.sync.events.master.models.EventSyncWorkerType.DOWNLOADER
import com.simprints.id.services.sync.events.up.EventUpSyncHelper
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.testtools.common.livedata.testObserver
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class SyncInformationViewModelTest {

    @get:Rule val rule = InstantTaskExecutorRule()

    @MockK lateinit var downySyncHelper: EventDownSyncHelper
    @MockK lateinit var upSyncHelper: EventUpSyncHelper
    @MockK lateinit var subjectRepository: SubjectRepository
    @MockK lateinit var preferencesManager: PreferencesManager
    @MockK lateinit var eventDownSyncScopeRepository: EventDownSyncScopeRepository
    @MockK lateinit var imageRepository: ImageRepository
    @MockK lateinit var eventSyncManager: EventSyncManager
    private lateinit var subjectRepositoryMock: SubjectRepository

    private val projectId = DEFAULT_PROJECT_ID
    private lateinit var viewModel: SyncInformationViewModel

    @ExperimentalCoroutinesApi
    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        UnitTestConfig(this).coroutinesMainThread()
        subjectRepositoryMock = mockk()
        viewModel = SyncInformationViewModel(
            downySyncHelper,
            upSyncHelper,
            subjectRepository,
            preferencesManager,
            projectId,
            eventDownSyncScopeRepository,
            imageRepository,
            eventSyncManager)
    }

    @Test
    fun syncInProgress_shouldHaveSyncingViewState() {
        runBlocking {
            every { eventSyncManager.getLastSyncState() } returns flowOf(buildSubjectsSyncState(Running)).asLiveData()

            val testObserver = viewModel.getViewStateLiveData().testObserver()

            assertThat(testObserver.observedValues.last()).isEqualTo(SyncInformationActivity.ViewState.LoadingState.Syncing)
        }
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
        every { eventSyncManager.getLastSyncState() } returns flowOf(buildSubjectsSyncState(Succeeded)).asLiveData()
        every { preferencesManager.selectedModules } returns setOf(moduleName)
        coEvery { subjectRepository.count(any()) } returns localCount
        every { imageRepository.getNumberOfImagesToUpload() } returns imagesToUpload
        coEvery { downySyncHelper.countForDownSync(any()) } returns listOf(
            EventCount(ENROLMENT_RECORD_CREATION, countInRemoteForCreate),
            EventCount(ENROLMENT_RECORD_DELETION, countInRemoteForDelete),
            EventCount(ENROLMENT_RECORD_MOVE, countInRemoteForMove)
        )

        val testObserver = viewModel.getViewStateLiveData().testObserver()

        assertThat(testObserver.observedValues.last()).isEqualTo(
            SyncDataFetched(
                recordsInLocal = localCount,
                recordsToDownSync = countInRemoteForCreate,
                recordsToUpSync = localCount,
                recordsToDelete = countInRemoteForDelete,
                imagesToUpload = imagesToUpload,
                moduleCounts = moduleCount
            )
        )
    }

    @Test
    fun syncComplete_DownSyncIsOff_shouldHaveCorrectViewState() {
        val localCount = 322
        val imagesToUpload = 12

        val moduleName = DEFAULT_MODULE_ID
        val moduleCount = listOf(ModuleCount(moduleName, localCount))

        every { preferencesManager.eventDownSyncSetting } returns OFF
        every { eventSyncManager.getLastSyncState() } returns flowOf(buildSubjectsSyncState(Succeeded)).asLiveData()
        every { preferencesManager.selectedModules } returns setOf(moduleName)
        coEvery { subjectRepository.count(any()) } returns localCount
        every { imageRepository.getNumberOfImagesToUpload() } returns imagesToUpload

        val testObserver = viewModel.getViewStateLiveData().testObserver()

        assertThat(testObserver.observedValues.last()).isEqualTo(
            SyncDataFetched(
                recordsInLocal = localCount,
                recordsToDownSync = null,
                recordsToUpSync = localCount,
                recordsToDelete = null,
                imagesToUpload = imagesToUpload,
                moduleCounts = moduleCount
            )
        )
    }

    @Test
    fun syncComplete_FetchFromRemoteFails_shouldHaveCorrectViewState() {
        val localCount = 322
        val imagesToUpload = 12

        val moduleName = "module1"
        val moduleCount = listOf(ModuleCount(moduleName, localCount))

        every { preferencesManager.eventDownSyncSetting } returns ON
        every { eventSyncManager.getLastSyncState() } returns flowOf(buildSubjectsSyncState(Succeeded)).asLiveData()
        every { preferencesManager.selectedModules } returns setOf(moduleName)
        coEvery { subjectRepository.count(any()) } returns localCount
        every { imageRepository.getNumberOfImagesToUpload() } returns imagesToUpload
        coEvery { subjectRepositoryMock.count(any()) } throws IOException()

        val testObserver = viewModel.getViewStateLiveData().testObserver()

        assertThat(testObserver.observedValues.last()).isEqualTo(
            SyncDataFetched(
                recordsInLocal = localCount,
                recordsToDownSync = null,
                recordsToUpSync = localCount,
                recordsToDelete = null,
                imagesToUpload = imagesToUpload,
                moduleCounts = moduleCount
            )
        )
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
}
