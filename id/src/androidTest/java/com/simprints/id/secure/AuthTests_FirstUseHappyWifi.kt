package com.simprints.id.secure

import android.support.test.InstrumentationRegistry
import android.support.test.filters.LargeTest
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.util.Base64
import android.util.Base64.NO_WRAP
import com.simprints.id.*
import com.simprints.id.activities.checkLogin.openedByIntent.CheckLoginFromIntentActivity
import com.simprints.id.data.db.local.models.LocalDbKey
import com.simprints.id.data.db.local.realm.RealmConfig
import com.simprints.id.templates.FirstUseLocal
import com.simprints.id.templates.HappyWifi
import com.simprints.id.tools.AppUtils.getApp
import com.simprints.id.tools.CalloutCredentials
import io.realm.Realm
import io.realm.RealmConfiguration
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
@LargeTest
class AuthTests_FirstUseHappyWifi: FirstUseLocal, HappyWifi {

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
    val activityTestRule = ActivityTestRule(CheckLoginFromIntentActivity::class.java, false, false)

    @Before
    override fun setUp() {
        super<HappyWifi>.setUp()
        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
        realmConfiguration = RealmConfig.get(localDbKey.projectId, localDbKey.value)

        super<FirstUseLocal>.setUp()
    }

    @Test
    fun validLegacyCredentials_shouldSucceed() {
        launchAppFromIntentEnrol(calloutCredentials.toLegacy(), activityTestRule)
        enterCredentialsDirectly(calloutCredentials, projectSecret)
        pressSignIn()
        ensureSignInSuccess(activityTestRule)
        getApp(activityTestRule).dataManager.remoteDbManager.signOutOfRemoteDb()
    }

    @Test
    fun validCredentials_shouldSucceed() {
        launchAppFromIntentEnrol(calloutCredentials, activityTestRule)
        enterCredentialsDirectly(calloutCredentials, projectSecret)
        pressSignIn()
        ensureSignInSuccess(activityTestRule)
        getApp(activityTestRule).dataManager.remoteDbManager.signOutOfRemoteDb()
    }

    @Test
    fun invalidIntentLegacyProjectIdAndInvalidSubmittedProjectId_shouldFail() {
        launchAppFromIntentEnrol(invalidCredentials.toLegacy(), activityTestRule)
        enterCredentialsDirectly(invalidCredentials, projectSecret)
        pressSignIn()
        ensureSignInFailure(activityTestRule)
    }

    @Test
    fun validIntentLegacyProjectIdAndInvalidSubmittedProjectId_shouldFail() {
        launchAppFromIntentEnrol(calloutCredentials.toLegacy(), activityTestRule)
        enterCredentialsDirectly(invalidCredentials, projectSecret)
        pressSignIn()
        ensureSignInFailure(activityTestRule)
    }

    @Test
    fun invalidIntentProjectIdAndInvalidSubmittedProjectId_shouldFail() {
        launchAppFromIntentEnrol(invalidCredentials, activityTestRule)
        enterCredentialsDirectly(invalidCredentials, projectSecret)
        pressSignIn()
        ensureSignInFailure(activityTestRule)
    }

    @Test
    fun validIntentProjectIdAndInvalidSubmittedProjectId_shouldFail() {
        launchAppFromIntentEnrol(calloutCredentials, activityTestRule)
        enterCredentialsDirectly(invalidCredentials, projectSecret)
        pressSignIn()
        ensureSignInFailure(activityTestRule)
    }

    @Test
    fun validProjectIdAndInvalidSecret_shouldFail() {
        launchAppFromIntentEnrol(calloutCredentials, activityTestRule)
        enterCredentialsDirectly(calloutCredentials, invalidSecret)
        pressSignIn()
        ensureSignInFailure(activityTestRule)
    }

    @Test
    fun validLegacyProjectIdAndInvalidSecret_shouldFail() {
        launchAppFromIntentEnrol(calloutCredentials.toLegacy(), activityTestRule)
        enterCredentialsDirectly(calloutCredentials, invalidSecret)
        pressSignIn()
        ensureSignInFailure(activityTestRule)
    }

    @Test
    fun invalidCredentials_shouldFail() {
        launchAppFromIntentEnrol(invalidCredentials, activityTestRule)
        enterCredentialsDirectly(invalidCredentials, invalidSecret)
        pressSignIn()
        ensureSignInFailure(activityTestRule)
    }

    @Test
    fun invalidLegacyCredentials_shouldFail() {
        launchAppFromIntentEnrol(invalidCredentials.toLegacy(), activityTestRule)
        enterCredentialsDirectly(invalidCredentials, invalidSecret)
        pressSignIn()
        ensureSignInFailure(activityTestRule)
    }
}
