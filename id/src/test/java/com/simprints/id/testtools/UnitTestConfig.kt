package com.simprints.id.testtools

import android.content.Context
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.work.Configuration
import androidx.work.testing.WorkManagerTestInitHelper
import com.google.firebase.FirebaseApp
import com.simprints.id.commontesttools.di.*
import com.simprints.id.testtools.di.AppComponentForTests
import com.simprints.id.testtools.di.DaggerAppComponentForTests
import com.simprints.testtools.common.di.DependencyRule
import com.simprints.testtools.common.di.injectClassFromComponent
import com.simprints.testtools.unit.BaseUnitTestConfig
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class UnitTestConfig<T : Any>(
    private val test: T,
    private val appModule: TestAppModule? = null,
    private val preferencesModule: TestPreferencesModule? = null,
    private val dataModule: TestDataModule? = null,
    private val syncModule: TestSyncModule? = null,
    private val loginModule: TestSecurityModule? = null,
    private val viewModelModule: TestViewModelModule? = null
) : BaseUnitTestConfig() {

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

    @ExperimentalCoroutinesApi
    fun fullSetup() =
        rescheduleRxMainThread()
            .coroutinesMainThread()
            .setupFirebase()
            .setupWorkManager()
            .initAndInjectComponent()

    @ExperimentalCoroutinesApi
    override fun rescheduleRxMainThread() = also {
        super.rescheduleRxMainThread()
    }

    @ExperimentalCoroutinesApi
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

    fun initAndInjectComponent() =
        initComponent().inject()

    private fun initComponent() = also {
        testAppComponent = DaggerAppComponentForTests.builder()
            .application(app)
            .appModule(appModule ?: defaultAppModuleWithoutRealm)
            .preferencesModule(preferencesModule ?: TestPreferencesModule())
            .dataModule(dataModule ?: TestDataModule())
            .syncModule(syncModule ?: TestSyncModule())
            .securityModule(loginModule ?: TestSecurityModule())
            .viewModelModule(viewModelModule ?: TestViewModelModule())
            .build()

        app.component = testAppComponent
    }

    private fun inject() = also {
        injectClassFromComponent(testAppComponent, test)
    }
}
