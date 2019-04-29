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
            .initDependencies()

    /** Runs [fullSetup] with an extra block of code inserted just before [initDependencies]
     * Useful for setting up mocks before the Application is created */
    fun fullSetupWith(block: () -> Unit) =
        initAndInjectComponent()
            .initRealm()
            .also { block() }
            .initDependencies()

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

    fun initDependencies() = also {
        app.initDependencies()
    }
}
