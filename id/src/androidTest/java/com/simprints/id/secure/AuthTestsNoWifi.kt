package com.simprints.id.secure

import android.support.test.InstrumentationRegistry
import android.support.test.filters.LargeTest
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.simprints.id.Application
import com.simprints.id.activities.checkLogin.openedByIntent.CheckLoginFromIntentActivity
import com.simprints.id.data.db.local.models.LocalDbKey
import com.simprints.id.data.db.local.realm.PeopleRealmConfig
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.di.AppModuleForAndroidTests
import com.simprints.id.di.DaggerForAndroidTests
import com.simprints.id.shared.DependencyRule.*
import com.simprints.id.shared.replaceRemoteDbManagerApiClientsWithFailingClients
import com.simprints.id.shared.replaceSecureApiClientWithFailingClientProvider
import com.simprints.id.testSnippets.*
import com.simprints.id.testTemplates.FirstUseLocal
import com.simprints.id.testTemplates.FirstUseLocal.Companion.sessionsRealmKey
import com.simprints.id.testTools.models.TestCalloutCredentials
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
class AuthTestsNoWifi : FirstUseLocal, DaggerForAndroidTests() {

    private val calloutCredentials = TestCalloutCredentials(
        "bWOFHInKA2YaQwrxZ7uJ",
        "the_one_and_only_module",
        "the_lone_user",
        "d95bacc0-7acb-4ff0-98b3-ae6ecbf7398f")

    private val localDbKey = LocalDbKey(
        calloutCredentials.projectId,
        sessionsRealmKey,
        calloutCredentials.legacyApiKey)

    private val projectSecret = "Z8nRspDoiQg1QpnDdKE6U7fQKa0GjpQOwnJ4OcSFWulAcIk4+LP9wrtDn8fRmqacLvkmtmOLl+Kxo1emXLsZ0Q=="

    override lateinit var peopleRealmConfiguration: RealmConfiguration

    @Rule
    @JvmField
    val loginTestRule = ActivityTestRule(CheckLoginFromIntentActivity::class.java, false, false)

    @Inject lateinit var randomGeneratorMock: RandomGenerator

    @Inject lateinit var remoteDbManagerSpy: RemoteDbManager

    override var module by lazyVar {
        AppModuleForAndroidTests(app,
            randomGeneratorRule = MockRule,
            remoteDbManagerRule = SpyRule,
            secureApiInterfaceRule = ReplaceRule { replaceSecureApiClientWithFailingClientProvider() })
    }

    @Before
    override fun setUp() {
        app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application
        super<DaggerForAndroidTests>.setUp()
        testAppComponent.inject(this)
        setupRandomGeneratorToGenerateKey(sessionsRealmKey, randomGeneratorMock)
        replaceRemoteDbManagerApiClientsWithFailingClients(remoteDbManagerSpy)

        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
        peopleRealmConfiguration = PeopleRealmConfig.get(localDbKey.projectId, localDbKey.value, localDbKey.projectId)

        super<FirstUseLocal>.setUp()
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
