package com.simprints.id.di

import com.simprints.id.testUtils.roboletric.TestApplication
import org.junit.Before

abstract class DaggerForTests {

    abstract var module: AppModuleForTests
    open var preferencesModule = PreferencesModule()
    lateinit var testAppComponent: AppComponentForTests
    lateinit var app: TestApplication

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
