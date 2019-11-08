package com.simprints.id.moduleselection

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.verify
import com.simprints.id.commontesttools.di.TestAppModule
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
class ModuleRepositoryTest {

    private val app = ApplicationProvider.getApplicationContext<TestApplication>()
    private val appModule = TestAppModule(app, crashReportManagerRule = DependencyRule.MockRule)
    private val preferencesModule = TestPreferencesModule(
        settingsPreferencesManagerRule = DependencyRule.MockRule
    )

    private lateinit var repository: ModuleRepository

    @Before
    fun setUp() {
        UnitTestConfig(this, appModule, preferencesModule).fullSetup()
        repository = ModuleRepository(app.component)
        configureMock()
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

        repository.updateModules(selectedModules)

        verify(repository.crashReportManager)
            .setModuleIdsCrashlyticsKey(repository.preferencesManager.selectedModules)
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

        assertThat(actual.value).isEqualTo(expected)
    }

    @Test
    fun shouldReturnSelectedModules() {
        val expected = listOf(
            Module("b", true),
            Module("c", true)
        )

        val actual = repository.getModules().value?.filter { it.isSelected }

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun shouldFetchMaxNumberOfModulesFromRemoteConfig() {
        whenever {
            repository.preferencesManager.maxNumberOfModules
        } thenReturn 10

        assertThat(repository.getMaxNumberOfModules()).isEqualTo(10)
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
