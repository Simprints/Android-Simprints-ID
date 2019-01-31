package com.simprints.id.di

import com.simprints.id.shared.PreferencesModuleForAnyTests
import com.simprints.id.tools.delegates.lazyVar
import com.simprints.libcommon.di.IAppComponent
import com.simprints.libcommon.di.IApplication
import com.simprints.testframework.common.di.DaggerForTests

abstract class DaggerForUnitTests : DaggerForTests {

    override lateinit var testAppComponent: IAppComponent
    override lateinit var app: IApplication

    open var module: AppModuleForTests by lazyVar {
        AppModuleForTests(app)
    }
    open var preferencesModule by lazyVar {
        PreferencesModuleForAnyTests()
    }

    override fun initComponent() {
        testAppComponent = DaggerAppComponentForTests
            .builder()
            .appModule(module)
            .preferencesModule(preferencesModule)
            .build()

        app.component = testAppComponent
    }
}
