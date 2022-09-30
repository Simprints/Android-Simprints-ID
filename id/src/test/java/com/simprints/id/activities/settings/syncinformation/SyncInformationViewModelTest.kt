package com.simprints.id.activities.settings.syncinformation

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.google.common.truth.Truth.assertThat
import com.simprints.eventsystem.event.EventRepository
import com.simprints.eventsystem.event.domain.EventCount
import com.simprints.eventsystem.event.domain.models.EventType.*
import com.simprints.eventsystem.events_sync.down.EventDownSyncScopeRepository
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_MODULE_ID
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.eventsystem.sampledata.SampleDefaults.projectDownSyncScope
import com.simprints.id.activities.settings.syncinformation.modulecount.ModuleCount
import com.simprints.id.data.db.subject.SubjectRepository
import com.simprints.id.data.images.repository.ImageRepository
import com.simprints.id.data.prefs.IdPreferencesManager
import com.simprints.id.data.prefs.settings.canSyncDataToSimprints
import com.simprints.id.services.sync.events.down.EventDownSyncHelper
import com.simprints.id.services.sync.events.master.EventSyncManager
import com.simprints.id.services.sync.events.master.models.EventSyncState
import com.simprints.id.services.sync.events.master.models.EventSyncState.SyncWorkerInfo
import com.simprints.id.services.sync.events.master.models.EventSyncWorkerState
import com.simprints.id.services.sync.events.master.models.EventSyncWorkerState.*
import com.simprints.id.services.sync.events.master.models.EventSyncWorkerType.*
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.infra.images.ImageRepository
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.DeviceConfiguration
import com.simprints.infra.config.domain.models.SynchronizationConfiguration
import com.simprints.infra.config.domain.models.SynchronizationConfiguration.Frequency.ONLY_PERIODICALLY_UP_SYNC
import com.simprints.infra.config.domain.models.SynchronizationConfiguration.Frequency.PERIODICALLY
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.livedata.getOrAwaitValue
import io.mockk.*
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class SyncInformationViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    lateinit var configManager: ConfigManager

    @MockK
    lateinit var downSyncHelper: EventDownSyncHelper

    @MockK
    lateinit var eventRepository: EventRepository

    @MockK
    lateinit var subjectRepository: SubjectRepository

    @MockK
    lateinit var eventDownSyncScopeRepository: EventDownSyncScopeRepository

    @MockK
    lateinit var imageRepository: ImageRepository

    @MockK
    lateinit var eventSyncManager: EventSyncManager

    private val synchronizationConfiguration = mockk<SynchronizationConfiguration>()
    private val deviceConfiguration = mockk<DeviceConfiguration>()
    private val projectId = DEFAULT_PROJECT_ID
    private lateinit var viewModel: SyncInformationViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        UnitTestConfig().coroutinesMainThread()

        coEvery { configManager.getProjectConfiguration() } returns mockk {
            every { synchronization } returns synchronizationConfiguration
        }
        coEvery { configManager.getDeviceConfiguration() } returns deviceConfiguration

        viewModel = SyncInformationViewModel(
            downSyncHelper,
            eventRepository,
            subjectRepository,
            projectId,
            eventDownSyncScopeRepository,
            imageRepository,
            configManager,
            testCoroutineRule.testCoroutineDispatcher
        )
    }

    @Test
    fun `should initialize the synchronization live data`() {
        assertThat(viewModel.synchronizationConfiguration.getOrAwaitValue()).isEqualTo(
            synchronizationConfiguration
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

        every { synchronizationConfiguration.frequency } returns PERIODICALLY
        every { eventSyncManager.getLastSyncState() } returns MutableLiveData(
            buildSubjectsSyncState(
                Succeeded
            )
        )
        coEvery {
            eventDownSyncScopeRepository.getDownSyncScope(
                any(),
                any(),
                any()
            )
        } returns projectDownSyncScope
        every { deviceConfiguration.selectedModules } returns listOf(moduleName)
        coEvery { eventRepository.localCount(any()) } returns localCount
        coEvery { eventRepository.localCount(any(), ENROLMENT_V2) } returns localCount
        coEvery { eventRepository.localCount(any(), ENROLMENT_RECORD_CREATION) } returns 0
        coEvery { subjectRepository.count(any()) } returns localCount
        every { imageRepository.getNumberOfImagesToUpload() } returns imagesToUpload
        coEvery { downSyncHelper.countForDownSync(any()) } returns listOf(
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

        every { synchronizationConfiguration.frequency } returns ONLY_PERIODICALLY_UP_SYNC
        every { eventSyncManager.getLastSyncState() } returns MutableLiveData(
            buildSubjectsSyncState(
                Succeeded
            )
        )
        every { deviceConfiguration.selectedModules } returns listOf(moduleName)
        coEvery { eventRepository.localCount(any()) } returns localCount
        coEvery { eventRepository.localCount(any(), ENROLMENT_V2) } returns localCount
        coEvery { eventRepository.localCount(any(), ENROLMENT_RECORD_CREATION) } returns 0
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

        every { synchronizationConfiguration.frequency } returns PERIODICALLY
        every { eventSyncManager.getLastSyncState() } returns MutableLiveData(
            buildSubjectsSyncState(
                Succeeded
            )
        )
        every { deviceConfiguration.selectedModules } returns listOf(moduleName)
        coEvery { eventRepository.localCount(any()) } returns localCount
        coEvery { eventRepository.localCount(any(), ENROLMENT_V2) } returns localCount
        coEvery { eventRepository.localCount(any(), ENROLMENT_RECORD_CREATION) } returns 0
        coEvery { subjectRepository.count(any()) } returns localCount
        coEvery {
            eventDownSyncScopeRepository.getDownSyncScope(
                any(),
                any(),
                any()
            )
        } throws IOException()
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
    fun syncing_ShouldOnlyRequestCountForSelectedModules() {
        coEvery { eventRepository.localCount(any()) } returns 0
        coEvery { eventRepository.localCount(any(), ENROLMENT_V2) } returns 0
        coEvery { eventRepository.localCount(any(), ENROLMENT_RECORD_CREATION) } returns 0
        coEvery { subjectRepository.count(any()) } returns 0
        every { imageRepository.getNumberOfImagesToUpload() } returns 0

        viewModel.fetchSyncInformation()
        verify(exactly = 2) { deviceConfiguration.selectedModules }
    }

    @Test
    fun fetchSyncInformationIfNeeded_fetchesWhenWorkersAreDone() {
        every { eventSyncManager.getLastSyncState() } returns MutableLiveData(
            buildSubjectsSyncState(Succeeded)
        )
        every { deviceConfiguration.selectedModules } returns listOf(DEFAULT_MODULE_ID)

        viewModel.fetchSyncInformationIfNeeded(eventSyncManager.getLastSyncState().value!!)
        coVerify(exactly = 2) { subjectRepository.count(any()) }
    }

    @Test
    fun fetchSyncInformationIfNeeded_doesNotFetchWhenWorkersAreNotDone() {
        every { eventSyncManager.getLastSyncState() } returns MutableLiveData(
            buildInProgressSyncState()
        )
        every { deviceConfiguration.selectedModules } returns listOf(DEFAULT_MODULE_ID)

        viewModel.fetchSyncInformationIfNeeded(eventSyncManager.getLastSyncState().value!!)
        coVerify(exactly = 0) { subjectRepository.count(any()) }
    }

    @Test
    fun fetchSyncInformationIfNeeded_fetchesOnlyOnceWhenConsecutiveCallsWithSameSyncState() {
        every { eventSyncManager.getLastSyncState() } returns MutableLiveData(
            buildSubjectsSyncState(Succeeded)
        )
        every { deviceConfiguration.selectedModules } returns listOf(DEFAULT_MODULE_ID)

        viewModel.fetchSyncInformationIfNeeded(eventSyncManager.getLastSyncState().value!!)
        viewModel.fetchSyncInformationIfNeeded(eventSyncManager.getLastSyncState().value!!)
        viewModel.fetchSyncInformationIfNeeded(eventSyncManager.getLastSyncState().value!!)
        coVerify(exactly = 2) { subjectRepository.count(any()) }
    }

    private fun buildSubjectsSyncState(syncWorkerState: EventSyncWorkerState) =
        EventSyncState(
            syncId = "sync_id", progress = 1, total = 20,
            upSyncWorkersInfo = listOf(),
            downSyncWorkersInfo = listOf(
                SyncWorkerInfo(DOWNLOADER, syncWorkerState)
            )
        )

    private fun buildInProgressSyncState() =
        EventSyncState(
            syncId = "sync_id", progress = 1, total = 20,
            upSyncWorkersInfo = listOf(
                SyncWorkerInfo(UP_COUNTER, Succeeded),
                SyncWorkerInfo(UPLOADER, Enqueued)
            ),
            downSyncWorkersInfo = listOf(
                SyncWorkerInfo(DOWN_COUNTER, Succeeded),
                SyncWorkerInfo(DOWNLOADER, Running)
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
