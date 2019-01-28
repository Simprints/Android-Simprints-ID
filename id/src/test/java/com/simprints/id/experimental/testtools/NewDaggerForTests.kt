package com.simprints.id.experimental.testtools

import com.simprints.id.di.AppComponentForTests
import com.simprints.id.di.AppModuleForTests
import com.simprints.id.di.DaggerAppComponentForTests
import com.simprints.id.shared.PreferencesModuleForAnyTests
import com.simprints.id.testUtils.roboletric.TestApplication
import com.simprints.id.tools.delegates.lazyVar

abstract class NewDaggerForTests {

    lateinit var testAppComponent: AppComponentForTests
    lateinit var app: TestApplication

    open var module: AppModuleForTests by lazyVar {
        AppModuleForTests(app)
    }
    open var preferencesModule by lazyVar {
        PreferencesModuleForAnyTests()
    }

    fun initComponent() {

        testAppComponent = DaggerAppComponentForTests
            .builder()
            .appModule(module)
            .preferencesModule(preferencesModule)
            .build()

        app.component = testAppComponent
    }
}
