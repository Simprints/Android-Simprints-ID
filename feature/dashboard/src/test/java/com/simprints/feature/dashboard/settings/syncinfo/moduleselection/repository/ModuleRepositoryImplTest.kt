package com.simprints.feature.dashboard.settings.syncinfo.moduleselection.repository

import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.modality.Modes
import com.simprints.core.domain.tokenization.asTokenizedRaw
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.DeviceConfiguration
import com.simprints.infra.config.domain.models.DownSynchronizationConfiguration
import com.simprints.infra.config.domain.models.GeneralConfiguration
import com.simprints.infra.config.domain.models.ProjectConfiguration
import com.simprints.infra.enrolment.records.EnrolmentRecordManager
import com.simprints.infra.eventsync.EventSyncManager
import io.mockk.*
import io.mockk.impl.annotations.MockK

import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ModuleRepositoryImplTest {

    @MockK
    lateinit var downSynchronizationConfiguration: DownSynchronizationConfiguration

    @MockK
    lateinit var projectConfiguration: ProjectConfiguration

    @MockK
    lateinit var mockConfigManager: ConfigManager

    @MockK
    lateinit var enrolmentRecordManager: EnrolmentRecordManager

    @MockK
    lateinit var eventSyncManager: EventSyncManager


    private lateinit var repository: ModuleRepositoryImpl

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        every { projectConfiguration.general.modalities } returns listOf(GeneralConfiguration.Modality.FINGERPRINT)
        every { projectConfiguration.synchronization.down } returns downSynchronizationConfiguration
        coEvery { mockConfigManager.getProjectConfiguration() } returns projectConfiguration

        every { downSynchronizationConfiguration.moduleOptions } returns listOf("a", "b", "c", "d").map(String::asTokenizedRaw)
        coEvery {
            mockConfigManager.getDeviceConfiguration()
        } returns DeviceConfiguration("", listOf("b", "c"), "")


        repository = ModuleRepositoryImpl(
            mockConfigManager,
            enrolmentRecordManager,
            eventSyncManager
        )
    }

    @Test
    fun saveModules_shouldSaveSelectedModules() = runTest {
        val updateConfigFn = slot<suspend (DeviceConfiguration) -> DeviceConfiguration>()
        coEvery { mockConfigManager.updateDeviceConfiguration(capture(updateConfigFn)) } returns Unit
        val modules = listOf(
            Module("1".asTokenizedRaw(), true),
            Module("2".asTokenizedRaw(), true),
            Module("3".asTokenizedRaw(), false),
            Module("4".asTokenizedRaw(), true),
            Module("5".asTokenizedRaw(), false)
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
            Module("1".asTokenizedRaw(), true),
            Module("2".asTokenizedRaw(), true),
            Module("3".asTokenizedRaw(), false),
            Module("4".asTokenizedRaw(), true),
            Module("5".asTokenizedRaw(), false)
        )

        repository.saveModules(modules)

        coVerify { enrolmentRecordManager.delete(any()) }
    }

    @Test
    fun saveModules_shouldDeleteOperationsForUnselectedModules() = runTest {
        val modules = listOf(
            Module("a".asTokenizedRaw(), true),
            Module("b".asTokenizedRaw(), false),
            Module("c".asTokenizedRaw(), false),
            Module("d".asTokenizedRaw(), true)
        )

        val unselectedModules = listOf("b", "c")

        repository.saveModules(modules)

        coVerify(exactly = 1) {
            eventSyncManager.deleteModules(unselectedModules)
        }
    }

    @Test
    fun shouldReturnAllModules() = runTest {
        val expected = listOf(
            Module("a".asTokenizedRaw(), false),
            Module("b".asTokenizedRaw(), true),
            Module("c".asTokenizedRaw(), true),
            Module("d".asTokenizedRaw(), false)
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
