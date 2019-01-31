package com.simprints.id.di

import com.simprints.id.shared.PreferencesModuleForAnyTests
import com.simprints.id.tools.delegates.lazyVar
import com.simprints.libcommon.di.IAppComponent
import com.simprints.libcommon.di.IApplication
import com.simprints.testframework.common.di.DaggerForTests
import org.junit.Before

abstract class DaggerForAndroidTests : DaggerForTests {

    override lateinit var testAppComponent: IAppComponent
    override lateinit var app: IApplication

    open var module: AppModuleForAndroidTests by lazyVar {
        AppModuleForAndroidTests(app)
    }

    open var preferencesModule by lazyVar {
        PreferencesModuleForAnyTests()
    }

    @Before
    open fun setUp() {

        testAppComponent = DaggerAppComponentForAndroidTests
            .builder()
            .appModule(module)
            .preferencesModule(preferencesModule)
            .serializerModule(SerializerModule())
            .build()

        app.component = testAppComponent
    }
}
