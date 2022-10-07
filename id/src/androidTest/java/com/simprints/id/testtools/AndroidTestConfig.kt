package com.simprints.id.testtools

import androidx.test.core.app.ApplicationProvider
import com.simprints.id.Application
import com.simprints.id.activities.settings.ModuleSelectionActivityAndroidTest
import com.simprints.id.testtools.di.*
import io.realm.Realm

class AndroidTestConfig(
    private val appModule: TestAppModule? = null,
    private val dataModule: TestDataModule? = null,
    private val securityModule: TestSecurityModule? = null,
    private val viewModelModule: TestViewModelModule? = null
) {

    private val app = ApplicationProvider.getApplicationContext<Application>()
    lateinit var testAppComponent: AppComponentForAndroidTests

    fun fullSetup() = initComponent()
        .initRealm()
        .initModules()


    fun componentBuilder() = DaggerAppComponentForAndroidTests.builder()
        .application(app)
        .appModule(appModule ?: TestAppModule(app))
        .dataModule(dataModule ?: TestDataModule())
        .securityModule(securityModule ?: TestSecurityModule())
        .viewModelModule(viewModelModule ?: TestViewModelModule())

    private fun initRealm() = also {
        Realm.init(app)
    }

    fun initComponent() = also {
        testAppComponent = componentBuilder().build()
        app.component = testAppComponent
    }


    private fun initModules() = also {
        app.handleUndeliverableExceptionInRxJava()
    }

    fun inject(test: ModuleSelectionActivityAndroidTest) {
        testAppComponent.inject(test)
    }
}
