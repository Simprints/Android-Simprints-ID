package com.simprints.id.testtools

import androidx.test.core.app.ApplicationProvider
import com.simprints.id.commontesttools.TestApplication
import com.simprints.id.commontesttools.di.TestAppModule
import com.simprints.id.commontesttools.di.TestPreferencesModule
import com.simprints.id.testtools.di.AppComponentForAndroidTests
import com.simprints.id.testtools.di.DaggerAppComponentForAndroidTests
import com.simprints.testframework.common.dagger.injectClassFromComponent

class AndroidTestConfig<T : Any>(
    private val test: T,
    private val appModule: TestAppModule? = null,
    private val preferencesModule: TestPreferencesModule? = null
) {

    private val app = ApplicationProvider.getApplicationContext() as TestApplication
    private lateinit var testAppComponent: AppComponentForAndroidTests

    fun fullSetup() =
        initComponent().inject()

    fun initComponent(): AndroidTestConfig<T> {

        testAppComponent = DaggerAppComponentForAndroidTests.builder()
            .appModule(appModule ?: TestAppModule(app))
            .preferencesModule(preferencesModule ?: TestPreferencesModule())
            .build()

        app.component = testAppComponent
        return this
    }

    fun inject(): AndroidTestConfig<T> {
        injectClassFromComponent(testAppComponent, test)
        return this
    }
}
