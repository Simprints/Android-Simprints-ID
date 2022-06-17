package com.simprints.id.testtools

import androidx.test.core.app.ApplicationProvider
import com.simprints.id.Application
import com.simprints.id.commontesttools.di.*
import com.simprints.id.testtools.di.AppComponentForAndroidTests
import com.simprints.id.testtools.di.DaggerAppComponentForAndroidTests
import com.simprints.testtools.common.di.injectClassFromComponent
import io.realm.Realm

class AndroidTestConfig<T : Any>(
    private val test: T,
    private val appModule: TestAppModule? = null,
    private val dataModule: TestDataModule? = null,
    private val preferencesModule: TestPreferencesModule? = null,
    private val syncModule: TestSyncModule? = null,
    private val securityModule: TestSecurityModule? = null,
    private val viewModelModule: TestViewModelModule? = null
) {

    private val app = ApplicationProvider.getApplicationContext<Application>()
    private lateinit var testAppComponent: AppComponentForAndroidTests

    fun fullSetup() = initAndInjectComponent()
        .initRealm()
        .initModules()

    fun initAndInjectComponent() = initComponent().inject()

    fun componentBuilder() = DaggerAppComponentForAndroidTests.builder()
        .application(app)
        .appModule(appModule ?: TestAppModule(app))
        .dataModule(dataModule ?: TestDataModule())
        .preferencesModule(preferencesModule ?: TestPreferencesModule())
        .syncModule(syncModule ?: TestSyncModule())
        .securityModule(securityModule ?: TestSecurityModule())
        .viewModelModule(viewModelModule ?: TestViewModelModule())

    private fun initRealm() = also {
        Realm.init(app)
    }

    private fun initComponent() = also {
        testAppComponent = componentBuilder().build()
        app.component = testAppComponent
    }

    private fun inject() = also {
        injectClassFromComponent(testAppComponent, test)
    }

    private fun initModules() = also {
        app.handleUndeliverableExceptionInRxJava()
    }

}
