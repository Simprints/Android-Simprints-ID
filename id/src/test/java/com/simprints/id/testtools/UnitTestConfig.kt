package com.simprints.id.testtools

import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.work.Configuration
import androidx.work.testing.WorkManagerTestInitHelper
import com.google.firebase.FirebaseApp
import com.simprints.id.commontesttools.TestApplication
import com.simprints.id.commontesttools.di.TestAppModule
import com.simprints.id.commontesttools.di.TestPreferencesModule
import com.simprints.id.testtools.di.AppComponentForTests
import com.simprints.id.testtools.di.DaggerAppComponentForTests
import com.simprints.testframework.common.dagger.injectClassFromComponent

class UnitTestConfig<T : Any>(
    private val test: T,
    private val appModule: TestAppModule? = null,
    private val preferencesModule: TestPreferencesModule? = null
) {

    private val app = ApplicationProvider.getApplicationContext() as TestApplication
    private lateinit var testAppComponent: AppComponentForTests

    fun fullSetup() =
        rescheduleRxMainThread()
            .setupFirebase()
            .setupWorkManager()
            .initComponent()
            .inject()

    fun rescheduleRxMainThread() : UnitTestConfig<T> {
        com.simprints.testframework.unit.reactive.rescheduleRxMainThread()
        return this
    }

    fun setupFirebase(): UnitTestConfig<T> {
        FirebaseApp.initializeApp(app)
        return this
    }

    fun setupWorkManager(): UnitTestConfig<T> {
        try {
            WorkManagerTestInitHelper.initializeTestWorkManager(app, Configuration.Builder().build())
        } catch (e: IllegalStateException) {
            Log.d("TestConfig", "WorkManager already initialized")
        }
        return this
    }

    fun initComponent(): UnitTestConfig<T> {

        testAppComponent = DaggerAppComponentForTests.builder()
            .appModule(appModule ?: TestAppModule(app))
            .preferencesModule(preferencesModule ?: TestPreferencesModule())
            .build()

        app.component = testAppComponent
        return this
    }

    fun inject(): UnitTestConfig<T> {
        injectClassFromComponent(testAppComponent, test)
        return this
    }
}
