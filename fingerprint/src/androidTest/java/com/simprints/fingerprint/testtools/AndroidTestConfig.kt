package com.simprints.fingerprint.testtools

import com.simprints.fingerprint.di.KoinInjector
import com.squareup.rx2.idler.Rx2Idler
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.plugins.RxJavaPlugins
import org.junit.rules.TestWatcher
import org.junit.runner.Description

object AndroidTestConfig {

    fun fullSetup() =
        initRxIdler()
            .acquireFingerprintModules()

    fun initRxIdler() = also {
        RxJavaPlugins.setInitComputationSchedulerHandler(Rx2Idler.create("RxJava 2.x Computation Scheduler"))
        RxJavaPlugins.setInitIoSchedulerHandler(Rx2Idler.create("RxJava 2.x Io Scheduler"))
        RxJavaPlugins.setInitNewThreadSchedulerHandler(Rx2Idler.create("RxJava 2.x New Thread Scheduler"))
        RxJavaPlugins.setInitSingleSchedulerHandler(Rx2Idler.create("RxJava 2.x Single Scheduler"))
        RxAndroidPlugins.setInitMainThreadSchedulerHandler(Rx2Idler.create("RxJava 2.x Main Scheduler"))
    }

    fun acquireFingerprintModules() = also {
        KoinInjector.acquireFingerprintKoinModules()
    }

    fun fullTearDown() =
        releaseFingerprintModules()

    fun releaseFingerprintModules() = also {
        KoinInjector.releaseFingerprintKoinModules()
    }
}

class FullAndroidTestConfigRule : TestWatcher() {

    override fun starting(description: Description?) {
        AndroidTestConfig.fullSetup()
    }

    override fun finished(description: Description?) {
        AndroidTestConfig.fullTearDown()
    }
}
