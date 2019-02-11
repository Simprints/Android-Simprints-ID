package com.simprints.id.secure

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import com.simprints.id.Application
import com.simprints.id.activities.checkLogin.openedByIntent.CheckLoginFromIntentActivity
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_SECRET
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_REALM_KEY
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_TEST_CALLOUT_CREDENTIALS
import com.simprints.id.commontesttools.di.DependencyRule.*
import com.simprints.id.commontesttools.di.TestAppModule
import com.simprints.id.commontesttools.state.replaceRemoteDbManagerApiClientsWithFailingClients
import com.simprints.id.commontesttools.state.replaceSecureApiClientWithFailingClientProvider
import com.simprints.id.data.db.remote.people.RemotePeopleManager
import com.simprints.id.data.db.remote.sessions.RemoteSessionsManager
import com.simprints.id.testSnippets.*
import com.simprints.id.testTemplates.FirstUseLocal
import com.simprints.id.testtools.AndroidTestConfig
import com.simprints.id.tools.RandomGenerator
import io.realm.Realm
import io.realm.RealmConfiguration
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@LargeTest
class AuthTestsNoWifi : FirstUseLocal {

    private val app = ApplicationProvider.getApplicationContext<Application>()

    override var peopleRealmConfiguration: RealmConfiguration? = null
    override var sessionsRealmConfiguration: RealmConfiguration? = null

    @Rule
    @JvmField
    val loginTestRule = ActivityTestRule(CheckLoginFromIntentActivity::class.java, false, false)

    @Inject lateinit var randomGeneratorMock: RandomGenerator

    @Inject lateinit var remotePeopleManagerSpy: RemotePeopleManager

    @Inject lateinit var remoteSessionsManagerSpy: RemoteSessionsManager

    private val module by lazy {
        TestAppModule(app,
            randomGeneratorRule = MockRule,
            remoteDbManagerRule = SpyRule,
            remotePeopleManagerRule = SpyRule,
            remoteSessionsManagerRule =  SpyRule,
            secureApiInterfaceRule = ReplaceRule { replaceSecureApiClientWithFailingClientProvider() })
    }

    @Before
    override fun setUp() {
        AndroidTestConfig(this, module).fullSetup()
        setupRandomGeneratorToGenerateKey(DEFAULT_REALM_KEY, randomGeneratorMock)
        replaceRemoteDbManagerApiClientsWithFailingClients(remotePeopleManagerSpy, remoteSessionsManagerSpy)

        Realm.init(app)
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
