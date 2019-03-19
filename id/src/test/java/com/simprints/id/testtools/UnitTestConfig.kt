package com.simprints.id.testtools

import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.work.Configuration
import androidx.work.testing.WorkManagerTestInitHelper
import com.google.firebase.FirebaseApp
import com.simprints.id.commontesttools.di.TestAppModule
import com.simprints.id.commontesttools.di.TestPreferencesModule
import com.simprints.id.testtools.di.AppComponentForTests
import com.simprints.id.testtools.di.DaggerAppComponentForTests
import com.simprints.testtools.common.dagger.injectClassFromComponent
import io.fabric.sdk.android.Fabric

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
            .setupCrashlytics()
            .initAndInjectComponent()

    fun rescheduleRxMainThread() = also {
        com.simprints.testtools.unit.reactive.rescheduleRxMainThread()
    }

    fun setupFirebase() = also {
        FirebaseApp.initializeApp(app)
    }

    fun setupWorkManager() = also {
        try {
            WorkManagerTestInitHelper.initializeTestWorkManager(app, Configuration.Builder().build())
        } catch (e: IllegalStateException) {
            Log.d("TestConfig", "WorkManager already initialized")
        }
    }

    fun setupCrashlytics() = also {
        Fabric.with(app)
    }

    fun initAndInjectComponent() =
        initComponent().inject()

    private fun initComponent() = also {

        testAppComponent = DaggerAppComponentForTests.builder()
            .appModule(appModule ?: TestAppModule(app))
            .preferencesModule(preferencesModule ?: TestPreferencesModule())
            .build()

        app.component = testAppComponent
    }

    private fun inject() = also {
        injectClassFromComponent(testAppComponent, test)
    }
}
