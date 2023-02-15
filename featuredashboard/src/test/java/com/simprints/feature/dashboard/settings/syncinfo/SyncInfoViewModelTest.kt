package com.simprints.feature.dashboard.settings.syncinfo

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.google.common.truth.Truth.assertThat
import com.simprints.eventsystem.event.EventRepository
import com.simprints.eventsystem.event.domain.EventCount
import com.simprints.eventsystem.event.domain.models.EventType
import com.simprints.eventsystem.event.domain.models.subject.EnrolmentRecordEventType
import com.simprints.eventsystem.events_sync.down.EventDownSyncScopeRepository
import com.simprints.eventsystem.events_sync.down.domain.EventDownSyncScope
import com.simprints.eventsystem.events_sync.down.domain.RemoteEventQuery
import com.simprints.eventsystem.events_sync.models.EventSyncState
import com.simprints.eventsystem.events_sync.models.EventSyncWorkerState
import com.simprints.eventsystem.events_sync.models.EventSyncWorkerType
import com.simprints.feature.dashboard.main.sync.DeviceManager
import com.simprints.feature.dashboard.main.sync.EventSyncManager
import com.simprints.feature.dashboard.settings.syncinfo.modulecount.ModuleCount
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.DownSynchronizationConfiguration
import com.simprints.infra.config.domain.models.ProjectConfiguration
import com.simprints.infra.config.domain.models.SynchronizationConfiguration
import com.simprints.infra.enrolment.records.EnrolmentRecordManager
import com.simprints.infra.enrolment.records.domain.models.SubjectQuery
import com.simprints.infra.images.ImageRepository
import com.simprints.infra.login.LoginManager
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.livedata.getOrAwaitValue
import com.simprints.testtools.common.livedata.getOrAwaitValues
import io.mockk.*
import io.mockk.impl.annotations.MockK

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
    private lateinit var eventRepository: EventRepository

    @MockK
    private lateinit var enrolmentRecordManager: EnrolmentRecordManager

    @MockK
    private lateinit var loginManager: LoginManager

    @MockK
    private lateinit var deviceManager: DeviceManager

    @MockK
    private lateinit var eventDownSyncScopeRepository: EventDownSyncScopeRepository

    @MockK
    private lateinit var imageRepository: ImageRepository

    @MockK
    private lateinit var eventSyncManager: EventSyncManager

    private lateinit var connectionLiveData: MutableLiveData<Boolean>
    private lateinit var stateLiveData: MutableLiveData<EventSyncState>

    private lateinit var viewModel: SyncInfoViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        every { loginManager.getSignedInProjectIdOrEmpty() } returns PROJECT_ID

        connectionLiveData = MutableLiveData<Boolean>()
        every { deviceManager.isConnectedLiveData } returns connectionLiveData

        stateLiveData = MutableLiveData<EventSyncState>()
        every { eventSyncManager.getLastSyncState() } returns stateLiveData

        viewModel = SyncInfoViewModel(
            configManager,
            deviceManager,
            eventRepository,
            enrolmentRecordManager,
            loginManager,
            eventDownSyncScopeRepository,
            imageRepository,
            eventSyncManager,
        )
    }

    @Test
    fun `should initialize the configuration live data correctly`() {
        val configuration = mockk<ProjectConfiguration>()
        coEvery { configManager.getProjectConfiguration() } returns configuration

        viewModel.refreshInformation()

        assertThat(viewModel.configuration.getOrAwaitValue()).isEqualTo(configuration)
    }

    @Test
    fun `should initialize the recordsInLocal live data correctly`() {
        val number = 10
        coEvery { enrolmentRecordManager.count(SubjectQuery(projectId = PROJECT_ID)) } returns number

        viewModel.refreshInformation()

        assertThat(viewModel.recordsInLocal.getOrAwaitValue()).isEqualTo(number)
    }

    @Test
    fun `should initialize the recordsToUpSync live data correctly`() {
        val number = 10
        coEvery { eventRepository.localCount(PROJECT_ID, EventType.ENROLMENT_V2) } returns number

        viewModel.refreshInformation()

        assertThat(viewModel.recordsToUpSync.getOrAwaitValue()).isEqualTo(number)
    }

    @Test
    fun `should initialize the imagesToUpload live data correctly`() {
        val number = 10
        coEvery { imageRepository.getNumberOfImagesToUpload(PROJECT_ID) } returns number

        viewModel.refreshInformation()

        assertThat(viewModel.imagesToUpload.getOrAwaitValue()).isEqualTo(number)
    }

    @Test
    fun `should initialize the moduleCounts live data correctly`() {
        val module1 = "module1"
        val module2 = "module2"
        val numberForModule1 = 10
        val numberForModule2 = 20
        coEvery { configManager.getDeviceConfiguration() } returns mockk {
            every { selectedModules } returns listOf(module1, module2)
        }
        coEvery {
            enrolmentRecordManager.count(
                SubjectQuery(
                    projectId = PROJECT_ID,
                    moduleId = module1
                )
            )
        } returns numberForModule1
        coEvery {
            enrolmentRecordManager.count(
                SubjectQuery(
                    projectId = PROJECT_ID,
                    moduleId = module2
                )
            )
        } returns numberForModule2

        viewModel.refreshInformation()

        assertThat(viewModel.moduleCounts.getOrAwaitValue()).isEqualTo(
            listOf(
                ModuleCount(module1, numberForModule1),
                ModuleCount(module2, numberForModule2),
            )
        )
    }

    @Test
    fun `should initialize the recordsToDownSync and recordsToDelete live data to 0 if an exception is thrown`() {
        coEvery { configManager.getDeviceConfiguration() } throws Exception()

        viewModel.refreshInformation()

        assertThat(viewModel.recordsToDownSync.getOrAwaitValue()).isEqualTo(0)
        assertThat(viewModel.recordsToDelete.getOrAwaitValue()).isEqualTo(0)
    }

    @Test
    fun `should initialize the recordsToDownSync and recordsToDelete live data to the count otherwise`() {
        val module1 = "module1"
        val module2 = "module2"
        val creationForModule1 = 10
        val deletionForModule1 = 5
        val creationForModule2 = 20
        coEvery { configManager.getDeviceConfiguration() } returns mockk {
            every { selectedModules } returns listOf(module1, module2)
        }
        coEvery {
            eventDownSyncScopeRepository.getDownSyncScope(any(), listOf(module1, module2), any())
        } returns EventDownSyncScope.SubjectModuleScope(
            PROJECT_ID,
            listOf(module1, module2),
            listOf()
        )

        coEvery {
            eventRepository.countEventsToDownload(
                RemoteEventQuery(
                    PROJECT_ID,
                    moduleIds = listOf(module1),
                    modes = listOf(),
                )
            )
        } returns listOf(
            EventCount(EnrolmentRecordEventType.EnrolmentRecordCreation, creationForModule1),
            EventCount(EnrolmentRecordEventType.EnrolmentRecordDeletion, deletionForModule1),
        )
        coEvery {
            eventRepository.countEventsToDownload(
                RemoteEventQuery(
                    PROJECT_ID,
                    moduleIds = listOf(module2),
                    modes = listOf(),
                )
            )
        } returns listOf(
            EventCount(EnrolmentRecordEventType.EnrolmentRecordCreation, creationForModule2),
        )

        viewModel.refreshInformation()

        assertThat(viewModel.recordsToDownSync.getOrAwaitValue()).isEqualTo(creationForModule1 + creationForModule2)
        assertThat(viewModel.recordsToDelete.getOrAwaitValue()).isEqualTo(deletionForModule1)
    }

    @Test
    fun `refreshInformation should first reset the information and then reload`() {
        coEvery { enrolmentRecordManager.count(SubjectQuery(projectId = PROJECT_ID)) } returnsMany listOf(
            2,
            4
        )
        viewModel.refreshInformation()

        val records = viewModel.recordsInLocal.getOrAwaitValues(3) {
            viewModel.refreshInformation()
        }

        // Init, refresh and reload
        assertThat(records).isEqualTo(listOf(2, null, 4))
    }

    @Test
    fun `fetchSyncInformationIfNeeded should not fetch the information if there is a non succeeded worker`() {
        viewModel.fetchSyncInformationIfNeeded(EventSyncState("", 0, 0, listOf(), listOf(
            EventSyncState.SyncWorkerInfo(
                EventSyncWorkerType.DOWNLOADER,
                EventSyncWorkerState.Running
            )
        )))

        coVerify(exactly = 0) { enrolmentRecordManager.count(SubjectQuery(projectId = PROJECT_ID)) }
    }

    @Test
    fun `fetchSyncInformationIfNeeded should fetch the information if there is only succeeded worker`() {
        viewModel.fetchSyncInformationIfNeeded(EventSyncState("", 0, 0, listOf(), listOf(
            EventSyncState.SyncWorkerInfo(
                EventSyncWorkerType.DOWNLOADER,
                EventSyncWorkerState.Succeeded
            )
        )))

        coVerify(exactly = 1) { enrolmentRecordManager.count(SubjectQuery(projectId = PROJECT_ID)) }
    }

    @Test
    fun `fetchSyncInformationIfNeeded should not fetch the information if the state hasn't changed`() {
        val state = EventSyncState("", 0, 0, listOf(), listOf(
            EventSyncState.SyncWorkerInfo(
                EventSyncWorkerType.DOWNLOADER,
                EventSyncWorkerState.Succeeded
            )
        ))

        viewModel.fetchSyncInformationIfNeeded(state)
        viewModel.fetchSyncInformationIfNeeded(state)

        coVerify(exactly = 1) { enrolmentRecordManager.count(SubjectQuery(projectId = PROJECT_ID)) }
    }

    @Test
    fun `should invoke sync manager when sync is requested`() {
        viewModel.forceSync()

        verify(exactly = 1) { eventSyncManager.sync() }
        assertThat(viewModel.isSyncAvailable.getOrAwaitValue()).isEqualTo(false)
    }

    @Test
    fun `isModuleSyncAndModuleIdOptionsNotEmpty returns true only if module sync and has modules`() {
        // Not module sync
        assertThat(
            viewModel.isModuleSyncAndModuleIdOptionsNotEmpty(createMockDownSyncConfig(
                partitionType = DownSynchronizationConfiguration.PartitionType.USER,
            ))
        ).isFalse()
        // Module sync + no modules
        assertThat(
            viewModel.isModuleSyncAndModuleIdOptionsNotEmpty(createMockDownSyncConfig(
                partitionType = DownSynchronizationConfiguration.PartitionType.MODULE,
            ))
        ).isFalse()
        // Module sync + has modules
        assertThat(
            viewModel.isModuleSyncAndModuleIdOptionsNotEmpty(createMockDownSyncConfig(
                partitionType = DownSynchronizationConfiguration.PartitionType.MODULE,
                modules = listOf("module")
            ))
        ).isTrue()
    }

    @Test
    fun `emit correct sync availability when connection status changes`() {
        coEvery { configManager.getProjectConfiguration() } returns mockk {
            every { synchronization } returns createMockDownSyncConfig(
                partitionType = DownSynchronizationConfiguration.PartitionType.MODULE,
                modules = listOf("module")
            )
        }
        viewModel.refreshInformation()
        stateLiveData.value = EventSyncState("", 0, 0, emptyList(), emptyList())

        connectionLiveData.value = false
        assertThat(viewModel.isSyncAvailable.getOrAwaitValue()).isFalse()

        connectionLiveData.value = true
        assertThat(viewModel.isSyncAvailable.getOrAwaitValue()).isTrue()
    }

    @Test
    fun `emit correct sync availability when sync status changes`() {
        coEvery { configManager.getProjectConfiguration() } returns mockk {
            every { synchronization } returns createMockDownSyncConfig(
                partitionType = DownSynchronizationConfiguration.PartitionType.MODULE,
                modules = listOf("module")
            )
        }
        viewModel.refreshInformation()
        connectionLiveData.value = true

        stateLiveData.value = EventSyncState("", 0, 0, emptyList(), emptyList())
        assertThat(viewModel.isSyncAvailable.getOrAwaitValue()).isTrue()

        stateLiveData.value = EventSyncState("", 0, 0, emptyList(),
            listOf(EventSyncState.SyncWorkerInfo(EventSyncWorkerType.DOWNLOADER, EventSyncWorkerState.Running))
        )
        assertThat(viewModel.isSyncAvailable.getOrAwaitValue()).isFalse()

        stateLiveData.value = EventSyncState("", 0, 0, emptyList(),
            listOf(EventSyncState.SyncWorkerInfo(EventSyncWorkerType.DOWNLOADER, EventSyncWorkerState.Succeeded))
        )
        assertThat(viewModel.isSyncAvailable.getOrAwaitValue()).isTrue()
    }

    @Test
    fun `emit correct sync availability when non-module config`() {
        coEvery { configManager.getProjectConfiguration() } returns mockk {
            every { synchronization } returns createMockDownSyncConfig(
                partitionType = DownSynchronizationConfiguration.PartitionType.USER,
            )
        }
        viewModel.refreshInformation()
        connectionLiveData.value = true
        stateLiveData.value = EventSyncState("", 0, 0, emptyList(), emptyList())

        assertThat(viewModel.isSyncAvailable.getOrAwaitValue()).isTrue()
    }

    @Test
    fun `emit correct sync availability when module config without modules`() {
        coEvery { configManager.getProjectConfiguration() } returns mockk {
            every { synchronization } returns createMockDownSyncConfig(
                partitionType = DownSynchronizationConfiguration.PartitionType.MODULE,
                modules = emptyList()
            )
        }
        viewModel.refreshInformation()
        connectionLiveData.value = true
        stateLiveData.value = EventSyncState("", 0, 0, emptyList(), emptyList())

        assertThat(viewModel.isSyncAvailable.getOrAwaitValue()).isFalse()
    }

    private fun createMockDownSyncConfig(
        partitionType: DownSynchronizationConfiguration.PartitionType,
        modules: List<String> = emptyList(),
    ) = mockk<SynchronizationConfiguration> {
        every { frequency }.returns(SynchronizationConfiguration.Frequency.PERIODICALLY)
        every { down }.returns(DownSynchronizationConfiguration(
            partitionType = partitionType,
            moduleOptions = modules,
            maxNbOfModules = 0,
        ))
    }
}
