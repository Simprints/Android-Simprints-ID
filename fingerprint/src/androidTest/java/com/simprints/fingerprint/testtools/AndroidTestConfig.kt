package com.simprints.fingerprint.testtools

import androidx.test.core.app.ApplicationProvider
import com.simprints.fingerprint.commontesttools.di.TestFingerprintCoreModule
import com.simprints.fingerprint.commontesttools.di.TestFingerprintModule
import com.simprints.fingerprint.di.FingerprintComponentBuilder
import com.simprints.fingerprint.di.FingerprintCoreModule
import com.simprints.fingerprint.di.FingerprintModule
import com.simprints.fingerprint.testtools.di.DaggerFingerprintComponentForAndroidTests
import com.simprints.fingerprint.testtools.di.FingerprintComponentForAndroidTests
import com.simprints.id.Application
import com.simprints.testtools.common.di.injectClassFromComponent

class AndroidTestConfig<T : Any>(
    private val test: T,
    private val fingerprintModule: FingerprintModule? = null,
    private val fingerprintCoreModule: FingerprintCoreModule? = null
) {

    private val app = ApplicationProvider.getApplicationContext<Application>()
    private lateinit var testAppComponent: FingerprintComponentForAndroidTests

    fun fullSetup() = initAndInjectComponent()

    /** Runs [fullSetup] with an extra block of code inserted just before [initAndInjectComponent]
     * Useful for setting up mocks before the Application is created */
    fun fullSetupWith(block: () -> Unit) =
        initAndInjectComponent()
            .also { block() }

    fun initAndInjectComponent() =
        initComponent().inject()

    private fun initComponent() = also {

        testAppComponent = DaggerFingerprintComponentForAndroidTests
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
