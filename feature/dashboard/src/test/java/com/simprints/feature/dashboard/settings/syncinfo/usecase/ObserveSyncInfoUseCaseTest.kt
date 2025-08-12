package com.simprints.feature.dashboard.settings.syncinfo.usecase

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timer
import com.simprints.core.tools.time.Timestamp
import com.simprints.feature.dashboard.settings.syncinfo.SyncInfoModuleCount
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.store.models.DeviceConfiguration
import com.simprints.infra.config.store.models.GeneralConfiguration
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.store.models.ProjectState
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.config.store.models.isEventDownSyncAllowed
import com.simprints.infra.config.store.models.isModuleSelectionAvailable
import com.simprints.infra.config.store.models.isMissingModulesToChooseFrom
import com.simprints.infra.config.store.tokenization.TokenizationProcessor
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.enrolment.records.repository.EnrolmentRecordRepository
import com.simprints.infra.eventsync.EventSyncManager
import com.simprints.infra.eventsync.status.models.DownSyncCounts
import com.simprints.infra.eventsync.status.models.EventSyncState
import com.simprints.infra.images.ImageRepository
import com.simprints.infra.network.ConnectivityTracker
import com.simprints.infra.sync.SyncOrchestrator
import com.simprints.infra.sync.ImageSyncStatus
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ObserveSyncInfoUseCaseTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private val configManager = mockk<ConfigManager>()
    private val connectivityTracker = mockk<ConnectivityTracker>()
    private val enrolmentRecordRepository = mockk<EnrolmentRecordRepository>()
    private val authStore = mockk<AuthStore>()
    private val imageRepository = mockk<ImageRepository>()
    private val eventSyncManager = mockk<EventSyncManager>()
    private val syncOrchestrator = mockk<SyncOrchestrator>()
    private val tokenizationProcessor = mockk<TokenizationProcessor>()
    private val timeHelper = mockk<TimeHelper>()
    private val timer = mockk<Timer>()

    private lateinit var useCase: ObserveSyncInfoUseCase

    private companion object {
        const val TEST_PROJECT_ID = "test_project_id"
        const val TEST_USER_ID = "test_user_id"
        const val TEST_MODULE_NAME = "test_module"
        val TEST_TIMESTAMP = Timestamp(1000L)
    }

    private val mockProjectConfiguration = mockk<ProjectConfiguration>(relaxed = true) {
        every { general } returns mockk<GeneralConfiguration>(relaxed = true) {
            every { modalities } returns emptyList()
        }
    }
    private val mockDeviceConfiguration = mockk<DeviceConfiguration>(relaxed = true) {
        every { selectedModules } returns emptyList()
    }
    private val mockProject = mockk<Project>(relaxed = true) {
        every { state } returns ProjectState.RUNNING
    }
    private val mockEventSyncState = mockk<EventSyncState>(relaxed = true) {
        every { isSyncCompleted() } returns false
        every { isSyncInProgress() } returns false
        every { isSyncConnecting() } returns false
        every { isSyncRunning() } returns false
        every { isSyncFailed() } returns false
        every { isSyncFailedBecauseReloginRequired() } returns false
        every { isSyncFailedBecauseBackendMaintenance() } returns false
        every { isSyncFailedBecauseTooManyRequests() } returns false
        every { getEstimatedBackendMaintenanceOutage() } returns null
        every { isThereNotSyncHistory() } returns false
        every { progress } returns null
        every { total } returns null
    }
    private val mockImageSyncStatus = mockk<ImageSyncStatus>(relaxed = true) {
        every { isSyncing } returns false
        every { progress } returns null
        every { secondsSinceLastUpdate } returns null
    }

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        mockkStatic("androidx.lifecycle.FlowLiveDataConversions")
        mockkStatic("com.simprints.infra.config.store.models.ProjectConfigurationKt")
        mockkStatic("com.simprints.core.tools.extentions.Flow_extKt")
        setupDefaultMocks()
        createUseCase()
    }

    private fun setupDefaultMocks() {
        every { authStore.signedInProjectId } returns TEST_PROJECT_ID
        every { authStore.signedInUserId } returns TokenizableString.Raw(TEST_USER_ID)
        every { authStore.observeSignedInProjectId() } returns MutableStateFlow(TEST_PROJECT_ID)

        val connectivityLiveData = MutableLiveData(true)
        every { connectivityTracker.observeIsConnected() } returns connectivityLiveData
        every { connectivityLiveData.asFlow() } returns flowOf(true)

        every { configManager.observeIsProjectRefreshing() } returns MutableStateFlow(false)
        every { configManager.observeProjectConfiguration() } returns MutableStateFlow(mockProjectConfiguration)
        every { configManager.observeDeviceConfiguration() } returns MutableStateFlow(mockDeviceConfiguration)
        coEvery { configManager.getProjectConfiguration() } returns mockProjectConfiguration
        coEvery { configManager.getDeviceConfiguration() } returns mockDeviceConfiguration
        coEvery { configManager.getProject(any()) } returns mockProject

        val eventSyncLiveData = MutableLiveData(mockEventSyncState)
        every { eventSyncManager.getLastSyncState() } returns eventSyncLiveData
        every { eventSyncManager.getLastSyncState(any()) } returns eventSyncLiveData
        every { eventSyncLiveData.asFlow() } returns flowOf(mockEventSyncState)
        coEvery { eventSyncManager.getLastSyncTime() } returns TEST_TIMESTAMP
        coEvery { eventSyncManager.countEventsToUpload(any()) } returns flowOf(0)
        coEvery { eventSyncManager.countEventsToDownload(any()) } returns DownSyncCounts(0, isLowerBound = false)

        every { syncOrchestrator.observeImageSyncStatus() } returns MutableStateFlow(mockImageSyncStatus)
        coEvery { syncOrchestrator.startEventSync(any()) } returns Unit
        coEvery { syncOrchestrator.stopEventSync() } returns Unit
        coEvery { syncOrchestrator.startImageSync() } returns Unit
        coEvery { syncOrchestrator.stopImageSync() } returns Unit

        coEvery { imageRepository.getNumberOfImagesToUpload(any()) } returns 0
        coEvery { enrolmentRecordRepository.count(any()) } returns 0

        every { timer.observeTickOncePerMinute() } returns MutableStateFlow(Unit)
        every { timeHelper.now() } returns TEST_TIMESTAMP
        every { timeHelper.msBetweenNowAndTime(any()) } returns 0L
        every { timeHelper.readableBetweenNowAndTime(any()) } returns "0 minutes ago"

        every { tokenizationProcessor.decrypt(any(), any(), any()) } returns TokenizableString.Raw("decrypted_module")

        every { any<ProjectConfiguration>().isModuleSelectionAvailable() } returns false
        every { any<ProjectConfiguration>().isEventDownSyncAllowed() } returns true
        every { any<ProjectConfiguration>().isMissingModulesToChooseFrom() } returns false
    }

    private fun createUseCase() {
        useCase = ObserveSyncInfoUseCase(
            configManager = configManager,
            connectivityTracker = connectivityTracker,
            enrolmentRecordRepository = enrolmentRecordRepository,
            authStore = authStore,
            imageRepository = imageRepository,
            eventSyncManager = eventSyncManager,
            syncOrchestrator = syncOrchestrator,
            tokenizationProcessor = tokenizationProcessor,
            timeHelper = timeHelper,
            timer = timer,
        )
    }

    @Test
    fun `should not show re-login prompt when sync has not failed due to authentication`() = runTest {
        val mockNormalEventSyncState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncFailedBecauseReloginRequired() } returns false
        }
        every { eventSyncManager.getLastSyncState(any()) } returns MutableLiveData(mockNormalEventSyncState)
        createUseCase()

        val result = useCase().first()

        assertThat(result.isLoginPromptSectionVisible).isFalse()
    }

    @Test
    fun `should show configuration loading when project is refreshing`() = runTest {
        every { configManager.observeIsProjectRefreshing() } returns MutableStateFlow(true)
        createUseCase()

        val result = useCase().first()

        assertThat(result.isConfigurationLoadingProgressBarVisible).isTrue()
    }

    @Test
    fun `should show re-login prompt when sync failed due to authentication required`() = runTest {
        val mockFailedEventSyncState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncFailedBecauseReloginRequired() } returns true
        }
        every { eventSyncManager.getLastSyncState(any()) } returns MutableLiveData(mockFailedEventSyncState)
        createUseCase()

        val result = useCase().first()

        assertThat(result.isLoginPromptSectionVisible).isTrue()
    }

    @Test
    fun `should show re-login prompt correctly based on pre-logout mode`() = runTest {
        val mockFailedEventSyncState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncFailedBecauseReloginRequired() } returns true
        }
        every { eventSyncManager.getLastSyncState(any()) } returns MutableLiveData(mockFailedEventSyncState)
        createUseCase()

        val result = useCase(isPreLogoutUpSync = true /* This should hide the login prompt */).first()

        assertThat(result.isLoginPromptSectionVisible).isFalse()
    }

    @Test
    fun `should handle project state correctly in sync info`() = runTest {
        val mockEndingProject = mockk<Project> {
            every { state } returns ProjectState.PROJECT_ENDING
        }
        coEvery { configManager.getProject(any()) } returns mockEndingProject
        createUseCase()

        val result = useCase().first()

        assertThat(result.syncInfoSectionRecords.isCounterRecordsToDownloadVisible).isFalse()
    }

    @Test
    fun `should show correct login prompt visibility when not logged in`() = runTest {
        every { authStore.observeSignedInProjectId() } returns MutableStateFlow("")
        createUseCase()

        val result = useCase().first()

        assertThat(result.isLoggedIn).isFalse()
    }

    // Section-specific tests

    @Test
    fun `should emit SyncInfo with correct syncInfoSectionRecords instruction visibility`() = runTest {
        val mockOfflineEventSyncState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncFailed() } returns false
            every { isSyncInProgress() } returns false
        }
        every { eventSyncManager.getLastSyncState(any()) } returns MutableLiveData(mockOfflineEventSyncState)
        every { connectivityTracker.observeIsConnected().asFlow() } returns flowOf(false)
        createUseCase()

        val result = useCase().first()

        assertThat(result.syncInfoSectionRecords.isInstructionOfflineVisible).isTrue()
        assertThat(result.syncInfoSectionRecords.isInstructionErrorVisible).isFalse()
        assertThat(result.syncInfoSectionRecords.isInstructionDefaultVisible).isFalse()
    }

    @Test
    fun `should emit SyncInfo with correct syncInfoSectionRecords button states`() = runTest {
        val mockNormalEventSyncState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncRunning() } returns false
            every { isSyncFailedBecauseReloginRequired() } returns false
            every { isSyncFailed() } returns false
        }
        every { eventSyncManager.getLastSyncState(any()) } returns MutableLiveData(mockNormalEventSyncState)
        every { connectivityTracker.observeIsConnected().asFlow() } returns flowOf(true)
        createUseCase()

        val result = useCase().first()

        assertThat(result.syncInfoSectionRecords.isSyncButtonEnabled).isTrue()
        assertThat(result.syncInfoSectionRecords.isSyncButtonVisible).isTrue()
        assertThat(result.syncInfoSectionRecords.isSyncButtonForRetry).isFalse()
    }

    @Test
    fun `should emit SyncInfo with correct syncInfoSectionRecords footer states`() = runTest {
        val mockCompletedEventSyncState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncInProgress() } returns false
        }
        every { eventSyncManager.getLastSyncState(any()) } returns MutableLiveData(mockCompletedEventSyncState)
        coEvery { eventSyncManager.getLastSyncTime() } returns TEST_TIMESTAMP
        createUseCase()

        val result = useCase().first()

        assertThat(result.syncInfoSectionRecords.isFooterLastSyncTimeVisible).isTrue()
        assertThat(result.syncInfoSectionRecords.footerLastSyncMinutesAgo).isEqualTo("0 minutes ago")
        assertThat(result.syncInfoSectionRecords.isFooterSyncInProgressVisible).isFalse()
    }

    @Test
    fun `should emit SyncInfo with correct syncInfoSectionImages instruction visibility`() = runTest {
        val mockNotSyncingImageStatus = mockk<ImageSyncStatus>(relaxed = true) {
            every { isSyncing } returns false
            every { progress } returns null
        }
        every { syncOrchestrator.observeImageSyncStatus() } returns MutableStateFlow(mockNotSyncingImageStatus)
        every { connectivityTracker.observeIsConnected().asFlow() } returns flowOf(false)
        createUseCase()

        val result = useCase().first()

        assertThat(result.syncInfoSectionImages.isInstructionOfflineVisible).isTrue()
        assertThat(result.syncInfoSectionImages.isInstructionDefaultVisible).isFalse()
    }

    @Test
    fun `should emit SyncInfo with correct syncInfoSectionImages button states`() = runTest {
        val mockNormalEventSyncState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncFailedBecauseReloginRequired() } returns false
        }
        every { eventSyncManager.getLastSyncState(any()) } returns MutableLiveData(mockNormalEventSyncState)
        every { connectivityTracker.observeIsConnected().asFlow() } returns flowOf(true)
        createUseCase()

        val result = useCase().first()

        assertThat(result.syncInfoSectionImages.isSyncButtonEnabled).isTrue()
    }

    @Test
    fun `should emit SyncInfo with correct syncInfoSectionImages footer states`() = runTest {
        val mockImageStatusWithLastSync = mockk<ImageSyncStatus>(relaxed = true) {
            every { isSyncing } returns false
            every { progress } returns null
            every { secondsSinceLastUpdate } returns 120 // 2 minutes
        }
        every { syncOrchestrator.observeImageSyncStatus() } returns MutableStateFlow(mockImageStatusWithLastSync)
        every { timeHelper.readableBetweenNowAndTime(Timestamp(120 * 1000)) } returns "2 minutes ago"
        createUseCase()

        val result = useCase().first()

        assertThat(result.syncInfoSectionImages.isFooterLastSyncTimeVisible).isTrue()
        assertThat(result.syncInfoSectionImages.footerLastSyncMinutesAgo).isEqualTo("2 minutes ago")
    }

    @Test
    fun `should emit SyncInfo with correct syncInfoSectionModules data`() = runTest {
        val mockProjectConfigWithModules = mockk<ProjectConfiguration> {
            every { general } returns mockk<GeneralConfiguration> {
                every { modalities } returns listOf(GeneralConfiguration.Modality.FINGERPRINT)
            }
        }
        val mockDeviceConfigWithModules = mockk<DeviceConfiguration> {
            every { selectedModules } returns listOf(TokenizableString.Raw(TEST_MODULE_NAME))
        }
        every { configManager.observeProjectConfiguration() } returns MutableStateFlow(mockProjectConfigWithModules)
        every { configManager.observeDeviceConfiguration() } returns MutableStateFlow(mockDeviceConfigWithModules)
        coEvery { configManager.getProjectConfiguration() } returns mockProjectConfigWithModules
        coEvery { configManager.getDeviceConfiguration() } returns mockDeviceConfigWithModules
        coEvery { enrolmentRecordRepository.count(any()) } returns 50
        every { mockProjectConfigWithModules.isModuleSelectionAvailable() } returns true
        createUseCase()

        val result = useCase().first()

        assertThat(result.syncInfoSectionModules.isSectionAvailable).isTrue()
        assertThat(result.syncInfoSectionModules.moduleCounts).hasSize(2) // total + module
        assertThat(result.syncInfoSectionModules.moduleCounts[0].isTotal).isTrue()
        assertThat(result.syncInfoSectionModules.moduleCounts[0].count).isEqualTo("50")
    }

    // Progress calculation tests

    @Test
    fun `should calculate correct event sync progress when sync in progress`() = runTest {
        val mockInProgressEventSyncState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncInProgress() } returns true
            every { isSyncCompleted() } returns false
            every { progress } returns 5
            every { total } returns 10
        }
        every { eventSyncManager.getLastSyncState(any()) } returns MutableLiveData(mockInProgressEventSyncState)
        createUseCase()

        val result = useCase().first()

        assertThat(result.syncInfoSectionRecords.isProgressVisible).isTrue()
        assertThat(result.syncInfoSectionRecords.progress.progressBarPercentage).isEqualTo(50) // half
    }

    @Test
    fun `should calculate correct event sync progress when sync connecting`() = runTest {
        val mockConnectingEventSyncState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncConnecting() } returns true
            every { isSyncInProgress() } returns false
            every { isSyncCompleted() } returns false
            every { isThereNotSyncHistory() } returns false
        }
        every { eventSyncManager.getLastSyncState(any()) } returns MutableLiveData(mockConnectingEventSyncState)
        createUseCase()

        val result = useCase().first()

        assertThat(result.syncInfoSectionRecords.progress.progressBarPercentage).isEqualTo(0) // not started
    }

    @Test
    fun `should calculate correct event sync progress when sync approached completion`() = runTest {
        val mockCompletedEventSyncState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncInProgress() } returns true
            every { progress } returns 10
            every { total } returns 10
        }
        every { eventSyncManager.getLastSyncState(any()) } returns MutableLiveData(mockCompletedEventSyncState)
        createUseCase()

        val result = useCase().first()

        assertThat(result.syncInfoSectionRecords.progress.progressBarPercentage).isEqualTo(100)
    }

    @Test
    fun `should not show event sync progress when sync completed`() = runTest {
        val mockCompletedEventSyncState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncCompleted() } returns true
        }
        every { eventSyncManager.getLastSyncState(any()) } returns MutableLiveData(mockCompletedEventSyncState)
        createUseCase()

        val result = useCase().first()

        assertThat(result.syncInfoSectionRecords.isProgressVisible).isFalse()
    }

    @Test
    fun `should calculate correct combined progress during pre-logout sync events phase`() = runTest {
        val mockInProgressEventSyncState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncInProgress() } returns true
            every { isSyncCompleted() } returns false
            every { progress } returns 3
            every { total } returns 6
        }
        val mockNotSyncingImageStatus = mockk<ImageSyncStatus>(relaxed = true) {
            every { isSyncing } returns false
            every { progress } returns null
        }
        every { eventSyncManager.getLastSyncState(any()) } returns MutableLiveData(mockInProgressEventSyncState)
        every { syncOrchestrator.observeImageSyncStatus() } returns MutableStateFlow(mockNotSyncingImageStatus)
        createUseCase()

        val result = useCase(isPreLogoutUpSync = true).first()

        // 50% of the first half (0-50%) of scale dedicated to the records, so 25% total
        assertThat(result.syncInfoSectionRecords.progress.progressBarPercentage).isEqualTo(25)
    }

    @Test
    fun `should calculate correct combined progress during pre-logout sync images phase`() = runTest {
        val mockCompletedEventSyncState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncCompleted() } returns true
            every { isSyncInProgress() } returns false
        }
        val mockSyncingImageStatus = mockk<ImageSyncStatus>(relaxed = true) {
            every { isSyncing } returns true
            every { progress } returns Pair(2, 4) // 2 out of 4 images
        }
        every { eventSyncManager.getLastSyncState(any()) } returns MutableLiveData(mockCompletedEventSyncState)
        every { syncOrchestrator.observeImageSyncStatus() } returns MutableStateFlow(mockSyncingImageStatus)
        createUseCase()

        val result = useCase(isPreLogoutUpSync = true).first()

        // 50% of the second half (50-75%) of scale dedicated to the images, so 75% total
        assertThat(result.syncInfoSectionRecords.progress.progressBarPercentage).isEqualTo(75)
    }

    @Test
    fun `should calculate correct image sync progress when images syncing`() = runTest {
        val mockSyncingImageStatus = mockk<ImageSyncStatus>(relaxed = true) {
            every { isSyncing } returns true
            every { progress } returns Pair(3, 10) // 3 out of 10 images
        }
        every { syncOrchestrator.observeImageSyncStatus() } returns MutableStateFlow(mockSyncingImageStatus)
        createUseCase()

        val result = useCase().first()

        assertThat(result.syncInfoSectionImages.isProgressVisible).isTrue()
        assertThat(result.syncInfoSectionImages.progress.progressBarPercentage).isEqualTo(30)
    }

    @Test
    fun `should calculate correct image sync progress when images not syncing`() = runTest {
        val mockNotSyncingImageStatus = mockk<ImageSyncStatus>(relaxed = true) {
            every { isSyncing } returns false
            every { progress } returns null
        }
        every { syncOrchestrator.observeImageSyncStatus() } returns MutableStateFlow(mockNotSyncingImageStatus)
        coEvery { imageRepository.getNumberOfImagesToUpload(any()) } returns 0
        createUseCase()

        val result = useCase().first()

        assertThat(result.syncInfoSectionImages.isProgressVisible).isFalse()
        assertThat(result.syncInfoSectionImages.progress.progressBarPercentage).isEqualTo(0)
    }

    // Counter tests

    @Test
    fun `should emit SyncInfo with correct record counters when sync not in progress`() = runTest {
        val mockIdleEventSyncState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncInProgress() } returns false
            every { isSyncRunning() } returns false
        }
        every { eventSyncManager.getLastSyncState(any()) } returns MutableLiveData(mockIdleEventSyncState)
        coEvery { enrolmentRecordRepository.count(any()) } returns 25
        coEvery { eventSyncManager.countEventsToUpload(any()) } returns flowOf(5)
        coEvery { eventSyncManager.countEventsToDownload(any()) } returns DownSyncCounts(8, isLowerBound = false)
        createUseCase()

        val result = useCase().first()

        assertThat(result.syncInfoSectionRecords.counterTotalRecords).isEqualTo("25")
        assertThat(result.syncInfoSectionRecords.counterRecordsToUpload).isEqualTo("5")
        assertThat(result.syncInfoSectionRecords.counterRecordsToDownload).isEqualTo("8")
    }

    @Test
    fun `should emit SyncInfo with empty record counters when sync in progress`() = runTest {
        val mockInProgressEventSyncState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncInProgress() } returns true
        }
        every { eventSyncManager.getLastSyncState(any()) } returns MutableLiveData(mockInProgressEventSyncState)
        createUseCase()

        val result = useCase().first()

        assertThat(result.syncInfoSectionRecords.counterTotalRecords).isEmpty()
        assertThat(result.syncInfoSectionRecords.counterRecordsToUpload).isEmpty()
        assertThat(result.syncInfoSectionRecords.counterRecordsToDownload).isEmpty()
    }

    @Test
    fun `should emit SyncInfo with correct images to upload counter when sync not in progress`() = runTest {
        val mockNotSyncingImageStatus = mockk<ImageSyncStatus>(relaxed = true) {
            every { isSyncing } returns false
            every { progress } returns null
        }
        every { syncOrchestrator.observeImageSyncStatus() } returns MutableStateFlow(mockNotSyncingImageStatus)
        coEvery { imageRepository.getNumberOfImagesToUpload(any()) } returns 15
        createUseCase()

        val result = useCase().first()

        assertThat(result.syncInfoSectionRecords.counterImagesToUpload).isEqualTo("15") // may be shown within records
        assertThat(result.syncInfoSectionImages.counterImagesToUpload).isEqualTo("15")
    }

    @Test
    fun `should emit SyncInfo with empty images counter when sync in progress`() = runTest {
        val mockSyncingImageStatus = mockk<ImageSyncStatus>(relaxed = true) {
            every { isSyncing } returns true
            every { progress } returns null
        }
        every { syncOrchestrator.observeImageSyncStatus() } returns MutableStateFlow(mockSyncingImageStatus)
        createUseCase()

        val result = useCase().first()

        assertThat(result.syncInfoSectionRecords.counterImagesToUpload).isEmpty() // may be shown within records
        assertThat(result.syncInfoSectionImages.counterImagesToUpload).isEmpty()
    }

    @Test
    fun `should emit SyncInfo with correct module counts when modules selected`() = runTest {
        val mockProjectConfigWithModules = mockk<ProjectConfiguration> {
            every { general } returns mockk<GeneralConfiguration> {
                every { modalities } returns listOf(GeneralConfiguration.Modality.FINGERPRINT)
            }
        }
        val mockDeviceConfigWithModules = mockk<DeviceConfiguration> {
            every { selectedModules } returns listOf(TokenizableString.Raw("module_1"), TokenizableString.Raw("module_2"))
        }
        every { configManager.observeProjectConfiguration() } returns MutableStateFlow(mockProjectConfigWithModules)
        every { configManager.observeDeviceConfiguration() } returns MutableStateFlow(mockDeviceConfigWithModules)
        coEvery { configManager.getProjectConfiguration() } returns mockProjectConfigWithModules
        coEvery { configManager.getDeviceConfiguration() } returns mockDeviceConfigWithModules
        coEvery { enrolmentRecordRepository.count(any()) } returnsMany listOf(10, 15, 25) // records total, module_1, module_2
        every { mockProjectConfigWithModules.isModuleSelectionAvailable() } returns true
        createUseCase()

        val result = useCase().first()

        assertThat(result.syncInfoSectionModules.isSectionAvailable).isTrue()
        assertThat(result.syncInfoSectionModules.moduleCounts).hasSize(3) // sum of modules + the 2 modules
        // sum of modules
        assertThat(result.syncInfoSectionModules.moduleCounts[0].isTotal).isTrue()
        assertThat(result.syncInfoSectionModules.moduleCounts[0].count).isEqualTo("40")
        // module_1
        assertThat(result.syncInfoSectionModules.moduleCounts[1]).isEqualTo(
            SyncInfoModuleCount(isTotal = false, name = "module_1", count = "15")
        )
        // module_2
        assertThat(result.syncInfoSectionModules.moduleCounts[2]).isEqualTo(
            SyncInfoModuleCount(isTotal = false, name = "module_2", count = "25")
        )
    }

    @Test
    fun `should emit SyncInfo with empty module counts when no modules selected`() = runTest {
        val mockProjectConfigWithoutModules = mockk<ProjectConfiguration> {
            every { general } returns mockk<GeneralConfiguration> {
                every { modalities } returns emptyList()
            }
        }
        val mockDeviceConfigWithoutModules = mockk<DeviceConfiguration> {
            every { selectedModules } returns emptyList()
        }
        every { configManager.observeProjectConfiguration() } returns MutableStateFlow(mockProjectConfigWithoutModules)
        every { configManager.observeDeviceConfiguration() } returns MutableStateFlow(mockDeviceConfigWithoutModules)
        coEvery { configManager.getProjectConfiguration() } returns mockProjectConfigWithoutModules
        coEvery { configManager.getDeviceConfiguration() } returns mockDeviceConfigWithoutModules
        every { mockProjectConfigWithoutModules.isModuleSelectionAvailable() } returns false
        createUseCase()

        val result = useCase().first()

        assertThat(result.syncInfoSectionModules.isSectionAvailable).isFalse()
        assertThat(result.syncInfoSectionModules.moduleCounts).isEmpty()
    }

    @Test
    fun `should emit SyncInfo with correct records to download counter visible when allowed`() = runTest {
        val mockProjectConfigWithDownSync = mockk<ProjectConfiguration> {
            every { general } returns mockk<GeneralConfiguration> {
                every { modalities } returns emptyList()
            }
        }
        val mockIdleEventSyncState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncInProgress() } returns false
        }

        every { eventSyncManager.getLastSyncState(any()) } returns MutableLiveData(mockIdleEventSyncState)
        every { configManager.observeProjectConfiguration() } returns MutableStateFlow(mockProjectConfigWithDownSync)
        coEvery { configManager.getProjectConfiguration() } returns mockProjectConfigWithDownSync
        coEvery { eventSyncManager.countEventsToDownload(any()) } returns DownSyncCounts(42, isLowerBound = false)
        every { mockProjectConfigWithDownSync.isEventDownSyncAllowed() } returns true
        every { mockProjectConfigWithDownSync.isModuleSelectionAvailable() } returns false
        createUseCase()

        val result = useCase().first()

        assertThat(result.syncInfoSectionRecords.isCounterRecordsToDownloadVisible).isTrue()
        assertThat(result.syncInfoSectionRecords.counterRecordsToDownload).isEqualTo("42")
    }

    @Test
    fun `should emit SyncInfo with hidden records to download counter when pre-logout mode`() = runTest {
        val mockIdleEventSyncState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncInProgress() } returns false
        }

        every { eventSyncManager.getLastSyncState(any()) } returns MutableLiveData(mockIdleEventSyncState)
        createUseCase()

        val result = useCase(isPreLogoutUpSync = true).first()

        assertThat(result.syncInfoSectionRecords.isCounterRecordsToDownloadVisible).isFalse()
    }

    @Test
    fun `should handle timeout when counting records to download`() = runTest {
        val mockProjectConfigWithDownSync = mockk<ProjectConfiguration> {
            every { general } returns mockk<GeneralConfiguration> {
                every { modalities } returns emptyList()
            }
        }
        val mockIdleEventSyncState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncInProgress() } returns false
        }

        every { eventSyncManager.getLastSyncState(any()) } returns MutableLiveData(mockIdleEventSyncState)
        every { configManager.observeProjectConfiguration() } returns MutableStateFlow(mockProjectConfigWithDownSync)
        coEvery { configManager.getProjectConfiguration() } returns mockProjectConfigWithDownSync
        coEvery { eventSyncManager.countEventsToDownload(any()) } throws Exception("Timeout")
        every { mockProjectConfigWithDownSync.isEventDownSyncAllowed() } returns true
        every { mockProjectConfigWithDownSync.isModuleSelectionAvailable() } returns false
        createUseCase()

        val result = useCase().first()

        assertThat(result.syncInfoSectionRecords.counterRecordsToDownload).isEqualTo("0")
    }

    @Test
    fun `should handle when records download counting throws exception`() = runTest {
        val mockProjectConfigWithDownSync = mockk<ProjectConfiguration> {
            every { general } returns mockk<GeneralConfiguration> {
                every { modalities } returns emptyList()
            }
        }
        val mockIdleEventSyncState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncInProgress() } returns false
        }
        every { eventSyncManager.getLastSyncState(any()) } returns MutableLiveData(mockIdleEventSyncState)
        every { configManager.observeProjectConfiguration() } returns MutableStateFlow(mockProjectConfigWithDownSync)
        coEvery { configManager.getProjectConfiguration() } returns mockProjectConfigWithDownSync
        coEvery { eventSyncManager.countEventsToDownload(any()) } throws RuntimeException("Network error")
        every { mockProjectConfigWithDownSync.isEventDownSyncAllowed() } returns true
        every { mockProjectConfigWithDownSync.isModuleSelectionAvailable() } returns false
        createUseCase()

        val result = useCase().first()

        assertThat(result.syncInfoSectionRecords.counterRecordsToDownload).isEqualTo("0")
    }

    @Test
    fun `should handle network errors indication`() = runTest {
        val connectivityFlow = MutableStateFlow(false) // start offline
        every { connectivityTracker.observeIsConnected().asFlow() } returns connectivityFlow
        createUseCase()

        val offlineResult = useCase().first()

        assertThat(offlineResult.syncInfoSectionRecords.isInstructionOfflineVisible).isTrue()
        assertThat(offlineResult.syncInfoSectionRecords.isSyncButtonEnabled).isFalse()
        assertThat(offlineResult.syncInfoSectionImages.isSyncButtonEnabled).isFalse()

        connectivityFlow.value = true

        val onlineResult = useCase().first()

        assertThat(onlineResult.syncInfoSectionRecords.isInstructionOfflineVisible).isFalse()
        assertThat(onlineResult.syncInfoSectionRecords.isSyncButtonEnabled).isTrue()
        assertThat(onlineResult.syncInfoSectionImages.isSyncButtonEnabled).isTrue()
    }

    // Flow combination tests

    @Test
    fun `should handle changes in connectivity stream`() = runTest {
        val connectivityFlow = MutableStateFlow(false) // started offline
        every { connectivityTracker.observeIsConnected().asFlow() } returns connectivityFlow
        createUseCase()

        val offlineResult = useCase().first()

        assertThat(offlineResult.syncInfoSectionRecords.isInstructionOfflineVisible).isTrue()

        connectivityFlow.value = true // changed to online

        val onlineResult = useCase().first()

        assertThat(onlineResult.syncInfoSectionRecords.isInstructionOfflineVisible).isFalse()
    }

    @Test
    fun `should handle changes in auth stream`() = runTest {
        val authFlow = MutableStateFlow("") // started not signed in
        every { authStore.observeSignedInProjectId() } returns authFlow
        createUseCase()

        val loggedOutResult = useCase().first()

        assertThat(loggedOutResult.isLoggedIn).isFalse()

        authFlow.value = TEST_PROJECT_ID // changed to signed in

        val loggedInResult = useCase().first()

        assertThat(loggedInResult.isLoggedIn).isTrue()
    }

    @Test
    fun `should handle changes in project refreshing stream`() = runTest {
        val refreshingFlow = MutableStateFlow(false) // started non refreshing
        every { configManager.observeIsProjectRefreshing() } returns refreshingFlow
        createUseCase()

        val notRefreshingResult = useCase().first()

        assertThat(notRefreshingResult.isConfigurationLoadingProgressBarVisible).isFalse()

        refreshingFlow.value = true // changed to refreshing

        val refreshingResult = useCase().first()

        assertThat(refreshingResult.isConfigurationLoadingProgressBarVisible).isTrue()
    }

    @Test
    fun `should handle changes in event sync state stream`() = runTest {
        val eventSyncStateFlow = MutableLiveData<EventSyncState>()
        every { eventSyncManager.getLastSyncState(any()) } returns eventSyncStateFlow
        createUseCase()
        val mockIdleState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncInProgress() } returns false
        }
        eventSyncStateFlow.value = mockIdleState // started not syncing

        val idleResult = useCase().first()

        assertThat(idleResult.syncInfoSectionRecords.isProgressVisible).isFalse()

        val mockSyncingState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncInProgress() } returns true
            every { progress } returns 1
            every { total } returns 2
        }
        eventSyncStateFlow.value = mockSyncingState // changed to syncing

        val syncingResult = useCase().first()

        assertThat(syncingResult.syncInfoSectionRecords.isProgressVisible).isTrue()
    }

    @Test
    fun `should handle changes in image sync status stream`() = runTest {
        val imageSyncStatusFlow = MutableStateFlow<ImageSyncStatus>(mockk {
            every { isSyncing } returns false
            every { progress } returns null
            every { secondsSinceLastUpdate } returns null
        })  // started not syncing
        every { syncOrchestrator.observeImageSyncStatus() } returns imageSyncStatusFlow
        createUseCase()

        val notSyncingResult = useCase().first()

        assertThat(notSyncingResult.syncInfoSectionImages.isProgressVisible).isFalse()

        imageSyncStatusFlow.value = mockk {
            every { isSyncing } returns true
            every { progress } returns Pair(1, 2)
            every { secondsSinceLastUpdate } returns null
        } // changed to syncing

        val syncingResult = useCase().first()

        assertThat(syncingResult.syncInfoSectionImages.isProgressVisible).isTrue()
    }

    @Test
    fun `should handle changes in project config stream`() = runTest {
        val projectConfigFlow = MutableStateFlow(mockProjectConfiguration)
        every { configManager.observeProjectConfiguration() } returns projectConfigFlow // started without modules
        createUseCase()

        val initialResult = useCase().first()

        assertThat(initialResult.syncInfoSectionModules.isSectionAvailable).isFalse()

        val mockConfigWithModules = mockk<ProjectConfiguration> {
            every { general } returns mockk<GeneralConfiguration> {
                every { modalities } returns listOf(GeneralConfiguration.Modality.FINGERPRINT)
            }
        }
        every { mockConfigWithModules.isModuleSelectionAvailable() } returns true
        projectConfigFlow.value = mockConfigWithModules // now with modules

        val moduleConfigResult = useCase().first()

        assertThat(moduleConfigResult.syncInfoSectionModules.isSectionAvailable).isTrue()
    }

    @Test
    fun `should handle changes in device config stream`() = runTest {
        every { configManager.observeProjectConfiguration() } returns flowOf(
            mockk<ProjectConfiguration> {
                every { general } returns mockk<GeneralConfiguration> {
                    every { modalities } returns emptyList()
                }
            }
        )
        val deviceConfigFlow = MutableStateFlow(
            mockk<DeviceConfiguration>(relaxed = true) {
                every { selectedModules } returns emptyList()
            }
        ) // started without selected modules
        every { configManager.observeDeviceConfiguration() } returns deviceConfigFlow
        createUseCase()

        val noModulesResult = useCase().first()

        assertThat(noModulesResult.syncInfoSectionModules.moduleCounts).isEmpty()

        deviceConfigFlow.emit(
            mockk<DeviceConfiguration>(relaxed = true) {
                every { selectedModules } returns listOf(TokenizableString.Raw(TEST_MODULE_NAME))
            }
        ) // now with selected modules

        val withModulesResult = useCase().first()

        assertThat(withModulesResult.syncInfoSectionModules.moduleCounts).isNotEmpty()
    }

    @Test
    fun `should handle changes in time pacing stream`() = runTest {
        val mockIdleEventSyncState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncRunning() } returns false
        }
        every { eventSyncManager.getLastSyncState() } returns MutableLiveData(mockIdleEventSyncState)
        coEvery { eventSyncManager.getLastSyncTime() } returns TEST_TIMESTAMP
        every { timeHelper.now() } returnsMany listOf(TEST_TIMESTAMP, Timestamp(TEST_TIMESTAMP.ms + 60_000))
        every { timeHelper.readableBetweenNowAndTime(any()) } returnsMany listOf("0 minutes ago", "1 minute ago")
        // MutableStateFlow of Unit won't emit another (identical) Unit, so we'll count minutes and map to Units
        val timePaceFlow = MutableStateFlow(0)
        every { timer.observeTickOncePerMinute() } returns timePaceFlow.map { }
        createUseCase()

        val initialResult = useCase().first()

        assertThat(initialResult.syncInfoSectionRecords.footerLastSyncMinutesAgo).isEqualTo("0 minutes ago")

        timePaceFlow.value = -1 // just a different value for a time beat, doesn't matter which

        val updatedResult = useCase().first()

        assertThat(updatedResult.syncInfoSectionRecords.footerLastSyncMinutesAgo).isEqualTo("1 minute ago")
    }

    // UI state tests

    @Test
    fun `should calculate correct record last sync time when sync time available`() = runTest {
        val timestamp = Timestamp(0L)
        coEvery { eventSyncManager.getLastSyncTime() } returns timestamp
        every { timeHelper.readableBetweenNowAndTime(timestamp) } returns "5 minutes ago"
        createUseCase()

        val result = useCase().first()

        assertThat(result.syncInfoSectionRecords.isFooterLastSyncTimeVisible).isTrue()
        assertThat(result.syncInfoSectionRecords.footerLastSyncMinutesAgo).isEqualTo("5 minutes ago")
    }

    @Test
    fun `should have hidden record last sync time footer when no sync history`() = runTest {
        coEvery { eventSyncManager.getLastSyncTime() } returns null
        createUseCase()

        val result = useCase().first()

        assertThat(result.syncInfoSectionRecords.isFooterLastSyncTimeVisible).isFalse()
    }

    @Test
    fun `should calculate correct image last sync time when available`() = runTest {
        val mockImageStatusWithLastSync = mockk<ImageSyncStatus>(relaxed = true) {
            every { isSyncing } returns false
            every { progress } returns null
            every { secondsSinceLastUpdate } returns 180 // 3 minutes
        }
        every { syncOrchestrator.observeImageSyncStatus() } returns MutableStateFlow(mockImageStatusWithLastSync)
        every { timeHelper.readableBetweenNowAndTime(Timestamp(180 * 1000)) } returns "3 minutes ago"
        createUseCase()

        val result = useCase().first()

        assertThat(result.syncInfoSectionImages.isFooterLastSyncTimeVisible).isTrue()
        assertThat(result.syncInfoSectionImages.footerLastSyncMinutesAgo).isEqualTo("3 minutes ago")
    }

    @Test
    fun `should have hidden image last sync time footer when unavailable`() = runTest {
        val mockImageStatusWithoutLastSync = mockk<ImageSyncStatus>(relaxed = true) {
            every { isSyncing } returns false
            every { progress } returns null
            every { secondsSinceLastUpdate } returns null
        }
        every { syncOrchestrator.observeImageSyncStatus() } returns MutableStateFlow(mockImageStatusWithoutLastSync)
        createUseCase()

        val result = useCase().first()

        assertThat(result.syncInfoSectionImages.isFooterLastSyncTimeVisible).isFalse()
    }

    @Test
    fun `should show correct visibility states for offline instructions`() = runTest {
        every { connectivityTracker.observeIsConnected().asFlow() } returns flowOf(false)
        createUseCase()

        val result = useCase().first()

        assertThat(result.syncInfoSectionRecords.isInstructionOfflineVisible).isTrue()
        assertThat(result.syncInfoSectionRecords.isInstructionDefaultVisible).isFalse()
        assertThat(result.syncInfoSectionRecords.isInstructionErrorVisible).isFalse()
        assertThat(result.syncInfoSectionImages.isInstructionOfflineVisible).isTrue()
        assertThat(result.syncInfoSectionImages.isInstructionDefaultVisible).isFalse()
    }

    @Test
    fun `should show correct visibility states for error instructions`() = runTest {
        val mockFailedEventSyncState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncFailed() } returns true
            every { isSyncInProgress() } returns false
        }
        every { eventSyncManager.getLastSyncState(any()) } returns MutableLiveData(mockFailedEventSyncState)
        every { connectivityTracker.observeIsConnected().asFlow() } returns flowOf(true)
        createUseCase()

        val result = useCase().first()

        assertThat(result.syncInfoSectionRecords.isInstructionErrorVisible).isTrue()
        assertThat(result.syncInfoSectionRecords.isInstructionDefaultVisible).isFalse()
        assertThat(result.syncInfoSectionRecords.isInstructionOfflineVisible).isFalse()
    }

    @Test
    fun `should show correct visibility states for module selection instructions`() = runTest {
        val mockProjectConfigRequiringModules = mockk<ProjectConfiguration> {
            every { general } returns mockk<GeneralConfiguration> {
                every { modalities } returns listOf(GeneralConfiguration.Modality.FINGERPRINT)
            }
        }
        val mockEmptyDeviceConfig = mockk<DeviceConfiguration> {
            every { selectedModules } returns emptyList()
        }
        val mockIdleEventSyncState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncFailed() } returns false
            every { isSyncInProgress() } returns false
        }
        every { configManager.observeProjectConfiguration() } returns MutableStateFlow(mockProjectConfigRequiringModules)
        every { configManager.observeDeviceConfiguration() } returns MutableStateFlow(mockEmptyDeviceConfig)
        coEvery { configManager.getProjectConfiguration() } returns mockProjectConfigRequiringModules
        coEvery { configManager.getDeviceConfiguration() } returns mockEmptyDeviceConfig
        every { eventSyncManager.getLastSyncState(any()) } returns MutableLiveData(mockIdleEventSyncState)
        every { connectivityTracker.observeIsConnected().asFlow() } returns flowOf(true)
        every { mockProjectConfigRequiringModules.isModuleSelectionAvailable() } returns true
        createUseCase()

        val result = useCase().first()

        assertThat(result.syncInfoSectionRecords.isInstructionNoModulesVisible).isTrue()
        assertThat(result.syncInfoSectionRecords.isInstructionDefaultVisible).isFalse()
        assertThat(result.syncInfoSectionRecords.isInstructionOfflineVisible).isFalse()
        assertThat(result.syncInfoSectionRecords.isInstructionErrorVisible).isFalse()
    }

    @Test
    fun `should show correct visibility states for default instructions`() = runTest {
        val mockIdleEventSyncState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncFailed() } returns false
            every { isSyncInProgress() } returns false
        }
        every { eventSyncManager.getLastSyncState(any()) } returns MutableLiveData(mockIdleEventSyncState)
        every { connectivityTracker.observeIsConnected().asFlow() } returns flowOf(true)
        createUseCase()

        val result = useCase().first()

        assertThat(result.syncInfoSectionRecords.isInstructionDefaultVisible).isTrue()
        assertThat(result.syncInfoSectionRecords.isInstructionOfflineVisible).isFalse()
        assertThat(result.syncInfoSectionRecords.isInstructionErrorVisible).isFalse()
        assertThat(result.syncInfoSectionRecords.isInstructionNoModulesVisible).isFalse()
        assertThat(result.syncInfoSectionImages.isInstructionDefaultVisible).isTrue()
        assertThat(result.syncInfoSectionImages.isInstructionOfflineVisible).isFalse()
    }

    @Test
    fun `should handle failed sync retry indication correctly`() = runTest {
        val eventSyncStateFlow = MutableLiveData<EventSyncState>()
        every { eventSyncManager.getLastSyncState(any()) } returns eventSyncStateFlow
        createUseCase()
        val mockFailedState = mockk<EventSyncState>(relaxed = true) {
            every { isSyncFailed() } returns true
            every { isSyncInProgress() } returns false
            every { isSyncFailedBecauseReloginRequired() } returns false
        }
        eventSyncStateFlow.value = mockFailedState

        val failedResult = useCase().first()

        assertThat(failedResult.syncInfoSectionRecords.isInstructionErrorVisible).isTrue()
        assertThat(failedResult.syncInfoSectionRecords.isSyncButtonForRetry).isTrue()
    }

    // Module tokenization tests

    @Test
    fun `should correctly decrypt tokenized module names`() = runTest {
        val tokenizedModule = TokenizableString.Tokenized("encrypted_module_name")
        val mockProjectConfigWithModules = mockk<ProjectConfiguration> {
            every { general } returns mockk<GeneralConfiguration> {
                every { modalities } returns listOf(GeneralConfiguration.Modality.FINGERPRINT)
            }
        }
        val mockDeviceConfigWithTokenizedModules = mockk<DeviceConfiguration> {
            every { selectedModules } returns listOf(tokenizedModule)
        }
        every { configManager.observeProjectConfiguration() } returns MutableStateFlow(mockProjectConfigWithModules)
        every { configManager.observeDeviceConfiguration() } returns MutableStateFlow(mockDeviceConfigWithTokenizedModules)
        coEvery { configManager.getProjectConfiguration() } returns mockProjectConfigWithModules
        coEvery { configManager.getDeviceConfiguration() } returns mockDeviceConfigWithTokenizedModules
        coEvery { enrolmentRecordRepository.count(any()) } returnsMany listOf(10, 10) // total, and the module
        every {
            tokenizationProcessor.decrypt(tokenizedModule, TokenKeyType.ModuleId, any())
        } returns TokenizableString.Raw("decrypted_module")
        every { mockProjectConfigWithModules.isModuleSelectionAvailable() } returns true
        createUseCase()

        val result = useCase().first()

        assertThat(result.syncInfoSectionModules.isSectionAvailable).isTrue()
        assertThat(result.syncInfoSectionModules.moduleCounts).hasSize(2) // total + the module
        assertThat(result.syncInfoSectionModules.moduleCounts[1].name).isEqualTo("decrypted_module")
        verify { tokenizationProcessor.decrypt(tokenizedModule, TokenKeyType.ModuleId, any()) }
    }

    @Test
    fun `should correctly handle raw module names`() = runTest {
        val rawModule = TokenizableString.Raw("raw_module_name")
        val mockProjectConfigWithModules = mockk<ProjectConfiguration> {
            every { general } returns mockk<GeneralConfiguration> {
                every { modalities } returns listOf(GeneralConfiguration.Modality.FINGERPRINT)
            }
        }
        val mockDeviceConfigWithRawModules = mockk<DeviceConfiguration> {
            every { selectedModules } returns listOf(rawModule)
        }
        every { configManager.observeProjectConfiguration() } returns MutableStateFlow(mockProjectConfigWithModules)
        every { configManager.observeDeviceConfiguration() } returns MutableStateFlow(mockDeviceConfigWithRawModules)
        coEvery { configManager.getProjectConfiguration() } returns mockProjectConfigWithModules
        coEvery { configManager.getDeviceConfiguration() } returns mockDeviceConfigWithRawModules
        coEvery { enrolmentRecordRepository.count(any()) } returnsMany listOf(10, 10) // total, and the module
        every { mockProjectConfigWithModules.isModuleSelectionAvailable() } returns true
        createUseCase()

        val result = useCase().first()

        assertThat(result.syncInfoSectionModules.isSectionAvailable).isTrue()
        assertThat(result.syncInfoSectionModules.moduleCounts).hasSize(2) // total + the module
        assertThat(result.syncInfoSectionModules.moduleCounts[1].name).isEqualTo("raw_module_name")
        verify(exactly = 0) { tokenizationProcessor.decrypt(any(), any(), any()) }
    }

}

