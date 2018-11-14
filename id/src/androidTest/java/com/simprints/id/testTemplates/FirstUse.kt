package com.simprints.id.testTemplates

import android.support.test.InstrumentationRegistry
import com.simprints.id.data.db.local.models.LocalDbKey
import com.simprints.id.data.db.local.realm.PeopleRealmConfig
import com.simprints.id.testTools.DEFAULT_REALM_KEY
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
 * class AndroidTestClass : DaggerForAndroidTests(), FirstUse {
 *
 *     override lateinit var testProject: TestProject
 *     override lateinit var peopleRealmConfiguration
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
 *         super<FirstUse>.setUp()
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
interface FirstUse : FirstUseLocal {

    var testProject: TestProject

    override var peopleRealmConfiguration: RealmConfiguration

    override fun setUp() {

        testProject = RemoteTestingManager.create().createTestProject()

        val localDbKey = LocalDbKey(
            testProject.id,
            DEFAULT_REALM_KEY,
            testProject.legacyId)

        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
        peopleRealmConfiguration = PeopleRealmConfig.get(localDbKey.projectId, localDbKey.value, localDbKey.projectId)

        super.setUp()
    }

    override fun tearDown() {
        RemoteTestingManager.create().deleteTestProject(testProject.id)
        super.tearDown()
    }
}
