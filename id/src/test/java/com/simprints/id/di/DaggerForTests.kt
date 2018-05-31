package com.simprints.id.di

import com.simprints.id.testUtils.roboletric.TestApplication
import org.junit.Before

open class DaggerForTests {

    open lateinit var module: AppModuleForTests
    lateinit var testAppComponent: AppComponentForTests
    lateinit var app: TestApplication

    @Before
    open fun setUp() {

        testAppComponent = DaggerAppComponentForTests
            .builder()
            .appModule(module)
            .build()

        app.component = testAppComponent
    }
}
