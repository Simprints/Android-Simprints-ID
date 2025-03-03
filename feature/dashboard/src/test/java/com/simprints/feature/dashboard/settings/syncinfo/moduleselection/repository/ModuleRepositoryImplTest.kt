package com.simprints.feature.dashboard.settings.syncinfo.moduleselection.repository

import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.infra.config.store.models.DeviceConfiguration
import com.simprints.infra.config.store.models.DownSynchronizationConfiguration
import com.simprints.infra.config.store.models.GeneralConfiguration
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.enrolment.records.repository.EnrolmentRecordRepository
import com.simprints.infra.eventsync.EventSyncManager
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ModuleRepositoryImplTest {
    @MockK
    lateinit var downSynchronizationConfiguration: DownSynchronizationConfiguration

    @MockK
    lateinit var projectConfiguration: ProjectConfiguration

    @MockK
    lateinit var configManager: ConfigManager

    @MockK
    lateinit var enrolmentRecordRepository: EnrolmentRecordRepository

    @MockK
    lateinit var eventSyncManager: EventSyncManager

    private lateinit var repository: ModuleRepositoryImpl

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        every { projectConfiguration.general.modalities } returns listOf(GeneralConfiguration.Modality.FINGERPRINT)
        every { projectConfiguration.synchronization.down } returns downSynchronizationConfiguration
        coEvery { configManager.getProjectConfiguration() } returns projectConfiguration

        every { downSynchronizationConfiguration.moduleOptions } returns listOf("a", "b", "c", "d").map(String::asTokenizableRaw)
        coEvery {
            configManager.getDeviceConfiguration()
        } returns DeviceConfiguration("", listOf("b", "c").map(TokenizableString::Tokenized), "")

        repository = ModuleRepositoryImpl(
            configManager,
            enrolmentRecordRepository,
            eventSyncManager,
        )
    }

    @Test
    fun saveModules_shouldSaveSelectedModules() = runTest {
        val updateConfigFn = slot<suspend (DeviceConfiguration) -> DeviceConfiguration>()
        coEvery { configManager.updateDeviceConfiguration(capture(updateConfigFn)) } returns Unit
        val modules = listOf(
            Module("1".asTokenizableRaw(), true),
            Module("2".asTokenizableRaw(), true),
            Module("3".asTokenizableRaw(), false),
            Module("4".asTokenizableRaw(), true),
            Module("5".asTokenizableRaw(), false),
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
            Module("1".asTokenizableRaw(), true),
            Module("2".asTokenizableRaw(), true),
            Module("3".asTokenizableRaw(), false),
            Module("4".asTokenizableRaw(), true),
            Module("5".asTokenizableRaw(), false),
        )

        repository.saveModules(modules)

        coVerify { enrolmentRecordRepository.delete(any()) }
    }

    @Test
    fun saveModules_shouldDeleteOperationsForUnselectedModules() = runTest {
        val modules = listOf(
            Module("a".asTokenizableRaw(), true),
            Module("b".asTokenizableRaw(), false),
            Module("c".asTokenizableRaw(), false),
            Module("d".asTokenizableRaw(), true),
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
            Module("a".asTokenizableRaw(), false),
            Module("b".asTokenizableRaw(), true),
            Module("c".asTokenizableRaw(), true),
            Module("d".asTokenizableRaw(), false),
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
