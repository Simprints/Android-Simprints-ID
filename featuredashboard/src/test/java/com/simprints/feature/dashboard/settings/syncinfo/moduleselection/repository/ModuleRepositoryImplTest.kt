package com.simprints.feature.dashboard.settings.syncinfo.moduleselection.repository

import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.modality.Modes
import com.simprints.eventsystem.events_sync.down.EventDownSyncScopeRepository
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.DeviceConfiguration
import com.simprints.infra.config.domain.models.DownSynchronizationConfiguration
import com.simprints.infra.config.domain.models.GeneralConfiguration
import com.simprints.infra.config.domain.models.ProjectConfiguration
import com.simprints.infra.enrolment.records.EnrolmentRecordManager
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ModuleRepositoryImplTest {

    private val downSynchronizationConfiguration = mockk<DownSynchronizationConfiguration>()
    private val projectConfiguration = mockk<ProjectConfiguration> {
        every { general } returns mockk {
            every { modalities } returns listOf(GeneralConfiguration.Modality.FINGERPRINT)
        }
        every { synchronization } returns mockk {
            every { down } returns downSynchronizationConfiguration
        }
    }
    private val mockConfigManager: ConfigManager = mockk(relaxed = true)
    private val enrolmentRecordManager: EnrolmentRecordManager = mockk(relaxed = true)
    private val eventDownSyncScopeRepository: EventDownSyncScopeRepository = mockk(relaxed = true)

    private var repository = ModuleRepositoryImpl(
        mockConfigManager,
        enrolmentRecordManager,
        eventDownSyncScopeRepository
    )

    @Before
    fun setUp() {
        coEvery { mockConfigManager.getProjectConfiguration() } returns projectConfiguration

        every { downSynchronizationConfiguration.moduleOptions } returns listOf("a", "b", "c", "d")
        coEvery {
            mockConfigManager.getDeviceConfiguration()
        } returns DeviceConfiguration("", listOf("b", "c"), "")
    }

    @Test
    fun saveModules_shouldSaveSelectedModules() = runTest {
        val updateConfigFn = slot<suspend (DeviceConfiguration) -> DeviceConfiguration>()
        coEvery { mockConfigManager.updateDeviceConfiguration(capture(updateConfigFn)) } returns Unit
        val modules = listOf(
            Module("1", true),
            Module("2", true),
            Module("3", false),
            Module("4", true),
            Module("5", false)
        )

        val selectedModuleNames = modules.filter { it.isSelected }.map { it.name }.toSet()

        repository.saveModules(modules)

        val updatedConfig = updateConfigFn.captured(DeviceConfiguration("", listOf(), ""))
        // Comparing string representation as when executing the lambda captured in the mock it will
        // not return an ArrayList but a LinkedHashMap.
        assertThat(updatedConfig.selectedModules.toString()).isEqualTo(selectedModuleNames.toString())
    }

    @Test
    fun saveModules_shouldDeleteRecordsFromUnselectedModules() = runTest {
        val modules = listOf(
            Module("1", true),
            Module("2", true),
            Module("3", false),
            Module("4", true),
            Module("5", false)
        )

        repository.saveModules(modules)

        coVerify { enrolmentRecordManager.delete(any()) }
    }

    @Test
    fun saveModules_shouldDeleteOperationsForUnselectedModules() = runTest {
        val modules = listOf(
            Module("a", true),
            Module("b", false),
            Module("c", false),
            Module("d", true)
        )

        val unselectedModules = listOf("b", "c")

        repository.saveModules(modules)

        coVerify(exactly = 1) {
            eventDownSyncScopeRepository.deleteOperations(unselectedModules, listOf(Modes.FINGERPRINT))
        }
    }

    @Test
    fun shouldReturnAllModules() = runTest {
        val expected = listOf(
            Module("a", false),
            Module("b", true),
            Module("c", true),
            Module("d", false)
        )

        val actual = repository.getModules()

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun shouldFetchMaxNumberOfModulesFromRemoteConfig() = runTest {
        every { downSynchronizationConfiguration.maxNbOfModules } returns 10

        assertThat(repository.getMaxNumberOfModules()).isEqualTo(10)
    }
}
