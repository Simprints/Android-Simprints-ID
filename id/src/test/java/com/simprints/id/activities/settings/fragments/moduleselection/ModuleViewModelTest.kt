package com.simprints.id.activities.settings.fragments.moduleselection

import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.id.moduleselection.ModuleRepository
import com.simprints.id.moduleselection.model.Module
import com.simprints.testtools.common.syntax.mock
import com.simprints.testtools.common.syntax.whenever
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
class ModuleViewModelTest {

    private val repository: ModuleRepository = mock()
    private val viewModel = ModuleViewModel(repository)

    @Before
    fun setUp() {
        configureMock()
    }

    @Test
    fun shouldReturnAllModules() {
        val expected = listOf(
            Module("a", false),
            Module("b", true),
            Module("c", true),
            Module("d", false)
        )

        val actual = viewModel.getModules().value

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun shouldReturnSelectedModules() {
        val expected = listOf(
            Module("b", true),
            Module("c", true)
        )

        val actual = viewModel.getModules().value?.filter { it.isSelected }

        assertThat(actual).isEqualTo(expected)
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    private fun configureMock() {
        whenever {
            repository.getModules()
        } thenReturn MutableLiveData<List<Module>>().apply {
            value = listOf(
                Module("a", false),
                Module("b", true),
                Module("c", true),
                Module("d", false)
            )
        }
    }

}
