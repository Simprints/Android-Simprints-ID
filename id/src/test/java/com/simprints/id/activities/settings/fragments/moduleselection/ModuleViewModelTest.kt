package com.simprints.id.activities.settings.fragments.moduleselection

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.id.commontesttools.di.TestPreferencesModule
import com.simprints.id.moduleselection.model.Module
import com.simprints.id.testtools.TestApplication
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.testtools.common.di.DependencyRule
import com.simprints.testtools.common.syntax.whenever
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class)
class ModuleViewModelTest {

    private val app = ApplicationProvider.getApplicationContext<TestApplication>()
    private val preferencesModule = TestPreferencesModule(settingsPreferencesManagerRule = DependencyRule.MockRule)

    private lateinit var viewModel: ModuleViewModel

    @Before
    fun setUp() {
        UnitTestConfig(this, null, preferencesModule).fullSetup()
        viewModel = ModuleViewModel(app)
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

    private fun configureMock() {
        whenever {
            app.component.getPreferencesManager().moduleIdOptions
        } thenReturn setOf("a", "b", "c", "d")
        whenever {
            app.component.getPreferencesManager().selectedModules
        } thenReturn setOf("b", "c")
    }

}
