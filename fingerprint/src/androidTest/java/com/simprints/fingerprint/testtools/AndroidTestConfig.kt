package com.simprints.fingerprint.testtools

import androidx.test.core.app.ApplicationProvider
import com.simprints.fingerprint.commontesttools.DefaultTestConstants
import com.simprints.fingerprint.commontesttools.di.TestAppModule
import com.simprints.fingerprint.commontesttools.di.TestFingerprintModule
import com.simprints.fingerprint.commontesttools.di.TestPreferencesModule
import com.simprints.fingerprint.di.DaggerFingerprintComponent
import com.simprints.fingerprint.di.FingerprintComponentBuilder
import com.simprints.fingerprint.testtools.di.AppComponentForFingerprintAndroidTests
import com.simprints.fingerprint.testtools.di.DaggerAppComponentForFingerprintAndroidTests
import com.simprints.id.Application
import com.simprints.id.data.analytics.eventdata.controllers.local.RealmSessionEventsDbManagerImpl
import com.simprints.id.data.analytics.eventdata.controllers.local.SessionRealmConfig
import com.simprints.id.data.db.local.models.LocalDbKey
import com.simprints.id.data.db.local.realm.PeopleRealmConfig
import com.simprints.id.data.prefs.PreferencesManagerImpl
import com.simprints.testtools.android.StorageUtils
import com.simprints.testtools.common.di.injectClassFromComponent
import io.realm.Realm

// TODO : Currently used for Fingerprint Android tests & Integration tests
// TODO : Combine/extend functionality from id/../AndroidTestConfig
class AndroidTestConfig<T : Any>(
    private val test: T
) {

    private val app = ApplicationProvider.getApplicationContext<Application>()
    private lateinit var testAppComponent: AppComponentForFingerprintAndroidTests

    fun fullSetup() = initAndInjectComponent()

    /** Runs [fullSetup] with an extra block of code inserted just before [initDependencies]
     * Useful for setting up mocks before the Application is created */
    fun fullSetupWith(block: () -> Unit) =
        initAndInjectComponent()
            .also { block() }

    fun initAndInjectComponent() =
        initComponent().inject()

    private fun initComponent() = also {

        testAppComponent = DaggerAppComponentForFingerprintAndroidTests
            .builder()
            .appComponent(app.component)
            .build()

        FingerprintComponentBuilder.setComponent(testAppComponent)
    }

    private fun inject() = also {
        injectClassFromComponent(testAppComponent, test)
    }
}
