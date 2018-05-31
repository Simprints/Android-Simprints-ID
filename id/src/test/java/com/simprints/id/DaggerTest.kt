package com.simprints.id

import com.simprints.id.testUtils.roboletric.TestApplication
import org.junit.Before

open class DaggerTest {

    open lateinit var module: TestAppModule
    lateinit var testAppComponent: TestAppComponent
    lateinit var app: TestApplication

    @Before
    open fun setUp() {

        testAppComponent = DaggerTestAppComponent
            .builder()
            .appModule(module)
            .build()

        app.component = testAppComponent
    }
}
