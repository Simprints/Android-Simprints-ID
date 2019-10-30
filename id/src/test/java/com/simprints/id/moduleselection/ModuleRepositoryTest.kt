package com.simprints.id.moduleselection

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.verify
import com.simprints.id.commontesttools.di.TestAppModule
import com.simprints.id.commontesttools.di.TestPreferencesModule
import com.simprints.id.moduleselection.model.Module
import com.simprints.id.testtools.TestApplication
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.testtools.common.di.DependencyRule
import com.simprints.testtools.common.syntax.whenever
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class)
class ModuleRepositoryTest {

    private val app = ApplicationProvider.getApplicationContext<TestApplication>()
    private val appModule = TestAppModule(app, crashReportManagerRule = DependencyRule.MockRule)
    private val preferencesModule = TestPreferencesModule(settingsPreferencesManagerRule = DependencyRule.MockRule)

    private lateinit var repository: ModuleRepository

    @Before
    fun setUp() {
        UnitTestConfig(this, appModule, preferencesModule).fullSetup()
        repository = ModuleRepository(app.component)
        configureMock()
    }

    @Test
    fun whenSelectingNoModules_shouldTriggerCallback() {
        val selectedModules = emptyList<Module>()

        repository.setSelectedModules(selectedModules)

        verify(repository.callback).noModulesSelected()
    }

    @Test
    fun whenSelectingTooManyModules_shouldTriggerCallback() {
        val selectedModules = listOf(
            Module("1", true),
            Module("2", true),
            Module("3", true),
            Module("4", true),
            Module("5", true),
            Module("6", true),
            Module("7", true)
        )

        repository.setSelectedModules(selectedModules)

        verify(repository.callback).tooManyModulesSelected(maxAllowed = 6)
    }

    @Test
    fun whenSelectingAnAcceptableNumberOfModules_shouldLogOnCrashReport() {
        val selectedModules = listOf(
            Module("1", true),
            Module("2", true),
            Module("3", true),
            Module("4", true),
            Module("5", true)
        )

        repository.setSelectedModules(selectedModules)

        verify(repository.crashReportManager)
            .setModuleIdsCrashlyticsKey(repository.preferencesManager.selectedModules)
    }

    @Test
    fun shouldReturnAvailableModules() {
        val expected = listOf(
            Module("a", false),
            Module("b", false),
            Module("c", false),
            Module("d", false)
        )

        val actual = repository.getAvailableModules()

        assertThat(actual, `is`(expected))
    }

    @Test
    fun shouldReturnSelectedModules() {
        val expected = listOf(
            Module("b", true),
            Module("c", true)
        )

        val actual = repository.getSelectedModules()

        assertThat(actual, `is`(expected))
    }

    private fun configureMock() {
        whenever {
            repository.preferencesManager.moduleIdOptions
        } thenReturn setOf("a", "b", "c", "d")
        whenever {
            repository.preferencesManager.selectedModules
        } thenReturn setOf("b", "c")
    }

}
