package com.simprints.id.secure

import androidx.test.InstrumentationRegistry
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import com.simprints.id.Application
import com.simprints.id.activities.checkLogin.openedByIntent.CheckLoginFromIntentActivity
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.di.AppModuleForAndroidTests
import com.simprints.id.di.DaggerForAndroidTests
import com.simprints.id.shared.DefaultTestConstants.DEFAULT_PROJECT_SECRET
import com.simprints.id.shared.DefaultTestConstants.DEFAULT_REALM_KEY
import com.simprints.id.shared.DefaultTestConstants.DEFAULT_TEST_CALLOUT_CREDENTIALS
import com.simprints.id.shared.DependencyRule.*
import com.simprints.id.shared.replaceRemoteDbManagerApiClientsWithFailingClients
import com.simprints.id.shared.replaceSecureApiClientWithFailingClientProvider
import com.simprints.id.testSnippets.*
import com.simprints.id.testTemplates.FirstUseLocal
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

    override var peopleRealmConfiguration: RealmConfiguration? = null
    override var sessionsRealmConfiguration: RealmConfiguration? = null

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
        setupRandomGeneratorToGenerateKey(DEFAULT_REALM_KEY, randomGeneratorMock)
        replaceRemoteDbManagerApiClientsWithFailingClients(remoteDbManagerSpy)

        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
        peopleRealmConfiguration = FirstUseLocal.defaultPeopleRealmConfiguration
        sessionsRealmConfiguration = FirstUseLocal.defaultSessionRealmConfiguration
        super<FirstUseLocal>.setUp()
    }

    @Test
    fun validCredentialsWithoutWifi_shouldFail() {
        launchAppFromIntentEnrol(DEFAULT_TEST_CALLOUT_CREDENTIALS, loginTestRule)
        enterCredentialsDirectly(DEFAULT_TEST_CALLOUT_CREDENTIALS, DEFAULT_PROJECT_SECRET)
        pressSignIn()
        ensureSignInFailure()
    }

    @Test
    fun validLegacyCredentialsWithoutWifi_shouldFail() {
        launchAppFromIntentEnrol(DEFAULT_TEST_CALLOUT_CREDENTIALS.toLegacy(), loginTestRule)
        enterCredentialsDirectly(DEFAULT_TEST_CALLOUT_CREDENTIALS, DEFAULT_PROJECT_SECRET)
        pressSignIn()
        ensureSignInFailure()
    }
}
