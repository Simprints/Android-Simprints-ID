package com.simprints.id.activities.settings.fragments.moduleselection

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.id.moduleselection.ModuleRepository
import com.simprints.id.moduleselection.model.Module
import com.simprints.id.services.sync.events.master.EventSyncManager
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.livedata.getOrAwaitValue
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ModuleSelectionViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private val repository: ModuleRepository = mockk()
    private val eventSyncManager: EventSyncManager = mockk()
    private lateinit var viewModel: ModuleSelectionViewModel

    @Before
    fun setUp() {
        configureMockRepository()
        viewModel = ModuleSelectionViewModel(
            repository,
            eventSyncManager,
            CoroutineScope(testCoroutineRule.testCoroutineDispatcher),
            testCoroutineRule.testCoroutineDispatcher
        )
    }

    @Test
    fun `should initialize the live data with the correct values`() {
        assertThat(viewModel.maxNumberOfModules.getOrAwaitValue()).isEqualTo(5)
        assertThat(viewModel.modulesList.getOrAwaitValue()).isEqualTo(
            listOf(
                Module("a", false),
                Module("b", true),
                Module("c", true),
                Module("d", false)
            )
        )
    }

    @Test
    fun shouldReturnAllModules() {
        val expected = listOf(
            Module("a", false),
            Module("b", true),
            Module("c", true),
            Module("d", false)
        )

        val actual = viewModel.modulesList.getOrAwaitValue()

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun shouldUpdateCachedModules() {
        val updatedModules = listOf(
            Module("a", true),
            Module("b", true),
            Module("c", true),
            Module("d", false)
        )
        viewModel.updateModules(updatedModules)
        val actual = viewModel.modulesList.value

        assertThat(actual).isEqualTo(updatedModules)
    }

    @Test
    fun shouldResetCachedModules() {
        val updatedModules = listOf(
            Module("a", true),
            Module("b", true),
            Module("c", true),
            Module("d", false)
        )
        viewModel.updateModules(updatedModules)
        viewModel.resetModules()
        val actual = viewModel.modulesList.value

        val expected = listOf(
            Module("a", false),
            Module("b", true),
            Module("c", true),
            Module("d", false)
        )

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun shouldSaveModulesAndTriggerSync() {
        val updatedModules = listOf(
            Module("a", true),
            Module("b", true),
            Module("c", true),
            Module("d", false)
        )
        viewModel.saveModules(updatedModules)
        coVerify(exactly = 1) { repository.saveModules(updatedModules) }
        coVerify(exactly = 1) { eventSyncManager.stop() }
        coVerify(exactly = 1) { eventSyncManager.sync() }
    }

    private fun configureMockRepository() {
        coEvery {
            repository.getModules()
        } returns listOf(
            Module("a", false),
            Module("b", true),
            Module("c", true),
            Module("d", false)
        )
        coEvery {
            repository.getMaxNumberOfModules()
        } returns 5
    }
}
