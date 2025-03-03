package com.simprints.feature.dashboard.settings.syncinfo

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.feature.dashboard.settings.syncinfo.modulecount.ModuleCount
import com.simprints.feature.login.LoginResult
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.store.models.DownSynchronizationConfiguration
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.store.models.SynchronizationConfiguration
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.config.store.tokenization.TokenizationProcessor
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.enrolment.records.repository.EnrolmentRecordRepository
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectQuery
import com.simprints.infra.events.event.domain.models.EventType
import com.simprints.infra.eventsync.EventSyncManager
import com.simprints.infra.eventsync.status.models.DownSyncCounts
import com.simprints.infra.eventsync.status.models.EventSyncState
import com.simprints.infra.eventsync.status.models.EventSyncWorkerState
import com.simprints.infra.eventsync.status.models.EventSyncWorkerType
import com.simprints.infra.images.ImageRepository
import com.simprints.infra.network.ConnectivityTracker
import com.simprints.infra.recent.user.activity.RecentUserActivityManager
import com.simprints.infra.sync.SyncOrchestrator
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.livedata.getOrAwaitValue
import com.simprints.testtools.common.livedata.getOrAwaitValues
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SyncInfoViewModelTest {
    companion object {
        private const val PROJECT_ID = "projectId"
    }

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    private lateinit var configManager: ConfigManager

    @MockK
    private lateinit var enrolmentRecordRepository: EnrolmentRecordRepository

    @MockK
    private lateinit var authStore: AuthStore

    @MockK
    private lateinit var connectivityTracker: ConnectivityTracker

    @MockK
    private lateinit var imageRepository: ImageRepository

    @MockK
    private lateinit var eventSyncManager: EventSyncManager

    @MockK
    private lateinit var syncOrchestrator: SyncOrchestrator

    @MockK
    lateinit var recentUserActivityManager: RecentUserActivityManager

    @MockK
    private lateinit var project: Project

    @MockK(relaxed = true)
    private lateinit var tokenizationProcessor: TokenizationProcessor

    private lateinit var connectionLiveData: MutableLiveData<Boolean>
    private lateinit var stateLiveData: MutableLiveData<EventSyncState>

    private lateinit var viewModel: SyncInfoViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        every { authStore.signedInProjectId } returns PROJECT_ID

        connectionLiveData = MutableLiveData<Boolean>()
        every { connectivityTracker.observeIsConnected() } returns connectionLiveData

        stateLiveData = MutableLiveData<EventSyncState>()
        every { eventSyncManager.getLastSyncState() } returns stateLiveData
        coEvery { configManager.getProject(PROJECT_ID) } returns project
        viewModel = SyncInfoViewModel(
            configManager = configManager,
            connectivityTracker = connectivityTracker,
            enrolmentRecordRepository = enrolmentRecordRepository,
            authStore = authStore,
            imageRepository = imageRepository,
            eventSyncManager = eventSyncManager,
            syncOrchestrator = syncOrchestrator,
            tokenizationProcessor = tokenizationProcessor,
            recentUserActivityManager = recentUserActivityManager,
        )
    }

    @Test
    fun `should initialize the configuration live data correctly`() = runTest {
        val configuration = mockk<ProjectConfiguration>(relaxed = true)
        coEvery { configManager.getProjectConfiguration() } returns configuration

        viewModel.refreshInformation()

        assertThat(viewModel.configuration.getOrAwaitValue()).isEqualTo(configuration)
    }

    @Test
    fun `should initialize the recordsInLocal live data correctly`() = runTest {
        val number = 10
        coEvery { enrolmentRecordRepository.count(SubjectQuery(projectId = PROJECT_ID)) } returns number

        viewModel.refreshInformation()

        assertThat(viewModel.recordsInLocal.getOrAwaitValue()).isEqualTo(number)
    }

    @Test
    fun `should initialize the recordsToUpSync live data correctly`() = runTest {
        val number = 10
        coEvery {
            eventSyncManager.countEventsToUpload(EventType.ENROLMENT_V2)
        } returns flowOf(number)

        viewModel.refreshInformation()

        assertThat(viewModel.recordsToUpSync.getOrAwaitValue()).isEqualTo(number)
    }

    @Test
    fun `should initialize the imagesToUpload live data correctly`() = runTest {
        val number = 10
        coEvery { imageRepository.getNumberOfImagesToUpload(PROJECT_ID) } returns number

        viewModel.refreshInformation()

        assertThat(viewModel.imagesToUpload.getOrAwaitValue()).isEqualTo(number)
    }

    @Test
    fun `should initialize the moduleCounts live data correctly`() = runTest {
        val module1 = "module1".asTokenizableEncrypted()
        val module2 = "module2".asTokenizableEncrypted()
        val numberForModule1 = 10
        val numberForModule2 = 20
        coEvery { configManager.getDeviceConfiguration() } returns mockk {
            every { selectedModules } returns listOf(module1, module2)
        }
        coEvery {
            enrolmentRecordRepository.count(
                SubjectQuery(
                    projectId = PROJECT_ID,
                    moduleId = module1,
                ),
            )
        } returns numberForModule1
        coEvery {
            enrolmentRecordRepository.count(
                SubjectQuery(
                    projectId = PROJECT_ID,
                    moduleId = module2,
                ),
            )
        } returns numberForModule2
        listOf(module1, module2).forEach { moduleName ->
            every {
                tokenizationProcessor.decrypt(
                    encrypted = moduleName,
                    tokenKeyType = TokenKeyType.ModuleId,
                    project = project,
                )
            } returns moduleName
        }

        viewModel.refreshInformation()

        assertThat(viewModel.moduleCounts.getOrAwaitValue()).isEqualTo(
            listOf(
                ModuleCount(module1.value, numberForModule1),
                ModuleCount(module2.value, numberForModule2),
            ),
        )
    }

    @Test
    fun `should initialize the recordsToDownSync live data to the count otherwise`() = runTest {
        val module1 = "module1".asTokenizableEncrypted()
        coEvery { configManager.getDeviceConfiguration() } returns mockk {
            every { selectedModules } returns listOf(module1)
        }
        coEvery {
            eventSyncManager.countEventsToDownload()
        } returns DownSyncCounts(15, isLowerBound = false)

        viewModel.refreshInformation()

        assertThat(viewModel.recordsToDownSync.getOrAwaitValue()?.count).isEqualTo(15)
    }

    @Test
    fun `should initialize the recordsToDownSync live data to the default count value if fetch fails`() = runTest {
        val module1 = "module1".asTokenizableEncrypted()
        coEvery { configManager.getDeviceConfiguration() } returns mockk {
            every { selectedModules } returns listOf(module1)
        }
        coEvery {
            eventSyncManager.countEventsToDownload()
        } throws Exception()

        viewModel.refreshInformation()

        assertThat(viewModel.recordsToDownSync.getOrAwaitValue()?.count).isEqualTo(0)
    }

    @Test
    fun `refreshInformation should first reset the information and then reload`() = runTest {
        coEvery { enrolmentRecordRepository.count(SubjectQuery(projectId = PROJECT_ID)) } returnsMany listOf(
            2,
            4,
        )
        viewModel.refreshInformation()

        val records = viewModel.recordsInLocal.getOrAwaitValues(3) {
            viewModel.refreshInformation()
        }

        // Init, refresh and reload
        assertThat(records).isEqualTo(listOf(2, null, 4))
    }

    @Test
    fun `fetchSyncInformationIfNeeded should not fetch the information if there is a non succeeded worker`() = runTest {
        viewModel.fetchSyncInformationIfNeeded(
            EventSyncState(
                syncId = "",
                progress = 0,
                total = 0,
                upSyncWorkersInfo = listOf(),
                downSyncWorkersInfo = listOf(
                    EventSyncState.SyncWorkerInfo(
                        EventSyncWorkerType.DOWNLOADER,
                        EventSyncWorkerState.Running,
                    ),
                ),
                reporterStates = listOf(),
            ),
        )

        coVerify(exactly = 0) { enrolmentRecordRepository.count(SubjectQuery(projectId = PROJECT_ID)) }
    }

    @Test
    fun `fetchSyncInformationIfNeeded should fetch the information if there is only succeeded worker`() = runTest {
        viewModel.fetchSyncInformationIfNeeded(
            EventSyncState(
                syncId = "",
                progress = 0,
                total = 0,
                upSyncWorkersInfo = listOf(),
                downSyncWorkersInfo = listOf(
                    EventSyncState.SyncWorkerInfo(
                        EventSyncWorkerType.DOWNLOADER,
                        EventSyncWorkerState.Succeeded,
                    ),
                ),
                reporterStates = listOf(),
            ),
        )

        coVerify(exactly = 1) { enrolmentRecordRepository.count(SubjectQuery(projectId = PROJECT_ID)) }
    }

    @Test
    fun `fetchSyncInformationIfNeeded should not fetch the information if the state hasn't changed`() = runTest {
        val state = EventSyncState(
            syncId = "",
            progress = 0,
            total = 0,
            upSyncWorkersInfo = listOf(),
            downSyncWorkersInfo = listOf(
                EventSyncState.SyncWorkerInfo(
                    EventSyncWorkerType.DOWNLOADER,
                    EventSyncWorkerState.Succeeded,
                ),
            ),
            reporterStates = listOf(),
        )

        viewModel.fetchSyncInformationIfNeeded(state)
        viewModel.fetchSyncInformationIfNeeded(state)

        coVerify(exactly = 1) { enrolmentRecordRepository.count(SubjectQuery(projectId = PROJECT_ID)) }
    }

    @Test
    fun `should invoke sync manager when sync is requested`() = runTest {
        viewModel.forceSync()

        verify(exactly = 1) { syncOrchestrator.startEventSync() }
        assertThat(viewModel.isSyncAvailable.getOrAwaitValue()).isEqualTo(false)
    }

    @Test
    fun `isModuleSyncAndModuleIdOptionsNotEmpty returns true only if module sync and has modules`() = runTest {
        // Not module sync
        assertThat(
            viewModel.isModuleSyncAndModuleIdOptionsNotEmpty(
                createMockDownSyncConfig(
                    partitionType = DownSynchronizationConfiguration.PartitionType.USER,
                ),
            ),
        ).isFalse()
        // Module sync + no modules
        assertThat(
            viewModel.isModuleSyncAndModuleIdOptionsNotEmpty(
                createMockDownSyncConfig(
                    partitionType = DownSynchronizationConfiguration.PartitionType.MODULE,
                ),
            ),
        ).isFalse()
        // Module sync + has modules
        assertThat(
            viewModel.isModuleSyncAndModuleIdOptionsNotEmpty(
                createMockDownSyncConfig(
                    partitionType = DownSynchronizationConfiguration.PartitionType.MODULE,
                    modules = listOf("module"),
                ),
            ),
        ).isTrue()
    }

    @Test
    fun `emit correct sync availability when connection status changes`() = runTest {
        coEvery { configManager.getProjectConfiguration() } returns mockk {
            every { synchronization } returns createMockDownSyncConfig(
                partitionType = DownSynchronizationConfiguration.PartitionType.MODULE,
                modules = listOf("module"),
            )
        }
        viewModel.refreshInformation()
        stateLiveData.value = EventSyncState("", 0, 0, emptyList(), emptyList(), emptyList())

        connectionLiveData.value = false
        assertThat(viewModel.isSyncAvailable.getOrAwaitValue()).isFalse()

        connectionLiveData.value = true
        assertThat(viewModel.isSyncAvailable.getOrAwaitValue()).isTrue()
    }

    @Test
    fun `emit correct sync availability when sync status changes`() = runTest {
        coEvery { configManager.getProjectConfiguration() } returns mockk {
            every { synchronization } returns createMockDownSyncConfig(
                partitionType = DownSynchronizationConfiguration.PartitionType.MODULE,
                modules = listOf("module"),
            )
        }
        viewModel.refreshInformation()
        connectionLiveData.value = true

        stateLiveData.value = EventSyncState("", 0, 0, emptyList(), emptyList(), emptyList())
        assertThat(viewModel.isSyncAvailable.getOrAwaitValue()).isTrue()

        stateLiveData.value = EventSyncState(
            syncId = "",
            progress = 0,
            total = 0,
            upSyncWorkersInfo = emptyList(),
            downSyncWorkersInfo = listOf(
                EventSyncState.SyncWorkerInfo(
                    EventSyncWorkerType.DOWNLOADER,
                    EventSyncWorkerState.Running,
                ),
            ),
            reporterStates = listOf(),
        )
        assertThat(viewModel.isSyncAvailable.getOrAwaitValue()).isFalse()

        stateLiveData.value = EventSyncState(
            syncId = "",
            progress = 0,
            total = 0,
            upSyncWorkersInfo = emptyList(),
            downSyncWorkersInfo = listOf(
                EventSyncState.SyncWorkerInfo(
                    EventSyncWorkerType.DOWNLOADER,
                    EventSyncWorkerState.Succeeded,
                ),
            ),
            reporterStates = listOf(),
        )
        assertThat(viewModel.isSyncAvailable.getOrAwaitValue()).isTrue()
    }

    @Test
    fun `emit correct sync availability when non-module config`() = runTest {
        coEvery { configManager.getProjectConfiguration() } returns mockk {
            every { synchronization } returns createMockDownSyncConfig(
                partitionType = DownSynchronizationConfiguration.PartitionType.USER,
            )
        }
        viewModel.refreshInformation()
        connectionLiveData.value = true
        stateLiveData.value = EventSyncState("", 0, 0, emptyList(), emptyList(), emptyList())

        assertThat(viewModel.isSyncAvailable.getOrAwaitValue()).isTrue()
    }

    @Test
    fun `emit correct sync availability when module config without modules`() = runTest {
        coEvery { configManager.getProjectConfiguration() } returns mockk {
            every { synchronization } returns createMockDownSyncConfig(
                partitionType = DownSynchronizationConfiguration.PartitionType.MODULE,
                modules = emptyList(),
            )
        }
        viewModel.refreshInformation()
        connectionLiveData.value = true
        stateLiveData.value = EventSyncState("", 0, 0, emptyList(), emptyList(), emptyList())

        assertThat(viewModel.isSyncAvailable.getOrAwaitValue()).isFalse()
    }

    @Test
    fun `emit ReloginRequired = false when lastSyncState updates with different status`() = runTest {
        stateLiveData.value = EventSyncState(
            "",
            0,
            0,
            listOf(),
            listOf(
                EventSyncState.SyncWorkerInfo(
                    EventSyncWorkerType.DOWNLOADER,
                    EventSyncWorkerState.Failed(failedBecauseBackendMaintenance = true),
                ),
            ),
            reporterStates = listOf(),
        )

        assertThat(viewModel.isReloginRequired.getOrAwaitValue()).isFalse()
    }

    @Test
    fun `emit ReloginRequired = true when lastSyncState updates with such status`() = runTest {
        stateLiveData.value = EventSyncState(
            "",
            0,
            0,
            listOf(),
            listOf(
                EventSyncState.SyncWorkerInfo(
                    EventSyncWorkerType.DOWNLOADER,
                    EventSyncWorkerState.Failed(failedBecauseReloginRequired = true),
                ),
            ),
            reporterStates = listOf(),
        )

        assertThat(viewModel.isReloginRequired.getOrAwaitValue()).isTrue()
    }

    @Test
    fun `calling login() sends respective event to the view`() {
        viewModel.login()

        val loginRequestedEvent = viewModel.loginRequestedEventLiveData.getOrAwaitValue()
        assertThat(loginRequestedEvent).isNotNull()
    }

    @Test
    fun `calling handleLoginResult() triggers sync if result is success`() {
        viewModel.handleLoginResult(LoginResult(true))

        verify(exactly = 1) { syncOrchestrator.startEventSync() }
    }

    @Test
    fun `calling handleLoginResult() does not trigger sync if result is not success`() {
        viewModel.handleLoginResult(LoginResult(false))

        verify(exactly = 0) { syncOrchestrator.startEventSync() }
    }

    private fun createMockDownSyncConfig(
        partitionType: DownSynchronizationConfiguration.PartitionType,
        modules: List<String> = emptyList(),
    ) = mockk<SynchronizationConfiguration> {
        every { frequency }.returns(SynchronizationConfiguration.Frequency.PERIODICALLY)
        every { down }.returns(
            DownSynchronizationConfiguration(
                partitionType = partitionType,
                moduleOptions = modules.map(String::asTokenizableRaw),
                maxNbOfModules = 0,
                maxAge = "PT24H",
            ),
        )
    }
}
