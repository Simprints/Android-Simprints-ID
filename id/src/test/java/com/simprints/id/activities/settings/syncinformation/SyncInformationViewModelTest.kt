package com.simprints.id.activities.settings.syncinformation

import androidx.lifecycle.asLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.id.activities.settings.syncinformation.SyncInformationActivity.ViewState.SyncDataFetched
import com.simprints.id.activities.settings.syncinformation.modulecount.ModuleCount
import com.simprints.id.data.db.common.models.SubjectsCount
import com.simprints.id.data.db.subject.SubjectRepository
import com.simprints.id.data.db.subject.local.SubjectLocalDataSource
import com.simprints.id.data.db.subjects_sync.down.SubjectsDownSyncScopeRepository
import com.simprints.id.data.images.repository.ImageRepository
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.services.scheduledSync.subjects.master.SubjectsSyncManager
import com.simprints.id.services.scheduledSync.subjects.master.models.SubjectsDownSyncSetting
import com.simprints.id.services.scheduledSync.subjects.master.models.SubjectsSyncState
import com.simprints.id.services.scheduledSync.subjects.master.models.SubjectsSyncWorkerState
import com.simprints.id.services.scheduledSync.subjects.master.models.SubjectsSyncWorkerState.Running
import com.simprints.id.services.scheduledSync.subjects.master.models.SubjectsSyncWorkerState.Succeeded
import com.simprints.id.services.scheduledSync.subjects.master.models.SubjectsSyncWorkerType
import com.simprints.id.testtools.TestApplication
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.testtools.common.livedata.testObserver
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.io.IOException

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class SyncInformationViewModelTest {

    @MockK lateinit var subjectLocalDataSourceMock: SubjectLocalDataSource
    @MockK lateinit var preferencesManagerMock: PreferencesManager
    @MockK lateinit var subjectsDownSyncScopeRepositoryMock: SubjectsDownSyncScopeRepository
    @MockK lateinit var imageRepositoryMock: ImageRepository
    @MockK lateinit var subjectsSyncManagerMock: SubjectsSyncManager
    private lateinit var subjectRepositoryMock: SubjectRepository

    private val projectId = "projectId"
    private lateinit var viewModel: SyncInformationViewModel

    @ExperimentalCoroutinesApi
    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        UnitTestConfig(this).coroutinesMainThread()
        subjectRepositoryMock = mockk()
        viewModel = spyk(SyncInformationViewModel(subjectRepositoryMock, subjectLocalDataSourceMock,
            preferencesManagerMock, projectId, subjectsDownSyncScopeRepositoryMock,
            imageRepositoryMock, subjectsSyncManagerMock))
    }

    @Test
    fun syncInProgress_shouldHaveSyncingViewState() {
        every { subjectsSyncManagerMock.getLastSyncState() } returns flowOf(buildSubjectsSyncState(Running)).asLiveData()

        viewModel.updateSyncInfo()
        val testObserver = viewModel.getViewStateLiveData().testObserver()

        assertThat(testObserver.observedValues.last()).isEqualTo(SyncInformationActivity.ViewState.LoadingState.Syncing)
    }

    @Test
    fun syncComplete_shouldCallFetchRecords() {
        every { subjectsSyncManagerMock.getLastSyncState() } returns flowOf(buildSubjectsSyncState(Succeeded)).asLiveData()

        viewModel.updateSyncInfo()
        viewModel.getViewStateLiveData().testObserver()

        coVerify { viewModel.fetchRecords() }
    }

    @Test
    fun syncComplete_fetchFromRemoteAndLocalSucceeds_shouldHaveCorrectViewState() {
        val localCount = 322
        val imagesToUpload = 12
        val countInRemoteForCreate = 123
        val countInRemoteForDelete = 22
        val countInRemoteForMove = 0
        val subjectsCount = SubjectsCount(countInRemoteForCreate, countInRemoteForDelete, countInRemoteForMove)

        val moduleName = "module1"
        val moduleCount = listOf(ModuleCount(moduleName, localCount))

        every { preferencesManagerMock.subjectsDownSyncSetting } returns SubjectsDownSyncSetting.ON
        every { subjectsSyncManagerMock.getLastSyncState() } returns flowOf(buildSubjectsSyncState(Succeeded)).asLiveData()
        every { preferencesManagerMock.selectedModules } returns setOf(moduleName)
        coEvery { subjectLocalDataSourceMock.count(any()) } returns localCount
        every { imageRepositoryMock.getNumberOfImagesToUpload() } returns imagesToUpload
        coEvery { subjectRepositoryMock.countToDownSync(any()) } returns subjectsCount

        viewModel.updateSyncInfo()
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

        val moduleName = "module1"
        val moduleCount = listOf(ModuleCount(moduleName, localCount))

        every { preferencesManagerMock.subjectsDownSyncSetting } returns SubjectsDownSyncSetting.OFF
        every { subjectsSyncManagerMock.getLastSyncState() } returns flowOf(buildSubjectsSyncState(Succeeded)).asLiveData()
        every { preferencesManagerMock.selectedModules } returns setOf(moduleName)
        coEvery { subjectLocalDataSourceMock.count(any()) } returns localCount
        every { imageRepositoryMock.getNumberOfImagesToUpload() } returns imagesToUpload

        viewModel.updateSyncInfo()
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

        every { preferencesManagerMock.subjectsDownSyncSetting } returns SubjectsDownSyncSetting.ON
        every { subjectsSyncManagerMock.getLastSyncState() } returns flowOf(buildSubjectsSyncState(Succeeded)).asLiveData()
        every { preferencesManagerMock.selectedModules } returns setOf(moduleName)
        coEvery { subjectLocalDataSourceMock.count(any()) } returns localCount
        every { imageRepositoryMock.getNumberOfImagesToUpload() } returns imagesToUpload
        coEvery { subjectRepositoryMock.countToDownSync(any()) } throws IOException()

        viewModel.updateSyncInfo()
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

    private fun buildSubjectsSyncState(syncWorkerState: SubjectsSyncWorkerState) =
        SubjectsSyncState(syncId = "sync_id", progress = 1, total = 20,
            upSyncWorkersInfo = listOf(),
            downSyncWorkersInfo = listOf(
                SubjectsSyncState.SyncWorkerInfo(
                    SubjectsSyncWorkerType.DOWNLOADER, syncWorkerState
                )
            )
        )
}
