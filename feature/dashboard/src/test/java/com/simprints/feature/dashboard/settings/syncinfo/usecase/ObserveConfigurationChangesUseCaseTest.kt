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

    private lateinit var useCase: ObserveConfigurationChangesUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        useCase = ObserveConfigurationChangesUseCase(
            configRepository = configRepository,
            tokenizationProcessor = tokenizationProcessor,
            enrolmentRecordRepository = enrolmentRepository,
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

        val result = useCase().toList()

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
}
