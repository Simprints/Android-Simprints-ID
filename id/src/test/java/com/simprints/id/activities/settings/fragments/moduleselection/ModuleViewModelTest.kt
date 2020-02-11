package com.simprints.id.activities.settings.fragments.moduleselection

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.id.moduleselection.ModuleRepository
import com.simprints.id.moduleselection.model.Module
import io.mockk.every
import io.mockk.mockk
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
class ModuleViewModelTest {

    private val repository: ModuleRepository = mockk()
    private lateinit var viewModel: ModuleViewModel

    @Before
    fun setUp() {
        configureMock()
        viewModel = ModuleViewModel(repository)
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
    fun shouldReturnSelectedModules() {
        val expected = listOf(
            Module("b", true),
            Module("c", true)
        )

        val actual = viewModel.modulesList.value?.filter { it.isSelected }

        assertThat(actual).isEqualTo(expected)
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    private fun configureMock() {
        every {
            repository.getModules()
        } returns listOf(
            Module("a", false),
            Module("b", true),
            Module("c", true),
            Module("d", false)
        )
    }
}
