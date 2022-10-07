package com.simprints.id.testtools

import android.content.Context
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.work.Configuration
import androidx.work.testing.WorkManagerTestInitHelper
import com.google.firebase.FirebaseApp
import com.simprints.id.testtools.di.*
import com.simprints.testtools.common.di.DependencyRule
import com.simprints.testtools.unit.BaseUnitTestConfig

class UnitTestConfig(
    private val appModule: TestAppModule? = null,
    private val dataModule: TestDataModule? = null,
    private val viewModelModule: TestViewModelModule? = null
) : BaseUnitTestConfig() {

    private val defaultAppModuleWithoutRealm by lazy {
        TestAppModule(
            app,
            sessionEventsLocalDbManagerRule = DependencyRule.MockRule
        )
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
            .initComponent()

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
            WorkManagerTestInitHelper.initializeTestWorkManager(
                ctx,
                Configuration.Builder().build()
            )
        } catch (e: IllegalStateException) {
            Log.d("TestConfig", "WorkManager already initialized")
        }
    }


    fun initComponent(): AppComponentForTests {
        testAppComponent = DaggerAppComponentForTests.builder()
            .application(app)
            .appModule(appModule ?: defaultAppModuleWithoutRealm)
            .dataModule(dataModule ?: TestDataModule())
            .viewModelModule(viewModelModule ?: TestViewModelModule())
            .build()

        app.component = testAppComponent
        return testAppComponent
    }
}
