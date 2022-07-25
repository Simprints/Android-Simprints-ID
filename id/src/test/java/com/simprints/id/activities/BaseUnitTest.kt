package com.simprints.id.activities

import androidx.test.core.app.ApplicationProvider
import com.simprints.id.testtools.TestApplication
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.id.testtools.di.TestAppModule
import com.simprints.id.testtools.di.TestPreferencesModule
import com.simprints.testtools.common.di.DependencyRule.MockkRule
import com.simprints.testtools.common.di.DependencyRule.SpykRule
import kotlinx.coroutines.ExperimentalCoroutinesApi

/*
* A base class to be used in all unit tests.
 */
@ExperimentalCoroutinesApi
open class BaseUnitTest {
    private val app = ApplicationProvider.getApplicationContext() as TestApplication

    private val preferencesModule by lazy {
        TestPreferencesModule(
            settingsPreferencesManagerRule = SpykRule
        )
    }

    private val module by lazy {
        TestAppModule(
            app,
            dbManagerRule = MockkRule,
            sessionEventsLocalDbManagerRule = MockkRule
        )
    }

    open fun setUp() {
        UnitTestConfig(module, preferencesModule).fullSetup()
    }
}
