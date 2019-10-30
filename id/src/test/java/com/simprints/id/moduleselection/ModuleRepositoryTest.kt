package com.simprints.id.moduleselection

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.verify
import com.simprints.id.commontesttools.di.TestAppModule
import com.simprints.id.moduleselection.model.Module
import com.simprints.id.testtools.TestApplication
import com.simprints.id.testtools.UnitTestConfig
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class)
class ModuleRepositoryTest {

    private val app = ApplicationProvider.getApplicationContext<TestApplication>()
    private val appModule = TestAppModule(app)

    private lateinit var repository: ModuleRepository

    @Before
    fun setUp() {
        UnitTestConfig(this, appModule).fullSetup()
        repository = ModuleRepository(app.component)
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

        verify(repository.callback).tooManyModulesSelected()
    }

}
