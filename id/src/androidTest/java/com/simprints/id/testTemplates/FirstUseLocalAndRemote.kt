package com.simprints.id.testTemplates

import androidx.test.InstrumentationRegistry
import com.simprints.id.data.analytics.eventData.controllers.local.RealmSessionEventsDbManagerImpl
import com.simprints.id.data.analytics.eventData.controllers.local.SessionRealmConfig
import com.simprints.id.data.db.local.models.LocalDbKey
import com.simprints.id.data.db.local.realm.PeopleRealmConfig
import com.simprints.id.shared.DefaultTestConstants.DEFAULT_REALM_KEY
import com.simprints.id.testTools.models.TestProject
import com.simprints.id.testTools.remote.RemoteTestingManager
import io.realm.Realm
import io.realm.RealmConfiguration

/**
 * Interface for tests where an empty project should be created server side, as well as clearing
 * local data. The newly created project will appear in [testProject].
 *
 * A sample test class should look like this:
 *
 * ```
 * @RunWith(AndroidJUnit4::class)
 * class AndroidTestClass : DaggerForAndroidTests(), FirstUseLocalAndRemote {
 *
 *     override lateinit var testProject: TestProject
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
 *         super<FirstUseLocalAndRemote>.setUp()
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
interface FirstUseLocalAndRemote : FirstUseLocal {

    var testProject: TestProject

    override var peopleRealmConfiguration: RealmConfiguration?
    override var sessionsRealmConfiguration: RealmConfiguration?

    override fun setUp() {

        testProject = RemoteTestingManager.create().createTestProject()

        val localDbKey = LocalDbKey(
            testProject.id,
            DEFAULT_REALM_KEY,
            testProject.legacyId)

        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
        peopleRealmConfiguration = PeopleRealmConfig.get(localDbKey.projectId, localDbKey.value, localDbKey.projectId)
        sessionsRealmConfiguration = SessionRealmConfig.get(RealmSessionEventsDbManagerImpl.SESSIONS_REALM_DB_FILE_NAME, localDbKey.value)

        super.setUp()
    }

    override fun tearDown() {
        RemoteTestingManager.create().deleteTestProject(testProject.id)
        super.tearDown()
    }
}
