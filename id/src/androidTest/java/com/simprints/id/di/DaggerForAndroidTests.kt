package com.simprints.id.di

import com.simprints.id.Application
import org.junit.Before

open class DaggerForAndroidTests {

    open lateinit var module: AppModuleForAndroidTests
    lateinit var testAppComponent: AppComponentForAndroidTests
    lateinit var app: Application

    @Before
    open fun setUp() {

        testAppComponent = DaggerAppComponentForAndroidTests
            .builder()
            .appModule(module)
            .build()

        app.component = testAppComponent
    }
}
