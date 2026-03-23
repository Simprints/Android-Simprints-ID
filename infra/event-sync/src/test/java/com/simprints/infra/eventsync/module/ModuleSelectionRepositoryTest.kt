package com.simprints.infra.eventsync.module

import com.google.common.truth.*
import com.google.common.truth.Truth.*
import com.simprints.core.domain.common.Modality
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.DeviceConfiguration
import com.simprints.infra.config.store.models.DownSynchronizationConfiguration
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.enrolment.records.repository.EnrolmentRecordRepository
import com.simprints.infra.events.device.DeviceEventTracker
import com.simprints.infra.eventsync.DeleteModulesUseCase
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ModuleSelectionRepositoryTest {
    @MockK
    lateinit var downSynchronizationConfiguration: DownSynchronizationConfiguration

    @MockK
    lateinit var projectConfiguration: ProjectConfiguration

    @MockK
    lateinit var configRepository: ConfigRepository

    @MockK
    lateinit var enrolmentRecordRepository: EnrolmentRecordRepository

    @MockK
    lateinit var deleteModules: DeleteModulesUseCase

    @MockK
    private lateinit var deviceEventTracker: DeviceEventTracker

    private lateinit var repository: ModuleSelectionRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        every { projectConfiguration.general.modalities } returns listOf(Modality.FINGERPRINT)
        every { projectConfiguration.synchronization.down } returns downSynchronizationConfiguration
        coEvery { configRepository.getProjectConfiguration() } returns projectConfiguration

        every { downSynchronizationConfiguration.simprints?.moduleOptions } returns listOf("a", "b", "c", "d").map(
            String::asTokenizableRaw,
        )
        coEvery {
            configRepository.getDeviceConfiguration()
        } returns DeviceConfiguration("", listOf("b", "c").map(TokenizableString::Tokenized), "")

        coJustRun { deviceEventTracker.trackDeviceConfigurationUpdatedEvent(any(), any()) }

        repository = ModuleSelectionRepository(
            configRepository,
            deleteModules,
            enrolmentRecordRepository,
            deviceEventTracker,
        )
    }

    @Test
    fun forceModuleSelection_shouldSaveSelectedModules() = runTest {
        val updateConfigFn = slot<suspend (DeviceConfiguration) -> DeviceConfiguration>()
        coEvery { configRepository.updateDeviceConfiguration(capture(updateConfigFn)) } returns Unit
        val selectedModules = listOf(
            "a".asTokenizableRaw(),
            "d".asTokenizableRaw(),
        )

        repository.forceModuleSelection(selectedModules, false)

        val updatedConfig = updateConfigFn.captured(DeviceConfiguration("", listOf(), ""))
        // Comparing string representation as when executing the lambda captured in the mock it will
        // not return an ArrayList but a LinkedHashMap.
        assertThat(updatedConfig.selectedModules).containsExactlyElementsIn(selectedModules)
        deleteModules(listOf("b", "d"))
    }

    @Test
    fun saveModules_shouldSaveSelectedModules() = runTest {
        val updateConfigFn = slot<suspend (DeviceConfiguration) -> DeviceConfiguration>()
        coEvery { configRepository.updateDeviceConfiguration(capture(updateConfigFn)) } returns Unit
        val modules = listOf(
            SelectableModule("1".asTokenizableRaw(), true),
            SelectableModule("2".asTokenizableRaw(), true),
            SelectableModule("3".asTokenizableRaw(), false),
            SelectableModule("4".asTokenizableRaw(), true),
            SelectableModule("5".asTokenizableRaw(), false),
        )

        val selectedModuleNames = modules.filter { it.isSelected }.map { it.name }.toSet()

        repository.saveModules(modules)

        val updatedConfig = updateConfigFn.captured(DeviceConfiguration("", listOf(), ""))
        // Comparing string representation as when executing the lambda captured in the mock it will
        // not return an ArrayList but a LinkedHashMap.
        assertThat(updatedConfig.selectedModules.toString())
            .isEqualTo(selectedModuleNames.map(TokenizableString::value).toString())
    }

    @Test
    fun saveModules_shouldDeleteRecordsFromUnselectedModules() = runTest {
        val modules = listOf(
            SelectableModule("1".asTokenizableRaw(), true),
            SelectableModule("2".asTokenizableRaw(), true),
            SelectableModule("3".asTokenizableRaw(), false),
            SelectableModule("4".asTokenizableRaw(), true),
            SelectableModule("5".asTokenizableRaw(), false),
        )

        repository.saveModules(modules)

        coVerify { enrolmentRecordRepository.delete(any()) }
    }

    @Test
    fun saveModules_shouldDeleteOperationsForUnselectedModules() = runTest {
        val modules = listOf(
            SelectableModule("a".asTokenizableRaw(), true),
            SelectableModule("b".asTokenizableRaw(), false),
            SelectableModule("c".asTokenizableRaw(), false),
            SelectableModule("d".asTokenizableRaw(), true),
        )

        val unselectedModules = listOf("b", "c")

        repository.saveModules(modules)

        coVerify(exactly = 1) {
            deleteModules(unselectedModules)
        }
    }

    @Test
    fun saveModules_shouldTrackModulesInEvent() = runTest {
        val updateConfigFn = slot<suspend (DeviceConfiguration) -> DeviceConfiguration>()
        coEvery { configRepository.updateDeviceConfiguration(capture(updateConfigFn)) } returns Unit
        val modules = listOf(
            SelectableModule("a".asTokenizableRaw(), true),
            SelectableModule("b".asTokenizableRaw(), false),
            SelectableModule("c".asTokenizableRaw(), false),
            SelectableModule("d".asTokenizableRaw(), true),
        )

        repository.saveModules(modules)
        updateConfigFn.captured(DeviceConfiguration("", listOf(), ""))

        coVerify(exactly = 1) {
            deviceEventTracker.trackDeviceConfigurationUpdatedEvent(
                withArg { config ->
                    Truth.assertThat(config.selectedModules).containsExactly(
                        "a".asTokenizableRaw(),
                        "d".asTokenizableRaw(),
                    )
                },
                true,
            )
        }
    }

    @Test
    fun shouldReturnAllModules() = runTest {
        val expected = listOf(
            SelectableModule("a".asTokenizableRaw(), false),
            SelectableModule("b".asTokenizableRaw(), true),
            SelectableModule("c".asTokenizableRaw(), true),
            SelectableModule("d".asTokenizableRaw(), false),
        )

        val actual = repository.getModules()

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun shouldFetchMaxNumberOfModulesFromRemoteConfig() = runTest {
        every { downSynchronizationConfiguration.simprints?.maxNbOfModules } returns 10

        assertThat(repository.getMaxNumberOfModules()).isEqualTo(10)
    }
}
