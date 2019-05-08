package com.simprints.fingerprint

import androidx.test.core.app.ApplicationProvider
import com.simprints.fingerprint.commontesttools.di.TestFingerprintCoreModule
import com.simprints.fingerprint.commontesttools.di.TestFingerprintModule
import com.simprints.fingerprint.di.FingerprintComponentBuilder
import com.simprints.fingerprint.di.FingerprintCoreModule
import com.simprints.fingerprint.di.FingerprintModule
import com.simprints.fingerprint.testtools.di.DaggerFingerprintComponentForTests
import com.simprints.fingerprint.testtools.di.FingerprintComponentForTests
import com.simprints.id.Application
import com.simprints.testtools.common.di.injectClassFromComponent

class UnitTestConfig<T : Any>(
    private val test: T,
    private val fingerprintModule: FingerprintModule? = null,
    private val fingerprintCoreModule: FingerprintCoreModule? = null
) {

    private val app = ApplicationProvider.getApplicationContext<Application>()
    private lateinit var testAppComponent: FingerprintComponentForTests

    fun fullSetup() =
        rescheduleRxMainThread()
            .initAndInjectComponent()

    fun rescheduleRxMainThread() = also {
        com.simprints.testtools.unit.reactive.rescheduleRxMainThread()
    }

    fun initAndInjectComponent() =
        initComponent().inject()

    private fun initComponent() = also {

        testAppComponent = DaggerFingerprintComponentForTests
            .builder()
            .fingerprintModule(fingerprintModule ?: TestFingerprintModule())
            .fingerprintCoreModule(fingerprintCoreModule ?: TestFingerprintCoreModule())
            .appComponent(app.component)
            .build()

        FingerprintComponentBuilder.setComponent(testAppComponent)
    }

    private fun inject() = also {
        injectClassFromComponent(testAppComponent, test)
    }
}
