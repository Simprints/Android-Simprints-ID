package com.simprints.id.secure

import android.support.test.InstrumentationRegistry
import android.support.test.filters.LargeTest
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.util.Base64
import android.util.Base64.NO_WRAP
import com.simprints.mockscanner.MockBluetoothAdapter
import com.simprints.mockscanner.MockScannerManager
import com.simprints.id.Application
import com.simprints.id.activities.checkLogin.openedByIntent.CheckLoginFromIntentActivity
import com.simprints.id.data.db.local.models.LocalDbKey
import com.simprints.id.data.db.local.realm.RealmConfig
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.di.AppModuleForAndroidTests
import com.simprints.id.di.DaggerForAndroidTests
import com.simprints.id.shared.DependencyRule.*
import com.simprints.id.testSnippets.*
import com.simprints.id.testTemplates.FirstUseLocal
import com.simprints.id.testTemplates.HappyWifi
import com.simprints.id.testTools.CalloutCredentials
import com.simprints.id.tools.RandomGenerator
import com.simprints.id.tools.delegates.lazyVar
import io.realm.Realm
import io.realm.RealmConfiguration
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@LargeTest
class AuthTestsHappyWifi : FirstUseLocal, HappyWifi, DaggerForAndroidTests() {

    private val calloutCredentials = CalloutCredentials(
        "bWOFHInKA2YaQwrxZ7uJ",
        "the_one_and_only_module",
        "the_lone_user",
        "d95bacc0-7acb-4ff0-98b3-ae6ecbf7398f")

    private val realmKey = Base64.decode("Jk1P0NPgwjViIhnvrIZTN3eIpjWRrok5zBZUw1CiQGGWhTFgnANiS87J6asyTksjCHe4SHJo0dHeawAPz3JtgQ==", NO_WRAP)
    private val localDbKey = LocalDbKey(
        calloutCredentials.projectId,
        realmKey,
        calloutCredentials.legacyApiKey)

    private val invalidCredentials = CalloutCredentials(
        "beefdeadbeefdeadbeef",
        "the_one_and_only_module",
        "the_lone_user",
        "deadbeef-dead-beef-dead-deaddeadbeef"
    )

    private val projectSecret = "Z8nRspDoiQg1QpnDdKE6U7fQKa0GjpQOwnJ4OcSFWulAcIk4+LP9wrtDn8fRmqacLvkmtmOLl+Kxo1emXLsZ0Q=="

    private val invalidSecret = "deadbeefdeadbeefdeadbeefdeadbeefdeadbeefdeadbeefdeadbeefdeadbeefdeadbeefdeadbeefdeadbeef"

    override var realmConfiguration: RealmConfiguration? = null

    @Rule
    @JvmField
    val loginTestRule = ActivityTestRule<CheckLoginFromIntentActivity>(CheckLoginFromIntentActivity::class.java, false, false)

    @Inject
    lateinit var remoteDbManager: RemoteDbManager
    @Inject
    lateinit var randomGeneratorMock: RandomGenerator

    override var module by lazyVar {
        AppModuleForAndroidTests(app,
            randomGeneratorRule = MockRule(),
            bluetoothComponentAdapterRule = ReplaceRule { mockScannerManager })
    }

    private var mockScannerManager = MockBluetoothAdapter(MockScannerManager())

    @Before
    override fun setUp() {
        super<HappyWifi>.setUp()
        app = InstrumentationRegistry.getTargetContext().applicationContext as Application
        super<DaggerForAndroidTests>.setUp()
        testAppComponent.inject(this)
        setupRandomGeneratorToGenerateKey(realmKey, randomGeneratorMock)

        app.initDependencies()

        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
        realmConfiguration = RealmConfig.get(localDbKey.projectId, localDbKey.value, localDbKey.projectId)
        super<FirstUseLocal>.setUp()

        signOut()
    }

    @Test
    fun validLegacyCredentials_shouldSucceed() {
        launchAppFromIntentEnrol(calloutCredentials.toLegacy(), loginTestRule)
        enterCredentialsDirectly(calloutCredentials, projectSecret)
        pressSignIn()
        ensureSignInSuccess()
        signOut()
    }

