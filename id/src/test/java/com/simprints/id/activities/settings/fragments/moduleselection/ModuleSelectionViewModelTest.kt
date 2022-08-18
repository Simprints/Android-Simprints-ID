package com.simprints.id.activities.settings.fragments.moduleselection

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.id.moduleselection.ModuleRepository
import com.simprints.id.moduleselection.model.Module
import com.simprints.id.services.sync.events.master.EventSyncManager
import com.simprints.id.testtools.TestApplication
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class ModuleSelectionViewModelTest {

    private val repository: ModuleRepository = mockk()
    private val eventSyncManager: EventSyncManager = mockk()
    private lateinit var viewModel: ModuleSelectionViewModel

    @Before
    fun setUp() {
        configureMockRepository()
        viewModel = ModuleSelectionViewModel(repository, eventSyncManager)
    }

    @Test
    fun shouldReturnAllModules() {
        val expected = listOf(
            Module("a", false),
            Module("b", true),
            Module("c", true),
            Module("d", false)
        )

        val actual = viewModel.modulesList.value

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

    @Test
    fun shouldReturnMaxNumberOfModules() {
        assertThat(viewModel.getMaxNumberOfModules()).isEqualTo(5)
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    private fun configureMockRepository() {
        every {
            repository.getModules()
        } returns listOf(
            Module("a", false),
            Module("b", true),
            Module("c", true),
            Module("d", false)
        )
        every {
            repository.getMaxNumberOfModules()
        } returns 5
    }
}
