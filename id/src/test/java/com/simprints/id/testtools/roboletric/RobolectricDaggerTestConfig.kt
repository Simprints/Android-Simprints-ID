package com.simprints.id.testtools.roboletric

import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.work.Configuration
import androidx.work.testing.WorkManagerTestInitHelper
import com.google.firebase.FirebaseApp
import com.simprints.id.commontesttools.di.AppModuleForAnyTests
import com.simprints.id.commontesttools.di.PreferencesModuleForAnyTests
import com.simprints.id.testtools.di.AppComponentForTests
import com.simprints.id.testtools.di.AppModuleForTests
import com.simprints.id.testtools.di.DaggerAppComponentForTests
import kotlin.reflect.full.createType
import kotlin.reflect.full.functions

class RobolectricDaggerTestConfig<T : Any>(
    private val test: T,
    private val appModule: AppModuleForAnyTests? = null,
    private val preferencesModule: PreferencesModuleForAnyTests? = null
) {

    private val app = ApplicationProvider.getApplicationContext() as TestApplication
    private lateinit var testAppComponent: AppComponentForTests

    fun setupAllAndFinish() =
        rescheduleRxMainThread()
            .setupFirebase()
            .setupWorkManager()
            .finish()

    fun rescheduleRxMainThread() : RobolectricDaggerTestConfig<T> {
        com.simprints.testframework.unit.reactive.rescheduleRxMainThread()
        return this
    }

    fun setupFirebase(): RobolectricDaggerTestConfig<T> {
        FirebaseApp.initializeApp(app)
        return this
    }

    fun setupWorkManager(): RobolectricDaggerTestConfig<T> {
        try {
            WorkManagerTestInitHelper.initializeTestWorkManager(app, Configuration.Builder().build())
        } catch (e: IllegalStateException) {
            Log.d("TestConfig", "WorkManager already initialized")
        }
        return this
    }

    fun finish(): RobolectricDaggerTestConfig<T> = initComponent().inject()

    fun initComponent(): RobolectricDaggerTestConfig<T> {

        testAppComponent = DaggerAppComponentForTests.builder()
            .appModule(appModule ?: AppModuleForTests(app))
            .preferencesModule(preferencesModule ?: PreferencesModuleForAnyTests())
            .build()

        app.component = testAppComponent
        return this
    }

    fun inject(): RobolectricDaggerTestConfig<T> {

        // Go through all the functions of the AppComponent and try to find the one corresponding to the test
        val injectFunction = testAppComponent::class.functions.find { function ->

            // These inject KFunctions take two parameters, the first is the AppComponent, the second is the injectee
            function.parameters.size == 2 && function.parameters.last().type == test::class.createType()

        } ?: throw NoSuchMethodError("Forgot to add inject method to ${testAppComponent::class.createType()} for ${test::class.createType()}")

        injectFunction.call(testAppComponent, test)
        return this
    }
}
