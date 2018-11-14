package com.simprints.id.secure

import android.support.test.InstrumentationRegistry
import android.support.test.filters.LargeTest
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.simprints.id.Application
import com.simprints.id.activities.checkLogin.openedByIntent.CheckLoginFromIntentActivity
import com.simprints.id.data.db.local.realm.PeopleRealmConfig
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.di.AppModuleForAndroidTests
import com.simprints.id.di.DaggerForAndroidTests
import com.simprints.id.shared.DependencyRule.MockRule
import com.simprints.id.shared.DependencyRule.ReplaceRule
import com.simprints.id.testSnippets.*
import com.simprints.id.testTemplates.FirstUseLocal
import com.simprints.id.testTools.DEFAULT_LOCAL_DB_KEY
import com.simprints.id.testTools.DEFAULT_PROJECT_SECRET
import com.simprints.id.testTools.DEFAULT_REALM_KEY
import com.simprints.id.testTools.DEFAULT_TEST_CALLOUT_CREDENTIALS
import com.simprints.id.testTools.models.TestCalloutCredentials
import com.simprints.id.tools.RandomGenerator
import com.simprints.id.tools.delegates.lazyVar
import com.simprints.mockscanner.MockBluetoothAdapter
import com.simprints.mockscanner.MockScannerManager
import io.realm.Realm
import io.realm.RealmConfiguration
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@LargeTest
class AuthTestsHappyWifi : FirstUseLocal, DaggerForAndroidTests() {

    private val invalidCredentials = TestCalloutCredentials(
        "beefdeadbeefdeadbeef",
        "the_one_and_only_module",
        "the_lone_user",
        "deadbeef-dead-beef-dead-deaddeadbeef"
    )

    private val invalidSecret = "deadbeefdeadbeefdeadbeefdeadbeefdeadbeefdeadbeefdeadbeefdeadbeefdeadbeefdeadbeefdeadbeef"

    override lateinit var peopleRealmConfiguration: RealmConfiguration

    @Rule
    @JvmField
    val loginTestRule = ActivityTestRule<CheckLoginFromIntentActivity>(CheckLoginFromIntentActivity::class.java, false, false)

    @Inject lateinit var remoteDbManager: RemoteDbManager
    @Inject lateinit var randomGeneratorMock: RandomGenerator

    override var module by lazyVar {
        AppModuleForAndroidTests(app,
            randomGeneratorRule = MockRule,
            bluetoothComponentAdapterRule = ReplaceRule { mockBluetoothAdapter })
    }

    private val mockBluetoothAdapter = MockBluetoothAdapter(MockScannerManager())

    @Before
    override fun setUp() {
        app = InstrumentationRegistry.getTargetContext().applicationContext as Application
        super<DaggerForAndroidTests>.setUp()
        testAppComponent.inject(this)
        setupRandomGeneratorToGenerateKey(DEFAULT_REALM_KEY, randomGeneratorMock)

        app.initDependencies()

        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
        peopleRealmConfiguration = PeopleRealmConfig.get(DEFAULT_LOCAL_DB_KEY.projectId, DEFAULT_LOCAL_DB_KEY.value, DEFAULT_LOCAL_DB_KEY.projectId)
        super<FirstUseLocal>.setUp()

        signOut()
    }

    @Test
    fun validLegacyCredentials_shouldSucceed() {
        launchAppFromIntentEnrol(DEFAULT_TEST_CALLOUT_CREDENTIALS.toLegacy(), loginTestRule)
        enterCredentialsDirectly(DEFAULT_TEST_CALLOUT_CREDENTIALS, DEFAULT_PROJECT_SECRET)
        pressSignIn()
        ensureSignInSuccess()
        signOut()
    }

    @Test
    fun validCredentials_shouldSucceed() {
        launchAppFromIntentEnrol(DEFAULT_TEST_CALLOUT_CREDENTIALS, loginTestRule)
        enterCredentialsDirectly(DEFAULT_TEST_CALLOUT_CREDENTIALS, DEFAULT_PROJECT_SECRET)
        pressSignIn()
        ensureSignInSuccess()
        signOut()
    }

    @Test
    fun invalidIntentLegacyProjectIdAndInvalidSubmittedProjectId_shouldFail() {
        launchAppFromIntentEnrol(invalidCredentials.toLegacy(), loginTestRule)
        enterCredentialsDirectly(invalidCredentials, DEFAULT_PROJECT_SECRET)
        pressSignIn()
        ensureSignInFailure()
    }

