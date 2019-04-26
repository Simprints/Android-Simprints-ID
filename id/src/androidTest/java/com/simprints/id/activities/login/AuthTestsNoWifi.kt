//package com.simprints.fingerprint.integration.secure
//
//import androidx.test.core.app.ApplicationProvider
//import androidx.test.ext.junit.runners.AndroidJUnit4
//import androidx.test.filters.LargeTest
//import androidx.test.rule.ActivityTestRule
//import com.simprints.fingerprint.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_SECRET
//import com.simprints.fingerprint.commontesttools.DefaultTestConstants.DEFAULT_REALM_KEY
//import com.simprints.fingerprint.commontesttools.DefaultTestConstants.DEFAULT_TEST_CALLOUT_CREDENTIALS
//import com.simprints.fingerprint.commontesttools.di.TestAppModule
//import com.simprints.fingerprint.commontesttools.state.replaceRemoteDbManagerApiClientsWithFailingClients
//import com.simprints.fingerprint.commontesttools.state.replaceSecureApiClientWithFailingClientProvider
//import com.simprints.id.activities.login.ensureSignInFailure
//import com.simprints.id.activities.login.enterCredentialsDirectly
//import com.simprints.fingerprint.integration.testsnippets.launchActivityEnrol
//import com.simprints.id.activities.login.pressSignIn
//import com.simprints.fingerprint.testtools.AndroidTestConfig
//import com.simprints.fingerprint.testtools.state.setupRandomGeneratorToGenerateKey
//import com.simprints.id.Application
//import com.simprints.id.activities.checkLogin.openedByIntent.CheckLoginFromIntentActivity
//import com.simprints.id.data.db.remote.people.RemotePeopleManager
//import com.simprints.id.data.db.remote.sessions.RemoteSessionsManager
//import com.simprints.id.tools.RandomGenerator
//import com.simprints.testtools.common.di.DependencyRule.*
//import org.junit.Before
//import org.junit.Rule
//import org.junit.Test
//import org.junit.runner.RunWith
//import javax.inject.Inject
//
//@RunWith(AndroidJUnit4::class)
//@LargeTest
//class AuthTestsNoWifi { // TODO : Failing since Sessions Realm is being decrypted with wrong key
//
//    private val app = ApplicationProvider.getApplicationContext<Application>()
//
//    @get:Rule val loginTestRule = ActivityTestRule(CheckLoginFromIntentActivity::class.java, false, false)
//
//    @Inject lateinit var randomGeneratorMock: RandomGenerator
//    @Inject lateinit var remotePeopleManagerSpy: RemotePeopleManager
//    @Inject lateinit var remoteSessionsManagerSpy: RemoteSessionsManager
//
//    private val module by lazy {
//        TestAppModule(app,
//            randomGeneratorRule = MockRule,
//            remoteDbManagerRule = SpyRule,
//            remotePeopleManagerRule = SpyRule,
//            remoteSessionsManagerRule =  SpyRule,
//            secureApiInterfaceRule = ReplaceRule { replaceSecureApiClientWithFailingClientProvider() })
//    }
//
//    @Before
//    fun setUp() {
//        AndroidTestConfig(this, module).fullSetup()
//        setupRandomGeneratorToGenerateKey(DEFAULT_REALM_KEY, randomGeneratorMock)
//        replaceRemoteDbManagerApiClientsWithFailingClients(remotePeopleManagerSpy, remoteSessionsManagerSpy)
//    }
//
//    @Test
//    fun validCredentialsWithoutWifi_shouldFail() {
//        launchActivityEnrol(DEFAULT_TEST_CALLOUT_CREDENTIALS, loginTestRule)
//        enterCredentialsDirectly(DEFAULT_TEST_CALLOUT_CREDENTIALS, DEFAULT_PROJECT_SECRET)
//        pressSignIn()
//        ensureSignInFailure()
//    }
//}
