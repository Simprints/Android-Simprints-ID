package com.simprints.id.testtools

import androidx.test.core.app.ApplicationProvider
import com.simprints.id.Application
import com.simprints.id.commontesttools.DefaultTestConstants
import com.simprints.id.commontesttools.di.TestAppModule
import com.simprints.id.commontesttools.di.TestPreferencesModule
import com.simprints.id.data.analytics.eventdata.controllers.local.RealmSessionEventsDbManagerImpl
import com.simprints.id.data.analytics.eventdata.controllers.local.SessionRealmConfig
import com.simprints.id.data.db.local.models.LocalDbKey
import com.simprints.id.data.db.local.realm.PeopleRealmConfig
import com.simprints.id.testtools.di.AppComponentForAndroidTests
import com.simprints.id.testtools.di.DaggerAppComponentForAndroidTests
import com.simprints.testtools.common.dagger.injectClassFromComponent
import io.realm.Realm

class AndroidTestConfig<T : Any>(
    private val test: T,
    private val appModule: TestAppModule? = null,
    private val preferencesModule: TestPreferencesModule? = null
) {

    private val app = ApplicationProvider.getApplicationContext<Application>()
    private lateinit var testAppComponent: AppComponentForAndroidTests

    private val defaultSessionLocalDbKey = LocalDbKey(RealmSessionEventsDbManagerImpl.SESSIONS_REALM_DB_FILE_NAME, DefaultTestConstants.DEFAULT_REALM_KEY)
    private val sessionRealmConfiguration = SessionRealmConfig.get(defaultSessionLocalDbKey.projectId, defaultSessionLocalDbKey.value)
    private val peopleRealmConfiguration = PeopleRealmConfig.get(DefaultTestConstants.DEFAULT_LOCAL_DB_KEY.projectId, DefaultTestConstants.DEFAULT_LOCAL_DB_KEY.value, DefaultTestConstants.DEFAULT_LOCAL_DB_KEY.projectId)

    fun fullSetup() =
        initAndInjectComponent()
            .initRealm()
            .clearData()
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
        StorageUtils.clearApplicationData(app)
        StorageUtils.clearRealmDatabase(peopleRealmConfiguration)
        StorageUtils.clearRealmDatabase(sessionRealmConfiguration)
    }

    fun initDependencies() = also {
        app.initDependencies()
    }
}
