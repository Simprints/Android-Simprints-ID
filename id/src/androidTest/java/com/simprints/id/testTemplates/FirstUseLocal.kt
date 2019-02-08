package com.simprints.id.testTemplates

import androidx.test.InstrumentationRegistry
import com.simprints.id.data.analytics.eventData.controllers.local.RealmSessionEventsDbManagerImpl
import com.simprints.id.data.analytics.eventData.controllers.local.SessionRealmConfig
import com.simprints.id.data.db.local.models.LocalDbKey
import com.simprints.id.data.db.local.realm.PeopleRealmConfig
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_LOCAL_DB_KEY
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_REALM_KEY
import com.simprints.id.testTools.StorageUtils
import com.simprints.id.testTools.log
import io.realm.RealmConfiguration

/**
 * Interface for tests where we only care about clearing local data, and don't care if we work with
 * a project that has existing data. [peopleRealmConfiguration] should be configured correctly
 * before calling setUp on this interface.
 *
 * A sample test class should look like this:
 *
 * ```
 * @RunWith(AndroidJUnit4::class)
 * class AndroidTestClass : DaggerForAndroidTests(), FirstUseLocal {
 *
 *     override var peopleRealmConfiguration: RealmConfiguration? = null
 *
 *     @Inject lateinit var randomGeneratorMock: RandomGenerator
 *     @Inject lateinit var remoteDbManager: RemoteDbManager
 *
 *     override var module by lazyVar {
 *         AppModuleForAndroidTests(app,
 *             randomGeneratorRule = MockRule)
 *     }
 *
 *     @Before
 *     override fun setUp() {
 *         app = InstrumentationRegistry.getTargetContext().applicationContext as Application
 *         super<DaggerForAndroidTests>.setUp()
 *         testAppComponent.inject(this)
 *
 *         setupRandomGeneratorToGenerateKey(DEFAULT_REALM_KEY, randomGeneratorMock)
 *
 *         app.initDependencies()
 *
 *         Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
 *         peopleRealmConfiguration = PeopleRealmConfig.get(DEFAULT_LOCAL_DB_KEY.projectId, DEFAULT_LOCAL_DB_KEY.value, DEFAULT_LOCAL_DB_KEY.projectId)
 *         super<FirstUseLocal>.setUp()
 *
 *         signOut()
 *     }
 *
 *     @After
 *     override fun tearDown() {
 *         super.tearDown()
 *     }
 *
 *     private fun signOut() [
 *         remoteDbManager.signOutOfRemoteDb()
 *     }
 * }
 * ```
 */
interface FirstUseLocal {

    companion object {
        private val defaultSessionLocalDbKey = LocalDbKey(RealmSessionEventsDbManagerImpl.SESSIONS_REALM_DB_FILE_NAME, DEFAULT_REALM_KEY)
        val defaultSessionRealmConfiguration = SessionRealmConfig.get(defaultSessionLocalDbKey.projectId, defaultSessionLocalDbKey.value)
        val defaultPeopleRealmConfiguration = PeopleRealmConfig.get(DEFAULT_LOCAL_DB_KEY.projectId, DEFAULT_LOCAL_DB_KEY.value, DEFAULT_LOCAL_DB_KEY.projectId)
    }

    var peopleRealmConfiguration: RealmConfiguration?
    var sessionsRealmConfiguration: RealmConfiguration?

    fun setUp() {
        log("FirstUseTest.setUp(): cleaning internal data")

        StorageUtils.clearApplicationData(InstrumentationRegistry.getTargetContext())
        StorageUtils.clearRealmDatabase(peopleRealmConfiguration)
        StorageUtils.clearRealmDatabase(sessionsRealmConfiguration)
    }

    fun tearDown() {
        log("FirstUseTest.tearDown(): cleaning internal data")
        StorageUtils.clearApplicationData(InstrumentationRegistry.getTargetContext())
        StorageUtils.clearRealmDatabase(peopleRealmConfiguration)
        StorageUtils.clearRealmDatabase(sessionsRealmConfiguration)
    }
}
