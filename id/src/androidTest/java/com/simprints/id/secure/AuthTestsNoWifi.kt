package com.simprints.id.secure

import android.support.test.InstrumentationRegistry
import android.support.test.rule.ActivityTestRule
import android.util.Base64
import com.simprints.id.Application
import com.simprints.id.activities.checkLogin.openedByIntent.CheckLoginFromIntentActivity
import com.simprints.id.data.db.local.models.LocalDbKey
import com.simprints.id.data.db.local.realm.RealmConfig
import com.simprints.id.testSnippets.*
import com.simprints.id.testTemplates.FirstUseLocal
import com.simprints.id.testTemplates.NoWifi
import com.simprints.id.testTools.CalloutCredentials
import io.realm.Realm
import io.realm.RealmConfiguration
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AuthTestsNoWifi : FirstUseLocal, NoWifi {

    private val calloutCredentials = CalloutCredentials(
        "EGkJFvCS7202A07I0fup",
        "the_one_and_only_module",
        "the_lone_user",
        "ec9a52bf-6803-4237-a647-6e76b133fc29")

    private val realmKey = Base64.decode("Jk1P0NPgwjViIhnvrIZTN3eIpjWRrok5zBZUw1CiQGGWhTFgnANiS87J6asyTksjCHe4SHJo0dHeawAPz3JtgQ==", Base64.NO_WRAP)
    private val localDbKey = LocalDbKey(
        calloutCredentials.projectId,
        realmKey,
        calloutCredentials.legacyApiKey)

    private val projectSecret = "orZje76yBgsjE2UWw/8jCtw/pBMgtURTfhO/4hZVP4vGnm1uji1OtwcRQkhn1MzQb5OoMjtu2xsbSIs40vSMEQ=="

    override var realmConfiguration: RealmConfiguration? = null

    @Rule
    @JvmField
    val loginTestRule = ActivityTestRule(CheckLoginFromIntentActivity::class.java, false, false)

    @Before
    override fun setUp() {
        super<NoWifi>.setUp()
        val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application
        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
        realmConfiguration = RealmConfig.get(localDbKey.projectId, localDbKey.value, localDbKey.projectId)

        super<FirstUseLocal>.setUp()

        mockSecureDataManagerToGenerateKey(app, realmKey)
    }

    @Test
    fun validCredentialsWithoutWifi_shouldFail() {
        launchAppFromIntentEnrol(calloutCredentials, loginTestRule)
        enterCredentialsDirectly(calloutCredentials, projectSecret)
        pressSignIn()
        ensureSignInFailure()
    }

    @Test
    fun validLegacyCredentialsWithoutWifi_shouldFail() {
        launchAppFromIntentEnrol(calloutCredentials.toLegacy(), loginTestRule)
        enterCredentialsDirectly(calloutCredentials, projectSecret)
        pressSignIn()
        ensureSignInFailure()
    }
}
