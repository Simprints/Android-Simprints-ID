package com.simprints.feature.dashboard.settings.syncinfo.usecase

import com.google.common.truth.Truth.*
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.feature.dashboard.settings.syncinfo.modulecount.ModuleCount
import com.simprints.infra.config.store.models.DeviceConfiguration
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.store.models.ProjectState
import com.simprints.infra.config.store.tokenization.TokenizationProcessor
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.enrolment.records.repository.EnrolmentRecordRepository
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ObserveConfigurationChangesUseCaseTest {
    @MockK
    private lateinit var configManager: ConfigManager

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

    private lateinit var useCase: ObserveConfigurationChangesUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        useCase = ObserveConfigurationChangesUseCase(
            configManager = configManager,
            tokenizationProcessor = tokenizationProcessor,
            enrolmentRecordRepository = enrolmentRepository,
        )
    }

    @Test
    fun `returns combined state`() = runTest {
        coEvery { configManager.getProject() } returns null
        every { configManager.observeIsProjectRefreshing() } returns flowOf(true)
        every { configManager.observeProjectConfiguration() } returns flowOf(projectConfiguration)
        every { configManager.observeDeviceConfiguration() } returns flowOf(deviceConfiguration)

        val result = useCase().first()

        assertThat(result.isRefreshing).isTrue()
        assertThat(result.isProjectRunning).isFalse()
    }

    @Test
    fun `returns combined state on multiple emissions of combined flow`() = runTest {
        coEvery { configManager.getProject() } returns null
        every { configManager.observeIsProjectRefreshing() } returns flowOf(true, false)
        every { configManager.observeProjectConfiguration() } returns flowOf(projectConfiguration)
        every { configManager.observeDeviceConfiguration() } returns flowOf(deviceConfiguration)

        val result = useCase().toList()

        assertThat(result.first().isRefreshing).isTrue()
        assertThat(result.last().isRefreshing).isFalse()
    }

    @Test
    fun `invoke untokenized list of modules`() = runTest {
        coEvery { configManager.getProject() } returns project
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

        every { configManager.observeIsProjectRefreshing() } returns flowOf(true)
        every { configManager.observeProjectConfiguration() } returns flowOf(projectConfiguration)
        every { configManager.observeDeviceConfiguration() } returns flowOf(deviceConfiguration)

        val result = useCase().first()

        assertThat(result.selectedModules).containsExactly(
            ModuleCount("moduleRaw", 1),
            ModuleCount("moduleUntokenized", 2),
        )
    }
}
