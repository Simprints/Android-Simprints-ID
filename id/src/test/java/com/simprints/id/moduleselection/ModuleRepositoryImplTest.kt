package com.simprints.id.moduleselection

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.eventsystem.events_sync.down.EventDownSyncScopeRepository
import com.simprints.id.data.db.subject.SubjectRepository
import com.simprints.id.data.prefs.IdPreferencesManager
import com.simprints.id.moduleselection.model.Module
import com.simprints.id.testtools.TestApplication
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class)
@ExperimentalCoroutinesApi
class ModuleRepositoryImplTest {

    private val mockPreferencesManager: IdPreferencesManager = mockk(relaxed = true)
    private val mockSubjectRepository: SubjectRepository = mockk(relaxed = true)
    private val eventDownSyncScopeRepository: EventDownSyncScopeRepository = mockk(relaxed = true)

    private var repository = ModuleRepositoryImpl(
        mockPreferencesManager,
        mockSubjectRepository,
        eventDownSyncScopeRepository
    )

    @Before
    fun setUp() {
        configureMock()
    }

    @Test
    fun saveModules_shouldSaveSelectedModules() = runTest {
        val modules = listOf(
            Module("1", true),
            Module("2", true),
            Module("3", false),
            Module("4", true),
            Module("5", false)
        )

        val selectedModuleNames = modules.filter { it.isSelected }.map { it.name }.toSet()

        repository.saveModules(modules)

        coVerify {
            mockPreferencesManager.selectedModules = selectedModuleNames
        }
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

        coVerify { mockSubjectRepository.delete(any()) }
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
            eventDownSyncScopeRepository.deleteOperations(unselectedModules, any())
        }
    }

    @Test
    fun shouldReturnAllModules() {
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
        every {
            mockPreferencesManager.maxNumberOfModules
        } returns 10

        assertThat(repository.getMaxNumberOfModules()).isEqualTo(10)
    }

    private fun configureMock() {
        every {
            mockPreferencesManager.moduleIdOptions
        } returns setOf("a", "b", "c", "d")

        every {
            mockPreferencesManager.selectedModules
        } returns setOf("b", "c")
    }

}