    @Test
    fun validIntentLegacyProjectIdAndInvalidSubmittedProjectId_shouldFail() {
        launchAppFromIntentEnrol(DEFAULT_TEST_CALLOUT_CREDENTIALS.toLegacy(), loginTestRule)
        enterCredentialsDirectly(invalidCredentials, DEFAULT_PROJECT_SECRET)
        pressSignIn()
        ensureSignInFailure()
    }

    @Test
    fun invalidIntentProjectIdAndInvalidSubmittedProjectId_shouldFail() {
        launchAppFromIntentEnrol(invalidCredentials, loginTestRule)
        enterCredentialsDirectly(invalidCredentials, DEFAULT_PROJECT_SECRET)
        pressSignIn()
        ensureSignInFailure()
    }

    @Test
    fun validIntentProjectIdAndInvalidSubmittedProjectId_shouldFail() {
        launchAppFromIntentEnrol(DEFAULT_TEST_CALLOUT_CREDENTIALS, loginTestRule)
        enterCredentialsDirectly(invalidCredentials, DEFAULT_PROJECT_SECRET)
        pressSignIn()
        ensureSignInFailure()
    }

    @Test
    fun validProjectIdAndInvalidSecret_shouldFail() {
        launchAppFromIntentEnrol(DEFAULT_TEST_CALLOUT_CREDENTIALS, loginTestRule)
        enterCredentialsDirectly(DEFAULT_TEST_CALLOUT_CREDENTIALS, invalidSecret)
        pressSignIn()
        ensureSignInFailure()
    }

    @Test
    fun validLegacyProjectIdAndInvalidSecret_shouldFail() {
        launchAppFromIntentEnrol(DEFAULT_TEST_CALLOUT_CREDENTIALS.toLegacy(), loginTestRule)
        enterCredentialsDirectly(DEFAULT_TEST_CALLOUT_CREDENTIALS, invalidSecret)
        pressSignIn()
        ensureSignInFailure()
    }

    @Test
    fun invalidCredentials_shouldFail() {
        launchAppFromIntentEnrol(invalidCredentials, loginTestRule)
        enterCredentialsDirectly(invalidCredentials, invalidSecret)
        pressSignIn()
        ensureSignInFailure()
    }

    @Test
    fun invalidLegacyCredentials_shouldFail() {
        launchAppFromIntentEnrol(invalidCredentials.toLegacy(), loginTestRule)
        enterCredentialsDirectly(invalidCredentials, DEFAULT_PROJECT_SECRET)
        pressSignIn()
        ensureSignInFailure()
    }

    @Test
    fun validCredentials_shouldPersistAcrossAppRestart() {
        launchAppFromIntentEnrolAndDoLogin(DEFAULT_TEST_CALLOUT_CREDENTIALS, loginTestRule, DEFAULT_PROJECT_SECRET)

        launchAppFromIntentEnrol(DEFAULT_TEST_CALLOUT_CREDENTIALS, loginTestRule)
        ensureSignInSuccess()
        signOut()
    }

    @Test
    fun validLegacyCredentials_shouldPersistAcrossAppRestart() {
        launchAppFromIntentEnrol(DEFAULT_TEST_CALLOUT_CREDENTIALS.toLegacy(), loginTestRule)
        enterCredentialsDirectly(DEFAULT_TEST_CALLOUT_CREDENTIALS, DEFAULT_PROJECT_SECRET)
        pressSignIn()
        ensureSignInSuccess()

        launchAppFromIntentEnrol(DEFAULT_TEST_CALLOUT_CREDENTIALS.toLegacy(), loginTestRule)
        ensureSignInSuccess()
        signOut()
    }

    @Test
    fun validCredentialsThenRestartingWithInvalidCredentials_shouldFail() {
        launchAppFromIntentEnrolAndDoLogin(DEFAULT_TEST_CALLOUT_CREDENTIALS, loginTestRule, DEFAULT_PROJECT_SECRET)

        launchAppFromIntentEnrol(invalidCredentials, loginTestRule)
        ensureConfigError()
        signOut()
    }

    @Test
    fun validLegacyCredentialsThenRestartingWithInvalidCredentials_shouldFail() {
        launchAppFromIntentEnrol(DEFAULT_TEST_CALLOUT_CREDENTIALS.toLegacy(), loginTestRule)
        enterCredentialsDirectly(DEFAULT_TEST_CALLOUT_CREDENTIALS, DEFAULT_PROJECT_SECRET)
        pressSignIn()
        ensureSignInSuccess()

        launchAppFromIntentEnrol(invalidCredentials.toLegacy(), loginTestRule)
        ensureConfigError()
        signOut()
    }

    @After
    override fun tearDown() {
        super.tearDown()
    }

    private fun signOut() {
        remoteDbManager.signOutOfRemoteDb()
    }
}
