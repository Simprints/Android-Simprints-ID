package com.simprints.id.testUtils.di

import com.simprints.id.Application
import com.simprints.id.di.AppComponent
import com.simprints.id.shared.DaggerForTests
import com.simprints.id.shared.PreferencesModuleForAnyTests
import com.simprints.id.tools.delegates.lazyVar

abstract class DaggerForUnitTests : DaggerForTests {

    override lateinit var testAppComponent: AppComponent
    override lateinit var app: Application

    open var module: AppModuleForTests by lazyVar {
        AppModuleForTests(app)
    }
    open var preferencesModule by lazyVar {
        PreferencesModuleForAnyTests()
    }

    override fun initComponent() {
        testAppComponent = DaggerAppComponentForTests.builder()
            .appModule(module)
            .preferencesModule(preferencesModule)
            .build()

        app.component = testAppComponent
    }
}
