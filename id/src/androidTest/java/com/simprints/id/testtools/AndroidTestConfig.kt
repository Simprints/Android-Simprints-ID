package com.simprints.id.testtools

import androidx.test.core.app.ApplicationProvider
import com.simprints.id.Application
import com.simprints.id.commontesttools.DefaultTestConstants
import com.simprints.id.commontesttools.di.TestAppModule
import com.simprints.id.commontesttools.di.TestPreferencesModule
import com.simprints.id.data.analytics.eventdata.controllers.local.RealmSessionEventsDbManagerImpl
import com.simprints.id.data.db.local.models.LocalDbKey
import com.simprints.id.data.prefs.PreferencesManagerImpl
import com.simprints.id.testtools.di.AppComponentForAndroidTests
import com.simprints.id.testtools.di.DaggerAppComponentForAndroidTests
import com.simprints.testtools.android.StorageUtils
import com.simprints.testtools.android.StorageUtils.deleteAllDatabases
import com.simprints.testtools.common.di.injectClassFromComponent
import io.realm.Realm

class AndroidTestConfig<T : Any>(
    private val test: T,
    private val appModule: TestAppModule? = null,
    private val preferencesModule: TestPreferencesModule? = null
) {

    private val app = ApplicationProvider.getApplicationContext<Application>()
    private lateinit var testAppComponent: AppComponentForAndroidTests

    private val defaultSessionLocalDbKey by lazy { LocalDbKey(RealmSessionEventsDbManagerImpl.SESSIONS_REALM_DB_FILE_NAME, DefaultTestConstants.DEFAULT_REALM_KEY) }

    fun fullSetup() =
        initAndInjectComponent()
            .clearData()
            .initRealm()
            .initDependencies()

    /** Runs [fullSetup] with an extra block of code inserted just before [initDependencies]
     * Useful for setting up mocks before the Application is created */
    fun fullSetupWith(block: () -> Unit) =
        initAndInjectComponent()
            .initRealm()
            .clearData()
            .also { block() }
            .initDependencies()

    fun initAndInjectComponent() =
        initComponent().inject()

    private fun initComponent() = also {

        testAppComponent = DaggerAppComponentForAndroidTests.builder()
            .application(app)
            .appModule(appModule ?: TestAppModule(app))
            .preferencesModule(preferencesModule ?: TestPreferencesModule())
            .build()

        app.component = testAppComponent
    }

    private fun inject() = also {
        injectClassFromComponent(testAppComponent, test)
    }

    fun initRealm() = also {
        Realm.init(app)
    }

    fun clearData() = also {
        StorageUtils.clearSharedPrefs(app, PreferencesManagerImpl.PREF_FILE_NAME, PreferencesManagerImpl.PREF_MODE)
        deleteAllDatabases(app)
    }

    fun initDependencies() = also {
        app.initDependencies()
    }
}
