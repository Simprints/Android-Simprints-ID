package com.simprints.id.testtools

import androidx.test.core.app.ApplicationProvider
import com.simprints.id.Application
import com.simprints.id.commontesttools.di.TestAppModule
import com.simprints.id.commontesttools.di.TestPreferencesModule
import com.simprints.id.testtools.di.AppComponentForAndroidTests
import com.simprints.id.testtools.di.DaggerAppComponentForAndroidTests
import com.simprints.testframework.common.dagger.injectClassFromComponent
import io.realm.Realm

class AndroidTestConfig<T : Any>(
    private val test: T,
    private val appModule: TestAppModule? = null,
    private val preferencesModule: TestPreferencesModule? = null
) {

    private val app = ApplicationProvider.getApplicationContext<Application>()
    private lateinit var testAppComponent: AppComponentForAndroidTests

    fun fullSetup() =
        initComponent()
            .inject()
            .initRealm()
            .initDependencies()

    /** Runs [fullSetup] with an extra block of code inserted just before [initDependencies]
     * Useful for setting up mocks before the Application is created */
    fun fullSetupWith(block: () -> Unit): AndroidTestConfig<T> {
        val config = initComponent().inject().initRealm()
        block()
        return config.initDependencies()
    }

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

    fun initRealm(): AndroidTestConfig<T> {
        Realm.init(app)
        return this
    }

    fun initDependencies(): AndroidTestConfig<T> {
        app.initDependencies()
        return this
    }
}
