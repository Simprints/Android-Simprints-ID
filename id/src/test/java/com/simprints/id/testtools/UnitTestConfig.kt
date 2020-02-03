package com.simprints.id.testtools

import android.content.Context
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.work.Configuration
import androidx.work.testing.WorkManagerTestInitHelper
import com.google.firebase.FirebaseApp
import com.simprints.id.commontesttools.di.TestAppModule
import com.simprints.id.commontesttools.di.TestDataModule
import com.simprints.id.commontesttools.di.TestPreferencesModule
import com.simprints.id.commontesttools.di.TestSyncModule
import com.simprints.id.testtools.di.AppComponentForTests
import com.simprints.id.testtools.di.DaggerAppComponentForTests
import com.simprints.testtools.common.di.DependencyRule
import com.simprints.testtools.common.di.injectClassFromComponent
import com.simprints.testtools.unit.BaseUnitTestConfig
import io.fabric.sdk.android.Fabric

class UnitTestConfig<T : Any>(
    private val test: T,
    private val appModule: TestAppModule? = null,
    private val preferencesModule: TestPreferencesModule? = null,
    private val dataModule: TestDataModule? = null,
    private val syncModule: TestSyncModule? = null
    ): BaseUnitTestConfig() {

    private val defaultAppModuleWithoutRealm by lazy {
        TestAppModule(app,
            sessionEventsLocalDbManagerRule = DependencyRule.MockRule)
    }

    private val app by lazy {
        ApplicationProvider.getApplicationContext() as TestApplication
    }

    private val ctx by lazy {
        ApplicationProvider.getApplicationContext() as Context
    }

    private lateinit var testAppComponent: AppComponentForTests

    fun fullSetup() =
        rescheduleRxMainThread()
            .coroutinesMainThread()
            .setupFirebase()
            .setupWorkManager()
            .setupCrashlytics()
            .initAndInjectComponent()

    override fun rescheduleRxMainThread() = also {
        super.rescheduleRxMainThread()
    }

    override fun coroutinesMainThread() = also {
        super.coroutinesMainThread()
    }

    fun setupFirebase() = also {
        FirebaseApp.initializeApp(ctx)
    }

    fun setupWorkManager() = also {
        try {
            WorkManagerTestInitHelper.initializeTestWorkManager(ctx, Configuration.Builder().build())
        } catch (e: IllegalStateException) {
            Log.d("TestConfig", "WorkManager already initialized")
        }
    }

    fun setupCrashlytics() = also {
        Fabric.with(ctx)
    }

    fun initAndInjectComponent() =
        initComponent().inject()

    private fun initComponent() = also {

        testAppComponent = DaggerAppComponentForTests.builder()
            .application(app)
            .appModule(appModule ?: defaultAppModuleWithoutRealm)
            .preferencesModule(preferencesModule ?: TestPreferencesModule())
            .dataModule(dataModule ?: TestDataModule())
            .syncModule(syncModule ?: TestSyncModule())
            .build()

        app.component = testAppComponent
    }

    private fun inject() = also {
        injectClassFromComponent(testAppComponent, test)
    }
}
