package com.simprints.fingerprint.testtools

import com.simprints.fingerprint.di.KoinInjector
import org.junit.rules.TestWatcher
import org.junit.runner.Description

object AndroidTestConfig {

    fun fullSetup() = acquireFingerprintModules()


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
