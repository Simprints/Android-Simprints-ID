package com.simprints.id.experimental.testtools

import androidx.test.core.app.ApplicationProvider
import androidx.work.Configuration
import androidx.work.WorkManager
import com.google.firebase.FirebaseApp
import com.simprints.id.testUtils.roboletric.TestApplication
import timber.log.Timber
import kotlin.reflect.full.createType
import kotlin.reflect.full.functions

class RobolectricDaggerTestConfig<T : NewDaggerForTests>(val test: T) {

    init {
        test.app = (ApplicationProvider.getApplicationContext() as TestApplication)
    }

    fun setupAllAndFinish() = setupFirebase().setupWorkManager().finish()

    fun setupFirebase(): RobolectricDaggerTestConfig<T> {
        FirebaseApp.initializeApp(test.app)
        return this
    }

    fun setupWorkManager(): RobolectricDaggerTestConfig<T> {
        try {
            WorkManager.initialize(test.app, Configuration.Builder().build())
        } catch (e: IllegalStateException) {
            Timber.d("WorkManager already initialized")
        }
        return this
    }

    fun finish(): RobolectricDaggerTestConfig<T> = initComponent().inject()

    fun initComponent(): RobolectricDaggerTestConfig<T> {
        test.initComponent()
        return this
    }

    fun inject(): RobolectricDaggerTestConfig<T> {

        // Go through all the functions of the AppComponent and try to find the one corresponding to the test
        val injectFunction = test.testAppComponent::class.functions.find { function ->

            // These inject KFunctions take two parameters, the first is the AppComponent, the second is the injectee
            function.parameters.size == 2 && function.parameters.last().type == test::class.createType()

        }?: throw NoSuchMethodError("Forgot to add inject method to ${test.testAppComponent::class.createType()} for ${test::class.createType()}")

        injectFunction.call(test.testAppComponent, test)
        return this
    }
}
