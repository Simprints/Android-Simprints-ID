package com.simprints.fingerprint.testtools

import com.simprints.fingerprint.di.KoinInjector
import com.simprints.testtools.unit.BaseUnitTestConfig
import org.junit.rules.TestWatcher
import org.junit.runner.Description

object UnitTestConfig : BaseUnitTestConfig() {

    fun fullSetup() =
        rescheduleRxMainThread()
            .coroutinesMainThread()
            .startKoin()
            .acquireFingerprintModules()

    override fun rescheduleRxMainThread() = also {
        super.rescheduleRxMainThread()
    }

    override fun coroutinesMainThread() = also {
        super.coroutinesMainThread()
    }

    fun startKoin() = also {
        org.koin.core.context.startKoin {}
    }

    fun acquireFingerprintModules() = also {
        KoinInjector.acquireFingerprintKoinModules()
    }

    fun fullTearDown() =
        releaseFingerprintModules()
            .stopKoin()

    fun releaseFingerprintModules() = also {
        KoinInjector.releaseFingerprintKoinModules()
    }

    fun stopKoin() = also {
        org.koin.core.context.stopKoin()
    }
}

class FullUnitTestConfigRule : TestWatcher() {

    override fun starting(description: Description?) {
        UnitTestConfig.fullSetup()
    }

    override fun finished(description: Description?) {
        UnitTestConfig.fullTearDown()
    }
}
