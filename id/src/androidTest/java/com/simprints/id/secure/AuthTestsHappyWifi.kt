package com.simprints.id.secure

import android.support.test.InstrumentationRegistry
import android.support.test.filters.LargeTest
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.util.Base64
import android.util.Base64.NO_WRAP
import com.simprints.id.activities.checkLogin.openedByIntent.CheckLoginFromIntentActivity
import com.simprints.id.data.db.local.models.LocalDbKey
import com.simprints.id.data.db.local.realm.RealmConfig
import com.simprints.id.testTemplates.FirstUseLocal
import com.simprints.id.testTemplates.HappyWifi
import com.simprints.id.testSnippets.*
import com.simprints.id.testTemplates.HappyBluetooth
import com.simprints.id.testTools.AppUtils.getApp
import com.simprints.id.testTools.CalloutCredentials
import io.realm.Realm
import io.realm.RealmConfiguration
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
@LargeTest
class AuthTestsHappyWifi: FirstUseLocal, HappyWifi, HappyBluetooth {

    private val calloutCredentials = CalloutCredentials(
        "EGkJFvCS7202A07I0fup",
        "the_one_and_only_module",
        "the_lone_user",
        "ec9a52bf-6803-4237-a647-6e76b133fc29")

    private val localDbKey = LocalDbKey(
        calloutCredentials.projectId,
        Base64.decode("Jk1P0NPgwjViIhnvrIZTN3eIpjWRrok5zBZUw1CiQGGWhTFgnANiS87J6asyTksjCHe4SHJo0dHeawAPz3JtgQ==", NO_WRAP),
        calloutCredentials.legacyApiKey)

    private val invalidCredentials = CalloutCredentials(
        "beefdeadbeefdeadbeef",
        "the_one_and_only_module",
        "the_lone_user",
        "deadbeef-dead-beef-dead-deaddeadbeef"
    )

    private val projectSecret = "orZje76yBgsjE2UWw/8jCtw/pBMgtURTfhO/4hZVP4vGnm1uji1OtwcRQkhn1MzQb5OoMjtu2xsbSIs40vSMEQ=="

    private val invalidSecret = "deadbeefdeadbeefdeadbeefdeadbeefdeadbeefdeadbeefdeadbeefdeadbeefdeadbeefdeadbeefdeadbeef"

    override var realmConfiguration: RealmConfiguration? = null

    @Rule
    @JvmField
    val loginTestRule = ActivityTestRule(CheckLoginFromIntentActivity::class.java, false, false)

    @Before
    override fun setUp() {
        super<HappyBluetooth>.setUp()
        super<HappyWifi>.setUp()
        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
        realmConfiguration = RealmConfig.get(localDbKey.projectId, localDbKey.value)

        super<FirstUseLocal>.setUp()
    }

    @Test
    fun validLegacyCredentials_shouldSucceed() {
        launchAppFromIntentEnrol(calloutCredentials.toLegacy(), loginTestRule)
        enterCredentialsDirectly(calloutCredentials, projectSecret)
        pressSignIn()
        ensureSignInSuccess()
        signOut(loginTestRule)
    }

    @Test
    fun validCredentials_shouldSucceed() {
        launchAppFromIntentEnrol(calloutCredentials, loginTestRule)
        enterCredentialsDirectly(calloutCredentials, projectSecret)
        pressSignIn()
        ensureSignInSuccess()
        signOut(loginTestRule)
    }

    @Test
    fun invalidIntentLegacyProjectIdAndInvalidSubmittedProjectId_shouldFail() {
        launchAppFromIntentEnrol(invalidCredentials.toLegacy(), loginTestRule)
        ensureConfigError()
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
        ensureConfigError()
    }

    @Test
    fun validCredentials_shouldPersistAcrossAppRestart() {
        launchAppFromIntentEnrol(calloutCredentials, loginTestRule)
        enterCredentialsDirectly(calloutCredentials, projectSecret)
        pressSignIn()
        ensureSignInSuccess()

        launchAppFromIntentEnrol(calloutCredentials, loginTestRule)
        ensureSignInSuccess()
        signOut(loginTestRule)
    }

    @Test
    fun validLegacyCredentials_shouldPersistAcrossAppRestart() {
        launchAppFromIntentEnrol(calloutCredentials.toLegacy(), loginTestRule)
        enterCredentialsDirectly(calloutCredentials, projectSecret)
        pressSignIn()
        ensureSignInSuccess()

        launchAppFromIntentEnrol(calloutCredentials.toLegacy(), loginTestRule)
        ensureSignInSuccess()
        signOut(loginTestRule)
    }

    @Test
    fun validCredentialsThenRestartingWithInvalidCredentials_shouldFail() {
        launchAppFromIntentEnrol(calloutCredentials, loginTestRule)
        enterCredentialsDirectly(calloutCredentials, projectSecret)
        pressSignIn()
        ensureSignInSuccess()

        launchAppFromIntentEnrol(invalidCredentials, loginTestRule)
        ensureConfigError()
        signOut(loginTestRule)
    }

    @Test
    fun validLegacyCredentialsThenRestartingWithInvalidCredentials_shouldFail() {
        launchAppFromIntentEnrol(calloutCredentials.toLegacy(), loginTestRule)
        enterCredentialsDirectly(calloutCredentials, projectSecret)
        pressSignIn()
        ensureSignInSuccess()

        launchAppFromIntentEnrol(invalidCredentials.toLegacy(), loginTestRule)
        ensureConfigError()
        signOut(loginTestRule)
    }

    private fun signOut(activityTestRule: ActivityTestRule<*>) {
        getApp(activityTestRule).dataManager.remoteDbManager.signOutOfRemoteDb()
    }
}
