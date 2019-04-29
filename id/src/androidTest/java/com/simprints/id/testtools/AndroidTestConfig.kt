package com.simprints.id.testtools

import androidx.test.core.app.ApplicationProvider
import com.simprints.id.Application
import com.simprints.id.commontesttools.di.TestAppModule
import com.simprints.id.commontesttools.di.TestPreferencesModule
import com.simprints.id.testtools.di.AppComponentForAndroidTests
import com.simprints.id.testtools.di.DaggerAppComponentForAndroidTests
import com.simprints.testtools.common.di.injectClassFromComponent
import io.realm.Realm

class AndroidTestConfig<T : Any>(
    private val test: T,
    private val appModule: TestAppModule? = null,
    private val preferencesModule: TestPreferencesModule? = null
) {

    private val app = ApplicationProvider.getApplicationContext<Application>()
    private lateinit var testAppComponent: AppComponentForAndroidTests

    fun fullSetup() =
        initAndInjectComponent()
            .initRealm()
            .initModules()

    fun initAndInjectComponent() =
        initComponent().inject()

    private fun initComponent() = also {

        testAppComponent = DaggerAppComponentForAndroidTests.builder()
            .application(app)
            .appModule(appModule ?: TestAppModule(app))
            .preferencesModule(preferencesModule ?: TestPreferencesModule())
            .build()

        app.component = testAppComponent
    }

    private fun inject() = also {
        injectClassFromComponent(testAppComponent, test)
    }

    fun initRealm() = also {
        Realm.init(app)
    }

    fun initModules() = also {
        app.initModules()
    }
}
