package com.simprints.id.activities

import androidx.test.core.app.ApplicationProvider
import com.simprints.id.testtools.TestApplication
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.id.testtools.di.TestAppModule
import com.simprints.testtools.common.di.DependencyRule.MockkRule

/*
* A base class to be used in all unit tests.
 */
open class BaseUnitTest {
    private val app = ApplicationProvider.getApplicationContext() as TestApplication

    private val module by lazy {
        TestAppModule(
            app,
            dbManagerRule = MockkRule,
            sessionEventsLocalDbManagerRule = MockkRule
        )
    }

    open fun setUp() {
        UnitTestConfig(module).fullSetup()
    }
}
