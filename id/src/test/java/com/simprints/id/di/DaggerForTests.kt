package com.simprints.id.di

import com.simprints.id.shared.PreferencesModuleForAnyTests
import com.simprints.id.testUtils.roboletric.TestApplication
import com.simprints.id.tools.delegates.lazyVar
import org.junit.Before

abstract class DaggerForTests {

    lateinit var testAppComponent: AppComponentForTests
    lateinit var app: TestApplication

    open var module: AppModuleForTests by lazyVar {
        AppModuleForTests(app)
    }
    open var preferencesModule by lazyVar {
        PreferencesModuleForAnyTests()
    }

    @Before
    open fun setUp() {

        testAppComponent = DaggerAppComponentForTests
            .builder()
            .appModule(module)
            .preferencesModule(preferencesModule)
            .build()

        app.component = testAppComponent
    }
}