    @Test
    fun validCredentials_shouldSucceed() {
        launchAppFromIntentEnrol(calloutCredentials, loginTestRule)
        enterCredentialsDirectly(calloutCredentials, projectSecret)
        pressSignIn()
        ensureSignInSuccess()
        signOut()
    }

    @Test
    fun invalidIntentLegacyProjectIdAndInvalidSubmittedProjectId_shouldFail() {
        launchAppFromIntentEnrol(invalidCredentials.toLegacy(), loginTestRule)
        enterCredentialsDirectly(invalidCredentials, projectSecret)
        pressSignIn()
        ensureSignInFailure()
    }

    @Test
    fun validIntentLegacyProjectIdAndInvalidSubmittedProjectId_shouldFail() {
        launchAppFromIntentEnrol(calloutCredentials.toLegacy(), loginTestRule)
        enterCredentialsDirectly(invalidCredentials, projectSecret)
        pressSignIn()
        ensureSignInFailure()
    }

    @Test
    fun invalidIntentProjectIdAndInvalidSubmittedProjectId_shouldFail() {
        launchAppFromIntentEnrol(invalidCredentials, loginTestRule)
        enterCredentialsDirectly(invalidCredentials, projectSecret)
        pressSignIn()
        ensureSignInFailure()
    }

    @Test
    fun validIntentProjectIdAndInvalidSubmittedProjectId_shouldFail() {
        launchAppFromIntentEnrol(calloutCredentials, loginTestRule)
        enterCredentialsDirectly(invalidCredentials, projectSecret)
        pressSignIn()
        ensureSignInFailure()
    }

    @Test
    fun validProjectIdAndInvalidSecret_shouldFail() {
        launchAppFromIntentEnrol(calloutCredentials, loginTestRule)
        enterCredentialsDirectly(calloutCredentials, invalidSecret)
        pressSignIn()
        ensureSignInFailure()
    }

    @Test
    fun validLegacyProjectIdAndInvalidSecret_shouldFail() {
        launchAppFromIntentEnrol(calloutCredentials.toLegacy(), loginTestRule)
        enterCredentialsDirectly(calloutCredentials, invalidSecret)
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
        enterCredentialsDirectly(invalidCredentials, projectSecret)
        pressSignIn()
        ensureSignInFailure()
    }

    @Test
    fun validCredentials_shouldPersistAcrossAppRestart() {
        launchAppFromIntentEnrolAndDoLogin(calloutCredentials, loginTestRule, projectSecret)

        launchAppFromIntentEnrol(calloutCredentials, loginTestRule)
        ensureSignInSuccess()
        signOut()
    }

    @Test
    fun validLegacyCredentials_shouldPersistAcrossAppRestart() {
        launchAppFromIntentEnrol(calloutCredentials.toLegacy(), loginTestRule)
        enterCredentialsDirectly(calloutCredentials, projectSecret)
        pressSignIn()
        ensureSignInSuccess()

        launchAppFromIntentEnrol(calloutCredentials.toLegacy(), loginTestRule)
        ensureSignInSuccess()
        signOut()
    }

    @Test
    fun validCredentialsThenRestartingWithInvalidCredentials_shouldFail() {
        launchAppFromIntentEnrolAndDoLogin(calloutCredentials, loginTestRule, projectSecret)

        launchAppFromIntentEnrol(invalidCredentials, loginTestRule)
        ensureConfigError()
        signOut()
    }

    @Test
    fun validLegacyCredentialsThenRestartingWithInvalidCredentials_shouldFail() {
        launchAppFromIntentEnrol(calloutCredentials.toLegacy(), loginTestRule)
        enterCredentialsDirectly(calloutCredentials, projectSecret)
        pressSignIn()
        ensureSignInSuccess()

        launchAppFromIntentEnrol(invalidCredentials.toLegacy(), loginTestRule)
        ensureConfigError()
        signOut()
    }

    private fun signOut() {
        remoteDbManager.signOutOfRemoteDb()
    }
}
