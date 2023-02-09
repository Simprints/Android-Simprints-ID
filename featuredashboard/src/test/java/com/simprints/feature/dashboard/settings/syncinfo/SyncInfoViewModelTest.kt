package com.simprints.feature.dashboard.settings.syncinfo

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
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
import com.simprints.infra.config.domain.models.ProjectConfiguration
import com.simprints.infra.enrolment.records.EnrolmentRecordManager
import com.simprints.infra.enrolment.records.domain.models.SubjectQuery
import com.simprints.infra.images.ImageRepository
import com.simprints.infra.login.LoginManager
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.livedata.getOrAwaitValue
import com.simprints.testtools.common.livedata.getOrAwaitValues
import io.mockk.*
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

    private val configManager = mockk<ConfigManager>(relaxed = true)
    private val eventRepository = mockk<EventRepository>(relaxed = true)
    private val enrolmentRecordManager = mockk<EnrolmentRecordManager>(relaxed = true)
    private val loginManager = mockk<LoginManager> {
        every { getSignedInProjectIdOrEmpty() } returns PROJECT_ID
    }
    private val deviceManager = mockk<DeviceManager>(relaxed = true)
    private val eventDownSyncScopeRepository = mockk<EventDownSyncScopeRepository>(relaxed = true)
    private val imageRepository = mockk<ImageRepository>(relaxed = true)
    private val eventSyncManager = mockk<EventSyncManager>(relaxed = true)
    private val viewModel = SyncInfoViewModel(
        configManager,
        deviceManager,
        eventRepository,
        enrolmentRecordManager,
        loginManager,
        eventDownSyncScopeRepository,
        imageRepository,
        eventSyncManager,
    )

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
        viewModel.fetchSyncInformationIfNeeded(
            EventSyncState(
                "", 0, 0, listOf(), listOf(
                    EventSyncState.SyncWorkerInfo(
                        EventSyncWorkerType.DOWNLOADER,
                        EventSyncWorkerState.Running
                    )
                )
            )
        )

        coVerify(exactly = 0) { enrolmentRecordManager.count(SubjectQuery(projectId = PROJECT_ID)) }
    }

    @Test
    fun `fetchSyncInformationIfNeeded should fetch the information if there is only succeeded worker`() {
        viewModel.fetchSyncInformationIfNeeded(
            EventSyncState(
                "", 0, 0, listOf(), listOf(
                    EventSyncState.SyncWorkerInfo(
                        EventSyncWorkerType.DOWNLOADER,
                        EventSyncWorkerState.Succeeded
                    )
                )
            )
        )

        coVerify(exactly = 1) { enrolmentRecordManager.count(SubjectQuery(projectId = PROJECT_ID)) }
    }

    @Test
    fun `fetchSyncInformationIfNeeded should not fetch the information if the state hasn't changed`() {
        val state = EventSyncState(
            "", 0, 0, listOf(), listOf(
                EventSyncState.SyncWorkerInfo(
                    EventSyncWorkerType.DOWNLOADER,
                    EventSyncWorkerState.Succeeded
                )
            )
        )

        viewModel.fetchSyncInformationIfNeeded(state)
        viewModel.fetchSyncInformationIfNeeded(state)

        coVerify(exactly = 1) { enrolmentRecordManager.count(SubjectQuery(projectId = PROJECT_ID)) }
    }

    @Test
    fun `should invoke sync manager when sync is requested`() {
        viewModel.forceSync()

        verify(exactly = 1) { eventSyncManager.sync() }
    }
}
