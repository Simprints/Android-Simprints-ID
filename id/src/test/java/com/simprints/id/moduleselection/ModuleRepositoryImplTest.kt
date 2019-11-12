package com.simprints.id.moduleselection

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.verify
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.moduleselection.model.Module
import com.simprints.id.testtools.TestApplication
import com.simprints.testtools.common.syntax.mock
import com.simprints.testtools.common.syntax.whenever
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class)
class ModuleRepositoryImplTest {

    private val preferencesManager: PreferencesManager = mock()
    private val crashReportManager: CrashReportManager = mock()
    private var repository = ModuleRepositoryImpl(preferencesManager, crashReportManager)

    @Before
    fun setUp() {
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

        verify(crashReportManager).setModuleIdsCrashlyticsKey(preferencesManager.selectedModules)
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

    private fun configureMock() {
        whenever {
            preferencesManager.moduleIdOptions
        } thenReturn setOf("a", "b", "c", "d")
        whenever {
            preferencesManager.selectedModules
        } thenReturn setOf("b", "c")
    }

}
