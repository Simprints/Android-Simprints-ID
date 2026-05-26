package com.simprints.feature.dashboard.settings.syncinfo.usecase

import com.google.common.truth.Truth.*
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.feature.dashboard.settings.syncinfo.modulecount.ModuleCount
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.DeviceConfiguration
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.store.models.ProjectState
import com.simprints.infra.config.store.tokenization.TokenizationProcessor
import com.simprints.infra.enrolment.records.repository.EnrolmentRecordRepository
import com.simprints.infra.eventsync.status.models.EventSyncState
import com.simprints.infra.sync.ImageSyncStatus
import com.simprints.infra.sync.SyncOrchestrator
import com.simprints.infra.sync.SyncStatus
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ObserveConfigurationChangesUseCaseTest {
    @MockK
    private lateinit var configRepository: ConfigRepository

    @MockK
    private lateinit var tokenizationProcessor: TokenizationProcessor

    @MockK
    private lateinit var enrolmentRepository: EnrolmentRecordRepository

    @MockK
    private lateinit var project: Project

    @MockK
    private lateinit var projectConfiguration: ProjectConfiguration

    @MockK
    private lateinit var deviceConfiguration: DeviceConfiguration

    @MockK
    private lateinit var syncOrchestrator: SyncOrchestrator

    private lateinit var syncStateFlow: MutableStateFlow<SyncStatus>

    private lateinit var useCase: ObserveConfigurationChangesUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        syncStateFlow = MutableStateFlow(createSyncStatus(isSyncCompleted = false))
        every { syncOrchestrator.observeSyncState() } returns syncStateFlow

        useCase = ObserveConfigurationChangesUseCase(
            configRepository = configRepository,
            tokenizationProcessor = tokenizationProcessor,
            enrolmentRecordRepository = enrolmentRepository,
            syncOrchestrator = syncOrchestrator,
        )
    }

    @Test
    fun `returns combined state`() = runTest {
        coEvery { configRepository.getProject() } returns null
        every { configRepository.observeIsProjectRefreshing() } returns flowOf(true)
        every { configRepository.observeProjectConfiguration() } returns flowOf(projectConfiguration)
        every { configRepository.observeDeviceConfiguration() } returns flowOf(deviceConfiguration)

        val result = useCase().first()

        assertThat(result.isRefreshing).isTrue()
        assertThat(result.isProjectRunning).isFalse()
    }

    @Test
    fun `returns combined state on multiple emissions of combined flow`() = runTest {
        coEvery { configRepository.getProject() } returns null
        every { configRepository.observeIsProjectRefreshing() } returns flowOf(true, false)
        every { configRepository.observeProjectConfiguration() } returns flowOf(projectConfiguration)
        every { configRepository.observeDeviceConfiguration() } returns flowOf(deviceConfiguration)

        val result = useCase().take(2).toList()

        assertThat(result.first().isRefreshing).isTrue()
        assertThat(result.last().isRefreshing).isFalse()
    }

    @Test
    fun `invoke untokenized list of modules`() = runTest {
        coEvery { configRepository.getProject() } returns project
        every { project.id } returns "projectId"
        every { project.state } returns ProjectState.RUNNING
        every { deviceConfiguration.selectedModules } returns listOf(
            "moduleRaw".asTokenizableRaw(),
            "moduleToken".asTokenizableEncrypted(),
        )
        every {
            tokenizationProcessor.decrypt(any(), any(), any())
        } returns "moduleUntokenized".asTokenizableRaw()
        coEvery { enrolmentRepository.count(any(), any()) } returnsMany listOf(1, 2)

        every { configRepository.observeIsProjectRefreshing() } returns flowOf(true)
        every { configRepository.observeProjectConfiguration() } returns flowOf(projectConfiguration)
        every { configRepository.observeDeviceConfiguration() } returns flowOf(deviceConfiguration)

        val result = useCase().first()

        assertThat(result.selectedModules).containsExactly(
            ModuleCount("moduleRaw", 1),
            ModuleCount("moduleUntokenized", 2),
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `re-emits configuration state when sync completes`() = runTest {
        every { syncOrchestrator.observeSyncState() } returns syncStateFlow

        coEvery { configRepository.getProject() } returns project
        every { project.id } returns "projectId"
        every { project.state } returns ProjectState.RUNNING
        every { deviceConfiguration.selectedModules } returns listOf("moduleRaw".asTokenizableRaw())
        coEvery { enrolmentRepository.count(any(), any()) } returnsMany listOf(1, 2)

        every { configRepository.observeIsProjectRefreshing() } returns flowOf(false)
        every { configRepository.observeProjectConfiguration() } returns flowOf(projectConfiguration)
        every { configRepository.observeDeviceConfiguration() } returns flowOf(deviceConfiguration)

        val emissions = mutableListOf<ConfigurationState>()
        val job = backgroundScope.launch {
            useCase().take(2).toList(emissions)
        }

        advanceUntilIdle()
        syncStateFlow.value = createSyncStatus(isSyncCompleted = true)
        advanceUntilIdle()
        job.join()

        assertThat(emissions).hasSize(2)
        assertThat(emissions[0].selectedModules).containsExactly(ModuleCount("moduleRaw", 1))
        assertThat(emissions[1].selectedModules).containsExactly(ModuleCount("moduleRaw", 2))
        coVerify(exactly = 2) { enrolmentRepository.count(any(), any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `does not re-emit when sync completed state is repeated`() = runTest {
        every { syncOrchestrator.observeSyncState() } returns syncStateFlow

        coEvery { configRepository.getProject() } returns project
        every { project.id } returns "projectId"
        every { project.state } returns ProjectState.RUNNING
        every { deviceConfiguration.selectedModules } returns listOf("moduleRaw".asTokenizableRaw())
        coEvery { enrolmentRepository.count(any(), any()) } returnsMany listOf(1, 2, 3)

        every { configRepository.observeIsProjectRefreshing() } returns flowOf(false)
        every { configRepository.observeProjectConfiguration() } returns flowOf(projectConfiguration)
        every { configRepository.observeDeviceConfiguration() } returns flowOf(deviceConfiguration)

        val emissions = mutableListOf<ConfigurationState>()
        val job = launch {
            useCase().toList(emissions)
        }

        advanceUntilIdle()
        syncStateFlow.value = createSyncStatus(isSyncCompleted = true)
        advanceUntilIdle()
        syncStateFlow.value = createSyncStatus(isSyncCompleted = true)
        advanceUntilIdle()
        job.cancelAndJoin()

        assertThat(emissions).hasSize(2)
        assertThat(emissions.map { it.selectedModules.single().count }).containsExactly(1, 2).inOrder()
        coVerify(exactly = 2) { enrolmentRepository.count(any(), any()) }
    }

    private fun createSyncStatus(isSyncCompleted: Boolean): SyncStatus {
        val eventSyncState = mockk<EventSyncState>()
        every { eventSyncState.isSyncCompleted() } returns isSyncCompleted
        return SyncStatus(
            eventSyncState = eventSyncState,
            imageSyncStatus = mockk<ImageSyncStatus>(relaxed = true),
        )
    }
}
